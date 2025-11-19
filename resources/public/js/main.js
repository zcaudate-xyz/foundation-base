document.addEventListener('DOMContentLoaded', () => {
    const appDiv = document.getElementById('app');
    const relaysListDiv = document.getElementById('relays-list');
    const newRelayCommandInput = document.getElementById('newRelayCommand');
    const createRelayButton = document.getElementById('createRelayButton');
    const globalMessagesDiv = document.getElementById('global-messages');

    const relayOutputElements = {}; // Stores output divs for each relay

    // WebSocket setup
    const ws = new WebSocket('ws://localhost:8080/ws');

    ws.onopen = () => {
        console.log('WebSocket connected');
        appendGlobalMessage('WebSocket connected.');
        fetchRelays(); // Fetch relays once connected
    };

    ws.onmessage = (event) => {
        const message = JSON.parse(event.data);
        console.log('Received WebSocket message:', message);

        switch (message.type) {
            case 'relay/output':
                handleRelayOutput(message.data);
                break;
            case 'relay/update': // Assuming backend sends updates for relay status
                updateRelayCard(message.data);
                break;
            default:
                appendGlobalMessage(`Echo: ${JSON.stringify(message)}`);
                break;
        }
    };

    ws.onclose = () => {
        console.log('WebSocket disconnected');
        appendGlobalMessage('WebSocket disconnected.');
    };

    ws.onerror = (error) => {
        console.error('WebSocket error:', error);
        appendGlobalMessage(`WebSocket error: ${error.message}`);
    };

    function appendGlobalMessage(text) {
        const p = document.createElement('p');
        p.textContent = text;
        globalMessagesDiv.appendChild(p);
        globalMessagesDiv.scrollTop = globalMessagesDiv.scrollHeight;
    }

    function handleRelayOutput(data) {
        const { id, type, line } = data;
        if (relayOutputElements[id]) {
            const p = document.createElement('p');
            p.textContent = `[${type}] ${line}`;
            relayOutputElements[id].appendChild(p);
            relayOutputElements[id].scrollTop = relayOutputElements[id].scrollHeight;
        }
    }

    async function fetchRelays() {
        try {
            const response = await fetch('/relays');
            const relays = await response.json(); // Assuming JSON for initial fetch
            relaysListDiv.innerHTML = ''; // Clear existing list
            relays.forEach(relay => renderRelayCard(relay));
        } catch (error) {
            console.error('Error fetching relays:', error);
            appendGlobalMessage(`Error fetching relays: ${error.message}`);
        }
    }

    async function createRelay() {
        const command = newRelayCommandInput.value;
        if (!command) {
            alert('Please enter a command.');
            return;
        }

        try {
            const response = await fetch('/relays', {
                method: 'POST',
                headers: { 'Content-Type': 'application/edn' },
                body: JSON.stringify({ command: command })
            });
            const newRelay = await response.json();
            renderRelayCard(newRelay);
            newRelayCommandInput.value = '';
            appendGlobalMessage(`Relay ${newRelay.id} created.`);
        } catch (error) {
            console.error('Error creating relay:', error);
            appendGlobalMessage(`Error creating relay: ${error.message}`);
        }
    }

    async function sendRelayInput(id, input) {
        try {
            await fetch(`/relay/${id}/input`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/edn' },
                body: JSON.stringify({ input: input })
            });
            appendGlobalMessage(`Input sent to relay ${id}.`);
        } catch (error) {
            console.error(`Error sending input to relay ${id}:`, error);
            appendGlobalMessage(`Error sending input to relay ${id}: ${error.message}`);
        }
    }

    async function stopRelay(id) {
        try {
            await fetch(`/relay/${id}/stop`, {
                method: 'POST'
            });
            appendGlobalMessage(`Relay ${id} stopped.`);
            // Optionally remove card or update status immediately
            const card = document.getElementById(`relay-card-${id}`);
            if (card) {
                const statusSpan = card.querySelector('.status');
                if (statusSpan) {
                    statusSpan.textContent = 'Stopped';
                    statusSpan.classList.remove('running');
                    statusSpan.classList.add('stopped');
                }
                // Disable controls after stopping
                card.querySelectorAll('.control-button, .input-field').forEach(el => el.disabled = true);
            }
        } catch (error) {
            console.error(`Error stopping relay ${id}:`, error);
            appendGlobalMessage(`Error stopping relay ${id}: ${error.message}`);
        }
    }

    function renderRelayCard(relay) {
        const { id, command, running } = relay;
        let card = document.getElementById(`relay-card-${id}`);
        if (!card) {
            card = document.createElement('div');
            card.id = `relay-card-${id}`;
            card.className = 'relay-card';
            relaysListDiv.appendChild(card);
        }

        card.innerHTML = `
            <h3>Relay ID: ${id}</h3>
            <p>Command: <code>${command}</code></p>
            <p>Status: <span class="status ${running ? 'running' : 'stopped'}">${running ? 'Running' : 'Stopped'}</span></p>
            <div class="output" id="relay-output-${id}"></div>
            <div class="controls">
                <input type="text" class="input-field" placeholder="Send input">
                <button class="control-button input-button">Send</button>
                <button class="control-button stop-button">Stop</button>
            </div>
        `;

        // Store reference to output div
        relayOutputElements[id] = document.getElementById(`relay-output-${id}`);

        // Add event listeners
        card.querySelector('.input-button').addEventListener('click', (e) => {
            const inputField = e.target.previousElementSibling;
            sendRelayInput(id, inputField.value);
            inputField.value = '';
        });
        card.querySelector('.stop-button').addEventListener('click', () => stopRelay(id));
    }

    function updateRelayCard(relay) {
        const { id, running } = relay;
        const card = document.getElementById(`relay-card-${id}`);
        if (card) {
            const statusSpan = card.querySelector('.status');
            if (statusSpan) {
                statusSpan.textContent = running ? 'Running' : 'Stopped';
                statusSpan.classList.toggle('running', running);
                statusSpan.classList.toggle('stopped', !running);
            }
            // Enable/disable controls based on running status
            card.querySelectorAll('.control-button, .input-field').forEach(el => el.disabled = !running);
        }
    }

    // Initial setup
    createRelayButton.addEventListener('click', createRelay);
    // fetchRelays is called on WebSocket open
});