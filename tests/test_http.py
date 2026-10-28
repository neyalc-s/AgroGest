#!/usr/bin/env python3
"""Teste de integração real: inicia Tomcat com dados temporários, testa e encerra.
Requisitos: Linux, Python 3 (só biblioteca padrão) e Java 17 no PATH.
Executar a partir de qualquer pasta: python3 tests/test_http.py
"""
from pathlib import Path
import html
import http.cookiejar
import json
import os
import re
import socket
import subprocess
import tempfile
import time
import urllib.error
import urllib.parse
import urllib.request

ROOT = Path(__file__).resolve().parents[1]
BASE = 'http://127.0.0.1:8081/agrogest'


class Browser:
    def __init__(self):
        self.opener = urllib.request.build_opener(urllib.request.ProxyHandler({}), urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()))
        self.token = ''
        self.confirmacao_compra = ''

    def request(self, route, data=None):
        payload = None if data is None else urllib.parse.urlencode(data).encode()
        try:
            response = self.opener.open(BASE + route, payload, timeout=25)
        except urllib.error.HTTPError as error:
            response = error
        text = response.read().decode('utf-8')
        match = re.search(r'name="csrf" value="([^"]+)"', text)
        if match:
            self.token = match.group(1)
        confirmation = re.search(r'name="confirmacaoCompra" value="([^"]+)"', text)
        if confirmation:
            self.confirmacao_compra = confirmation.group(1)
        return response.code, response.url, text

    def post(self, route, **data):
        if route == '/compra':
            data = {'confirmacaoCompra': self.confirmacao_compra, **data}
        return self.request(route, {'csrf': self.token, **data})

    def login(self, email, password):
        self.request('/login')
        result = self.post('/login', email=email, senha=password)
        assert result[0] == 200 and not result[1].endswith('/login'), result[:2]
        return result


checks = []


def check(name, condition):
    if not condition:
        raise AssertionError(name)
    checks.append(name)
    print('OK:', name, flush=True)


def plain(response):
    return html.unescape(re.sub(r'<[^>]+>', ' ', response[2]))


def entity_id(text, name):
    for row in re.findall(r'<tr>.*?</tr>', text, flags=re.S):
        if name in html.unescape(row):
            match = re.search(r'name="id" value="(\d+)"', row)
            if match:
                return int(match.group(1))
    raise AssertionError('ID não encontrado para ' + name)


def start(data_dir, log):
    # Não executar os pedidos de teste contra uma instância que o utilizador já usa.
    with socket.socket() as probe:
        probe.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        try:
            probe.bind(('127.0.0.1', 8081))
        except OSError:
            raise RuntimeError('A porta 8081 está ocupada. Pare o outro servidor antes dos testes.')
    env = os.environ.copy()
    env['AGROGEST_DATA_DIR'] = str(data_dir)
    process = subprocess.Popen(['bash', str(ROOT / 'scripts' / 'iniciar.sh')], cwd=ROOT, env=env, stdout=log, stderr=log)
    limit = time.monotonic() + 45
    while time.monotonic() < limit:
        if process.poll() is not None:
            raise RuntimeError('Tomcat terminou durante o arranque. Consulte o log.')
        try:
            if Browser().request('/login')[0] == 200:
                return process
        except (OSError, urllib.error.URLError):
            pass
        time.sleep(.2)
    process.terminate()
    raise RuntimeError('Tomcat não arrancou em 45 segundos.')


def stop(process):
    process.terminate()  # SIGTERM executa o shutdown hook do Tomcat.
    process.wait(timeout=30)


