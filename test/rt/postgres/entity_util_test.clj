(ns rt.postgres.entity-util-test
  (:use code.test)
  (:require [rt.postgres.entity-util :as ut]))

^{:refer rt.postgres.entity-util/default-application :added "4.1"}
(fact "gets or sets the default application"
  (with-redefs [ut/+app+ (atom {})
                std.lang/rt:module (constantly nil)]
    (ut/default-application 'my.app))
  => nil)

^{:refer rt.postgres.entity-util/default-ns-str :added "4.1"}
(fact "gets the default ns string"
  (with-redefs [ut/+app+ (atom {'my.app "my-app"})]
    (ut/default-ns-str 'my.app)
    => "my-app"))

^{:refer rt.postgres.entity-util/init-default-ns-str :added "4.1"}
(fact "initializes the default ns string"
  (with-redefs [ut/+app+ (atom {})]
    (ut/init-default-ns-str 'my.app "my-app")
    @ut/+app+
    => {'my.app "my-app"}))

^{:refer rt.postgres.entity-util/type-id-v1 :added "4.1"}
(fact "generates uuid v1 type"
  (ut/type-id-v1)
  => {:type :uuid :primary "default" :priority 0
      :sql {:default '(rt.postgres/uuid-generate-v1)}})

^{:refer rt.postgres.entity-util/type-id-v4 :added "4.1"}
(fact "generates uuid v4 type"
  (ut/type-id-v4)
  => {:type :uuid :primary "default" :priority 0
      :sql {:default '(rt.postgres/uuid-generate-v4)}})

^{:refer rt.postgres.entity-util/type-id-text :added "4.1"}
(fact "generates text id type"
  (ut/type-id-text "schema")
  => (contains {:type :citext :primary "default"}))

^{:refer rt.postgres.entity-util/type-name :added "4.1"}
(fact "generates name type"
  (ut/type-name "schema")
  => (contains {:type :citext :required true :scope :-/info}))

^{:refer rt.postgres.entity-util/type-code :added "4.1"}
(fact "generates code type"
  (ut/type-code "schema")
  => (contains {:type :citext :scope :-/info}))

^{:refer rt.postgres.entity-util/type-image :added "4.1"}
(fact "generates image type"
  (ut/type-image "schema")
  => (contains {:type :image}))

^{:refer rt.postgres.entity-util/type-color :added "4.1"}
(fact "generates color type"
  (ut/type-color "schema")
  => (contains {:type :citext :required true}))

^{:refer rt.postgres.entity-util/type-tags :added "4.1"}
(fact "generates tags type"
  (ut/type-tags "schema")
  => (contains {:type :array :scope :-/info}))

^{:refer rt.postgres.entity-util/type-log :added "4.1"}
(fact "generates log type"
  (ut/type-log "schema")
  => (contains {:type :array :required true :scope :-/detail}))

^{:refer rt.postgres.entity-util/type-log-entry :added "4.1"}
(fact "generates log entry type"
  (ut/type-log-entry "schema")
  => (contains {:type :map :required true :scope :-/detail}))

^{:refer rt.postgres.entity-util/type-detail :added "4.1"}
(fact "generates detail type"
  (ut/type-detail "schema")
  => (contains {:type :map :required true :scope :-/detail}))

^{:refer rt.postgres.entity-util/type-boolean :added "4.1"}
(fact "generates boolean type"
  (ut/type-boolean true)
  => (contains {:type :boolean :required true :sql {:default true}}))

^{:refer rt.postgres.entity-util/type-class :added "4.1"}
(fact "generates class type"
  (ut/type-class "schema")
  => (contains {:type :enum :scope :-/hidden}))

^{:refer rt.postgres.entity-util/type-ref :added "4.1"}
(fact "generates ref type"
  (ut/type-ref "schema" "table")
  => (contains {:type :ref :required true :ref {:ns 'schema/table}}))

^{:refer rt.postgres.entity-util/type-class-ref :added "4.1"}
(fact "generates class ref type"
  (ut/type-class-ref)
  => (contains {:type :uuid :required true}))

^{:refer rt.postgres.entity-util/normalise-ref :added "4.1"}
(fact "normalises reference"
  (ut/normalise-ref 'my.ns.User)
  => 'my.ns.User

  (ut/normalise-ref #'clojure.core/str)
  => 'clojure.core/str

  (ut/normalise-ref {:module :my.ns :id :User})
  => nil)

^{:refer rt.postgres.entity-util/default-fields :added "4.1"}
(fact "generates default fields"
  (keys (ut/default-fields "schema"))
  => (contains [:name :code :color :title :description] :in-any-order :gaps-ok))

^{:refer rt.postgres.entity-util/init-addons :added "4.1"}
(fact "initializes addons"
  (with-redefs [ut/+addons+ (atom {})]
    (ut/init-addons {} 'my.app)
    @ut/+addons+
    => (contains {'my.app map?})))

^{:refer rt.postgres.entity-util/get-addon :added "4.1"}
(fact "gets an addon"
  (with-redefs [ut/+addons+ (atom {(ut/default-application) {:test {:field :val}}})]
    (ut/get-addon :test)
    => {:key :test :field :val}))

^{:refer rt.postgres.entity-util/add-addon :added "4.1"}
(fact "adds an addon"
  (with-redefs [ut/+addons+ (atom {})]
    (ut/add-addon :test {:type :int} 10)
    (get-in @ut/+addons+ [(ut/default-application) :test])
    => {:field {:type :int} :priority 10}))

^{:refer rt.postgres.entity-util/addons-remove :added "4.1"}
(fact "removes an addon"
  (with-redefs [ut/+addons+ (atom {(ut/default-application) {:test {:field :val}}})]
    (ut/addons-remove :test)
    (get-in @ut/+addons+ [(ut/default-application) :test])
    => nil))

^{:refer rt.postgres.entity-util/get-tracking :added "4.1"}
(fact "gets tracking configuration"
  (ut/get-tracking :track/log)
  => (contains {:name "log" :in map?}))

^{:refer rt.postgres.entity-util/get-tracking-columns :added "4.1"}
(fact "gets tracking columns"
  (ut/get-tracking-columns :track/log)
  => [:op-created {:type :uuid} :time-created {:type :time}])

^{:refer rt.postgres.entity-util/get-access :added "4.1"}
(fact "gets access configuration"
  (ut/get-access :access/public)
  => (contains {:sb/rls true :sb/access (contains {:auth :select :anon :select})}))

^{:refer rt.postgres.entity-util/fill-priority :added "4.1"}
(fact "fills priority in array"
  (ut/fill-priority [{:a 1} {:b 2}] 10)
  => [{:a 1 :priority 10 :priority-index 0} {:b 2 :priority 10 :priority-index 1}])
