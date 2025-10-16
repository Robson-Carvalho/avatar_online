// Variável global para o cliente STOMP
let stompClient = null;

// Elementos do DOM
const sendBtn = document.getElementById('sendBtn');
const messageInput = document.getElementById('message');
const nameInput = document.getElementById('name');
const messagesDiv = document.getElementById('messages');

function connect() {
    // Cria uma conexão usando SockJS com o endpoint definido no Spring
    const socket = new SockJS('/ws-connect');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, function (frame) {
        console.log('Conectado: ' + frame);
        updateUI(true);

        // ** INSCRIÇÃO CRUCIAL **
        // Se inscreve no tópico de resposta específico do usuário.
        // O Spring enviará mensagens de @SendToUser para este destino.
        stompClient.subscribe('/user/queue/reply', function (message) {
            const response = JSON.parse(message.body);
            displayMessage(`[RESPOSTA PRIVADA] Sessão ${response.replyToSessionId} respondeu: ${response.content}`, 'private-reply');
        });
    }, function(error) {
        console.error('Erro de conexão: ', error);
        updateUI(false);
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    updateUI(false);
    console.log("Desconectado");
}

function sendMessage() {
    const name = nameInput.value.trim();
    const content = messageInput.value.trim();

    if (name && content && stompClient) {
        const clientMessage = {
            from: name,
            content: content
        };

        // Envia a mensagem para o destino que o @MessageMapping no Spring está ouvindo
        stompClient.send("/app/processar", {}, JSON.stringify(clientMessage));

        displayMessage(`[VOCÊ ENVIOU]: ${content}`, 'sent');
        messageInput.value = ''; // Limpa o campo de mensagem
    } else {
        alert('Por favor, digite seu nome e uma mensagem.');
    }
}

function displayMessage(message, cssClass) {
    const messageElement = document.createElement('div');
    messageElement.classList.add('message');
    if (cssClass) {
        messageElement.classList.add(cssClass);
    }
    messageElement.innerText = message;
    messagesDiv.appendChild(messageElement);
    messagesDiv.scrollTop = messagesDiv.scrollHeight; // Auto-scroll
}

function updateUI(isConnected) {
    sendBtn.disabled = !isConnected;
    if (isConnected) {
        displayMessage('Conectado ao servidor WebSocket!', 'system');
    } else {
        displayMessage('Desconectado do servidor WebSocket.', 'system-error');
    }
}


// Event Listener para o botão de enviar
sendBtn.addEventListener('click', sendMessage);

// Conectar assim que a página carregar
connect();