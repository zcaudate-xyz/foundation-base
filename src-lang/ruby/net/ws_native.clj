(ns ruby.net.ws-native
  (:require [hara.lang :as l]
            [xt.lang.common-protocol :refer [defimpl.xt]]))

(l/script :ruby
  {:require [[xt.lang.spec-base :as xt]
             [xt.net.ws-native :as websocket]]})

;;
;; minimal RFC6455 client over TCPSocket (ws:// only, no TLS)
;;

(defn.rb ws-read-exact [sock n]
  (var data (. sock (read n)))
  (if (xt/x:nil? data) (return nil))
  (if (< (. data bytesize) n) (return nil))
  (return data))

(defn.rb ws-send-frame [sock opcode input]
  (var pbytes (. input bytes))
  (var n (. input bytesize))
  (var mask (. (:- "SecureRandom") (random_bytes 4)))
  (var mbytes (. mask bytes))
  (var header [])
  (cond (< n 126)
        (:= header [(b:| 128 opcode) (b:| 128 n)])

        (< n 65536)
        (:= header [(b:| 128 opcode) (b:| 128 126)
                    (b:& (b:>> n 8) 255)
                    (b:& n 255)])

        :else
        (:= header [(b:| 128 opcode) (b:| 128 127)
                    0 0 0 0
                    (b:& (b:>> n 24) 255)
                    (b:& (b:>> n 16) 255)
                    (b:& (b:>> n 8) 255)
                    (b:& n 255)]))
  (var masked [])
  (var i 0)
  (while (< i n)
    (xt/x:arr-push masked (b:xor (. pbytes [i]) (. mbytes [(b:& i 3)])))
    (:= i (+ i 1)))
  (. sock (write (xt/x:cat (. header (pack "C*"))
                           mask
                           (. masked (pack "C*")))))
  (return sock))

(defn.rb ws-recv-frame [sock]
  (var header (-/ws-read-exact sock 2))
  (if (xt/x:nil? header) (return nil))
  (var b0 (. header (getbyte 0)))
  (var b1 (. header (getbyte 1)))
  (var opcode (b:& b0 15))
  (var n (b:& b1 127))
  (when (== n 126)
    (var ext (-/ws-read-exact sock 2))
    (if (xt/x:nil? ext) (return nil))
    (:= n (+ (* (. ext (getbyte 0)) 256)
             (. ext (getbyte 1)))))
  (when (== n 127)
    (var ext8 (-/ws-read-exact sock 8))
    (if (xt/x:nil? ext8) (return nil))
    (:= n (+ (* (. ext8 (getbyte 4)) 16777216)
             (+ (* (. ext8 (getbyte 5)) 65536)
                (+ (* (. ext8 (getbyte 6)) 256)
                   (. ext8 (getbyte 7)))))))
  (var mask-bytes nil)
  (when (> b1 127)
    (var mask-data (-/ws-read-exact sock 4))
    (if (xt/x:nil? mask-data) (return nil))
    (:= mask-bytes (. mask-data bytes)))
  (var payload "")
  (when (> n 0)
    (var payload-data (-/ws-read-exact sock n))
    (if (xt/x:nil? payload-data) (return nil))
    (:= payload payload-data))
  (when (xt/x:not-nil? mask-bytes)
    (var pbytes (. payload bytes))
    (var unmasked [])
    (var i 0)
    (while (< i n)
      (xt/x:arr-push unmasked (b:xor (. pbytes [i]) (. mask-bytes [(b:& i 3)])))
      (:= i (+ i 1)))
    (:= payload (. unmasked (pack "C*"))))
  (cond (== opcode 8)
        (return {"type" "close" "data" payload})

        (== opcode 9)
        (do (-/ws-send-frame sock 10 payload)
            (return {"type" "ping" "data" payload}))

        (== opcode 10)
        (return {"type" "pong" "data" payload})

        :else
        (return {"type" "text" "data" payload})))

(defn.rb ws-handshake [url]
  (require "socket")
  (require "uri")
  (require "digest/sha1")
  (require "base64")
  (require "securerandom")
  (var uri (. (:- "URI") (parse url)))
  (var path (. uri path))
  (when (or (xt/x:nil? path) (== path ""))
    (:= path "/"))
  (var sock (. (:- "TCPSocket") (new (. uri host) (. uri port))))
  (var key (. (:- "Base64")
              (strict_encode64 (. (:- "SecureRandom") (random_bytes 16)))))
  (. sock (write (xt/x:cat "GET " path " HTTP/1.1\r\n"
                           "Host: " (. uri host) ":" (xt/x:to-string (. uri port)) "\r\n"
                           "Upgrade: websocket\r\n"
                           "Connection: Upgrade\r\n"
                           "Sec-WebSocket-Key: " key "\r\n"
                           "Sec-WebSocket-Version: 13\r\n\r\n")))
  (var status (. sock gets))
  (when (xt/x:nil? status)
    (xt/x:err "websocket handshake failed: eof"))
  (when (not (. status (include? "101")))
    (xt/x:err (xt/x:cat "websocket handshake failed: " status)))
  (var accept nil)
  (var line (. sock gets))
  (while (xt/x:not-nil? line)
    (:= line (. line strip))
    (when (== line "") (break))
    (when (. (. line downcase) (start_with? "sec-websocket-accept:"))
      (:= accept (. (. (. line (split ":" 2)) [1]) strip)))
    (:= line (. sock gets)))
  (var expected (. (:- "Base64")
                   (strict_encode64
                    (. (:- "Digest::SHA1")
                       (digest (xt/x:cat key "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"))))))
  (when (not (== accept expected))
    (xt/x:err "websocket handshake accept mismatch"))
  (return sock))

;;
;; adapter (mirrors python.net.ws-native)
;;

(defn.rb dispatch-ws [client event payload]
  (var callbacks (xt/x:get-key client "callbacks"))
  (var handler (xt/x:get-key callbacks event))
  (when (xt/x:is-function? handler) (handler payload))
  (return payload))

(defn.rb receive-loop [client]
  (var raw (xt/x:get-key client "raw"))
  (while (xt/x:not-nil? raw)
    (try
      (var frame (-/ws-recv-frame raw))
      (cond (xt/x:nil? frame)
            (break)

            (== "text" (xt/x:get-key frame "type"))
            (-/dispatch-ws client "message" {"data" (xt/x:get-key frame "data")})

            (== "close" (xt/x:get-key frame "type"))
            (break))
      (catch err
        (-/dispatch-ws client "error" err)
        (break)))
    (:= raw (xt/x:get-key client "raw")))
  (-/dispatch-ws client "close" {})
  (return client))

(defn.rb connect-ws [client opts]
  (var url (websocket/prepare-url client (or opts {})))
  (var raw (-/ws-handshake url))
  (xt/x:set-key client "raw" raw)
  (var thunk (fn [] (return (-/receive-loop client))))
  (var thread (. (:- "Thread") (new (:- "&thunk"))))
  (xt/x:set-key client "thread" thread)
  (return client))

(defn.rb disconnect-ws [client]
  (var raw (xt/x:get-key client "raw"))
  (xt/x:set-key client "raw" nil)
  (when (xt/x:not-nil? raw) (. raw (close)))
  (return client))

(defn.rb send-ws [client input]
  (var raw (xt/x:get-key client "raw"))
  (when (xt/x:not-nil? raw) (-/ws-send-frame raw 1 input))
  (return client))

(defn.rb add-listeners-ws [client m]
  (var callbacks (xt/x:get-key client "callbacks"))
  (xt/for:object [[event handler] m] (xt/x:set-key callbacks event handler))
  (return (xt/x:obj-keys m)))

(defn.rb start-heartbeat-ws [client name f interval] (return nil))
(defn.rb stop-heartbeat-ws [client name] (return client))

(defimpl.xt ^{:lang :ruby}
  RubyWebsocketClient
  [raw defaults state callbacks thread]
  websocket/IWebsocket
  {websocket/connect -/connect-ws
   websocket/disconnect -/disconnect-ws
   websocket/send -/send-ws
   websocket/add-listeners -/add-listeners-ws}
  websocket/IWebsocketHeartbeat
  {websocket/start-heartbeat -/start-heartbeat-ws
   websocket/stop-heartbeat -/stop-heartbeat-ws})

(defn.rb create [defaults]
  (var client (-/RubyWebsocketClient nil (or defaults {}) {} {} nil))
  (xt/x:set-key client "::/override"
                {"connect" -/connect-ws
                 "disconnect" -/disconnect-ws
                 "send" -/send-ws
                 "add_listeners" -/add-listeners-ws
                 "start_heartbeat" -/start-heartbeat-ws
                 "stop_heartbeat" -/stop-heartbeat-ws})
  (return client))
