(ns code.dev.client.page-demo
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :js
  {:require [[js.react :as r :include [:dom]]]})

(defn.js Greeter
  [#{name}]
  (return
   [:h1 (+ "Hello " name " This is JSX.")]))

(defn.js Counter
  []
  (var [count setCount] (r/useState 0))
  (return
   [:div
    [:p "You clicked " count " times"]
    [:button
     {:onClick (fn [] (setCount (+ count 1)))}
     "Click me"]]))

(defn.js App
  []
  (return
   [:div
    [:% -/Greeter]
    [:% -/Counter]]))

(defn.js main
  []
  (var rootElement (document.getElementById "root"))
  (var root (ReactDOM.createRoot rootElement))
  (root.render [:% -/App]))

