(ns rt.postgres.grammar.form-deftype-test
  (:use code.test)
  (:require [rt.postgres.grammar.form-deftype :refer :all]
            [rt.postgres.grammar :as g]
            [rt.postgres.script.scratch :as scratch]
            [std.lang :as l]
            [rt.postgres.grammar.common :as common]
            [std.lang.base.library-snapshot :as snap]
            [std.lib :as h]))

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-enum-col :added "4.0"}
(fact "creates the enum column"
  ^:hidden

  (with-redefs [common/pg-linked-token (fn [& _] '(:enum))]
    (pg-deftype-enum-col [:col :attr] {} {}))
  => '[:col (:enum)])

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-ref-link :added "4.0"}
(fact "creates the ref entry for "
  ;; Requires book access mock
  )

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-ref-current :added "4.0"}
(fact "creates the ref entry for "
  ^:hidden

  (pg-deftype-ref-current :col {:current {:id "id" :schema "schema" :type :uuid}} {})
  => '["col_id" [:uuid] [((. #{"schema"} #{"id"}) #{"id"})]])

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-ref :added "4.0"}
(fact "creates the ref entry"
  ;; delegates
  )

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-col-sql :added "4.0"}
(fact "formats the sql on deftype"
  ^:hidden

  (pg-deftype-col-sql [] {:cascade true :default 1})
  => [:on-delete-cascade :default 1]

  (pg-deftype-col-sql [] {:generated '(* w h)})
  => [:generated :always :as ''(* w h) :stored]

  (pg-deftype-col-sql [] {:identity true})
  => [:generated :by :default :as :identity])

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-col-fn :added "4.0"}
(fact "formats the column on deftype"
  ^:hidden

  (with-redefs [common/pg-type-alias (fn [x] x)]
    (pg-deftype-col-fn [:col {:type :uuid :primary true}] {}))
  => (contains [:uuid :primary-key]))

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-uniques :added "4.0"}
(fact "collect unique keys on deftype"
  ^:hidden

  (pg-deftype-uniques [[:col {:type :text :sql {:unique true}}]])
  => '[(% [:unique (quote (#{"col"}))])])

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-indexes :added "4.0"}
(fact "create index statements"
  ^:hidden

  (pg-deftype-indexes [[:col {:type :text :sql {:index true}}]] "table")
  => '[(% [:create-index :on "table" (quote (#{"col"}))])])

^{:refer rt.postgres.grammar.form-deftype/pg-deftype :added "4.0"}
(fact "creates a deftype statement"
  ^:hidden

  (with-redefs [common/pg-full-token (fn [s sch] (str sch "." s))]
    (pg-deftype '(deftype ^{:static/schema "s"} t [] {})))
  => '(do [:drop-table :if-exists "s.t" :cascade] [:create-table :if-not-exists "s.t" \( \\ (\| []) \\ \)]))

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-fragment :added "4.0"}
(fact "parses the fragment contained by the symbol"
  ;; Requires resolving symbol
  )

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-format :added "4.0"}
(fact "formats an input form"
  (pg-deftype-format '(deftype t [:a {:type :int}] {}))
  => vector?)

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-hydrate-check-link :added "4.0"}
(fact "checks a link making sure it exists and is correct type"
  ^:hidden

  (with-redefs [snap/get-book (fn [& _] nil)
                std.lang.base.book/get-base-entry (fn [& _] {:static/dbtype :table})]
    (pg-deftype-hydrate-check-link nil {} :table))
  => true)

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-hydrate-link :added "4.0"}
(fact "resolves the link for hydration"
  (pg-deftype-hydrate-link 'sym {:id :mod} {:ns '-/sym})
  => [{:section :code, :lang :postgres, :module :mod, :id 'sym} false])

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-hydrate-process-sql :added "4.0"}
(fact "processes the sql attribute"
  (pg-deftype-hydrate-process-sql {:process 's} :k {})
  => (throws))

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-hydrate-process-foreign :added "4.0"}
(fact "processes the foreign attribute"
  (with-redefs [pg-deftype-hydrate-check-link (fn [_ _ _])]
    (pg-deftype-hydrate-process-foreign {:a {:ns :n}} (fn [_] [{:id :i} true]) nil))
  => (contains {:a (contains {:link {:id :i}})}))

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-hydrate-process-ref :added "4.0"}
(fact "processes the ref type"
  (pg-deftype-hydrate-process-ref :k {:ref [:s :i :t {}]} nil nil)
  => (just [:k (contains {:type :ref, :required true})])

  (with-redefs [pg-deftype-hydrate-check-link (fn [_ _ _])]
    (pg-deftype-hydrate-process-ref :k {:ref {:ns :n}} (fn [_] [{:id :i} true]) nil))
  => (just [:k (contains {:ref (contains {:link {:id :i}})})]))

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-hydrate-process-enum :added "4.0"}
(fact "processes the enum type"
  (with-redefs [pg-deftype-hydrate-check-link (fn [_ _ _])
                resolve (constantly (atom {:id :i :module :m :lang :l :section :s}))]
    (pg-deftype-hydrate-process-enum :k {:enum {:ns 'foo}} nil))
  => (just [:k (contains {:enum (contains {:ns 'm/i})})]))

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-hydrate-attr :added "4.0"}
(fact "hydrates a single attribute")

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-hydrate-spec :added "4.0"}
(fact "hydrates the spec")

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-hydrate :added "4.0"}
(fact "hydrates the form with linked ref information"
  ;; Complex
  )

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-hydrate-hook :added "4.0"}
(fact "updates the application schema"
  ;; side effect on atom
  )


^{:refer rt.postgres.grammar.form-deftype/pg-deftype-primaries :added "4.0"}
(fact "gets the primary keys for deftype"
  ^:hidden

  (pg-deftype-primaries [{:id "a" :type :uuid} {:id "b" :type :uuid}])
  => '[(:- [:primary-key (quote (a b))])])


^{:refer rt.postgres.grammar.form-deftype/pg-deftype-partition :added "4.1"}
(fact "creates partition by statement"
  (pg-deftype-partition {:partition-by [:range :created_at]}
                        [[:created_at {:type :long}]])
  => '(:partition-by :range (quote ("created_at")))

  (pg-deftype-partition {:partition-by [:range :user]}
                        [[:user {:type :ref :ref {:ns :user :link {:id :user}}}]])
  => '(:partition-by :range (quote ("user_id"))))

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-partition-constraints :added "4.1"}
(fact "TODO")

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-ref-name :added "4.1"}
(fact "gets the ref name"
  ^:hidden

  (pg-deftype-ref-name :user {})
  => "user_id"

  (pg-deftype-ref-name :user_account {})
  => "user_account_id"

  (pg-deftype-ref-name :user {:raw "custom_id"})
  => "custom_id")

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-foreign-groups :added "4.1"}
(fact "collects foreign key groups"
  ^:hidden

  (pg-deftype-foreign-groups
   [[:u {:type :ref :ref {:group :g1 :ns :user :link {:id :user}}}]
    [:a {:type :text :foreign {:g1 {:column :uid :ns :user :link {:id :user}}}}]])
  => {:g1 '({:local-col "u_id" :remote-col :id :ns :user :link {:id :user}}
            {:local-col "a"    :remote-col :uid :ns :user :link {:id :user}})})

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-gen-constraint :added "4.1"}
(fact "generates a foreign key constraint"
  ^:hidden

  (pg-deftype-gen-constraint
   'mytable
   [:g1 [{:local-col "u_id" :remote-col :id :ns :user :link {:id :users}}]]
   {})
  => '(% [:constraint fk_mytable_g1
          :foreign-key (quote (u_id))
          :references (#{"users"} (quote (id)))]))

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-foreigns :added "4.1"}
(fact "creates foreign key constraints"
  ^:hidden

  (pg-deftype-foreigns
   'mytable
   [[:u {:type :ref :ref {:group :g1 :ns :user :link {:id :users}}}]
    [:a {:type :text :foreign {:g1 {:column :uid :ns :user :link {:id :users}}}}]]
   {})
  => '[(% [:constraint fk_mytable_g1
           :foreign-key (quote (u_id a))
           :references (#{"users"} (quote (id uid)))])])

(fact "basic deftype emission"
  (l/emit-as :postgres
             ['(deftype
                 ^{:final true}
                 Demo
                 [:id {:type :uuid :primary true}
                  :name {:type :text}])])
  => "CREATE TABLE IF NOT EXISTS demo (\n  id uuid PRIMARY KEY,\n  name text\n);")

(fact "deftype with docstring"
  (l/emit-as :postgres
             ['(deftype
                 ^{:final true}
                 Demo
                 "A demo table"
                 [:id {:type :uuid :primary true}
                  :name {:type :text :sql {:comment "The name"}}])])
  => "CREATE TABLE IF NOT EXISTS demo (\n  id uuid PRIMARY KEY,\n  name text\n);\n\nCOMMENT ON TABLE demo IS 'A demo table';\n\nCOMMENT ON COLUMN demo.name IS 'The name';")

(fact "deftype with identity"
  (l/emit-as :postgres
             ['(deftype
                 ^{:final true}
                 Demo
                 [:id {:type :long :sql {:identity true}}
                  :name {:type :text}])])
  => "CREATE TABLE IF NOT EXISTS demo (\n  id bigint GENERATED BY DEFAULT AS IDENTITY,\n  name text\n);")

(fact "deftype with generated"
  (l/emit-as :postgres
             ['(deftype
                 ^{:final true}
                 Demo
                 [:w {:type :long}
                 :h {:type :long}
                 :area {:type :long :sql {:generated (* w h)}}])])
  => "CREATE TABLE IF NOT EXISTS demo (\n  w bigint,\n  h bigint,\n  area bigint GENERATED ALWAYS AS ((w * h)) STORED\n);")
