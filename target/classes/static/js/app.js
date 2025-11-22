// Main Application Module

const App = {
    // Initialize the application
    init() {
        console.log('Initializing Study Buddy App...');

        // Initialize authentication
        Auth.init();

        // Set up navigation
        this.setupNavigation();

        // Set up form handlers
        this.setupForms();

        // Set up other event listeners
        this.setupEventListeners();

        // Load initial page
        this.navigateTo('home');

        console.log('App initialized successfully');
    },

    // Set up navigation handlers
    setupNavigation() {
        // Handle all navigation clicks
        document.addEventListener('click', (e) => {
            const pageLink = e.target.closest('[data-page]');
            if (pageLink) {
                e.preventDefault();
                const page = pageLink.getAttribute('data-page');
                this.navigateTo(page);
            }
        });

        // Mobile menu toggle
        const navToggle = document.getElementById('navToggle');
        const navMenu = document.getElementById('navMenu');

        if (navToggle && navMenu) {
            navToggle.addEventListener('click', () => {
                navMenu.classList.toggle('active');
            });

            // Close menu when clicking a link
            navMenu.addEventListener('click', (e) => {
                if (e.target.classList.contains('nav-link')) {
                    navMenu.classList.remove('active');
                }
            });
        }

        // Logout button
        const logoutBtn = document.getElementById('logoutBtn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', () => Auth.logout());
        }
    },

    // Navigate to a page
    navigateTo(pageName) {
        // Clean up timers when leaving sessionView
        if (Sessions.timerInterval) {
            clearInterval(Sessions.timerInterval);
            Sessions.timerInterval = null;
        }

        // Check if page requires authentication
        const protectedPages = ['create', 'profile'];
        if (protectedPages.includes(pageName) && !Auth.isLoggedIn()) {
            this.showToast('Please login to access this page', 'error');
            pageName = 'login';
        }

        // Hide all pages
        document.querySelectorAll('.page').forEach(page => {
            page.classList.remove('active');
        });

        // Show target page
        const targetPage = document.getElementById(`${pageName}Page`);
        if (targetPage) {
            targetPage.classList.add('active');
        }

        // Update nav links
        document.querySelectorAll('.nav-link').forEach(link => {
            link.classList.remove('active');
            if (link.getAttribute('data-page') === pageName) {
                link.classList.add('active');
            }
        });

        // Page-specific initialization
        this.onPageLoad(pageName);

        // Scroll to top
        window.scrollTo(0, 0);
    },

    // Handle page-specific initialization
    browseRefreshInterval: null,
    onPageLoad(pageName) {
        // Clear any refresh intervals from previous pages
        if (this.browseRefreshInterval) {
            clearInterval(this.browseRefreshInterval);
            this.browseRefreshInterval = null;
        }

        switch (pageName) {
            case 'browse':
                this.loadSessions();
                // Auto-refresh every 30 seconds to update timers and remove expired sessions
                this.browseRefreshInterval = setInterval(() => this.loadSessions(), 30000);
                break;
            case 'profile':
                this.loadProfile();
                break;
            case 'create':
                // Set default date to today
                const dateInput = document.getElementById('sessionDate');
                if (dateInput) {
                    const today = new Date().toISOString().split('T')[0];
                    dateInput.min = today;
                    dateInput.value = today;
                }

                // Set up startNow toggle (remove old listeners by cloning)
                const startNowCheckbox = document.getElementById('startNow');
                const scheduleFields = document.getElementById('scheduleFields');
                if (startNowCheckbox && scheduleFields) {
                    // Clone to remove old event listeners
                    const newCheckbox = startNowCheckbox.cloneNode(true);
                    startNowCheckbox.parentNode.replaceChild(newCheckbox, startNowCheckbox);

                    newCheckbox.addEventListener('change', (e) => {
                        if (e.target.checked) {
                            scheduleFields.style.display = 'none';
                        } else {
                            scheduleFields.style.display = 'block';
                        }
                    });
                    // Reset on page load
                    newCheckbox.checked = false;
                    scheduleFields.style.display = 'block';
                }
                break;
        }
    },

    // Set up form handlers
    setupForms() {
        // Login form
        const loginForm = document.getElementById('loginForm');
        if (loginForm) {
            loginForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                const email = document.getElementById('loginEmail').value;
                const password = document.getElementById('loginPassword').value;
                const errorEl = document.getElementById('loginError');

                try {
                    errorEl.textContent = '';
                    await Auth.login(email, password);
                    loginForm.reset();
                } catch (error) {
                    errorEl.textContent = error.message;
                }
            });
        }

        // Signup form
        const signupForm = document.getElementById('signupForm');
        if (signupForm) {
            signupForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                const name = document.getElementById('signupName').value;
                const email = document.getElementById('signupEmail').value;
                const year = document.getElementById('signupYear').value;
                const password = document.getElementById('signupPassword').value;
                const confirm = document.getElementById('signupConfirm').value;
                const errorEl = document.getElementById('signupError');

                // Validate passwords match
                if (password !== confirm) {
                    errorEl.textContent = 'Passwords do not match';
                    return;
                }

                try {
                    errorEl.textContent = '';
                    await Auth.signup(name, email, password, year);
                    signupForm.reset();
                } catch (error) {
                    errorEl.textContent = error.message;
                }
            });
        }

        // Create session form
        const createForm = document.getElementById('createSessionForm');
        if (createForm) {
            createForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                const errorEl = document.getElementById('createError');

                // Get form data
                const formData = new FormData(createForm);

                // Get preferences checkboxes
                const preferences = [];
                document.querySelectorAll('input[name="preferences"]:checked').forEach(cb => {
                    preferences.push(cb.value);
                });

                // Check if starting now
                const startNow = document.getElementById('startNow').checked;

                // Validate date/time if not starting now
                if (!startNow) {
                    if (!formData.get('date') || !formData.get('time')) {
                        errorEl.textContent = 'Please select date and time or check "Start Now"';
                        return;
                    }
                }

                const sessionData = {
                    title: formData.get('title'),
                    module: formData.get('module'),
                    year: formData.get('year'),
                    date: startNow ? null : formData.get('date'),
                    time: startNow ? null : formData.get('time'),
                    duration: parseInt(formData.get('duration')),
                    maxParticipants: parseInt(formData.get('maxParticipants')),
                    preferences: preferences.join(','),
                    description: formData.get('description'),
                    startNow: startNow
                };

                try {
                    errorEl.textContent = '';
                    await Sessions.create(sessionData);
                    this.showToast('Session created successfully!', 'success');
                    createForm.reset();
                    this.navigateTo('browse');
                } catch (error) {
                    errorEl.textContent = error.message;
                    this.showToast('Failed to create session', 'error');
                }
            });
        }
    },

    // Set up other event listeners
    setupEventListeners() {
        // Filter sessions
        const applyFilters = document.getElementById('applyFilters');
        if (applyFilters) {
            applyFilters.addEventListener('click', () => this.loadSessions());
        }

        // Join session buttons (delegated)
        document.addEventListener('click', async (e) => {
            if (e.target.classList.contains('join-btn')) {
                const sessionId = e.target.getAttribute('data-session-id');
                await this.handleJoinSession(sessionId);
            }

            // View session buttons
            if (e.target.classList.contains('view-btn')) {
                const sessionId = e.target.getAttribute('data-session-id');
                await Sessions.viewSession(sessionId);
            }
        });

        // Profile tabs
        document.querySelectorAll('.tab-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
                e.target.classList.add('active');
            });
        });
    },

    // Load sessions into browse page
    async loadSessions() {
        const grid = document.getElementById('sessionsGrid');
        const noSessions = document.getElementById('noSessions');

        if (!grid) return;

        // Get filters
        const year = document.getElementById('filterYear')?.value || '';
        const module = document.getElementById('filterModule')?.value || '';

        try {
            const sessions = await Sessions.getAll({ year, module });

            if (sessions.length === 0) {
                grid.innerHTML = '';
                noSessions?.classList.remove('hidden');
            } else {
                noSessions?.classList.add('hidden');
                grid.innerHTML = sessions.map(session => Sessions.renderCard(session)).join('');
            }
        } catch (error) {
            console.error('Error loading sessions:', error);
            this.showToast('Failed to load sessions', 'error');
        }
    },

    // Handle join session
    async handleJoinSession(sessionId) {
        if (!Auth.isLoggedIn()) {
            this.showToast('Please login to join a session', 'error');
            this.navigateTo('login');
            return;
        }

        try {
            await Sessions.requestJoin(sessionId);
            this.showToast('Join request sent! Waiting for host approval.', 'success');
            // Reload sessions to show updated status
            this.loadSessions();
        } catch (error) {
            this.showToast(error.message, 'error');
        }
    },

    // Load user profile
    async loadProfile() {
        if (!Auth.isLoggedIn()) return;

        try {
            // Get user data - fallback to Auth.currentUser if API fails
            let user = Auth.currentUser;
            let stats = { sessionsCreated: 0, sessionsJoined: 0, averageRating: 0, ratingCount: 0 };

            try {
                const [userData, userStats] = await Promise.all([
                    API.get('/users/me'),
                    API.get('/users/me/stats')
                ]);
                user = userData;
                stats = userStats;
            } catch (apiError) {
                console.warn('Could not fetch profile from API, using local data');
            }

            // Update profile UI
            const nameEl = document.getElementById('profileName');
            const yearEl = document.getElementById('profileYear');
            const emailEl = document.getElementById('profileEmail');
            const avatarEl = document.getElementById('profileAvatar');

            if (nameEl) nameEl.textContent = user.name || user.email || 'User';
            if (yearEl) yearEl.textContent = user.year ? `Year ${user.year}` : 'Year not set';
            if (emailEl) emailEl.textContent = user.email || '';
            if (avatarEl) {
                const name = user.name || user.email || 'U';
                const initials = name.split(' ').map(n => n[0]).join('').toUpperCase().substring(0, 2);
                avatarEl.querySelector('span').textContent = initials;
            }

            // Update stats
            const statSessions = document.getElementById('statSessions');
            const statJoined = document.getElementById('statJoined');
            const statRating = document.getElementById('statRating');

            if (statSessions) statSessions.textContent = stats.sessionsCreated || 0;
            if (statJoined) statJoined.textContent = stats.sessionsJoined || 0;
            if (statRating) {
                statRating.textContent = stats.averageRating > 0
                    ? stats.averageRating.toFixed(1)
                    : '-';
            }

            // Update rating stars and count
            const ratingStars = document.getElementById('profileStars');
            const ratingCountEl = document.getElementById('profileRatingCount');
            if (ratingStars) {
                if (stats.averageRating > 0) {
                    const fullStars = Math.floor(stats.averageRating);
                    ratingStars.textContent = '⭐'.repeat(fullStars) + '☆'.repeat(5 - fullStars);
                } else {
                    ratingStars.textContent = '☆☆☆☆☆';
                }
            }
            if (ratingCountEl) {
                ratingCountEl.textContent = `(${stats.ratingCount || 0} ratings)`;
            }

            // Load user's sessions
            await this.loadProfileSessions();

            // Set up tab switching
            this.setupProfileTabs();

        } catch (error) {
            console.error('Error loading profile:', error);
        }
    },

    // Load sessions for profile page
    async loadProfileSessions() {
        try {
            const [created, joined] = await Promise.all([
                Sessions.getMySessions(),
                Sessions.getJoined()
            ]);

            // Filter created sessions to find those with pending requests
            const pending = created.filter(session =>
                session.joinRequests && session.joinRequests.length > 0
            );

            // Store for tab switching
            this.profileData = { created, joined, pending };

            // Show created sessions by default
            this.showProfileTab('created');
        } catch (error) {
            console.error('Error loading profile sessions:', error);
        }
    },

    // Show specific tab content
    showProfileTab(tabName) {
        const container = document.getElementById('profileSessions');
        if (!container || !this.profileData) return;

        let sessions = [];
        if (tabName === 'created') {
            sessions = this.profileData.created || [];
        } else if (tabName === 'joined') {
            sessions = this.profileData.joined || [];
        } else if (tabName === 'pending') {
            sessions = this.profileData.pending || [];
        }

        if (sessions.length === 0) {
            container.innerHTML = `
                <div class="empty-state">
                    <p>No ${tabName === 'pending' ? 'pending requests' : tabName + ' sessions'} yet</p>
                </div>
            `;
        } else if (tabName === 'pending') {
            // Special rendering for pending requests
            this.renderPendingRequestsTab(container, sessions);
        } else {
            container.innerHTML = sessions.map(session => Sessions.renderCard(session)).join('');
        }
    },

    // Render pending requests tab with session info and request list
    async renderPendingRequestsTab(container, sessions) {
        container.innerHTML = sessions.map(session => {
            const requestCount = session.joinRequests ? session.joinRequests.length : 0;
            return `
                <div class="pending-session-card" data-session-id="${session.id}">
                    <div class="pending-session-header">
                        <div>
                            <h4 class="pending-session-title">${session.title}</h4>
                            <span class="pending-session-module">${session.module}</span>
                        </div>
                        <span class="pending-count">${requestCount} pending</span>
                    </div>
                    <div class="pending-requests-list" id="pending-${session.id}">
                        <p class="loading-text">Loading requests...</p>
                    </div>
                </div>
            `;
        }).join('');

        // Load request details for each session
        for (const session of sessions) {
            const listContainer = document.getElementById(`pending-${session.id}`);
            if (listContainer && session.joinRequests) {
                await Sessions.renderPendingRequests(session.id, session.joinRequests, listContainer);
            }
        }
    },

    // Set up profile tab switching
    setupProfileTabs() {
        const tabBtns = document.querySelectorAll('.profile-section .tab-btn');
        tabBtns.forEach(btn => {
            btn.addEventListener('click', (e) => {
                // Update active state
                tabBtns.forEach(b => b.classList.remove('active'));
                e.target.classList.add('active');

                // Show tab content
                const tabName = e.target.getAttribute('data-tab');
                this.showProfileTab(tabName);
            });
        });
    },

    // Show toast notification
    showToast(message, type = 'info') {
        const container = document.getElementById('toastContainer');
        if (!container) return;

        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.innerHTML = `<span>${message}</span>`;

        container.appendChild(toast);

        // Remove after 4 seconds
        setTimeout(() => {
            toast.style.animation = 'slideIn 0.3s ease reverse';
            setTimeout(() => toast.remove(), 300);
        }, 4000);
    }
};

// Initialize app when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    App.init();
});

// Export for use in other files
window.App = App;
