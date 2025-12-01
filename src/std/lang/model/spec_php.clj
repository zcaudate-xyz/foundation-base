(ns std.lang.model.spec-php
  (:require [std.lang.base.emit :as emit]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.emit-common :as common]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.emit-preprocess :as preprocess]
            [std.lang.base.emit-top-level :as top]
            [std.lang.base.emit-data :as data]
            [std.lang.base.book :as book]
            [std.lang.base.script :as script]
            [std.lang.base.util :as ut]
            [std.lang.model.spec-xtalk]
            [std.lang.model.spec-xtalk.fn-php :as fn]
            [std.string :as str]
            [std.lib :as h]))

(defn is-capitalized?
  "checks if string is capitalized"
  [s]
  (let [s (name s)]
    (and (not (empty? s))
         (Character/isUpperCase (char (first s))))))

(defn php-symbol
  "emit php symbol with $ prefix if it's a variable"
  [sym grammar mopts]
  (cond (or (:php/func mopts)
            (:php/func (meta sym))
            (is-capitalized? sym))
        (common/emit-symbol sym grammar mopts)

        (namespace sym)
        (let [ns (namespace sym)
              nm (name sym)]
          (str (str/replace ns "." "\\") "::" nm))

        :else
        (str "$" (common/emit-symbol sym grammar mopts))))

(defn php-invoke-args
  [args grammar mopts]
  (str/join ", " (common/emit-invoke-args args grammar mopts)))

(defn php-invoke
  "emit php function call"
  [[f & args] grammar mopts]
  (let [f-str (common/*emit-fn* f grammar (assoc mopts :php/func true))
        args-str (php-invoke-args args grammar (dissoc mopts :php/func))]
    (str f-str "(" args-str ")")))

(defn php-var
  "emit php variable declaration"
  [[_ sym & args]]
  (let [val (last args)]
    (list := sym val)))

(defn php-defn
  "emit php function definition"
  [[_ sym args & body]]
  (let [grammar preprocess/*macro-grammar*
        mopts   preprocess/*macro-opts*
        sym-str (common/emit-symbol sym grammar (assoc mopts :php/func true))
        args-str (php-invoke-args args grammar (dissoc mopts :php/func))
        body-str (common/*emit-fn* (cons 'do body) grammar mopts)]
    (list :- (str "function " sym-str "(" args-str ") {\n" body-str "\n}"))))

(defn php-defn-
  "emit php anonymous function"
  [[_ args & body]]
  (let [grammar preprocess/*macro-grammar*
        mopts   preprocess/*macro-opts*
        args-str (php-invoke-args args grammar (dissoc mopts :php/func))
        body-str (common/*emit-fn* (cons 'do body) grammar mopts)]
    (list :- (str "function (" args-str ") {\n" body-str "\n}"))))

(defn php-array
  "emit php array"
  [arr grammar mopts]
  (str "[" (str/join ", " (common/emit-array arr grammar mopts)) "]"))

(defn php-map
  "emit php associative array"
  [m grammar mopts]
  (let [entries (map (fn [[k v]]
                       (str (common/*emit-fn* k grammar mopts)
                            " => "
                            (common/*emit-fn* v grammar mopts)))
                     m)]
    (str "[" (str/join ", " entries) "]")))

(defn php-dot-string
  [obj props grammar mopts]
  (loop [curr-str (common/*emit-fn* obj grammar mopts)
         curr-obj obj
         [p & more] props]
    (if p
      (let [static?  (and (symbol? curr-obj) (is-capitalized? curr-obj))
            sep      (if static? "::" "->")
            next-val (if (list? p) (first p) p)
            next-str (if (list? p)
                       (str (common/*emit-fn* (first p) grammar (assoc mopts :php/func true))
                            "(" (php-invoke-args (rest p) grammar mopts) ")")
                       (common/emit-symbol p grammar (assoc mopts :php/func true)))]
        (recur (str curr-str sep next-str)
               next-val
               more))
      curr-str)))

(defn php-dot
  "emit php object access ->"
  [[_ obj & props]]
  (let [grammar preprocess/*macro-grammar*
        mopts   preprocess/*macro-opts*]
    (list :- (php-dot-string obj props grammar mopts))))

(defn php-new
  "emit new Class()"
  [[_ cls & args]]
  (let [grammar preprocess/*macro-grammar*
        mopts   preprocess/*macro-opts*
        cls-str (common/emit-symbol cls grammar (assoc mopts :php/func true))
        args-str (php-invoke-args args grammar mopts)]
    (list :- (str "new " cls-str "(" args-str ")"))))

(def +features+
  (-> (grammar/build :exclude [:pointer :block :data-range])
      (grammar/build:override
       {:var        {:macro #'php-var :emit :macro}
        :defn       {:macro #'php-defn :emit :macro}
        :fn         {:macro #'php-defn- :emit :macro}
        :index      {:macro #'php-dot :emit :macro}
        :new        {:macro #'php-new :emit :macro}
        :and        {:raw "&&"}
        :or         {:raw "||"}
        :not        {:raw "!"}
        :eq         {:raw "=="}
        :neq        {:raw "!="}
        :gt         {:raw ">"}
        :lt         {:raw "<"}
        :gte        {:raw ">="}
        :lte        {:raw "<="}})
       (grammar/build:override fn/+php+)
       (grammar/build:extend
        {:phparray   {:op :phparray :symbol #{'array} :raw "array"}
         :echo       {:op :echo :symbol #{'echo} :raw "echo" :emit :prefix}
         :die        {:op :die  :symbol #{'die}  :raw "die"  :emit :prefix}})))

(def +template+
  (->> {:banned #{:keyword}
        :allow   {:assign  #{:symbol}}
        :default {:common    {:statement ";"
                              :start  "<?php\n"
                              :end    "\n?>"}
                  :block     {:parameter {:start "(" :end ")"}
                              :body      {:start "{" :end "}"}}
                  :invoke    {:custom #'php-invoke}}
        :token   {:nil       {:as "null"}
                  :boolean   {:as (fn [b] (if b "true" "false"))}
                  :string    {:quote :single}
                  :symbol    {:custom #'php-symbol}}
        :data    {:vector    {:custom #'php-array}
                  :map       {:custom #'php-map}}
        :define  {:def       {:raw ""}
                  :defglobal {:raw ""}}}
       (h/merge-nested (emit/default-grammar))))

(def +grammar+
  (grammar/grammar :php
    (grammar/to-reserved +features+)
    +template+))

(def +book+
  (book/book {:lang :php
              :parent :xtalk
              :meta (book/book-meta {})
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
