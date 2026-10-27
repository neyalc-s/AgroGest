#!/usr/bin/env python3
"""Casos adicionais de concorrência, formulários antigos e recuperação.
Requisitos: Linux, Java 17, Python 3 padrão, portas 8081/8082/8105/8106 livres.
"""
from pathlib import Path
from html.parser import HTMLParser
from decimal import Decimal
from concurrent.futures import ThreadPoolExecutor
import hashlib
import json
import os
import re
import shutil
import subprocess
import tempfile
import time
import urllib.error
import urllib.request
import test_http as h

ROOT = Path(__file__).resolve().parents[1]
checks = []


def check(name, value):
    if not value:
        raise AssertionError(name)
    checks.append(name)
    print('OK:', name, flush=True)


class Formularios(HTMLParser):
    def __init__(self, text):
        super().__init__(convert_charrefs=True)
        self.forms = []
        self.current = None
        self.select = None
        self.textarea = None
        self.feed(text)

    def handle_starttag(self, tag, attrs):
        a = dict(attrs)
        if tag == 'form':
            self.current = {'action': a.get('action', ''), 'data': {}}
        if self.current is None:
            return
        if tag == 'input' and a.get('name'):
            self.current['data'][a['name']] = a.get('value', '')
        if tag == 'select':
            self.select = a.get('name')
        if tag == 'option' and self.select:
            values = self.current['data']
            if self.select not in values or 'selected' in a:
                values[self.select] = a.get('value', '')
        if tag == 'textarea':
            self.textarea = a.get('name')
            self.current['data'][self.textarea] = ''

    def handle_data(self, text):
        if self.current and self.textarea:
            self.current['data'][self.textarea] += text

    def handle_endtag(self, tag):
        if tag == 'form' and self.current is not None:
            self.forms.append(self.current)
            self.current = None
        if tag == 'select':
            self.select = None
        if tag == 'textarea':
            self.textarea = None


def edit(browser, product):
    text = browser.request('/admin/produtos?editar=' + str(product))[2]
    for form in Formularios(text).forms:
        if form['data'].get('acao') == 'guardar':
            return form['data']
    raise AssertionError('Formulário de edição em falta')


def submit_edit(browser, form):
    return browser.request('/admin/produtos', form)


def create_product(admin, name, price='10', stock='10', expiry=''):
    r = admin.post('/admin/produtos', acao='guardar', nome=name, categoria='FERRAMENTA', fornecedorId=1, preco=price, quantidade=stock, unidade='unidade', descricao='Produto dos testes de revisão', foto='', validade=expiry)
    return h.entity_id(r[2], name)


def invoices(admin):
    return set(re.findall(r'factura\?id=(\d+)', admin.request('/admin/compras')[2]))


def second_instance(data, folder):
    base = folder / 'segundo-tomcat'
    shutil.copytree(ROOT/'runtime/tomcat/conf', base/'conf')
    for name in ['webapps', 'logs', 'temp', 'work']:
        (base/name).mkdir(exist_ok=True)
    shutil.copy2(ROOT/'dist/agrogest.war', base/'webapps/agrogest.war')
    xml = base/'conf/server.xml'
    xml.write_text(xml.read_text().replace('8081', '8082').replace('8105', '8106'))
    env = os.environ.copy()
    env.update(CATALINA_HOME=str(ROOT/'runtime/tomcat'), CATALINA_BASE=str(base), AGROGEST_DATA_DIR=str(data))
    log = (folder/'segunda-instancia.log').open('w')
    process = subprocess.Popen(['bash', str(ROOT/'runtime/tomcat/bin/catalina.sh'), 'run'], env=env, stdout=log, stderr=log)
    try:
        opener = urllib.request.build_opener(urllib.request.ProxyHandler({}))
        status = None
        for _ in range(150):
            if process.poll() is not None:
                break
            try:
                status = opener.open('http://127.0.0.1:8082/agrogest/login', timeout=2).status
            except urllib.error.HTTPError as e:
                status = e.code
            except OSError:
                pass
            if status is not None:
                break
            time.sleep(.1)
        check('Segunda instância sobre os mesmos dados não inicia a aplicação', status == 404)
    finally:
        if process.poll() is None:
            process.terminate()
            process.wait(timeout=20)
        log.close()
    logs = (folder/'segunda-instancia.log').read_text()
    logs += '\n'.join(p.read_text() for p in (base/'logs').glob('*.log'))
    check('Log explica a pasta já em uso', 'pasta de dados já está em uso' in logs)


