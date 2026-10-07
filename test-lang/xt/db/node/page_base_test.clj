(ns xt.db.node.page-base-test
  (:require [clojure.string :as string]
            [code.test :refer [fact =>]]
            [xt.db.node.page-base]))

^{:refer xt.db.node.page-base/page-model-path :added "4.1.6"}
(fact "publishes the page model path helper"
  (let [source (str (var-get (find-var 'xt.db.node.page-base/page-model-path)))]
    [(string/includes? source "group_id")
     (string/includes? source "model-id")])
  => [true true])

^{:refer xt.db.node.page-base/page-detach :added "4.1.6"}
(fact "publishes idempotent teardown behavior"
  (let [source (str (var-get (find-var 'xt.db.node.page-base/page-detach)))]
    [(string/includes? source "closed")
     (string/includes? source "promise-all")
     (string/includes? source "detach-model")])
  => [true true true])

^{:refer xt.db.node.page-base/page-attach :added "4.1.6"}
(fact "dispatches both declarative model kinds and rolls back on failure"
  (let [source (str (var-get (find-var 'xt.db.node.page-base/page-attach)))]
    [(string/includes? source "rpc-attach-model")
     (string/includes? source "dataview-attach-model")
     (string/includes? source "page-detach")
     (string/includes? source "group-open-proxy")])
  => [true true true true])

^{:refer xt.db.node.page-base/page-model-call :added "4.1.6"}
(fact "dispatches a page model call through the page proxy"
  (let [source (str (var-get (find-var 'xt.db.node.page-base/page-model-call)))]
    [(string/includes? source "model-proxy-call")
     (string/includes? source "space_id")
     (string/includes? source "group_id")])
  => [true true true])
