/**
 * This file handles opening and closing the confirmation modal for
 * allowing/blocking BrickAI from accessing renovation details
 */
const allowBrickAICheck = document.getElementById('brickAIOptOut')
const brickAIConfirmationModalElement = document.getElementById('allowBrickAIModal')
const brickAIConfirmationModal = new bootstrap.Modal(brickAIConfirmationModalElement)

allowBrickAICheck.addEventListener('change', function (event) {
        brickAIConfirmationModal.show();
})
brickAIConfirmationModalElement.addEventListener('hidden.bs.modal', function (e) {
    allowBrickAICheck.checked=!allowBrickAICheck.checked;
})
