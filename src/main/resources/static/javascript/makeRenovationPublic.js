const publicRenovationCheck = document.getElementById('publicRenovation')
const publicRenovationForm = document.getElementById('visibilityForm')
const confirmationModalElement = document.getElementById('makePublicModal')
confirmationModal = new bootstrap.Modal(confirmationModalElement)

publicRenovationCheck.addEventListener('change', function (event) {
    if (event.target.checked) {
        confirmationModal.show();
    } else {
        publicRenovationForm.submit();
    }
})

confirmationModalElement.addEventListener('hidden.bs.modal', function (e) {
    publicRenovationCheck.checked=false;
})