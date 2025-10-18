document.addEventListener("DOMContentLoaded", ()=>{
    updateViewsBasedOnConnection()
})

const log = (msg) => {
    console.log(msg);
};

let stompClient = null;

connect();

function connect() {
    log("🎛️ Conectando ao servidor...");

    try {
        const socket = new SockJS("http://127.0.0.1:8081/ws");
        stompClient = Stomp.over(socket);
        stompClient.debug = null;
        
        socket.onopen = function() {
            log("🔗 Socket aberto com sucesso");
            updateViewsBasedOnConnection();
        };
        
        socket.onclose = function(event) {
            log(`❌ Socket fechado: código ${event.code}, motivo: ${event.reason}`);
            updateViewsBasedOnConnection();
            attemptReconnect();
        };
        
        socket.onerror = function(error) {
            log("❌ Erro no socket: " + JSON.stringify(error));
            updateViewsBasedOnConnection();
            attemptReconnect();
        };
        
        stompClient.connect({}, 
            function(frame) {
                log("✅ STOMP Conectado: " + frame);
            
                stompClient.subscribe("/user/queue/response", function(message) {
                    handlerMain(message)
                });

                updateViewsBasedOnConnection();
            },
            function(error) {
                log("❌ Erro STOMP: " + error.toString());
                attemptReconnect();
            }
        );
        
    } catch (error) {
        log("💥 Erro ao criar conexão: " + error.toString());
        attemptReconnect();
    }
}

function attemptReconnect() {
    log(`🔄 Tentando reconexão!`);
    updateViewsBasedOnConnection();
    
    setTimeout(() => {
        if (!stompClient || !stompClient.connected) {
            connect();
        }
    }, 2000);
}
