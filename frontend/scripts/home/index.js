document.addEventListener('DOMContentLoaded', function() {
    const statusIndicator = document.getElementById('status-indicator');
    const journeyButton = document.getElementById('journey-button');
    let isServerOnline = false;

    function updateUI(isOnline) {
        isServerOnline = isOnline;
        if (isOnline) {
            statusIndicator.classList.remove('bg-gray-400', 'bg-red-500');
            statusIndicator.classList.add('bg-green-500');
            journeyButton.textContent = 'Iniciar Jornada';
        } else {
            statusIndicator.classList.remove('bg-gray-400', 'bg-green-500');
            statusIndicator.classList.add('bg-red-500');
            journeyButton.textContent = 'Tentar Novamente';
        }
    }

    function checkServerStatus() {
        journeyButton.textContent = 'Verificando Servidor...';
        
        WebSocketService.connect(
            () => {
                console.log("Servidor online. Conexão de verificação bem-sucedida.");
                updateUI(true);
                WebSocketService.disconnect();
            },
            () => { 
                console.error("Servidor offline ou inacessível.");
                updateUI(false);
            }
        );
    }

    journeyButton.addEventListener('click', function(e) {
        e.preventDefault(); 

        if (isServerOnline) {
            window.location.href = 'screens/signin.html';
        } else {
            console.log("Tentando reconectar...");
            checkServerStatus();
        }
    });

    checkServerStatus();
});
