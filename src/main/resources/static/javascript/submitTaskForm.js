/**
 * Ensures that the task form passes to the controller
 * if the date is invalid  (i.e. 30th of Feb) or not
 */
const taskForm = document.getElementById('taskForm')
taskForm.setAttribute('novalidate', true)
taskForm.addEventListener('submit', function (e) {
    e.preventDefault(); // Stop default form submission

    const dateInput = document.getElementById('dateInput');

    if (!dateInput.checkValidity()) {
        // Browser considers date invalid (e.g., 30th Feb)
        document.getElementById('dateInvalid').value = "Date Invalid";
    } else {
        document.getElementById('dateInvalid').value = "";
    }
    this.submit();
});