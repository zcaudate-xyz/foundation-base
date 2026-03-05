(ns bb.lang.model.spec-r
  (:require [bb.lang.base.emit :as emit]
            [bb.lang.base.emit-common :as common]
            [bb.lang.base.emit-top-level :as top]
            [bb.lang.base.grammar :as grammar]
            [bb.lang.base.grammar-spec :as spec]
            [bb.lang.base.util :as ut]
	    [bb.lang.base.book :as book]
            [bb.lang.base.script :as script]
            [bb.lang.model.spec-xtalk]
            [bb.lang.model.spec-xtalk.fn-r :as fn]
            [bb.string :as str]
            [bb.lib :as h]))

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
   (list 'def sym (apply list 'fn args body))))

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
      (h/$ (do (var ~i := 0)
               (for [~e :in (% ~arr)]
                 (:= ~i (+ ~i 1))
                 ~@body))))
    (apply list 'for [e :in (list '% arr)]
                body)))

(defn tf-for-iter
  "transform for `for:iter`"
  {:added "4.0"}
  [[_ [e it] & body]]
  (h/$ (for [~e :in (% ~it)]
         ~@body)))

(defn tf-for-index
  "transform for `for:index`"
  {:added "4.0"}
  [[_ [i [start end step]] & body]]
  (h/$ (for [~i :in (seq ~start
                         ~end
                         ~(or step 1))]
         ~@body)))

(defn tf-for-return
  "transform for `for:return`"
  {:added "4.0"}
  [[_ [[res err] statement] {:keys [success error]}]]
  (h/$ (tryCatch
        (block
         (var ~res ~statement)
         ~success)
        :error (fn [~err] ~error))))

(defn- r-token-boolean
  [bool]
  (if bool "TRUE" "FALSE"))

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
        :for-index   {:macro #'tf-for-index  :emit :macro}
        :for-return  {:macro #'tf-for-return :emit :macro}})
      (grammar/build:override fn/+r+ )
      (grammar/build:extend
       {;;:na     {:op :na    :symbol '#{NA}    :raw "NA"    :value true :emit :throw}
        :next   {:op :next  :symbol '#{:next} :raw "next"  :emit :return}
        :throw  {:op :next  :symbol '#{throw} :raw 'stop :emit :alias}
        :repeat {:op :repeat
                 :symbol '#{repeat} :type :block
                 :block {:raw "repeat"
                         :main    #{:body}
                         :control [[:until {:required true
                                            :input #{:parameter}}]]}}})))

(def +template+
  (->> {:banned #{:set :keyword}
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
                 :map       {:start "list(" :end ")" :space ""}
                 :map-entry {:start ""  :end ""  :space "" :assign "=" :keyword :symbol}}
        :define {:def       {:raw ""}
                 :defn      {:raw ""}}}
       (h/merge-nested (emit/default-grammar))))

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

(comment
  (bb.lang/script :r)

  (!.R
    (NA (+ 1 2 NA))))


(comment
  :on     {:op :on    :symbol '#{on :on}     :raw "~" :emit :bi}
  :quot   {:op :quot  :symbol '#{quot}   :raw "%/%" :emit :bi}
  :in     {:op :in    :symbol '#{in :in} :raw "%in%" :emit :bi}
  :mmul   {:op :mmul  :symbol '#{*| mmul} :raw "%*%" :emit :bi}


  (def +reserved+
    (-> (grammar/ops :exclude [[:general :exclude [:try]]
                               :type
                               :counter
                               :yield
                               :infix
                               [:return :exclude [:ret]]
                               [:bit :exclude [:bitsl :bitsr]]])
        (h/merge-nested
         {:new    {:value true}
          :setrq  {:op :setrq :symbol '#{:>} :raw "->"}
          :neg    {:emit :invoke}

          :defn   {:emit r-defn}

          :prog   {:op :prog
                   :symbol '#{prog} :type :block
                   :block {:raw ""
                           :main    #{:body}}}})
        (grammar/map-symbols)))

  )
