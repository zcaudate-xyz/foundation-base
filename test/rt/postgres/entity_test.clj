(ns rt.postgres.entity-test
  (:use code.test)
  (:require [rt.postgres.entity :refer :all]
            [rt.postgres.entity-util :as ut]
            [std.lib :as h]
            [std.lang :as l]
            [std.lang.base.grammar-spec :as grammar-spec]))

(def +dummy-atom+ (atom {:api/input {:class :1d/base}}))

^{:refer rt.postgres.entity/E-check-input :added "4.1"}
(fact "checks E input"
  (E-check-input {:class :1d/base})
  => {:class :1d/base}

  (E-check-input {:class :invalid})
  => (throws))

^{:refer rt.postgres.entity/E-entity-class-fields :added "4.1"}
(fact "generates entity class fields"
  (E-entity-class-fields {:class :1d/base :entity {:for 'Table}})
  => (contains {:class-table (contains {:foreign {:table {:ns 'Table :column :class-table}}})
                :table {:type :ref :required true :ref {:ns 'Table}
                        :sql {:cascade true :unique ["table"]}}})

  (E-entity-class-fields {:class :2d/log :entity {:for 'Table}})
  => (contains {:table (contains {:primary "default"})}))

^{:refer rt.postgres.entity/E-addon-columns-single :added "4.1"}
(fact "generates single addon columns"
  (with-redefs [ut/get-addon (fn [k] {:field {:type :text} :key k})]
    (E-addon-columns-single :a)
    => {:field {:type :text} :key :a}

    (E-addon-columns-single [:a :Ref 10])
    => {:key :a :field {:type :ref :required true :ref {:ns :Ref}} :priority 10}))

^{:refer rt.postgres.entity/E-addon-columns-match :added "4.1"}
(fact "matches addon columns"
  (with-redefs [resolve (constantly #'+dummy-atom+)
                ut/get-addon (constantly {:field {:type :ref}})]

    (E-addon-columns-match :1d/entry {:key :a :field {:type :ref :ref {:ns :Ref}}})
    => (contains {:class-table {:foreign {:a {:ns :Ref :column :class-table}}}})))

^{:refer rt.postgres.entity/E-addon-columns :added "4.1"}
(fact "generates all addon columns"
  (with-redefs [E-addon-columns-single (fn [x] {:key x :field {:type :text} :priority 10})
                E-addon-columns-match (constantly {})]
    (E-addon-columns {:class :1d/base :addons [:a :b]})
    => [{:a {:type :text :priority 10} :b {:type :text :priority 10}}
        {}]))

^{:refer rt.postgres.entity/E-class-link-columns :added "4.1"}
(fact "generates class link columns"
  (E-class-link-columns {:class :1d/entry :link {:for 'Table}})
  => (contains {:class-table {:foreign {:table {:ns 'Table :column :class-table}}}
                :table {:type :ref :required true :ref {:ns 'Table} :sql {:cascade true :unique ["table"]}}})

  (E-class-link-columns {:class :2d/entry :link {:for 'Table}})
  => (contains {:class-context {:foreign {:table {:ns 'Table :column :class-context}}}}))

^{:refer rt.postgres.entity/E-class-columns :added "4.1"}
(fact "generates class columns"
  (E-class-columns {:class :1d/base :entity {:context "Ctx"}})
  => (contains {:class-table {:primary "default" :sql {:unique ["class"]}
                              :type :enum :scope :-/hidden :priority 1
                              :enum {:ns '-/EnumClassType}}
                :class-context {:generated "Ctx"
                                :type :enum :scope :-/hidden :priority 3
                                :enum {:ns '-/EnumClassType}}}))

^{:refer rt.postgres.entity/E-class-merge :added "4.1"}
(fact "merges class columns"
  (E-class-merge {:class :1d/base} {} {} {} {})
  => {}

  (E-class-merge {:class :1d/simple} {} {} {} {})
  => (fn [m] (not (get-in m [:class-context :sql]))))

^{:refer rt.postgres.entity/E-main-track :added "4.1"}
(fact "generates main track columns"
  (E-main-track {:class :1d/base})
  => (contains [{:id {:type :uuid :primary "default" :priority 0 :sql {:default '(rt.postgres/uuid-generate-v4)}}}
                {:op-created {:type :uuid :priority 200 :priority-index 0} :time-created {:type :time :priority 200 :priority-index 1}}
                {:name "log" :in {:create {:op-created :id :time-created :time}} :disable #{:modify} :ignore #{:delete}}]))

^{:refer rt.postgres.entity/E-main :added "4.1"}
(fact "generates main entity structure"
  (with-redefs [ut/default-ns-str (constantly "schema")
                ut/type-class (constantly {})
                ut/type-class-ref (constantly {})]
    (E-main {:class :1d/base :entity {:for 'Table} :track :track/log :ns-str "schema" :access :access/auth})
    => (contains {:api/meta {:sb/rls true :sb/access {:admin :all :auth :select :anon :select}}
                  :public true
                  :track map?
                  :raw vector?})))

^{:refer rt.postgres.entity/E-main-spec :added "4.1"}
(fact "handles main spec"
  (with-redefs [grammar-spec/*symbol* 'Sym
                h/ns-sym (constantly 'ns)
                ut/add-addon (fn [k ref p] [k ref p])]
    (E-main-spec {:spec/addon {:key :k :priority 10}})
    => [:k {:type :ref :required true :priority nil :ref {:ns 'ns/Sym}} 10]))

^{:refer rt.postgres.entity/E :added "4.1"}
(fact "top level E function"
  (with-redefs [ut/default-ns-str (constantly "schema")
                ut/type-class (constantly {})
                ut/type-class-ref (constantly {})]
    (E {:class :1d/base :entity {:for 'Table} :track :track/log})
    => (contains {:api/meta {:sb/rls true, :sb/access {:admin :all, :auth :select, :anon :select}} :raw vector?})))
