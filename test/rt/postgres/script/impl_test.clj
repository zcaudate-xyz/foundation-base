(ns rt.postgres.script.impl-test
  (:use code.test)
  (:require [rt.postgres.script.impl :as impl]
            [rt.postgres.script.impl-main :as main]
            [rt.postgres.grammar.common-application :as app]
            [rt.postgres.grammar.common-tracker :as tracker]
            [std.lang :as l]
            [std.lang.base.book :as book]
            [std.lib :as h]))

(l/script- :postgres
  {:runtime :jdbc.client
   :config  {:dbname "test-scratch"}
   :require [[rt.postgres :as pg]
             [rt.postgres.script.scratch :as scratch]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:setup-to :postgres)]
  :teardown [(l/rt:teardown :postgres)
             (l/rt:stop)]})

^{:refer rt.postgres.script.impl/t:select :added "4.0"
  :setup [(pg/t:delete scratch/Task)
          (pg/t:delete scratch/TaskCache)
          (pg/t:insert scratch/TaskCache
            {:id (str (h/uuid-nil))}
            {:track {}})
          (pg/t:insert scratch/Task
            {:status "pending"
             :name "001"
             :cache (str (h/uuid-nil))}
            {:track {}})
          (pg/t:insert scratch/Task
            {:status "pending"
             :name "002"
             :cache (str (h/uuid-nil))}
            {:track {}})]}
(fact "flat select"
  ^:hidden
  
  (pg/t:select scratch/Task)
  => vector?

  (pg/t:select scratch/Task
    {:returning #{:name}})
  => [{:name "001"} {:name "002"}]

  (pg/t:select scratch/Task
    {:returning #{:name}
     :as :raw})
  => '("001" "002")
  
  (pg/t:select scratch/Task {:single true
                             :returning #{:-/data}
                             :where {:name "002"}})
  => {:name "002", :time-updated nil, :time-created nil}

  (pg/t:select scratch/Task {:single true
                             :where {:name "002"}})
  => (contains {:cache-id "00000000-0000-0000-0000-000000000000",
                :name "002",
                :op-updated nil,
                :time-updated nil,
                :time-created nil,
                :status "pending",
                :id string?
                :op-created nil})
  
  (pg/t:select scratch/Task {:single true
                             :returning #{:*/everything}
                             :where {:name "002"}})
  => (contains {:cache-id "00000000-0000-0000-0000-000000000000",
                :name "002",
                :op-updated nil,
                :time-updated nil,
                :time-created nil,
                :status "pending",
                :id string?
                :op-created nil,
                :--deleted-- false})
  
  (pg/t:select scratch/Task
    {:returning #{:name}
     :where #{[:name "002"
               :or
               :name "001"]}})
  => '[{:name "001"} {:name "002"}])

^{:refer rt.postgres.script.impl/t:get-field :added "4.0"}
(fact "gets single field")

^{:refer rt.postgres.script.impl/t:get :added "4.0"}
(fact "get single entry")

^{:refer rt.postgres.script.impl/t:id :added "4.0"}
(fact "get id entry")

^{:refer rt.postgres.script.impl/t:count :added "4.0"}
(fact "get count entry")

^{:refer rt.postgres.script.impl/t:delete :added "4.0"}
(fact "flat delete")

^{:refer rt.postgres.script.impl/t:insert :added "4.0"}
(fact "flat insert")

^{:refer rt.postgres.script.impl/t:insert! :added "4.0"}
(fact "inserts without o-op")

^{:refer rt.postgres.script.impl/t:upsert :added "4.0"}
(fact "flat upsert")

^{:refer rt.postgres.script.impl/t:update :added "4.0"}
(fact "flat update")

^{:refer rt.postgres.script.impl/t:update! :added "4.0"}
(fact "updates with o-op")

^{:refer rt.postgres.script.impl/t:modify :added "4.0"}
(fact "flat modify")


^{:refer rt.postgres.script.impl/t:fields :added "4.0"}
(fact "TODO")