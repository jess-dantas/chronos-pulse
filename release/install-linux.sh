#!/usr/bin/env bash
# Instala o Chronos Pulse on-prem em /opt/chronos-pulse como serviço systemd.
# Requisitos: root, docker e Java 25 instalados.
set -euo pipefail

if [ "$(id -u)" -ne 0 ]; then
    echo "Execute como root (sudo ./install-linux.sh)."
    exit 1
fi

INSTALL_DIR="/opt/chronos-pulse"
SRC_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [ ! -f "$SRC_DIR/chronos-pulse.jar" ]; then
    echo "[ERRO] chronos-pulse.jar nao encontrado nesta pasta. Gere com release/build-release.ps1 ou copie o jar."
    exit 1
fi
if ! command -v docker >/dev/null 2>&1; then
    echo "[ERRO] 'docker' nao encontrado. Instale o Docker (com o serviço habilitado)."
    exit 1
fi
if ! command -v java >/dev/null 2>&1; then
    echo "[ERRO] 'java' nao encontrado. Instale o Java 25 (Temurin/Adoptium)."
    exit 1
fi

mkdir -p "$INSTALL_DIR"
cp -f "$SRC_DIR/chronos-pulse.jar" "$INSTALL_DIR/"
cp -f "$SRC_DIR/docker-compose.yml" "$INSTALL_DIR/"
cp -f "$SRC_DIR/start.sh" "$INSTALL_DIR/"
cp -f "$SRC_DIR/stop.sh" "$INSTALL_DIR/"
cp -f "$SRC_DIR/README.md" "$INSTALL_DIR/"

if [ ! -f "$INSTALL_DIR/.env" ]; then
    SECRET="$(head -c 32 /dev/urandom | od -An -tx1 | tr -d ' \n')"
    PGPASS="$(head -c 12 /dev/urandom | od -An -tx1 | tr -d ' \n')"
    cat > "$INSTALL_DIR/.env" <<EOF
JWT_SECRET=$SECRET
POSTGRES_USER=chronos_user
POSTGRES_PASSWORD=$PGPASS
POSTGRES_DB=chronos_db
POSTGRES_PORT=5432
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/chronos_db
SPRING_DATASOURCE_USERNAME=chronos_user
SPRING_DATASOURCE_PASSWORD=$PGPASS
PORT=3030
CHRONOS_MAIL_ENABLED=false
MANAGEMENT_HEALTH_MAIL_ENABLED=false
EOF
    chmod 600 "$INSTALL_DIR/.env"
    echo ".env criado em $INSTALL_DIR/.env (credenciais geradas)."
fi

if ! id chronos >/dev/null 2>&1; then
    useradd --system --home "$INSTALL_DIR" --shell /usr/sbin/nologin chronos
fi
usermod -aG docker chronos
chown -R chronos:chronos "$INSTALL_DIR"
chmod 600 "$INSTALL_DIR/.env"

cp -f "$SRC_DIR/chronos-pulse.service" /etc/systemd/system/chronos-pulse.service
systemctl daemon-reload
systemctl enable --now chronos-pulse.service
systemctl status --no-pager chronos-pulse.service || true

echo
echo "Instalado: $INSTALL_DIR"
echo "Dados:     volume Docker chronos_pgdata (NUNCA apagar sem backup)"
echo "Logs:      journalctl -u chronos-pulse -f"
echo "Edite o .env e reinicie: sudo systemctl restart chronos-pulse"