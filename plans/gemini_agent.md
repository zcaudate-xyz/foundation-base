# Gemini Agent Configuration

## Overview

This document provides context and instructions for the Gemini agent to interact with the `code.ai.server`. This server exposes a `bash` shell via a relay mechanism, allowing the agent to introspect the codebase, run tests, and execute system commands.

## Connection Options

There are two methods to connect to the environment:

### 1. Model Context Protocol (MCP) [Preferred]

If you are running in an environment that supports the Model Context Protocol (e.g., Claude Desktop, or a custom MCP client), you should use the `mcp-clj` server.

*   **Command:** `lein run -m mcp-clj.mcp-server.core`
*   **Capabilities:** Filesystem access (`ls`), Clojure evaluation (`clj-eval`).

### 2. Code AI Server (Manual Relay)

If you are connecting via a standard LLM interface or a custom script without MCP support, you can use the `code.ai.server`. This requires you (the agent) to manage a WebSocket connection for output and HTTP requests for input.

*   **Server URL:** `http://localhost:8080` (Default)
*   **WebSocket:** `ws://localhost:8080/ws`

## `code.ai.server` API Reference

### Endpoints

*   **POST** `/relays`
    *   **Body:** `{"command": "bash"}`
    *   **Response:** `{"id": "<relay-id>", ...}`
    *   **Description:** Creates a new shell session.

*   **POST** `/relay/<relay-id>/input`
    *   **Body:** `{"input": "ls -la\n"}`
    *   **Description:** Sends input (commands) to the shell. **Must include newline `\n`.**

*   **POST** `/relay/<relay-id>/stop`
    *   **Description:** Stops the relay session.

*   **GET** `/relays`
    *   **Description:** Lists active relays.

*   **WebSocket** `/ws`
    *   **Description:** Streams output from the shell (stdout/stderr).
    *   **Format:** JSON `{"type": "relay/output", "id": "<relay-id>", "line": "...", "type": "stdout|stderr"}`

## Python Bridge Script

Use the following Python script to bridge your environment with the `code.ai.server`. This script handles the asynchronous WebSocket connection and allows you to send commands synchronously.

```python
import asyncio
import websockets
import requests
import json
import threading
import sys
import time

SERVER_URL = "http://localhost:8080"
WS_URL = "ws://localhost:8080/ws"

class RelayClient:
    def __init__(self):
        self.relay_id = None
        self.running = True
        self.ws_thread = None

    def start(self):
        # 1. Start WebSocket listener
        self.ws_thread = threading.Thread(target=self._run_ws_loop)
        self.ws_thread.daemon = True
        self.ws_thread.start()

        # 2. Create Relay
        response = requests.post(f"{SERVER_URL}/relays", json={"command": "bash"})
        if response.status_code == 201:
            data = response.json()
            self.relay_id = data['id']
            print(f"Connected to relay: {self.relay_id}")
        else:
            print(f"Failed to create relay: {response.text}")
            sys.exit(1)

    def _run_ws_loop(self):
        asyncio.run(self._listen())

    async def _listen(self):
        async with websockets.connect(WS_URL) as websocket:
            while self.running:
                try:
                    message = await websocket.recv()
                    data = json.loads(message)
                    if data.get('type') == 'relay/output' and data.get('id') == self.relay_id:
                        # Print output directly to stdout so the Agent can see it
                        line = data.get('line', '').rstrip()
                        if line:
                            print(f"[OUT] {line}")
                except Exception as e:
                    if self.running:
                        print(f"WebSocket Error: {e}")
                    break

    def send_command(self, command):
        if not self.relay_id:
            print("No relay active.")
            return

        print(f"> {command}")
        payload = {"input": command + "\n"}
        res = requests.post(f"{SERVER_URL}/relay/{self.relay_id}/input", json=payload)
        if res.status_code != 200:
            print(f"Error sending command: {res.text}")

    def stop(self):
        self.running = False
        if self.relay_id:
            requests.post(f"{SERVER_URL}/relay/{self.relay_id}/stop")

if __name__ == "__main__":
    client = RelayClient()
    try:
        client.start()
        # Allow time for connection
        time.sleep(1)

        print("Ready. Type commands (or 'exit' to quit).")

        # Simple REPL
        while True:
            try:
                cmd = input()
                if cmd.strip() == 'exit':
                    break
                client.send_command(cmd)
                # Small sleep to allow output to flush (optional)
                time.sleep(0.5)
            except EOFError:
                break
    finally:
        client.stop()
```

## Agent Persona Instructions

**Role:** You are an autonomous software engineer.

**Objective:** Explore the codebase, understand the architecture, and perform tasks such as running tests or querying code.

**Protocol:**
1.  **Check Environment:** If you have access to the `RelayClient` script (or can write and run it), use it to establish a shell session.
2.  **Explore:** Use `ls -R`, `find`, and `grep` to navigate the source tree (`src/code`).
3.  **Read Code:** Use `cat` or `grep` to read file contents.
4.  **Run Tests:** Use `lein test` to run the test suite.
    *   *Note:* The codebase uses `code.test`.
5.  **Reflect:** Analyze the output and determine the next step.

**Important:**
-   The server returns output asynchronously. Ensure your script captures and prints it.
-   If you need to execute complex logic, write a script to a file (e.g., `script.py` or `script.clj`) using `echo "..." > file` and then execute it.
