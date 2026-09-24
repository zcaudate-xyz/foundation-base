(ns lang-demos.js-004-xtdb-backbone.app.sharedworker
  (:require [hara.lang :as l]))

(l/script :js
  {:runtime :websocket
   :config {:id :lang-demos.js-004-xtdb-backbone/sharedworker
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}}
   :require [[lang-demos.js-004-xtdb-backbone.app.backbone :as backbone]
            [lang-demos.js-004-xtdb-backbone.app.worker-base :as worker-base]]
   :import [["./custom.js" :as custom]]
   :file "main.js"})

(defn.js default-config
  []
  (return (backbone/sharedworker-config)))

(defn.js custom-config
  []
  (return (worker-base/custom-config custom)))

(defn.js worker-config
  []
  (return
   (worker-base/worker-config
    (-/default-config)
    (-/custom-config))))

(defn.js runtime-init
  []
  (return
   (worker-base/runtime-init
    (-/worker-config))))
