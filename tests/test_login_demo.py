#!/usr/bin/env python3
"""Contas de demonstração dinâmicas, isolamento da sessão e opção de desligar."""
from pathlib import Path
from html.parser import HTMLParser
import json
import os
import tempfile
import test_http as h

checks = []


def check(name, value):
    if not value:
        raise AssertionError(name)
    checks.append(name)
    print('OK:', name, flush=True)


class Accounts(HTMLParser):
    def __init__(self, text):
        super().__init__(convert_charrefs=True)
        self.accounts = {}
        self.feed(text)

    def handle_starttag(self, tag, attrs):
        a = dict(attrs)
        if tag == 'button' and 'data-demo-account' in a:
            self.accounts[a['data-demo-email']] = a


def accounts(browser):
    return Accounts(browser.request('/login')[2]).accounts


def select_login(browser, email):
    account = accounts(browser)[email]
    return browser.post('/login', email=email, senha=account['data-demo-senha'])


def main():
    old_flag = os.environ.get('AGROGEST_DEMO_LOGIN')
    process = None
    with tempfile.TemporaryDirectory(prefix='agrogest-login-demo-') as tmp:
        folder = Path(tmp)
        with (folder/'tomcat.log').open('w') as log:
            try:
                os.environ['AGROGEST_DEMO_LOGIN'] = 'true'
                process = h.start(folder/'dados', log)
                browser = h.Browser()
                available = accounts(browser)
                expected = {'admin@agro.mz', 'joao@teste.mz', 'ana@teste.mz', 'amelia@teste.mz'}
                check('As quatro contas existentes aparecem no selector', set(available) == expected)
                check('Contas são botões que não submetem o formulário', all(a.get('type') == 'button' for a in available.values()))
                check('Credenciais temporárias são diferentes por conta', len({a['data-demo-senha'] for a in available.values()}) == 4)
                for email in sorted(expected):
                    b = h.Browser()
                    response = select_login(b, email)
                    target = '/admin' if email == 'admin@agro.mz' else '/catalogo'
                    check('Entrar com a conta seleccionada: ' + email, response[0] == 200 and response[1].endswith(target))
                    original = 'admin234' if email == 'admin@agro.mz' else 'cliente123'
                    check('Senha original preservada: ' + email, h.Browser().login(email, original)[1].endswith(target))

                other = h.Browser()
                own = accounts(other)
                borrowed = available['admin@agro.mz']['data-demo-senha']
                check('Cada sessão recebe credenciais próprias', own['admin@agro.mz']['data-demo-senha'] != borrowed)
                response = other.post('/login', email='admin@agro.mz', senha=borrowed)
                check('Credencial de outra sessão não permite entrar', response[1].endswith('/login') and 'incorrectos' in response[2])

                new = h.Browser()
                new.request('/registo')
                new.post('/registo', nome='Cliente criado agora', email='novo-demo@teste.mz', senha='senha-nova-123', confirmacao='senha-nova-123', telefone='84', endereco='Nampula')
                new.post('/logout')
                available_new = accounts(new)
                check('Nova conta é incluída automaticamente', len(available_new) == 5 and 'novo-demo@teste.mz' in available_new)
                response = new.post('/login', email='novo-demo@teste.mz', senha=available_new['novo-demo@teste.mz']['data-demo-senha'])
                check('Nova conta entra com credencial temporária', response[1].endswith('/catalogo'))
                check('Nova conta continua a aceitar a senha original', h.Browser().login('novo-demo@teste.mz', 'senha-nova-123')[1].endswith('/catalogo'))

                admin = h.Browser(); admin.login('admin@agro.mz', 'admin234')
                admin.post('/admin/persistencia', acao='gravar')
                state = (folder/'dados/agrogest.ser').read_bytes()
                check('Credenciais temporárias não ficam no estado do sistema', all(a['data-demo-senha'].encode() not in state for a in available_new.values()))
                h.stop(process)
                os.environ['AGROGEST_DEMO_LOGIN'] = 'false'
                process = h.start(folder/'dados', log)
                b = h.Browser()
                response = b.request('/login')
                check('Modo temporário pode ser desligado', not Accounts(response[2]).accounts and 'data-demo-senha' not in response[2])
                check('Login normal funciona com o modo temporário desligado', b.login('novo-demo@teste.mz', 'senha-nova-123')[1].endswith('/catalogo'))
            except Exception:
                log.flush(); print((folder/'tomcat.log').read_text()[-10000:])
                raise
            finally:
                if process is not None and process.poll() is None:
                    h.stop(process)
                if old_flag is None:
                    os.environ.pop('AGROGEST_DEMO_LOGIN', None)
                else:
                    os.environ['AGROGEST_DEMO_LOGIN'] = old_flag
    print(json.dumps({'verificacoes': len(checks), 'resultado': 'PASSOU'}, ensure_ascii=False))


if __name__ == '__main__':
    main()
