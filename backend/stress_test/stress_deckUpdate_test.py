#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Teste de carga de escolha de deck usando PlayerIds reais.
- Endpoint: POST /api/deck
- Payload: {"userId": "<id>", "card1Id": "...", "card2Id": "...", ...}
- Lista de PlayerIds e suas cartas: arquivo JSON ou lista em memória
"""

import requests
import threading
import time
import json
import random
from collections import Counter

LEADER_IP = "localhost"
PORT = 8080
URL = f"http://{LEADER_IP}:{PORT}/api/deck"

NUM_THREADS = 100
REQUESTS_PER_THREAD = 1
TIMEOUT = 20
SLEEP_BETWEEN_THREAD_STARTS = 0.05

USERS_FILE = "users_with_cards.json"  # {"user_id1": ["card1","card2",...], ...}
lock = threading.Lock()
all_results = []
status_counter = Counter()
duplicate_deck_count = 0
users_data = {}

def load_users_data():
    global users_data
    try:
        with open(USERS_FILE, "r") as f:
            users_list = json.load(f)
            users_data = {u["userId"]: u["cards"] for u in users_list}
        print(f"Carregado {len(users_data)} usuários com suas cartas.")
    except Exception as e:
        print(f"Erro ao carregar {USERS_FILE}: {e}")
        exit(1)

def generate_payload():
    user_id = random.choice(list(users_data.keys()))
    cards = users_data[user_id]

    if len(cards) < 5:
        raise RuntimeError(f"Usuário {user_id} tem menos de 5 cartas!")

    selected_cards = random.sample(cards, 5)
    payload = {
        "userId": user_id,
        "card1Id": selected_cards[0],
        "card2Id": selected_cards[1],
        "card3Id": selected_cards[2],
        "card4Id": selected_cards[3],
        "card5Id": selected_cards[4]
    }

    if len(set(selected_cards)) < 5:
        global duplicate_deck_count
        with lock:
            duplicate_deck_count += 1

    return payload

def send_request(session, payload):
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
    if resp.text.strip() == "":
        return True, None
    try:
        data = resp.json()
        return True, data
    except Exception:
        print(f"Resposta inválida do servidor: {resp.text}")
        return False, "json_parse_error"

def worker(thread_id):
    session = requests.Session()
    successes = 0
    failures = 0
    latencies = []

    for _ in range(REQUESTS_PER_THREAD):
        try:
            payload = generate_payload()
        except Exception as e:
            print(f"[Thread {thread_id}] Erro ao gerar payload: {e}")
            with lock:
                status_counter["payload_error"] += 1
            failures += 1
            continue

        resp, latency, err = send_request(session, payload)
        latencies.append(latency)

        if err:
            failures += 1
            with lock:
                status_counter[err] += 1
            print(f"[Thread {thread_id}] Erro: {err}")
            continue

        ok, result = validate_response(resp)
        if ok:
            successes += 1
            with lock:
                status_counter["http_200"] += 1
        else:
            failures += 1
            with lock:
                status_counter[result] += 1
            print(f"[Thread {thread_id}] Falha validação: {result}")

    with lock:
        all_results.append({
            "thread": thread_id,
            "success": successes,
            "failure": failures,
            "latencies": latencies
        })

def run_load_test():
    load_users_data()
    total_requests = NUM_THREADS * REQUESTS_PER_THREAD
    print(f"Iniciando teste de escolha de deck: {NUM_THREADS} threads x {REQUESTS_PER_THREAD} req = {total_requests}")

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

    total_success = sum(r["success"] for r in all_results)
    total_failure = sum(r["failure"] for r in all_results)
    latencies = [l for r in all_results for l in r["latencies"]]
    total_time = end_global - start_global
    throughput = total_success / total_time if total_time > 0 else 0

    latencies.sort()
    avg_latency = sum(latencies)/len(latencies) if latencies else 0
    p90 = latencies[int(len(latencies)*0.90)-1] if latencies else 0
    p95 = latencies[int(len(latencies)*0.95)-1] if latencies else 0

    print("\n--- RESULTADOS ---")
    print(f"Total requisições: {total_requests}")
    print(f"Sucessos: {total_success}")
    print(f"Falhas: {total_failure}")
    print(f"Taxa de erro: {total_failure/total_requests*100:.2f}%")
    print(f"Cartões duplicados detectados: {duplicate_deck_count}")
    print(f"Tempo total: {total_time:.2f}s | Throughput: {throughput:.2f} req/s")
    print(f"Lat média: {avg_latency:.2f} ms | P90: {p90:.2f} ms | P95: {p95:.2f} ms")
    print("Status counts:", dict(status_counter))

    out = {
        "total_requested": total_requests,
        "success": total_success,
        "failure": total_failure,
        "duplicate_deck_count": duplicate_deck_count,
        "time_seconds": total_time,
        "throughput_req_s": throughput,
        "avg_latency_ms": avg_latency,
        "p90_latency_ms": p90,
        "p95_latency_ms": p95,
        "status_counts": dict(status_counter)
    }
    with open("results_set_deck.json", "w") as f:
        json.dump(out, f, indent=2, ensure_ascii=False)
    print("Resultados salvos em results_set_deck.json")

if __name__ == "__main__":
    run_load_test()
