(ns demo-xtdb-backbone.app.main
  (:require [hara.lang :as l]))

(l/script :js
  {:import [["https://esm.sh/react@18.3.1" :as React]
            ["https://esm.sh/react-dom@18.3.1/client" :as ReactDOM]]
   :require [[demo-xtdb-backbone.app.remote :as remote]
             [xt.lang.spec-promise :as promise]]
   :static {:export false}})

(defn.js error-output
  [error]
  (return
   {"status" "error"
    "message" (or (. error message)
                  (String error))}))

(defn.js pretty
  [value]
  (return (JSON.stringify value nil 2)))

(defn.js Output
  [#{value}]
  (return
   [:pre (-/pretty value)]))

(defn.js reset-busy
  [setter]
  (setter false)
  (return true))

(defn.js handle-promise
  [task set-output set-busy]
  (return
   (promise/x:promise-catch
    (promise/x:promise-then
     task
     (fn [result]
       (set-output result)
       (-/reset-busy set-busy)
       (return result)))
    (fn [error]
      (set-output (-/error-output error))
      (-/reset-busy set-busy)
      (return nil)))))

(defn.js boot-worker
  [set-output set-busy]
  (set-busy true)
  (set-output {"status" "running"
               "detail" "booting..."})
  (return
   (-/handle-promise
    (remote/bootstrap
     (new SharedWorker "./workers/demo-xtdb-backbone-worker.js")
     {})
    set-output
    set-busy)))

(defn.js run-ping
  [page-node set-output set-busy]
  (set-busy true)
  (set-output {"status" "running"})
  (return
   (-/handle-promise
    (remote/refresh-page-view page-node
                              "screen/main"
                              "ping"
                              "main")
    set-output
    set-busy)))

(defn.js run-append
  [event page-node message email password set-output set-busy]
  (. event (preventDefault))
  (set-busy true)
  (set-output {"status" "running"})
  (remote/set-view-input page-node
                         "screen/main"
                         "log_append"
                         "main"
                         [message email password])
  (return
   (-/handle-promise
    (remote/refresh-page-view page-node
                              "screen/main"
                              "log_append"
                              "main")
    set-output
    set-busy)))

(defn.js App
  []
  (var page-node
       (React.useMemo
        (fn []
          (var node (remote/create-page-node))
          (remote/install-demo-models node "screen/main")
          (return node))
        []))
  (var [worker-output set-worker-output]
       (React.useState {"status" "idle"
                        "detail" "not started"}))
  (var [worker-busy set-worker-busy]
       (React.useState false))
  (var [ping-output set-ping-output]
       (React.useState {"status" "idle"}))
  (var [ping-busy set-ping-busy]
       (React.useState false))
  (var [append-output set-append-output]
       (React.useState {"status" "idle"}))
  (var [append-busy set-append-busy]
       (React.useState false))
  (var [message set-message]
       (React.useState "hello from scratch_v0"))
  (var [email set-email]
       (React.useState "demo@greenways.local"))
  (var [password set-password]
       (React.useState "greenways-demo"))
  (return
   [:main {:className "shell"}
    [:section {:className "panel"}
     [:h1 "demo-xtdb-backbone"]
     [:p "React webapp + split sharedworker sample for scratch_v0 ping and log_append page-models."]
     [:button {:onClick (fn []
                          (-/boot-worker set-worker-output
                                         set-worker-busy))
               :disabled worker-busy}
      (:? worker-busy
          "Booting sharedworker..."
          "Boot sharedworker")]
     [:% -/Output {:value worker-output}]]
    [:section {:className "panel"}
     [:h2 "page-model: ping"]
     [:button {:onClick (fn []
                          (-/run-ping page-node
                                      set-ping-output
                                      set-ping-busy))
               :disabled ping-busy}
      (:? ping-busy
          "Running ping..."
          "Run ping")]
     [:% -/Output {:value ping-output}]]
    [:section {:className "panel"}
     [:h2 "page-model: log_append"]
     [:form {:onSubmit (fn [event]
                         (-/run-append event
                                       page-node
                                       message
                                       email
                                       password
                                       set-append-output
                                       set-append-busy))}
      [:label
       "Message"
       [:input {:value message
                :onChange (fn [event]
                            (set-message (. event target value)))}]]
      [:label
       "Email"
       [:input {:value email
                :onChange (fn [event]
                            (set-email (. event target value)))}]]
      [:label
       "Password"
       [:input {:type "password"
                :value password
                :onChange (fn [event]
                            (set-password (. event target value)))}]]
      [:button {:type "submit"
                :disabled append-busy}
       (:? append-busy
           "Appending log..."
           "Append log")]]
     [:% -/Output {:value append-output}]]]))

(defn.js mount
  []
  (var root-el (. document (getElementById "app")))
  (var root (. ReactDOM (createRoot root-el)))
  (. root (render [:% -/App]))
  (return root))

(defrun.js __init__
  (-/mount))
