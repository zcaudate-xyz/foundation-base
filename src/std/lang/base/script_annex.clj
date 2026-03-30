(ns std.lang.base.script-annex
  (:require [std.json :as json]
             [std.lang.base.impl :as impl]
             [std.lang.base.registry :as sreg]
             [std.lang.base.library :as lib]
             [std.lang.base.library-snapshot :as snap]
             [std.lang.base.runtime :as rt]
            [std.lang.base.util :as ut]
            [std.lib.atom :as atom]
            [std.lib.collection :as collection]
            [std.lib.component :as component]
            [std.lib.context.registry :as reg]
            [std.lib.context.space :as space]
            [std.lib.env :as env]
            [std.lib.foundation :as f]
            [std.lib.impl]
            [std.lib.resource :as resource]))

(defn- rt-annex-string
  "returns the annex string"
  {:added "4.0"}
  ([{:keys [runtimes]}]
   (str "#lang.annex " (vec (sort (map (fn [[k rt]]
                                         [k (:lang rt)])
                                       @runtimes))))))

(std.lib.impl/defimpl RuntimeAnnex [id registry runtimes library]
  :string rt-annex-string)

(defn rt-annex?
  "checks that object is an annex"
  {:added "4.0"}
  ([obj]
   (instance? RuntimeAnnex obj)))

(defn rt-annex:create
  "creates an annex object
   
   
   (annex/rt-annex:create {})
   => annex/rt-annex?"
  {:added "4.0"}
  ([{:keys [id] :as m}]
   (let [runtimes (atom {})
         registry (atom {})
         library  (lib/library {:parent #'impl/default-library})]
     (map->RuntimeAnnex (merge m {:library library
                                  :registry registry
                                  :runtimes runtimes})))))

(def +init-annex+
  [(resource/res:spec-add
    {:type :hara/lang.rt.annex
     :config {}
     :instance {:create rt-annex:create}})

   (reg/registry-install
    :lang.annex
    {:config {}
     :rt  {:default {:resource :hara/lang.rt.annex}}})])



(defn annex-current
  "gets the current annex. May not exist
 
   (annex/annex-current)
   => any?"
  {:added "4.0"}
  ([]
   (annex-current (env/ns-sym)))
  ([ns]
   (let [{:keys [state] :as sp} (space/space ns)
         rec (:lang.annex @state)]
     (if rec (:instance rec)))))

(defn annex-reset
  "resets the current annex"
  {:added "4.0"}
  ([]
   (annex-reset (env/ns-sym)))
  ([ns]
   (let [sp (space/space ns)]
     (space/space:rt-stop sp :lang.annex))))

(defn get-annex
  "gets the current annex in the namespace"
  {:added "4.0"}
  ([]
   (get-annex (env/ns-sym)))
  ([ns]
   (let [{:keys [state] :as sp} (space/space ns)
         check  (or (:lang.annex @state)
                    (space/space:context-set sp :lang.annex :default {}))]

     (space/space:rt-start sp :lang.annex))))

(defn clear-annex
  "clears all runtimes in the annex"
  {:added "4.0"}
  ([]
   (clear-annex (env/ns-sym)))
  ([ns]
   (let [{:keys [runtimes]} (get-annex ns)]
     (atom/swap-return! runtimes
                        (fn [m]
                          [(collection/map-vals component/stop m) {}])))))

(defn get-annex-library
  "gets the current annex library
 
   (annex/get-annex-library (h/ns-sym))
   => lib/library?"
  {:added "4.0"}
  ([ns]
   (:library (get-annex ns))))

(defn get-annex-book
  "gets the current book in the annex
  
   (annex/get-annex-book (h/ns-sym) :lua)
   => book/book?"
  {:added "4.0"}
  ([ns lang]
   (let [curr (:library (get-annex ns))]
      (or (snap/get-book @(:instance curr) lang)
          (let [default-lib (impl/default-library)
                book   (or (lib/get-book default-lib lang)
                           (do (some-> (sreg/registry-book-ns lang)
                                       require)
                               (lib/get-book default-lib lang))
                           (f/error "Book not found"
                                    {:lang lang
                                     :available (map first (sreg/registry-book-list))}))]
            (lib/add-book! curr (assoc book :modules {})))))))

(defn add-annex-runtime
  "adds a runtime to the annex"
  {:added "4.0"}
  [ns tag rt]
  (let [{:keys [registry
                runtimes]} (get-annex ns)]
    (atom/swap-return! runtimes
                       (fn [m]
                         (let [curr (get m tag)
                               _  (if curr (component/stop curr))]
                           [[curr rt] (assoc m tag rt)])))))

(defn get-annex-runtime
  "gets the annex rutime"
  {:added "4.0"}
  [ns tag]
  (let [{:keys [runtimes]} (get-annex ns)]
    (get @runtimes tag)))

(defn remove-annex-runtime
  "removes the annex runtime"
  {:added "4.0"}
  [ns tag]
  (let [{:keys [runtimes]} (get-annex ns)]
    (atom/swap-return! runtimes
                       (fn [m]
                         (let [curr (get m tag)
                               _  (if curr (component/stop curr))]
                           [curr (dissoc m tag)])))))

(defn register-annex-tag
  "registers a config for the tag"
  {:added "4.0"}
  [ns tag lang runtime config]
  (let [{:keys [registry]} (get-annex ns)]
    (atom/swap-return! registry
                       (fn [m]
                         [[(get m tag) lang]
                          (assoc m tag {:lang lang
                                        :runtime runtime
                                        :config config})]))))

(defn deregister-annex-tag
  "removes the config for the tag"
  {:added "4.0"}
  [ns tag]
  (let [{:keys [registry]} (get-annex ns)]
    (atom/swap-return! registry
                       (fn [m]
                         [(get m tag) (dissoc m tag)]))))

(defn start-runtime
  "starts the runtime in the annex"
  {:added "4.0"}
  [lang runtime config]
  (let [ctx (ut/lang-context lang)
        {:keys [resource] :as reg} (reg/registry-rt ctx runtime)
        spec (resource/res:spec-get resource)
        {:keys [instance]} spec]
    (-> ((:create instance) (merge (:config reg)
                                   {:lang lang
                                    :runtime runtime}
                                   config))
        ((:setup instance)))))

(defn same-runtime?
  "checks that one runtime is the same as another"
  {:added "4.0"}
  [rt lang runtime config]
  (let [ctx (ut/lang-context lang)
        reg (reg/registry-rt ctx runtime)
        ckeys [:lang :runtime :layout :emit]
        new-conf (-> (merge (:config reg)
                            {:lang lang
                             :runtime runtime}
                            config)
                     (select-keys ckeys))
        old-conf (select-keys rt ckeys)]
    (= new-conf old-conf)))
