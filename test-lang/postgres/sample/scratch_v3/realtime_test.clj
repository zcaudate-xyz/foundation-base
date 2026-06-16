(ns postgres.sample.scratch-v3.realtime-test
  (:use code.test)
  (:require [postgres.sample.scratch-v3.realtime :refer :all]))

^{:refer postgres.sample.scratch-v3.realtime/sync-tables :added "4.1"}
(fact "TODO")

^{:refer postgres.sample.scratch-v3.realtime/db-sync :added "4.1"}
(fact "TODO")

^{:refer postgres.sample.scratch-v3.realtime/payload-id :added "4.1"}
(fact "TODO")

^{:refer postgres.sample.scratch-v3.realtime/postgres-change->sync-request :added "4.1"}
(fact "TODO")

^{:refer postgres.sample.scratch-v3.realtime/postgres-change->update :added "4.1"}
(fact "TODO")