// Chat Module - Handles session chat functionality

const Chat = {
    currentSessionId: null,
    messages: [],
    pollInterval: null,
    lastTimestamp: 0,

    // Initialize chat for a session
    async init(sessionId) {
        // Clean up any existing chat first
        this.cleanup();

        this.currentSessionId = sessionId;
        this.messages = [];
        this.lastTimestamp = 0;

        // Set up form handler - remove any existing handler first
        const chatForm = document.getElementById('chatForm');
        const newForm = chatForm.cloneNode(true);
        chatForm.parentNode.replaceChild(newForm, chatForm);
        newForm.onsubmit = (e) => this.handleSendMessage(e);

        // Load existing messages
        await this.loadMessages();

        // Start polling for new messages
        this.startPolling();
    },

    // Load all messages for the session
    async loadMessages() {
        try {
            const messages = await API.get(`/sessions/${this.currentSessionId}/chat`);
            this.messages = messages;

            if (messages.length > 0) {
                this.lastTimestamp = messages[messages.length - 1].timestamp;
            }

            this.renderMessages();
        } catch (error) {
            console.error('Error loading messages:', error);
            this.updateStatus('Error loading messages');
        }
    },

    // Send a new message
    async handleSendMessage(e) {
        e.preventDefault();

        const input = document.getElementById('chatInput');
        const content = input.value.trim();

        if (!content) return;

        try {
            const message = await API.post(`/sessions/${this.currentSessionId}/chat`, {
                content: content
            });

            // Add message to local array
            this.messages.push(message);
            this.lastTimestamp = message.timestamp;

            // Clear input and render
            input.value = '';
            this.renderMessages();
            this.scrollToBottom();
        } catch (error) {
            console.error('Error sending message:', error);
            App.showToast('Failed to send message', 'error');
        }
    },

    // Poll for new messages
    startPolling() {
        // Clear any existing interval
        if (this.pollInterval) {
            clearInterval(this.pollInterval);
        }

        // Poll every 3 seconds
        this.pollInterval = setInterval(async () => {
            await this.checkNewMessages();
        }, 3000);
    },

    // Stop polling
    stopPolling() {
        if (this.pollInterval) {
            clearInterval(this.pollInterval);
            this.pollInterval = null;
        }
    },

    // Check for new messages since last timestamp
    async checkNewMessages() {
        if (!this.currentSessionId) return;

        try {
            const newMessages = await API.get(
                `/sessions/${this.currentSessionId}/chat?since=${this.lastTimestamp}`
            );

            if (newMessages.length > 0) {
                // Add new messages to array
                this.messages.push(...newMessages);
                this.lastTimestamp = newMessages[newMessages.length - 1].timestamp;

                // Re-render and scroll
                this.renderMessages();
                this.scrollToBottom();
            }

            this.updateStatus('Connected');
        } catch (error) {
            console.error('Error checking messages:', error);
            this.updateStatus('Connection error');
        }
    },

    // Render all messages
    renderMessages() {
        const container = document.getElementById('chatMessages');
        const currentUserId = Auth.currentUser?.id;

        if (this.messages.length === 0) {
            container.innerHTML = `
                <div class="chat-empty">
                    <p>No messages yet. Start the conversation!</p>
                </div>
            `;
            return;
        }

        container.innerHTML = this.messages.map(msg => {
            const isOwn = msg.senderId === currentUserId;
            const time = this.formatTime(msg.timestamp);

            return `
                <div class="chat-message ${isOwn ? 'own' : ''}">
                    <div class="message-header">
                        <span class="message-sender">${isOwn ? 'You' : msg.senderName}</span>
                        <span class="message-time">${time}</span>
                    </div>
                    <div class="message-content">${this.escapeHtml(msg.content)}</div>
                </div>
            `;
        }).join('');
    },

    // Scroll chat to bottom
    scrollToBottom() {
        const container = document.getElementById('chatMessages');
        container.scrollTop = container.scrollHeight;
    },

    // Update connection status
    updateStatus(status) {
        const statusEl = document.getElementById('chatStatus');
        if (statusEl) {
            statusEl.textContent = status;
            statusEl.className = 'chat-status ' + (status === 'Connected' ? 'connected' : 'error');
        }
    },

    // Format timestamp to readable time
    formatTime(timestamp) {
        const date = new Date(timestamp);
        return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    },

    // Escape HTML to prevent XSS
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    },

    // Clean up when leaving chat
    cleanup() {
        this.stopPolling();
        this.currentSessionId = null;
        this.messages = [];
        this.lastTimestamp = 0;
    }
};

// Export for use in other files
window.Chat = Chat;