def suite(admin, client):
    old = edit(admin, 1)
    initial = Decimal(old['quantidade'])
    client.post('/carrinho/adicionar', acao='adicionar', id=1, quantidade='1')
    client.request('/compra')
    check('Compra durante edição concluída', '/factura?id=' in client.post('/compra')[1])
    r = submit_edit(admin, old)
    check('Formulário anterior à compra é recusado', 'alterado desde que abriu' in r[2])
    check('Stock vendido não reaparece', Decimal(edit(admin, 1)['quantidade']) == initial - 1)
    fresh = edit(admin, 1)
    fresh['preco'] = '85.75'
    check('Edição reaberta é aceite', 'Operação concluída' in submit_edit(admin, fresh)[2])
    check('Alterar preço conserva stock', Decimal(edit(admin, 1)['quantidade']) == initial - 1)
    first = edit(admin, 1)
    other_tab = dict(first)
    first['preco'] = '86'
    submit_edit(admin, first)
    other_tab['preco'] = '1'
    check('Dois separadores antigos não sobrepõem edições', 'alterado desde que abriu' in submit_edit(admin, other_tab)[2])
    check('Preço da primeira edição mantido', Decimal(edit(admin, 1)['preco']) == 86)
    before_restock = edit(admin, 1)
    admin.post('/admin/produtos', acao='repor', id=1, quantidade='5')
    check('Reposição invalida formulário anterior', 'alterado desde que abriu' in submit_edit(admin, before_restock)[2])
    check('Reposição conservada', Decimal(edit(admin, 1)['quantidade']) == initial + 4)
    no_version = edit(admin, 1)
    del no_version['versao']
    check('Edição sem versão é recusada', 'Reabra a edição' in submit_edit(admin, no_version)[2])
    client.post('/carrinho/adicionar', acao='adicionar', id=3, quantidade='2')
    client.request('/compra')
    r = client.post('/compra', preco='0', total='0', clienteId='3')
    check('Preços e cliente enviados pelo browser não alteram a venda', '900,00' in r[2] and 'João Silva' in r[2] and 'Ana António' not in r[2])
    client.post('/carrinho/adicionar', acao='adicionar', id=3, quantidade='1')
    client.request('/compra')
    before = invoices(admin)
    with ThreadPoolExecutor(max_workers=2) as pool:
        results = list(pool.map(lambda _: client.post('/compra'), range(2)))
    check('Dois envios simultâneos do mesmo carrinho criam uma só factura', len(invoices(admin)-before) == 1)
    check('Só um dos envios abre uma nova factura', sum('/factura?id=' in r[1] for r in results) == 1)
    last = create_product(admin, 'Última unidade da revisão', '100', '1')
    ana = h.Browser()
    ana.login('ana@teste.mz', 'cliente123')
    client.post('/carrinho/adicionar', acao='adicionar', id=last, quantidade='1')
    ana.post('/carrinho/adicionar', acao='adicionar', id=last, quantidade='1')
    client.request('/compra')
    ana.request('/compra')
    before = invoices(admin)
    with ThreadPoolExecutor(max_workers=2) as pool:
        a = pool.submit(client.post, '/compra')
        b = pool.submit(ana.post, '/compra')
        results = [a.result(), b.result()]
    check('Dois clientes HTTP disputam a última unidade', sum('/factura?id=' in r[1] for r in results) == 1)
    check('Só uma factura na disputa de stock', len(invoices(admin)-before) == 1)
    check('Stock final zero e nunca negativo', Decimal(edit(admin, last)['quantidade']) == 0)
    check('Cliente que perdeu recebe indisponibilidade', any('stock insuficiente' in r[2].lower() for r in results))
    client.post('/carrinho/esvaziar', acao='esvaziar')
    for name, price, stock in [('ZX-A','30','2'),('ZX-B','10','5'),('ZX-C','20','0')]:
        create_product(admin, name, price, stock)
    html = client.request('/catalogo?q=ZX-&ordem=preco')[2]
    check('Ordenação por preço confere com os valores', html.index('ZX-B') < html.index('ZX-C') < html.index('ZX-A'))
    html = client.request('/catalogo?q=ZX-&ordem=stock')[2]
    check('Ordenação por stock confere com os valores', html.index('ZX-B') < html.index('ZX-A') < html.index('ZX-C'))
    check('Armazém exclui stock zero', 'ZX-C' not in admin.request('/admin/armazem')[2])
    expired = create_product(admin, 'Expirado da revisão', '10', '5', '2020-01-01')
    r = client.post('/carrinho/adicionar', acao='adicionar', id=expired, quantidade='1')
    check('Produto expirado recusado no servidor', 'fora de validade' in r[2])
    r = admin.post('/admin/produtos', acao='guardar', nome='Foto insegura', categoria='SEMENTE', fornecedorId=1, preco='1', quantidade='1', unidade='kg', descricao='Teste', foto='javascript:alert(1)', validade='')
    check('URL de foto inválido rejeitado', 'URL HTTP/HTTPS válido' in r[2])
    check('Preço negativo rejeitado', 'número positivo' in admin.post('/admin/produtos', acao='guardar', nome='Preço negativo', categoria='SEMENTE', fornecedorId=1, preco='-1', quantidade='1', unidade='kg', descricao='Teste', foto='', validade='')[2])
    check('Quantidade negativa rejeitada no carrinho', 'número positivo' in client.post('/carrinho/adicionar', acao='adicionar', id=1, quantidade='-1')[2])
    return client


