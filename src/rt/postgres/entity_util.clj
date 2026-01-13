(ns rt.postgres.entity-util
  (:require [std.lib :as h]
            [std.lang :as l]
            [std.string :as str]
            [rt.postgres :as pg]
            [std.lang.base.grammar-spec :as grammar-spec]))

(defonce +app+
  (atom {}))

(def ^:dynamic *ns-str* "-")

(defn default-application
  [& [module]]
  (first (:application
          (:static (h/suppress
                    (l/rt:module
                     (l/rt (or module
                               (h/ns-sym))
                           :postgres)))))))

(defn default-ns-str
  [& [application]]
  (get @+app+ (or application
                  (default-application (h/ns-sym)))))

(defn init-default-ns-str
  [& [application ns-str]]
  (let [ns-str (or ns-str (name (h/ns-sym)))
        application (or application (default-application (h/ns-sym)))]
    (when application 
      (swap! +app+ assoc application ns-str))))




;;
;; Default Types
;;

(defn type-id-v1
  []
  {:type :uuid :primary "default" :priority 0
   :sql {:default '(rt.postgres/uuid-generate-v1)}})

(defn type-id-v4
  []
  {:type :uuid :primary "default" :priority 0
   :sql {:default '(rt.postgres/uuid-generate-v4)}})

(defn type-id-text
  [ns-str]
  {:type :citext :primary "default" :priority 0
   :sql {:process [[(symbol ns-str "as-upper-formatted")]
                   [(symbol ns-str "as-upper-limit-length") 100]]}})

(defn type-name
  [ns-str & [priority]]
  {:type :citext :required true :scope :-/info  :priority (or priority 7)
   :sql  {:process [[(symbol ns-str "as-upper-formatted")]
                    [(symbol ns-str "as-upper-limit-length") 36]]
          :unique ["name"]}})

(defn type-code
  [ns-str & [priority]]
  {:type :citext :scope :-/info  :priority (or priority 7)
   :sql  {:process [[(symbol ns-str "as-upper-formatted")]
                    [(symbol ns-str "as-upper-limit-length") 36]]
          :unique ["code"]}})

(defn type-image
  [ns-str & [priority]]
  {:type :image :priority (or priority 10)
   :sql  {:process [[(symbol ns-str "as-jsonb")]]
          :default "{}"}
   :profiles {:web {:type "image"}}})

(defn type-color
  [ns-str & [priority]]
  {:type :citext :required true :scope :-/info :priority (or priority 20)
   :sql {:default    (list (symbol ns-str "color-rand"))
         :constraint (list (symbol ns-str "color-check") #{"color"})}
   :profile {:web {:edit #{:create :modify}
                   :type "color"}}})

(defn type-tags
  [ns-str & [priority]]
  {:type :array :scope :-/info :priority (or priority 25)
   :sql {:process [[(symbol ns-str "as-jsonb-array")]]
         :default "[]"}
   :profile {:web {:edit #{:create :modify}
                   :type "chip"}}})

(defn type-log
  [ns-str & [priority]]
  {:type :array :required true :priority (or priority 90)
   :scope :-/detail
   :sql  {:process [[(symbol ns-str "as-jsonb-array")]]
          :default "[]"}
   :map  {:status  {:type :text}
          :message {:type :text}
          :error   {:type :text}}})

(defn type-log-entry
  [ns-str & [priority]]
  {:type :map :required true :priority (or priority 80)
   :scope :-/detail
   :sql  {:process [[(symbol ns-str "as-jsonb")]]
          :default "{}"}
   :map  {:status  {:type :text}
          :message {:type :text}
          :error   {:type :text}}})

(defn type-detail
  [ns-str & [priority]]
  {:type :map :required true :priority (or priority 50)
   :scope :-/detail
   :sql  {:process [[(symbol ns-str "as-jsonb")]]
          :default "{}"}})

(defn type-boolean
  [& [default priority]]
  {:type :boolean :required true :priority priority
   :sql {:default (not (not default))}})

(defn type-class
  [ns-str & [priority]]
  {:type :enum :scope :-/hidden :priority (or priority 1)
   :enum {:ns (symbol ns-str "EnumClassType")}})

(defn type-ref
  [ns-str table-str & [priority]]
  {:type :ref :required true :priority priority
   :ref {:ns (symbol ns-str table-str)}})

(defn type-class-ref
  [& [m priority]]
  (merge m
         {:type :uuid :required true :priority (or priority 5)}))

(defn normalise-ref
  [ptr]
  (cond (h/pointer? ptr)
        (symbol (name (:module ptr))
                (name (:id ptr)))

        (symbol? ptr) ptr

        (var? ptr) (symbol (name (.getName (.ns ptr)))
                           (name (.sym ptr)))))

(defn default-fields
  [ns-str]
  {:name          {:priority     7  :field    (type-name  ns-str)}
   :code          {:priority     8  :field    (type-code  ns-str)}
   :color         {:priority     9  :field    (type-color ns-str)}
   :title         {:priority    10  :field    {:type :text :required true}}
   :description   {:priority    11  :field    {:type :text}}
   
   :icon          {:priority    20  :field    (type-image ns-str)}
   :picture       {:priority    21  :field    (type-image ns-str)}
   :background    {:priority    22  :field    (type-image ns-str)}
   
   
   
   
   
   :is-active     {:priority    30  :field    (type-boolean true)}
   :is-public     {:priority    31  :field    (type-boolean true)}
   :is-draft      {:priority    32  :field    (type-boolean true)}
   :detail        {:priority    51  :field    (type-detail  ns-str)}
   :tags          {:priority    52  :field    (type-tags  ns-str)}
   
   :is-official   {:priority    80  :field    (type-boolean false)}
   :is-onboarded  {:priority    81  :field    (type-boolean false)}
   :is-archived   {:priority    89  :field    (type-boolean false)}
   :log           {:priority    90  :field    (type-log   ns-str)}
   
   :entry         {:priority    95  :field    (type-log-entry  ns-str)}})

;;
;; Default Addons
;;

(defonce +addons+
  (atom {}))

(defn init-addons
  [& [m application]]
  (let [application   (or application (default-application (h/ns-sym)))
        ns-str   (default-ns-str application)]
    (when application 
      (swap! +addons+ assoc application (merge m (default-fields ns-str))))))

(defn get-addon
  [key]
  (assoc (get-in @+addons+ [(default-application (h/ns-sym))
                            key])
         :key key))

(defn add-addon
  [key field priority]
  (let [application (default-application (h/ns-sym))]
    (swap! +addons+ assoc-in [application key] {:field field
                                                :priority priority})))

(defn addons-remove
  [key]
  (swap! +addons+ update-in [(default-application (h/ns-sym))] dissoc key))


;;
;; Default Tracking
;;

(defn get-tracking
  [track]
  (case track
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
     :ignore  #{:delete}}

    :track/none {}))

(defn get-tracking-columns
  [track]
  (case track
    :track/record [:op-created   {:type :uuid}
                   :op-updated   {:type :uuid}
                   :time-created {:type :time}
                   :time-updated {:type :time}
                   :__deleted__  {:type :boolean
                                  :scope :-/hidden
                                  :sql {:default false}}]
    
    :track/data    [:op-created   {:type :uuid}
                    :op-updated   {:type :uuid}
                    :time-created {:type :time}
                    :time-updated {:type :time}]
    
    :track/log     [:op-created   {:type :uuid}
                    :time-created {:type :time}]

    :track/temp    [:time-created {:type :time}
                    :time-updated {:type :time}]

    :track/none     []))

(defn get-access
  [access]
  (if (not= access :none)
    {:sb/rls true
     :sb/access {:admin :all
                 :auth  (case access
                          (:access/hidden) :none
                          (:access/system
                           :access/auth
                           :access/public)  :select)
                 :anon  (case access
                          (:access/hidden
                           :access/system)  :none
                          (:access/auth
                           :access/public)  :select)}}))

(defn fill-priority
  [arr priority]
  (let [curr (volatile! -1)]
    (mapv (fn [m]
            (if (map? m)
              (assoc m :priority priority :priority-index (vswap! curr inc))
              m))
          arr)))
