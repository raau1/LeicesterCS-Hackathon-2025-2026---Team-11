// Sessions Module - Handles all session-related operations

const Sessions = {
    // Create a new study session
    async create(sessionData) {
        try {
            const response = await API.post('/sessions', sessionData);
            return response;
        } catch (error) {
            throw error;
        }
    },

    // Get all sessions with optional filters
    async getAll(filters = {}) {
        try {
            let endpoint = '/sessions';
            const params = new URLSearchParams();

            if (filters.year) {
                params.append('year', filters.year);
            }
            if (filters.module) {
                params.append('module', filters.module);
            }

            if (params.toString()) {
                endpoint += `?${params.toString()}`;
            }

            return await API.get(endpoint);
        } catch (error) {
            console.error('Error getting sessions:', error);
            return [];
        }
    },

    // Get a single session by ID
    async getById(sessionId) {
        try {
            return await API.get(`/sessions/${sessionId}`);
        } catch (error) {
            throw error;
        }
    },

    // Get sessions created by current user
    async getMySessions() {
        try {
            return await API.get('/sessions/my-sessions');
        } catch (error) {
            console.error('Error getting my sessions:', error);
            return [];
        }
    },

    // Get sessions user has joined
    async getJoined() {
        try {
            return await API.get('/sessions/joined');
        } catch (error) {
            console.error('Error getting joined sessions:', error);
            return [];
        }
    },

    // Request to join a session
    async requestJoin(sessionId) {
        try {
            return await API.post(`/sessions/${sessionId}/request`, {});
        } catch (error) {
            throw error;
        }
    },

    // Accept a join request
    async acceptRequest(sessionId, userId) {
        try {
            return await API.post(`/sessions/${sessionId}/accept/${userId}`, {});
        } catch (error) {
            throw error;
        }
    },

    // Decline a join request
    async declineRequest(sessionId, userId) {
        try {
            return await API.post(`/sessions/${sessionId}/decline/${userId}`, {});
        } catch (error) {
            throw error;
        }
    },

    // Delete a session
    async delete(sessionId) {
        try {
            return await API.delete(`/sessions/${sessionId}`);
        } catch (error) {
            throw error;
        }
    },

    // Render session card HTML
    renderCard(session) {
        const dateObj = session.date ? new Date(session.date) : new Date();
        const formattedDate = dateObj.toLocaleDateString('en-GB', {
            weekday: 'short',
            day: 'numeric',
            month: 'short'
        });

        const preferences = session.preferences || [];
        const preferenceTags = preferences.map(pref =>
            `<span class="preference-tag">${pref}</span>`
        ).join('');

        const spotsLeft = session.spotsLeft || (session.maxParticipants - (session.participantCount || 1));
        const initials = session.creatorName ?
            session.creatorName.split(' ').map(n => n[0]).join('').toUpperCase() : 'U';

        return `
            <div class="session-card" data-session-id="${session.id}">
                <div class="session-header">
                    <div>
                        <h3 class="session-title">${session.title}</h3>
                        <span class="session-module">${session.module}</span>
                    </div>
                    <span class="session-status ${session.status}">${session.status}</span>
                </div>

                <div class="session-details">
                    <div class="session-detail">
                        <span class="session-detail-icon">📅</span>
                        ${formattedDate} at ${session.time}
                    </div>
                    <div class="session-detail">
                        <span class="session-detail-icon">📍</span>
                        ${session.location}
                    </div>
                    <div class="session-detail">
                        <span class="session-detail-icon">👥</span>
                        ${spotsLeft} spots left
                    </div>
                    <div class="session-detail">
                        <span class="session-detail-icon">🎓</span>
                        Year ${session.year}
                    </div>
                </div>

                ${preferenceTags ? `<div class="session-preferences">${preferenceTags}</div>` : ''}

                <div class="session-footer">
                    <div class="session-creator">
                        <div class="creator-avatar">${initials}</div>
                        <div>
                            <div class="creator-name">${session.creatorName}</div>
                            <div class="creator-rating">⭐ 4.5</div>
                        </div>
                    </div>
                    <button class="btn btn-primary btn-sm join-btn" data-session-id="${session.id}">
                        Join
                    </button>
                </div>
            </div>
        `;
    }
};

// Export for use in other files
window.Sessions = Sessions;
