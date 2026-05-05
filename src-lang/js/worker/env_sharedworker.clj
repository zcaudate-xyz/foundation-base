(ns js.worker.env-sharedworker
  "SharedWorker runtime script entrypoint for js.worker."
  (:require [hara.lang :as l]))

(l/script :js
  {:require []})

(defn.js init-port
  "boots a SharedWorker port"
  [port]
  (. port (addEventListener
           "message"
           (fn [e]
             (return e))
           false))
  (. port (postMessage {:signal "@cell/::INIT"
                        :body {:done true}}))
  (return port))

(defn.js runtime-init
  "boots js.worker inside a SharedWorker"
  []
  (:= (. (!:G self) ["onconnect"])
      (fn [e]
        (var port (. e ["ports"] [0]))
        (. port (start))
        (-/init-port port)
        (return port)))
  (return true))
