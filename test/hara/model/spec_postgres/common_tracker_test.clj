(ns hara.model.spec-postgres.common-tracker-test
  (:require [hara.model.spec-postgres.common-tracker :as tracker]
            [hara.runtime.postgres.test.scratch-v1 :as scratch])
  (:use code.test))

^{:refer hara.model.spec-postgres.common-tracker/tracker-string :added "4.1"}
(fact "tracker-string formats tracker metadata"
  (tracker/tracker-string {:name "Task"
                           :in true
                           :out true})
  => "#pg.tracker [Task] {:in true, :out true}")

^{:refer hara.model.spec-postgres.common-tracker/add-tracker :added "4.0"}
(fact "call to adjust data to that of the tracker"

  (tracker/add-tracker {:track 'op}
                       (:static/tracker @scratch/Task)
                       `scratch/Task
                       :insert)
  => (contains {:track 'op, :static/tracker map?}))

^{:refer hara.model.spec-postgres.common-tracker/tracker-map-in :added "4.0"}
(fact "creates the insert map"

  (tracker/tracker-map-in
   (tracker/add-tracker {:track 'op}
                       (:static/tracker @scratch/Task)
                       `scratch/Task
                       :insert))
  => '{:op-created (:->> op "id")
       :op-updated (:->> op "id")
       :time-created (:->> op "time")
       :time-updated (:->> op "time")})

^{:refer hara.model.spec-postgres.common-tracker/tracker-map-modify :added "4.0"}
(fact "creates the modify map"

  (tracker/tracker-map-modify
   (tracker/add-tracker {:track 'op}
                        (:static/tracker @scratch/Task)
                        `scratch/Task
                        :update))
  => '{:op-updated (:->> op "id"), :time-updated (:->> op "time")})