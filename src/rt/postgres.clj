(ns rt.postgres
  (:require [rt.postgres.base.client :as client]
            [rt.postgres.base.client-impl :as client-impl]
            [rt.postgres.base.typed :as typed]
            [rt.postgres.base.grammar.gen-bind]
            [rt.postgres.base.grammar.entity :as entity]
            [rt.postgres.base.application :as app]
            [rt.postgres.runtime.addon]
            [rt.postgres.runtime.builtin]
            [rt.postgres.runtime.graph]
            [rt.postgres.runtime.graph-view :as graph-view]
            [rt.postgres.runtime.impl]
            [std.lang :as l]
            [std.lib.foundation :as f])
  (:refer-clojure :exclude [abs concat replace reverse mod name case drop update format
                            assert repeat bit-and bit-or count max min]))

(f/intern-all rt.postgres.runtime.builtin
              rt.postgres.runtime.addon
              rt.postgres.runtime.impl
              rt.postgres.runtime.graph
              rt.postgres.base.grammar.gen-bind)

(f/intern-in client/rt-add-notify
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
             app/app-typed
             app/app-rebuild
             app/app-clear
             
             graph-view/defret.pg
             graph-view/defsel.pg

             entity/E
             typed/Type)

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
                                             :id 'rt.postgres.test.scratch-v1})
      (l/delete-module! (l/runtime-library) {:lang :postgres
                                             :id 'rt.postgres.test.scratch-v1})
      (app/app-clear "scratch-v1")
      (require 'jvm.namespace)
      (eval '(jvm.namespace/reset '[rt.postgres.test.scratch-v1]))))

(comment)
