(ns net.http.websocket-test
  (:require [net.http.websocket :refer :all])
  (:use code.test))

^{:refer net.http.websocket/request->WebSocketListener :added "3.0"}
(fact "Constructs a new WebSocket listener to receive events for a given WebSocket connection.

  Takes a map of:

  - `:on-open`    Called when a `WebSocket` has been connected. Called with the WebSocket instance.
  - `:on-message` A textual/binary data has been received. Called with the WebSocket instance, the data, and whether this invocation completes the message.
  - `:on-ping`    A Ping message has been received. Called with the WebSocket instance and the ping message.
  - `:on-pong`    A Pong message has been received. Called with the WebSocket instance and the pong message.
  - `:on-close`   Receives a Close message indicating the WebSocket's input has been closed. Called with the WebSocket instance, the status code, and the reason.
  - `:on-error`   An error has occurred. Called with the WebSocket instance and the error.")

^{:refer net.http.websocket/websocket* :added "3.0"}
(fact "Same as `websocket` but take all arguments as a single map")

^{:refer net.http.websocket/websocket :added "3.0"}
(fact "Builds a new WebSocket connection from a request object and returns a future connection.

  Arguments:

  - `uri` a websocket uri
  - `opts` (optional), a map of:
    - `:http-client` An HttpClient - will use a default HttpClient if not provided
    - `:listener` A WebSocket$Listener - alternatively will be created from the handlers passed into opts:
                  :on-open, :on-message, :on-ping, :on-pong, :on-close, :on-error
    - `:headers` Adds the given name-value pair to the list of additional
                 HTTP headers sent during the opening handshake.
    - `:connect-timeout` Sets a timeout for establishing a WebSocket connection (in millis).
    - `:subprotocols` Sets a request for the given subprotocols.")

^{:refer net.http.websocket/send! :added "3.0"}
(fact "Sends a message to the WebSocket.

  `data` can be a CharSequence (e.g. string) or ByteBuffer")

^{:refer net.http.websocket/ping! :added "3.0"}
(fact  "Sends a Ping message with bytes from the given buffer.")

^{:refer net.http.websocket/pong! :added "3.0"}
(fact  "Sends a Pong message with bytes from the given buffer.")

^{:refer net.http.websocket/close! :added "3.0"}
(fact  "Initiates an orderly closure of this WebSocket's output by sending a
  Close message with the given status code and the reason.")

^{:refer net.http.websocket/abort! :added "3.0"}
(fact "Closes this WebSocket's input and output abruptly.")
