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
  (helper-source/test
   (fn [props]
     (var log (ext-log/makeLog {"maximum" 2}))
     (xt/x:set-key (. props ["state"]) "log" log)
     (return (r/createElement "span" nil "ready")))
   (fn [_]
     (return {"state" {}}))
   (fn [props document _]
     (var log (. (. props ["state"]) ["log"]))
     (event-log/queue-entry log {"id" "a"}
                            nil
                            (fn [entry] entry)
                            nil)
     (return
      (new Promise
           (fn [resolve]
             (setTimeout
              (fn []
                (var result {"type" (. log ["::"])
                             "count" (event-log/get-count log)})
                (resolve result))
              0))))))
  => {"type" "event.log" "count" 1})

^{:refer js.react.ext-log/listenLogLatest :added "4.0"}
(fact "updates with the latest log entry"
  (helper-source/test
   (fn [props]
     (var log (ext-log/makeLog {}))
     (xt/x:set-key (. props ["state"]) "log" log)
     (var latest (ext-log/listenLogLatest log nil))
     (return (r/createElement "span" nil (JSON.stringify (or latest {})))))
   (fn [_]
     (return {"state" {}}))
   (fn [props document _]
     (var log (. (. props ["state"]) ["log"]))
     (return
      (. (Promise.resolve
          (r/act
           (fn []
             (event-log/queue-entry log {"id" "a" "value" 1}
                                    nil
                                    (fn [entry] entry)
                                    nil))))
         (then (fn [_]
                 (return
                  (helper/await-dom
                   (fn []
                     (var latest (JSON.parse document.body.textContent))
                     (var result {"id" (xt/x:is-string? (. latest ["id"]))
                                  "count" (event-log/get-count log)})
                     (return result))))))))))
  => {"id" true "count" 1})
