(ns rt.basic.type-remote-ws
  (:require [net.http.websocket :as ws]
            [std.concurrent :as cc]
            [std.json :as json]
            [std.lang.base.pointer :as ptr]
            [std.lang.base.runtime :as default]
            [std.protocol.context :as protocol.context]))
