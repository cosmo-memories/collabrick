/** Activates the currently selected task icon **/

// From this point to the next comment ChatGPT helped to code this
/** Ensures the current task icon is highlight when form is opened, ensures no other buttons are highlighted, sets the input values to return to the task id and the name of the image file **/
document.addEventListener("DOMContentLoaded", function () {
    document.addEventListener("click", function (event) {
        const addImageBtn = event.target.closest(".add-image-button");

        if (addImageBtn) {
            const savedIcon = addImageBtn.getAttribute("data-icon");
            const taskId = addImageBtn.getAttribute("data-task-id");

            document.getElementById('taskId').value = taskId;
            document.getElementById('selectedIcon').value = savedIcon;

            document.querySelectorAll(".task-icon-button").forEach(btn => {
                btn.classList.remove("active");
            });

            if (savedIcon) {
                const activeButton = document.querySelector(`.task-icon-button[data-icon="${savedIcon}"]`);
                if (activeButton) {
                    activeButton.classList.add("active");
                }
            }
        }
    });
// To here

    /** Un-highlights all buttons, then highlights the button just clicked and sets the return value for the image file name to the corresponding selected image **/
    const taskIconButtons = document.querySelectorAll(".task-icon-button");

    taskIconButtons.forEach((btn) => {
        btn.addEventListener("click", (e) => {
            taskIconButtons.forEach(f => f.classList.remove('active'));
            e.target.classList.add("active");
            document.getElementById('selectedIcon').value = e.target.getAttribute('data-icon');
        });
    });

    /** Un-highlights all buttons, then highlights the default image button and sets the return value to the default image name **/
    const resetDefaultBtn = document.getElementById('resetDefaultIcon');
    resetDefaultBtn.addEventListener('click', function () {
        taskIconButtons.forEach(f => f.classList.remove('active'));
        const defaultButton = document.getElementById('defaultButton');
        document.getElementById('selectedIcon').value = defaultButton.getAttribute('data-icon');
        defaultButton.classList.add("active");
    })
});