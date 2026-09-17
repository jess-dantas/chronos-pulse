#!/usr/bin/env bash
# Para o banco Postgres local (dados preservados no volume chronos_pgdata).
cd "$(dirname "${BASH_SOURCE[0]}")"
docker compose stop postgres
echo "Postgres parado. Para apagar TODOS os dados: docker compose down -v"