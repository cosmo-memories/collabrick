// combined reusable autocompletion utility made from the previous navigation bar search autocompletion
// and user invitation. ChatGPT assisted with making it modular.

/**
 * A reusable class for implementing search suggestions with a dropdown menu.
 */
class Autocompletion {
    /**
     * Creates an instance of Autocompletion.
     * @param inputElement The input element to attach the search suggestions to.
     * @param getSuggestions Function that returns an array of suggestions based on the query.
     * @param renderSuggestion The suggestion item to render.
     * @param onSelect Callback function invoked when a suggestion is selected.
     * @param clearOnSelect Flag indicating if the input should be cleared when a suggestion is selected.
     */
    constructor(inputElement, getSuggestions, renderSuggestion, onSelect, clearOnSelect) {
        this.inputElement = inputElement;
        this.getSuggestions = getSuggestions;
        this.renderSuggestion = renderSuggestion;
        this.onSelect = onSelect;
        this.dropdownMenu = inputElement.nextElementSibling;
        this.clearOnSelect = clearOnSelect;

        this.setup();
    }

    /**
     * Filters suggestions based on the input.
     * @param input The current input query.
     * @returns Array of filtered suggestion items.
     */
    filterSuggestions(input) {
        return this.getSuggestions(input);
    }

    /**
     * Populates the dropdown menu with filtered suggestions.
     * @param input The current input query.
     * @param filteredSuggestions Array of suggestions to display.
     * @private
     */
    populateDropdown(input, filteredSuggestions) {
        this.dropdownMenu.innerHTML = '';

        const handleSelect = (suggestion) => {
            if (this.clearOnSelect) {
                this.inputElement.value = '';
            }
            this.selectSuggestion(suggestion);
        }

        filteredSuggestions.forEach((suggestion) => {
            const li = document.createElement('li');
            const div = this.renderSuggestion(input, suggestion);
            if (div) {
                div.classList.add('dropdown-item');
                div.setAttribute('tabindex', '0');
                div.addEventListener('click', () => handleSelect(suggestion));
                div.addEventListener('keydown', (e) => {
                    if (e.key === 'Enter') {
                        handleSelect(suggestion)
                    }
                });
                li.appendChild(div);
                this.dropdownMenu.appendChild(li);
            }
        });
    }

    /**
     * Shows the dropdown menu.
     */
    showDropdown() {
        this.dropdownMenu.classList.add('show');
    }

    /**
     * Hides the dropdown menu.
     */
    hideDropdown() {
        this.dropdownMenu.classList.remove('show');
    }

    /**
     * Handles the selection of a suggestion.
     * @param suggestion The selected suggestion item.
     */
    selectSuggestion(suggestion) {
        if (!this.clearOnSelect) {
            this.inputElement.value = suggestion.text || suggestion;
        }
        this.hideDropdown();
        if (this.onSelect) {
            this.onSelect(suggestion);
        }
    }

    setup() {
        // setup input changes listener
        const onType = async () => {
            const query = this.inputElement.value;
            const filteredSuggestions = await this.filterSuggestions(query);
            this.populateDropdown(query, filteredSuggestions);
            if (filteredSuggestions.length > 0 && query.length > 0) {
                this.showDropdown();
            } else {
                this.hideDropdown();
            }
        }

        this.inputElement.addEventListener('input', () => {
            clearTimeout(this.debounceTimer);
            this.debounceTimer = setTimeout(onType, 250);
        });

        // setup keyboard navigation
        this.inputElement.addEventListener('keydown', (e) => {
            if (e.key === 'Tab' && this.dropdownMenu.classList.contains('show')) {
                e.preventDefault();
                const firstSuggestion = this.dropdownMenu.querySelector('.dropdown-item');
                if (firstSuggestion) {
                    firstSuggestion.focus();
                }
            } else if (e.key === 'Escape') {
                this.hideDropdown();
            }
        });

        // setup dropdown keyboard navigation
        this.dropdownMenu.addEventListener('keydown', (e) => {
            if (e.key === 'Tab') {
                e.preventDefault();
                const current = document.activeElement;
                const items = Array.from(this.dropdownMenu.querySelectorAll('.dropdown-item'));
                const index = items.indexOf(current);
                if (e.shiftKey) {
                    const prevIndex = index > 0 ? index - 1 : items.length - 1;
                    items[prevIndex].focus();
                } else {
                    const nextIndex = index < items.length - 1 ? index + 1 : 0;
                    items[nextIndex].focus();
                }
            }
        });

        // setup hide dropdown when clicking outside
        document.addEventListener('click', (e) => {
            if (!this.dropdownMenu.contains(e.target) && e.target !== this.inputElement) {
                this.hideDropdown();
            }
        });
    }
}