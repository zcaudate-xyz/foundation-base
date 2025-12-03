(ns rt.postgres.grammar.form-defconst-test
  (:use code.test)
  (:require [rt.postgres.grammar.form-defconst :as form]
            [rt.postgres.grammar :as g]
            [rt.postgres.script.scratch :as scratch]
            [std.lang :as l]))

^{:refer rt.postgres.grammar.form-defconst/pg-defconst-hydrate :added "4.0"}
(fact "creates the "
  ^:hidden
  
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
                                         'rt.postgres.script.scratch)
                               :snapshot (l/get-snapshot (l/runtime-library))}))
  -out-
  => vector?)

^{:refer rt.postgres.grammar.form-defconst/pg-defconst :added "4.0"}
(fact "emits the static form"
  ^:hidden
  
  (form/pg-defconst
   (second -out-))
  => '(do:block
       (let
           [o-track {}]
         [:insert-into
          rt.postgres.script.scratch/Task
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
            (++ "ok" rt.postgres.script.scratch/EnumStatus)
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
  
  
  
  
