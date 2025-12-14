/* Creates a list of rooms that have been checked on an add task form to add to the task, ChatGPT used to translate a jQuery tutorial to javascript */
function createAddedRoomsList() {
    const checkedIds = [];
    const checkboxes = document.querySelectorAll('input[type="checkbox"]:checked');
    checkboxes.forEach(function (checkbox) {
        checkedIds.push(checkbox.id);
    });

}

