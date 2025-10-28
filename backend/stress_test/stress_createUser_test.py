import requests
import threading
import time
import json
import random
import string
import os

# --- CONFIGURAÇÕES DO TESTE ---
SERVER_IP1 = "localhost"
SERVER_IP2 = "172.16.201.5"
SERVER_IP3 = "172.16.201.8"
# 🎯 NOVA LISTA DE NÓS: Altere as portas conforme a configuração real dos seus nós Raft
NODE_URLS = [
    f"http://{SERVER_IP1}:8080/api/users",
    f"http://{SERVER_IP2}:8080/api/users",
    f"http://{SERVER_IP3}:8080/api/users",
]
# Use a primeira URL para o Warm-up, se necessário, ou escolha um nó conhecido
WARMUP_URL = NODE_URLS[2] 

NUM_USERS = 300
REQUESTS_PER_USER = 1
TOTAL_REQUESTS = NUM_USERS * REQUESTS_PER_USER
# ------------------------------

all_player_ids = []
lock = threading.Lock()  # Para thread-safe ao adicionar IDs

def generate_payload(thread_id, req_num, is_dummy=False):
    timestamp = int(time.time() * 1000)
    random_suffix = ''.join(random.choices(string.ascii_letters + string.digits, k=15))
    
    if is_dummy:
        unique_key = f"DUMMY_WARMUP{timestamp}"
        email = f"dummy_user_{timestamp}@warmup.com"
    else:
        unique_key = f"{thread_id}_{req_num}_{timestamp}_{random_suffix}"
        email = f"user_{unique_key}@domain.com"
        
    return {
        "name": f"user_name_{unique_key}_{timestamp}",
        "nickname": f"nick_{unique_key}",
        "email": email,
        "password": ''.join(random.choices(string.ascii_letters + string.digits, k=15))
    }

# --- FUNÇÃO WARM-UP ATUALIZADA ---
def warm_up_cluster():
    """Envia uma requisição única para estabilizar o cluster Raft/DB."""
    print("\n--- INICIANDO WARM-UP ---")
    payload = generate_payload(0, 0, is_dummy=True)
    
    try:
        start_time = time.time()
        response = requests.post(
            WARMUP_URL,
            headers={'Content-Type': 'application/json'},
            data=json.dumps(payload),
            timeout=10
        )
        end_time = time.time()
        
        if response.status_code == 200 or response.status_code == 201:
            print(f"✅ Warm-up bem-sucedido. Log 1 (Dummy User) criado e commitado em {end_time - start_time:.2f}s (via {WARMUP_URL}).")
            return True
        else:
            print(f"❌ Falha no Warm-up HTTP {response.status_code}: {response.text}")
            return False
    except requests.exceptions.RequestException as e:
        print(f"❌ Falha de Conexão no Warm-up: {e}")
        print("Aguarde mais e tente novamente.")
        return False
# ------------------------------


def send_load(thread_id, results_list):
    success_count = 0
    failure_count = 0
    thread_latencies = []

    for req_num in range(REQUESTS_PER_USER):
        target_url = random.choice(NODE_URLS) 
        payload = generate_payload(thread_id, req_num) 

        try:
            start_time = time.time()
            response = requests.post(
                target_url, # Usa a URL aleatória
                headers={'Content-Type': 'application/json'},
                data=json.dumps(payload),
                timeout=20
            )
            end_time = time.time()
            latency = (end_time - start_time) * 1000
            thread_latencies.append(latency)

            if response.status_code == 200 or response.status_code == 201:
                success_count += 1
                try:
                    user_data = response.json()
                    player_id = user_data.get("playerId") or user_data.get("id")
                    if player_id:
                        with lock:
                            all_player_ids.append(player_id)
                except Exception as e:
                    # Não imprimir a cada falha, só se a extração falhar após o 200/201
                    pass 
            else:
                failure_count += 1
                # O erro InvalidChunkLength vai aparecer aqui!
                print(f"Thread {thread_id} -> {target_url} - Falha HTTP {response.status_code}: {response.text}") 

        except requests.exceptions.Timeout:
            failure_count += 1
            print(f"Thread {thread_id} -> {target_url} - Timeout da requisição.")
        except requests.exceptions.RequestException as e:
            failure_count += 1
            # O erro InvalidChunkLength (que é um RequestException) aparece aqui
            print(f"Thread {thread_id} -> {target_url} - Erro de Conexão/Chunk: {e}") 

    results_list.append({
        'success': success_count,
        'failure': failure_count,
        'latencies': thread_latencies
    })


def run_load_test():
    print(f"Nós de Cluster Alvo: {NODE_URLS}")
    
    if not warm_up_cluster():
        print("\n!!! TESTE DE CARGA ABORTADO: Falha na estabilização do cluster (Warm-up).")
        return

    print(f"\nIniciando Carga: {NUM_USERS} usuários x {REQUESTS_PER_USER} requisições = {TOTAL_REQUESTS} total.")

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
    
    # Excluímos o ID do dummy user
    if all_player_ids and 'DUMMY_WARMUP' in all_player_ids[0]:
        all_player_ids.pop(0)

    # --- CÁLCULO DE MÉTRICAS (Igual ao original) ---
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
    print(f"Total de Requisições Enviadas (Carga): {TOTAL_REQUESTS}")
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

    duplicates = set([x for x in all_player_ids if all_player_ids.count(x) > 1])
    print(f"\nTotal IDs gerados: {len(all_player_ids)}")
    print(f"IDs duplicados: {len(duplicates)}")
    if duplicates:
        print("Duplicados:", duplicates)
    else:
        print("Nenhum ID duplicado encontrado!")

if __name__ == "__main__":
    run_load_test()

    import json
    with open("all_player_ids.json", "w") as f:
        json.dump(all_player_ids, f, indent=2)
    print("IDs dos jogadores salvos em all_player_ids.json")