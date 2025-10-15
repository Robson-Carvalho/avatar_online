function showNotify(type, message) {
    const styles = {
        success: {
            background: "#4CAF50", // verde
            color: "#fff"
        },
        warning: {
            background: "#FFB300", // amarelo queimado
            color: "#000"
        },
        alert: {
            background: "#2196F3", // azul suave
            color: "#fff"
        },
        error: {
            background: "#F44336", // vermelho
            color: "#fff"
        }
    };

    const toastStyle = styles[type] || styles.alert;

    Toastify({
        text: message || "Notificação",
        duration: 3500,
        gravity: "top",
        position: "right",
        close: true,
        stopOnFocus: true,
        style: {
            background: toastStyle.background,
            color: toastStyle.color,
            borderRadius: "8px",
            fontFamily: "system-ui, sans-serif",
            fontSize: "0.9rem",
            padding: "10px 16px",
            boxShadow: "0 4px 14px rgba(0,0,0,0.1)",
            letterSpacing: "0.3px"
        }
    }).showToast();
}

// Exemplos de uso:
// notify("success", "Ação realizada com sucesso!");
// notify("warning", "Verifique as informações antes de continuar.");
// notify("alert", "Conexão restabelecida.");
// notify("error", "Ocorreu um erro inesperado!");
