/**
 * Opens a tab by hiding all others and displaying the selected one. Updates the URL with the current tab.
 * @param tabName - The name of the tab to open.
 */
function openTab(tabName) {
    const tabLinks = document.getElementsByClassName("tab-links");
    const tabContent = document.getElementsByClassName("tab-content");

    for (let i = 0; i < tabLinks.length; i++) {
        tabContent[i].style.display = "none";
    }

    for (let i = 0; i < tabLinks.length; i++) {
        tabLinks[i].className = tabLinks[i].className.replace(" active", "");
    }

    const elements = document.getElementsByClassName("tab-" + tabName);
    for (let element of elements) {
        element.classList.add("active");
    }

    document.getElementById(tabName).style.display = "block";

    const url = new URL(window.location.href);
    url.searchParams.set('tab', tabName.toLowerCase());
    window.history.pushState({ path: url.toString() }, '', url.toString())
}

/**
 * On initial page load, extract the tab from the query string and open that tab. If the
 * tab is not defined in the query string, then the tab which is marked as the default is opened.
 */
document.addEventListener('DOMContentLoaded', () => {
    const button = document.getElementById('filterStatesBtn');
    // document listener as dropdown can be closed by clicking anywhere on the document,
    document.addEventListener('click', (e) => {
        if (button.classList.contains('show')) {
            button.classList.add('active');
        } else {
            button.classList.remove('active')
        }
    });
    const tabName = tab // tab is a model attribute passed through the HTML document
    if (tabName !== null) {
        const tabElement = document.getElementById(tabName);
        if (tabElement) {
            openTab(tabName);
            return;
        }
    }
    document.getElementById("defaultOpen").click();
});
