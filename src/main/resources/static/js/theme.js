(() => {
    const stored = (() => {
        try {
            return localStorage.getItem("theme");
        } catch {
            return null;
        }
    })();
    const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
    const theme = stored === "light" || stored === "dark" ? stored : prefersDark ? "dark" : "light";
    document.documentElement.setAttribute("data-bs-theme", theme);
})();
