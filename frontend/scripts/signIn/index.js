document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.querySelector('form');

    WebSocketService.connect(() => {
        console.log("Conexão WebSocket estabelecida e pronta para uso.");
    });

    loginForm.addEventListener('submit', function(e) {
        e.preventDefault();

        const nickname = document.getElementById('nickname').value.trim();
        const password = document.getElementById('password').value;

        if (!nickname || !password) {
            showNotify("error", "Por favor, preencha nickname e senha.");
            return;
        }

        const signInPayload = {
            nickname: nickname,
            password: password
        };
    
        WebSocketService.sendCommand('signIn', signInPayload);
    });
});