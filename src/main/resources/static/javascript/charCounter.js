/**
 * Updates the character counter label with the current length and maximum length.
 * @param label - The label element to update.
 * @param currentLength - The current length of the text input.
 * @param maxLength - The maximum length of the text input.
 */
const setCharacterCounterLabel = (label, currentLength, maxLength) => {
    label.innerText = `${currentLength}/${maxLength}`
}

/**
 * Handles the input event on the textarea element, updates the character counter label, and trims the text if it
 * exceeds the maximum length.
 * @param event - The input event.
 * @param label - The label element for this input.
 * @param maxLength - The maximum length of the text input.
 */
const handleInput = (event, label, maxLength) => {
    const target = event.currentTarget;

    // set the character counter label
    const currentGraphemeClustersCount = countGraphemeClusters(target.value);
    setCharacterCounterLabel(label, currentGraphemeClustersCount, maxLength)
}

/**
 * Counts the number of user-perceived characters (grapheme clusters) in a string.
 * Translated from Java to JavaScript by GPT
 *
 * @param {string} message - The input string to count grapheme clusters for
 * @returns {number} The number of grapheme clusters in the string
 */
const countGraphemeClusters = (message) => {
    const segmenter = new Intl.Segmenter('en', { granularity: 'grapheme' });
    const segments = segmenter.segment(message);

    let count = 0;
    for (const _ of segments) {
        count++;
    }

    return count;
}

/**
 * Initializes the character counter functionality when the DOM content has loaded. This ensures that the page has
 * loaded, and we can find the word counter elements.
 */
document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".char-counter-wrapper").forEach(wrapper => {
        const textarea = wrapper.querySelector(".char-counter");
        const label = wrapper.querySelector(".char-counter-label");

        // check if the wrapper contains both a textarea and label
        if (!textarea || !label) return;

        // check if the max length has been specified, otherwise don't add the character counter
        const maxLength = parseInt(textarea.getAttribute("data-char-max"), 10);
        if (isNaN(maxLength)) return;

        const currentGraphemeClustersCount = countGraphemeClusters(textarea.value);
        textarea.addEventListener("input", (event) => handleInput(event, label, maxLength))
        setCharacterCounterLabel(label, currentGraphemeClustersCount, maxLength)
    })
});
