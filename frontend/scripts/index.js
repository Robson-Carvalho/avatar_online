const log = (msg) => {
    const logDiv = document.getElementById("log");
    logDiv.innerHTML += `<div>${msg}</div>`;
    logDiv.scrollTop = logDiv.scrollHeight;
};

const socket = new SockJS("http://127.0.0.1:8080/ws");
const stompClient = Stomp.over(socket);

stompClient.connect({}, (frame) => {
    log("✅ Conectado: " + frame);

    // "onmessage" → callback do subscribe
    stompClient.subscribe("/user/queue/response", (message) => {
        const data = JSON.parse(message.body);
        log(`📩 Resposta: ${data.message}`);
    });
});

function sendOperation() {
    const operation = "Operação_" + Math.floor(Math.random() * 100);
    stompClient.send("/app/operation", {}, JSON.stringify({ operation }));
    log(`📤 Enviado: ${operation}`);
}
