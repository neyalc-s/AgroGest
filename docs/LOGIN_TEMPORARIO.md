# AgroGest 1.2.2 — login temporário por clique

A página de login mostra todos os utilizadores existentes, com nome, perfil e email.

1. Clique na conta pretendida.
2. O email e a credencial são preenchidos automaticamente.
3. Carregue em **Entrar** para iniciar a sessão. O clique na conta, por si só, não entra.

A lista é obtida dos dados actuais: inclui o administrador, todos os clientes de exemplo e os clientes criados posteriormente. Contas inactivas aparecem desactivadas. A lista tem deslocamento quando há muitas contas e adapta-se ao telemóvel.

## Credenciais

O selector preenche `admin234` para administradores e `cliente123` para clientes, incluindo contas criadas posteriormente. Estas credenciais só permitem o acesso de demonstração quando o modo está activo. As palavras-passe originais continuam a funcionar e não são alteradas: são guardadas apenas como hash. O estado `agrogest.ser` não recebe estas credenciais em texto simples. O login normal, os perfis, o CSRF e o botão Entrar continuam activos.

## Activar ou desligar

Os scripts incluídos `scripts/iniciar.sh` e `scripts/iniciar.bat` activam o selector temporário por omissão.

Em Linux, para desligar:

```bash
AGROGEST_DEMO_LOGIN=false bash scripts/iniciar.sh
```

Em Windows, no terminal:

```bat
set AGROGEST_DEMO_LOGIN=false
scripts/iniciar.bat
```

Num Tomcat externo, o selector está desligado por omissão. Para o activar, defina `AGROGEST_DEMO_LOGIN=true`, ou a propriedade Java `-Dagrogest.demoLogin=true`. A propriedade Java tem prioridade sobre a variável de ambiente.

## Verificações desta versão

- `mvn clean verify`: 41 testes Java, sem falhas nem erros.
- As quatro suites HTTP passaram: 128 + 32 + 22 + 19 = 201 verificações.
- O teste de navegador passou 52 verificações, com cliques reais nas quatro contas iniciais, em 1440 e 390 px, e numa conta criada durante o teste.
- Confirmado que clicar só preenche os campos e que carregar em Entrar abre o perfil correcto.
- As verificações actuais confirmam as credenciais por perfil, a preservação das palavras-passe originais e a opção de desligar o selector.
- Páginas desktop e móvel inspeccionadas visualmente, sem transbordo horizontal.

Testes novos: `src/test/java/mz/agrogest/DemonstracaoTest.java`, `tests/test_login_demo.py` e `tests/test_login_browser.py` / `.cjs`.

O teste de navegador é opcional e requer Node.js, Playwright e Chromium. O launcher Python inicia o Tomcat com dados temporários e encerra-o no fim. Node.js e Playwright não são necessários para utilizar a aplicação. Os scripts Windows e o serviço systemd não foram executados nesta revisão.

As correcções da versão 1.2.1 permanecem incluídas e voltaram a passar nos testes de regressão. O ZIP contém o WAR recompilado e as fontes completas.
