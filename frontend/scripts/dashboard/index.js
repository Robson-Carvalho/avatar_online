document.addEventListener('DOMContentLoaded', function() {
    const userUUID = sessionStorage.getItem('userUUID');

    if (!userUUID) {
        alert("Você não está autenticado!");
        window.location.href = '../screens/signin.html';
        return;
    }

    WebSocketService.connect(() => {
      showNotify("sucess", "Bem-vindo ao Avatar Online!");
    });
});

