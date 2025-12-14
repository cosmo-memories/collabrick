// Heavily used a W3Schools guide for this: https://www.w3schools.com/howto/howto_js_autocomplete.asp

/*
Displays a list of suggestions for autocompletion for the individual renovation tag search bar
searchInput: the search string inputted by the user
 */
const localBaseUrl = document.getElementById('app-data').dataset.baseUrl;
const tagSearchBar = document.querySelector('.autocomplete');
function autocomplete(searchInput) {

    let currentFocus;

    searchInput.addEventListener("input", async function(e) {
        let suggestionsDiv, individualSuggestion, i, inputString = this.value;
        closeAllLists();
        if (!inputString) {return false;}
        currentFocus = -1;


        const response = await fetch(`${localBaseUrl}tagAutoCompleteRenovation?term=${encodeURIComponent(inputString)}`);
        const suggestions = await response.json();

        // create a DIV element that will contain the items (values)
        suggestionsDiv = document.createElement("DIV");
        suggestionsDiv.setAttribute("id", this.id + "autocomplete-list");
        suggestionsDiv.setAttribute("class", "autocomplete-items");
        this.parentNode.appendChild(suggestionsDiv);
        let numOfSuggestions = suggestions.length;


        // Show message if no tags are found
        if (numOfSuggestions < 1) {
            individualSuggestion = document.createElement("DIV");
            individualSuggestion.innerHTML = "<i>No matching tags</i>"
            suggestionsDiv.appendChild(individualSuggestion);
        } else {

            // Prevent all suggestions from showing for UI reasons
            if (numOfSuggestions > 10) {
                numOfSuggestions = 10;
            }
            let matchString = inputString;
            inputString = inputString.replace(/</g, "&lt;")
                .replace(/>/g, "&gt;");
            for (i = 0; i < numOfSuggestions; i++) {
                let originalSuggestion = suggestions[i];
                originalSuggestion = originalSuggestion.replace(/</g, "&lt;")
                    .replace(/>/g, "&gt;");
                if (suggestions[i].toLowerCase().includes(matchString.toLowerCase())) {

                    let lowerOriginal = originalSuggestion.toLowerCase();
                    let matchingLetterIndex = lowerOriginal.indexOf(inputString.toLowerCase());

                    individualSuggestion = document.createElement("DIV");

                    // Beginning letters + bold matched letters + remaining letters
                    individualSuggestion.innerHTML =
                        originalSuggestion.substring(0, matchingLetterIndex) +
                        "<strong>" + originalSuggestion.substring(matchingLetterIndex, matchingLetterIndex + inputString.length) + "</strong>" +
                        originalSuggestion.substring(matchingLetterIndex + inputString.length);

                    individualSuggestion.innerHTML += "<input type='hidden' value='" + suggestions[i] + "'>";
                    individualSuggestion.addEventListener("click", function(e) {
                        // Gives a bit of user feedback as the search can take a bit
                        const addTagButton = document.getElementById("add-tag-button");
                        const addTagInput = document.getElementById("tagAutocomplete")
                        addTagButton.classList.add("add-tag-submit");
                        addTagInput.classList.add("tag-input-submit");
                        addTagButton.innerText = "Adding...";

                        searchInput.value = this.getElementsByTagName("input")[0].value;
                        closeAllLists();

                        const form = searchInput.closest("form");
                        if (form) form.submit();
                    });
                    suggestionsDiv.appendChild(individualSuggestion);
                }
            }
        }

        if (suggestionsDiv.children.length > 0) {
            tagSearchBar.classList.add('drop');
        } else {
            tagSearchBar.classList.remove('drop');
            suggestionsDiv.remove();   // no point leaving an empty container hanging around
        }
    });
    /*execute a function presses a key on the keyboard:*/
    searchInput.addEventListener("keydown", function(e) {
        let suggestions = document.getElementById(this.id + "autocomplete-list");
        if (suggestions) suggestions = suggestions.getElementsByTagName("div");
        if (e.key === "Tab") {
            if (suggestions && suggestions.length > 0) {
                e.preventDefault();
                currentFocus++;
                addActive(suggestions);
            }
        }     else if (e.key === "Enter") {
            if (suggestions && currentFocus > -1) {
                e.preventDefault();
                suggestions[currentFocus].click();
            }
        }
    });
    function addActive(x) {
        /*a function to classify an item as "active":*/
        if (!x) return false;
        /*start by removing the "active" class on all items:*/
        removeActive(x);
        if (currentFocus >= x.length) currentFocus = 0;
        if (currentFocus < 0) currentFocus = (x.length - 1);
        /*add class "autocomplete-active":*/
        x[currentFocus].classList.add("autocomplete-active");
    }
    function removeActive(x) {
        /*a function to remove the "active" class from all autocomplete items:*/
        for (let i = 0; i < x.length; i++) {
            x[i].classList.remove("autocomplete-active");
        }
    }
    function closeAllLists(elmnt) {
        tagSearchBar.classList.remove('drop')
        /*close all autocomplete lists in the document,
        except the one passed as an argument:*/
        let x = document.getElementsByClassName("autocomplete-items");
        for (let i = 0; i < x.length; i++) {
            if (elmnt !== x[i] && elmnt !== searchInput) {
                x[i].parentNode.removeChild(x[i]);
            }
        }
    }
    /*execute a function when someone clicks in the document:*/
    document.addEventListener("click", function (e) {
        closeAllLists(e.target);
    });
}

document.addEventListener("DOMContentLoaded", function () {
    autocomplete(document.getElementById("tagAutocomplete"));

    // GPT'd why the colour wasn't changing when I submitted a tag
    const tagForm = document.querySelector(".tag-form");
    const addTagButton = document.getElementById("add-tag-button");
    const addTagInput = document.getElementById("tagAutocomplete");

    tagForm.addEventListener("submit", function (e) {
        e.preventDefault();
        // Visual feedback to acknowledge submission
        addTagButton.disabled = true;
        addTagButton.classList.add("add-tag-submit");
        addTagButton.innerText = "Adding...";

        addTagInput.classList.add("tag-input-submit");
        tagForm.submit();
    });

});
