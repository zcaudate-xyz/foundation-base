(ns code.test.compile.rewrite
  (:require [std.lib.collection :as c]
            [std.lib.result :as res]
            [code.test.base.process :as process]
            [code.test.compile.types :as types]))

(declare rewrite-nested-checks)

(defn rewrite-list
  "rewrites a list containing checks"
  [form]
  (loop [acc []
         [curr & rest :as forms] form]
    (if (empty? forms)
      (seq acc)
      (let [[next & next-rest] rest]
        (if (= next '=>)
          (let [[target & target-rest] next-rest]
            (if (nil? target)
              ;; Trailing =>, just keep going
              (recur (conj acc (rewrite-nested-checks curr)) rest)
              ;; Found A => B, rewrite to check
              ;; Capture values using try/catch wrappers
              (let [input-wrapper  `(try {:status :success :data ~curr}
                                         (catch Throwable ~'t {:status :exception :data ~'t}))
                    output-wrapper `(try {:status :success :data ~target}
                                         (catch Throwable ~'t {:status :exception :data ~'t}))
                    line   (or (:line (meta next)) (:line (meta curr)) (:line (meta form)))
                    column (or (:column (meta next)) (:column (meta curr)) (:column (meta form)))
                    check-form `(process/process
                                 {:type :test-equal
                                  :input  {:form (quote ~curr)   :value (res/result (assoc ~input-wrapper :form (quote ~curr)))}
                                  :output {:form (quote ~target) :value (res/result (assoc ~output-wrapper :form (quote ~target)))}
                                  :meta (merge types/*compile-meta*
                                               ~{:path types/*file-path*}
                                               ~(meta form)
                                               {:line ~line :column ~column}
                                               {:parent-form (quote ~form)})})]
                (recur (conj acc check-form) target-rest))))
          ;; Not a check, recurse on curr and continue
          (recur (conj acc (rewrite-nested-checks curr)) rest))))))

(defn rewrite-nested-checks
  "rewrites code to replace `a => b` with check calls"
  [form]
  (cond (and (seq? form) (= 'quote (first form)))
        form

        (c/form? form)
        (if (some #(= % '=>) form)
          (with-meta (rewrite-list form) (meta form))
          (with-meta (apply list (map rewrite-nested-checks form)) (meta form)))

        (coll? form)
        (if (map? form)
          (into {} (mapv (fn [[k v]] [(rewrite-nested-checks k) (rewrite-nested-checks v)]) form))
          (into (empty form) (mapv rewrite-nested-checks form)))

        :else form))
