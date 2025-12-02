(ns std.lang.model.spec-haskell
  (:require [std.lang.base.emit :as emit]
            [std.lang.base.emit-fn :as emit-fn]
            [std.lang.base.emit-data :as data]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.util :as ut]
            [std.lang.base.book :as book]
            [std.lang.base.script :as script]
            [std.string :as str]
            [std.lib :as h]))

(defn haskell-typesystem
  "emit haskell types"
  {:added "4.0"}
  [arr grammar mopts]
  (let [[sym & more] (rest arr)]
    (str sym " " (str/join " "
                           (map (fn [input]
                                  (cond (string? input)
                                        input

                                        :else
                                        (emit/emit-main input grammar mopts)))
                                more)))))

(defn haskell-vector
  "emit haskell vectors and types"
  {:added "4.0"}
  ([arr grammar mopts]
   (let [sym (first arr)]
     (cond (= :> sym)
           (haskell-typesystem arr grammar mopts)

           :else
           (data/emit-coll :vector arr grammar mopts)))))

(defn emit-raw-str
  "emits a raw string"
  {:added "4.0"}
  [[_ s] grammar mopts]
  s)

(defn emit-indent-body
  "indents the body"
  {:added "4.0"}
  [[_ form] grammar mopts]
  (let [s (emit/emit-main form grammar mopts)]
    (str/indent s 2)))

(defn tf-defn
  "custom defn for Haskell"
  {:added "4.0"}
  [[_ sym args & body]]
  (let [ret-type (-> sym meta :tag)
        sym      (if (string? sym) (symbol sym) sym)
        args-emit (map (fn [arg]
                        (cond (vector? arg)
                              (let [[t n] arg] n)
                              :else arg))
                       args)
        body-emit (if (> (count body) 1)
                    (cons 'do body)
                    (first body))
        sig (if ret-type
              (let [arg-types (map (fn [arg]
                                     (if (vector? arg)
                                       (first arg)
                                       (or (-> arg meta :tag) "Object")))
                                   args)]
                (str (ut/sym-default-str sym) " :: " (str/join " -> " (map str arg-types)) " -> " ret-type))
              nil)]
    (if sig
        (list :lines (list :- sig) (list :- sym (list :h-args args-emit) "=" body-emit))
        (list :- sym (list :h-args args-emit) "=" body-emit))))

(defn tf-case
  "transforms case"
  {:added "4.0"}
  [[_ val & clauses]]
  (let [clauses (partition 2 clauses)
        out (map (fn [[pattern result]]
                   (list :% pattern (list :raw-str " -> ") (list :% result)))
                 clauses)]
    (list :%
          (list :% (list :raw-str "case ") val (list :raw-str " of\n"))
          (list :indent-body (apply list :lines out)))))

(defn tf-if
  "transforms if"
  {:added "4.0"}
  [[_ cond then else]]
  (list :% (list :raw-str "if ") cond (list :raw-str " then ") then (list :raw-str " else ") else))

(defn tf-let
  "transforms let"
  {:added "4.0"}
  [[_ bindings body]]
  (let [bindings (partition 2 bindings)
        out (map (fn [[sym val]]
                   (list :% sym (list :raw-str " = ") val))
                 bindings)]
    (list :%
          (list :raw-str "let\n")
          (list :indent-body (apply list :lines out))
          (list :raw-str "\nin ") body)))

(defn tf-lambda
  "transforms fn/lambda"
  {:added "4.0"}
  [[_ args body]]
  (list :% (list :raw-str "\\ ") (list :h-args args) (list :raw-str " -> ") body))

(defn tf-do
  "transforms do"
  {:added "4.0"}
  [[_ & body]]
  (list :% (list :raw-str "do\n")
        (list :indent-body (apply list :lines body))))

(defn haskell-args
  "custom haskell arguments emission (space separated)"
  {:added "4.0"}
  [[_ args] grammar mopts]
  (let [args (cond (and (list? args) (= 'quote (first args)))
                   (second args)

                   (coll? args) args

                   :else [args])]
    (str/join " "
              (map (fn [arg]
                     (emit/emit-main arg grammar mopts))
                   args))))

(def +features+
  (-> (grammar/build :exclude [:control-try-catch
                               :class
                               :macro-arrow
                               :control-base
                               :control-general])
      (grammar/build:override
       {:defn    {:macro #'tf-defn :emit :macro}
        :fn      {:macro #'tf-lambda :emit :macro}
        :if      {:op :if :symbol #{'if} :emit :macro :macro #'tf-if :type :macro}
        :case    {:op :case :symbol #{'case} :emit :macro :macro #'tf-case :type :macro}})
      (grammar/build:extend
       {:let:h   {:op :let:h :symbol #{'let:h 'let} :emit :macro :macro #'tf-let :type :macro}
        :do      {:op :do :symbol #{'do} :emit :macro :macro #'tf-do :type :macro}
        :cons    {:op :cons :symbol #{'cons} :emit :infix :raw ":"}
        :concat  {:op :concat :symbol #{'concat} :emit :infix :raw "++"}
        :h-args  {:op :h-args :symbol #{:h-args} :emit #'haskell-args}
        :raw-str {:op :raw-str :symbol #{:raw-str} :emit #'emit-raw-str}
        :indent-body {:op :indent-body :symbol #{:indent-body} :emit #'emit-indent-body}
        :%       {:op :% :symbol #{:%} :emit :squash}
        :lines   {:op :lines :symbol #{:lines} :emit :free :sep "\n"}})))

(def +sym-replace+
  {\- "_"
   \> ">"
   \< "<"})

(def +template+
  (->> {:banned #{:set :map :regex}
        :highlight '#{case if then else let in module where import qualified as type data newtype class instance deriving do return}
        :default {:comment   {:prefix "--"}
                  :common    {:statement ""
                              :namespace-full "."
                              :namespace-sep  "."}
                  :index     {:offset 0 :end-inclusive false}
                  :block     {:parameter {:start " " :end " "}
                              :body      {:start "" :end ""}}
                  :function  {:raw ""
                              :args {:sep " "}}
                  :invoke    {:sep " " :start " " :end ""}}
        :data    {:vector    {:start "[" :end "]" :space "" :custom #'haskell-vector}
                  :tuple     {:start "(" :end ")" :space ""}}
        :token   {:symbol    {:replace +sym-replace+}}
        :block  {:list       {:start "[" :end "]" :space ""}}
        :function {:defn      {:raw ""}
                   :lambda    {:raw "\\" :args {:start "" :end " -> "}}}
        :define   {:def       {:raw ""}}}
       (h/merge-nested (emit/default-grammar))))

(def +grammar+
  (grammar/grammar :haskell
    (grammar/to-reserved +features+)
    +template+))

(def +meta+
  (book/book-meta
   {:module-current   (fn [])
    :module-import    (fn [name {:keys [as qualified]} opts]
                        (let [parts ["import"]
                              parts (if qualified (conj parts "qualified") parts)
                              parts (conj parts (str name))
                              parts (if as (conj parts "as" (str as)) parts)]
                          (h/$ (:- ~@parts))))
    :module-export    (fn [{:keys [as refer]} opts])}))

(def +book+
  (book/book {:lang :haskell
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
