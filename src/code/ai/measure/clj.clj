(ns code.ai.measure.clj
  (:require [std.lib :as h]
            [std.fs :as fs]
            [code.ai.measure.common :as common])
  (:import (java.io PushbackReader StringReader)))

;; Clojure Specific Configuration
(def ^:dynamic *clj-config*
  (merge common/*config*
         {:control-flow-types #{'if 'when 'cond 'case 'loop 'recur 'try 'catch 'doseq 'for
                                'let 'letfn 'fn 'defn 'def 'defmacro 'ns}
          :ignored-keys #{}}))

(defn- score-form
  [form depth config]
  (let [{:keys [base-score depth-factor control-flow-bonus control-flow-types]} config]
    (cond
      (sequential? form)
      (let [head (first form)
            is-control? (and (symbol? head) (contains? control-flow-types head))
            ;; If it's a list, it's a structural element.
            self-score (+ base-score
                          (* depth depth-factor)
                          (if is-control? control-flow-bonus 0))
            children-score (reduce (fn [acc child]
                                     (+ acc (score-form child (inc depth) config)))
                                   0
                                   (if (and is-control? (seq form)) (rest form) form))]
        (+ self-score children-score))

      (map? form)
      (reduce-kv (fn [acc k v]
                   (+ acc (score-form k (inc depth) config)
                          (score-form v (inc depth) config)))
                 (+ base-score (* depth depth-factor))
                 form)

      (set? form)
      (reduce (fn [acc v]
                (+ acc (score-form v (inc depth) config)))
              (+ base-score (* depth depth-factor))
              form)

      ;; Atoms (symbols, keywords, numbers, strings) - minimal score
      :else base-score)))

(defn- count-atoms
  [form config]
  (cond
    (sequential? form)
    (reduce + 1 (map #(count-atoms % config) form)) ;; 1 for container

    (map? form)
    (reduce + 1 (concat (map #(count-atoms % config) (keys form))
                        (map #(count-atoms % config) (vals form))))

    (set? form)
    (reduce + 1 (map #(count-atoms % config) form))

    :else 1))

(defn score-code
  "Calculates structural complexity for Clojure forms."
  ([forms]
   (score-code forms *clj-config*))
  ([forms config]
   (reduce + (map #(score-form % 0 config) forms))))

(defn count-code
  "Calculates base size (atom count) for Clojure forms."
  ([forms]
   (count-code forms *clj-config*))
  ([forms config]
   (reduce + (map #(count-atoms % config) forms))))

(defn read-all-forms
  [code-str]
  (let [rdr (PushbackReader. (StringReader. code-str))]
    (loop [forms []]
      (let [form (try (read rdr nil :eof)
                      (catch Throwable _ :eof))]
        (if (= form :eof)
          forms
          (recur (conj forms form)))))))

(defn generate-metrics
  "Generates metrics for Clojure code string."
  [code]
  (try
    (let [forms (read-all-forms code)
          complexity (score-code forms)
          base (count-code forms)
          surface (common/calculate-surface base complexity)]
      {:complexity complexity
       :surface    surface})
    (catch Throwable e
      (h/local :println "Error generating CLJ metrics:" (.getMessage e))
      {:complexity 0 :surface 0})))
