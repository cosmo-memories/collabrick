/**
 * Initialise bootstrap tooltips
 */
document.addEventListener('DOMContentLoaded', function () {
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]:not([data-tooltip-initialized])');
    tooltipTriggerList.forEach(el => {
        new bootstrap.Tooltip(el);
        el.setAttribute('data-tooltip-initialized', 'true');
    });
});