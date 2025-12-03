(ns rt.graal
  (:require [std.protocol.component :as protocol.component]
            [std.protocol.context :as protocol.context]
            [rt.basic.impl.process-js :as js]
            [rt.basic.impl.process-python :as python]
            [std.lang.base.impl :as impl]
            [std.lang.base.runtime :as default]
            [std.lang.base.pointer :as ptr]
            [std.json :as json]
            [std.lang :as l]
            [std.lib :as h :refer [defimpl]]
            [std.fs :as fs]
            [xt.lang.base-repl :as k])
  (:import (org.graalvm.polyglot Context
                                 Context$Builder
                                 Engine
                                 Value
                                 Source)
           (org.graalvm.polyglot.io ByteSequence)
           (hara.lib.graal Require
                           ResourceFolder
                           FilesystemFolder)))

(def ^:dynamic *lang* nil)

(defonce ^:dynamic *raws* (atom {}))

(defn add-resource-path
  "adds resource path to context
 
   (add-resource-path +js+ \"assets\")"
  {:added "3.0"}
  ([raw path]
   (h/-> (.getContextClassLoader (Thread/currentThread))
         (ResourceFolder/create path "UTF-8")
         (Require/enable raw %))
   raw))

(defn add-system-path
  "adds system path to context
 
   (add-system-path +js+ \".\")"
  {:added "3.0"}
  ([raw path]
   (h/-> (fs/file path)
         (FilesystemFolder/create "UTF-8")
         (Require/enable raw %))
   raw))

(defn make-raw
  "creates the base graal context
 
   (str (.eval ^Context +js+ \"js\" \"1 + 1\"))
   => \"2\""
  {:added "3.0"}
  ([{:keys [lang resource system]}]
   (let [ctx (h/-> (Context/newBuilder (into-array [(name lang)]))
                   (.allowAllAccess true)
                   (.build)
                   (if resource (reduce add-resource-path % resource) %)
                   (if system   (reduce add-system-path % (cons "." system)) %))
         _ (swap! *raws* assoc ctx {:lang lang})]
     ctx)))

(defn close-raw
  "closes the base graal context"
  {:added "3.0"}
  ([^Context ctx]
   (swap! *raws* dissoc ctx)
   (.close ctx)))

(defn raw-lang
  "gets the language context
 
   (raw-lang +js+)
   => :js"
  {:added "3.0"}
  ([ctx]
   (or *lang* (get-in @*raws* [ctx :lang]))))

(defn eval-raw
  "performs an exec expression
 
   (str (eval-raw +js+ \"1 + 1\"))
   => \"2\""
  {:added "3.0"}
  ([ctx string]
   (let [lang (raw-lang ctx)]
     (.eval ^Context ctx (name lang) string))))

(defn unwrap
  "unwraps a polyglot value"
  {:added "4.0"}
  [^Value v]
  (cond (.isString v) (.asString v)
        (.isNumber v) (if (.fitsInLong v) (.asLong v) (.asDouble v))
        (.isBoolean v) (.asBoolean v)
        (.isNull v)   nil
        :else v))

(defn eval-graal
  "evals body in the runtime
 
   (str (eval-graal (l/rt :js)
                    \"1+1\"))
   => \"2\""
  {:added "4.0"}
  ([{:keys [raw]} string]
   (unwrap (eval-raw @raw string))))

(def +options+
  (delay
    {:js      {:bootstrap (impl/emit-entry-deps
                           k/return-eval
                           {:lang :js
                            :layout :flat
                            :emit {:lang/format :commonjs}})
               :json   false
               :emit  {:body  {:transform #'default/return-transform}}}
     :python  {:bootstrap  (impl/emit-entry-deps
                            k/return-eval
                            {:lang :python
                             :layout :flat})
               :main  {:out   (comp json/read str)}
               :emit  {:body  {:transform (fn [forms mopts]
                                            (default/return-transform
                                             forms mopts {:format-fn identity
                                                          :wrap-fn (fn [forms]
                                                                     (apply list 'do forms))}))}}}}))

(defn invoke-graal
  "invokes a pointer in the runtime
 
   (invoke-graal (l/rt :js)
                 k/sub
                 [1 2])
   => -1"
  {:added "4.0"}
  ([{:keys [state lang layout] :as rt} ptr args]
   (let [{:keys [main emit json]} (get @+options+ lang)]
     (default/default-invoke-script
      rt ptr args eval-graal
      {:main main
       :emit emit
       :json (if (nil? json) :full json)}))))

(defn start-graal
  "starts the graal runtime"
  {:added "3.0"}
  ([{:keys [lang resource system raw] :as graal}]
   (if (nil? @raw)
     (reset! raw (make-raw {:lang lang
                            :resource resource
                            :system system})))
   (eval-graal graal (get-in @+options+ [lang :bootstrap]))
   graal))

(defn stop-graal
  "stops the graal runtime"
  {:added "3.0"}
  ([{:keys [lang raw] :as graal}]
   (if-not (nil? @raw)
     (h/swap-return! raw
                     (fn [ctx]
                       (close-raw ctx)
                       [ctx nil])))
   graal))

(defn- rt-graal-string
  ([graal]
   (str "#rt.graal" (into {} graal))))

(defimpl RuntimeGraal [lang raw]
  :string rt-graal-string
  :protocols [std.protocol.component/IComponent
              :exclude [-kill]
              :suffix "-graal"
              protocol.context/IContext
              :prefix "default/default-"
              :method {-raw-eval eval-graal
                       -invoke-ptr invoke-graal}])

(defn rt-graal:create
  "creates a graal runtime
 
   (h/-> (rt-graal:create {:lang :js})
         (h/start)
         (h/stop))
   => rt-graal?"
  {:added "4.0"}
  [{:keys [id]
    :or {id (h/sid)}
    :as m}]
  (map->RuntimeGraal (assoc m
                            :id id
                            :raw (atom nil))))

(defn rt-graal
  "creates and starts a graal runtime"
  {:added "3.0"}
  ([m]
   (-> (rt-graal:create m)
       (h/start))))

(defn rt-graal?
  "checks that object is a graal runtime"
  {:added "3.0"}
  [obj]
  (instance? RuntimeGraal obj))

;;
;; Hooks for lang
;;

(def +init+
  [(default/install-type!
    :python :graal
    {:type :hara/rt.graal
     :config {:lang :python :layout :full}
     :instance {:create rt-graal:create}})
   (default/install-type!
    :js :graal
    {:type :hara/rt.graal
     :config {:lang :js :layout :full}
     :instance {:create rt-graal:create}})])
