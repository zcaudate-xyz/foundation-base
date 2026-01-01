(ns rt.postgres.grammar.form-deftype-test
  (:use code.test)
  (:require [rt.postgres.grammar.form-deftype :refer :all]
            [rt.postgres.grammar.form-deftype-hydrate :as hydrate]
            [rt.postgres.grammar :as g]
            [rt.postgres.script.scratch :as scratch]
            [std.lang :as l]
            [rt.postgres.grammar.common :as common]
            [std.lang.base.library-snapshot :as snap]
            [std.lang.base.book :as book]
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
  => [:generated :always :as ''(* w h) :stored])

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
  => '(do [:drop-table :if-exists "s.t" :cascade] [:create-table :if-not-exists "s.t" \( \\ (\| []) \\ \)])

  (fact "pg-deftype renders partition logic"
    (with-redefs [common/pg-full-token (fn [s sch] (str sch "." s))]
      (let [form '(deftype ^{:static/schema "s"} t
                           [[:class {:type :text}]]
                           {:partition-by {:strategy :list :columns [:class]
                                           :default {:in "schema_type_impl"
                                                     :name "$DEFAULT"}}})
            res (pg-deftype form)]
        (last res)))
    => [:create-table :if-not-exists '(. #{"schema_type_impl"} #{"t__$DEFAULT"})
        :partition-of "s.t" :default]))

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-fragment :added "4.0"}
(fact "parses the fragment contained by the symbol"
  ;; Requires resolving symbol
  )

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-format :added "4.0"}
(fact "formats an input form"
  ^:hidden
  
  (pg-deftype-format '(deftype t [:a {:type :int}] {}))
  => vector?)

^{:refer rt.postgres.grammar.form-defpartition/pg-deftype-partition :added "4.1"}
(fact "creates partition by statement"
  ^:hidden
  
  (pg-deftype-partition {:partition-by [:range :abc-def]}
                                          [[:abc-def {:type :time}]])
  => '(:partition-by :range (quote (#{"abc_def"})))

  (let [colspec [[:id {:type :uuid, :primary "default", :sql {:default '(uuid-generate-v4)}, :scope :-/id}]
                 [:class {:type :enum, :required true, :scope :-/info, :primary "default", :enum {:ns 'szndb.core.type-seed/EnumClassType}, :sql {:unique ["class"]}}]
                 [:class-table {:type :enum, :required true, :scope :-/info, :primary "primary", :enum {:ns 'szndb.core.type-seed/EnumTableType}, :sql {:unique ["class"]}}]
                 [:class-ref {:type :uuid, :required true, :sql {:unique ["class"]}, :scope :-/data}]
                 [:index {:type :integer, :required true, :scope :-/info, :sql {:default 0}}]
                 [:current {:type :map, :sql {:default "{}"}, :scope :-/data}]
                 [:op-created {:type :uuid, :scope :-/data}]
                 [:op-updated {:type :uuid, :scope :-/data}]
                 [:time-created {:type :time, :scope :-/data}]
                 [:time-updated {:type :time, :scope :-/data}]]]
    (pg-deftype-partition {:partition-by {:strategy :list :columns [:class]}}
                                            colspec))
  => '(:partition-by :list (quote (#{"class"})))

  (fact "pg-deftype-partition handles sets in column list"
    (pg-deftype-partition {:partition-by [:list #{"class"}]} [])
    => '(:partition-by :list (quote (#{"class"}))))

  (fact "pg-deftype-partition handles map format"
    (pg-deftype-partition {:partition-by {:strategy :list :columns [:class]}} [])
    => '(:partition-by :list (quote (#{"class"}))))

  (fact "pg-deftype-partition should error if column is not found"
    (pg-deftype-partition {:partition-by {:strategy :list :columns [:wrong-column]}} 
                                            [[:class {:type :text}]])
    => (throws)))

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-spec-normalize :added "4.1"}
(fact "normalizes the spec, inferring groups"
  (let [spec [[:class       {:foreign {:rev {:ns :Rev :column :class :link {:id :Rev}}}}]
              [:class-table {:foreign {:rev {:ns :Rev :column :class-table :link {:id :Rev}}}}]
              [:rev         {:type :ref :ref {:ns :Rev :link {:id :Rev}}}]]]
    (second (last (pg-deftype-spec-normalize spec))))
  => (contains {:ref (contains {:group :rev})}))

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-col-fn :added "4.0"}
(fact "formats the column on deftype"
  ^:hidden
  
  (with-redefs [common/pg-type-alias (fn [x] x)]
    (pg-deftype-col-fn [:col {:type :uuid :primary true}] {}))
  => (contains [:uuid :primary-key])

  (with-redefs [snap/get-book (constantly {})
                book/get-base-entry (constantly {:form [nil nil [:id {:type :uuid}]]
                                                 :static/schema "schema"})]
    (set (pg-deftype-col-fn 
          [:rev {:type :ref :ref {:group :rev :ns :Rev} :sql {:cascade true}}]
          {})))
  => (fn [s] (not (contains? s :on-delete-cascade)))

  
  (with-redefs [snap/get-book (constantly {})
                book/get-base-entry (constantly {:form [nil nil [:id {:type :uuid}]]
                                                 :static/schema "schema"})]
    (set (pg-deftype-col-fn 
          [:rev {:type :ref :ref {:ns :Rev} :sql {:cascade true}}]
          {})))
  => (fn [s] (contains? s :on-delete-cascade)))

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-foreign-groups :added "4.1"}
(fact "collects foreign key groups"
  ^:hidden

  (pg-deftype-foreign-groups
   [[:u {:type :ref :ref {:group :g1 :ns :user :link {:id :user}}}]
    [:a {:type :text :foreign {:g1 {:column :uid :ns :user :link {:id :user}}}}]])
  => {:g1 '({:local-col "u_id" :remote-col :id :ns :user :link {:id :user} :cascade nil}
            {:local-col "a"    :remote-col :uid :ns :user :link {:id :user} :cascade nil})}

  (pg-deftype-foreign-groups
   [[:rev {:type :ref :ref {:group :rev :ns :Rev :link {:id :Rev}}
           :sql {:cascade true}}]])
  => (contains {:rev (contains [(contains {:cascade true})])}))

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-gen-constraint :added "4.1"}
(fact "generates a foreign key constraint"
  ^:hidden

  (pg-deftype-gen-constraint
   'mytable
   [:g1 [{:local-col "u_id" :remote-col :id :ns :user :link {:id :users}}]]
   {})
  => '(% [:constraint fk_mytable_g1
          :foreign-key (quote (u_id))
          :references #{"users"} (quote (id))])

  (let [res (pg-deftype-gen-constraint
             'RevLog
             [:rev [{:local-col "rev_id" :remote-col :id :ns :Rev :link {:id :Rev} :cascade true}]]
             {})]
    (some #{:on-delete-cascade} (second res)))
  => :on-delete-cascade)

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-primaries :added "4.1"}
(fact "TODO")

^{:refer rt.postgres.grammar.form-deftype/pg-deftype-foreigns :added "4.1"}
(fact "TODO")
