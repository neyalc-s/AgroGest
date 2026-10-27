#!/bin/sh
set -eu

port="${PORT:-8080}"
case "$port" in
  ''|*[!0-9]*)
    echo "PORT must be a numeric TCP port; received: $port" >&2
    exit 1
    ;;
esac

# Render injecta PORT. O bind 0.0.0.0 está definido no server.xml do container.
sed -i -E "s/(<Connector port=\")[0-9]+/\1${port}/" "$CATALINA_HOME/conf/server.xml"

exec "$CATALINA_HOME/bin/catalina.sh" run
