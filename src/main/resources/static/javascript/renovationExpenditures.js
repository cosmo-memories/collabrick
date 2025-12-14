// Chatgpt was used to help implement the functionality provided by this file

document.addEventListener('DOMContentLoaded', () => {
    const headers = document.querySelectorAll('th');
    const tbody = document.querySelector('tbody');

    if (!tbody) return; // stop if no tbody exists

    let sortState = {
        columnIndex: -1,
        ascending: true
    };

    const originalHeaderText = Array.from(headers).map(h => h.textContent.trim());

    function sortTableBy(index, forceAscending = null) {
        let rows = Array.from(tbody.querySelectorAll('tr'));

        // find the direction to be sorting in if it is already sorted by that column
        if (sortState.columnIndex === index) {
            sortState.ascending = !sortState.ascending;
        } else {
            sortState.columnIndex = index;
            sortState.ascending = index === 1 ? false : true;
        }

        rows.sort((a, b) => {
            // sorting for name and category columns
            const aText = a.children[index].textContent.trim();
            const bText = b.children[index].textContent.trim();

            // sorting for date column
            if (index === 1) {
                const aDate = new Date(aText);
                const bDate = new Date(bText);
                return sortState.ascending ? aDate - bDate : bDate - aDate;
            }

            // sorting for price column
            if (index === 2) {
                const aNum = parseFloat(aText.replace(/[^0-9.-]+/g, ""));
                const bNum = parseFloat(bText.replace(/[^0-9.-]+/g, ""));
                return sortState.ascending ? aNum - bNum : bNum - aNum;
            }

            return sortState.ascending
                ? aText.localeCompare(bText)
                : bText.localeCompare(aText);
        });

        // Display ↑ or ↓ based on if the column is sorted in ascending or descending order.
        headers.forEach((h, i) => {
            h.textContent = originalHeaderText[i];
            if (i === sortState.columnIndex) {
                h.textContent += sortState.ascending ? ' ↑' : ' ↓';
            }
        });

        rows.forEach(row => tbody.appendChild(row));
    }

    headers.forEach((header, index) => {
        header.addEventListener('click', () => sortTableBy(index));
    });

    // when loaded sort by date descending
    sortTableBy(1, false);
});
