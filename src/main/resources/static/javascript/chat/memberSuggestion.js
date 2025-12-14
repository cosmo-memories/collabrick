let memberSuggestionsDiv;
let inputField;
let activeIndex = -1;



window.mentions = window.mentions || [];


/**
 * Adds backslashes to any special characters to ensure a different regex doesn't make them "disappear"
 * @param str   the string being checked over
 * @returns {*} the checked string
 */
function escapeRegex(str) {
    return str.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

document.addEventListener('DOMContentLoaded', () => {
    inputField = document.getElementById("message");
    memberSuggestionsDiv = document.getElementById("member-suggestions");

    // Mousedown instead of click to make sure that the unfocusing of the suggestions menu happens later
    memberSuggestionsDiv.addEventListener('mousedown', (e) => {
        e.preventDefault();
    });
    // Sort members alphabetically with BrickAI at the top
    members.sort((a, b) => {
        if (a.firstName === "BrickAI") return -1;
        if (b.firstName === "BrickAI") return 1;

        // Otherwise, sort alphabetically by first name, then by last name
        const nameA = `${a.firstName} ${a.lastName}`;
        const nameB = `${b.firstName} ${b.lastName}`;

        return nameA.localeCompare(nameB);
    });

    // On input find the last '@' and the string entered after it, if there is a match between string and members
    // display member suggestions div with matching members
    inputField.addEventListener('input', () => {
        const caret = inputField.selectionStart;
        const textBeforeCaret = inputField.value.slice(0, caret);
        const atPos = textBeforeCaret.lastIndexOf("@");

        if (atPos === -1) {
            closeMemberList(true);
            return;
        }

        const queryRaw = textBeforeCaret.slice(atPos + 1);
        const query = queryRaw.toLowerCase();

        const x = getCaretX(inputField, atPos);
        memberSuggestionsDiv.style.left = (x + 20) + "px";

        activeIndex = 0;

        if (query.length === 0) {
            showMemberList(members);
            return;
        }

        const filtered = members.filter(m => {
            const full = (m.firstName + ' ' + m.lastName).toLowerCase();
            return full.startsWith(query);
        });

        if (filtered.length === 0) {
            closeMemberList(true);
            return;
        }

        showMemberList(filtered);
    });

    // Conditional timeout to prevent menu closing when keyboard navigating
    inputField.addEventListener('blur', () => {
        setTimeout(() => {
            const focusElement = document.activeElement;
            if (focusElement !== inputField && !memberSuggestionsDiv.contains(focusElement)) {
                closeMemberList(false);
            }
        }, 0);
    });


    inputField.addEventListener('keydown', (e) => {
        const items = Array.from(memberSuggestionsDiv.querySelectorAll('.member-suggestion-item'));
        if (items.length === 0 || memberSuggestionsDiv.style.display !== 'block') {
            return;
        }

        if (e.key === 'ArrowLeft' || e.key === 'ArrowRight') {
            closeMemberList();
            return;
        }

        activeIndex = autocompleteKeyBehaviour(e, Array.from(memberSuggestionsDiv.children), activeIndex, () => {
        });
    });


    inputField.addEventListener('mousedown', () => {
        if (memberSuggestionsDiv && memberSuggestionsDiv.style.display === 'block') {
            closeMemberList();
        }
    });

    // Keep the global mentions store in sync with the text content
    attachGlobalMentionRecompute();
});


/**
 * Create a members list in html for the mention div
 * @param list the list of members who match the query
 */
function showMemberList(list) {
    memberSuggestionsDiv.style.display = 'block';
    memberSuggestionsDiv.innerHTML = ''; // clear previous items
    window.mentionsList = []

    if (!list || list.length === 0) {
        const empty = document.createElement('div');
        empty.className = 'member-suggestion-empty';
        empty.textContent = 'No matches';
        memberSuggestionsDiv.appendChild(empty);
        return;
    }

    list.forEach((m, i) => {
        let name;
        if (m.lastName) {
            name =  m.firstName + ' ' + m.lastName;
        } else {
            name = m.firstName
        }
        const element = document.createElement('div');
        element.className = 'member-suggestion-item' + (i === activeIndex ? ' active' : '');
        element.dataset.index = i;
        if (m.firstName === 'BrickAI') {
            element.innerHTML = `
              <img src=${window.fullBaseUrl + '/' + m.image} class="suggestion-pfp">
              <div>
                <div>${name}</div>
              </div>`;
        } else {
            element.innerHTML = `
              <img src=${window.fullBaseUrl + '/' + m.image} class="suggestion-pfp">
              <div>
                <div>${name}</div>
                <div style="font-size:12px;">${m.email}</div>
              </div>`;
        }
        element.setAttribute("tabindex", "-1"); //Make div focusable
        element.addEventListener("keydown",             (e) => {
            activeIndex = autocompleteKeyBehaviour(e, Array.from(memberSuggestionsDiv.children), activeIndex, () => {
            }) });

        element.addEventListener('click', (e) => {
            onClickMemberElement(e, name);
        });

        memberSuggestionsDiv.appendChild(element);
    });
    checkXPos();

}

function checkXPos() {
    const offset = 70;
    const windowWidth = window.innerWidth;
    const maxR = windowWidth - offset
    const rect = memberSuggestionsDiv.getBoundingClientRect();
    const width = memberSuggestionsDiv.offsetWidth;
    let memberDivR = rect.right;
    if (memberDivR > maxR) {
        const lPos = maxR - width
        console.log(`Left position: ${lPos}`);
        memberSuggestionsDiv.style.left = null;
        memberSuggestionsDiv.style.right = '50px'
    }
    console.log(`Max: ${windowWidth}, Member: ${memberDivR}`);
}

function closeMemberList(focus) {
    memberSuggestionsDiv.style.display = 'none';
    memberSuggestionsDiv.innerHTML = '';
    if (focus) {
        inputField.focus()
    }
    activeIndex = -1;
}

/**
 * When clicking a given member in the suggestions, autofill the name of the member into the input field
 * and push the mention to the global mentions list
 *
 * @param e       The element of the selected member in the suggestions div
 * @param name    The full name of the selected member
 */
function onClickMemberElement(e, name) {
    e.preventDefault();
    const caret = inputField.selectionStart;
    const textBeforeCaret = inputField.value.slice(0, caret);
    const atSignPos = textBeforeCaret.lastIndexOf("@");
    const beforeAtSign = inputField.value.slice(0, atSignPos);
    const afterCaret = inputField.value.slice(caret);
    inputField.value = beforeAtSign + '@' + name + ' ' + afterCaret;

    inputField.setSelectionRange(beforeAtSign.length + name.length + 2, beforeAtSign.length + name.length + 2);

    window.mentions = recomputeMentionsFromText(inputField.value, members);

    closeMemberList(true);
}

/**
 * Scans the text for all valid @mentions that match a member's name and returns a list of mentions
 * @param text      The text being scanned over
 * @param members   The members of the chat channel
 * @returns {*[]}   The list of mentions
 */
function recomputeMentionsFromText(text, members) {
    if (!members || members.length === 0) {
        return [];
    }

    const names = members.map((m) => ({ id: m.id, name: [m.firstName, m.lastName].filter(Boolean).join(' ') }))

    const pattern = names.map((n) => escapeRegex(n.name)).join("|");
    if (!pattern) {
        return [];
    }

    // Regex for @Name followed by end, whitespace, or punctuation. ChatGPT wrote this regex
    const mentionRegex = new RegExp(`@(${pattern})(?=$|\\s|[.,;:!?()\\[\\]{}<>@])`, "gu");

    const mentions = [];
    for (const match of text.matchAll(mentionRegex)) {
        const fullName = match[1];
        const atIndex = match.index;
        const member = names.find((n) => n.name === fullName);
        if (!member) {
            continue;
        }

        mentions.push({
            userId: member.id,
            startPosition: atIndex,
            endPosition: atIndex + fullName.length
        });
    }
    return mentions;
}

/**
 * Attach event listeners to the input field to automatically recompute mentions
 */
function attachGlobalMentionRecompute() {
    const recompute = () => {
        window.mentions = recomputeMentionsFromText(inputField.value, members);
    };

    inputField.addEventListener("input", recompute);
    // GPT helped me figure out how to deal with pasting/cutting
    ["paste", "cut"].forEach((evt) => {
        inputField.addEventListener(evt, () => setTimeout(recompute, 0));
    });
}

/**
 * ChatGPT helped to create this, calculates the x position of the '@' character using a mirror div
 * so that the suggestions can appear above the '@'.
 * @param input the text input in the text field
 * @param atIndex the index of the @.
 * @returns {number} the x offset to position the suggestion div at.
 */
function getCaretX(input, atIndex) {
    const mirror = document.getElementById("mirror");
    mirror.style.font = window.getComputedStyle(input).font;
    mirror.style.letterSpacing = window.getComputedStyle(input).letterSpacing;
    mirror.textContent = input.value.substring(0, atIndex);
    const span = document.createElement("span");
    span.textContent = "\u200b";
    mirror.appendChild(span);
    const rect = span.getBoundingClientRect();
    const mirrorRect = mirror.getBoundingClientRect();
    mirror.removeChild(span);
    return rect.left - mirrorRect.left;
}

// Update the recompute to stay in sync
window.recomputeMentionsFromText = recomputeMentionsFromText;