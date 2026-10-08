(ns js.react.ext-log-test
    (:require [lang.core :as l]
              [js.react.helper-jsdom :as helper-source])
    (:use code.test))

(l/script- :js
           {:runtime :basic
            :require [[xt.lang.spec-base :as xt]
                      [js.react :as r]
                      [js.react.helper-jsdom :as helper]
                      [js.react.ext-log :as ext-log]
                      [xt.event.base-log :as event-log]]})

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

^{:refer js.react.ext-log/makeLog :added "4.0"}
(fact "creates a React log"
  (helper-source/wait-on
   {}
   (var controls {})
   (var Component
        (fn []
            (var log (ext-log/makeLog {"maximum" 2}))
            (xt/x:set-key controls "log" log)
            (return (r/createElement "span" nil "ready"))))
   (return
    (. (helper/render env Component {})
       (then (fn [_]
                 (var log (. controls ["log"]))
                 (event-log/queue-entry log {"id" "a"}
                                        (fn [entry t] (. entry ["id"]))
                                        (fn [entry] entry)
                                        nil)
                 (return {"type" (. log ["::"])
                          "count" (event-log/get-count log)}))))))
  => {"type" "event.log" "count" 1})

^{:refer js.react.ext-log/listenLogLatest :added "4.0"}
(fact "updates with the latest log entry"
  (helper-source/wait-on
   {}
   (var controls {})
   (var Component
        (fn []
            (var log (ext-log/makeLog {}))
            (var latest (ext-log/listenLogLatest log nil))
            (xt/x:set-key controls "log" log)
            (xt/x:set-key controls "latest" latest)
            (return (r/createElement "span" nil "ready"))))
   (return
    (. (helper/render env Component {})
       (then (fn [_]
                 (var log (. controls ["log"]))
                 (event-log/queue-entry log {"id" "a" "value" 1}
                                        (fn [entry t] (. entry ["id"]))
                                        (fn [entry] entry)
                                        nil)
                 (return
                  (new Promise
                       (fn [resolve]
                           (setTimeout
                            (fn []
                                (resolve {"id" (. (. controls ["latest"]) ["id"])
                                          "data" (. (. controls ["latest"]) ["data"])}))
                            0)))))))))
  => {"id" "a" "data" {"id" "a" "value" 1}})
