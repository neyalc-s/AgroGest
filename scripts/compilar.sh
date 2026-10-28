#!/usr/bin/env bash
set -euo pipefail
AGROGEST_ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$AGROGEST_ROOT"
if ! command -v mvn >/dev/null 2>&1; then echo "Instale Maven 3.9+ e um JDK 17. Consulte README.md."; exit 1; fi
mvn clean verify
mkdir -p dist
cp target/agrogest.war dist/agrogest.war
echo "Compilação e testes concluídos. WAR actualizado em dist/agrogest.war."
