#!/bin/bash

# Start the SSE connection in the background
curl -N -s http://localhost:3002/sse > sse_output.txt &
SSE_PID=$!

echo "Started SSE connection with PID $SSE_PID"
sleep 2

# Extract session ID from sse_output.txt
# Expected format: event: endpoint\ndata: /messages?session_id=...\n
SESSION_ID=$(grep -o "session_id=[a-f0-9]*" sse_output.txt | cut -d= -f2 | head -n 1)

if [ -z "$SESSION_ID" ]; then
  echo "Failed to get session ID"
  kill $SSE_PID
  exit 1
fi

echo "Got session ID: $SESSION_ID"

# Send initialize request
echo "Sending initialize..."
curl -s -X POST "http://localhost:3002/messages?session_id=$SESSION_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 1,
    "method": "initialize",
    "params": {
      "protocolVersion": "2024-11-05",
      "capabilities": {},
      "clientInfo": {"name": "test-client", "version": "1.0"}
    }
  }'
echo ""

# Send initialized notification
echo "Sending initialized..."
curl -s -X POST "http://localhost:3002/messages?session_id=$SESSION_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "method": "notifications/initialized",
    "params": {}
  }'
echo ""

# Send ping
echo "Sending ping..."
curl -s -X POST "http://localhost:3002/messages?session_id=$SESSION_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 2,
    "method": "ping",
    "params": {}
  }'
echo ""

# Send tools/list
echo "Sending tools/list..."
curl -s -X POST "http://localhost:3001/messages?session_id=$SESSION_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 3,
    "method": "tools/list",
    "params": {}
  }'
echo ""

# Send tool call (echo)
echo "Sending tool call (echo)..."
curl -s -X POST "http://localhost:3001/messages?session_id=$SESSION_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 4,
    "method": "tools/call",
    "params": {
      "name": "echo",
      "arguments": {"text": "hello"}
    }
  }'
echo ""

sleep 2
kill $SSE_PID
echo "Done."
cat sse_output.txt
rm sse_output.txt
