#!/bin/bash
#
# Start script for accounts-filing-api
PORT=3000
exec java -Xmx400m -jar -Dserver.port="${PORT}" "./accounts-filing-api.jar"