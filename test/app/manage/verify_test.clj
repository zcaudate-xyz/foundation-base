(ns app.manage.verify-test
  (:require [app.manage.verify :as v] [app.manage-test :as fixture])
  (:use code.test))
^{:refer app.manage.verify/compare-result :added "4.1"}
(fact "exact null is different from a missing result and errors cannot pass positive checks"
  [(v/compare-result {:result nil} {:equals nil})
   (v/compare-result {} {:equals nil})
   (v/compare-result {:error {:code "23503"}} {:contains {}})
   (v/compare-result {:error {:code "23503" :message "Rejected"}} {:error {:code "23503"}})
   (v/compare-result {:result {:detail {:visibility "private"} :id "generated"}} {:contains {:detail {:visibility "private"}}})
   (v/compare-result {:result 1} nil)]
  => [:passed :failed :failed :passed :passed :executed])
^{:refer app.manage.verify/save! :added "4.1"}
(fact "saved cases require assertions and reject stale revisions"
  (fixture/with-project
    (fn [p]
      (v/save! p {:id "one" :revision 0 :case {:page "logbook" :expect {:equals nil}}})
      [(get-in (v/cases p) [:cases "one" :page])
       (:http-status (fixture/error-data #(v/save! p {:id "two" :revision 0 :case {:expect {:equals 1}}})))
       (:http-status (fixture/error-data #(v/save! p {:id "two" :revision 1 :case {}})))]))
  => ["logbook" 409 400])
^{:refer app.manage.verify/evaluate-request :added "4.1"}
(fact "a scenario fails when any assertion fails or an execution step is missing"
  (let [request {:steps [{:id "a" :expect {:equals 1}} {:id "b" :expect {:equals nil}}]}]
    [(:status (v/evaluate-request request {:steps [{:id "a" :result 1} {:id "b" :result nil}]}))
     (:status (v/evaluate-request request {:steps [{:id "a" :result 1}]}))
     (:status (v/evaluate-request request {:steps [{:id "a" :result 2} {:id "b" :result nil}]}))
     (:status (v/evaluate-request request {:steps [{:id "wrong" :result 1} {:id "b" :result nil}]}))
     (:status (v/evaluate-request request {:steps [{:id "a" :result 1} {:id "b" :result nil} {:id "extra" :result 3}]}))])
  => [:passed :failed :failed :failed :failed])

^{:refer app.manage.verify/ready? :added "4.1"}
(fact "verification blocks stale runtimes and evidence becomes stale after source or case edits"
  (fixture/with-project
    (fn [p]
      (let [p (-> p (assoc :verification-targets [:page] :verification-receipt "generated/receipt.edn"
                           :page-verifier (fn [_] {:result nil}))
                  (update-in [:targets :page :outputs] conj "generated/receipt.edn")
                  (update-in [:targets :page] v/with-receipt "generated/receipt.edn"))]
        (app.manage/generate p {:write true})
        (v/execute! p {:expect {:equals nil}})
        (let [fresh (get-in (v/snapshot p) [:latest :status])]
          (v/save! p {:id "one" :revision 0 :case {:expect {:equals nil}}})
          (let [cases-stale (get-in (v/snapshot p) [:latest :status])
                ready-after-save (boolean (v/ready? p))]
            (spit (app.manage/project-file p "generated/page.edn") "edited")
            (let [edited (boolean (v/ready? p))]
              (app.manage/generate p {:write true})
              (clojure.java.io/make-parents (app.manage/project-file p "src/pages/source.clj"))
              (spit (app.manage/project-file p "src/pages/source.clj") "changed")
              [fresh cases-stale ready-after-save edited (boolean (v/ready? p))
               (:status (v/execute! p {:expect {:equals nil}}))]))))))
  => [:passed :stale true false false :blocked])
