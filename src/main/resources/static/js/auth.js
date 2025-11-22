// Authentication Module

const Auth = {
    // Current user state
    currentUser: null,

    // Initialize auth
    init() {
        this.checkAuth();
    },

    // Check if user is authenticated
    async checkAuth() {
        const token = API.getToken();
        if (token) {
            try {
                const user = await API.get('/users/me');
                this.currentUser = user;
                this.updateUI(user);
            } catch (error) {
                // Token invalid, clear it
                API.removeToken();
                this.currentUser = null;
                this.updateUI(null);
            }
        } else {
            this.updateUI(null);
        }
    },

    // Update UI based on auth state
    updateUI(user) {
        const authLinks = document.getElementById('authLinks');
        const userMenu = document.getElementById('userMenu');
        const userName = document.getElementById('userName');

        if (user) {
            authLinks.classList.add('hidden');
            userMenu.classList.remove('hidden');
            userName.textContent = user.name;
        } else {
            authLinks.classList.remove('hidden');
            userMenu.classList.add('hidden');
        }
    },

    // Sign up new user
    async signup(name, email, password, year) {
        try {
            const response = await API.post('/auth/signup', {
                name,
                email,
                password,
                year
            });

            API.setToken(response.token);
            this.currentUser = {
                id: response.userId,
                name: response.name,
                email: response.email
            };
            this.updateUI(this.currentUser);

            App.showToast('Account created successfully!', 'success');
            App.navigateTo('browse');

            return response;
        } catch (error) {
            throw error;
        }
    },

    // Sign in existing user
    async login(email, password) {
        try {
            const response = await API.post('/auth/login', {
                email,
                password
            });

            API.setToken(response.token);
            this.currentUser = {
                id: response.userId,
                name: response.name,
                email: response.email
            };
            this.updateUI(this.currentUser);

            App.showToast('Welcome back!', 'success');
            App.navigateTo('browse');

            return response;
        } catch (error) {
            throw error;
        }
    },

    // Sign out user
    logout() {
        API.removeToken();
        this.currentUser = null;
        this.updateUI(null);
        App.showToast('Logged out successfully', 'info');
        App.navigateTo('home');
    },

    // Check if user is logged in
    isLoggedIn() {
        return this.currentUser !== null;
    },

    // Get current user
    getUser() {
        return this.currentUser;
    }
};

// Export for use in other files
window.Auth = Auth;
