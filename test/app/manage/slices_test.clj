(ns app.manage.slices-test
  (:require [app.manage :as manage] [app.manage.slices :as slices]
            [app.manage-test :as fixture] [clojure.java.io :as io])
  (:use code.test))
^{:refer app.manage.slices/status :added "4.1"}
(fact "generation evidence survives reload and becomes stale after sources change"
  (fixture/with-project
    (fn [p]
      (let [p (assoc p :slices {:one {:stages [{:id :db :targets [:db]}]}})
            before (slices/fingerprints p)
            result (manage/build p {:targets [:db] :tests? false})]
        (slices/record! p [:db] result before)
        (let [fresh (get-in (slices/status p) [0 :stages 0 :status])]
          (io/make-parents (io/file (:root p) "src/db/source.clj"))
          (spit (io/file (:root p) "src/db/source.clj") "changed")
          [fresh (get-in (slices/status p) [0 :stages 0 :status])]))))
  => [:generated :stale])
^{:refer app.manage.slices/update-work! :added "4.1"}
(fact "notes use optimistic revisions and never establish verification"
  (fixture/with-project
    (fn [p]
      (let [p (assoc p :slices {:one {:stages [{:id :db :targets [:db]}]}})]
        (slices/update-work! p {:slice "one" :revision 0 :note "Implement RPC" :state "active"})
        [(get-in (slices/status p) [0 :work :note])
         (get-in (slices/status p) [0 :stages 0 :status])
         (:http-status (fixture/error-data #(slices/update-work! p {:slice "one" :revision 0 :note "old" :state "active"})))])))
  => ["Implement RPC" :missing 409])
