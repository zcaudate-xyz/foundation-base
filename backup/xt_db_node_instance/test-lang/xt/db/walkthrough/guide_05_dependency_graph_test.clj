(ns xt.db.walkthrough.guide-05-dependency-graph-test
  (:require [hara.lang :as l]
            [xt.lang.common-notify :as notify])
  (:use code.test))

^{:seedgen/root {:all true}}
(l/script- :js
  {:runtime :basic
   :require [[xt.db.node.instance-model :as model]
             [xt.db.helpers.test-fixtures :as fixtures]
             [xt.substrate :as event-node]
             [xt.lang.common-data :as xtd]
             [xt.lang.common-repl :as repl]
             [xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as promise]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.db.walkthrough.guide-05-dependency-graph/STEP.00-dependency-index :added "4.1"}
(fact "step 00: an admin screen can record how its detail and filtered list views are linked"

  (!.js
    (var node (event-node/node-create {"id" "node-a"}))
    (model/install node fixtures/InstallOpts)
    (model/model-put node "room/a" "orders" fixtures/DependentModelSpec)
    {"detail-dependents" (model/view-dependents node "room/a" "orders" "main")
     "list-filter" (model/view-input node "room/a" "orders" "open")})
  => {"detail-dependents" {"orders" ["open"]}
      "list-filter" ["open"]})

^{:refer xt.db.walkthrough.guide-05-dependency-graph/STEP.01-dependent-refresh :added "4.1"}
(fact "step 01: refreshing the selected detail view can also refresh the admin list state that depends on it"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-a"}))
    (model/install node fixtures/InstallOpts)
    (model/model-put node "room/a" "orders" fixtures/DependentModelSpec)
    (-> (model/sync node "room/a" {"db/sync" fixtures/Seed})
        (promise/x:promise-then
         (fn [_]
           (return
            (model/view-refresh node "room/a" "orders" "main"))))
        (promise/x:promise-then
         (fn [_]
           (repl/notify
            {"detail-status" (xtd/get-in (model/view-get node "room/a" "orders" "main")
                                         ["status"])
             "list-status" (xtd/get-in (model/view-get node "room/a" "orders" "open")
                                       ["status"])
             "list-query-key?" (xt/x:is-string?
                                (xtd/get-in (model/view-get node "room/a" "orders" "open")
                                            ["query_key"]))
             "list-count" (xt/x:len (or (model/view-val node "room/a" "orders" "open")
                                        []))})))))
  => {"detail-status" "ready"
      "list-status" "ready"
      "list-query-key?" true
      "list-count" 1})

^{:refer xt.db.walkthrough.guide-05-dependency-graph/STEP.02-organisation-topics :added "4.1"}
(fact "step 02: a Topics list can depend on the selected Organisation name even though the organisation itself is selected by id"

  (!.js
    (var node (event-node/node-create {"id" "node-b"}))
    (model/install node fixtures/InstallOpts)
    (model/model-put
     node
     "room/a"
     "organisations"
     {"views"
      {"selected"
       {"default_input" ["00000000-0000-0000-0000-0000000000b1"]
        "value" [{"id" "00000000-0000-0000-0000-0000000000b1"
                  "name" "Greenways Foundation"}]}}})
    (model/model-put
     node
     "room/a"
     "topics"
     {"views"
      {"by-organisation-name"
       {"default_input" ["Greenways Foundation"]
        "deps" [["organisations" "selected"]]}}})
    {"organisation-model-dependents" (model/model-dependents node "room/a" "organisations")
     "organisation-view-dependents" (model/view-dependents node "room/a" "organisations" "selected")
     "organisation-selection-id" (model/view-input node "room/a" "organisations" "selected")
     "selected-organisation-name" (xtd/get-in
                                   (model/view-val node "room/a" "organisations" "selected")
                                   [0 "name"])
     "topics-filter-name" (model/view-input node "room/a" "topics" "by-organisation-name")})
  => {"organisation-model-dependents" {"topics" true}
      "organisation-view-dependents" {"topics" ["by-organisation-name"]}
      "organisation-selection-id" ["00000000-0000-0000-0000-0000000000b1"]
      "selected-organisation-name" "Greenways Foundation"
      "topics-filter-name" ["Greenways Foundation"]})

^{:refer xt.db.walkthrough.guide-05-dependency-graph/STEP.03-organisation-topics-pull :added "4.1"}
(fact "step 03: pull the selected Organisation by id, then feed its name into the dependent Topics list and refresh that list"

  (notify/wait-on :js
    (var node (event-node/node-create {"id" "node-c"}))
    (var organisation-id "00000000-0000-0000-0000-0000000000b1")
    (var seed {"Task"
               [{"id" "00000000-0000-0000-0000-0000000000b1"
                 "status" "organisation"
                 "name" "Greenways Foundation"}
                {"id" "00000000-0000-0000-0000-0000000000b2"
                 "status" "organisation"
                 "name" "Blue River Co"}
                {"id" "00000000-0000-0000-0000-0000000000c1"
                 "status" "Greenways Foundation"
                 "name" "Topic Alpha"}
                {"id" "00000000-0000-0000-0000-0000000000c2"
                 "status" "Greenways Foundation"
                 "name" "Topic Beta"}
                {"id" "00000000-0000-0000-0000-0000000000c3"
                 "status" "Blue River Co"
                 "name" "Other Topic"}]})
    (var organisation-query
         {:table "Task"
          :select-entry {"input" [{"symbol" "i_task_id" "type" "uuid"}]
                         "view" {"query" {"id" "{{i_task_id}}"}}}
          :return-entry {"input" [{"symbol" "i_task_id" "type" "uuid"}]
                         "return" "jsonb"
                         "view" {"table" "Task"
                                 "type" "return"
                                 "tag" "selected-organisation"
                                 "access" {"roles" {}}
                                 "guards" []
                                 "query" ["id" "name"]}}})
    (var topics-query
         {:table "Task"
          :select-entry {"input" [{"symbol" "i_organisation_name" "type" "text"}]
                         "view" {"query" {"status" "{{i_organisation_name}}"}}}
          :return-entry {"input" [{"symbol" "i_topic_id" "type" "uuid"}]
                         "return" "jsonb"
                         "view" {"table" "Task"
                                 "type" "return"
                                 "tag" "topic-summary"
                                 "access" {"roles" {}}
                                 "guards" []
                                 "query" ["id" "name"]}}})
    (model/install node fixtures/InstallOpts)
    (model/model-put
     node
     "room/a"
     "organisations"
     {"views"
      {"selected"
       {"query" organisation-query
        "default_input" [organisation-id]}}})
    (model/model-put
     node
     "room/a"
     "topics"
     {"views"
      {"by-organisation-name"
       {"query" topics-query
        "default_input" ["Greenways Foundation"]
        "deps" [["organisations" "selected"]]}}})
    (-> (model/sync node "room/a" {"db/sync" seed})
        (promise/x:promise-then
         (fn [_]
           (return
            (model/view-refresh node "room/a" "organisations" "selected"))))
        (promise/x:promise-then
         (fn [_]
           (var organisation-name (xtd/get-in
                                   (model/view-val node "room/a" "organisations" "selected")
                                   [0 "name"]))
           (return
            (promise/x:promise-then
             (model/view-set-input
              node
              "room/a"
              "topics"
              "by-organisation-name"
              [organisation-name])
             (fn [_]
               (return organisation-name))))))
        (promise/x:promise-then
         (fn [organisation-name]
           (var topic-names
                (xtd/arr-sort
                 (xt/x:arr-map
                  (or (model/view-val node "room/a" "topics" "by-organisation-name") [])
                  (fn [row]
                    (return (xt/x:get-key row "name"))))
                 (fn [x]
                   (return x))
                 xt/x:lt))
           (repl/notify
            {"organisation-selection-id" (model/view-input node "room/a" "organisations" "selected")
             "organisation-name" organisation-name
             "organisation-status" (xtd/get-in
                                    (model/view-get node "room/a" "organisations" "selected")
                                    ["status"])
             "topics-dependents" (model/view-dependents node "room/a" "organisations" "selected")
             "topics-filter-name" (model/view-input node "room/a" "topics" "by-organisation-name")
             "topics-status" (xtd/get-in
                              (model/view-get node "room/a" "topics" "by-organisation-name")
                              ["status"])
             "topics-query-key?" (xt/x:is-string?
                                  (xtd/get-in
                                   (model/view-get node "room/a" "topics" "by-organisation-name")
                                   ["query_key"]))
             "topic-count" (xt/x:len (or (model/view-val node "room/a" "topics" "by-organisation-name")
                                         []))
             "topic-names" topic-names})))))
  => {"organisation-selection-id" ["00000000-0000-0000-0000-0000000000b1"]
      "organisation-name" "Greenways Foundation"
      "organisation-status" "ready"
      "topics-dependents" {"topics" ["by-organisation-name"]}
      "topics-filter-name" ["Greenways Foundation"]
      "topics-status" "ready"
      "topics-query-key?" true
      "topic-count" 2
      "topic-names" ["Topic Alpha" "Topic Beta"]})
