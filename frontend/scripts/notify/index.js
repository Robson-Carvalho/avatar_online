const toastConfig = {
    duration: 1500,
    gravity: "top",
    position: "right",
    stopOnFocus: true,
    close: true,
    className: "custom-toast",
    escapeMarkup: false
};

function showSuccess(message = "Operação realizada com sucesso!") {
    Toastify({
        ...toastConfig,
        text: `✅ ${message}`,
        style: {
            background: "linear-gradient(135deg, #4CAF50, #45a049)",
            borderRadius: "8px",
            fontWeight: "500"
        }
    }).showToast();
}

function showError(message = "Ocorreu um erro inesperado!") {
    Toastify({
        ...toastConfig,
        text: `${message}`,
        duration: 5000,
        style: {
            background: "linear-gradient(135deg, #f44336, #da190b)",
            borderRadius: "8px",
            fontWeight: "500"
        }
    }).showToast();
}

function showWarning(message = "Atenção! Verifique os dados.") {
    Toastify({
        ...toastConfig,
        text: `${message}`,
        style: {
            background: "linear-gradient(135deg, #ff9800, #e68900)",
            borderRadius: "8px",
            fontWeight: "500"
        }
    }).showToast();
}

function showInfo(message = "Esta é uma informação importante.") {
    Toastify({
        ...toastConfig,
        text: `${message}`,
        style: {
            background: "linear-gradient(135deg, #2196F3, #0b7dda)",
            borderRadius: "8px",
            fontWeight: "500"
        }
    }).showToast();
}