#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Teste de carga de abertura de pacotes usando PlayerIds reais.
- Endpoint: POST /api/cards/pack
- Payload: {"PlayerId": "<id>"}
- Lista de PlayerIds: arquivo JSON ou lista em memória
"""

import requests
import threading
import time
import json
import random
from collections import Counter

LEADER_IP = "localhost"
PORT = 8081
URL = f"http://{LEADER_IP}:{PORT}/api/cards/pack"

NUM_THREADS = 2
REQUESTS_PER_THREAD = 1
TIMEOUT = 20
SLEEP_BETWEEN_THREAD_STARTS = 0.05

PLAYER_IDS_FILE = "all_player_ids.json"  # arquivo JSON com lista de PlayerIds
lock = threading.Lock()
all_results = []
all_card_ids = []
status_counter = Counter()
player_ids = []

# Carrega os PlayerIds de arquivo JSON
def load_player_ids():
    global player_ids
    try:
        with open(PLAYER_IDS_FILE, "r") as f:
            player_ids = json.load(f)
        print(f"Carregado {len(player_ids)} PlayerIds.")
    except Exception as e:
        print(f"Erro ao carregar {PLAYER_IDS_FILE}: {e}")
        exit(1)

def generate_payload():
    player_id = random.choice(player_ids)
    return {"PlayerId": player_id}

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
    try:
        data = resp.json()
        card_ids = [str(card.get("id")) for card in data]
        return True, card_ids
    except Exception:
        return False, "json_parse_error"

def worker(thread_id):
    session = requests.Session()
    successes = 0
    failures = 0
    latencies = []
    local_card_ids = []

    for _ in range(REQUESTS_PER_THREAD):
        payload = generate_payload()
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
            local_card_ids.extend(result)
            with lock:
                status_counter["http_200"] += 1
        else:
            failures += 1
            with lock:
                status_counter[result] += 1
            print(f"[Thread {thread_id}] Falha validação: {result}")

    with lock:
        all_card_ids.extend(local_card_ids)
        all_results.append({
            "thread": thread_id,
            "success": successes,
            "failure": failures,
            "latencies": latencies
        })

def run_load_test():
    load_player_ids()
    total_requests = NUM_THREADS * REQUESTS_PER_THREAD
    print(f"Iniciando teste de abertura de pacotes: {NUM_THREADS} threads x {REQUESTS_PER_THREAD} req = {total_requests}")

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

    card_counts = Counter(all_card_ids)
    duplicates = {card_id: count for card_id, count in card_counts.items() if count > 1}

    print("\n--- RESULTADOS ---")
    print(f"Total requisições: {total_requests}")
    print(f"Sucessos: {total_success}")
    print(f"Falhas: {total_failure}")
    print(f"Taxa de erro: {total_failure/total_requests*100:.2f}%")
    print(f"Tempo total: {total_time:.2f}s | Throughput: {throughput:.2f} req/s")
    print(f"Lat média: {avg_latency:.2f} ms | P90: {p90:.2f} ms | P95: {p95:.2f} ms")
    print("Status counts:", dict(status_counter))
    print(f"Total cartas recebidas: {len(all_card_ids)} | Exemplo primeiros IDs: {all_card_ids[:20]}")
    if duplicates:
        print("IDs duplicados e quantidades:")
        for card_id, count in list(duplicates.items())[:20]:  # mostrar só os primeiros 20
            print(f"{card_id} => {count} vezes")
    else:
        print("Nenhuma carta duplicada encontrada!")

    # salvar resultados
    out = {
        "total_requested": total_requests,
        "success": total_success,
        "failure": total_failure,
        "time_seconds": total_time,
        "throughput_req_s": throughput,
        "avg_latency_ms": avg_latency,
        "p90_latency_ms": p90,
        "p95_latency_ms": p95,
        "status_counts": dict(status_counter),
        "sample_card_ids": all_card_ids[:200],
        "duplicates": duplicates  # adiciona info de duplicados no JSON
    }

    with open("results_open_packages_real_players.json", "w") as f:
        json.dump(out, f, indent=2, ensure_ascii=False)
    print("Resultados salvos em results_open_packages_real_players.json")

if __name__ == "__main__":
    run_load_test()
