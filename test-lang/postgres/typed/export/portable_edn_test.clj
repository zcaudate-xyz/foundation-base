(ns postgres.typed.export.portable-edn-test
  (:require [clojure.edn :as edn]
            [postgres.typed :as typed]
            [postgres.typed.export.portable-edn :as portable-edn]
            [postgres.typed.typed-common :as types])
  (:use code.test))

^{:refer postgres.typed.export.portable-edn/export-edn :added "4.1"}
(fact "round-trips a registry context and exposes the same API through postgres.typed"
  (let [table (types/make-table-def "demo" "Entry" [] :id)
        ctx (typed/load-registry {'demo/Entry table})
        snapshot (portable-edn/export-edn ctx)]
    [(map? snapshot)
     (= snapshot (edn/read-string (pr-str snapshot)))
     (= ctx (portable-edn/import-edn snapshot))
     (= ctx (portable-edn/import-edn (edn/read-string (pr-str snapshot))))
     (= snapshot (typed/export-edn ctx))
     (= ctx (typed/import-edn snapshot))])
  => [true true true true true true])
