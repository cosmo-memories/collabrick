let legendValues = [
    "Miscellaneous",
    "Material",
    "Labour",
    "Equipment",
    "Professional Services",
    "Permit",
    "Cleanup",
    "Delivery"
];

let expenseValues = [
    miscExpenses,
    materialExpenses,
    labourExpenses,
    equipmentExpenses,
    serviceExpenses,
    permitExpenses,
    cleanupExpenses,
    deliveryExpenses
];

let budgetValues = [
    miscBudget,
    materialBudget,
    labourBudget,
    equipmentBudget,
    serviceBudget,
    permitBudget,
    cleanupBudget,
    deliveryBudget
];

let barColors = [
    "#7F7F7F",  // Miscellaneous
    "#E06B29",  // Material
    "#BF4353",  // Labour
    "#B28C7F",  // Equipment
    "#7857E6",  // Professional Services
    "#D4B400",  // Permit
    "#4A9B58",  // Cleanup
    "#5F9DE1"   // Delivery
];

let expenseColors = expenseValues.map((value, index) => {
    return value > budgetValues[index] ? "red" : barColors[index];
});

const expenseChart = document.getElementById("expenseChart");
const budgetChart = document.getElementById("budgetChart");

// Budget Chart
if (budgetChart) {
    new Chart(budgetChart, {
        type: "pie",
        data: {
            labels: legendValues,
            datasets: [{
                backgroundColor: barColors,
                data: budgetValues
            }]
        },
        options: {
            plugins: {
                legend: {
                    labels: {
                        generateLabels(chart) {
                            return chart.data.labels.map((label, i) => {
                                return {
                                    text: label,
                                    fillStyle: barColors[i],
                                    strokeStyle: '#FFFFFF',
                                    fontColor: '#666',
                                    index: i
                                }
                            })
                        }
                    }
                },
                title: {
                    display: true,
                    text: "Budget",
                    font: {
                        size: 20
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            const value = (context.parsed ?? 0).toFixed(2);
                            return ' $' + value;
                        }
                    }
                }
            }
        }
    });
}

// Expense Chart
if (expenseChart) {
    const expenseChartInstance = new Chart(expenseChart, {
        type: "pie",
        data: {
            labels: legendValues,
            datasets: [{
                backgroundColor: expenseColors,
                data: expenseValues,
                offset: expenseValues.map((expense, index) => {
                    return expense > budgetValues[index] ? 20 : 0;
                })
            }]
        },
        options: {
            plugins: {
                legend: {
                    labels: {
                        generateLabels(chart) {
                            return chart.data.labels.map((label, i) => {
                                const expense = chart.data.datasets[0].data[i];
                                const budget = budgetValues[i];
                                const color = expense > budget ? 'red' : barColors[i];
                                const textColor = expense > budget ? 'red' : '#666';
                                return {
                                    text: label,
                                    fillStyle: color,
                                    strokeStyle: '#FFFFFF',
                                    fontColor: textColor,
                                    index: i
                                }
                            })
                        }
                    }
                },
                title: {
                    display: true,
                    text: "Expenditure",
                    font: {
                        size: 20
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            const expense = (context.parsed ?? 0).toFixed(2);
                            const budget = Number(budgetValues[context.dataIndex]).toFixed(2);
                            return Number(expense) > Number(budget) ? ' $' + expense + ' (of $' + budget + ') ⚠️ Over budget! ⚠️'
                                                    : ' $' + expense + ' (of $' + budget + ')';
                        }
                    }
                }
            }
        }
    });

    expenseChartInstance.$budgetValues = budgetValues;
    expenseChartInstance.$barColors = barColors;
}