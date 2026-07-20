(ns dart.ui.view
  "Dart/Wind entrypoint for xt.substrate.view."
  (:require [hara.lang :as l]))

(l/script :dart
  {:require [[dart.ui.view.runtime :as runtime]
             [dart.ui.view.backend :as backend]
             [dart.ui.view.polyfill :as polyfill]]})

(def.dt runtime-create runtime/runtime-create)
(def.dt snapshot runtime/snapshot)
(def.dt local-set runtime/local-set)
(def.dt prepare runtime/prepare)
(def.dt open runtime/open)
(def.dt close runtime/close)
(def.dt native-registry backend/native-registry)
(def.dt polyfill-registry polyfill/registry)
