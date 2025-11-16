(ns lua.nginx.ws-client
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :lua
  {:import [["resty.websocket.client" :as ngxwsclient]
            ["resty.websocket.client" :as ngxwsclient]]})

(defmacro.lua new
  "creates a new ws client"
  {:added "4.0"}
  []
  '(. ngxwsclient (new)))

(h/template-entries [l/tmpl-macro {:base "websocket"
                                   :inst "wb"
                                   :tag "lua"}]
  [[connect     [url] {:optional [options]}]
   [close       []]
   [set-keepalive  [max-idle-timeout, pool-size]]
   [set-timeout    [ms]]
   [send_text      [text]]
   [send_binary    [data]]
   [send_ping   [] {:optional [msg]}]
   [send_pong   [] {:optional [msg]}]
   [send_close  [] {:optional [code msg]}]
   [send_frame  [fin opcode payload]]
   [recv_frame  []]])
