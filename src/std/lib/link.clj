(ns std.lib.link
  (:require [std.lib.env :as env]
            [std.lib.foundation]
            [std.lib.function :as fn]
            [std.lib.future]
            [std.lib.impl :refer [defimpl]]
            [std.lib.invoke :refer [definvoke]])
  (:import (clojure.lang Symbol Var Namespace IDeref)))

;; -----------------
;;      LINK
;; -----------------
;;
;; - structure is {:source {:ns <source-ns> :var <source-var>}
;;                 :alias   {:ns <alias-ns> :var <alias-var>}
;;
;; - on invocation, the link will:
;;       1. resolve its source function
;;       2. if function is present, rebind var-root, erroring otherwise
;;       3. invoke the function on given arguments
;;
;; - on initiation, the link is initiated with:
;;       1. :lazy, doing nothing until invoked
;;       2. :preempt, try to bind the var if source is loaded
;;       3. :eager, will load the source var and bind
;;       4. :metadata, will load arglist from source code
;;       5. :auto, will :preempt or load :metadata if source not loaded
;;
;; - the link will register itself on creation and register itself once resolved 

(def ^:dynamic *bind-root* nil)

(defonce ^:dynamic *registry* (atom {}))

(def ^:dynamic *suffix* ".clj")

(defn resource-path
  "converts a namespace to a resource path
 
   (resource-path 'code.test)
   => \"code/test.clj\""
  {:added "3.0"}
  ([ns]
   (-> (str ns)
       ^String (munge)
       (.replaceAll "\\." std.lib.foundation/*sep*)
       (str *suffix*))))

(defn ns-metadata-raw
  "returns the metadata associated with a given namespace"
  {:added "3.0"}
  ([ns]
   (->> (resource-path ns)
        (env/sys:resource)
        (slurp)
        (std.lib.foundation/suppress)
        (#(str "[" % "]"))
        (read-string)
        (filter (comp '#{defn
                         defmacro} first))
        (map (fn [[fsym name & more]]
               (let [[_ _ arglist & more] (fn/fn:create-args more)
                     arglists (if (vector? arglist)
                                (list arglist)
                                (map first (cons arglist more)))]
                 [name (cond-> {:arglists arglists}
                         (= fsym 'defmacro) (assoc :macro true))])))
        (into {}))))

(definvoke ns-metadata
  "provides source metadata support for links
 
   (ns-metadata 'std.lib)
   => map?"
  {:added "3.0"}
  [:memoize {:arglists '([ns])
             :function ns-metadata-raw}])

(declare link:info link-invoke link:status bind-resolve)

(defn- link-string
  ([link]
   (str "#link " (link:info link))))

(defimpl Link [source alias transform registry]
  :invoke link-invoke
  :string link-string
  :final true

  IDeref
  (deref [link] (bind-resolve link)))

(defn ^Link link:create
  "creates a link"
  {:added "3.0"}
  ([source alias]
   (link:create source alias identity))
  ([source alias transform]
   (link:create source alias transform *registry*))
  ([source alias transform registry]
   (let [{:keys [ns name]} source
         ns (or ns (.getName *ns*))]
     (Link. (assoc source :ns ns)
            alias
            transform
            registry))))

(defn link?
  "checks if object is a link"
  {:added "3.0"}
  ([obj]
   (instance? Link obj)))

(defn register-link
  "adds link to global registry"
  {:added "3.0"}
  ([^Link link]
   (let [alias (.alias link)
         registry (.registry link)]
     (register-link link alias registry)))
  ([^Link link alias registry]
   (swap! registry assoc alias link)
   alias))

(defn deregister-link
  "removes a link from global registry"
  {:added "3.0"}
  ([^Link link]
   (let [alias (.alias link)
         registry (.registry link)]
     (deregister-link link alias registry)))
  ([^Link link alias registry]
   (swap! registry dissoc alias)
   alias))

(defn registered-link?
  "checks if a link is registered"
  {:added "3.0"}
  ([^Link link]
   (let [alias     (.alias link)
         registry (.registry link)]
     (= link (get @registry alias)))))

(defn registered-links
  "returns all registered links"
  {:added "3.0"}
  ([]
   (registered-links *registry*))
  ([registry]
   (keys @registry)))

(defn link:unresolved
  "returns all unresolved links
 
   (link:unresolved)"
  {:added "3.0"}
  ([]
   (link:unresolved *registry*))
  ([registry]
   (keep (fn [[k link]]
           (= :unresolved (link:status link))
           k)
         @registry)))

(defn link:resolve-all
  "resolves all unresolved links in a background thread
 
   (link:resolve-all)"
  {:added "3.0"}
  ([]
   (link:resolve-all *registry*))
  ([registry]
   (std.lib.future/future
     (mapv (fn [v]
             (try
               (bind-resolve @v)
               (catch Throwable t
                 (env/local :println "LINK RESOLVE ERROR:" @v))))
           (link:unresolved registry)))))

(defn link:bound?
  "checks if the var of the link has been bound, should be true"
  {:added "3.0"}
  ([^Link link]
   (= link
      (and (.alias link) @(.alias link)))))

(defn link:status
  "lists the current status of the link"
  {:added "3.0"}
  ([^Link link]
   (let [{:keys [ns name]} (.source link)
         source-ns  (find-ns ns)
         source-var (std.lib.foundation/suppress (ns-resolve ns name))]
     (cond (nil? source-ns)
           :unresolved

           (nil? source-var)
           :source-var-not-found

           (link? @source-var)
           :linked

           :else
           :resolved))))

(defn find-source-var
  "finds the source var in the link"
  {:added "3.0"}
  ([^Link link]
   (let [{:keys [ns name]} (.source link)]
     (if (find-ns ns) (ns-resolve ns name)))))

(defn link-synced?
  "checks if the source and alias have the same value"
  {:added "3.0"}
  ([^Link link]
   (and (= :resolved (link:status link))
        (= @(find-source-var link)
           (and (.alias link) @(.alias link))))))

(defn link-selfied?
  "checks if the source and alias have the same value"
  {:added "3.0"}
  ([^Link link]
   (and (link:bound? link)
        (= :linked (link:status link))
        (= (.alias link) (find-source-var link)))))

(defn link:info
  "displays the link
 
   (link:info -selfied-)
   => {:source 'std.lib.link-test/-selfied-
       :bound true,
       :status :linked,
       :synced false,
       :registered false}"
  {:added "3.0"}
  ([^Link link]
   (let [{:keys [ns name]} (.source link)]
     {:source  (symbol (str ns) (str name))
      :bound   (link:bound? link)
      :status  (link:status link)
      :synced  (link-synced? link)
      :registered (registered-link? link)})))

(defn transform-metadata
  "helper function for adding metadata to vars"
  {:added "3.0"}
  ([alias transform metadata]
   (let [meta-fn (:meta transform)
         metadata (if meta-fn
                    (meta-fn metadata)
                    metadata)]
     (doto alias
       (alter-meta! merge metadata)))))

(defn bind-metadata
  "retrievess the metadata of a function from source code"
  {:added "3.0"}
  ([^Link link]
   (let [{:keys [ns name]} (.source link)]
     (doto (.alias link)
       (transform-metadata (.transform link)
                           (get (ns-metadata ns) name))))))

(declare bind-resolve)

(defn bind-source
  "retrieves the source var"
  {:added "3.0"}
  ([alias source-var transform]
   (let [source-obj @source-var
         source-obj (if (link? source-obj)
                      (bind-resolve source-obj)
                      source-obj)
         source-obj (if-let [value-fn (:value transform)]
                      (value-fn source-obj)
                      source-obj)]
     (if *bind-root*
       (doto ^Var alias
         (.bindRoot source-obj)
         (transform-metadata transform (meta source-var))))
     source-obj)))

(defn bind-resolve
  "binds a link or a series of links"
  {:added "3.0"}
  ([^Link link]
   (cond (link-selfied? link)
         (throw (ex-info "Link selfied." {:link link}))

         :else
         (let [{:keys [ns name]} (.source link)
               status (std.lib.foundation/suppress (env/require ns) :source-ns-not-found)
               source-var (if (not= status :source-ns-not-found)
                            (ns-resolve ns name))
               source-obj (if source-var
                            (bind-source (.alias link) source-var (.transform link))
                            (std.lib.foundation/error "Link not found."
                                     {:source (.source link)
                                      :status (or status
                                                  :source-var-not-found)}))]
           (deregister-link link)
           source-obj))))

(defn bind-preempt
  "retrieves the source var if available"
  {:added "3.0"}
  ([^Link link]
   (if *bind-root*
     (let [source-var (find-source-var link)]
       (if (and source-var (not (link? @source-var)))
         (bind-source (.alias link) source-var (.transform link)))))))

(defn bind-verify
  "retrieves the source var if available"
  {:added "3.0"}
  ([^Link link]
   (let [{:keys [ns name]} (.source link)]
     (if (get (ns-metadata ns) name)
       (.alias link)
       (throw (ex-info "Source var not found." {:ns ns :name name}))))))

(defn link:bind
  "automatically loads the var if possible"
  {:added "3.0"}
  ([^Link link key]
   (case key
     :lazy      nil
     :preempt   (bind-preempt link)
     :metadata  (bind-metadata link)
     :verify    (bind-verify link)
     :resolve   (bind-resolve link)
     :auto      (or (bind-preempt link)
                    (bind-metadata link))
     nil)))

(defn link-invoke
  "invokes a link"
  {:added "3.0"}
  ([^Link link & args]
   (let [func (bind-resolve link)]
     (apply func args))))

(defn ^Link intern-link
  "creates a registers a link"
  {:added "3.0"}
  ([name source]
   (intern-link *ns* name source))
  ([ns name source]
   (intern-link ns name source identity))
  ([ns name source transform]
   (intern-link ns name source transform *registry*))
  ([ns name source transform registry]
   (let [ns   (the-ns ns)
         alias (Var/intern ^Namespace ns ^Symbol name)
         lk   (link:create source alias transform registry)
         _    (doto alias
                (.setMeta {})
                (.bindRoot lk))]
     (register-link lk alias registry)
     lk)))

(defn link-form
  "creates the form in `link` macro"
  {:added "3.0"}
  ([ns sym resolve]
   (let [[to from] (if (vector? sym)
                     sym
                     [sym sym])
         tsym  (symbol (name to))
         tfull (symbol (name ns) (name tsym))]
     `(do (intern-link (quote ~ns)
                       (quote ~tsym)
                       {:ns   (quote ~(symbol (or (namespace from)
                                                  (name (env/ns-sym)))))
                        :name (quote ~(symbol (name from)))})
          (eval (quote (do (link:bind @(var ~tfull) ~resolve)
                           (var ~tfull))))))))

(defmacro deflink
  "creates a named link"
  {:added "3.0"}
  ([name source]
   (link-form (env/ns-sym) [name source] :auto)))

(defmacro link
  "creates invokable aliases for early binding"
  {:added "3.0"}
  ([& [opts? & sources]]
   (let [[opts sources] (if (map? opts?)
                          [opts? sources]
                          [{} (cons opts? sources)])
         resolve (or (:resolve opts) :auto)
         ns (or (:ns opts) (env/ns-sym))]
     (mapv (fn [source]
             (link-form ns source resolve))
           sources))))
