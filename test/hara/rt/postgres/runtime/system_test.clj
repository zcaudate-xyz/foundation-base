(ns hara.rt.postgres.runtime.system-test
  (:require [hara.rt.postgres.runtime.system :as sys]
            [hara.lang :as l])
  (:use code.test))

(l/script- :postgres
  {:runtime :jdbc.client
   :config  {:dbname "test-scratch"}
   :require [[hara.rt.postgres.runtime.system :as sys]]})

^{:refer hara.rt.postgres.runtime.system/CANARY :adopt true :added "4.0"}
(fact "creates a pg template"

  (sys/jit-available)
  => boolean?

  (l/with:raw
   (sys/jit-available))
  => (contains-in [{:pg_jit_available boolean?}]))

^{:refer hara.rt.postgres.runtime.system/pg-tmpl :added "4.0"}
(fact "creates a pg template"

  (sys/pg-tmpl 'hello)
  => '(def$.pg hello pg-hello))
