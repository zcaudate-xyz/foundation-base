(ns rt.basic.type-common
  (:require [std.lib.atom]
            [std.lib.collection]
            [std.lib.foundation]
            [std.lib.os]))

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
   (std.lib.atom/swap-return! *context-options*
     (fn [m] [m {}])))
  ([lang]
   (std.lib.atom/swap-return! *context-options*
     (fn [m] [(get m lang) (dissoc m lang)])))
  ([lang context]
   (std.lib.atom/swap-return! *context-options*
     (fn [m] [(get-in m [lang context])
              (update-in m [lang] dissoc context)])))
  ([lang context program]
   (std.lib.atom/swap-return! *context-options*
     (fn [m] [(get-in m [lang context program])
              (update-in m [lang context] dissoc program)]))))

(defn put-context-options
  "puts entries into context options"
  {:added "4.0"}
  [[lang context] m]
  (std.lib.atom/atom:put *context-options*
              [lang context]
              m))

(defn set-context-options
  "sets a entry into context options"
  {:added "4.0"}
  [[lang context program] opts]
  (std.lib.atom/atom:set *context-options*
              [lang context program]
              opts))

(defonce ^:dynamic *program-options* (atom {}))

(defn program-exists?
  "checks if an executable exists
   
   (program-exists? \"gcc\")
   => true"
  {:added "4.0"}
  ([exec]
   (->> @(std.lib.os/sh "which" exec {:inherit false})
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
  (-> (std.lib.atom/atom:put *program-options*
                  [lang]
                  m)
      (std.lib.atom/atom:put-changed)))

(defn swap-program-options
  "swaps out the program options using a funciotn"
  {:added "4.0"}
  [lang f & args]
  (apply swap! *program-options* update-in [lang] f args))

(defn get-program-default
  "gets the default program"
  {:added "4.0"}
  [lang context program]
  (or program
      (get-in (get-program-options lang)
              [:default context])
      (std.lib.foundation/error "No program found" {:input [lang context]})))

(defn get-program-flags
  "gets program flags"
  {:added "4.0"}
  [lang program]
  (get-in (get-program-options lang) [:env program :flags]))

(defn get-program-exec
  "gets running parameters for program"
  {:added "4.0"}
  [lang context program]
  (let [_   (assert (#{:oneshot
                       :twostep
                       :interactive
                       :basic
                       :websocket
                       :remote-port
                       :remote-ws}
                     context)
                    (str "Invalid context: " context))
        all (get-program-options lang)
        env (get-in all [:env program])
        {:keys [exec flags]} env
        args  (get flags context)]
    (if args
      (vec (cons exec args))
      (std.lib.foundation/error "Cannot be nil" {:input [lang context program]
                                :options (keys (std.lib.collection/filter-vals (fn [env]
                                                                (get-in env [:flags context]))
                                                              (get-in all [lang :env])))}))))

(defn get-options
  "gets merged options for context"
  {:added "4.0"}
  [lang context program]
  (let [_   (assert (#{:oneshot
                       :twostep
                       :interactive
                       :basic
                       :websocket
                       :remote-port
                       :remote-ws}
                     context)
                    (str "Invalid key: " context))
        copts    (get-in @*context-options* [lang context])
        _        (assert copts (str "Should not be empty: " [lang context]))]
    (std.lib.collection/merge-nested (:default copts)
                    (get-in (get-program-options lang) [:env program])
                    (get copts program))))  
