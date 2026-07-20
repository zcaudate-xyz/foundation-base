(ns xt.ui.react-playground-test
  "Browser-playground test for the xt.ui React renderer POC.

   Starts a js-playground runtime, emits the React app into the browser, and
   verifies that the playground is reachable and the emitted script contains
   the expected UI descriptors and React renderer. Manual verification:
   open the printed URL in a browser and interact with the list."
  (:use code.test)
  (:require [hara.lang :as l]
            [hara.runtime.js-playground :as playground]
            [std.lib.component :as component]
            [xt.ui.react-playground-ui]))

(defn- url-reachable?
  "returns true if the given URL returns HTTP 200 within timeout-ms"
  [url timeout-ms]
  (try
    (let [conn (.openConnection (java.net.URL. url))]
      (.setConnectTimeout conn timeout-ms)
      (.setReadTimeout conn timeout-ms)
      (= 200 (.getResponseCode conn)))
    (catch Exception _
      false)))

^{:refer hara.runtime.js-playground/play-script :added "4.1"}
(fact "emits the xt.ui React POC script and serves it in a playground"
  (let [rt (playground/rt-js-playground {:lang :js :port 0})]
    (try
      (let [script (playground/play-script rt '[(xt.ui.react-playground-ui/mount!)] true)
            url (playground/play-url rt)]
        (println "xt.ui React Playground URL:" url)
        script => #"xt.ui React Playground"
        script => #"render_ui_node"
        script => #"react_registry"
        script => #"setStage"
        (url-reachable? url 3000) => true)
      (finally
        (component/stop rt)))))
