(ns std.lang.model-annex.spec-ocaml
  (:require [clojure.string]
            [std.lang.base.book :as book]
            [std.lang.base.emit :as emit]
            [std.lang.base.emit-common :as common]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.script :as script]
            [std.lang.model.spec-xtalk]
            [std.lib.collection :as collection]
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

(defn emit-lines-with
  "joins forms with a separator"
  {:added "4.1"}
  [[_ sep & forms] grammar mopts]
  (clojure.string/join sep
                       (map #(emit/emit-main % grammar mopts)
                            forms)))

(defn ml-invoke
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

(defn ml-args
  "emit OCaml arguments"
  {:added "4.1"}
  [[_ args] grammar mopts]
  (->> (if (coll? args) args [args])
       (map #(emit/emit-main % grammar mopts))
       (clojure.string/join " ")))

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

(defn body-expr
  "wraps multi-form bodies into a sequence"
  {:added "4.1"}
  [body]
  (if (> (count body) 1)
    (list :%
          (list :raw-str "begin\n")
          (list :indent-body
                (apply list :lines-with (cons ";\n" body)))
          (list :raw-str "\nend"))
    (first body)))

(defn tf-defn
  "custom defn for OCaml"
  {:added "4.1"}
  [[_ sym args & body]]
  (let [body-emit (body-expr body)]
    (list :%
          (list :raw-str "let rec ")
          sym
          (list :raw-str " ")
          (list :ml-args args)
          (list :raw-str " = ")
          body-emit)))

(defn tf-match
  "transforms match"
  {:added "4.1"}
  [[_ val & clauses]]
  (let [clauses (parse-match-clauses clauses)
        out (map (fn [{:keys [pattern guard body]}]
                   (apply list :%
                          (concat [(list :raw-str "| ")
                                   pattern]
                                  (when guard
                                    [(list :raw-str " when ")
                                     guard])
                                  [(list :raw-str " -> ")
                                   body])))
                 clauses)]
    (list :%
          (list :raw-str "match ")
          val
          (list :raw-str " with\n")
          (list :indent-body (apply list :lines out)))))

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
                                      (list :raw-str " = ")
                                      val))
                              bindings)]
    (list :%
          (apply list :lines lines)
          (list :raw-str "\nin ")
          (body-expr body))))

(defn tf-lambda
  "transforms lambda"
  {:added "4.1"}
  [[_ args & body]]
  (list :%
        (list :raw-str "fun ")
        (list :ml-args args)
        (list :raw-str " -> ")
        (body-expr body)))

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
       {:ml-args     {:op :ml-args :symbol #{:ml-args} :emit #'ml-args}
        :raw-str     {:op :raw-str :symbol #{:raw-str} :emit #'emit-raw-str}
        :indent-body {:op :indent-body :symbol #{:indent-body} :emit #'emit-indent-body}
        :lines-with  {:op :lines-with :symbol #{:lines-with} :emit #'emit-lines-with}
        :%           {:op :% :symbol #{:%} :emit :squash}
        :lines       {:op :lines :symbol #{:lines} :emit :free :sep "\n"}})))

(def +template+
  (->> {:default {:comment   {:prefix "(*"}
                  :common    {:statement ""
                              :namespace-full "."
                              :namespace-sep "."}
                  :block     {:parameter {:start " " :end " "}
                              :body      {:start "" :end ""}}
                  :function  {:raw ""
                              :args {:sep " "}}
                  :invoke    {:sep " " :start " " :end ""
                              :custom #'ml-invoke}}
        :function {:defn {:raw ""}}}
       (collection/merge-nested (emit/default-grammar))))

(def +grammar+
  (grammar/grammar :ml
    (grammar/to-reserved +features+)
    +template+))

(def +meta+
  (book/book-meta {}))

(def +book+
  (book/book {:lang :ocaml
              :parent :xtalk
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
