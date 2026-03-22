(ns rt.postgres.compile.server-db
  (:require [clojure.walk :as walk]
            [code.framework.generate :as gen]
            [rt.postgres.grammar.typed-analyze :as analyze]
            [rt.postgres.grammar.typed-common :as types]))

(def ^:private +default-targets+
  [:supabase-db])

(def ^:private +default-sync-fn+
  'rt.postgres.compile.server-db/db-sync-merge)

(def ^:private +mutation-ops+
  #{:insert :update :delete :upsert})

(defn- collect-pg-ops
  [form]
  (let [ops (atom #{})]
    (walk/postwalk
     (fn [x]
       (when (seq? x)
         (when-let [info (get analyze/+pg-operations+ (first x))]
           (swap! ops conj (:op info))))
       x)
     form)
    @ops))

(defn infer-sync-spec
  "Infers a minimal sync generation spec for a FnDef."
  {:added "4.0"}
  [fn-def]
  (let [body-meta  (:body-meta fn-def)
        sync-mode  (:sync/mode body-meta)
        sync-table (:sync/tables body-meta)
        inferred   (analyze/cached-infer fn-def)
        operations (collect-pg-ops (:raw-body body-meta))
        tables     (cond
                     (coll? sync-table)
                     (vec (map name sync-table))

                     (and (= :shaped (:kind inferred))
                          (:table inferred))
                     [(name (:table inferred))]

                     (and (= :array (:kind inferred))
                          (:table inferred))
                     [(name (:table inferred))]

                     (and (= :shaped (:kind inferred))
                          (types/jsonb-shape? (:shape inferred))
                          (-> inferred :shape :source-table))
                     [(-> inferred :shape :source-table name)]

                     (and (types/jsonb-shape? inferred)
                          (:source-table inferred))
                     [(-> inferred :source-table name)]

                     :else [])
        mutating?  (boolean
                    (and (some +mutation-ops+ operations)
                         (not (contains? #{:manual :off} sync-mode))))
        mode       (cond
                     (= :off sync-mode) :off
                     (= :manual sync-mode) :manual
                     (and mutating? (seq tables)) :auto
                     :else :none)]
    {:mode mode
     :mutating? mutating?
     :operations operations
     :tables tables
     :inferred inferred
     :sync-fn (or (:sync/fn body-meta) +default-sync-fn+)
     :confidence (if (seq tables) :high :low)}))

(defn db-sync-merge
  "Default sync helper used by generated DB-server wrappers."
  {:added "4.0"}
  [output table-names]
  (cond
    (or (nil? output)
        (contains? output :db/sync)
        (empty? table-names))
    output

    :else
    (assoc output :db/sync
           (into {}
                 (map (fn [table-name]
                        [table-name [output]])
                      table-names)))))

(def ^:private SUPABASE_DB_TEMPLATE
  "
(defn.pg ~wrapper-sym
  ~input
  (let [out (~inner-sym ~@call-args)]
    (~sync-fn out ~tables)))")

(defn supabase-db-input
  "Template input for the `:supabase-db` target."
  {:added "4.0"}
  [{:keys [wrapper-sym fn-def sync-spec]}]
  (let [inner-sym (symbol (or (:ns fn-def) "")
                          (name (:name fn-def)))
        inputs    (:inputs fn-def)
        call-args (mapv :name inputs)]
    {'wrapper-sym wrapper-sym
     'input       (vec (mapcat (fn [arg]
                                 [(:type arg) (:name arg)])
                               inputs))
     'inner-sym   inner-sym
     'call-args   call-args
     'sync-fn     (:sync-fn sync-spec)
     'tables      (vec (:tables sync-spec))}))

(def +targets+
  {:supabase-db {:template (gen/get-template SUPABASE_DB_TEMPLATE
                                             supabase-db-input)
                 :suffix   "-sync"}})

(defn target-entry
  "Creates a target emission entry for a DB-server target."
  {:added "4.0"}
  [fn-def target]
  (let [sync-spec   (infer-sync-spec fn-def)
        target-meta (get +targets+ target)
        base-name   (name (:name fn-def))
        suffix      (:suffix target-meta)
        emitted-sym (symbol (str base-name suffix))]
    {:target target
     :fn-def fn-def
     :sync-spec sync-spec
     :emitted-sym emitted-sym}))

(defn emit-target
  "Emits generated source for a single DB-server target."
  {:added "4.0"}
  [fn-def target]
  (let [{:keys [template]} (get +targets+ target)
        {:keys [mode] :as sync-spec} (infer-sync-spec fn-def)]
    (when (and template
               (not= :off mode)
               (or (= :auto mode)
                   (= :manual mode)))
      (let [base-name (name (:name fn-def))
            suffix    (get-in +targets+ [target :suffix])
            emitted-sym (symbol (str base-name suffix))
            input     {:fn-def fn-def
                       :sync-spec sync-spec
                       :wrapper-sym emitted-sym}]
        (gen/fill-template template input)))))

(defn emit-targets
  "Emits generated source for multiple DB-server targets."
  {:added "4.0"}
  [fn-def & [targets]]
  (into {}
        (keep (fn [target]
                (when-let [out (emit-target fn-def target)]
                  [target out])))
        (or targets +default-targets+)))

(defn list-targets
  "Lists supported DB-server generation targets."
  {:added "4.0"}
  []
  (keys +targets+))
