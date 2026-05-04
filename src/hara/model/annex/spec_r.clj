(ns hara.model.annex.spec-r
  (:require [clojure.string :as str]
            [hara.lang.base.book :as book]
            [hara.common.emit :as emit]
            [hara.common.emit-common :as common]
            [hara.common.emit-data :as data]
            [hara.common.emit-preprocess :as preprocess]
            [hara.common.emit-top-level :as top]
            [hara.common.grammar :as grammar]
            [hara.common.grammar-spec :as spec]
            [hara.lang.base.script :as script]
            [hara.lang.typed.xtalk-analysis :as xtalk-analysis]
            [hara.common.util :as ut]
            [hara.model.spec-xtalk]
            [hara.model.annex.spec-r.rewrite :as rewrite]
            [hara.model.annex.spec-xtalk.fn-r :as fn]
            [std.lib.collection :as collection]
            [std.lib.template :as template]))

(declare r-apply-optional-defaults)

(defn tf-defn
  "function declaration for python
 
   (tf-defn '(defn hello [x y] (return (+ x y))))
   => '(def hello (fn [x y] (return (+ x y))))
   
   (!.R
    (defn ^{:inner true}
      hello [x y] (+ x y))
    (hello 1 2))
  => 3"
  {:added "3.0"}
  ([[_ sym args & body]]
   (list 'def sym (apply list 'fn (r-apply-optional-defaults sym args) body))))

(defn- r-qualified-symbol
  [sym]
  (let [{:keys [module]} (preprocess/macro-opts)
        module-id (:id module)]
    (cond
      (or (nil? sym)
          (namespace sym)
          (:inner (meta sym))
          (nil? module-id))
      sym

      :else
      (symbol (name module-id) (name sym)))))

(defn- r-optional-input?
  [input]
  (= :maybe (get-in input [:type :kind])))

(defn- r-apply-optional-defaults
  [sym args]
  (if-not (and sym (vector? args))
    args
    (try
      (let [qualified      (r-qualified-symbol sym)
            fn-def         (xtalk-analysis/resolve-function-def qualified)
            inferred-count (when fn-def
                             (count (take-while r-optional-input?
                                                (reverse (:inputs fn-def)))))
            optional-count (when (and inferred-count
                                      (pos? inferred-count))
                             inferred-count)]
        (if (and optional-count (pos? optional-count))
          (let [optional-args (take-last optional-count args)]
            (if (not (neg? (collection/index-at #{:=} args)))
              args
              (vec
               (concat (drop-last optional-count args)
                       (mapcat (fn [arg]
                                 [arg := nil])
                               optional-args)))))
          args))
      (catch Throwable _
        args))))

(defn tf-infix-if
  "transform for infix if"
  {:added "4.0"}
  ([[_ expr & args]]
   (cond (= 1 (count args))
         (if (vector? (first args))
           (apply list (list :- "`if`") expr (first args))
           (list (list :- "`if`") expr (first args)))
         
         (= 2 (count args))
         (apply list (list :- "`if`") expr args)
         
         (<= 2 (count args))
         (list (list :- "`if`") expr (first args)
               (tf-infix-if (cons nil (rest (remove #(= % :else)
                                                    args))))))))

(defn tf-for-object
  "transform for `for:object`"
  {:added "4.0"}
  [[_ [[k v] m] & body]]
  (if (= k '_)
    (apply list 'for [v :in (list '% m)]
           body)
    (apply list 'for [k :in (list 'names m)]
           (concat (if (not= v '_)
                     [(list ':= v (list '. m [k]))])
                   body))))

(defn tf-for-array
  "transform for `for:array`"
  {:added "4.0"}
  [[_ [e arr] & body]]
  (if (vector? e)
    (let [[i e] e]
      (template/$ (do (var ~i := 0)
               (for [~e :in (% ~arr)]
                 (:= ~i (+ ~i 1))
                 ~@body))))
    (apply list 'for [e :in (list '% arr)]
                body)))

