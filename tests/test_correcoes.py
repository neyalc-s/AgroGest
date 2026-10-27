#!/usr/bin/env python3
"""Regressões de confirmação: preços, carrinho, tokens e reenvios.
Executar com Java 17 e Python 3: python3 tests/test_correcoes.py
"""
from pathlib import Path
from decimal import Decimal
import html
import json
import re
import tempfile

import test_http as h
import test_revisao as r

checks = []


def check(name, value):
    if not value:
        raise AssertionError(name)
    checks.append(name)
    print('OK:', name, flush=True)


def total(response):
    value = re.search(r'class="summary-row total".*?<strong>(.*?)</strong>', response[2], re.S).group(1)
    return Decimal(re.sub(r'[^0-9,.-]', '', html.unescape(value)).replace(',', '.'))


def stock(admin, product=1):
    return Decimal(r.edit(admin, product)['quantidade'])


def add(client, product=1, amount='1'):
    client.post('/carrinho/adicionar', acao='adicionar', id=product, quantidade=amount)


def suite(admin, client):
    before = r.invoices(admin)
    initial_stock = stock(admin)
    add(client)
    check('POST sem abrir confirmação é recusado', '/factura?id=' not in client.post('/compra', confirmacaoCompra='')[1])
    check('POST sem confirmação não altera vendas nem stock', r.invoices(admin) == before and stock(admin) == initial_stock)

    page = client.request('/compra')
    old_token = client.confirmacao_compra
    check('Resumo apresenta 98,60 MT e um token próprio', total(page) == Decimal('98.60') and bool(old_token))
    edit = r.edit(admin, 1)
    edit['preco'] = '170.00'
    r.submit_edit(admin, edit)
    result = client.post('/compra', confirmacaoCompra=old_token)
    check('Preço alterado exige nova confirmação', 'confirme novamente' in result[2] and '/factura?id=' not in result[1])
    check('Preço alterado não desconta stock nem cria factura', r.invoices(admin) == before and stock(admin) == initial_stock)
    page = client.request('/compra')
    check('Novo resumo apresenta 197,20 MT', total(page) == Decimal('197.20'))
    result = client.post('/compra')
    check('Nova confirmação factura exactamente 197,20 MT', '/factura?id=' in result[1] and total(result) == Decimal('197.20'))

    add(client)
    page = client.request('/compra')
    old_token = client.confirmacao_compra
    before = r.invoices(admin)
    initial_stock = stock(admin)
    client.post('/carrinho/atualizar', acao='atualizar', id=1, quantidade='3')
    result = client.post('/compra', confirmacaoCompra=old_token)
    check('Quantidade alterada noutro separador é recusada', 'confirme novamente' in result[2] and '/factura?id=' not in result[1])
    check('Quantidade alterada não desconta stock nem cria factura', r.invoices(admin) == before and stock(admin) == initial_stock)
    check('Novo resumo apresenta as 3 unidades e 591,60 MT', total(client.request('/compra')) == Decimal('591.60'))
    result = client.post('/compra')
    invoice_url = result[1]
    used_token = client.confirmacao_compra
    check('Confirmar o novo carrinho cria uma única factura correcta', total(result) == Decimal('591.60') and len(r.invoices(admin) - before) == 1)

    add(client)
    before = r.invoices(admin)
    initial_stock = stock(admin)
    result = client.post('/compra', confirmacaoCompra=used_token)
    check('Reenvio antigo não compra um carrinho repovoado', '/factura?id=' not in result[1])
    check('Reenvio antigo conserva o novo carrinho e o stock', r.invoices(admin) == before and stock(admin) == initial_stock and 'Sementes de milho' in client.request('/carrinho')[2])

    client.request('/compra')
    token_a = client.confirmacao_compra
    client.request('/compra')
    token_b = client.confirmacao_compra
    check('Nova página recebe identificador diferente', token_a != token_b)
    check('Formulário anterior à nova página não confirma outra proposta', '/factura?id=' not in client.post('/compra', confirmacaoCompra=token_a)[1])
    check('Token inventado é recusado', '/factura?id=' not in client.post('/compra', confirmacaoCompra='inventado')[1])
    check('Token de compra não substitui CSRF', client.request('/compra', {'confirmacaoCompra': token_b})[0] == 403)
    result = client.post('/compra', confirmacaoCompra=token_b, preco='0', total='0', clienteId='3')
    check('Proposta actual mantém preço e cliente calculados no servidor', '/factura?id=' in result[1] and total(result) == Decimal('197.20') and 'João Silva' in result[2])

    add(client)
    client.request('/compra')
    initial_stock = stock(admin)
    admin.post('/admin/produtos', acao='repor', id=1, quantidade='5')
    result = client.post('/compra')
    check('Reposição sem mudar preços permite confirmar', '/factura?id=' in result[1] and stock(admin) == initial_stock + 4)

    add(client)
    client.request('/compra')
    before = r.invoices(admin)
    initial_stock = stock(admin)
    add(client, 2)
    check('Adicionar um novo produto invalida o resumo anterior', '/factura?id=' not in client.post('/compra')[1])
    check('Alteração de itens não cria venda parcial', r.invoices(admin) == before and stock(admin) == initial_stock)
    client.post('/carrinho/esvaziar', acao='esvaziar')
    return invoice_url


def main():
    with tempfile.TemporaryDirectory(prefix='agrogest-correcoes-') as directory:
        folder = Path(directory)
        process = None
        with (folder/'tomcat.log').open('w') as log:
            try:
                process = h.start(folder/'dados', log)
                admin, client = h.Browser(), h.Browser()
                admin.login('admin@agro.mz', 'admin234')
                client.login('joao@teste.mz', 'cliente123')
                invoice_url = suite(admin, client)
                admin.post('/admin/persistencia', acao='gravar')
                h.stop(process)
                process = h.start(folder/'dados', log)
                client = h.Browser()
                client.login('joao@teste.mz', 'cliente123')
                path = invoice_url.split(h.BASE, 1)[1]
                check('Reinício conserva a factura confirmada com os novos valores', total(client.request(path)) == Decimal('591.60'))
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
