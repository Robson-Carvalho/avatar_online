const WebSocketService = {
    stompClient: null,
    isConnected: false,

    connect: function(onConnectCallback) {
        if (this.isConnected) {
            console.log("Já está conectado.");
            if (onConnectCallback) onConnectCallback();
            return;
        }

        const socket = new SockJS('http://localhost:8080/gateway-connect');
      
        this.stompClient = new StompJs.Client({
            webSocketFactory: () => socket,
            debug: function (str) {
               
                console.log('STOMP: ' + str);
            },
            reconnectDelay: 5000, 
        });

      
        this.stompClient.onConnect = (frame) => {
            this.isConnected = true;
            console.log('Conectado ao WebSocket:', frame);

          
            this.stompClient.subscribe('/user/topic/update', (message) => {
                this.onMessageReceived(message);
            });

            if (onConnectCallback) {
                onConnectCallback();
            }
        };
      
        this.stompClient.onStompError = (frame) => {
            console.error('Erro no broker STOMP:', frame.headers['message']);
            console.error('Detalhes:', frame.body);
            this.isConnected = false;
        };
      
        this.stompClient.activate();
    },


    onMessageReceived: function(message) {
        console.log("Mensagem recebida do servidor:", message.body);
        const serverEvent = JSON.parse(message.body);
        const eventType = serverEvent.eventType;
        const data = serverEvent.data; 

        // Exemplo de como tratar diferentes tipos de eventos
        switch(eventType) {
            case 'LOGIN_RESPONSE':
                if (data.Status === 'OK') {
                    showNotify("success", "Login realizado com sucesso!");
                    // Exemplo: redirecionar para a página principal
                    window.location.href = '../screens/dashboard.html';
                } else {
                     showNotify("error", data.error || "Falha no login.");
                }
                break;
            case 'SIGNUP_RESPONSE':
                 if (data.Status === 'OK') {
                    showNotify("success", "Cadastro realizado com sucesso! Faça seu login.");
                    // Exemplo: redirecionar para a página de login
                    // window.location.href = '/signin.html';
                } else {
                     showNotify("error", data.error || "Falha no cadastro.");
                }
                break;
            case 'ERROR_RESPONSE':
                showNotify("error", data.error || "Ocorreu um erro.");
                break;
            default:
                console.warn("Tipo de evento não tratado:", eventType);
                break;
        }
    },


    sendCommand: function(commandType, payload) {
        if (!this.isConnected || !this.stompClient) {
            showNotify("error", "Não foi possível enviar o comando. Sem conexão com o servidor.");
            return;
        }

        const clientMessage = {
            commandType: commandType,
            payload: JSON.stringify(payload) 
        };

        this.stompClient.publish({
            destination: '/app/command',
            body: JSON.stringify(clientMessage)
        });

        console.log("Comando enviado:", clientMessage);
    },
  
    disconnect: function() {
        if (this.stompClient) {
            this.stompClient.deactivate();
            this.isConnected = false;
            console.log("Desconectado.");
        }
    }
};
