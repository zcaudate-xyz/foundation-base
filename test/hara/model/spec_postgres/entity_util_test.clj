tahto/model/spec_postgres/entity_util_test.clj:1:(ns tahto.model.spec-postgres.entity-util-test
tahto/model/spec_postgres/entity_util_test.clj:2:  (:require [tahto.model.spec-postgres.entity-util :refer :all])
  (:use code.test))

(defn with-demo-app
  [f]
  (with-redefs [default-application (fn [& _] :app/demo)]
    (reset! +app+ {:app/demo "demo.ns"})
    (reset! +addons+ {:app/demo {:name {:field {:type :text}
                                        :priority 1}}})
    (f)))

tahto/model/spec_postgres/entity_util_test.clj:13:^{:refer tahto.model.spec-postgres.entity-util/default-application :added "4.1"}
(fact "default-application is resolved via the runtime module metadata"
  (with-redefs [default-application (fn [& _] :app/demo)]
    (default-application))
  => :app/demo)

tahto/model/spec_postgres/entity_util_test.clj:19:^{:refer tahto.model.spec-postgres.entity-util/default-ns-str :added "4.1"}
(fact "default-ns-str reads the configured app namespace"
  (with-demo-app
    #(default-ns-str :app/demo))
  => "demo.ns")

tahto/model/spec_postgres/entity_util_test.clj:25:^{:refer tahto.model.spec-postgres.entity-util/init-default-ns-str :added "4.1"}
(fact "init-default-ns-str stores the namespace string for an application"
  (with-demo-app
    #(do (init-default-ns-str :app/other "other.ns")
         @+app+))
  => {:app/demo "demo.ns"
      :app/other "other.ns"})

