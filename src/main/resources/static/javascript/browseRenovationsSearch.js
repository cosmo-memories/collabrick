const ONLY_MINE_PARAM_NAME = "onlyMine"
let searchSelectedTagsInputContainer
let searchSelectedTagsContainer;
const selectedTags = [];

document.addEventListener('DOMContentLoaded', () => {
    searchSelectedTagsInputContainer = document.getElementById("search-selected-tags-input");
    searchSelectedTagsContainer = document.getElementById("search-selected-tags");

    // setup show my renovations checkbox
    const checkbox = document.getElementById("show-my-renovations-checkbox");
    if (checkbox) {
        checkbox.addEventListener('change', (event) => {
            const onlyMineSelected = event.target.checked
            updateOnlyMineParam(onlyMineSelected)
        })
    }

    // setup search
    const searchInput = document.getElementById("searchInput")
    new Autocompletion(searchInput, fetchSearchSuggestions, renderSearchSuggestion, addTagToSearch, true)

    // render tags passed from thymeleaf
    thTags.forEach((tag) => {
        addTagToSearch(tag);
    })
})

/**
 * Updates the URL query parameter to toggle showing only the users renovations
 * @param onlyMine Whether to filter results to only the user's renovations
 */
const updateOnlyMineParam = (onlyMine) => {
    const params = new URLSearchParams(window.location.search);

    if (onlyMine) {
        params.set(ONLY_MINE_PARAM_NAME, 'true');
    } else {
        params.delete(ONLY_MINE_PARAM_NAME)
    }

    // triggers reload with updated param
    window.location.search = params.toString();
}

/**
 * Fetches tag suggestions from the server based on search query
 * @param query User’s search input
 * @returns Array of tag suggestions
 */
const fetchSearchSuggestions = async (query) => {
    try {
        const encodedQuery = encodeURIComponent(query);
        const response = await fetch(`${baseUrl}tagAutoComplete?tag=${encodedQuery}`);
        if (response.ok) {
            const items = await (response.json());
            return items;
        }
        console.error("Failed to fetch search suggestions")
    } catch (error) {
        console.error("Failed to fetch search suggestions", error)
    }
}

/**
 * Renders an autocomplete suggestion with highlighted matching substring
 * @param search Current user input
 * @param tag Suggested tag to render
 * @returns Rendered suggestion element
 */
const renderSearchSuggestion = (search, tag) => {
    // don't add tag if its already been selected
    if (selectedTags.includes(tag)) {
        return;
    }

    const idx = tag.toLowerCase().indexOf(search.toLowerCase());
    const element = document.createElement("div");

    //highlight matching substring of each tag
    if (idx > -1) {
        const before = tag.slice(0, idx);
        const match  = tag.slice(idx, idx + search.length);
        const after  = tag.slice(idx + search.length);
        const beforeSpan = document.createElement("span");
        const matchStrong = document.createElement("strong");
        const afterSpan = document.createElement("span");
        beforeSpan.innerText = before;
        matchStrong.innerText = match;
        afterSpan.innerText = after;
        element.append(beforeSpan, matchStrong, afterSpan);
    } else {
        const matchStrong = document.createElement("strong");
        matchStrong.innerText = tag;
        element.append(matchStrong);
    }

    return element;
}

/**
 * Adds a tag badge and corresponding hidden input to the search filters
 * @param tag Tag name to be added
 */
const addTagToSearch = (tag) => {
    const template = document.getElementById("search-tag-badge-template");
    const clone = template.content.cloneNode(true);
    const root = clone.querySelector(".tag-badge");
    if (root) {
        root.setAttribute("data-tag-name", tag);
        root.querySelector(".tag-badge-text").textContent = tag;
        root.querySelector(".tag-badge-input").value = tag;
        root.querySelector(".tag-remove").onclick = () => removeTagFromSearch(tag);
        searchSelectedTagsContainer.appendChild(root);
    }

    const input = document.createElement("input");
    input.name = "tags"
    input.value = tag;
    input.setAttribute("data-tag-name", tag);
    searchSelectedTagsInputContainer.appendChild(input);
    selectedTags.push(tag);
}

/**
 * Removes a tag badge and corresponding input from the search filters
 * @param {string} tag Tag name to be removed
 */
const removeTagFromSearch = (tag) => {
    const element = searchSelectedTagsContainer.querySelector(`[data-tag-name="${tag}"]`)
    searchSelectedTagsContainer.removeChild(element);

    const input = searchSelectedTagsInputContainer.querySelector(`[data-tag-name="${tag}"]`)
    searchSelectedTagsInputContainer.removeChild(input);

    const index = selectedTags.indexOf(tag);
    if (index !== -1) {
        selectedTags.splice(index, 1);
    }
}

