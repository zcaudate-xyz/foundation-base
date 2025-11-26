(ns indigo.frontend.main
  (:require [std.lang :as l]))

(l/script :js
  (l/module "indigo.frontend.main")
  (defonce.js event-source nil)

  (defn.js log-message [message]
    (let [log-div (js/document.getElementById "log")]
      (set! (.-innerHTML log-div)
            (+ (.-innerHTML log-div)
               "<pre>" (js/JSON.stringify message) "</pre>"))))

  (defn.js connect []
    (set! event-source (js/EventSource. "/events"))
    (set! (.-onmessage event-source)
          (fn [event]
            (let [data (js/JSON.parse (.-data event))]
              (log-message data))))
    (set! (.-onerror event-source)
          (fn [error]
            (log-message "SSE Error")
            (.-close event-source))))

  (defn.js disconnect []
    (when event-source
      (.-close event-source)
      (set! event-source nil)))

  (defn.js -main []
    (connect)))
