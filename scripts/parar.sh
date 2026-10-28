#!/usr/bin/env bash
set -euo pipefail
AGROGEST_ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
export CATALINA_HOME="$AGROGEST_ROOT/runtime/tomcat"
export CATALINA_BASE="$CATALINA_HOME"
if [[ -n "${JAVA_HOME:-}" ]]; then export JRE_HOME="$JAVA_HOME"; fi
bash "$CATALINA_HOME/bin/catalina.sh" stop
echo "Paragem solicitada. Aguarde a mensagem de estado gravado no terminal do servidor."
