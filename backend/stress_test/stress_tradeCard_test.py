#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Teste de carga para trocas de cartas entre dois jogadores reais.
Endpoint: POST /api/cards/trade
Payload:
{
  "PLayerId1": "<id_player1>",
  "PLayerId2": "<id_player2>",
  "CardId1": "<id_card_player1>",
  "CardId2": "<id_card_player2>"
}
"""

import requests
import threading
import time
import json
import random
from collections import Counter

# Configurações de rede
LEADER_IP = "localhost"
PORT = 8081
URL = f"http://{LEADER_IP}:{PORT}/api/cards/trade"

# Configurações do teste
NUM_THREADS = 200
REQUESTS_PER_THREAD = 1
TIMEOUT = 20
SLEEP_BETWEEN_THREAD_STARTS = 0.03

# Dados de entrada
USERS_FILE = "users_with_cards.json"  # [{"userId": "...", "cards": ["...","..."]}, ...]

# Variáveis globais
lock = threading.Lock()
users_data = []
status_counter = Counter()
results = []
duplicate_trades = 0

def load_users_data():
    global users_data
    try:
        with open(USERS_FILE, "r") as f:
            users_data = json.load(f)
        print(f"✅ Carregado {len(users_data)} usuários com cartas disponíveis.")
    except Exception as e:
        print(f"❌ Erro ao carregar {USERS_FILE}: {e}")
        exit(1)

def generate_trade_payload():
    """Gera uma troca entre dois usuários distintos e uma carta de cada."""
    user1, user2 = random.sample(users_data, 2)
    cards1 = user1["cards"]
    cards2 = user2["cards"]

    if not cards1 or not cards2:
        raise RuntimeError("Usuário sem cartas suficientes.")

    card1 = random.choice(cards1)
    card2 = random.choice(cards2)

    payload = {
        "PLayerId1": user1["userId"],
        "PLayerId2": user2["userId"],
        "CardId1": card1,
        "CardId2": card2
    }

    # Valida duplicidade (não deve ocorrer)
    if card1 == card2:
        global duplicate_trades
        with lock:
            duplicate_trades += 1

    return payload

def send_trade_request(session, payload):
    start = time.time()
    try:
        resp = session.post(URL, json=payload, timeout=TIMEOUT)
        latency = (time.time() - start) * 1000
        return resp, latency, None
    except requests.exceptions.RequestException as e:
        latency = (time.time() - start) * 1000
        return None, latency, str(e)

def validate_response(resp):
    if resp is None:
        return False, "no_response"
    if resp.status_code != 200:
        return False, f"http_{resp.status_code}"
    try:
        json.loads(resp.text)
        return True, None
    except Exception:
        return False, "invalid_json"

def worker(thread_id):
    session = requests.Session()
    successes, failures = 0, 0
    latencies = []

    for _ in range(REQUESTS_PER_THREAD):
        try:
            payload = generate_trade_payload()
        except Exception as e:
            with lock:
                status_counter["payload_error"] += 1
            print(f"[Thread {thread_id}] Erro payload: {e}")
            failures += 1
            continue

        resp, latency, err = send_trade_request(session, payload)
        latencies.append(latency)

        if err:
            with lock:
                status_counter[err] += 1
            print(f"[Thread {thread_id}] Erro requisição: {err}")
            failures += 1
            continue

        ok, err_status = validate_response(resp)
        with lock:
            status_counter["http_200" if ok else err_status] += 1

        if ok:
            successes += 1
        else:
            print(f"[Thread {thread_id}] Falha validação: {err_status}")
            failures += 1

    with lock:
        results.append({
            "thread": thread_id,
            "success": successes,
            "failure": failures,
            "latencies": latencies
        })

def run_load_test():
    load_users_data()
    total_requests = NUM_THREADS * REQUESTS_PER_THREAD
    print(f"🚀 Iniciando teste de troca de cartas: {NUM_THREADS} threads x {REQUESTS_PER_THREAD} req = {total_requests}")

    threads = []
    start_global = time.time()

    for i in range(NUM_THREADS):
        t = threading.Thread(target=worker, args=(i+1,))
        threads.append(t)
        t.start()
        time.sleep(SLEEP_BETWEEN_THREAD_STARTS)

    for t in threads:
        t.join()

    end_global = time.time()
    total_time = end_global - start_global

    total_success = sum(r["success"] for r in results)
    total_failure = sum(r["failure"] for r in results)
    latencies = [l for r in results for l in r["latencies"]]
    latencies.sort()

    avg_latency = sum(latencies) / len(latencies) if latencies else 0
    p90 = latencies[int(len(latencies)*0.9)-1] if latencies else 0
    p95 = latencies[int(len(latencies)*0.95)-1] if latencies else 0
    throughput = total_success / total_time if total_time > 0 else 0

    print("\n📊 --- RESULTADOS ---")
    print(f"Total de requisições: {total_requests}")
    print(f"Sucessos: {total_success}")
    print(f"Falhas: {total_failure}")
    print(f"Duplicações detectadas: {duplicate_trades}")
    print(f"Taxa de erro: {total_failure/total_requests*100:.2f}%")
    print(f"Tempo total: {total_time:.2f}s | Throughput: {throughput:.2f} req/s")
    print(f"Latência média: {avg_latency:.2f} ms | P90: {p90:.2f} ms | P95: {p95:.2f} ms")
    print("Status counts:", dict(status_counter))

    # salvar resultados
    out = {
        "total_requests": total_requests,
        "success": total_success,
        "failure": total_failure,
        "duplicate_trades": duplicate_trades,
        "error_rate_percent": total_failure/total_requests*100 if total_requests else 0,
        "time_seconds": total_time,
        "throughput_req_s": throughput,
        "avg_latency_ms": avg_latency,
        "p90_latency_ms": p90,
        "p95_latency_ms": p95,
        "status_counts": dict(status_counter)
    }

    with open("results_trade_cards.json", "w") as f:
        json.dump(out, f, indent=2, ensure_ascii=False)

    print("📁 Resultados salvos em results_trade_cards.json")

if __name__ == "__main__":
    run_load_test()
