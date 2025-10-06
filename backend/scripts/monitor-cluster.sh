#!/bin/bash

echo "📊 Monitor do Cluster Avatar"

NODES=(
    "172.16.201.10:8080"
    "172.16.201.11:8081"
    "172.16.201.12:8082"
)

while true; do
    clear
    echo "🕐 $(date)"
    echo "================================="

    for node in "${NODES[@]}"; do
        status=$(curl -s "http://$node/health" 2>/dev/null | jq -r '.status' 2>/dev/null || echo "OFFLINE")
        leader=$(curl -s "http://$node/api/cluster/status" 2>/dev/null | jq -r '.isLeader' 2>/dev/null || echo "?")

        if [ "$leader" = "true" ]; then
            leader_icon="👑"
        else
            leader_icon="👥"
        fi

        echo "$leader_icon $node: $status"
    done

    echo "================================="
    echo "Pressione Ctrl+C para parar..."
    sleep 5
done