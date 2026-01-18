(ns rt.postgres.entity-test
  (:use code.test)
  (:require [rt.postgres.entity :refer :all]
            [rt.postgres.entity-util :as ut]))

^{:refer rt.postgres.entity/E-check-input :added "4.1"}
(fact "checks input for E"
  (E-check-input {:id :id/v1 :class :none})
  => {:id :id/v1 :class :none}

  (E-check-input {:id :id/invalid :class :none})
  => (throws))

^{:refer rt.postgres.entity/E-entity-class-fields :added "4.1"}
(fact "generates fields for entity class"
  (E-entity-class-fields {:class :2d/base :entity {:for 'user}})
  => (contains {:user (contains {:type :ref})
                :class-table {:foreign {:user {:ns 'user, :column :class-table}}}
                :class-context {:foreign {:user {:ns 'user, :column :class-context}}}})

  (E-entity-class-fields {:class :1d/log :entity {:for 'user}})
  => (contains {:user (contains {:type :ref})
                :class-table {:foreign {:user {:ns 'user, :column :class-table}}}}))

^{:refer rt.postgres.entity/E-addon-columns-single :added "4.1"}
(fact "generates single addon columns"
  (E-addon-columns-single [:key :ref 10])
  => (contains {:key :key :priority 10 :field {:type :ref :required true :ref {:ns :ref}}})

  (with-redefs [ut/get-addon (fn [k] {:key k :field {:type :ref}})]
    (E-addon-columns-single :my-addon))
  => (contains {:key :my-addon :field {:type :ref}}))

^{:refer rt.postgres.entity/E-addon-columns-match :added "4.1"}
(fact "matches addon columns based on class"
  (with-redefs [resolve (fn [x] (atom (atom {:api/input {:class :2d/base}})))]
    (E-addon-columns-match :1d/entry {:key :user :field {:ref {:ns 'User}}})
    => (contains {:class-table {:foreign {:user {:ns 'User :column :class-context}}}
                  :class-link {:foreign {:user {:ns 'User :column :class-table}}}})))

^{:refer rt.postgres.entity/E-addon-columns :added "4.1"}
(fact "generates addon columns"
  (with-redefs [resolve (fn [x] (atom (atom {:api/input {:class :2d/base}})))]
    (E-addon-columns {:class :1d/entry :addons [[:user 'User 10]]}))
  => vector?)

^{:refer rt.postgres.entity/E-class-link-columns :added "4.1"}
(fact "generates link columns"
  (E-class-link-columns {:class :1d/entry :link {:for 'user}})
  => (contains {:user (contains {:type :ref :required true :ref {:ns 'user} :sql {:cascade true :unique ["user"]}})})

  (E-class-link-columns {:class :2d/entry :link {:for 'user}})
  => (contains {:user (contains {:type :ref :required true :ref {:ns 'user} :sql {:cascade true}})
                :class-context {:foreign {:user {:ns 'user, :column :class-context}}}}))

^{:refer rt.postgres.entity/E-class-columns :added "4.1"}
(fact "generates class columns"
  (E-class-columns {:class :0d/entry :symname "Global" :entity {:context "Global"}})
  => (contains {:class-table (contains {:generated "Global"})
                :class-context (contains {:generated "Global"})})

  (E-class-columns {:class :1d/base :entity {:context "Global"}})
  => (contains {:class-table (contains {:primary "default"})
                :class-context (contains {:generated "Global"})}))

^{:refer rt.postgres.entity/E-class-merge :added "4.1"}
(fact "merges class columns"
  (E-class-merge {:class :none :columns []} {:id {:type :uuid}} {} {} {})
  => (contains {:id {:type :uuid}}))

^{:refer rt.postgres.entity/E-main-track :added "4.1"}
(fact "generates main track columns"
  (E-main-track {:class :none :track :track/none})
  => [nil {} {}]

  (E-main-track {:class :log :track :track/log :id :id/v1})
  => vector?)

^{:refer rt.postgres.entity/E-main :added "4.1"}
(fact "main entity function"
  (E-main {:id :id/none :class :none :track :track/none :access :none :ns-str "test"})
  => (contains {:api/meta nil :public false :raw vector?}))

^{:refer rt.postgres.entity/E-main-spec :added "4.1"}
(fact "adds addon to spec"
  (E-main-spec {:spec {:addon {:key :test :priority 10}}})
  => any?)

^{:refer rt.postgres.entity/E :added "4.1"}
(fact "entry point for entity definition"
  (E {:id :id/none :class :none :track :track/none :access :none :ns-str "test"})
  => map?)
