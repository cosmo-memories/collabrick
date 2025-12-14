document.addEventListener('DOMContentLoaded', function () {
    setTimeout(() => {
        document.querySelectorAll(".confirmationToast").forEach(toast => {
            toast.classList.add('fade-out');
        });
    }, 4500);

    setTimeout(() => {
        document.querySelectorAll(".confirmationToast").forEach(toast => {
            toast.classList.remove('show');
        });
    }, 5500);
});
