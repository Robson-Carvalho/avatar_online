#!/bin/bash

echo "🚀 Setup Automático do Nó Avatar"

# Verifica Docker
if ! command -v docker &> /dev/null; then
    echo "❌ Docker não encontrado. Instale primeiro."
    exit 1
fi

# Verifica Docker Compose
if ! command -v docker-compose &> /dev/null && ! command -v docker compose &> /dev/null; then
    echo "❌ Docker Compose não encontrado. Instale primeiro."
    exit 1
fi

# Configuração automática do IP
DEFAULT_IP=$(hostname -I | awk '{print $1}')
echo "🔍 IP detectado: $DEFAULT_IP"

read -p "ID do nó (ex: node-1): " NODE_ID
read -p "IP do nó [${DEFAULT_IP}]: " NODE_IP
NODE_IP=${NODE_IP:-$DEFAULT_IP}

# Portas baseadas no ID do nó
case $NODE_ID in
    "node-1") SERVER_PORT="8080" ;;
    "node-2") SERVER_PORT="8081" ;;
    "node-3") SERVER_PORT="8082" ;;
    *) SERVER_PORT="8080" ;;
esac

# Cria configuração
cat > .node-config << EOF
NODE_ID="$NODE_ID"
NODE_IP="$NODE_IP"
SERVER_PORT="$SERVER_PORT"
DB_HOST="localhost"
EOF

# Gera docker-compose
./docker/config-node.sh

# Build da imagem
echo "🏗️  Construindo imagem Docker..."
docker build -t avatar-online:latest .

# Inicia serviços
echo "🚀 Iniciando serviços..."
docker-compose up -d

echo "✅ Setup completo!"
echo "📊 Nó: $NODE_ID (http://$NODE_IP:$SERVER_PORT)"
echo "🐘 Banco: postgres://localhost:5432/avatar_$NODE_ID"
echo "🔍 Verifique com: docker-compose logs -f app"