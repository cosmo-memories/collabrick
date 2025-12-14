document.addEventListener('DOMContentLoaded', () => {
    const checkboxes = document.querySelectorAll('.task-filter-checkbox');
    checkboxes.forEach(cb => {
        if (states != null) {
            cb.checked = states.includes(cb.value);
        }
    });
});
