(ns rt.postgres.script.impl-test
  (:use code.test)
  (:require [rt.postgres.script.impl :as impl]
            [rt.postgres.script.impl-main :as main]
            [rt.postgres.grammar.common-application :as app]
            [rt.postgres.grammar.common-tracker :as tracker]
            [std.lang :as l]
            [std.lang.base.book :as book]
            [std.lib :as h]
            [rt.postgres :as pg]
            [rt.postgres.script.scratch :as scratch]))

(l/script- :postgres
  {:runtime :jdbc.client
   :config  {:dbname "test-scratch"}
   :require [[rt.postgres :as pg]
             [rt.postgres.script.scratch :as scratch]]})

;; Removed global setup to avoid connection attempt

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
  => vector?

  (pg/t:select scratch/Task
    {:returning #{:name}
     :as :raw})
  => list?
  
  (pg/t:select scratch/Task {:single true
                             :returning #{:-/data}
                             :where {:name "002"}})
  => map?

  (pg/t:select scratch/Task {:single true
                             :where {:name "002"}})
  => map?
  
  (pg/t:select scratch/Task {:single true
                             :returning #{:*/everything}
                             :where {:name "002"}})
  => map?
  
  (pg/t:select scratch/Task
    {:returning #{:name}
     :where #{[:name "002"
               :or
               :name "001"]}})
  => vector?)

^{:refer rt.postgres.script.impl/t:get-field :added "4.0"}
(fact "gets single field"
  ^:hidden
  
  (l/with:emit
    (l/emit-as :postgres
               `[(pg/t:get-field scratch/Task
                   {:where {:id "1"}
                    :returning :name})]))
  => "SELECT \"name\" FROM \"scratch\".\"Task\"\nWHERE \"id\" = '1'\nLIMIT 1")

^{:refer rt.postgres.script.impl/t:get :added "4.0"}
(fact "get single entry"
  ^:hidden
  
  (l/with:emit
    (l/emit-as :postgres
               `[(pg/t:get scratch/Task
                   {:where {:id "1"}})]))
  => "WITH j_ret AS (  \n  SELECT \"id\",\"status\",\"name\",\"cache_id\",\"time_created\",\"time_updated\" FROM \"scratch\".\"Task\"\n  WHERE \"id\" = '1'\n  LIMIT 1)\nSELECT to_jsonb(j_ret) FROM j_ret")

^{:refer rt.postgres.script.impl/t:id :added "4.0"}
(fact "get id entry"
  ^:hidden
  
  (l/with:emit
    (l/emit-as :postgres
               `[(pg/t:id scratch/Task
                   {:where {:name "home"}})]))
  => "SELECT \"id\" FROM \"scratch\".\"Task\"\nWHERE \"name\" = 'home'\nLIMIT 1")

^{:refer rt.postgres.script.impl/t:count :added "4.0"}
(fact "get count entry"
  ^:hidden
  
  (l/with:emit
    (l/emit-as :postgres
               `[(pg/t:count scratch/Task
                   {:where {:name "home"}})]))
  => "SELECT count(*) FROM \"scratch\".\"Task\"\nWHERE \"name\" = 'home'")

^{:refer rt.postgres.script.impl/t:delete :added "4.0"}
(fact "flat delete"
  ^:hidden
  
  (l/with:emit
    (l/emit-as :postgres
               `[(pg/t:delete scratch/Task
                   {:where {:name "home"}})]))
  => "WITH j_ret AS (  \n  DELETE FROM \"scratch\".\"Task\" WHERE \"name\" = 'home'\n  RETURNING\n    \"id\",\n    \"status\",\n    \"name\",\n    \"cache_id\",\n    \"op_created\",\n    \"op_updated\",\n    \"time_created\",\n    \"time_updated\")\nSELECT jsonb_agg(j_ret) FROM j_ret")

^{:refer rt.postgres.script.impl/t:insert :added "4.0"}
(fact "flat insert"
  ^:hidden
  
  (l/with:emit
    (l/emit-as :postgres
               `[(pg/t:insert scratch/TaskCache
                   {}
                   {:track :ignore})]))
  => "WITH j_ret AS (INSERT INTO \"scratch\".\"TaskCache\" () VALUES () RETURNING \"id\",\"time_created\",\"time_updated\")\nSELECT to_jsonb(j_ret) FROM j_ret")

