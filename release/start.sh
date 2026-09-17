#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")"

if [ ! -f .env ]; then
    SECRET="$(head -c 32 /dev/urandom | od -An -tx1 | tr -d ' \n')"
    PGPASS="$(head -c 12 /dev/urandom | od -An -tx1 | tr -d ' \n')"
    cat > .env <<EOF
JWT_SECRET=$SECRET
POSTGRES_USER=chronos_user
POSTGRES_PASSWORD=$PGPASS
POSTGRES_DB=chronos_db
POSTGRES_PORT=5432
PORT=8080
EOF
    chmod 600 .env
    echo "Arquivo .env criado (credenciais do banco e JWT geradas)."
fi

set -a
. ./.env
set +a

if [ -z "${JWT_SECRET:-}" ]; then
    echo "[ERRO] JWT_SECRET ausente no .env." >&2
    exit 1
fi
if ! command -v docker >/dev/null 2>&1; then
    echo "[ERRO] 'docker' nao encontrado. Instale o Docker e inicie o daemon." >&2
    exit 1
fi
if ! command -v java >/dev/null 2>&1; then
    echo "[ERRO] 'java' nao encontrado. Instale o Java 25 (Temurin/Adoptium)." >&2
    exit 1
fi

echo "Subindo banco Postgres (Docker)..."
docker compose up -d postgres

echo "Aguardando o banco ficar pronto..."
READY=0
for _ in $(seq 1 60); do
    if docker compose exec -T postgres pg_isready -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" >/dev/null 2>&1; then
        READY=1
        break
    fi
    sleep 1
done
if [ "$READY" -ne 1 ]; then
    echo "[ERRO] Postgres nao ficou pronto a tempo. Veja: docker compose logs postgres" >&2
    exit 1
fi

export SPRING_PROFILES_ACTIVE=prod
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:${POSTGRES_PORT:-5432}/${POSTGRES_DB}"
export SPRING_DATASOURCE_USERNAME="${POSTGRES_USER}"
export SPRING_DATASOURCE_PASSWORD="${POSTGRES_PASSWORD}"
export CHRONOS_MAIL_ENABLED="${CHRONOS_MAIL_ENABLED:-false}"
export PORT="${PORT:-8080}"

echo "---------------------------------------------------------------"
echo " Chronos Pulse - modo on-prem (Postgres via Docker)"
echo " Acesse:  http://localhost:$PORT"
echo " Saude:   http://localhost:$PORT/actuator/health"
echo " Encerrar: Ctrl+C  (dados ficam no volume Docker)"
echo "---------------------------------------------------------------"
exec java -jar chronos-pulse.jar --server.port="$PORT"