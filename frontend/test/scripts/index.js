const log = (msg) => {
    const logDiv = document.getElementById("log");
    logDiv.innerHTML += `<div>${new Date().toLocaleTimeString()} - ${msg}</div>`;
    logDiv.scrollTop = logDiv.scrollHeight;
    console.log(msg);
};

let stompClient = null;

connect();

function connect() {
    log("🎛️ Conectando ao servidor...");
    
    try {
        const socket = new SockJS("http://127.0.0.1:8080/ws");
        stompClient = Stomp.over(socket);
        
        stompClient.debug = null;
        
        socket.onopen = function() {
            log("🔗 Socket aberto com sucesso");
        };
        
        socket.onclose = function(event) {
            log(`❌ Socket fechado: código ${event.code}, motivo: ${event.reason}`);
            alert("⚠️ Conexão com o servidor perdida!");
            attemptReconnect();
        };
        
        socket.onerror = function(error) {
            log("❌ Erro no socket: " + JSON.stringify(error));
            alert("⚠️ Erro de conexão com o servidor!");
            attemptReconnect();
        };
        
        stompClient.connect({}, 
            function(frame) {
                log("✅ STOMP Conectado: " + frame);
                reconnectAttempts = 0; // Reseta tentativas
                
                stompClient.subscribe("/user/queue/response", function(message) {
                    const data = JSON.parse(message.body);
                    log(`📩 Resposta: ${data.message}`);
                });
            },
            function(error) {
                log("❌ Erro STOMP: " + error.toString());
                alert("⚠️ Falha na conexão STOMP!");
                attemptReconnect();
            }
        );
        
    } catch (error) {
        log("💥 Erro ao criar conexão: " + error.toString());
        alert("⚠️ Erro crítico na conexão!");
        attemptReconnect();
    }
}

function attemptReconnect() {
    log(`🔄 Tentando reconexão!`);
    
    setTimeout(() => {
        if (!stompClient || !stompClient.connected) {
            connect();
        }
    }, 2000);
}

function sendOperation() {
    if (!stompClient || !stompClient.connected) {
        alert("⚠️ Não conectado ao servidor!");
        return;
    }
    
    const operation = "Operação_" + Math.floor(Math.random() * 100);
    stompClient.send("/app/operation", {}, JSON.stringify({ operation }));
    log(`📤 Enviado: ${operation}`);
}