def suite(data_dir):
    anon = Browser()
    for route in ['/catalogo', '/carrinho', '/carrinho/atualizar', '/compra', '/factura?id=1', '/minhas-compras']:
        response = anon.request(route)
        check('Sessão obrigatória: ' + route, response[1].endswith('/login'))
    check('Admin sem sessão devolve 403', anon.request('/admin')[0] == 403)
    check('CSRF ausente devolve 403', anon.request('/login', {'email': 'admin@agro.mz', 'senha': 'admin234'})[0] == 403)
    admin = Browser()
    check('Login de administrador', '/admin' in admin.login('admin@agro.mz', 'admin234')[1])
    for route in ['/admin', '/admin/fornecedores', '/admin/produtos', '/admin/produtos?editar=1', '/admin/campanhas', '/admin/cultivos', '/admin/colheitas', '/admin/clientes', '/admin/armazem', '/admin/compras', '/admin/listagens', '/admin/relatorios', '/admin/persistencia', '/catalogo', '/factura?id=1']:
        check('JSP compilada e acessível: ' + route, admin.request(route)[0] == 200)
    client = Browser()
    client.request('/registo')
    r = client.post('/registo', nome='Teste HTTP', email='http@teste.mz', senha='1234', confirmacao='1234', telefone='841234567', endereco='Nampula')
    check('Auto-registo cria sessão de cliente', r[1].endswith('/catalogo'))
    duplicate = Browser()
    duplicate.request('/registo')
    r = duplicate.post('/registo', nome='Duplicado', email='HTTP@TESTE.MZ', senha='1234', confirmacao='1234', telefone='84', endereco='Nampula')
    check('Email único sem distinguir maiúsculas', 'já está registado' in r[2])
    for route in ['/admin', '/admin/produtos', '/%61dmin/produtos', '/admin;teste/produtos']:
        check('Cliente bloqueado com 403: ' + route, client.request(route)[0] == 403)
    check('Cliente bloqueado na factura alheia', client.request('/factura?id=1')[0] == 403)
    check('Factura inexistente devolve 404', client.request('/factura?id=999999')[0] == 404)
    check('ID de factura inválido devolve 400', client.request('/factura?id=abc')[0] == 400)
    check('Filtro de categoria', 'Enxada de cabo longo' not in client.request('/catalogo?categoria=SEMENTE')[2])
    check('Pesquisa sem resultados', 'Nenhum produto encontrado' in client.request('/catalogo?q=naoexiste123')[2])
    check('Adicionar quantidade fraccionária', 'Produto adicionado' in plain(client.post('/carrinho/adicionar', acao='adicionar', id=1, quantidade='2.5')))
    client.post('/carrinho/adicionar', acao='adicionar', id=2, quantidade='1')
    check('Carrinho persiste na sessão', 'Sementes de milho' in client.request('/carrinho')[2] and 'Fertilizante NPK' in client.request('/carrinho')[2])
    check('Actualizar quantidade', 'Carrinho actualizado' in plain(client.post('/carrinho/atualizar', acao='atualizar', id=1, quantidade='3')))
    client.post('/carrinho/atualizar', acao='atualizar', id=1, quantidade='2.5')
    check('Rejeita excesso de stock', 'Stock insuficiente' in plain(client.post('/carrinho/adicionar', acao='adicionar', id=1, quantidade='999999')))
    check('Página de confirmação', client.request('/compra')[0] == 200)
    r = client.post('/compra')
    check('Compra emite factura', '/factura?id=' in r[1] and 'Compra confirmada' in r[2])
    sale_id = int(urllib.parse.parse_qs(urllib.parse.urlparse(r[1]).query)['id'][0])
    check('Factura contém dados completos e IVA correcto', all(t in r[2] for t in ['Teste HTTP', '841234567', 'Nampula', 'Sementes de milho', 'Fertilizante NPK']) and '2\xa0392,50' in html.unescape(r[2]))
    check('Carrinho esvaziado após compra', 'Ainda há espaço' in client.request('/carrinho')[2])
    check('Reenvio da confirmação não duplica compra', 'carrinho está vazio' in client.post('/compra')[2])
    check('Histórico mostra factura do cliente', f'factura?id={sale_id}' in client.request('/minhas-compras')[2])
    check('Histórico não inclui compras alheias', 'factura?id=1"' not in client.request('/minhas-compras')[2])
    check('Administrador vê factura do novo cliente', admin.request(f'/factura?id={sale_id}')[0] == 200)
    r = admin.post('/admin/fornecedores', acao='criar', nome='Fornecedor Teste HTTP', tipo='SEMENTES', contacto='841112222', provincia='Nampula')
    supplier_id = entity_id(r[2], 'Fornecedor Teste HTTP')
    check('Criar fornecedor', supplier_id > 3)
    r = admin.post('/admin/produtos', acao='guardar', nome='Produto Teste HTTP', categoria='SEMENTE', fornecedorId=supplier_id, preco='10.10', quantidade='2.5', unidade='kg', descricao='Teste de integração', foto='', validade='')
    product_id = entity_id(r[2], 'Produto Teste HTTP')
    check('Criar produto', product_id > 4)
    check('Bloqueia remoção de fornecedor com produtos', 'primeiro os produtos' in admin.post('/admin/fornecedores', acao='remover', id=supplier_id)[2])
    check('Reposição de stock', '3,75' in plain(admin.post('/admin/produtos', acao='repor', id=product_id, quantidade='1.25')))
    versao = re.search(r'name="versao" value="([^"]+)"', admin.request('/admin/produtos?editar='+str(product_id))[2]).group(1)
    r = admin.post('/admin/produtos', acao='guardar', id=product_id, versao=versao, nome='Produto Teste HTTP', categoria='SEMENTE', fornecedorId=supplier_id, preco='20.25', quantidade='3.75', unidade='kg', descricao='Descrição actualizada', foto='', validade='')
    check('Edição de produto', '20,25' in r[2])
    client.post('/carrinho/adicionar', acao='adicionar', id=1, quantidade='1')
    client.post('/carrinho/adicionar', acao='adicionar', id=product_id, quantidade='1')
    client.request('/compra')
    admin.post('/admin/produtos', acao='remover', id=product_id)
    check('Produto removido impede compra completa', 'produto foi removido' in client.post('/compra')[2])
    check('Item removido pode sair do carrinho', 'Carrinho actualizado' in client.post('/carrinho/remover', acao='remover', id=product_id)[2])
    client.post('/carrinho/esvaziar', acao='esvaziar')
    check('Esvaziar carrinho', 'Ainda há espaço' in client.request('/carrinho')[2])
    check('Remover fornecedor sem produtos', 'Fornecedor Teste HTTP' not in admin.post('/admin/fornecedores', acao='remover', id=supplier_id)[2])
    r = admin.post('/admin/campanhas', acao='criar', nome='Campanha Teste HTTP', inicio='2026-01-01', fim='2026-12-31')
    campaign_id = entity_id(r[2], 'Campanha Teste HTTP')
    check('Criar campanha', campaign_id > 1)
    r = admin.post('/admin/cultivos', acao='criar', campanhaId=campaign_id, cultura='Cultura Teste HTTP', variedade='A', hectares='8', plantio='2026-02-01')
    crop_id = entity_id(r[2], 'Cultura Teste HTTP')
    check('Criar cultivo', crop_id > 1)
    check('Campanha com cultivos não pode ser removida', 'cultivos associados' in admin.post('/admin/campanhas', acao='remover', id=campaign_id)[2])
    r = admin.post('/admin/colheitas', acao='criar', cultivoId=crop_id, data='2026-06-01', quantidade='3200', precoKg='22')
    check('Colheita calcula 3200 x 22', '70\xa0400,00' in html.unescape(r[2]))
    check('Cultivo com colheitas não pode ser removido', 'colheitas associadas' in admin.post('/admin/cultivos', acao='remover', id=crop_id)[2])
    admin.post('/admin/campanhas', acao='fechar', id=campaign_id)
    check('Campanha fechada bloqueia novo cultivo', 'Reabra a campanha' in admin.post('/admin/cultivos', acao='criar', campanhaId=campaign_id, cultura='Bloqueado', variedade='A', hectares='1', plantio='2026-03-01')[2])
    admin.post('/admin/campanhas', acao='reabrir', id=campaign_id)
    r = admin.post('/admin/cultivos', acao='criar', campanhaId=campaign_id, cultura='Cultivo sem colheita', variedade='A', hectares='1', plantio='2026-03-01')
    empty_crop = entity_id(r[2], 'Cultivo sem colheita')
    check('Remover cultivo sem colheitas', 'Cultivo sem colheita' not in admin.post('/admin/cultivos', acao='remover', id=empty_crop)[2])
    r = admin.post('/admin/campanhas', acao='criar', nome='Campanha vazia', inicio='2026-01-01', fim='2026-02-01')
    empty_campaign = entity_id(r[2], 'Campanha vazia')
    check('Remover campanha vazia', 'Campanha vazia' not in admin.post('/admin/campanhas', acao='remover', id=empty_campaign)[2])
    for section, fields in {'produtos':['nome','preco','stock','data'], 'fornecedores':['nome','provincia','tipo'], 'clientes':['nome','email','data'], 'compras':['data','valor','nome'], 'armazem':['nome','stock','preco'], 'campanhas':['nome','data'], 'cultivos':['nome','data','hectares'], 'colheitas':['data','valor','quantidade']}.items():
        for field in fields:
            for direction in ['asc', 'desc']:
                check(f'Ordenação {section}/{field}/{direction}', admin.request(f'/admin/{section}?ordem={field}&sentido={direction}')[0] == 200)
    for kind in ['nome','volume','assiduos','sem-compras']:
        check('Relatório: '+kind, admin.request('/admin/relatorios?tipo='+kind)[0] == 200)
    check('Relatório extra identifica cliente sem compras', 'Amélia Manuel' in admin.request('/admin/relatorios?tipo=sem-compras')[2])
    r = admin.post('/admin/fornecedores', acao='criar', nome='<script>AgroTeste</script>', tipo='SEMENTES', contacto='84', provincia='Nampula')
    check('Dados de utilizador escapados no HTML', '&lt;script&gt;AgroTeste&lt;/script&gt;' in r[2] and '<script>AgroTeste</script>' not in r[2])
    admin.post('/admin/persistencia', acao='gravar')
    check('Gravação manual cria .ser', (data_dir/'agrogest.ser').exists())
    admin.post('/admin/persistencia', acao='eliminar')
    check('Eliminação manual remove ficheiros', not (data_dir/'agrogest.ser').exists() and not (data_dir/'agrogest.ser.bak').exists())
    check('Eliminar ficheiro conserva dados em memória', 'Campanha Teste HTTP' in admin.request('/admin/campanhas')[2])
    admin.post('/admin/persistencia', acao='gravar')
    check('Passwords originais ausentes do estado', b'cliente123' not in (data_dir/'agrogest.ser').read_bytes() and b'admin234' not in (data_dir/'agrogest.ser').read_bytes())
    check('Logout termina sessão', client.post('/logout')[1].endswith('/login') and client.request('/carrinho')[1].endswith('/login'))
    return sale_id


