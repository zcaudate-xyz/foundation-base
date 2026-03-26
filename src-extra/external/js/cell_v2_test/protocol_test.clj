(ns js.cell-v2.protocol-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require [[js.cell-v2.protocol :as protocol]]})

(fact:global
 {:setup     [(l/rt:restart)]
  :teardown  [(l/rt:stop)]})

^{:refer js.cell-v2.protocol/frame :added "4.0" :unchecked true}
(fact "constructs protocol frames"
  (protocol/hello "h1" {:events true} nil)
  => {"op" "hello"
      "id" "h1"
      "body" {"capabilities" {"events" true}}
      "meta" {}}

  (protocol/result "c1" "ok" {:data true} {:source "test"} "r1")
  => {"op" "result"
      "id" "c1"
      "status" "ok"
      "ref" "r1"
      "body" {"data" true}
      "meta" {"source" "test"}}

  (protocol/emit "e1" "cell/::REMOTE" "ok" {:id "x"} nil "c1")
  => {"op" "emit"
      "id" "e1"
      "signal" "cell/::REMOTE"
      "status" "ok"
      "ref" "c1"
      "body" {"id" "x"}
      "meta" {}}

  (protocol/task "u1" "task-1" "pending" {:progress 0.5} nil)
  => {"op" "task"
      "id" "u1"
      "ref" "task-1"
      "status" "pending"
      "body" {"progress" 0.5}
      "meta" {}})
