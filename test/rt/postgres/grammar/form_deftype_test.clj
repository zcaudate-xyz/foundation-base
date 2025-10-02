(ns rt.postgres.grammar.form-deftype-test
  (:use code.test)
  (:require [rt.postgres.grammar.form-deftype :refer :all]
            [rt.postgres.grammar :as g]
            [rt.postgres.script.scratch :as scratch]
            [std.lang :as l]))

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-enum-col :added "4.0"}
(fact "creates the enum column"
  ^:hidden
  
  (pg-deftype-enum-col
   [:text]
   {:ns `scratch/EnumStatus}
   {:lang :postgres
    :snapshot (l/get-snapshot (l/runtime-library))})
  => '[(. #{"scratch"} #{"EnumStatus"})])

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-ref :added "4.0"}
(fact "creates the ref entry"
  ^:hidden
  
  (pg-deftype-ref :cache
                  '{:ns :TaskCache,
                    :link {:context :lang/postgres, :lang :postgres,
                           :id TaskCache, :module rt.postgres.script.scratch,
                           :section :code}}
                  {:lang :postgres
                   :snapshot (l/get-snapshot (l/runtime-library))})
  => '["cache_id" [:uuid] [((. #{"scratch"} #{"TaskCache"}) #{"id"})]])

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-col-sql :added "4.0"}
(fact "formats the sql on deftype"
  ^:hidden
  
  (pg-deftype-col-sql '() {:cascade true})
  => [:on-delete-cascade]

  (pg-deftype-col-sql '() {:default 5})
  => [5 :default]

  (pg-deftype-col-sql '() {:constraint '(ut/is-color #{"color"})})
  => '[:check (quote ((ut/is-color #{"color"})))])

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-col-fn :added "4.0"}
(fact "formats the column on deftype"
  ^:hidden
  
  (pg-deftype-col-fn [:id {:type :uuid :primary true}] {})
  => [#{"id"} :uuid :primary-key])

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-uniques :added "4.0"}
(fact "collect unique keys on deftype"
  ^:hidden
  
  (pg-deftype-uniques [[:name {:type :text :sql {:unique true}}]])
  => '[(% [:unique (quote (#{"name"}))])]
  
  (pg-deftype-uniques [[:first {:type :text :sql {:unique ["name"]}}]
                       [:last  {:type :text :sql {:unique ["name"]}}]])
  => '[(% [:unique (quote (#{"first"} #{"last"}))])]
  
  (pg-deftype-uniques [[:first {:type :text :sql {:unique ["name"]}}]
                       [:last  {:type :text :sql {:unique ["name"]}}]
                       [:key   {:type :text :sql {:unique "default"}}]])
  => '[(% [:unique (quote (#{"first"} #{"last"}))])
       (% [:unique (quote (#{"key"}))])]

  (pg-deftype-uniques [[:first     {:type :text :sql {:unique ["name" "account"]}}]
                       [:last      {:type :text :sql {:unique ["name" "default"]}}]
                       [:account   {:type :ref :sql  {:unique ["name" "account"]}}]])
  => '[(% [:unique (quote (#{"first"} #{"last"} #{"account_id"}))])
       (% [:unique (quote (#{"first"} #{"account_id"}))])
       (% [:unique (quote (#{"last"}))])])

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-indexes :added "4.0"}
(fact "create index statements"
  ^:hidden
  
  (pg-deftype-indexes [[:name {:type :text :sql {:index true}}]]
                      #{"scratch"})
  => '[(% [:create-index :on #{"scratch"} (quote (#{"name"}))])]

  (pg-deftype-indexes [[:name {:type :text :sql {:index {:using :hash
                                                         :where {:name [:neq "hello"]}}}}]]
                      #{"scratch"})
  => '[(% [:create-index :on #{"scratch"}
           :using :hash (quote (#{"name"}))
           \\ :where {:name [:neq "hello"]}])])

^{:refer rt.postgres.grammar.form-deftype/pg-deftype :added "4.0"}
(fact "creates a deftype statement"
  ^:hidden
  
  (l/with:emit
   (l/emit-as
    :postgres [(pg-deftype '(deftype TestTable [:id {:type :uuid :primary true}]))]))
  => (std.string/|
      "DROP TABLE IF EXISTS \"TestTable\" CASCADE;"
      "CREATE TABLE IF NOT EXISTS \"TestTable\" ("
      "  \"id\" UUID PRIMARY KEY"
      ");"))

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-fragment :added "4.0"}
(fact "parses the fragment contained by the symbol"
  ^:hidden
  
  (pg-deftype-fragment `scratch/Id)
  => '(:id {:type :uuid, :primary true, :sql {:default (rt.postgres/uuid-generate-v4)}})

  (pg-deftype-fragment `scratch/RecordType)
  => '[:op-created    {:type :uuid, :scope :-/system}
       :op-updated    {:type :uuid, :scope :-/system}
       :time-created  {:type :long}
       :time-updated  {:type :long}
       :__deleted__   {:type :boolean, :scope :-/hidden, :sql {:default false}}])

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-format :added "4.0"}
(fact "formats an input form"
  ^:hidden
  
  (pg-deftype-format
   '(deftype ^{:track   [scratch/TrackingMin]
               :prepend [scratch/Id]
               :public true}
        BasicTable
        [:name      {:type :citext :primary true 
                     :web {:example "AUD"}}]
      {:constraints {:not-admin [(not= #{"name"} "admin")]}}))
  => (contains [map?
                '(deftype BasicTable
                     [:id   {:type :uuid,
                             :primary true,
                             :sql {:default (rt.postgres/uuid-generate-v4)},
                             :scope :-/id}
                      :name {:type :citext,
                             :primary true,
                             :web {:example "AUD"},
                             :scope :-/id}]
                   {:constraints
                    {:not-admin [(not= #{"name"} "admin")]}})]))

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-hydrate-check-link :added "4.0"}
(fact "checks a link making sure it exists and is correct type"
  ^:hidden
  
  (pg-deftype-hydrate-check-link (l/get-snapshot (l/runtime-library))
                                 {:id 'Task,
                                  :module 'rt.postgres.script.scratch,
                                  :lang :postgres,
                                  :section :code}
                                 :table)
  => true

  (pg-deftype-hydrate-check-link (l/get-snapshot (l/runtime-library))
                                 {:id 'EnumStatus
                                  :module 'rt.postgres.script.scratch,
                                  :lang :postgres,
                                  :section :code}
                                 :enum)
  => true)

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-hydrate :added "4.0"}
(fact "hydrates the form with linked ref information"
  ^:hidden
  
  (-> (pg-deftype-format
       '(deftype ^{:track   [scratch/TrackingMin]
                   :prepend [scratch/Id]
                   :public true}
            BasicTable
            [:status   {:type :enum
                        :sql {:default "pending"}
                        :enum {:ns scratch/EnumStatus}}
             :task     {:type :ref
                        :ref {:ns scratch/Task}}]
          {:constraints {:not-admin [(not= #{"name"} "admin")]}}))
      second
      (pg-deftype-hydrate
       (:grammar (l/get-book (l/runtime-library) :postgres))
       {:lang :postgres
        :snapshot (l/get-snapshot (l/runtime-library))})
      second)
  => '(deftype BasicTable
          [:id    {:type :uuid,
                   :primary true,
                   :sql {:default (rt.postgres/uuid-generate-v4)},
                   :scope :-/id}
           :status {:type :enum,
                    :sql {:default "pending"},
                    :enum {:ns rt.postgres.script.scratch/EnumStatus},
                    :scope :-/data}
           :task   {:type :ref,
                    :ref
                    {:ns :Task,
                     :link
                     {:id Task,
                      :module rt.postgres.script.scratch,
                      :lang :postgres,
                      :section :code}},
                    :scope :-/ref}]
        {:constraints {:not-admin [(not= #{"name"} "admin")]}}))

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-hydrate-hook :added "4.0"}
(fact "updates the application schema")


^{:refer rt.postgres.grammar.form-deftype/pg-deftype-ref-link :added "4.0"}
(fact "TODO")

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-ref-current :added "4.0"}
(fact "TODO")