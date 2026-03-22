(ns std.lang.model.spec-bash
  (:require [clojure.string]
            [std.lang.base.book :as book]
            [std.lang.base.emit :as emit]
            [std.lang.base.emit-common :as common]
            [std.lang.base.emit-data :as data]
            [std.lang.base.emit-fn :as fn]
            [std.lang.base.emit-top-level :as top]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.script :as script]
            [std.lang.base.util :as ut]
            [std.lang.model.spec-xtalk]
            [std.lib.collection :as collection]
            [std.lib.foundation :as f]))

(defn bash-quote-item
  "quotes an item"
  {:added "4.0"}
  [v grammar mopts]
  (if (vector? v)
    (data/emit-data :free v grammar mopts)
    (str "\"" (.replaceAll ^String (common/*emit-fn* v grammar mopts)
                           "\"" "\\\\\"") "\"")))

(defn bash-set
  "quotes a string value"
  {:added "4.0"}
  [set grammar mopts]
  (bash-quote-item (first set) grammar mopts))

(defn bash-quote
  "quotes a string value"
  {:added "4.0"}
  [[_ item] grammar mopts]
  (bash-quote-item item grammar mopts))

(defn bash-dash-param
  "if keyword, replace `:` with `-`"
  {:added "4.0"}
  [k]
  (conj (if (keyword? k)
          (str "-" (name k))
          k)))

(defn bash-map
  "outputs a map"
  {:added "4.0"}
  [form grammar mopts]
  (let [arr (reduce-kv (fn [acc k v]
                         (cond-> acc
                           (not (false? v))  (conj (bash-dash-param k))
                           (not (boolean? v)) (conj v)))
                        []
                        form)]
    (clojure.string/join " " (common/emit-array arr grammar mopts))))

(defn bash-invoke
  "outputs an invocation (same as vector)"
  {:added "4.0"}
  [[f & args] grammar mopts]
  (let [args (map (fn [x]
                    (if (and (collection/form? x)
                             (not (get-in grammar [:reserved (first x)])))
                      (list '$ x)
                      x))
                  args)]
    (clojure.string/join " " (common/emit-array (cons f args) grammar mopts))))

(defn- bash-vector
  [arr grammar mopts]
  (clojure.string/join " " (common/emit-array arr grammar mopts)))

(defn bash-assign
  "outputs an assignment"
  {:added "4.0"}
  [[_ & args :as form] grammar mopts]
  (common/emit-assign "=" args (assoc-in grammar [:default :common :space] "") mopts))

(defn bash-var
  "transforms to var"
  {:added "4.0"}
  [[_ sym & args]]
  (let [val (last args)]
    (list :- :local (list := sym val))))

(defn bash-defn
  "transforms a function to allow for inputs"
  {:added "4.0"}
  [[_ sym args & body]]
  (let [vs (map-indexed (fn [i v]
                          (list 'var v (list :$ (inc i))))
                        args)]
    (apply list 'defn- sym []
           (concat vs body))))

(defn bash-defn-
  "emits a non paramerised version"
  {:added "4.0"}
  [[_ sym args & body] grammar mopts]
  (top/emit-top-level :defn (apply list nil sym [] body) grammar mopts))

(defn bash-expand
  "brace expansion syntax"
  {:added "4.0"}
  [[_ inner]]
  (list :% "${" (list '% inner) "}"))

(defn bash-subshell
  "brace subshell syntax"
  {:added "4.0"}
  [[_ inner]]
  (list :% "$(" (list '% inner) ")"))

(defn bash-test
  "constructs test syntax"
  {:added "4.0"}
  [[_ inner]]
  (list :% "[[ " (list '% (mapv bash-dash-param inner)) " ]]"))

(defn bash-pipeleft
  "constructs pipeleft syntax"
  {:added "4.0"}
  [[_ inner]]
  (list :% "<(" (list '% inner) ")"))

(defn bash-piperight
  "constructs pipeleft syntax"
  {:added "4.0"}
  [[_ inner]]
  (list :% ">(" (list '% inner) ")"))

(defn bash-here
  "construct here block and here string"
  {:added "4.0"}
  [[_ term lines]]
  (if (and (string? term) (empty? lines))
    (list :% "<<<" #{term})
    (list :% "<<" term
          (list \\ \\ (apply list 'do lines))
          (list \\ \\ term))))

(defn bash-check-fn
  "custom check for vectors"
  {:added "4.0"}
  [check]
  (if (vector? check)
    (list :test (apply vector check))
    check))

(defn bash-when
  "when support for custom vectors"
  {:added "4.0"}
  ([[_ check & body]]
   `(~'br* (~'if ~(bash-check-fn check) ~@body))))

(defn bash-if
  "if support for custom vectors"
  {:added "4.0"}
  ([[_ check then & [else]]]
   `(~'br*
     (~'if ~(bash-check-fn check) ~then)
     ~@(if else
         `[(~'else ~else)]))))

(defn bash-cond
  "cond support for custom vectors"
  {:added "4.0"}
  ([[_ check then & more]]
   (let [args (partition 2 more)
         [ei el] (if (= :else (first (last args)))
                   [(butlast args) (last args)]
                   [args nil])]
     `(~'br*
       (~'if ~(bash-check-fn check) ~then)
       ~@(map (fn [[check then]]
                `(~'elseif ~(bash-check-fn check) ~then))
              ei)
       ~@(if el
           `[(~'else ~(second el))])))))

(def +features+
  (-> (grammar/build :include [:builtin
                               :builtin-helper
                               :free-control
                               :free-literal
                               :math
                               :compare
                               :return
                               :vars
                               :control-base
                               :control-general
                               :block
                               :top-base
                               [:macro :include [:if :cond :when]]
                               :macro-case])
      (grammar/build:override
       {:mul       {:value true}
        :add       {:value true}
        :sub       {:value true}
        :div       {:value true}
        :eq        {:raw "=" :value true}
        :gt        {:value true}
        :lt        {:value true}
        :var       {:macro  #'bash-var  :emit :macro}
        :seteq     {:emit   #'bash-assign}
        :defn      {:symbol #{'defn} :macro #'bash-defn :emit :macro}
        :block     {:symbol #{:block}}
        :if        {:macro  #'bash-if}
        :when      {:macro  #'bash-when}
        :cond      {:macro  #'bash-cond}
        :quote     {:op :quote     :symbol '#{quote}  :emit #'bash-quote}})
      (grammar/build:extend
       {:defn-     {:op :defn-     :symbol #{'defn-}  :type :block :emit #'bash-defn-}
        :andflow   {:op :andflow   :symbol '#{&&}   :raw "&&" :value true  :emit :infix}
        :orflow    {:op :orflow    :symbol '#{||}   :raw "||" :value true  :emit :infix}
        :pipe      {:op :pipe      :symbol '#{>>}   :raw "|"  :emit :infix}
        :to        {:op :to        :symbol #{:..}   :emit :between      :raw ".."}
        :expand    {:op :expand    :symbol #{:$}    :emit :macro  :macro #'bash-expand :raw "$"}
        :test      {:op :test      :symbol #{:test} :emit :macro  :macro #'bash-test}
        :subshell  {:op :subshell  :symbol #{'$}    :raw "$" :emit :macro  :value true  :macro #'bash-subshell}
        :here      {:op :here      :symbol #{'<<}   :emit :macro  :macro #'bash-here}
        :toleft    {:op :toleft    :symbol #{'<$}   :raw "<" :value true :emit :macro  :macro #'bash-pipeleft}
        :toright   {:op :toright   :symbol #{'$>}   :raw ">" :value true :emit :macro  :macro #'bash-piperight}})))

(def +template+
  (->> {:banned #{:regex}
        :highlight '#{return break}
        :default {:common    {:statement ""
                              :start  "" :end ""
                              :sep ""
                              :namespace-full "_"
                              :space " "}
                  :invoke    {:custom #'bash-invoke}
                  :define    {:space ""}
                  :symbol    {:full {:replace {\. "_"
                                               \- "_"}
                                     :sep "_"}
                              :global identity}
                  :block     {:statement ""
                              :parameter {:start " " :end ""}
                              :body      {:start "; do" :end "done"}}}
        :token   {:nil       {:as "''"}
                  :string    {:custom #'identity}
                  :keyword   {:custom #'f/strn}}
        :data    {:map       {:custom #'bash-map}
                  :set       {:custom #'bash-set}
                  :vector    {:custom #'bash-vector}
                  :free      {:start "{" :end "}" :sep ","}}
        :block  {:branch     {:control   {:default   {:parameter {:start " " :end "; then"}
	                                              :body      {:start "" :end "" :append true}}
	                                  :elseif    {:raw "elif"}}
	                      :wrap      {:end "fi"}}
                 :switch     {:raw "case"
                              :body      {:start " in" :end "esac"}
                              :control {:default {:raw ""
                                                  :parameter {:start "" :end ""}
                                                  :body      {:start ")" :end ";;"}}}}}}
       (collection/merge-nested (update-in (emit/default-grammar)
                                  [:token :symbol]
                                  dissoc :replace))))

(def +grammar+
  (grammar/grammar :sh
    (grammar/to-reserved +features+)
    +template+))

(def +book+
  (book/book {:lang :bash
              :parent :xtalk
              :meta (book/book-meta {})
              :grammar +grammar+}))

(def +init+
  (script/install +book+))

(comment
  (./create-tests))
