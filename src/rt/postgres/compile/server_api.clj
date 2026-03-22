(ns rt.postgres.compile.server-api
  (:require [code.framework.generate :as gen]
            [rt.postgres.compile.server-db :as server-db]
            [rt.postgres.grammar.typed-common :as types]))

(def ^:private +default-targets+
  [:xtalk-contracts])

(def ^:private XTALK_CONTRACT_TEMPLATE
  "
(def.xt ^{:rt.postgres.compile/target :xtalk-contract}
  ~contract-sym
  {:id ~id
   :task \"supabase.rpc\"
   :sync {:mode ~sync-mode
          :tables ~tables}
   :handler {:fn ~handler-sym}})")

(defn xtalk-contract-input
  "Template input for the `:xtalk-contracts` target."
  {:added "4.0"}
  [{:keys [contract-sym wrapper-sym fn-def sync-spec]}]
  {'contract-sym contract-sym
   'id          (types/normalize-key (:name fn-def))
   'sync-mode   (:mode sync-spec)
   'tables      (vec (:tables sync-spec))
   'handler-sym (or wrapper-sym
                    (symbol (or (:ns fn-def) "")
                            (name (:name fn-def))))})

(def +targets+
  {:xtalk-contracts {:template (gen/get-template XTALK_CONTRACT_TEMPLATE
                                                 xtalk-contract-input)
                     :suffix   "-contract"}})

(defn target-entry
  "Creates a target emission entry for an API-server target."
  {:added "4.0"}
  [fn-def target]
  (let [sync-spec   (server-db/infer-sync-spec fn-def)
        target-meta (get +targets+ target)
        base-name   (name (:name fn-def))
        suffix      (:suffix target-meta)
        emitted-sym (symbol (str base-name suffix))]
    {:target target
     :fn-def fn-def
     :sync-spec sync-spec
     :emitted-sym emitted-sym}))

(defn emit-target
  "Emits generated source for a single API-server target."
  {:added "4.0"}
  [fn-def target]
  (let [{:keys [template]} (get +targets+ target)
        {:keys [mode] :as sync-spec} (server-db/infer-sync-spec fn-def)]
    (when (and template
               (not= :off mode))
      (let [base-name (name (:name fn-def))
            suffix    (get-in +targets+ [target :suffix])
            emitted-sym (symbol (str base-name suffix))
            input     {:fn-def fn-def
                       :sync-spec sync-spec
                       :wrapper-sym emitted-sym
                       :contract-sym emitted-sym}]
        (gen/fill-template template input)))))

(defn emit-targets
  "Emits generated source for multiple API-server targets."
  {:added "4.0"}
  [fn-def & [targets]]
  (into {}
        (keep (fn [target]
                (when-let [out (emit-target fn-def target)]
                  [target out])))
        (or targets +default-targets+)))

(defn list-targets
  "Lists supported API-server generation targets."
  {:added "4.0"}
  []
  (keys +targets+))
