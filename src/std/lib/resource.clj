(ns std.lib.resource
  (:require [std.lib.atom :as at]
            [std.lib.collection :as c]
            [std.lib.component :as component]
            [std.lib.env :as env]
            [std.lib.foundation :as h]
            [std.lib.template :as tmpl :refer [deftemplate]]))

(defonce ^:dynamic *namespace* nil)

(defonce ^:dynamic *alias*     (atom {}))

(defonce ^:dynamic *registry*  (atom {}))

(defonce ^:dynamic *active*    (atom {:global {}
                                      :namespace {}
                                      :shared {}}))

(def +type+ {:mode     {:key     :id
                        :allow   #{:global :namespace :shared :ext}
                        :default :global}
             :config   {}
             :instance {:create   identity
                        :setup    component/start
                        :teardown component/stop}
             :variant  {:default {:hook   {:pre-setup     h/NIL
                                           :post-setup    h/NIL
                                           :pre-teardown  h/NIL
                                           :post-teardown h/NIL}
                                  :config {}}}})

;;
;; Registry Management
;;

(defn res:spec-list
  "lists all available specs
 
   (res:spec-list)"
  {:added "3.0"}
  ([] (keys @*registry*)))

(defn res:spec-add
  "adds a new resource spec"
  {:added "3.0"}
  ([{:keys [type] :as spec}]
   (-> (at/atom:put *registry* [type] (c/merge-nested +type+ spec))
       (at/atom:put-changed))))

(defn res:spec-remove
  "removes the resource spec"
  {:added "3.0"}
  ([type]
   (at/atom:clear *registry* [type])))

(defn res:spec-get
  "retrieves a resource spec
 
   (res:spec-get :hara/concurrent.atom.executor)
   => map?"
  {:added "3.0"}
  ([type]
   (or (at/atom:get *registry* [type])
       (h/error "No spec available" {:type type
                                     :options (res:spec-list)}))))

(defn res:variant-list
  "retrieves a list of variants to the spec
 
   (res:variant-list :hara/concurrent.atom.executor)
   => coll?"
  {:added "3.0"}
  ([] (c/map-vals (comp keys :variant) @*registry*))
  ([type] (-> (res:spec-get type) :variant keys)))

(defn res:variant-add
  "adds a spec variant"
  {:added "3.0"}
  ([type {:keys [id alias] :as spec}]
   (if alias
     (at/atom:set *alias* [alias] [type id]))
   (-> (at/atom:set *registry* [type :variant id] spec)
       (at/atom:set-changed))))

(defn res:variant-remove
  "removes a spec variant"
  {:added "3.0"}
  ([type id]
   (let [{:keys [alias]} (at/atom:get *registry* [type :variant id])
         _  (if alias (at/atom:clear *alias* [alias]))]
     (at/atom:clear *registry* [type :variant id]))))

(defn res:variant-get
  "gets a new variant"
  {:added "3.0"}
  ([type]
   (res:variant-get type :default))
  ([type id]
   (let [{:keys [variant] :as spec} (res:spec-get type)
         variant (or (get-in spec [:variant id])
                     (h/error "No variant available"
                              {:type type
                               :variant id
                               :options (keys variant)}))
         variant (c/merge-nested (dissoc spec :variant)
                                 variant)]
     variant)))

;;
;; Instance Management
;;

(defn res:mode
  "gets the default mode of a resource
 
   (res:mode :hara/concurrent.atom.executor)
   => :global
 
   (res:mode :hara/concurrent.atom.executor :std.concurrent.print)
   => :global"
  {:added "3.0"}
  ([type]
   (res:mode type :default))
  ([type variant]
   (let [spec (res:spec-get type)]
     (or (get-in spec [:variant variant :mode :default])
         (-> spec :mode :default)))))

;;
;;
;;