(defn tf-for-iter
  "transform for `for:iter`"
  {:added "4.0"}
  [[_ [e it] & body]]
  (template/$ (for [~e :in (% ~it)]
         ~@body)))

(defn tf-for-index
  "transform for `for:index`"
  {:added "4.0"}
  [[_ [i [start end step]] & body]]
  (template/$ (for [~i :in (seq ~start
                         ~end
                         ~(or step 1))]
         ~@body)))

(defn- r-token-boolean
  [bool]
  (if bool "TRUE" "FALSE"))

(defn r-map
  [m grammar mopts]
  (if (empty? m)
    "structure(list(), names=character())"
    (data/emit-coll :map m grammar mopts)))

(def +features+
  (-> (merge (grammar/build :include [:builtin
                                      :builtin-global
                                      :builtin-module
                                      :builtin-helper
                                      :free-control
                                      :free-literal
                                      :math
                                      :compare
                                      :logic
                                      :block
                                      :data-shortcuts
                                      :data-range
                                      :vars
                                      :fn
                                      :control-base
                                      :control-general
                                      :top-base
                                      :top-global
                                      :top-declare
                                      :for
                                      :coroutine
                                      :macro
                                      :macro-arrow
                                      :macro-let
                                      :macro-xor])
              (grammar/build-xtalk))
      (grammar/build:override
        {:seteq       {:op :seteq :symbol '#{:=} :raw "<-"}
         :mod         {:raw "%%"}
         :defn        {:op :defn  :symbol '#{defn}     :macro  #'tf-defn :type :macro}
         :inif        {:macro #'tf-infix-if   :emit :macro}
         :for-object  {:macro #'tf-for-object :emit :macro}
         :for-array   {:macro #'tf-for-array  :emit :macro}
         :for-iter    {:macro #'tf-for-iter   :emit :macro}
         :for-index   {:macro #'tf-for-index  :emit :macro}})
      (grammar/build:override fn/+r+)
      (grammar/build:extend
        {;;:na     {:op :na    :symbol '#{NA}    :raw "NA"    :value true :emit :throw}
         :next   {:op :next  :symbol '#{:next} :raw "next"  :emit :return}
         :throw  {:op :next  :symbol '#{throw} :raw 'stop :emit :alias}
         :repeat {:op :repeat
                  :symbol '#{repeat}
                  :type :block
                  :block {:raw "repeat"
                          :main #{:body}
                          :control [[:until {:required true
                                             :input #{:parameter}}]]}}})))

(def +template+
  (->> {:banned #{:set :keyword}
        :allow  {:assign #{:symbol :vector :map :set}}
        :highlight '#{block}
        :default {:comment   {:prefix "#"}
                  :common    {:apply "$" :assign "<-"}
                  :invoke    {:space "" :assign "="}
                  :function  {:raw "function"}
                  :index     {:offset 1  :end-inclusive true
                              :start "[[" :end "]]"}}
         :token  {:nil       {:as "NULL"}
                  :boolean   {:as #'r-token-boolean}
                  :string    {:quote :single}
                  :symbol    {}}
         :data   {:vector    {:start "list(" :end ")" :space ""}
                  :map       {:start "list(" :end ")" :space ""
                              :custom #'r-map}
                  :map-entry {:start ""  :end ""  :space "" :assign "=" :keyword :symbol}}
         :rewrite {:staging [#'rewrite/r-rewrite-stage]}
         :define {:def       {:raw ""}
                  :defn      {:raw ""}
                  :shorthand true}}
        (collection/merge-nested (emit/default-grammar))))

(def +grammar+
  (grammar/grammar :R
    (grammar/to-reserved +features+)
    +template+))

(def +meta+
  (book/book-meta
   {:module-current   (fn [])
    :module-import    (fn [name {:keys [as refer]} opts]
                        (list 'library name))
    :module-export    (fn [name {:keys [as refer]} opts])}))

(def +book+
  (book/book {:lang :r
              :parent :xtalk
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
