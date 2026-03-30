(ns rt.basic.type-common
  (:require [std.lang.base.registry :as registry]
            [std.lib.atom :as atom]
            [std.lib.collection :as collection]
            [std.lib.foundation :as f]
            [std.lib.os :as os]))

(defonce ^:dynamic *context-options* (atom {}))

(defn get-context-options
  "gets all or a section of the `*context-options*` structure"
  {:added "4.0"}
  ([]
   @*context-options*)
  ([lang]
   (get @*context-options* lang))
  ([lang context]
   (get-in @*context-options* [lang context]))
  ([lang context program]
   (get-in @*context-options* [lang context program])))

(defn clear-context-options
  "clear entries from the `*context-options*` structure"
  {:added "4.0"}
  ([]
   (atom/swap-return! *context-options*
     (fn [m] [m {}])))
  ([lang]
   (atom/swap-return! *context-options*
     (fn [m] [(get m lang) (dissoc m lang)])))
  ([lang context]
   (atom/swap-return! *context-options*
     (fn [m] [(get-in m [lang context])
              (update-in m [lang] dissoc context)])))
  ([lang context program]
   (atom/swap-return! *context-options*
     (fn [m] [(get-in m [lang context program])
              (update-in m [lang context] dissoc program)]))))

(defn put-context-options
  "puts entries into context options"
  {:added "4.0"}
  [[lang context] m]
  (atom/atom:put *context-options*
              [lang context]
              m))

(defn set-context-options
  "sets a entry into context options"
  {:added "4.0"}
  [[lang context program] opts]
  (atom/atom:set *context-options*
              [lang context program]
              opts))

(defonce ^:dynamic *program-options* (atom {}))

(def +valid-contexts+
  #{:oneshot
    :twostep
    :interactive
    :basic
    :websocket
    :remote-port
    :remote-ws})

(defn available-runtimes
  "lists installed runtimes for a language"
  {:added "4.0"}
  [lang]
  (->> @registry/+registry+
       keys
       (keep (fn [[k-lang runtime]]
               (when (= k-lang lang)
                 runtime)))
       sort
       vec))

(defn valid-context!
  "checks that the runtime context is valid"
  {:added "4.0"}
  [context]
  (assert (+valid-contexts+ context)
          (str "Invalid context: " context)))

(defn require-runtime!
  "loads the runtime implementation if it has been registered"
  {:added "4.0"}
  [lang context]
  (valid-context! context)
  (if-let [ns (get @registry/+registry+ [lang context])]
    (do (require ns)
        ns)
    (f/error "Runtime not installed"
             {:lang lang
              :runtime context
              :available (available-runtimes lang)})))

(defn program-exists?
  "checks if an executable exists
   
   (program-exists? \"gcc\")
   => true"
  {:added "4.0"}
  ([exec]
   (->> @(os/sh "which" exec {:inherit false})
        (not= ""))))

(defn get-program-options
  "gets all program options"
  {:added "4.0"}
  ([]
   @*program-options*)
  ([lang]
   (get @*program-options* lang)))

(defn put-program-options
  "puts configuration into program options"
  {:added "4.0"}
  [lang m]
  (-> (atom/atom:put *program-options*
                  [lang]
                  m)
      (atom/atom:put-changed)))

(defn swap-program-options
  "swaps out the program options using a funciotn"
  {:added "4.0"}
  [lang f & args]
  (apply swap! *program-options* update-in [lang] f args))

(defn get-program-default
  "gets the default program"
  {:added "4.0"}
  [lang context program]
  (let [_   (require-runtime! lang context)
        all (get-program-options lang)]
    (or program
        (get-in all [:default context])
        (f/error "No program found for runtime"
                 {:lang lang
                  :runtime context
                  :available (-> all :env keys sort vec)}))))

(defn get-program-flags
  "gets program flags"
  {:added "4.0"}
  [lang program]
  (get-in (get-program-options lang) [:env program :flags]))

(defn get-program-exec
  "gets running parameters for program"
  {:added "4.0"}
  [lang context program]
  (let [_   (require-runtime! lang context)
        all (get-program-options lang)
        env (get-in all [:env program])
        {:keys [exec flags]} env
        args  (get flags context)]
    (if args
      (vec (cons exec args))
      (f/error "Program does not support runtime"
               {:lang lang
                :runtime context
                :program program
                :available (->> (:env all)
                                (collection/filter-vals (fn [env]
                                                          (get-in env [:flags context])))
                                keys
                                sort
                                vec)}))))

(defn get-options
  "gets merged options for context"
  {:added "4.0"}
  [lang context program]
  (let [_        (require-runtime! lang context)
        copts    (get-in @*context-options* [lang context])]
    (when-not copts
      (f/error "Runtime configuration not installed"
               {:lang lang
                :runtime context
                :available (available-runtimes lang)}))
    (collection/merge-nested (:default copts)
                             (get-in (get-program-options lang) [:env program])
                             (get copts program))))  
