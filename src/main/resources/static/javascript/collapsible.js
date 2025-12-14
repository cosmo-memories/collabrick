// From: https://www.w3schools.com/howto/howto_js_collapsible.asp
// Open and close collapsible element on the page
function collapsible() {

    this.classList.toggle("active");
    let content = this.nextElementSibling;
    if (content.style.display === "block") {
        content.style.display = "none";
    } else {
        content.style.display = "block";
    }
}

let coll = document.getElementsByClassName("collapsible");
let i;

for (i = 0; i < coll.length; i++) {
    coll[i].addEventListener("click", collapsible);
}