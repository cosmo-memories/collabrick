document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('addChatMembersModal');
    if (!modal) return;

    const form = modal.querySelector('form');
    if (!form) return;

    const checkboxes = form.querySelectorAll('.user-checkbox');
    let errorDiv = document.getElementById("member-error");
    let errorText = document.getElementById("member-error-message");

    clearErrors();

    modal.addEventListener('hidden.bs.modal', () => {
        clearErrors();
        checkboxes.forEach(cb => cb.checked = false);
    });

    form.addEventListener("submit", (e) => {
        e.preventDefault();
        const anyChecked = Array.from(checkboxes).some(cb => cb.checked);
        if (anyChecked) {
            form.submit();
        } else {
            showErrors();
        }
    });

    function clearErrors() {
        errorDiv.style.display = "none"
        errorText.textContent = "";
    }

    function showErrors() {
        errorDiv.style.display = "block";
        errorText.textContent = "You must select one or more users to add";
    }

});
