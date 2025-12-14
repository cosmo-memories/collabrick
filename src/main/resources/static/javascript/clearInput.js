/**
 * Clears the search input when the 'X' button is clicked
 */
document.addEventListener('DOMContentLoaded', function () {
    const resetButton = document.getElementById("clearButton");
    const searchInput = document.getElementById("searchInput");

    resetButton.addEventListener('click', function(e) {
        searchInput.value = '';
    })
})