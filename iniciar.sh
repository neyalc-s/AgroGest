#!/usr/bin/env bash
set -euo pipefail
AGROGEST_ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd "$AGROGEST_ROOT"
if ! command -v java >/dev/null 2>&1 && [[ -z "${JAVA_HOME:-}" ]] && [[ -z "${JRE_HOME:-}" ]]; then
  echo "Java não encontrado. Instale Java 17 e volte a executar este ficheiro."
  exit 1
fi
if [[ -n "${JAVA_HOME:-}" ]]; then
  AGROGEST_JAVA="$JAVA_HOME/bin/java"
  export JRE_HOME="$JAVA_HOME"
elif [[ -n "${JRE_HOME:-}" ]]; then
  AGROGEST_JAVA="$JRE_HOME/bin/java"
else
  AGROGEST_JAVA="$(command -v java)"
fi
if [[ ! -x "$AGROGEST_JAVA" ]]; then
  echo "Executável Java inválido. Verifique JAVA_HOME, JRE_HOME e o PATH."
  exit 1
fi
AGROGEST_VERSION="$("$AGROGEST_JAVA" -version 2>&1 | head -n 1 | sed -E 's/.*version "([0-9]+).*/\1/')"
if [[ ! "$AGROGEST_VERSION" =~ ^[0-9]+$ ]] || (( AGROGEST_VERSION < 17 )); then
  echo "É necessário Java 17 ou superior. Verifique java -version e JAVA_HOME."
  exit 1
fi
export CATALINA_HOME="$AGROGEST_ROOT/runtime/tomcat"
export CATALINA_BASE="$CATALINA_HOME"
export AGROGEST_DATA_DIR="${AGROGEST_DATA_DIR:-$AGROGEST_ROOT/dados}"
export AGROGEST_DEMO_LOGIN="${AGROGEST_DEMO_LOGIN:-true}"
mkdir -p "$CATALINA_BASE/webapps" "$CATALINA_BASE/logs" "$CATALINA_BASE/temp" "$CATALINA_BASE/work" "$AGROGEST_DATA_DIR"
if [[ ! -f dist/agrogest.war ]]; then echo "WAR em falta: dist/agrogest.war"; exit 1; fi
if ! cmp -s dist/agrogest.war "$CATALINA_BASE/webapps/agrogest.war"; then cp dist/agrogest.war "$CATALINA_BASE/webapps/agrogest.war"; fi
echo "AgroGest: http://localhost:8081/agrogest/"
echo "Para terminar e gravar: Ctrl+C, ou execute bash parar.sh noutro terminal."
exec bash "$CATALINA_HOME/bin/catalina.sh" run
