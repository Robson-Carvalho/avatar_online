#!/bin/bash

echo "🎯 Configurando nó do cluster Avatar..."

# Configurações padrão (cada máquina altera)
NODE_ID="node-1"
NODE_IP="172.16.201.10"
SERVER_PORT="8080"
DB_HOST="localhost"

# Verifica se é uma máquina específica
if [ -f ".node-config" ]; then
    source .node-config
    echo "📁 Configuração local encontrada: $NODE_ID - $NODE_IP"
else
    echo "❓ Nenhuma configuração local encontrada. Usando padrão: $NODE_ID"
    read -p "Deseja configurar este nó? (s/n): " configurar
    if [ "$configurar" = "s" ]; then
        read -p "ID do nó (ex: node-1): " NODE_ID
        read -p "IP da máquina (ex: 172.16.201.10): " NODE_IP
        read -p "Porta HTTP (ex: 8080): " SERVER_PORT

        # Salva configuração
        cat > .node-config << EOF
NODE_ID="$NODE_ID"
NODE_IP="$NODE_IP"
SERVER_PORT="$SERVER_PORT"
DB_HOST="localhost"
EOF
        echo "✅ Configuração salva em .node-config"
    fi
fi

# Gera docker-compose.yml específico
envsubst < docker/docker-compose.template.yml > docker-compose.yml

echo "✅ Docker Compose gerado para:"
echo "   Nó: $NODE_ID"
echo "   IP: $NODE_IP"
echo "   Porta: $SERVER_PORT"
echo "   Banco: avatar_$NODE_ID"

echo ""
echo "🚀 Para iniciar: docker-compose up -d"
echo "📊 Para ver logs: docker-compose logs -f app"