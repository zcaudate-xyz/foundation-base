(ns js.cell-v2.impl-common-test
  (:use code.test)
  (:require [std.lang :as l]
            [js.cell-v2.event :as event]
            [js.cell-v2.impl-common :as impl-common]
            [xt.lang.base-lib :as k]
            [xt.lang.base-notify :as notify]
            [xt.lang.base-repl :as repl]
            [js.core :as j]))

(l/script- :js
  {:runtime :basic
   :config {:id :test/js}
   :require [[js.cell-v2.event :as event]
             [js.cell-v2.impl-common :as impl-common]
             [xt.lang.base-lib :as k]
             [xt.lang.base-notify :as notify]
             [xt.lang.base-repl :as repl]
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

(fact "new-cell uses an event-common container and resolves init from the link callback"
  ^:hidden

  (notify/wait-on :js
    (var link (-/stub-link))
    (var cell (impl-common/new-cell link))
    (var handler (. link ["callbacks"] [event/EV_INIT] ["handler"]))
    (handler {} event/EV_INIT link)
    (. (. cell ["init"]) (then (repl/>notify))))
  => true)

(fact "listener helpers key by model/view path"
  ^:hidden

  (notify/wait-on :js
    (var cell (impl-common/new-cell (-/stub-link)))
    (impl-common/add-listener
     cell
     ["hello" "echo"]
     "@react/1234"
     (repl/>notify))
    (impl-common/trigger-listeners
     cell
     ["hello" "echo"]
     {:type "view.output"
      :data {:current ["HELLO"]}}))
  => (contains-in {"path" ["hello" "echo"]
                   "type" "view.output"
                   "meta" {"listener/id" "@react/1234"
                           "listener/type" "cell"}})

  (!.js
   (var cell (impl-common/new-cell (-/stub-link)))
   (impl-common/add-listener cell ["hello" "echo"] "@react/1234" (fn:>))
   [(impl-common/list-listeners cell ["hello" "echo"])
    (impl-common/list-all-listeners cell)])
  => [["@react/1234"]
      {"hello" {"echo" ["@react/1234"]}}])
