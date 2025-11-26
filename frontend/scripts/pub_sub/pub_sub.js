document.addEventListener("DOMContentLoaded", () => {
  updateViewsBasedOnConnection();
});

const log = (msg) => {
  console.log(msg);
};

let attempts = 0;
let stompClient = null;

connect();

function connect(host = "127.0.0.1") {
  try {
    const socket = new SockJS(`http://${host}:8081/ws`, null, {
      timeout: 200,
    });

    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    log("🎛️ Conectando ao servidor...");
    socket.onopen = function () {
      attempts = 0;
      log("🔗 Socket aberto com sucesso");
      updateViewsBasedOnConnection();
    };

    socket.onclose = function (event) {
      log(`❌ Socket fechado: código ${event.code}, motivo: ${event.reason}`);
      stopPingLoop();
      updateViewsBasedOnConnection();
      attemptReconnect();
    };

    socket.onerror = function (error) {
      log("❌ Erro no socket: " + JSON.stringify(error));
      updateViewsBasedOnConnection();
      attemptReconnect();
    };

    stompClient.connect(
      {},
      function (frame) {
        log("✅ STOMP Conectado: " + frame);
        startPingLoop();
        const user = getUser();

        if (user) {
          verifyAuth(user.id);
        }

        stompClient.subscribe("/user/queue/response", function (message) {
          handlerMain(message);
        });

        sendOperationGetOnlineUsers();
        updateViewsBasedOnConnection();
      },
      function (error) {
        log("❌ Erro STOMP: " + error.toString());
        cleanGame();
        attemptReconnect();
      }
    );
  } catch (error) {
    log("💥 Erro ao criar conexão: " + error.toString());
    attemptReconnect();
  }
}

let useLocalhost = true;
function attemptReconnect() {
  log(`🔄 Tentando reconexão!`);
  updateViewsBasedOnConnection();

  let hostBase = "172.16.201.";

  setTimeout(() => {
    if (!stompClient || !stompClient.connected) {
      let targetHost;

      if (useLocalhost) {
        targetHost = "127.0.0.1";
        console.log("🎯 Target host: " + targetHost);
      } else {
        attempts = (attempts % 30) + 1; // Mantém entre 1-30
        targetHost = hostBase + attempts.toString();
        console.log("🎯 Target host: " + targetHost);
      }

      useLocalhost = !useLocalhost;
      connect(targetHost);
    }
  }, 1000);
}
