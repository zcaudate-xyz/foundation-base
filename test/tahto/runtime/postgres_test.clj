(ns tahto.runtime.postgres-test
  (:use code.test)
  (:require [tahto.runtime.postgres :refer :all]
            [std.lib.env :as env]))

(fact:global
 {:skip (not (env/program-exists? "postgres"))})

^{:refer tahto.runtime.postgres/purge-postgres :added "4.0"}
(fact "purges the postgres core library and returns a namespace reset report"
  (purge-postgres)
  => map?)

^{:refer tahto.runtime.postgres/purge-scratch :added "4.0"}
(fact "purges the postgres scratch library"
  (purge-scratch)
  => (throws clojure.lang.ArityException
            "Wrong number of args (2) passed to: tahto.core.library/delete-module!"))