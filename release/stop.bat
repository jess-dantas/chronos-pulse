@echo off
setlocal EnableExtensions
cd /d "%~dp0"

docker compose stop postgres
echo.
echo Postgres parado (volume Docker preservado: chronos_pgdata).
echo Para remover TODOS os dados:  docker compose down -v
pause