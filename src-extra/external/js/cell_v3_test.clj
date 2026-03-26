(ns js.cell-v3-test
  (:use code.test)
  (:require [std.lang :as l]
            [js.cell-v3 :as cl]
            [js.cell-v3.kernel.protocol :as protocol]
            [js.cell-v3.kernel.base-fn :as base-fn]
            [xt.lang.base-lib :as k]
            [xt.lang.base-notify :as notify]
            [xt.lang.base-repl :as repl]
            [js.core :as j]))

(l/script- :js
  {:runtime :basic
   :config {:id :test/js}
   :require [[js.cell-v3 :as cl]
             [js.cell-v3.kernel.protocol :as protocol]
             [js.cell-v3.kernel.base-fn :as base-fn]
             [xt.lang.base-lib :as k]
             [xt.lang.base-notify :as notify]
             [xt.lang.base-repl :as repl]
             [js.core :as j]]})

(defn.js stub-link
  []
  (return {"::" "cell-v3.link"
           :id "link.stub"
           :transport {:pending {}
                        :subscriptions {}
                       :counter 0}
           :supportsSubscribe false
           :callbacks {}
           :signalCounts {}}))

(fact "cell-v3 exposes the runtime kernel with the new layering"
  ^:hidden

  (!.js
   (protocol/call "c-1" "@/ping" ["arg"] {:source "test"}))
  => {"op" "call"
      "id" "c-1"
      "action" "@/ping"
      "body" ["arg"]
      "meta" {"source" "test"}}

  (!.js
   (base-fn/fn-ping))
  => (contains-in ["pong" integer?])

  (!.js
   (var cell (cl/make-cell (-/stub-link)))
   (. cell ["::"]))
  => "cell-v3.cell"

  (notify/wait-on :js
    (var cell (cl/make-cell (-/stub-link)))
    (var model
         (cl/add-model
          "hello"
          {:echo {:handler (fn [link input]
                             (return [input "ok"]))
                  :defaultArgs ["HELLO"]}}
          cell))
    (. (. model ["init"])
       (then (fn []
               (. (k/first
                   (cl/view-set-input
                    ["hello" "echo"]
                    {:data ["UPDATED"]}
                    cell))
                  (then (repl/>notify)))))))
  => (contains-in
      {"::" "view.run"
       "path" ["hello" "echo"]
       "main" [true ["UPDATED" "ok"]]}))
