(ns hara.runtime.basic.type-remote-ws
  (:require [net.http.websocket :as ws]
            [std.concurrent :as cc]
            [std.json :as json]
            [hara.lang.base.pointer :as ptr]
            [hara.lang.base.runtime :as default]
            [std.protocol.context :as protocol.context]))
