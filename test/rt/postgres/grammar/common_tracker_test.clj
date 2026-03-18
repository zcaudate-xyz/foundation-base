(ns rt.postgres.grammar.common-tracker-test
  (:use code.test)
  (:require [rt.postgres.grammar.common-tracker :as tracker]
            [rt.postgres.script.test.scratch-v1 :as scratch]))

^{:refer rt.postgres.grammar.common-tracker/add-tracker :added "4.0"}
(fact "call to adjust data to that of the tracker"
  ^:hidden
  
  (tracker/add-tracker {:track 'op}
                       (:static/tracker @scratch/Task)
                       `scratch/Task
                       :insert)
  => (contains {:track 'op, :static/tracker map?}))

^{:refer rt.postgres.grammar.common-tracker/tracker-map-in :added "4.0"}
(fact "creates the insert map"
  ^:hidden
  
  (tracker/tracker-map-in
   (tracker/add-tracker {:track 'op}
                       (:static/tracker @scratch/Task)
                       `scratch/Task
                       :insert))
  => '{:op-created (:->> op "id")
       :op-updated (:->> op "id")
       :time-created (:->> op "time")
       :time-updated (:->> op "time")})

^{:refer rt.postgres.grammar.common-tracker/tracker-map-modify :added "4.0"}
(fact "creates the modify map"
  ^:hidden

  (tracker/tracker-map-modify
   (tracker/add-tracker {:track 'op}
                        (:static/tracker @scratch/Task)
                        `scratch/Task
                        :update))
  => '{:op-updated (:->> op "id"), :time-updated (:->> op "time")})
