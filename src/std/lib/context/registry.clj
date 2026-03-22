(ns std.lib.context.registry
  (:require [std.lib.atom :as at]
            [std.lib.collection :as c]
            [std.lib.foundation :as h]
            [std.lib.impl :refer [defimpl]]
            [std.lib.resource :as res]
            [std.protocol.context :as protocol.context]))

;;
;; Context 
;;

(def +null+ {:context :null
             :rt      {:default {:key :default
                                 :resource :hara/context.rt.null
                                 :config   {}}}})

;;
;; Default Rt
;;

(defn- rt-null-string
  ([_]
   (str "#rt.null" [])))

(defimpl RuntimeNull []
  :string  rt-null-string
  :protocols [std.protocol.context/IContext
              :body {-raw-eval      string
                     -init-ptr      nil
                     -tags-ptr      nil
                     -deref-ptr     (into {} ptr)
                     -display-ptr   (into {} ptr)
                     -invoke-ptr    (h/error "Not allowed")
                     -transform-in-ptr args
                     -transform-out-ptr return}])

(def +rt-null+ (map->RuntimeNull +null+))

(defn rt-null?
  "checks that object is of type NullRuntime"
  {:added "3.0"}
  ([obj]
   (instance? RuntimeNull obj)))

(res/res:spec-add
 {:type :hara/context.rt.null
  :instance {:create map->RuntimeNull}})

;;
;; Registry management
;;

(defonce ^:dynamic *registry*  (atom {}))

(defn registry-list
  "lists all contexts
 
   (registry-list)"
  {:added "3.0"}
  ([] (keys @*registry*)))

(defn registry-install
  "installs a new context type
 
   (registry-install :play.test)"
  {:added "3.0"}
  ([ctx]
   (registry-install ctx {}))
  ([ctx config]
   (->> (at/atom:put *registry* [ctx]
                     (c/merge-nested +null+
                                     (assoc config :context ctx)))
        (at/atom:put-changed))))

(defn registry-uninstall
  "uninstalls a new context type
 
   (registry-uninstall :play.test)"
  {:added "3.0"}
  ([ctx]
   (at/atom:clear *registry* [ctx])))

(defn registry-get
  "gets the context type
 
   (registry-get :null)
   => (contains-in
       {:context :null,
        :rt {:default {:key :default,
                       :resource :hara/context.rt.null,
                       :config {}}}})"
  {:added "3.0"}
  ([ctx]
   (or (at/atom:get *registry* [ctx])
       (h/error "No ctx available" {:context ctx
                                    :options (registry-list)}))))

(defn registry-rt-list
  "return all runtime types
 
   (registry-rt-list)"
  {:added "3.0"}
  ([] (c/map-vals (comp keys :rt) @*registry*))
  ([ctx] (-> (registry-get ctx) :rt keys)))

(defn registry-rt-add
  "installs a context runtime type"
  {:added "3.0"}
  ([ctx {:keys [key] :as config}]
   (-> (at/atom:set *registry* [ctx :rt key] config)
       (at/atom:set-changed))))

(defn registry-rt-remove
  "uninstalls a context runtime type"
  {:added "3.0"}
  ([ctx key]
   (at/atom:clear *registry* [ctx :rt key])))

(defn registry-rt
  "gets the runtime type information"
  {:added "3.0"}
  ([ctx]
   (registry-rt ctx nil))
  ([ctx key]
   (let [key (or key :default)
         {:keys [rt] :as ctx} (registry-get ctx)
         rt (or (get-in ctx [:rt key])
                (h/error "No rt installed"
                         {:ctx ctx
                          :rt key
                          :options (keys rt)}))
         rt (c/merge-nested (dissoc ctx :rt)
                            rt)]
     rt)))

(defn registry-scratch
  "gets the scratch runtime for a registered context"
  {:added "4.0"}
  ([ctx]
   (:scratch (registry-get ctx))))

(def +init+ (registry-install :null {:scratch (map->RuntimeNull {})}))
