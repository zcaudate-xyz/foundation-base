(ns postgres.core
  (:require [postgres.gen.bind-macro]
            [postgres.entity :as entity]
            [hara.runtime.postgres.base.application :as app]
            [postgres.core.addon]
            [postgres.core.builtin]
            [postgres.core.graph]
            [postgres.core.impl]
            [postgres.core.graph-view :as graph-view]
            [hara.lang :as l]
            [std.lib.foundation :as f])
  (:refer-clojure :exclude [abs concat replace reverse mod name case drop update format
                            assert repeat bit-and bit-or count max min]))

(f/intern-all postgres.core.builtin
              postgres.core.addon
              postgres.core.impl
              postgres.core.graph)

(f/intern-in graph-view/defret.pg
             graph-view/defsel.pg
             postgres.gen.bind-macro/bind-app
             postgres.gen.bind-macro/bind-schema
             app/app-create
             app/app-list
             app/app
             app/app-schema
             app/app-typed
             app/app-rebuild
             app/app-clear

             entity/E)
