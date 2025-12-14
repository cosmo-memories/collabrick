/**
 * Function for general autocomplete key behaviour
 * @param e keydown event
 * @param items list of div elements containing the autocomplete suggestions
 * @param currentFocusIndex the index of the autocomplete item currently focused on
 * @param manualInputFunction the function to be invoked when the add/confirm button is pressed
 */
function autocompleteKeyBehaviour(e, items, currentFocusIndex, manualInputFunction) {

    // ChatGPT assisted in creating the tabbing and entering functionality

    function updateHighlight(items) {
        const element = items[currentFocusIndex]
        element.focus();
        items.forEach((item, index) => {
            item.classList.toggle("autocomplete-active", index === currentFocusIndex);
        });
    }

    if (e.key === "ArrowDown" || e.key === "Tab") {
        e.preventDefault()
        currentFocusIndex++;
        if (currentFocusIndex >= items.length) {
            currentFocusIndex = 0; // Wrap around back to first suggestion
        }
        updateHighlight(items);

    } else if (e.key === "ArrowUp") {
        currentFocusIndex--;
        if (currentFocusIndex < 0) {
            currentFocusIndex = items.length - 1; // Wrap around back to last suggestion
        }
        updateHighlight(items);
        e.preventDefault()
    } else if (e.key === "Enter") {
        if (currentFocusIndex > -1) {
            items[currentFocusIndex].click();
        } else {
            manualInputFunction()
        }
        currentFocusIndex = -1
        e.preventDefault()
    }

    return currentFocusIndex
}
