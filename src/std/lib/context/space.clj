(ns std.lib.context.space
  (:require [std.protocol.context :as protocol.context]
            [std.protocol.component :as protocol.component]
            [std.lib.atom :as at]
            [std.lib.collection :as c]
            [std.lib.env :as env]
            [std.lib.foundation :as h]
            [std.lib.impl :as impl :refer [defimpl]]
            [std.lib.resource :as res]
            [std.lib.context.registry :as reg])
  (:import (clojure.lang Namespace)))

(defonce ^:dynamic *namespace* nil)

;;
;; Space
;;

(defn- space-string
  ([{:keys [namespace] :as sp}]
   (str "#space-" namespace (protocol.context/-rt-active sp))))

(defn space-context-set
  "sets the context in the space"
  {:added "3.0"}
  ([{:keys [state]} ctx key config]
   (let [rt     (reg/registry-rt ctx key)
         config (c/merge-nested (:config rt) config)]
     (-> (at/atom:set state [ctx] (assoc rt :config config))
         (at/atom:set-changed)))))

(defn space-context-unset
  "unsets the context in the space"
  {:added "3.0"}
  ([{:keys [state] :as sp} ctx]
   (at/atom:clear state [ctx])))

(defn space-context-get
  "gets the context in the space"
  {:added "3.0"}
  ([{:keys [state] :as sp} ctx]
   (let [config (or (get @state ctx)
                    (h/error "Context not found"
                             {:input ctx
                              :options (protocol.context/-context-list sp)}))
         variant (or (:variant config) :default)]
     (assoc config :variant variant))))

(defn space-rt-start
  "starts the context runtime"
  {:added "3.0"}
  ([{:keys [state] :as sp} ctx]
   (or (protocol.context/-rt-get sp ctx)
       (let [{:keys [config resource variant]} (space-context-get sp ctx)
             instance (res/res-setup resource variant config)]
         (at/atom:set state [ctx :instance] instance)
         instance))))

(defn space-rt-stop
  "stops the context runtime"
  {:added "3.0"}
  ([{:keys [state] :as sp} ctx]
   (let [{:keys [instance resource variant]} (space-context-get sp ctx)]
     (when instance
       (res/res-teardown resource variant instance)
       (at/atom:clear state [ctx :instance])))))

(defn space-stop
  "shutdown all runtimes in the space"
  {:added "3.0"}
  ([space]
   (let [ctxs (protocol.context/-rt-active space)]
     (mapv (fn [ctx]
             (protocol.context/-rt-stop space ctx))
           ctxs)
     space)))

(defimpl Space [namespace state]
  :prefix "space-"
  :string space-string
  :protocols [std.protocol.context/ISpace
              :body {-context-list (vec (keys @state))
                     -rt-get       (:instance (get @state ctx))
                     -rt-active    (vec (keys (c/filter-vals :instance @state)))
                     -rt-started?  (boolean (protocol.context/-rt-get sp ctx))
                     -rt-stopped?  (not (boolean (protocol.context/-rt-get sp ctx)))}
              protocol.component/IComponent
              :exclude [-kill]
              :body {-start component}])

(defn space?
  "checks that an object is of type space"
  {:added "3.0"}
  ([obj]
   (instance? Space obj)))

(defn space-create
  "creates a space"
  {:added "3.0"}
  ([{:keys [namespace] :as m}]
   (map->Space (merge m {:namespace (or namespace (env/ns-sym))
                         :state (atom {})}))))

(res/res:spec-add
 {:type :hara/context.space
  :mode {:key :namespace
         :allow #{:namespace}
         :default :namespace}
  :instance {:create space-create}})

(defn space
  "gets the space in the current namespace
 
   (space)"
  {:added "3.0"}
  ([]
   (space *namespace*))
  ([namespace]
   (res/res :hara/context.space {:namespace namespace})))

(defn space-resolve
  "resolves a space given various inputs
 
   (space-resolve *ns*)"
  {:added "3.0"}
  ([obj]
   (cond (or (nil? obj)
             (symbol? obj)) (space obj)

         (instance? Namespace obj) (space (.getName ^Namespace obj))

         :else obj)))

(defn protocol-tmpl
  "constructs a template function"
  {:added "3.0"}
  ([{:keys [prefix protocol instance resolve default]}]
   (fn [{:keys [name arglists]}]
     (let [sym (symbol (str prefix (subs (str name) 1)))
           arglist (first arglists)
           pmethod (symbol protocol (str name))]
       `(defn ~sym
          ~@(if default
              [`(~(vec (rest arglist))
                 (~sym ~default ~@(rest arglist)))])
          (~(vec (concat [instance] (rest arglist)))
           (-> (~resolve ~instance)
               (~pmethod ~@(rest arglist)))))))))

(h/template-entries [(protocol-tmpl {:prefix   "space:"
                                     :protocol "protocol.context"
                                     :instance 'sp
                                     :resolve  `space-resolve
                                     :default  `*namespace*})]
  (vals (:sigs protocol.context/ISpace)))

(defn space:rt-current
  "gets the current rt in the space"
  {:added "4.0"}
  ([ctx]
   (if (keyword? ctx)
     (space:rt-current (env/ns-sym) ctx)
     ctx))
  ([namespace ctx]
   (let [sp (space namespace)]
     (or (space:rt-get sp ctx)
         (reg/registry-scratch ctx)
         reg/+rt-null+))))
