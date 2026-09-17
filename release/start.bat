@echo off
setlocal EnableExtensions
cd /d "%~dp0"

if not exist ".env" (
    echo Gerando .env (primeiro uso)...
    powershell -NoProfile -Command "$s=[System.Guid]::NewGuid().ToString('N'); $p=[System.Guid]::NewGuid().ToString('N').Substring(0,12); $envVars=@('JWT_SECRET='+$s,'POSTGRES_USER=chronos_user','POSTGRES_PASSWORD='+$p,'POSTGRES_DB=chronos_db','POSTGRES_PORT=5432','PORT=8080'); [IO.File]::WriteAllLines((Join-Path $PWD '.env'),$envVars)"
)

for /f "usebackq eol=# tokens=1,* delims==" %%A in (".env") do (
    if not "%%B"=="" set "%%A=%%B"
)
if not defined JWT_SECRET ( echo [ERRO] JWT_SECRET ausente no .env & pause & exit /b 1 )

docker version >nul 2>&1
if errorlevel 1 (
    echo [ERRO] Docker nao encontrado. Instale o Docker Desktop (Windows 10/11) e abra-o.
    pause
    exit /b 1
)

where java >nul 2>&1
if errorlevel 1 (
    echo [ERRO] Java nao encontrado. Instale o Java 25 (Temurin/Adoptium) e adicione-o ao PATH.
    pause
    exit /b 1
)

echo Subindo banco Postgres (Docker)...
docker compose up -d postgres
if errorlevel 1 ( echo [ERRO] Falha ao subir o Postgres. & pause & exit /b 1 )

echo Aguardando o banco ficar pronto...
set /a _tries=0
:wait
docker compose exec -T postgres pg_isready -U %POSTGRES_USER% -d %POSTGRES_DB% >nul 2>&1
if errorlevel 1 (
    set /a _tries+=1
    if %_tries% geq 60 (
        echo [ERRO] Postgres nao ficou pronto a tempo. Veja: docker compose logs postgres
        pause
        exit /b 1
    )
    timeout /t 2 >nul
    goto wait
)

if not defined POSTGRES_PORT set "POSTGRES_PORT=5432"
set "SPRING_PROFILES_ACTIVE=prod"
set "SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:%POSTGRES_PORT%/%POSTGRES_DB%"
set "SPRING_DATASOURCE_USERNAME=%POSTGRES_USER%"
set "SPRING_DATASOURCE_PASSWORD=%POSTGRES_PASSWORD%"
if not defined CHRONOS_MAIL_ENABLED set "CHRONOS_MAIL_ENABLED=false"
if not defined MANAGEMENT_HEALTH_MAIL_ENABLED set "MANAGEMENT_HEALTH_MAIL_ENABLED=false"
if not defined PORT set "PORT=8080"

echo ---------------------------------------------------------------
echo  Chronos Pulse - modo on-prem (Postgres via Docker)
echo  Acesse:  http://localhost:%PORT%
echo  Saude:   http://localhost:%PORT%/actuator/health
echo  Encerrar: Ctrl+C  (dados ficam no volume Docker)
echo ---------------------------------------------------------------
java -jar chronos-pulse.jar --server.port=%PORT%