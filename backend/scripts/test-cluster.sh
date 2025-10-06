#!/bin/bash

echo "🧪 Teste do Cluster Avatar"

# Lista de nós para testar (cada máquina pode customizar)
NODES=(
    "172.16.201.10:8080"
    "172.16.201.11:8081"
    "172.16.201.12:8082"
)

test_node() {
    local url="http://$1"
    echo "🔍 Testando $url"

    # Health check
    if curl -s -f "$url/health" > /dev/null; then
        echo "  ✅ Health: OK"

        # Cluster status
        echo "  📊 Cluster Status:"
        curl -s "$url/api/cluster/status" | jq -r '
            "    Nó: \(.nodeInfo)",
            "    Líder: \(.isLeader)",
            "    Tamanho: \(.clusterSize)",
            "    Líder Info: \(.leaderInfo)"
        ' 2>/dev/null || curl -s "$url/api/cluster/status"
    else
        echo "  ❌ Health: FALHOU"
    fi
    echo ""
}

echo "🌐 Testando todos os nós..."
for node in "${NODES[@]}"; do
    test_node "$node"
done

echo "🎯 Teste de criação de usuário (apenas no líder)..."
# Encontra o líder
for node in "${NODES[@]}"; do
    if curl -s "$node/api/cluster/status" | grep -q '"isLeader":true'; then
        echo "  👑 Criando usuário no líder: $node"
        curl -X POST "$node/api/users" \
            -H "Content-Type: application/json" \
            -d '{
                "name": "Usuario Teste",
                "email": "teste@cluster.com",
                "password": "123456"
            }'
        break
    fi
done

echo ""
echo "✅ Teste completo!"