def main():
    with tempfile.TemporaryDirectory(prefix='agrogest-test-') as folder:
        folder = Path(folder)
        with (folder/'tomcat.log').open('w') as log:
            process = None
            try:
                process = start(folder/'dados', log)
                sale_id = suite(folder/'dados')
                stop(process)
                check('Encerramento grava estado', (folder/'dados/agrogest.ser').exists())
                process = start(folder/'dados', log)
                client = Browser(); client.login('http@teste.mz', '1234')
                check('Reinício restaura cliente e factura', client.request(f'/factura?id={sale_id}')[0] == 200)
                admin = Browser(); admin.login('admin@agro.mz','admin234')
                check('Reinício restaura campanhas', 'Campanha Teste HTTP' in admin.request('/admin/campanhas')[2])
                check('Stock restaurado após reinício', '459,5' in plain(admin.request('/admin/produtos')))
                client.post('/carrinho/adicionar', acao='adicionar', id=1, quantidade='1')
                client.request('/compra')
                next_sale = client.post('/compra')
                check('Número de factura continua a sequência', f'factura?id={sale_id+1}' in next_sale[1])
                # Gancho opcional usado na revisão visual local. Não é necessário para os testes.
                visual = os.environ.get('AGROGEST_VISUAL_TEST')
                if visual:
                    subprocess.run(['node', visual], check=True, timeout=90)
            except Exception:
                log.flush()
                print((folder/'tomcat.log').read_text()[-10000:])
                raise
            finally:
                if process is not None and process.poll() is None:
                    stop(process)
    print(json.dumps({'verificacoes': len(checks), 'resultado': 'PASSOU'}, ensure_ascii=False))


if __name__ == '__main__':
    main()
