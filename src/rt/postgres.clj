(ns rt.postgres
  (:require [rt.postgres.script.builtin]
            [rt.postgres.script.addon]
            [rt.postgres.script.impl]
            [rt.postgres.script.graph]
            [rt.postgres.gen-bind]
            [rt.postgres.client-impl :as client-impl]
            [rt.postgres.client :as client]
            [rt.postgres.grammar.common-application :as app]
            [rt.postgres.script.graph-view :as graph-view]
            [rt.postgres.script.graph-partition :as graph-partition]
            [std.string :as str]
            [std.lang :as l]
            [std.lib :as h])
  (:refer-clojure :exclude [abs concat replace reverse mod
                            name
                            case drop update 
                            format assert repeat
                            bit-and bit-or count max min]))

(h/intern-all rt.postgres.script.builtin
              rt.postgres.script.addon
              rt.postgres.script.impl
              rt.postgres.script.graph
              rt.postgres.gen-bind)

(h/intern-in client/rt-add-notify
             client/rt-remove-notify
             client/rt-list-notify
             client/rt-postgres
             client/rt-postgres:create
             
             [invoke client-impl/invoke-ptr-pg]
             [raw-eval client-impl/raw-eval-pg]
             
             app/app-create
             app/app-list
             app/app
             app/app-schema
             app/app-rebuild
             app/app-clear
             
             graph-view/defaccess.pg
             graph-view/defret.pg
             graph-view/defsel.pg
             graph-partition/defpartition.pg)

(defn purge-postgres
  "purges the rt.postgres library. Used for debugging"
  {:added "4.0"}
  []
  (do (l/purge-book! (l/default-library) :postgres)
      (l/purge-book! (l/runtime-library) :postgres)
      (require 'jvm.namespace)
      (eval '(jvm.namespace/reset '[rt.postgres]))))

(defn purge-scratch
  "purges the rt.postgres scratch library. Used for debugging"
  {:added "4.0"}
  []
  (do (l/delete-module! (l/default-library) {:lang :postgres
                                             :id 'rt.postgres.script.scratch})
      (l/delete-module! (l/runtime-library) {:lang :postgres
                                             :id 'rt.postgres.script.scratch})
      (app/app-clear "scratch")
      (require 'jvm.namespace)
      (eval '(jvm.namespace/reset '[rt.postgres.script.scratch]))))

(defn get-rev
  "formats access table"
  {:added "4.0"}
  [access account-sym opts]
  (list 'rt.postgres/g:get
        (-> access
            :reverse
            :table)
        {:where (h/merge-nested (->> access
                                     :reverse
                                     :clause
                                     (h/prewalk-replace {'<%> account-sym}))
                                opts)}))

(comment
  (bind-view )
  (purge-postgres))
