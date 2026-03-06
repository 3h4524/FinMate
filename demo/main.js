const panels = {
    overview: {
        kicker: "Overview state",
        title: "A clear financial dashboard for everyday decisions.",
        description: "The overview combines wallet health, monthly budget pacing, category performance, and automated reminders into one actionable screen.",
        metrics: [
            { label: "Available balance", value: "$12,480", note: "Across primary wallet" },
            { label: "Budget health", value: "72%", note: "Still within target" },
            { label: "Next reminder", value: "08:00", note: "Rent confirmation" },
        ],
        listTitle: "Recent signals",
        listTag: "Live system",
        items: [
            { title: "Wallet synced", text: "Income, bills, and daily spending are reflected in one unified balance view." },
            { title: "Budget watch", text: "Food and transport categories stay visible before users cross their planned limit." },
            { title: "Alert ready", text: "Notification and reminder endpoints keep upcoming payments from being missed." },
        ],
        highlightTitle: "Snapshot",
        highlights: [
            { title: "Monthly cashflow", text: "Income is pacing ahead of fixed costs for the current cycle." },
            { title: "Savings ratio", text: "27% of inflow is already allocated to long-term targets." },
        ],
    },
    transactions: {
        kicker: "Transaction module",
        title: "Detailed transaction flows with search, stats, and reminders.",
        description: "The transaction controller supports CRUD operations, reporting views, search filters, statistics, and confirmation links for reminder-driven actions.",
        metrics: [
            { label: "Entries this month", value: "146", note: "Auto + manual combined" },
            { label: "Top category", value: "Food", note: "$420 tracked" },
            { label: "Search latency", value: "Fast", note: "Filter-based history" },
        ],
        listTitle: "Example activity feed",
        listTag: "History",
        items: [
            { title: "Salary deposit", text: "+$1,450 posted to main wallet and reflected in statistics." },
            { title: "Groceries", text: "-$86 categorized under essential spending with budget impact updated." },
            { title: "Gym membership", text: "Recurring charge linked to reminder confirmation workflow." },
        ],
        highlightTitle: "Why it matters",
        highlights: [
            { title: "Reporting ready", text: "A dedicated reporting endpoint makes financial summaries easier to surface." },
            { title: "Searchable records", text: "Filterable history gives users a practical audit trail for day-to-day finance." },
        ],
    },
    goals: {
        kicker: "Goal tracking",
        title: "Savings goals that move from intention to measurable progress.",
        description: "FinMate exposes goal creation, update, cancellation, contribution tracking, and progress reporting to support milestone-based saving behavior.",
        metrics: [
            { label: "Active goals", value: "3", note: "Trip, laptop, emergency" },
            { label: "Saved so far", value: "$4,780", note: "Across all goals" },
            { label: "Completion pace", value: "+18%", note: "Ahead of target" },
        ],
        listTitle: "Tracked milestones",
        listTag: "Progress",
        items: [
            { title: "Emergency fund", text: "80% complete with weekly contribution automation." },
            { title: "Study abroad", text: "Shared progress timeline with overdue checks and reminders." },
            { title: "New laptop", text: "Small recurring contributions keep momentum without budget shock." },
        ],
        highlightTitle: "Premium impact",
        highlights: [
            { title: "More goal capacity", text: "The backend enforces free-tier limits and premium expansion for active planners." },
            { title: "Progress visibility", text: "Goal tracking endpoints make milestone dashboards straightforward to build." },
        ],
    },
    premium: {
        kicker: "Monetization layer",
        title: "Premium packages, coupons, subscriptions, and AI forecasting.",
        description: "The premium surface extends the product with package management, promotional offers, subscription metrics, AI budget prediction, and feature-based access control.",
        metrics: [
            { label: "Premium packages", value: "3", note: "Flexible tiers" },
            { label: "AI module", value: "Active", note: "Budget prediction" },
            { label: "Offer engine", value: "Coupons", note: "Promo support" },
        ],
        listTitle: "Premium capabilities",
        listTag: "Revenue",
        items: [
            { title: "Package catalog", text: "Premium packages can be fetched, sorted, updated, and managed from dedicated endpoints." },
            { title: "Subscription data", text: "Revenue and subscriber endpoints support admin-level product monitoring." },
            { title: "AI recommendations", text: "Prediction and retraining routes point to an ambition beyond simple expense logging." },
        ],
        highlightTitle: "Product positioning",
        highlights: [
            { title: "Free-to-premium path", text: "Feature gating provides a clear upgrade reason for users who need deeper planning tools." },
            { title: "Operational visibility", text: "Admin logs and package analytics make the system more product-like and monetizable." },
        ],
    },
};

const elements = {
    kicker: document.getElementById("panel-kicker"),
    title: document.getElementById("panel-title"),
    description: document.getElementById("panel-description"),
    metricRow: document.getElementById("metric-row"),
    listTitle: document.getElementById("list-title"),
    listTag: document.getElementById("list-tag"),
    detailList: document.getElementById("detail-list"),
    highlightTitle: document.getElementById("highlight-title"),
    highlightBody: document.getElementById("highlight-body"),
};

const tabButtons = Array.from(document.querySelectorAll(".tab-button"));

function renderPanel(key) {
    const panel = panels[key];
    if (!panel) return;

    elements.kicker.textContent = panel.kicker;
    elements.title.textContent = panel.title;
    elements.description.textContent = panel.description;
    elements.listTitle.textContent = panel.listTitle;
    elements.listTag.textContent = panel.listTag;
    elements.highlightTitle.textContent = panel.highlightTitle;

    elements.metricRow.innerHTML = panel.metrics
        .map(
            (metric) => `
                <article class="metric-item">
                    <span>${metric.label}</span>
                    <strong>${metric.value}</strong>
                    <span>${metric.note}</span>
                </article>
            `,
        )
        .join("");

    elements.detailList.innerHTML = panel.items
        .map(
            (item) => `
                <article>
                    <strong>${item.title}</strong>
                    <p>${item.text}</p>
                </article>
            `,
        )
        .join("");

    elements.highlightBody.innerHTML = panel.highlights
        .map(
            (highlight) => `
                <article class="highlight-chip">
                    <strong>${highlight.title}</strong>
                    <p>${highlight.text}</p>
                </article>
            `,
        )
        .join("");

    tabButtons.forEach((button) => {
        button.classList.toggle("is-active", button.dataset.panel === key);
    });
}

tabButtons.forEach((button) => {
    button.addEventListener("click", () => renderPanel(button.dataset.panel));
});

renderPanel("overview");