(defn res:active
  "gets all active resources
 
   (res:active)"
  {:added "3.0"}
  ([]
   (res:active nil))
  ([type]
   (res:active type nil))
  ([type variant]
   (let [keys-fn (apply comp keys (partial c/filter-vals not-empty)
                        (cond->   []
                          type    (conj (partial c/filter-keys (comp #{type} first)))
                          variant (conj (partial c/filter-keys (comp #{variant} first)))))
         filter-fn (partial c/filter-vals not-empty)]
     (h/-> @*active*
           (update :global keys-fn)
           (update :namespace (comp filter-fn (partial c/map-vals keys-fn)))
           (update :shared (comp filter-fn (partial c/map-vals keys-fn)))
           filter-fn))))

(defn res-setup
  "creates and starts a resource"
  {:added "3.0"}
  ([type variant config]
   (let [{:keys [instance hook] :as spec} (res:variant-get type variant)
         {:keys [create setup]} instance
         {:keys [pre-setup post-setup]} hook
         config (c/merge-nested (:config spec) config)]
     (cond-> (create config)
       pre-setup (doto pre-setup)
       :then setup
       post-setup (doto post-setup)))))

(defn res-teardown
  "shutsdown a resource"
  {:added "3.0"}
  ([type variant instance]
   (let [{:keys [hook] :as spec} (res:variant-get type variant)
         {:keys [teardown]} (:instance spec)
         {:keys [pre-teardown post-teardown]} hook]
     (h/suppress
      (cond-> instance
        pre-teardown (doto pre-teardown)
        :then teardown
        post-teardown (doto post-teardown))))))

(defn- res-input
  ([input]
   (cond (keyword? input) [input {}]
         (map? input)    [:default input]
         (symbol? input) [:default input]
         (string? input) [:default input]
         :else (h/error "Not valid" {:input input}))))

(defn res-key
  "gets a resource key
 
   (res-key :shared :hara/concurrent.atom.executor :default {:id :hello})
   => :hello"
  {:added "3.0"}
  ([mode type variant input & [args]]
   (let [input (or (last args) input)
         key (if (map? input)
               (let [spec (res:variant-get type variant)
                     {:keys [key]} (:mode spec)]
                 (key input))
               input)
         to-sym (fn [x]
                  (.getName (the-ns x)))]
     (case mode
       :global nil
       :namespace (to-sym (or key *namespace* *ns*))
       :shared key))))

(defn res-path
  "gets the resource path
 
   (res-path :shared :hara/concurrent.atom.executor :default {:id :hello})
   => '(:shared [:hara/concurrent.atom.executor :default] :hello)"
  {:added "3.0"}
  ([mode type variant input]
   (let [key (res-key mode type variant input)
         ext (cond-> [] key (conj key))]
     (concat (cons mode [[type variant]]) ext))))

(defn res-access-get
  "access function for active resource"
  {:added "3.0"}
  ([mode type variant key]
   (let [path (res-path mode type variant key)]
     (at/atom:get *active* path))))

(defn res-access-set
  "access set function for active resource"
  {:added "3.0"}
  ([mode type variant key entry]
   (let [path (res-path mode type variant key)]
     (-> (at/atom:set *active* path entry)
         (at/atom:set-changed)))))

(defn res-start
  "start methods for resource"
  {:added "3.0"}
  ([mode type variant key config]
   (if (res-access-get mode type variant key)
     (h/error "Already started" {:type type
                                 :variant variant
                                 :key key})
     (let [instance (res-setup type variant config)]
       (->> {:key key :config config :instance instance}
            (res-access-set mode type variant key))
       instance))))

(defn res-stop
  "stop method for resource"
  {:added "3.0"}
  ([mode type variant key]
   (when-let [entry (res-access-get mode type variant key)]
     (at/atom:clear *active* (res-path mode type variant key))
     (res-teardown type variant (:instance entry)))))

(defn res-restart
  "starts and stops current resource"
  {:added "3.0"}
  ([mode type variant key]
   (when-let [entry (res-access-get mode type variant key)]
     (res-stop mode type variant key)
     (res-start mode type variant key (:config entry)))))

(defn res-base
  "gets or sets a reosurce"
  {:added "3.0"}
  ([mode type variant key config]
   (let [spec (res:variant-get type variant)
         {:keys [allow]} (:mode spec)
         _     (or (allow mode)
                   (h/error "Mode not allowed" {:mode mode
                                                :options allow}))]
     (or (:instance (res-access-get mode type variant key))
         (res-start mode type variant key config)))))

(defn res-call-fn
  "helper function for res-apifn"
  {:added "3.0"}
  ([f]
   (fn res-call
     ([type args]
      (if-let [[type variant] (get @*alias* type)]
        (res-call type variant args)
        (res-call type :default args)))
     ([type input args]
      (let [[variant input] (res-input input)]
        (res-call type variant input args)))
     ([type variant input args]
      (let [mode (res:mode type variant)
            key  (res-key mode type variant input args)]
        (res-call mode type variant key args)))
     ([mode type variant key args]
      (apply f mode type variant key args)))))

(defn res-api-fn
  "helper function to create user friendly calls"
  {:added "3.0"}
  [res-call extra post default]
  (let [call-fn (res-call-fn res-call)]
    (fn [& all]
      (let [rall (reverse all)
            rall (if (and default (== (count all) 1))
                   (cons default rall)
                   rall)
            [rargs rstart] [(take extra rall) (drop extra rall)]]
        (cond-> (apply call-fn (conj (vec (reverse rstart)) (reverse rargs)))
          post (h/call post))))))

(deftemplate res-api-tmpl
  ([[sym res-sym config]]
   (let [extra    (count (:args config))
         args     (map :name (:args config))
         default  (:default (first (:args config)))
         arglists (cond->> (map (fn [arr] (vec (concat arr args)))
                                '([type] [type input] [type variant input] [mode type variant key]))
                    default (cons '[type])
                    :then (list 'quote))]
     (tmpl/$
      (def ~(with-meta sym {:arglists arglists})
        (res-api-fn ~res-sym ~extra ~(:post config) ~default))))))

;;
;; API
;;

(h/template-entries [res-api-tmpl]
  [[res:exists? res-access-get {:post boolean}]
   [res:set     res-access-set {:args [{:name instance}]}]
   [res:stop    res-stop  {}]
   [res:path    res-path  {}]
   [res:start   res-start {:args [{:name config :default {}}]}]
   [res:restart res-restart {:args []}]
   [res         res-base  {:args [{:name config :default {}}]}]])

