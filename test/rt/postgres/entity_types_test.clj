(ns rt.postgres.entity-types-test
  (:use code.test)
  (:require [rt.postgres.entity-types :as et]))

^{:refer rt.postgres.entity-types/get-app :added "4.1"}
(fact "gets the app"
  ^:hidden
  
  (et/get-app)
  => "-")

^{:refer rt.postgres.entity-types/get-app-ns-str :added "4.1"}
(fact "gets the app ns string"
  ^:hidden
  
  (et/get-app-ns-str)
  => (any nil? string?))

^{:refer rt.postgres.entity-types/init-app :added "4.1"}
(fact "initializes the app"
  ^:hidden
  
  (et/init-app)
  => (any nil? map?))

^{:refer rt.postgres.entity-types/type-id-v1 :added "4.1"}
(fact "creates v1 id type"
  ^:hidden
  
  (et/type-id-v1)
  => {:type :uuid, :primary "primary", :sql {:default '(rt.postgres/uuid-generate-v1)}})

^{:refer rt.postgres.entity-types/type-id-v4 :added "4.1"}
(fact "creates v4 id type"
  ^:hidden
  
  (et/type-id-v4)
  => {:type :uuid, :primary "primary", :sql {:default '(rt.postgres/uuid-generate-v4)}})

^{:refer rt.postgres.entity-types/type-id-text :added "4.1"}
(fact "creates text id type"
  ^:hidden
  
  (et/type-id-text "demo")
  => {:type :citext,
      :primary "primary",
      :sql {:process [['demo/as-upper-formatted] ['demo/as-upper-limit-length 100]]}})

^{:refer rt.postgres.entity-types/type-name :added "4.1"}
(fact "creates name type"
  ^:hidden
  
  (et/type-name "demo")
  => {:type :citext,
      :required true,
      :scope :-/info,
      :sql {:process [['demo/as-upper-formatted] ['demo/as-upper-limit-length 36]], :unique ["name"]}})

^{:refer rt.postgres.entity-types/type-image :added "4.1"}
(fact "creates image type"
  ^:hidden
  
  (et/type-image "demo")
  => {:type :image,
      :sql {:process [['demo/as-jsonb]], :default "{}"},
      :profiles {:web {:type "image"}}})

^{:refer rt.postgres.entity-types/type-color :added "4.1"}
(fact "creates color type"
  ^:hidden
  
  (et/type-color "demo")
  => '{:type :citext, :required true,
       :scope :-/info, :sql
       {:default (demo/color-rand), :constraint (demo/color-check #{"color"})},
       :profile {:web {:edit #{:create :modify}, :type "color"}}})

^{:refer rt.postgres.entity-types/type-tags :added "4.1"}
(fact "creates tags type"
  ^:hidden
  
  (et/type-tags "demo")
  => {:type :array,
      :scope :-/info,
      :sql {:process [['demo/as-jsonb-array]], :default "[]"},
      :profile {:web {:edit #{:create :modify}, :type "chip"}}})

^{:refer rt.postgres.entity-types/type-log :added "4.1"}
(fact "creates log type"
  ^:hidden
  
  (et/type-log "demo")
  => {:type :array,
      :required true,
      :scope :-/detail,
      :sql {:process [['demo/as-jsonb-array]], :default "[]"},
      :map {:status {:type :text}, :message {:type :text}, :error {:type :text}}})

^{:refer rt.postgres.entity-types/type-boolean :added "4.1"}
(fact "creates boolean type"
  ^:hidden
  
  (et/type-boolean) => {:type :boolean, :required true, :sql {:default false}}
  (et/type-boolean true) => {:type :boolean, :required true, :sql {:default true}})

^{:refer rt.postgres.entity-types/type-class-table :added "4.1"}
(fact "creates class table type"
  ^:hidden
  
  (et/type-class-table "demo")
  => {:type :enum, :scope :-/system, :enum {:ns 'demo/EnumClassTableType}})

^{:refer rt.postgres.entity-types/type-class-context :added "4.1"}
(fact "creates class context type"
  ^:hidden
  
  (et/type-class-context "demo")
  => {:type :enum, :scope :-/system, :enum {:ns 'demo/EnumClassContextType}})

^{:refer rt.postgres.entity-types/type-ref :added "4.1"}
(fact "creates ref type"
  ^:hidden
  
  (et/type-ref "demo" "User")
  => {:type :ref, :required true, :ref {:ns 'demo/User}})

^{:refer rt.postgres.entity-types/type-class-ref :added "4.1"}
(fact "creates class ref type"
  ^:hidden
  
  (et/type-class-ref)
  => {:type :uuid, :required true})

^{:refer rt.postgres.entity-types/type-class-key :added "4.1"}
(fact "creates class key type"
  ^:hidden
  
  (et/type-class-key "demo")
  => {:type :citext,
      :required true,
      :sql {:process [['demo/as-upper-formatted] ['demo/as-upper-limit-length 100]]}})

^{:refer rt.postgres.entity-types/normalise-ref :added "4.1"}
(fact "normalises ref"
  ^:hidden
  
  (et/normalise-ref 'demo/User) => 'demo/User)

^{:refer rt.postgres.entity-types/default-fields :added "4.1"}
(fact "gets default fields"
  ^:hidden
  
  (et/default-fields "demo")
  => (contains {:id {:priority 0, :field {:type :uuid, :primary "primary", :sql {:default '(rt.postgres/uuid-generate-v4)}}}}))

^{:refer rt.postgres.entity-types/addons-init :added "4.1"}
(fact "initializes addons"
  ^:hidden
  
  (et/addons-init)
  => (any nil? map?))

^{:refer rt.postgres.entity-types/addons-add :added "4.1"}
(fact "adds addon"
  ^:hidden
  
  (et/addons-add :test {:type :text} 100)
  => map?)

^{:refer rt.postgres.entity-types/addons-remove :added "4.1"}
(fact "removes addon"
  ^:hidden
  
  (et/addons-remove :test)
  => map?)

^{:refer rt.postgres.entity-types/config-tracking :added "4.1"}
(fact "configures tracking"
  ^:hidden
  
  (et/config-tracking :track/record)
  => {:name "data",
      :in {:create {:time-created :time, :time-updated :time, :op-created :id, :op-updated :id},
           :modify {:time-updated :time, :op-updated :id}},
      :ignore #{:delete}})

^{:refer rt.postgres.entity-types/config-tracking-columns :added "4.1"}
(fact "configures tracking columns"
  ^:hidden
  
  (et/config-tracking-columns :track/record)
  => [:op-created {:type :uuid} :op-updated {:type :uuid} :time-created {:type :time} :time-updated {:type :time} :__deleted__ {:type :boolean, :scope :-/hidden, :sql {:default false}}])

^{:refer rt.postgres.entity-types/config-access :added "4.1"}
(fact "configures access"
  ^:hidden
  
  (et/config-access :access/auth)
  => {:sb/rls true, :sb/access {:admin :all, :auth :select, :anon :select}})

^{:refer rt.postgres.entity-types/with-priority :added "4.1"}
(fact "adds priority to fields"
  ^:hidden
  
  (et/with-priority [{:type :text}] 10)
  => [{:type :text, :priority 10, :priority-index 0}])

^{:refer rt.postgres.entity-types/check-E :added "4.1"}
(fact "validates E inputs"
  ^:hidden
  
  (et/check-E {:id :id/v4}) => {:id :id/v4}
  
  (et/check-E {:id :id/invalid})
  => (throws clojure.lang.ExceptionInfo)

  (et/check-E {:access :access/auth}) => {:access :access/auth}
  
  (et/check-E {:access :invalid})
  => (throws clojure.lang.ExceptionInfo))

^{:refer rt.postgres.entity-types/E-process-class-columns :added "4.1"}
(fact "processes class columns"
  ^:hidden
  
  (et/E-process-class-columns
   {:class :0d/entry
    :entity {:table "Global" :context "Global"}})
  => (contains {:class-table {:type :enum, :scope :-/system, :enum {:ns '-/EnumClassTableType}, :priority 2, :generated "Global"}
                :class-context {:type :enum, :scope :-/system, :enum {:ns '-/EnumClassContextType}, :priority 3, :generated "Global"}})

  (et/E-process-class-columns
   {:class :1d/base
    :entity {:for :user}})
  => (contains-in {:class-ref {:type :uuid, :required true, :priority 4}})
  
  (et/E-process-class-columns
   {:class :2d/base
    :entity {:for :user}})
  => (contains-in {:class-ref {:type :uuid, :required true, :priority 4}}))

^{:refer rt.postgres.entity-types/E :added "4.1"}
(fact "constructs E type"
  ^:hidden
  
  (et/E {:id :id/v4
         :access :access/auth
         :track :track/data})
  => (contains {:api/meta {:sb/rls true, :sb/access {:admin :all, :auth :select, :anon :select}}
                :public true
                :track {:name "data", :in {:create {:time-created :time, :time-updated :time, :op-created :id, :op-updated :id}, :modify {:time-updated :time, :op-updated :id}}, :ignore #{:delete}}
                :columns vector?}))
