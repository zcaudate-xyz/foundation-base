(ns rt.postgres.entity-v2-test
  (:use code.test)
  (:require [rt.postgres.entity-v2 :as et :refer :all]
            [rt.postgres.entity-util :as ut]
            [std.lang.base.grammar-spec :as grammar-spec]
            [std.lib.env :as env]))

(def +reduced-target+
  (atom {:api/input {:class :1d/base
                     :basis #{:table :id}}}))

(def +full-target+
  (atom {:api/input {:class :2d/base
                     :basis #{:table :context :id}}}))

(defn test-resolve
  [sym]
  (case sym
    Rev    #'+full-target+
    Social #'+reduced-target+
    nil))

(defn test-addons
  []
  {:app {:rev    {:key :rev
                  :field {:type :ref :required true :ref {:ns 'Rev}}
                  :priority 99}
         :social {:key :social
                  :field {:type :ref :required true :ref {:ns 'Social}}
                  :priority 63}
         :detail {:key :detail
                  :field {:type :map}
                  :priority 51}}})

(defmacro with-test-entity-v2
  [& body]
  `(with-redefs [ut/+addons+ (atom (test-addons))
                 ut/default-application (constantly :app)
                 ut/default-ns-str (constantly "schema")
                 resolve test-resolve]
     ~@body))

^{:refer rt.postgres.entity-v2/E-check-input :added "4.1"}
(fact "checks entity-v2 input"
  (E-check-input {:class :1d/base})
  => {:class :1d/base}

  (E-check-input {:addons [:rev :detail]})
  => {:addons [:rev :detail]}

  (E-check-input {:class :1d/simple})
  => (throws)

  (E-check-input {:class :0d/data})
  => (throws))

(fact "resolves minimal and expanded bases from relation requirements"
  (with-test-entity-v2
    (#'et/E-resolve-basis {:class :1d/base})
    => #{:table :id}

    (#'et/E-resolve-basis {:class :1d/base
                           :addons [:rev]})
    => #{:table :context :id}

    (#'et/E-resolve-basis {:class :1d/entry
                           :link {:for 'Social}})
    => #{:table}

    (#'et/E-resolve-basis {:class :1d/entry
                           :addons [:rev]})
    => #{:table :context :link}

    (#'et/E-resolve-basis {:class :0d/entry})
    => #{}

    (#'et/E-resolve-basis {:class :0d/entry
                           :addons [:rev]})
    => #{:table :context}))

^{:refer rt.postgres.entity-v2/E :added "4.1"}
(fact "normalizes boolean addon shorthand into canonical addon input"
  (with-test-entity-v2
    (E {:class :0d/entry
        :rev true
        :detail false})
    => (contains-in {:api/input {:class :0d/entry
                              :basis #{:table :context}
                              :addons [:rev]}})))

^{:refer rt.postgres.entity-v2/E :added "4.1"}
(fact "produces reduced table-only and linked-entry shapes"
  (with-test-entity-v2
    (let [base-cols  (map first (:raw (E {:class :1d/base})))
          entry-cols (map first (:raw (E {:class :1d/entry
                                          :link {:for 'Social}})))]
      base-cols => (contains [:class-table :class-ref])
      base-cols => (fn [xs]
                     (not (some #{:class-context} xs)))

      entry-cols => (contains [:class-table :social])
      entry-cols => (fn [xs]
                      (and (not (some #{:class-context} xs))
                           (not (some #{:class-link} xs)))))))

^{:refer rt.postgres.entity-v2/E :added "4.1"}
(fact "upgrades to expanded contextual projections when needed"
  (with-test-entity-v2
    (let [entry-cols (map first (:raw (E {:class :1d/entry
                                          :addons [:rev]})))
          zero-cols  (map first (:raw (E {:class :0d/entry
                                          :addons [:rev]})))
          two-cols   (map first (:raw (E {:class :2d/base})))]
      entry-cols => (contains [:class-table :class-link :class-context :rev])
      zero-cols => (contains [:class-table :class-context :rev])
      two-cols => (contains [:class-table :class-context :class-ref]))))

(fact "encodes the 1d-entry to 2d-base projection through class-link"
  (with-test-entity-v2
    (#'et/plan-addon-relation {:class :1d/entry
                               :basis #{:table :context :link}
                               :addons [:rev]}
                              {:key :rev
                               :field {:type :ref :ref {:ns 'Rev}}})
    => {:class-table {:foreign {:rev {:ns 'Rev :column :class-context}}}
        :class-link {:foreign {:rev {:ns 'Rev :column :class-table}}}}))

^{:refer rt.postgres.entity-v2/E-main-spec :added "4.1"}
(fact "handles main spec addons the same way as v1"
  (with-redefs [grammar-spec/*symbol* 'Sym
                env/ns-sym (constantly 'ns)
                ut/add-addon (fn [k ref p] [k ref p])]
    (E-main-spec {:spec/addon {:key :k :priority 10}})
    => [:k {:type :ref :required true :priority nil :ref {:ns 'ns/Sym}} 10]))


^{:refer rt.postgres.entity-v2/E-addon-columns-single :added "4.1"}
(fact "normalizes keyword, vector, and map addon declarations"
  (with-test-entity-v2
    (E-addon-columns-single :rev)
    => {:key :rev
        :field {:type :ref
                :required true
                :ref {:ns 'Rev}}
        :priority 99}

    (E-addon-columns-single [:flag 'Rev 11 {:field {:required false}}])
    => {:key :flag
        :field {:type :ref
                :required false
                :ref {:ns 'Rev}}
        :priority 11}

    (E-addon-columns-single {:key :social
                             :ref 'Rev
                             :priority 77})
    => {:key :social
        :field {:type :ref
                :required true
                :ref {:ns 'Rev}}
        :priority 77}))

^{:refer rt.postgres.entity-v2/E-class-columns :added "4.1"}
(fact "emits structural class columns from the resolved basis"
  (let [base-cols (E-class-columns {:class :1d/base
                                    :basis #{:table :id}
                                    :ns-str "schema"})
        entry-cols (E-class-columns {:class :1d/entry
                                     :basis #{:table :context :link}
                                     :ns-str "schema"
                                     :symname "Asset"
                                     :entity {:context "Scoped"}})]
    (select-keys (get base-cols :class-table) [:type :scope :primary])
    => {:type :enum
        :scope :-/hidden
        :primary "default"}

    (select-keys (get base-cols :class-ref) [:type :required])
    => {:type :uuid
        :required true}

    (contains? base-cols :class-context)
    => false

    (select-keys (get entry-cols :class-table) [:type :scope :primary])
    => {:type :enum
        :scope :-/hidden
        :primary "default"}

    (select-keys (get entry-cols :class-link) [:type :scope :generated])
    => {:type :enum
        :scope :-/hidden
        :generated "Asset"}

    (select-keys (get entry-cols :class-context) [:type :scope :generated])
    => {:type :enum
        :scope :-/hidden
        :generated "Scoped"}))

^{:refer rt.postgres.entity-v2/E-addon-columns :added "4.1"}
(fact "splits addon payload fields from projected support-column wiring"
  (with-test-entity-v2
    (let [[addon-cols addon-class-cols]
          (E-addon-columns {:class :1d/entry
                            :basis #{:table :context :link}
                            :addons [:rev :detail]})]
      addon-cols
      => (contains {:rev {:type :ref
                          :required true
                          :ref {:ns 'Rev}
                          :priority 99
                          :sql {:unique ["rev"]}}
                    :detail {:type :map
                             :priority 51}})

      addon-class-cols
      => {:class-table {:foreign {:rev {:ns 'Rev :column :class-context}}}
          :class-link {:foreign {:rev {:ns 'Rev :column :class-table}}}})))

^{:refer rt.postgres.entity-v2/E-class-merge :added "4.1"}
(fact "propagates discovered uniqueness onto structural coordinates"
  (let [one-d (E-class-merge {:class :1d/entry
                              :basis #{:table}}
                             {}
                             {}
                             {:class-table {:type :enum}}
                             {:social {:type :ref
                                       :sql {:unique ["social"]}}})
        two-d (E-class-merge {:class :2d/base
                              :basis #{:table :context :id}}
                             {}
                             {}
                             {:class-table {:type :enum}
                              :class-context {:type :enum}}
                             {:rev {:type :ref
                                    :sql {:unique ["rev"]}}})]
    (get-in one-d [:class-table :sql :unique])
    => ["social"]

    (contains? (get one-d :class-context {}) :sql)
    => false

    (get-in two-d [:class-table :sql :unique])
    => ["rev"]

    (get-in two-d [:class-context :sql :unique])
    => ["rev"]))

^{:refer rt.postgres.entity-v2/E-main-track :added "4.1"}
(fact "selects id and tracking defaults from the public role"
  (let [[entry-id entry-cols entry-track]
        (E-main-track {:class :1d/entry
                       :ns-str "schema"})
        [log-id log-cols log-track]
        (E-main-track {:class :2d/log
                       :ns-str "schema"})]
    entry-id
    => {:id (ut/type-id-v4)}

    (set (keys entry-cols))
    => #{:op-created :op-updated :time-created :time-updated}

    entry-track
    => (ut/get-tracking :track/data)

    log-id
    => {:id (ut/type-id-v1)}

    (set (keys log-cols))
    => #{:op-created :time-created}

    log-track
    => (ut/get-tracking :track/log)))

^{:refer rt.postgres.entity-v2/E-main :added "4.1"}
(fact "builds the final entity payload from a resolved basis"
  (with-test-entity-v2
    (let [out (E-main {:class :1d/entry
                       :basis #{:table}
                       :ns-str "schema"
                       :access :access/hidden
                       :addons [:detail]
                       :link {:for 'Social}
                       :columns {:note {:type :text}}})
          raw-cols (map first (:raw out))]
      (:track out)
      => (ut/get-tracking :track/data)

      (:public out)
      => false

      (:api/meta out)
      => (ut/get-access :access/hidden)

      (:api/input out)
      => (contains {:class :1d/entry
                    :basis #{:table}
                    :addons [:detail]
                    :link {:for 'Social}})

      (set raw-cols)
      => #{:id
           :class-table
           :social
           :detail
           :note
           :op-created
           :op-updated
           :time-created
           :time-updated})))
