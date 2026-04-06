(ns net.http.websocket-test
  (:require [net.http.websocket :refer :all])
  (:use code.test))

(defn mock-ws [state]
  (reify java.net.http.WebSocket
    (request [_ n] (swap! state conj [:request n]) nil)
    (sendBinary [_ data last?] (swap! state conj [:binary data last?]) :binary)
    (sendText [_ data last?] (swap! state conj [:text data last?]) :text)
    (sendPing [_ data] (swap! state conj [:ping data]) :ping)
    (sendPong [_ data] (swap! state conj [:pong data]) :pong)
    (sendClose [_ status reason] (swap! state conj [:close status reason]) :close)
    (abort [_] (swap! state conj [:abort]) :abort)
    (getSubprotocol [_] "")
    (isInputClosed [_] false)
    (isOutputClosed [_] false)))

^{:refer net.http.websocket/request->WebSocketListener :added "3.0"}
(fact "Constructs a new WebSocket listener to receive events for a given WebSocket connection.

  Takes a map of:

  - `:on-open`    Called when a `WebSocket` has been connected. Called with the WebSocket instance.
  - `:on-message` A textual/binary data has been received. Called with the WebSocket instance, the data, and whether this invocation completes the message.
  - `:on-ping`    A Ping message has been received. Called with the WebSocket instance and the ping message.
  - `:on-pong`    A Pong message has been received. Called with the WebSocket instance and the pong message.
  - `:on-close`   Receives a Close message indicating the WebSocket's input has been closed. Called with the WebSocket instance, the status code, and the reason.
  - `:on-error`   An error has occurred. Called with the WebSocket instance and the error."
  (let [state    (atom [])
        ws       (mock-ws state)
        listener (request->WebSocketListener
                  {:on-open    (fn [_] (swap! state conj :open))
                   :on-message (fn [_ data last?] (swap! state conj [:message data last?]))
                   :on-ping    (fn [_ _] (swap! state conj :ping-handler))
                   :on-pong    (fn [_ _] (swap! state conj :pong-handler))
                   :on-close   (fn [_ status reason] (swap! state conj [:close-handler status reason]))
                   :on-error   (fn [_ err] (swap! state conj [:error (.getMessage err)]))})]
    (.onOpen listener ws)
    (.join (.onText listener ws "hello" true))
    (.join (.onPing listener ws (java.nio.ByteBuffer/wrap (.getBytes "p"))))
    (.join (.onPong listener ws (java.nio.ByteBuffer/wrap (.getBytes "q"))))
    (.join (.onClose listener ws 1000 "bye"))
    (.onError listener ws (ex-info "boom" {}))
    @state)
  => [[:request 1]
      :open
      [:request 1]
      [:message "hello" true]
      [:request 1]
      :ping-handler
      [:request 1]
      :pong-handler
      [:close-handler 1000 "bye"]
      [:error "boom"]])

^{:refer net.http.websocket/websocket* :added "3.0"}
(fact "Same as `websocket` but take all arguments as a single map"
  websocket*
  => fn?)

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
    - `:subprotocols` Sets a request for the given subprotocols."
  websocket
  => fn?)

^{:refer net.http.websocket/send! :added "3.0"}
(fact "Sends a message to the WebSocket.
 
  `data` can be a CharSequence (e.g. string) or ByteBuffer"
  (let [state (atom [])
        ws    (mock-ws state)]
    [(send! ws "hello")
     (send! ws (java.nio.ByteBuffer/wrap (.getBytes "hi")))
     @state])
  => [:text :binary [[:text "hello" true]
                     [:binary anything true]]])

^{:refer net.http.websocket/ping! :added "3.0"}
(fact  "Sends a Ping message with bytes from the given buffer."
  (let [state (atom [])
        ws    (mock-ws state)]
    [(ping! ws (java.nio.ByteBuffer/wrap (.getBytes "hi")))
     @state])
  => [:ping [[:ping anything]]])

^{:refer net.http.websocket/pong! :added "3.0"}
(fact  "Sends a Pong message with bytes from the given buffer."
  (let [state (atom [])
        ws    (mock-ws state)]
    [(pong! ws (java.nio.ByteBuffer/wrap (.getBytes "hi")))
     @state])
  => [:pong [[:pong anything]]])

^{:refer net.http.websocket/close! :added "3.0"}
(fact  "Initiates an orderly closure of this WebSocket's output by sending a
  Close message with the given status code and the reason."
  (let [state (atom [])
        ws    (mock-ws state)]
    [(close! ws)
     @state])
  => [:close [[:close 1000 ""]]])

^{:refer net.http.websocket/abort! :added "3.0"}
(fact "Closes this WebSocket's input and output abruptly."
  (let [state (atom [])
        ws    (mock-ws state)]
    [(abort! ws)
     @state])
  => [:abort [[:abort]]])
