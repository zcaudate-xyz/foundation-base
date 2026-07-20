(ns js.react.view
  "React entrypoint for xt.substrate.view."
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[js.react.view.runtime :as runtime]
             [js.react.view.backend :as backend]
             [js.react.view.polyfill :as polyfill]]})

(def.js runtime-create runtime/runtime-create)
(def.js snapshot runtime/snapshot)
(def.js local-set runtime/local-set)
(def.js render runtime/render)
(def.js render-node runtime/render-node)
(def.js View runtime/View)
(def.js native-registry backend/native-registry)
(def.js polyfill-registry polyfill/registry)
