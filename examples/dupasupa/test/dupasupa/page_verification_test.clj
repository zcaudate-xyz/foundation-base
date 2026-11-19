(ns dupasupa.page-verification-test
  (:require [app.manage.verify :as verify]
            [app.manage.integration.postgres :as database]
            [dupasupa.build :as build]
            [dupasupa.verification :as runtime])
  (:use code.test))

^{:refer dupasupa.pages/page-index :id contracts :timeout 60000}
(fact "campaign and profile registry members all have read/write operation contracts"
  (select-keys (verify/definitions! build/PROJECT) [:status :pages :findings])
  =>
  {:status :ok :pages 2 :findings []})

^{:refer dupasupa.verification/execute :id saved-lifecycle :timeout 120000}
(fact
  "saved campaign scenarios execute the actual generated model and verified Next handlers against disposable Supabase PostgreSQL and Auth users"
  (let [owned (atom nil)
        result (runtime/with-fixture (fn []
                                       (reset! owned (:database runtime/*fixture*))
                                       (verify/run-cases! (dissoc build/PROJECT
                                                           :page-test-fixture))))
        scenario (first (:cases result))]
    [(:status result) (:id scenario) (mapv :status (:steps scenario))
     (get-in scenario [:steps 2 :result :title]) (get-in scenario [:steps 4 :result :output])
     (get-in scenario [:steps 6 :error :message]) (some #(= 201 (:status %)) (:trace scenario))
     (database/exists? @owned)])
  =>
  [:ok "campaign-lifecycle"
   [:passed :passed :passed :passed :passed :passed :passed :passed :passed :passed :passed :passed]
   "Updated campaign" [] "Campaign title is required" true false])

^{:refer dupasupa.verification/execute :id unsupported :timeout 60000}
(fact "unsupported operations are blocked rather than reported as successful executions"
  (:blocked (runtime/execute {:page "profile" :kind "action" :member "save" :actor "alice"}))
  =>
  "This operation has no live workbench adapter yet.")
