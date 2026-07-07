(ns play.js-002-substrate-scratch-v3.app
  (:require [hara.lang :as l]))

(l/script :js
  {:import [["https://esm.sh/react@18.3.1" :as React]
            ["https://esm.sh/react-dom@18.3.1/client" :as ReactDOM]]
   :require [[play.js-002-substrate-scratch-v3.main :as demo]
             [xt.lang.spec-base :as xt]
             [xt.lang.common-data :as data]]
   :static {:export false}})

(defn.js pretty
  [value]
  (return (JSON.stringify value nil 2)))

(defn.js Output
  [#{value}]
  (return [:pre (-/pretty value)]))

(defn.js demo-by-id
  [demo-id]
  (return
   (data/arr-find demo/DEMOS
                  (fn [entry]
                    (return (== (x:get-key entry "id") demo-id))))))

(defn.js DemoCard
  [#{entry active on-select}]
  (var demo-id (x:get-key entry "id"))
  (return
   [:button {:className (+ "demo-card " (:? active "active" ""))
             :onClick (fn [] (on-select demo-id))}
    [:span {:className "eyebrow"} (x:get-key entry "space_id")]
    [:strong (x:get-key entry "title")]
    [:small (x:get-key entry "description")]
    [:code (x:get-key entry "model_id")]]))

(defn.js App
  []
  (var [demo-id set-demo-id] (React.useState "currencies"))
  (var [user-id set-user-id] (React.useState demo/DEFAULT_USER_ID))
  (var selected (-/demo-by-id demo-id))
  (var model (demo/demo-model demo-id user-id))
  (return
   [:main {:className "shell"}
    [:section {:className "hero"}
     [:p {:className "eyebrow"} "FOUNDATION / XT.SUBSTRATE / SCRATCH_V3"]
     [:h1 "Database-backed substrate slices"]
     [:p "The schema bindings and model descriptors are emitted from Hara DSL and derive from postgres.sample.scratch-v3."]]
    [:section {:className "demo-grid"}
     (data/arr-map demo/DEMOS
                   (fn [entry]
                     (return
                      [:% -/DemoCard
                       {:key (x:get-key entry "id")
                        :entry entry
                        :active (== demo-id (x:get-key entry "id"))
                        :on-select set-demo-id}])))]
    [:section {:className "panel controls"}
     [:h2 (x:get-key selected "title")]
     [:p (x:get-key selected "description")]
     [:label "Scratch user id"
      [:input {:value user-id
               :onChange (fn [event] (set-user-id (. event target value)))}]]]
    [:section {:className "columns"}
     [:article {:className "panel"}
      [:h2 "Generated dataview"]
      [:% -/Output {:value model}]]
     [:article {:className "panel"}
      [:h2 "Substrate invocation"]
      [:% -/Output
       {:value {"connect" "connect(config)"
                "attach" "attach-demo(client, demo-id, user-id)"
                "source_id" (x:get-key selected "source_id")
                "space_id" (x:get-key selected "space_id")
                "group_id" (x:get-key selected "group_id")
                "model_id" (x:get-key selected "model_id")
                "event" (demo/example-event demo-id)}}]]]]))

(defn.js mount
  []
  (var root-el (. document (getElementById "app")))
  (var root (. ReactDOM (createRoot root-el)))
  (. root (render [:% -/App]))
  (return root))

(defrun.js __init__
  (-/mount))