def main():
    with tempfile.TemporaryDirectory(prefix='agrogest-revisao-') as directory:
        folder = Path(directory)
        data = folder/'dados'
        with (folder/'tomcat.log').open('w') as log:
            process = None
            try:
                process = h.start(data, log)
                admin, client = h.Browser(), h.Browser()
                admin.login('admin@agro.mz','admin234')
                client.login('joao@teste.mz','cliente123')
                suite(admin, client)
                admin.post('/admin/persistencia', acao='gravar')
                saved = hashlib.sha256((data/'agrogest.ser').read_bytes()).digest()
                second_instance(data, folder)
                check('Arranque recusado não altera o estado guardado', saved == hashlib.sha256((data/'agrogest.ser').read_bytes()).digest())
                check('Primeira instância continua funcional', admin.request('/admin')[0] == 200)
                admin.post('/admin/fornecedores', acao='criar', nome='Só no principal', tipo='SEMENTES', contacto='84', provincia='Nampula')
                admin.post('/admin/persistencia', acao='gravar')
                (data/'agrogest.ser').write_text('principal danificado no teste')
                process.kill()  # Simula falha abrupta, sem gravação pelo listener.
                process.wait(timeout=20)
                process = h.start(data, log)
                admin = h.Browser()
                admin.login('admin@agro.mz','admin234')
                check('Reinício após falha abrupta recupera backup', 'ZX-A' in admin.request('/admin/produtos')[2])
                check('Recuperação usa a versão anterior válida', 'Só no principal' not in admin.request('/admin/fornecedores')[2])
                admin.post('/admin/persistencia', acao='gravar')
                check('Estado recuperado pode ser gravado novamente', (data/'agrogest.ser').read_bytes().startswith(bytes([0xac,0xed])))
                log.flush()
                check('Recuperação por backup é indicada no log', 'estado recuperado do backup anterior' in (folder/'tomcat.log').read_text())
            except Exception:
                log.flush()
                print((folder/'tomcat.log').read_text()[-10000:])
                raise
            finally:
                if process is not None and process.poll() is None:
                    h.stop(process)
    print(json.dumps({'verificacoes': len(checks), 'resultado': 'PASSOU'}, ensure_ascii=False))


if __name__ == '__main__':
    main()
