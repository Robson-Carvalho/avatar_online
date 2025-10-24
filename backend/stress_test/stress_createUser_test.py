import requests
import threading
import time
import json
import random
import string
import os

# --- CONFIGURAÇÕES DO TESTE ---
LEADER_IP = "localhost"
URL = f"http://{LEADER_IP}:8080/api/users"
NUM_USERS = 300
REQUESTS_PER_USER = 1
TOTAL_REQUESTS = NUM_USERS * REQUESTS_PER_USER
# ------------------------------

all_player_ids = []
lock = threading.Lock()  # Para thread-safe ao adicionar IDs

def generate_payload(thread_id, req_num):
    timestamp = int(time.time() * 1000)
    random_suffix = ''.join(random.choices(string.ascii_letters + string.digits, k=15))
    unique_key = f"{thread_id}_{req_num}_{timestamp}_{random_suffix}"
    return {
        "name": f"user_name_{unique_key}",
        "nickname": f"nick_{unique_key}",
        "email": f"user_{unique_key}@domain.com",
        "password": ''.join(random.choices(string.ascii_letters + string.digits, k=15))
    }


def send_load(thread_id, results_list):
    success_count = 0
    failure_count = 0
    thread_latencies = []

    for req_num in range(REQUESTS_PER_USER):
        payload = generate_payload(thread_id, req_num)

        try:
            start_time = time.time()
            response = requests.post(
                URL,
                headers={'Content-Type': 'application/json'},
                data=json.dumps(payload),
                timeout=20
            )
            end_time = time.time()
            latency = (end_time - start_time) * 1000
            thread_latencies.append(latency)

            if response.status_code == 200:
                success_count += 1
                # Captura o playerId retornado pelo backend
                try:
                    user_data = response.json()
                    player_id = user_data.get("playerId") or user_data.get("id")
                    with lock:
                        all_player_ids.append(player_id)
                except Exception as e:
                    print(f"Thread {thread_id} - Falha ao extrair playerId: {e}")
            else:
                failure_count += 1
                print(f"Thread {thread_id} - Falha HTTP {response.status_code}: {response.text}")

        except requests.exceptions.Timeout:
            failure_count += 1
            print(f"Thread {thread_id} - Timeout da requisição.")
        except requests.exceptions.RequestException as e:
            failure_count += 1
            print(f"Thread {thread_id} - Erro de conexão: {e}")

    results_list.append({
        'success': success_count,
        'failure': failure_count,
        'latencies': thread_latencies
    })


def run_load_test():
    print(f"Iniciando Teste de Carga Raft: {NUM_USERS} usuários x {REQUESTS_PER_USER} requisições = {TOTAL_REQUESTS} total.")
    print(f"Enviando para o Líder em: {URL}")

    threads = []
    all_results = []
    global_start_time = time.time()

    for i in range(NUM_USERS):
        t = threading.Thread(target=send_load, args=(i + 1, all_results))
        threads.append(t)
        t.start()
        time.sleep(0.02)

    for t in threads:
        t.join()

    global_end_time = time.time()
    total_success = sum(r['success'] for r in all_results)
    total_failure = sum(r['failure'] for r in all_results)
    total_latency_ms = [l for r in all_results for l in r['latencies']]
    total_time = global_end_time - global_start_time
    throughput = total_success / total_time

    total_latency_ms.sort()
    avg_latency = sum(total_latency_ms) / len(total_latency_ms) if total_latency_ms else 0
    p90_index = int(len(total_latency_ms) * 0.90) - 1
    p95_index = int(len(total_latency_ms) * 0.95) - 1
    p90_latency = total_latency_ms[p90_index] if total_latency_ms and p90_index >= 0 else 0
    p95_latency = total_latency_ms[p95_index] if total_latency_ms and p95_index >= 0 else 0

    print("\n--- RESULTADOS FINAIS ---")
    print(f"Total de Requisições Enviadas: {TOTAL_REQUESTS}")
    print(f"Total de Sucesso (200 OK): {total_success}")
    print(f"Total de Falha (Erro/Timeout): {total_failure}")
    print(f"Taxa de Erro: {total_failure / TOTAL_REQUESTS * 100:.2f}%")
    print("-" * 30)
    print(f"Tempo Total de Execução: {total_time:.2f} segundos")
    print(f"Throughput (RPS): {throughput:.2f}")
    print("-" * 30)
    print(f"Latência Média: {avg_latency:.2f} ms")
    print(f"Latência 90º Percentil (P90): {p90_latency:.2f} ms")
    print(f"Latência 95º Percentil (P95): {p95_latency:.2f} ms")
    print("-------------------------")

    # Verifica IDs duplicados
    duplicates = set([x for x in all_player_ids if all_player_ids.count(x) > 1])
    print(f"\nTotal IDs gerados: {len(all_player_ids)}")
    print(f"IDs duplicados: {len(duplicates)}")
    if duplicates:
        print("Duplicados:", duplicates)
    else:
        print("Nenhum ID duplicado encontrado!")

if __name__ == "__main__":
    run_load_test()

    # Salvar os PlayerIds gerados em JSON
    import json
    with open("all_player_ids.json", "w") as f:
        json.dump(all_player_ids, f, indent=2)
    print("IDs dos jogadores salvos em all_player_ids.json")