tahto/model/spec_postgres/entity_util_test.clj:33:^{:refer tahto.model.spec-postgres.entity-util/type-id-v1 :added "4.1"}
(fact "type-id-v1 uses a generated v1 uuid default"
  (type-id-v1)
  => {:type :uuid
      :primary "default"
      :priority 0
      :sql {:default '(postgres.core/uuid-generate-v1)}})

tahto/model/spec_postgres/entity_util_test.clj:41:^{:refer tahto.model.spec-postgres.entity-util/type-id-v4 :added "4.1"}
(fact "type-id-v4 uses a generated v4 uuid default"
  (type-id-v4)
  => {:type :uuid
      :primary "default"
      :priority 0
      :sql {:default '(postgres.core/uuid-generate-v4)}})

tahto/model/spec_postgres/entity_util_test.clj:49:^{:refer tahto.model.spec-postgres.entity-util/type-id-text :added "4.1"}
(fact "type-id-text uppercases and limits citext ids"
  (let [out (type-id-text "demo")]
    (:type out) => :citext
    (:primary out) => "default"
    (get-in out [:sql :process]) => '([demo/as-upper-formatted]
                                      [demo/as-upper-limit-length 100])))

tahto/model/spec_postgres/entity_util_test.clj:57:^{:refer tahto.model.spec-postgres.entity-util/type-name :added "4.1"}
(fact "type-name generates the default name field"
  (let [out (type-name "demo" 9)]
    [(:type out) (:required out) (:scope out) (:priority out)]
    => [:citext true :-/info 9]

    (get-in out [:sql :unique]) => ["name"]))

tahto/model/spec_postgres/entity_util_test.clj:65:^{:refer tahto.model.spec-postgres.entity-util/type-code :added "4.1"}
(fact "type-code generates the default code field"
  (let [out (type-code "demo")]
    [(:type out) (:scope out) (:priority out)]
    => [:citext :-/info 7]

    (get-in out [:sql :unique]) => ["code"]))

tahto/model/spec_postgres/entity_util_test.clj:73:^{:refer tahto.model.spec-postgres.entity-util/type-image :added "4.1"}
(fact "type-image stores images as jsonb"
  (let [out (type-image "demo")]
    [(:type out) (:priority out) (get-in out [:sql :default])]
    => [:image 10 "{}"]

    (get-in out [:profiles :web :type]) => "image"))

tahto/model/spec_postgres/entity_util_test.clj:81:^{:refer tahto.model.spec-postgres.entity-util/type-color :added "4.1"}
(fact "type-color sets color defaults and validation"
  (type-color "demo")
  => {:type :citext
      :required true
      :scope :-/info
      :priority 20
      :sql {:default '(demo/color-rand)
            :constraint '(demo/color-check #{"color"})}
      :profile {:web {:edit #{:create :modify}
                      :type "color"}}})

tahto/model/spec_postgres/entity_util_test.clj:93:^{:refer tahto.model.spec-postgres.entity-util/type-tags :added "4.1"}
(fact "type-tags stores tags as a jsonb array"
  (let [out (type-tags "demo")]
    [(:type out) (:scope out) (:priority out) (get-in out [:sql :default])]
    => [:array :-/info 25 "[]"]

    (get-in out [:profile :web :type]) => "chip"))

tahto/model/spec_postgres/entity_util_test.clj:101:^{:refer tahto.model.spec-postgres.entity-util/type-log :added "4.1"}
(fact "type-log generates the default log array shape"
  (type-log "demo")
  => (contains {:type :array
                :required true
                :priority 90
                :scope :-/detail
                :map {:status {:type :text}
                      :message {:type :text}
                      :error {:type :text}}}))

tahto/model/spec_postgres/entity_util_test.clj:112:^{:refer tahto.model.spec-postgres.entity-util/type-log-entry :added "4.1"}
(fact "type-log-entry generates the default log entry map"
  (type-log-entry "demo")
  => (contains {:type :map
                :required true
                :priority 80
                :scope :-/detail
                :map {:status {:type :text}
                      :message {:type :text}
                      :error {:type :text}}}))

tahto/model/spec_postgres/entity_util_test.clj:123:^{:refer tahto.model.spec-postgres.entity-util/type-detail :added "4.1"}
(fact "type-detail stores detail as jsonb"
  (let [out (type-detail "demo")]
    [(:type out) (:required out) (:priority out) (:scope out)]
    => [:map true 50 :-/detail]

    (get-in out [:sql :default]) => "{}"))

tahto/model/spec_postgres/entity_util_test.clj:131:^{:refer tahto.model.spec-postgres.entity-util/type-boolean :added "4.1"}
(fact "type-boolean normalizes boolean defaults"
  (type-boolean true 4)
  => {:type :boolean
      :required true
      :priority 4
      :sql {:default true}})

tahto/model/spec_postgres/entity_util_test.clj:139:^{:refer tahto.model.spec-postgres.entity-util/type-class :added "4.1"}
(fact "type-class builds the default class enum field"
  (type-class "demo")
  => {:type :enum
      :scope :-/hidden
      :priority 1
      :enum {:ns 'demo/EnumClassType}})

tahto/model/spec_postgres/entity_util_test.clj:147:^{:refer tahto.model.spec-postgres.entity-util/type-ref :added "4.1"}
(fact "type-ref builds a required reference field"
  (type-ref "demo" "Task" 3)
  => {:type :ref
      :required true
      :priority 3
      :ref {:ns 'demo/Task}})

tahto/model/spec_postgres/entity_util_test.clj:155:^{:refer tahto.model.spec-postgres.entity-util/type-class-ref :added "4.1"}
(fact "type-class-ref merges the supplied attrs with the uuid ref defaults"
  (type-class-ref {:scope :-/ref} 6)
  => {:scope :-/ref
      :type :uuid
      :required true
      :priority 6})

tahto/model/spec_postgres/entity_util_test.clj:163:^{:refer tahto.model.spec-postgres.entity-util/normalise-ref :added "4.1"}
(fact "normalise-ref preserves symbols"
  (normalise-ref 'demo.core/User)
  => 'demo.core/User)

tahto/model/spec_postgres/entity_util_test.clj:168:^{:refer tahto.model.spec-postgres.entity-util/default-fields :added "4.1"}
(fact "default-fields includes the common field presets"
  (select-keys (default-fields "demo") [:name :is-active :log])
  => (contains {:is-active {:priority 30
                            :field {:type :boolean
                                    :required true
                                    :priority nil
                                    :sql {:default true}}}})

  (get-in (default-fields "demo") [:name :field :type])
  => :citext

  (get-in (default-fields "demo") [:log :field :type])
  => :array)

tahto/model/spec_postgres/entity_util_test.clj:183:^{:refer tahto.model.spec-postgres.entity-util/init-addons :added "4.1"}
(fact "init-addons merges custom fields with the defaults"
  (with-demo-app
    #(do (init-addons {:custom {:field {:type :text}
                                :priority 2}}
                      :app/demo)
         (select-keys (get @+addons+ :app/demo) [:custom :name])))
  => (contains {:custom {:field {:type :text}
                         :priority 2}})

  (with-demo-app
    #(do (init-addons {:custom {:field {:type :text}
                                :priority 2}}
                      :app/demo)
         (get-in @+addons+ [:app/demo :name :field :type])))
  => :citext)

tahto/model/spec_postgres/entity_util_test.clj:200:^{:refer tahto.model.spec-postgres.entity-util/get-addon :added "4.1"}
(fact "get-addon returns the stored addon and echoes the key"
  (with-demo-app
    #(get-addon :name))
  => {:field {:type :text}
      :priority 1
      :key :name})

tahto/model/spec_postgres/entity_util_test.clj:208:^{:refer tahto.model.spec-postgres.entity-util/add-addon :added "4.1"}
(fact "add-addon stores a new addon entry for the current app"
  (with-demo-app
    #(do (add-addon :extra {:type :uuid} 12)
         (get-in @+addons+ [:app/demo :extra])))
  => {:field {:type :uuid}
      :priority 12})

tahto/model/spec_postgres/entity_util_test.clj:216:^{:refer tahto.model.spec-postgres.entity-util/addons-remove :added "4.1"}
(fact "addons-remove removes an addon from the current app"
  (with-demo-app
    #(do (addons-remove :name)
         (contains? (get @+addons+ :app/demo) :name)))
  => false)

tahto/model/spec_postgres/entity_util_test.clj:223:^{:refer tahto.model.spec-postgres.entity-util/get-tracking :added "4.1"}
(fact "get-tracking returns the built-in tracking presets"
  (get-tracking :track/log)
  => {:name "log"
      :in {:create {:op-created :id
                    :time-created :time}}
      :disable #{:modify}
      :ignore #{:delete}})

tahto/model/spec_postgres/entity_util_test.clj:232:^{:refer tahto.model.spec-postgres.entity-util/get-tracking-columns :added "4.1"}
(fact "get-tracking-columns returns the temp tracking columns"
  (get-tracking-columns :track/temp)
  => [:time-created {:type :time}
      :time-updated {:type :time}])

tahto/model/spec_postgres/entity_util_test.clj:238:^{:refer tahto.model.spec-postgres.entity-util/get-access :added "4.1"}
(fact "get-access returns the default public rls policy"
  (get-access :access/public)
  => {:sb/rls true
      :sb/access {:admin :all
                  :auth :select
                  :anon :select}})

tahto/model/spec_postgres/entity_util_test.clj:246:^{:refer tahto.model.spec-postgres.entity-util/fill-priority :added "4.1"}
(fact "fill-priority annotates map entries while preserving separators"
  (fill-priority [{:field {:type :text}}
                  :sep
                  {:field {:type :uuid}}]
                 10)
  => [{:field {:type :text}
       :priority 10
       :priority-index 0}
      :sep
      {:field {:type :uuid}
       :priority 10
       :priority-index 1}])
