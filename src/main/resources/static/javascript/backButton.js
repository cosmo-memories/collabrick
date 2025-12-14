const setPreviousUrl = (url) => {
    sessionStorage.setItem("previous_url", url);
}

const setCurrentUrl = (url) => {
    sessionStorage.setItem("current_url", url);
}

const getPreviousUrl = () => {
    return sessionStorage.getItem("previous_url");
}

const getCurrentUrl = () => {
    return sessionStorage.getItem("current_url");
}

const getBackButtonInfo = () => {
    const prevUrl = getPreviousUrl();

    if (prevUrl !== null) {
        if (prevUrl.includes("/myRenovations")) {
            return { url: prevUrl, label: "My Renovations" };
        }
        if (prevUrl.includes("/browse")) {
            return { url: prevUrl, label: "Browse Renovations" };
        }
        return { url: prevUrl, label: "Back" };
    }

    // no previous URL - decide based on server attributes

    if (window.isPublic && !window.isMember) {
        return { url: `${window.fullBaseUrl}/browse`, label: "Browse Renovations" };
    }
    if (window.isMember) {
        return { url: `${window.fullBaseUrl}/myRenovations`, label: "My Renovations" };
    }
    return { url: baseUrl, label: "Back" }; // safety fallback
};

const goBack = () => {
    const { url } = getBackButtonInfo();

    window.location.href = url;
};

document.addEventListener("DOMContentLoaded", () => {
    const currentUrl = getCurrentUrl();
    const newUrl = window.location.href;

    // don't update the previous url if we are on an individual renovation
    const regex = /^.*\/((myRenovations|renovation)\/\d+.*|browse\/\d+.*|newRenovation)$/;
    if (!(currentUrl && currentUrl.match(regex) && newUrl.match(regex))) {
        // only set previous_url if currentUrl exists
        if (currentUrl) {
            setPreviousUrl(currentUrl);
        }
        setCurrentUrl(newUrl);
    }

    // update back button label if present
    const backButtonLabel = document.getElementById("back-button-label");
    if (backButtonLabel) {
        backButtonLabel.textContent = getBackButtonInfo().label;
    }
});
