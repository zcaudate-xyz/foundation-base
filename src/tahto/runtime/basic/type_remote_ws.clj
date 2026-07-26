(ns tahto.runtime.basic.type-remote-ws
  (:require [net.http.websocket :as ws]
            [std.concurrent :as cc]
            [std.json :as json]
            [tahto.core.pointer :as ptr]
            [tahto.core.runtime :as default]
            [std.protocol.context :as protocol.context]))
