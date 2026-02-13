document.querySelectorAll("[data-theme-toggle]").forEach(button => {
    button.addEventListener("click", () => {
        const root = document.documentElement;
        const next = root.getAttribute("data-bs-theme") === "dark" ? "light" : "dark";
        root.setAttribute("data-bs-theme", next);
        try {
            localStorage.setItem("theme", next);
        } catch {
        }
    });
});

document.querySelectorAll("form[data-confirm]").forEach(form => {
    form.addEventListener("submit", event => {
        if (!window.confirm(form.dataset.confirm)) {
            event.preventDefault();
        }
    });
});

document.querySelectorAll("[data-autosubmit]").forEach(control => {
    control.addEventListener("change", () => control.form.requestSubmit());
});

const flash = document.querySelector(".flash");
if (flash) {
    setTimeout(() => flash.classList.add("is-hidden"), 3500);
    flash.addEventListener("transitionend", () => flash.remove());
}

document.addEventListener("keydown", event => {
    const target = event.target;
    const typing = target.closest("input, textarea, select, [contenteditable]");
    if (typing || event.ctrlKey || event.metaKey || event.altKey) {
        return;
    }
    if (event.key === "/") {
        const search = document.querySelector("[data-search]");
        if (search) {
            event.preventDefault();
            search.focus();
            search.select();
        }
    } else if (event.key === "n" || event.key === "т") {
        const link = document.querySelector(".new-task");
        if (link) {
            event.preventDefault();
            link.click();
        }
    }
});
