import requests
import threading
import time
import json
import random
from collections import Counter, defaultdict
import os

# --- CONFIGURAÇÕES DO TESTE ---
LEADER_IP = "localhost"
NODE_URLS = [
    f"http://{LEADER_IP}:8081/api/cards/pack",
    f"http://{LEADER_IP}:8082/api/cards/pack",
    f"http://{LEADER_IP}:8083/api/cards/pack",
]

NUM_THREADS = 200
REQUESTS_PER_THREAD = 5
TIMEOUT = 20
SLEEP_BETWEEN_THREAD_STARTS = 0.05

PLAYER_IDS_FILE = "all_player_ids.json" 
lock = threading.Lock()
all_results = []
all_card_ids = []
status_counter = Counter()
player_ids = []
users_cards_map = defaultdict(list)

# Variável global para rastreamento (agora conta como falha)
CHUNK_FAILURE_COUNT = 0 
CHUNK_ERROR_MESSAGES = ["InvalidChunkLength", "Premature end of chunk coded message body"]

def load_player_ids():
    global player_ids
    try:
        with open(PLAYER_IDS_FILE, "r") as f:
            player_ids = json.load(f)
        if len(player_ids) < NUM_THREADS:
            raise RuntimeError(f"É necessário pelo menos {NUM_THREADS} PlayerIds únicos.")
        print(f"Carregado {len(player_ids)} PlayerIds.")
    except Exception as e:
        print(f"❌ Erro ao carregar {PLAYER_IDS_FILE}: {e}")
        exit(1)

def assign_players_to_threads():
    return random.sample(player_ids, NUM_THREADS)

def generate_payload(thread_id, thread_player_ids):
    player_id = thread_player_ids[thread_id]
    return {"PlayerId": player_id}

# send_request: Retorna o erro de chunk como qualquer outro erro de conexão
def send_request(session, url, payload):
    start = time.time()
    try:
        resp = session.post(url, json=payload, timeout=TIMEOUT)
        latency = (time.time() - start) * 1000
        return resp, latency, None 
    except requests.exceptions.RequestException as e:
        latency = (time.time() - start) * 1000
        error_msg = str(e)
        return None, latency, error_msg

# validate_response: Função simples, sem lógica de reclassificação
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

def worker(thread_id, thread_player_ids):
    session = requests.Session()
    successes = 0
    failures = 0
    latencies = []
    local_card_ids = []

    for _ in range(REQUESTS_PER_THREAD):
        target_url = random.choice(NODE_URLS) 
        payload = generate_payload(thread_id, thread_player_ids)
        
        resp, latency, err = send_request(session, target_url, payload) 
        latencies.append(latency)
        
        # 🛑 INÍCIO DA LÓGICA DE TRATAMENTO DE ERROS DE CONEXÃO 🛑
        if err:
            failures += 1
            
            # 1. Checa se é um erro de CHUNK
            if any(msg in err for msg in CHUNK_ERROR_MESSAGES):
                with lock:
                    global CHUNK_FAILURE_COUNT
                    CHUNK_FAILURE_COUNT += 1
                    status_counter["CHUNK_FAILURE"] += 1 # Registra no contador geral
                
                # 🛑 SILENCIADO: Não imprime nada para CHUNK_FAILURE 🛑
            else:
                # 2. Outros erros de conexão/timeout (ERROS REAIS): Imprime e registra
                with lock:
                    status_counter[err] += 1
                # Mantenha o print para depurar outros erros de rede/timeout
                print(f"[Thread {thread_id}] -> {target_url} | Falha REAL de Conexão/Timeout: {err}") 
            
            continue
        # 🛑 FIM DA LÓGICA DE TRATAMENTO DE ERROS DE CONEXÃO 🛑

        # Validação de resposta HTTP (só chega aqui se send_request foi bem-sucedido)
        ok, result = validate_response(resp)

        if ok:
            successes += 1
            local_card_ids.extend(result)
            
            with lock:
                status_counter["http_200"] += 1
                users_cards_map[payload["PlayerId"]].extend(local_card_ids)

        else:
            failures += 1
            with lock:
                status_counter[result] += 1
            # Falha de validação/erro HTTP: Mantenha o print
            print(f"[Thread {thread_id}] -> {target_url} | Falha validação/HTTP: {result}") 

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
    thread_player_ids = assign_players_to_threads()
    total_requests = NUM_THREADS * REQUESTS_PER_THREAD
    
    print("\n--- INFORMAÇÕES DO TESTE ---")
    print(f"URLs Alvo: {NODE_URLS}")
    print(f"Total de requisições: {total_requests}")
    print(f"Threads: {NUM_THREADS} | Req/Thread: {REQUESTS_PER_THREAD}")
    print("----------------------------\n")

    threads = []
    start_global = time.time()
    for i in range(NUM_THREADS):
        t = threading.Thread(target=worker, args=(i, thread_player_ids))
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

    users_with_cards = []
    for user_id, cards in users_cards_map.items():
        unique_cards = list(cards)
        users_with_cards.append({
            "userId": user_id,
            "cards": unique_cards
        })

    print("\n--- RESULTADOS ---")
    print(f"Total requisições: {total_requests}")
    print(f"Sucessos HTTP (200 + JSON OK): {total_success}")
    print(f"Falhas TOTAIS: {total_failure}")
    print(f"Falhas de Chunk Ocultas: {CHUNK_FAILURE_COUNT}")
    print(f"Taxa de erro: {total_failure/total_requests*100:.2f}%")
    print(f"Tempo total: {total_time:.2f}s | Throughput: {throughput:.2f} req/s")
    print(f"Lat média: {avg_latency:.2f} ms | P90: {p90:.2f} ms | P95: {p95:.2f} ms")
    print("Status counts:", dict(status_counter))
    print(f"Total cartas recebidas: {len(all_card_ids)} | Exemplo primeiros IDs: {all_card_ids[:20]}")

    out = {
        "total_requested": total_requests,
        "success": total_success,
        "failure_total": total_failure,
        "chunk_failures": CHUNK_FAILURE_COUNT,
        "time_seconds": total_time,
        "throughput_req_s": throughput,
        "avg_latency_ms": avg_latency,
        "p90_latency_ms": p90,
        "p95_latency_ms": p95,
        "status_counts": dict(status_counter),
        "sample_card_ids": all_card_ids[:200],
    }

    with open("results_open_packages_real_players.json", "w") as f:
        json.dump(out, f, indent=2, ensure_ascii=False)
    print("Resultados salvos em results_open_packages_real_players.json")

    with open("users_with_cards.json", "w") as f:
        json.dump(users_with_cards, f, indent=2, ensure_ascii=False)
    print("JSON de usuários com cartas salvo em users_with_cards.json")

if __name__ == "__main__":
    run_load_test()