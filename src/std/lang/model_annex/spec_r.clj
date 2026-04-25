(ns std.lang.model-annex.spec-r
  (:require [clojure.string :as str]
            [std.lang.base.book :as book]
            [std.lang.base.emit :as emit]
            [std.lang.base.emit-common :as common]
            [std.lang.base.emit-data :as data]
            [std.lang.base.emit-top-level :as top]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.grammar-spec :as spec]
            [std.lang.base.script :as script]
            [std.lang.base.util :as ut]
            [std.lang.model.spec-xtalk]
            [std.lang.model-annex.spec-xtalk.fn-r :as fn]
            [std.lib.collection :as collection]
            [std.lib.template :as template]))

(defn r-normalize-args
  [args]
  (let [[mandatory tail] (split-with #(not= '& %) args)]
    (if-let [optional (and (= '& (first tail))
                           (vector? (second tail))
                           (second tail))]
      (vec (concat mandatory
                   (mapcat (fn [sym]
                             [sym := nil])
                           optional)))
      args)))

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
   (list 'def sym (apply list 'fn (r-normalize-args args) body))))

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

(defn r-truthy
  [expr]
  (template/$
   (:? (not (is.null ~expr))
       (not (identical ~expr FALSE))
       false)))

(defn r-tf-or
  [[_ & args]]
  (cond (empty? args)
        nil

        (= 1 (count args))
        (first args)

        :else
        (let [expr (first args)]
          (list ':?
                (r-truthy expr)
                expr
                (r-tf-or (cons nil (rest args)))))))

(defn r-tf-and
  [[_ & args]]
  (cond (empty? args)
        true

        (= 1 (count args))
        (first args)

        :else
        (let [expr (first args)]
          (list ':?
                (r-truthy expr)
                (r-tf-and (cons nil (rest args)))
                expr))))

(defn r-tf-var
  "transform plain `var` declarations into assignment-friendly forms"
  {:added "4.1"}
  [[_ decl & args]]
  (if (empty? args)
    (list 'var* decl)
    (list 'var* decl := (last args))))

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
  (let [step (or step 1)]
    (template/$
     (for [~i :in (:? (or (and (> ~step 0)
                               (<= ~start ~end))
                          (and (< ~step 0)
                               (>= ~start ~end)))
                        (seq ~start
                             ~end
                             ~step)
                        [])]
       ~@body))))

(defn tf-for-return
  "transform for `for:return`"
  {:added "4.0"}
  [[_ [[res err] statement] {:keys [success error final]}]]
  (if (and (seq? statement)
           (= 'x:return-run (first statement)))
    (let [[_ runner] statement]
      (template/$ (block
                   (var ~res nil)
                   (tryCatch
                    (block
                     (~runner
                      (fn [value]
                        (:= ~res value))
                      (fn [value]
                        (stop value)))
                     ~(if final (list 'return success) success))
                    :error (fn [~err]
                             ~(if final (list 'return error) error))))))
    (template/$ (tryCatch
                 (block
                  (var ~res ~statement)
                  ~(if final (list 'return success) success))
                 :error (fn [~err]
                          ~(if final (list 'return error) error))))))

(defn r-vector
  [arr grammar mopts]
  (let [grammar (-> grammar
                    (assoc-in [:data :vector :start] "list(")
                    (assoc-in [:data :vector :end] ")"))
        unpack? (fn [e]
                  (and (seq? e)
                       (#{'x:unpack 'xt/x:unpack} (first e))))]
    (if (some unpack? arr)
      (let [emit (fn [e]
                   (common/*emit-fn* e grammar mopts))
            parts (map (fn [e]
                         (if (unpack? e)
                           [:unpack (emit (second e))]
                           [:elem (emit e)]))
                       arr)]
        (reduce (fn [acc [tag value]]
                  (if (= :unpack tag)
                    (str "append(" acc ", " value ")")
                    (str "append(" acc ", list(" value "))")))
                "list()"
                parts))
      (data/emit-coll :vector arr grammar mopts))))

(defn r-map
  [m grammar mopts]
  (if (empty? m)
    "structure(list(), names=character())"
    (data/emit-coll :map m grammar mopts)))

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
         :or          {:op :or :symbol '#{or} :macro #'r-tf-or :emit :macro}
         :and         {:op :and :symbol '#{and} :macro #'r-tf-and :emit :macro}
         :var         {:symbol '#{var*}}
         :mod         {:raw "%%"}
         :defn        {:op :defn  :symbol '#{defn}     :macro  #'tf-defn :emit :macro}
         :inif        {:macro #'tf-infix-if   :emit :macro}
         :for-object  {:macro #'tf-for-object :emit :macro}
         :for-array   {:macro #'tf-for-array  :emit :macro}
        :for-iter    {:macro #'tf-for-iter   :emit :macro}
        :for-index   {:macro #'tf-for-index  :emit :macro}
        :for-return  {:macro #'tf-for-return :emit :macro}})
      (grammar/build:override fn/+r+ )
        (grammar/build:extend
         {:var-r   {:op :var-r :symbol '#{var} :macro #'r-tf-var :emit :macro}
          ;;:na     {:op :na    :symbol '#{NA}    :raw "NA"    :value true :emit :throw}
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
                  :function  {:raw "function"
                              :args {:assign "="}}
                  :index     {:offset 1  :end-inclusive true
                              :start "[[" :end "]]"}}
         :token  {:nil       {:as "NULL"}
                  :boolean   {:as #'r-token-boolean}
                  :string    {:quote :single}
                  :symbol    {}}
          :data   {:vector    {:custom #'r-vector
                               :start "c(" :end ")" :space ""}
                   :map       {:custom #'r-map
                               :start "list(" :end ")" :space ""}
                   :map-entry {:start ""  :end ""  :space "" :assign "=" :keyword :symbol}}
         :define {:assign "<-"
                  :def    {:raw ""}
                 :defn   {:raw ""}}}
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

(comment
  (std.lang/script :r)

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
        (collection/merge-nested
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
