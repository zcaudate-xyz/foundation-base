(ns js.cell-v2.impl-model-test
  (:use code.test)
  (:require [std.lang :as l]
            [js.cell-v2.impl-common :as impl-common]
            [js.cell-v2.impl-model :as impl-model]
            [xt.lang.base-lib :as k]
            [xt.lang.base-notify :as notify]
            [xt.lang.base-repl :as repl]
            [xt.lang.event-view :as event-view]
            [js.core :as j]))

(l/script- :js
  {:runtime :basic
   :config {:id :test/js}
   :require [[js.cell-v2.impl-common :as impl-common]
             [js.cell-v2.impl-model :as impl-model]
             [xt.lang.base-lib :as k]
             [xt.lang.base-notify :as notify]
             [xt.lang.base-repl :as repl]
             [xt.lang.event-view :as event-view]
             [js.core :as j]]})

(defn.js stub-link
  []
  (return {"::" "cell-v2.link"
           :id "link.stub"
           :transport {:pending {}
                       :subscriptions {}
                       :counter 0}
           :supportsSubscribe false
           :callbacks {}
           :signalCounts {}}))

(fact "create-view reuses event-view shape"
  ^:hidden

  (!.js
   (impl-model/create-view
    (impl-common/new-cell (-/stub-link))
    "hello"
    "ping"
    {:handler (fn [link input]
                (return input))
     :defaultArgs ["HELLO"]}))
  => (contains-in
      {"::" "event.view"
       "input" {"current" {"data" ["HELLO"]} "updated" integer?}
       "listeners" {"@/cell"
                    {"meta" {"listener/id" "@/cell"
                             "listener/type" "view"}}}}))

(fact "add-model, view-set-input, and trigger-all preserve impl-model behavior"
  ^:hidden

  (j/<!
   (. (impl-model/add-model
       (impl-common/new-cell (-/stub-link))
       "hello"
       {:echo {:handler (fn [link input]
                          (return [input "ok"]))
               :defaultArgs ["HELLO"]}
        :echo1 {:handler (fn [link]
                           (return "secondary"))
                :deps ["echo"]
                :trigger "topic/refresh"}})
      ["init"]))
  => (contains-in
      [{"::" "view.run"
        "path" ["hello" "echo"]
         "main" [true ["HELLO" "ok"]]
        "pre" [false]
        "post" [false]}
       {"::" "view.run"
        "path" ["hello" "echo1"]
        "main" [false]}])

  (notify/wait-on :js
    (var cell (impl-common/new-cell (-/stub-link)))
    (var runtime-model
         (impl-model/add-model
          cell
          "hello"
          {:echo {:handler (fn [link input]
                             (return [input "ok"]))
                  :defaultArgs ["HELLO"]}
           :echo1 {:handler (fn [link]
                              (return "secondary"))
                   :deps ["echo"]
                   :trigger "topic/refresh"}}))
    (. (. runtime-model ["init"])
       (then (fn []
               (. (k/first
                   (impl-model/view-set-input
                    cell
                    "hello"
                    "echo"
                    {:data ["UPDATED"]}))
                  (then (repl/>notify)))))))
  => (contains-in
      {"::" "view.run"
       "path" ["hello" "echo"]
       "main" [true ["UPDATED" "ok"]]
       "pre" [false]
       "post" [false]})

  (!.js
   (var cell (impl-common/new-cell (-/stub-link)))
   (impl-model/add-model-attach
    cell
    "hello"
    {:echo {:handler (fn [link input]
                       (return [input "ok"]))
            :defaultArgs ["HELLO"]}
     :echo1 {:handler (fn [link]
                        (return "secondary"))
              :deps ["echo"]
             :trigger "topic/refresh"}})
   (impl-model/trigger-all cell "topic/refresh" {}))
  => {"hello" ["echo" "echo1"]})
