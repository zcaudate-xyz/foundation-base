(ns lang.runtime.basic.type-remote-ws
  (:require [net.http.websocket :as ws]
            [std.concurrent :as cc]
            [std.json :as json]
            [lang.core.pointer :as ptr]
            [lang.core.runtime :as default]
            [std.protocol.context :as protocol.context]))
