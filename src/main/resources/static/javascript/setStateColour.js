document.addEventListener("DOMContentLoaded", () => {
    const updateSelectedState = (state, badgeHtml) => {
        document.getElementById('taskStateDropdownButton').innerHTML = badgeHtml;
        document.getElementById('stateInput').value = state;
    };

    // Set default selection
    const defaultItem = document.querySelector('.default-task-status');
    if (defaultItem) {
        const selectedValue = defaultItem.getAttribute('data-value');
        const badgeHtml = defaultItem.innerHTML;
        updateSelectedState(selectedValue, badgeHtml);
    }

    // Handle clicks
    document.querySelectorAll('.badge-select').forEach(item => {
        item.addEventListener('click', event => {
            event.preventDefault();
            const selectedValue = item.getAttribute('data-value');
            const badgeHtml = item.innerHTML;
            updateSelectedState(selectedValue, badgeHtml);
        });
    });
});