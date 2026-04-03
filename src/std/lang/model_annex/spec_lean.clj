(ns std.lang.model-annex.spec-lean
  (:require [clojure.string]
            [std.lang.base.book :as book]
            [std.lang.base.emit :as emit]
            [std.lang.base.emit-common :as common]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.script :as script]
            [std.lang.model.spec-xtalk]
            [std.lib.collection :as collection]
            [std.lib.foundation :as f]
            [std.string.prose :as prose]))

(defn emit-raw-str
  "emits a raw string"
  {:added "4.1"}
  [[_ s] grammar mopts]
  s)

(defn emit-indent-body
  "indents the body"
  {:added "4.1"}
  [[_ form] grammar mopts]
  (let [s (emit/emit-main form grammar mopts)]
    (prose/indent s 2)))

(defn lean-args
  "emit Lean arguments"
  {:added "4.1"}
  [[_ args] grammar mopts]
  (->> (if (coll? args) args [args])
       (map #(emit/emit-main % grammar mopts))
       (clojure.string/join " ")))

(defn lean-invoke
  "wraps wrappable arguments for function application"
  {:added "4.1"}
  [[sym & args] grammar mopts]
  (let [emit-arg (fn [arg]
                   (if (common/emit-wrappable? arg grammar)
                     (str "(" (emit/emit-main arg grammar mopts) ")")
                     (emit/emit-main arg grammar mopts)))]
    (str (emit/emit-main sym grammar mopts)
         (when (seq args) " ")
         (clojure.string/join " " (map emit-arg args)))))

(defn parse-match-clauses
  "parses shared `match` clauses"
  {:added "4.1"}
  [clauses]
  (mapv (fn [[pattern result]]
          (if (and (vector? result)
                   (= :when (first result)))
            {:pattern pattern
             :guard   (second result)
             :body    (nth result 2)}
            {:pattern pattern
             :body    result}))
        (partition 2 clauses)))

(declare match-form)

(defn catch-all-pattern?
  "returns true for broad variable-style fallback patterns"
  {:added "4.1"}
  [pattern]
  (or (= '_ pattern)
      (and (symbol? pattern)
           (nil? (namespace pattern)))))

(defn guarded-body
  "lowers guarded bodies into nested `if` and fallback `match`"
  {:added "4.1"}
  [expr {:keys [guard body]} remaining]
  (if guard
    (list :%
          (list :raw-str "if ")
          guard
          (list :raw-str " then ")
          body
          (list :raw-str " else ")
          (if (seq remaining)
            (match-form expr remaining)
            (f/error "Guarded match requires a fallback clause"
                     {:expr expr :body body})))
    body))

(defn match-form
  "emits a Lean match form"
  {:added "4.1"}
  [expr clauses]
  (let [out (loop [i 0
                   remaining clauses
                   acc []]
              (if-let [{:keys [pattern guard] :as clause} (first remaining)]
                (recur (inc i)
                       (if (and guard
                                (catch-all-pattern? pattern))
                         []
                         (subvec remaining 1))
                       (conj acc
                             (list :%
                                   (list :raw-str "| ")
                                   pattern
                                   (list :raw-str " => ")
                                   (guarded-body expr clause (subvec clauses (inc i))))))
                acc))]
    (list :%
          (list :raw-str "match ")
          expr
          (list :raw-str " with\n")
          (list :indent-body (apply list :lines out)))))

(defn tf-defn
  "custom defn for Lean"
  {:added "4.1"}
  [[_ sym args & body]]
  (list :%
        (list :raw-str "def ")
        sym
        (list :raw-str " ")
        (list :lean-args args)
        (list :raw-str " := ")
        (first body)))

(defn tf-match
  "transforms match"
  {:added "4.1"}
  [[_ expr & clauses]]
  (match-form expr (parse-match-clauses clauses)))

(defn tf-if
  "transforms if"
  {:added "4.1"}
  [[_ cond then else]]
  (list :%
        (list :raw-str "if ")
        cond
        (list :raw-str " then ")
        then
        (list :raw-str " else ")
        else))

(defn tf-letrec
  "transforms letrec"
  {:added "4.1"}
  [[_ bindings & body]]
  (let [bindings (partition 2 bindings)
        lines    (map-indexed (fn [i [sym val]]
                                (list :%
                                      (list :raw-str (if (zero? i) "let rec " "and "))
                                      sym
                                      (list :raw-str " := ")
                                      val))
                              bindings)]
    (list :%
          (apply list :lines lines)
          (list :raw-str "\n")
          (first body))))

(defn tf-lambda
  "transforms lambda"
  {:added "4.1"}
  [[_ args & body]]
  (list :%
        (list :raw-str "fun ")
        (list :lean-args args)
        (list :raw-str " => ")
        (first body)))

(def +features+
  (-> (merge (grammar/build :exclude [:control-try-catch
                                      :class
                                      :macro-arrow
                                      :macro-let
                                      :macro-case])
             (grammar/build-functional-core))
      (grammar/build:override
       {:defn    {:macro #'tf-defn :emit :macro}
        :fn      {:macro #'tf-lambda :emit :macro}
        :if      {:op :if :symbol #{'if} :emit :macro :macro #'tf-if}
        :match   {:op :match :symbol #{'match} :emit :macro :macro #'tf-match :type :block}
        :letrec  {:op :letrec :symbol #{'letrec 'letfn} :emit :macro :macro #'tf-letrec :type :block}})
      (grammar/build:extend
       {:lean-args   {:op :lean-args :symbol #{:lean-args} :emit #'lean-args}
        :raw-str     {:op :raw-str :symbol #{:raw-str} :emit #'emit-raw-str}
        :indent-body {:op :indent-body :symbol #{:indent-body} :emit #'emit-indent-body}
        :%           {:op :% :symbol #{:%} :emit :squash}
        :lines       {:op :lines :symbol #{:lines} :emit :free :sep "\n"}})))

(def +template+
  (->> {:default {:comment   {:prefix "--"}
                  :common    {:statement ""
                              :namespace-full "."
                              :namespace-sep "."}
                  :block     {:parameter {:start " " :end " "}
                              :body      {:start "" :end ""}}
                  :function  {:raw ""
                              :args {:sep " "}}
                  :invoke    {:sep " " :start " " :end ""
                              :custom #'lean-invoke}}
        :function {:defn {:raw ""}}}
       (collection/merge-nested (emit/default-grammar))))

(def +grammar+
  (grammar/grammar :lean
    (grammar/to-reserved +features+)
    +template+))

(def +meta+
  (book/book-meta {}))

(def +book+
  (book/book {:lang :lean
              :parent :xtalk
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