^{:refer rt.postgres.script.impl/t:insert! :added "4.0"}
(fact "inserts without o-op"
  ^:hidden
  
  (l/with:emit
    (l/emit-as :postgres
               `[(pg/t:insert! scratch/TaskCache
                   {}
                   {})]))
  => "WITH j_ret AS (INSERT INTO \"scratch\".\"TaskCache\" () VALUES () RETURNING \"id\",\"time_created\",\"time_updated\")\nSELECT to_jsonb(j_ret) FROM j_ret")

^{:refer rt.postgres.script.impl/t:upsert :added "4.0"}
(fact "flat upsert"
  ^:hidden
  
  (l/with:emit
    (l/emit-as :postgres
               `[(pg/t:upsert scratch/TaskCache
                   {}
                   {:track :ignore})]))
  => "WITH j_ret AS (INSERT INTO \"scratch\".\"TaskCache\" () VALUES () ON CONFLICT (\"id\") DO UPDATE SET () = row() RETURNING \"id\",\"time_created\",\"time_updated\")\nSELECT to_jsonb(j_ret) FROM j_ret")

^{:refer rt.postgres.script.impl/t:update :added "4.0"}
(fact "flat update"
  ^:hidden
  
  (l/with:emit
    (l/emit-as :postgres
               `[(pg/t:update scratch/Task
                   {:where {:name "home"}
                    :set {:name "hello"}
                    :track :ignore})]))
  => "WITH j_ret AS (  \n  UPDATE \"scratch\".\"Task\" SET \"name\" = ('hello')::TEXT WHERE \"name\" = 'home'\n  RETURNING \"id\",\"status\",\"name\",\"cache_id\",\"time_created\",\"time_updated\")\nSELECT jsonb_agg(j_ret) FROM j_ret")

^{:refer rt.postgres.script.impl/t:update! :added "4.0"}
(fact "updates with o-op"
  ^:hidden
  
  (l/with:emit
    (l/emit-as :postgres
               `[(pg/t:update! scratch/Task
                   {:where {:name "home"}
                    :set {:name "hello"}})]))
  => "UPDATE \"scratch\".\"Task\" SET \"name\" = ('hello')::TEXT WHERE \"name\" = 'home'")

^{:refer rt.postgres.script.impl/t:modify :added "4.0"}
(fact "flat modify"
  ^:hidden
  
  (l/with:emit
    (l/emit-as :postgres
               `[(pg/t:modify scratch/Task
                   {:where {:name "home"}
                    :set {:name "hello"}
                    :track :ignore})]))
  => "DECLARE\n  u_ret \"scratch\".\"Task\";\nBEGIN\n  UPDATE \"scratch\".\"Task\" SET \"name\" = ('hello')::TEXT WHERE \"name\" = 'home'\n  RETURNING * INTO u_ret;\n  IF not exists(SELECT u_ret) THEN\n    RAISE EXCEPTION 'Record Not Found';\n  END IF;\nEND;")

^{:refer rt.postgres.script.impl/t:fields :added "4.0"}
(fact "gets the fields"
  ^:hidden
  
  (l/with:emit
    (l/emit-as :postgres
               `[(pg/t:fields scratch/Task
                   {:where {:name "home"}})]))
  => string?)


^{:refer rt.postgres.script.impl/t:exists :added "4.0"}
(fact "check existence of entry"
  (l/with:emit
    (l/emit-as :postgres
               `[(pg/t:exists scratch/Task {:where {:id 1}})]))
  => string?)
