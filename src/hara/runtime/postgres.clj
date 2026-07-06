(ns hara.runtime.postgres
  (:require [hara.runtime.postgres.base.client :as client]
            [hara.runtime.postgres.base.client-impl :as client-impl]
            [postgres.gen.bind-macro]
            [postgres.entity :as entity]
            [hara.runtime.postgres.base.application :as app]
            [postgres.core.addon]
            [postgres.core.builtin]
            [postgres.core.graph]
            [postgres.core.graph-view :as graph-view]
            [postgres.core.impl]
            [hara.lang :as l]
            [std.lib.foundation :as f])
  (:refer-clojure :exclude [abs concat replace reverse mod name case drop update format
                            assert repeat bit-and bit-or count max min]))

(f/intern-all postgres.core.builtin
              postgres.core.addon
              postgres.core.impl
              postgres.core.graph
              postgres.gen.bind-macro)

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

             entity/E)

(defn purge-postgres
  "purges the postgres.core library. Used for debugging"
  {:added "4.0"}
  []
  (do (l/purge-book! (l/default-library) :postgres)
      (l/purge-book! (l/runtime-library) :postgres)
      (require 'jvm.namespace)
      (eval '(jvm.namespace/reset '[postgres.core]))))

(defn purge-scratch
  "purges the postgres.core scratch library. Used for debugging"
  {:added "4.0"}
  []
  (do (l/delete-module! (l/default-library) {:lang :postgres
                                             :id 'postgres.sample.scratch-v1})
      (l/delete-module! (l/runtime-library) {:lang :postgres
                                             :id 'postgres.sample.scratch-v1})
      (app/app-clear "scratch-v1")
      (require 'jvm.namespace)
      (eval '(jvm.namespace/reset '[postgres.sample.scratch-v1]))))

(comment)
