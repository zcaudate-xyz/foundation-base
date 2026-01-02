(ns rt.postgres.type-impl
  (:require [std.lib :as h]
            [std.lang :as l]
            [rt.postgres :as pg]))

;;
;; Default Types
;;

(defn type-id-v1
  []
  {:type :uuid :primary "primary"
   :sql {:default '(rt.postgres/uuid-generate-v1)}})

(defn type-id-v4
  []
  {:type :uuid :primary "primary"
   :sql {:default '(rt.postgres/uuid-generate-v4)}})

(defn type-id-text
  [ns-str]
  {:type :citext :primary "primary"
   :sql {:process [[(symbol ns-str "as-upper-formatted")]
                   [(symbol ns-str "as-upper-limit-length") 100]]}})

(defn type-name
  [ns-str]
  {:type :citext :required true :scope :-/info
   :sql {:process [[(symbol ns-str "as-upper-formatted")]
                   [(symbol ns-str "as-upper-limit-length") 36]]
         :unique ["name"]}})

(defn type-image
  [ns-str]
  {:type :image
   :sql  {:process [[(symbol ns-str "as-jsonb")]]
          :default "{}"}
   :profiles {:web {:type "image"}}})

(defn type-color
  [ns-str]
  {:type :citext :required true :scope :-/info
   :sql {:default    (list (symbol ns-str "color-rand"))
         :constraint (list (symbol ns-str "color-check") #{"color"})}
   :profile {:web {:edit #{:create :modify}
                   :type "color"}}})

(defn type-tags
  [ns-str]
  {:type :array :scope :-/info
   :sql {:process [[(symbol ns-str "as-jsonb-array")]]
         :default "[]"}
   :profile {:web {:edit #{:create :modify}
                   :type "chip"}}})

(defn type-log
  [ns-str]
  {:type :array :required true
   :scope :-/detail
   :sql  {:process [[(symbol ns-str "as-jsonb-array")]]
          :default "[]"}
   :map  {:status  {:type :text}
          :message {:type :text}
          :error   {:type :text}}})

(defn type-boolean
  [& [default]]
  {:type :boolean :required true
   :sql {:default (not (not default))}})

(defn type-class-table
  [ns-str]
  {:type :enum :scope :-/system
   :enum {:ns (symbol ns-str "EnumClassTableType")}})

(defn type-class-context
  [ns-str]
  {:type :enum :scope :-/system
   :enum {:ns  (symbol ns-str "EnumClassContextType")}})

(defn type-ref
  [ns-str table-str]
  {:type :ref :required true
   :ref {:ns (symbol ns-str table-str)}})

(defn default-fields
  [ns-str]
  {:id            {:priority     0  :field    (type-id-v4)}
   :class-table   {:priority     1  :field    (type-class-table ns-str)}
   :class-context {:priority     2  :field    (type-class-context ns-str)}
   :name          {:priority     5  :field    (type-name  ns-str)}
   :icon          {:priority    10  :field    (type-image ns-str)}
   :picture       {:priority    11  :field    (type-image ns-str)}
   :background    {:priority    12  :field    (type-image ns-str)}
   :color         {:priority    20  :field    (type-color ns-str)}
   :tags          {:priority    25  :field    (type-tags  ns-str)}
   :is-active     {:priority    30  :field    (type-boolean true)}
   :is-public     {:priority    31  :field    (type-boolean true)}
   :is-official   {:priority    80  :field    (type-boolean false)}
   :is-onboarded  {:priority    81  :field    (type-boolean false)}
   :rev           {:priority    99  :field    (type-ref ns-str "Rev")  :type :2d/entity}
   :log           {:priority   100  :field    (type-log   ns-str)}})

;;
;; Default Addons
;;

(defonce +addons+
  (atom {}))

(defn addons-init
  [& [m ns-str]]
  (reset! +addons+ (merge m (if ns-str
                              (default-fields ns-str)))))

(defn addons-add
  [field addon]
  (swap! +addons+ assoc field addon))

(defn addons-remove
  [field]
  (swap! +addons+ dissoc field))


;;
;; Default Tracking
;;

(defn config-tracking
  [& [type]]
  (case (or type :track/data)
    (:track/record :track/data)
    {:name   "data"
     :in     {:create {:time-created :time
                       :time-updated :time
                       :op-created :id
                       :op-updated :id}
              :modify {:time-updated :time
                       :op-updated :id}}
     :ignore #{:delete}}

    :track/temp
    {:name    "temp"
     :in      {:create  {:time-created :time
                         :time-updated :time}
               :modify {:time-updated :time}}
     :ignore  #{:delete}}
    
    :track/log
    {:name    "log"
     :in      {:create {:op-created :id :time-created :time}}
     :disable #{:modify}
     :ignore  #{:delete}}))

(defn config-tracking-fields
  [& [type]]
  (case (or type :track/data)
    :track/record [:op-created   {:type :uuid}
                   :op-updated   {:type :uuid}
                   :time-created {:type :time}
                   :time-updated {:type :time}
                   :__deleted__  {:type :boolean :scope :-/hidden
                                  :sql {:default false}}]
    
    :track/data    [:op-created   {:type :uuid}
                    :op-updated   {:type :uuid}
                    :time-created {:type :time}
                    :time-updated {:type :time}]
    
    :track/log     [:op-created   {:type :uuid}
                    :time-created {:type :time}]

    :track/temp    [:time-created {:type :time}
                    :time-updated {:type :time}]))

(defn config-api-meta
  [& [api]]
  (get {:api/system  {:sb/rls true
                      :sb/access {:admin :all
                                  :auth  :select
                                  :anon  :none}}
        :api/auth    {:sb/rls true
                      :sb/access {:admin :all
                                  :auth  :select}}
        :api/public  {:sb/rls true
                      :sb/access {:admin :all
                                  :auth  :select
                                  :anon  :select}}}
       (or api :api/auth)))








(comment
  :rev          {:priority 99
                 :field {:type :ref :required true
                         :ref {:ns `-/Rev}
                         :sql {:unique ["rev"]
                               :cascade true}}
                 :type :1d/entity}

  (defn Type
  [config]
  config)
  
  (def RevPart
    [])
  
  [:class-table          {:type :enum :scope :-/info
                          :ignore true
                          :enum {:ns `-/EnumClassTableType}}
   :class-context        {:type :enum :scope :-/info
                          :ignore true
                          :sql {:raw [:generated-always-as '((++ "Global" -/EnumClassContextType)) :stored]}
                          :enum {:ns `-/EnumClassContextType}}]
  
  [:class-table          {:type :enum :scope :-/info
                          :ignore true
                          :enum {:ns `-/EnumClassTableType}}
   :class-context        {:type :enum :scope :-/info
                          :ignore true
                          :sql {:raw [:generated-always-as '((++ "Global" -/EnumClassContextType)) :stored]}
                          :enum {:ns `-/EnumClassContextType}}]
  
  {:class-table    {}
   :class-context  {}}
  
  {:public true
   :track    [-/TrackingMin]
   :prepend  [-/IdClass
              [-/Class0DType
               :class-table {:ignore true:generated "Global"
                             :foreign   {:rev       {:ns -/Rev :column :class-context}}}]]
   :append  [-/DataType
             -/RevPart]
   :api/meta {:sb/rls true
              :sb/access {:admin :all
                          :auth  :select
                          :anon  :select}}}

  {:public true
   :track  (config-tracking)
   :prepend [[:id (type-id-v1)]]})

(comment

  
  
  {:id       #{:id/none :id/v4 :id/v1 type}
   :type     #{:0d/entity :0d/log
               :1d/entity :1d/log
               :2d/entity :2d/log}
   :track    #{:track/data
               :track/log
               :track/record
               :track/time}
   :api      #{:api/visible
               :api/public
               :api/user
               :api/hidden
               :api/system}
   :addons   #{:name}}
  
  (comment
    (Type {:meta     [:id/none
                      :0d/entity
                      :track/record]
           :class    {:table   "User"
                      :context "Global"}
           :include  [:name :color]}))

  (:static/typespec    )

  

  (def DeltasPart
    [:deltas       {:type :jsonb :required true}])
  
  (def BalancePart
    [:balance      {:type :numeric :required true
                    :sql {:default 0}}])


  (def IdClass
    [:id  {:type :uuid :primary "default"
           :sql {:default '(rt.postgres/uuid-generate-v4)}}])

  (def Id
    [:id  {:type :uuid :primary true
           :sql {:default '(rt.postgres/uuid-generate-v4)}}])



  (def IdLog
    [:id  {:type :uuid :primary true
           :sql {:default '(rt.postgres/uuid-generate-v1)}}])

  (defn IdText
    [ns-str]
    [:id  {:type :citext :primary true
           :sql {:process [[(symbol ns-str "as-upper-formatted")]
                           [(symbol ns-str "as-upper-limit-length") 50]]}}])



  (defenum.pg 
    ["Token"
     "Commodity"
     "Publisher"
     "Feed"
     "Chat"
     "ChatChannel"
     "User"
     "UserProfile"
     "Organisation"
     "Campaign"
     "Topic"
     "Wallet"
     "Prospect"
     "Task"])

  (defenum.pg EnumClassContextType
    ["Global"
     "User"
     "Organisation"
     "Campaign"
     "Topic"
     "Token"
     "Commodity"]))
