document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.querySelector('form');

    loginForm.addEventListener('submit', function(e) {
        e.preventDefault();

        const nickname = document.getElementById('nickname').value.trim();
        const password = document.getElementById('password').value;

        console.log(nickname);
        console.log(password);

        showNotify("success", "Ação realizada com sucesso!");
    });
});
