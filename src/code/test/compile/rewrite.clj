(ns code.test.compile.rewrite
  (:require [std.lib :as h]
            [code.test.base.process :as process]))

(def ^:dynamic *path* nil)

(def => '=>)

(declare rewrite-nested-checks)

(defn rewrite-list
  "rewrites a list containing checks"
  [form]
  (loop [acc []
         [curr & rest :as forms] form]
    (if (empty? forms)
      (seq acc)
      (let [[next & next-rest] rest]
        (if (= next =>)
          (let [[target & target-rest] next-rest]
            (if (nil? target)
              ;; Trailing =>, just keep going
              (recur (conj acc (rewrite-nested-checks curr)) rest)
              ;; Found A => B, rewrite to check
              ;; Capture values using try/catch wrappers
              (let [input-wrapper  `(try {:status :success :data ~curr}
                                         (catch Throwable t# {:status :exception :data t#}))
                    output-wrapper `(try {:status :success :data ~target}
                                         (catch Throwable t# {:status :exception :data t#}))
                    line   (or (:line (meta next)) (:line (meta curr)) (:line (meta form)))
                    column (or (:column (meta next)) (:column (meta curr)) (:column (meta form)))
                    check-form `(process/process
                                  {:type :test-equal
                                   :input {:form (quote ~curr) :value ~input-wrapper}
                                   :output {:form (quote ~target) :value ~output-wrapper}
                                   :meta (merge ~(when *path* {:path *path*})
                                                ~(meta form)
                                                {:line ~line :column ~column}
                                                {:parent-form (quote ~form)})})]
                (recur (conj acc check-form) target-rest))))
          ;; Not a check, recurse on curr and continue
          (recur (conj acc (rewrite-nested-checks curr)) rest))))))

(defn rewrite-nested-checks
  "rewrites code to replace `a => b` with check calls"
  [form]
  (cond (list? form)
        (if (some #(= % =>) form)
          (rewrite-list form)
          (map rewrite-nested-checks form))

        (coll? form)
        (if (map? form)
          (into {} (map (fn [[k v]] [(rewrite-nested-checks k) (rewrite-nested-checks v)]) form))
          (into (empty form) (map rewrite-nested-checks form)))

        :else form))
