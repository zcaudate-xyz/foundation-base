(ns hara.common.emit-rewrite
  (:require [hara.typed.xtalk-analysis :as xtalk-analysis]
            [hara.typed.xtalk-infer :as xtalk-infer]
            [hara.typed.xtalk-lower :as xtalk-lower]))

(defn stage-transforms
  "returns rewrite transforms for a compile stage"
  {:added "4.1"}
  [grammar stage]
  (vec (get-in grammar [:rewrite stage])))

(defn- xtalk-context
  [mopts]
  (let [module (:module mopts)
        entry  (:entry mopts)
        ns-sym (or (:ns mopts)
                   (:namespace entry)
                   (:namespace module)
                   (:id module))
        aliases (or (:aliases mopts)
                     (:aliases entry)
                     (:alias module))]
    (when (or ns-sym
              (seq aliases))
      {:ns ns-sym
       :aliases (or aliases {})
       :infer xtalk-infer/infer-type
       :env {}
       :preserve-unknown true})))

(defn- infer-environment
  [form ctx]
  (try
    (if-let [env (:env (xtalk-infer/infer-type form ctx))]
      (update ctx :env merge env)
      ctx)
    (catch Throwable _
      ctx)))

(defn- contains-dot?
  [form]
  (cond
    (seq? form)
    (or (= '. (first form))
        (some contains-dot? form))

    (vector? form)
    (some contains-dot? form)

    (map? form)
    (some contains-dot? (mapcat identity form))

    :else
    false))

(defn canonical-stage
  "rewrites canonical XTalk forms for XTalk entries"
  {:added "4.1"}
  [form {:keys [mopts]}]
  (let [source-entry (:entry mopts)
        entry (when (= :xtalk (get-in mopts [:entry :lang]))
                (:entry mopts))
        ctx (or (:hara/xtalk-context mopts)
                (when (nil? source-entry)
                  (xtalk-context (assoc mopts :entry entry))))
        fn-ref (when (and (:module entry) (:id entry))
                 (symbol (str (:module entry)) (name (:id entry))))
        fn-def (when fn-ref
                 (xtalk-analysis/resolve-function-def fn-ref))
        inferred-ctx (when fn-def
                       (merge (or ctx {})
                              {:infer xtalk-infer/infer-type
                               :env (zipmap (map :name (:inputs fn-def))
                                            (map :type (:inputs fn-def)))
                               :preserve-unknown true}))
        ctx (or inferred-ctx ctx)]
    (cond
      ctx
      (xtalk-lower/lower-form form
                              (if (contains-dot? form)
                                (infer-environment form ctx)
                                ctx))

      entry
      (xtalk-lower/lower-form form (or inferred-ctx
                                        {:preserve-unknown true}))

      :else
      form)))

(defn rewrite-stage
  "applies language rewrite transforms for a compile stage"
  {:added "4.1"}
  [stage form grammar mopts]
  (let [canonical (if (= :canonical stage)
                    form
                    (canonical-stage form {:stage :canonical
                                           :grammar grammar
                                           :mopts mopts}))]
    (reduce (fn [acc transform]
              (transform acc {:stage stage
                              :grammar grammar
                              :mopts mopts}))
            canonical
            (stage-transforms grammar stage))))
