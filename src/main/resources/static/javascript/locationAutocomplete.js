/*
    Method to fetch and present location suggestions to the user by
    reaching the /autocomplete endpoint which makes requests to mapbox api.

    Code modified from Chat GPT
*/

async function fetchSuggestions() {
    const baseUrl = document.getElementById('app-data').dataset.baseUrl;
    const entryBox = document.getElementById("streetAddress")
    const query = entryBox.value;
    if (query.length < 3) {
        return;
    }
    document.getElementById("results").display = "block"

    const contextPath = window.location.pathname.split('/')[1] === "test" ? window.location.pathname.split('/')[1]   : ''

    const response = await fetch(`${baseUrl}autocomplete?query=${encodeURIComponent(query)}`);
    const data = await response.json();
    const resultsDiv = document.getElementById("results");

    resultsDiv.innerHTML = "";

    // No locations found message
    if (!data.features || data.features.length === 0) {
        const noResultMessage = document.createElement("p");
        noResultMessage.textContent = "No location suggestions are available.";
        noResultMessage.style.fontStyle = "italic";
        noResultMessage.style.color = "gray";
        resultsDiv.appendChild(noResultMessage);
        return;
    }

    // Loop through each location feature returned from  data source
    data.features.forEach(location => {
        // Create a new paragraph element to display the full address
        const p = document.createElement("p");
        p.textContent = location.properties.full_address;

        // Add click handler to populate form fields when the address is selected
        p.onclick = () => {
            const context = location.properties.context;

            // Set street address based on available context keys
            if ("address" in context) {
                document.getElementById("streetAddress").value = context.address.name;
            } else if ("street" in context) {
                document.getElementById("streetAddress").value = context.street.name;
            } else {
                document.getElementById("streetAddress").value = "";
            }

            // Set postcode if available
            if ("postcode" in context) {
                document.getElementById("postcode").value = context.postcode.name;
            } else {
                document.getElementById("postcode").value = "";
            }

            // Set country if available
            if ("country" in context) {
                document.getElementById("country").value = context.country.name;
            } else {
                document.getElementById("country").value = "";
            }

            // Set region/state if available
            if ("region" in context) {
                document.getElementById("region").value = context.region.name;
            } else {
                document.getElementById("region").value = "";
            }

            // Set suburb: check for locality first, fall back to district if needed
            if (!("locality" in context)) {
                if ("district" in context) {
                    document.getElementById("suburb").value = context.district.name;
                } else {
                    document.getElementById("suburb").value = "";
                }
            } else {
                document.getElementById("suburb").value = context.locality.name;
            }

            // Set city: check for place first, fall back to district if needed
            if (!("place" in context)) {
                if ("district" in context) {
                    document.getElementById("city").value = context.district.name;
                } else {
                    document.getElementById("city").value = "";
                }
            } else {
                document.getElementById("city").value = context.place.name;
            }

            // Clear the results list after selecting an address
            resultsDiv.innerHTML = "";
        };

        // Add the paragraph element to the results container
        resultsDiv.appendChild(p);
    });


    entryBox.addEventListener('focus', () => {
        if (query.length >= 3) {
            resultsDiv.style.display = "block";
        }
    })

    // This is not from ChatGPT
    document.addEventListener('click', (event) => {
        if (entryBox.contains(event.target) || resultsDiv.contains(event.target)) {
            return;
        }

        resultsDiv.style.display = "none";
    })


}
