// Delete an expense from a task without reloading the page
function deleteExpense(taskId, expenseId, expenseCost) {
    const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    fetch(window.fullBaseUrl + `/task/` + taskId + `/expense/` + expenseId + `/delete`, {
        method: 'POST',
        headers: {
            'X-Requested-With': 'XMLHttpRequest',
            [header]: token
        }
    }).then(response => {
        if (response.ok) {
            document.getElementById("expense-" + expenseId).remove();
            const expenseTotalSpan = document.getElementById("expenseTotal")
            const expenseItemsSpan = document.getElementById("expenseItems")
            const expenseTotal = expenseTotalSpan.innerText.slice(1)
            const expenseItems = expenseItemsSpan.innerText
            expenseTotalSpan.innerText = "$" + ((expenseTotal - expenseCost).toFixed(2))
            expenseItemsSpan.innerText = (expenseItems - 1).toString()
            if (expenseItems - 1 === 0) {
                const expensesButton = document.getElementById("expensesButton")
                expensesButton.classList.remove("collapsible")
                expensesButton.classList.add("disabled-button")
                expensesButton.removeEventListener("click", collapsible)
            }
        } else {
            alert("Could not delete expense.")
        }
    })
}