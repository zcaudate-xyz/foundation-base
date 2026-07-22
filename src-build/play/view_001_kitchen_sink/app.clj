(ns play.view-001-kitchen-sink.app
  "Browser app mounting the xt.substrate.view kitchen-sink demo.

   One shared substrate node drives both demo screens; the tab switch only
   swaps the spec/render-fn pair passed to js.react.view/View."
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [xt.substrate :as substrate]
             [xt.substrate.view :as view]
             [xt.substrate.view-demos :as demos]
             [js.react :as r]
             [js.react.view :as react-view]]
   :static {:export false}})

(defn.js TabButton
  [#{label active on-press}]
  (return
   [:button {:className (+ "rounded border px-3 py-1 text-sm "
                           (:? active
                               "border-slate-900 bg-slate-900 text-white"
                               "border-slate-300 bg-white text-slate-700"))
            :onClick (fn [_] (on-press))}
    label]))

(defn.js App
  []
  (var local-state (r/local "sink"))
  (var tab (xt/x:get-idx local-state 0))
  (var setTab (xt/x:get-idx local-state 1))
  (var node-ref (r/ref nil))
  (when (xt/x:nil? (r/curr node-ref))
    (r/curr:set node-ref (substrate/node-create {}))
    (demos/install-handlers (r/curr node-ref)))
  (var node (r/curr node-ref))
  (var spec (:? (== tab "sink")
                (demos/kitchen-sink-spec)
                (demos/web-escape-spec)))
  (var render-fn (:? (== tab "sink")
                     demos/kitchen-sink-render
                     demos/web-escape-render))
  (return
   [:div {:className "min-h-screen bg-slate-50 text-slate-900"}
    [:div {:className "flex flex-row gap-2 border-b border-slate-200 bg-white p-4"}
     [:% -/TabButton {:label "Kitchen sink"
                      :active (== tab "sink")
                      :on-press (fn [] (setTab "sink"))}]
     [:% -/TabButton {:label "Web escape"
                      :active (== tab "escape")
                      :on-press (fn [] (setTab "escape"))}]]
    [:% react-view/View {:key tab
                         :node node
                         :spec spec
                         :render-fn render-fn
                         :options {"space_id" "app"}}]]))

(defn.js mount
  []
  (var root-el (. document (getElementById "app")))
  (var root (r/createDOMRoot root-el))
  (. root (render [:% -/App]))
  (return root))

(defrun.js __init__
  (-/mount))
