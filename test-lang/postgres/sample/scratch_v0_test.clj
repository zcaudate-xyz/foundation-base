(ns postgres.sample.scratch-v0-test
  (:require [hara.lang :as l]
            [postgres.sample.scratch-v0 :as scratch])
  (:use code.test))

(l/script- :postgres
  {:runtime :jdbc.client
   :require [[postgres.sample.scratch-v0 :as scratch]
             [postgres.core :as pg]]
   :config {:dbname "test-scratch"}})

(fact:global
  {:setup [(l/rt:restart)
           (l/rt:setup :postgres)]
   :teardown [(l/rt:teardown :postgres)
              (l/rt:stop)]})

(comment
  (pg/t:select scratch/Log)
  
  (scratch/log-append-public "hello")
  (scratch/log-append-public "hello1")
  
  )


^{:refer postgres.sample.scratch-v0/Log :added "4.1.4"}
(fact "constructs a log table")

^{:refer postgres.sample.scratch-v0/ping :added "4.1.4"}
(fact "emits a public ping rpc"

  (l/emit-as :postgres `[(scratch/ping)])
  => "\"scratch_v0\".ping()")

^{:refer postgres.sample.scratch-v0/log-append :added "4.1.4"}
(fact "emits an authenticated log append rpc"

  (l/emit-as :postgres `[(scratch/log-append "hello")])
  => "\"scratch_v0\".log_append('hello')")


^{:refer postgres.sample.scratch-v0/log-append-public :added "4.1"}
(fact "TODO")