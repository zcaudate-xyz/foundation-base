(ns js.react.ext-page-core-test
  (:require [lang.core :as l]
            [js.react.helper-jsdom :as helper-source])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.spec-base :as xt]
             [xt.event.base-model :as event-model]
             [js.react :as r]
             [lang-demos.js-007-ext-page.generated.core :as app-core]
             [lang-demos.js-007-ext-page.generated.runtime :as app-runtime]]})

(fact:global
 {:setup [(l/rt:restart :js)
          (l/rt:scaffold-imports :js)]
  :teardown [(l/rt:stop)]})

(defn.js LogCoreHarness
  [props]
  (var state (app-core/useLogState props))
  (var rows (. state rows))
  (xt/x:set-key (. props ["state"]) "core" state)
  (return
   [:main
    [:input {:id "message" :value (. state message)}]
    [:p {:id "notice"} (. state notice)]
    [:span {:id "count"} rows.length]
    [:div
     (xt/x:arr-map
      rows
      (fn [entry]
        (return [:article {:className "log-row"}
                 (. entry message)])))]]))

^{:refer lang-demos.js-007-ext-page.generated.runtime/table-impl
  :added "4.1"}
(fact "configures Log id and row reads as dataview queries"
  (!.js
    (var impl (app-runtime/table-impl))
    (var list-spec (xt/x:get-path impl ["base" "list" "spec"]))
    (var data-spec (xt/x:get-path impl ["base" "data" "spec"]))
    (return
     {"list-table" (xt/x:get-key list-spec "table")
      "list-type" (xt/x:get-path list-spec ["select_entry" "view" "type"])
      "list-object" (xt/x:is-object? list-spec)
      "data-table" (xt/x:get-key data-spec "table")
      "data-type" (xt/x:get-path data-spec ["return_entry" "view" "type"])
      "data-object" (xt/x:is-object? data-spec)}))
  => {"list-table" "Log"
      "list-type" "select"
      "list-object" true
      "data-table" "Log"
      "data-type" "return"
      "data-object" true})

^{:refer lang-demos.js-007-ext-page.generated.core/useLogState :added "4.1"}
(fact "appends through an injected action and renders the refreshed model in JSDOM"
  (helper-source/test
   (fn [props]
     (return (-/LogCoreHarness props)))
   (fn [_]
     (var entries [])
     (var calls [])
     (var view (event-model/create-model
                (fn [_] (return entries))
                {}
                []
                []
                nil
                nil))
     (event-model/init-model view)
     (return
      {"view" view
       "state" {}
       "calls" calls
       "append" (fn [message]
                  (var entry {"id" "memory-1" "message" message})
                  (. entries (push entry))
                  (. calls (push message))
                  (return (Promise.resolve entry)))}))
   (fn [props document _]
     (var initial-core (. props ["state"] core))
     (r/act
      (fn []
        ((. initial-core setMessage) "JSDOM shared log entry")
        nil))
     (return
      (new Promise
       (fn [resolve]
         (setTimeout
          (fn []
            (var current-core (. props ["state"] core))
            (r/act
             (fn []
               ((. current-core appendEntry))
               nil))
            (setTimeout
             (fn []
               (var row (document.querySelector ".log-row"))
               (var input (document.querySelector "#message"))
               (resolve
                {"entry" (:? row (. row textContent) "")
                 "count" (. (document.querySelector "#count") textContent)
                 "notice" (. (document.querySelector "#notice") textContent)
                 "input" (. input value)
                 "message" (. props ["state"] ["core"] ["message"])
                 "calls" (. props calls)
                 "model" (event-model/get-current (. props view) nil)}))
             30))
          0))))))
  => {"entry" "JSDOM shared log entry"
      "count" "1"
      "notice" "Entry added to the shared log."
      "input" ""
      "message" ""
      "calls" ["JSDOM shared log entry"]
      "model" [{"id" "memory-1" "message" "JSDOM shared log entry"}]})
