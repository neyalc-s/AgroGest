# Publicar no Render Free

O repositório inclui `Dockerfile`, `.dockerignore` e `render.yaml` para publicar
o AgroGest como um Web Service Docker no plano Free.

## Configuração exacta

- Repository: `neyalc-s/AgroGest`
- Branch: `render-deploy`
- Runtime: `Docker`
- Plan: `Free`
- Dockerfile path: `./Dockerfile`
- Docker context: `.`
- Health check path: `/agrogest/login`
- Environment variable: `AGROGEST_DEMO_LOGIN=true`

O container usa Tomcat 10.1 com Java 17. O script de entrada substitui a porta
do Connector pelo valor `PORT` fornecido pelo Render e o Tomcat fica ligado a
`0.0.0.0`. A aplicação continua disponível no contexto `/agrogest/`.

No Render, `AGROGEST_DATA_DIR` é `/tmp/agrogest-data`. A persistência existente
continua activa e não há limpeza forçada no arranque, mas o disco do plano Free
é efémero: uma nova instância pode perder `agrogest.ser` e o seu backup.

Para criar o serviço, abra o dashboard do Render, escolha **New → Blueprint**,
aponte para este repositório e seleccione a branch `render-deploy`. O Render
lerá o `render.yaml` e pedirá apenas a confirmação da criação do serviço.
