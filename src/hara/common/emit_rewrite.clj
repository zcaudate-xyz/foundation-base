(ns hara.common.emit-rewrite
  (:require [hara.typed.xtalk-analysis :as xtalk-analysis]
            [hara.typed.xtalk-infer :as xtalk-infer]
            [hara.typed.xtalk-lower :as xtalk-lower]))

(defn stage-transforms
  "returns rewrite transforms for a compile stage"
  {:added "4.1"}
  [grammar stage]
  (vec (get-in grammar [:rewrite stage])))

(defn canonical-stage
  "rewrites canonical XTalk forms for XTalk entries"
  {:added "4.1"}
  [form {:keys [mopts]}]
  (let [ctx (:hara/xtalk-context mopts)
        entry (when (= :xtalk (get-in mopts [:entry :lang]))
                (:entry mopts))
        fn-ref (when (and (:module entry) (:id entry))
                 (symbol (str (:module entry)) (name (:id entry))))
        fn-def (when fn-ref
                 (xtalk-analysis/resolve-function-def fn-ref))
        inferred-ctx (when fn-def
                       {:infer xtalk-infer/infer-type
                        :env (zipmap (map :name (:inputs fn-def))
                                     (map :type (:inputs fn-def)))
                        :preserve-unknown true})]
    (cond
      ctx
      (xtalk-lower/lower-form form ctx)

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
