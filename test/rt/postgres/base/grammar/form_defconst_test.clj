(ns rt.postgres.base.grammar.form-defconst-test
  (:require [rt.postgres.base.grammar :as g]
            [rt.postgres.base.grammar.form-defconst :as form]
            [rt.postgres.test.scratch-v1 :as scratch]
            [std.lang :as l])
  (:use code.test))

^{:refer rt.postgres.base.grammar.form-defconst/pg-defconst-hydrate :added "4.0"}
(fact "creates the "

  (def -out-
    (form/pg-defconst-hydrate (list 'defconst
                                    (with-meta 'hello {:track {}})
                                    [`scratch/Task]
                                    {:id "hello-0"
                                     :name "hello"
                                     :status "ok"
                                     :cache "cache-001"})
                              (:grammar (l/get-book (l/runtime-library) :postgres))
                              {:lang :postgres
                               :module  (l/get-module
                                         (l/runtime-library)
                                         :postgres
                                         'rt.postgres.test.scratch-v1)
                               :snapshot (l/get-snapshot (l/runtime-library))}))
  -out-
  => vector?)

^{:refer rt.postgres.base.grammar.form-defconst/pg-defconst :added "4.0"}
(fact "emits the static form"

  (form/pg-defconst
   (second -out-))
  => '(do:block
       (let
           [o-track {}]
         [:insert-into
          rt.postgres.test.scratch-v1/Task
          (>-<
           [#{"id"}
            #{"status"}
            #{"name"}
            #{"cache_id"}
            #{"op_created"}
            #{"op_updated"}
            #{"time_created"}
            #{"time_updated"}])
          :values
          (>-<
           [(:uuid "hello-0")
            (++ "ok" rt.postgres.test.scratch-v1/EnumStatus)
            (:text "hello")
            (:uuid "cache-001")
            (:uuid (:->> o-track "id"))
            (:uuid (:->> o-track "id"))
            (:bigint (:->> o-track "time"))
            (:bigint (:->> o-track "time"))])
          :on-conflict
          '(#{"id"})
          :do-update
          :set
          '(#{"id"}
            #{"status"}
            #{"name"}
            #{"cache_id"}
            #{"op_updated"}
            #{"time_updated"})
          :=
          (row
           (. (:- "EXCLUDED") #{"id"})
           (. (:- "EXCLUDED") #{"status"})
           (. (:- "EXCLUDED") #{"name"})
           (. (:- "EXCLUDED") #{"cache_id"})
           (. (:- "EXCLUDED") #{"op_updated"})
           (. (:- "EXCLUDED") #{"time_updated"}))])))




