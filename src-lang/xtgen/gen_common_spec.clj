(ns xtgen.gen-common-spec
  (:require [std.string.common :as str]
            [std.block.template :as gen]
            [hara.common.grammar-macro :as grammar-macro]
            [hara.common.grammar-spec :as grammar-spec]
            [hara.common.grammar-xtalk :as xtalk]))

(defn xtalk-entries
  []
  (->> (the-ns 'hara.common.grammar-xtalk)
       (ns-publics)
       (keep (fn [[k ^clojure.lang.Var v]]
               (let [s (.sym v)]
                 (if (.startsWith (name s)
                                  "+")
                   (symbol "xtalk" (str s))))))
       (sort)
       (vec)))

(def +xtalk-entries+
  ;; (xtalk-entries)
  (apply concat
         [xtalk/+xt-common-array+
          xtalk/+xt-common-basic+
          xtalk/+xt-common-index+
          xtalk/+xt-common-lu+
          xtalk/+xt-common-math+
          xtalk/+xt-common-nil+
          xtalk/+xt-common-number+
          xtalk/+xt-common-object+
          xtalk/+xt-common-primitives+
          xtalk/+xt-common-print+
          xtalk/+xt-common-string+
          xtalk/+xt-functional-array+
          xtalk/+xt-functional-base+
          xtalk/+xt-functional-invoke+
          xtalk/+xt-functional-iter+
          xtalk/+xt-functional-return+
          xtalk/+xt-lang-bit+
          xtalk/+xt-lang-global+
          xtalk/+xt-lang-random+
          xtalk/+xt-lang-throw+
          xtalk/+xt-lang-time+
          xtalk/+xt-lang-unpack+
          xtalk/+xt-lang-json+
           xtalk/+xt-socket+
           xtalk/+xt-notify-http+
           
           xtalk/+xt-runtime-promise+
           xtalk/+xt-runtime-file+
           xtalk/+xt-runtime-shell+]))

(def +primitive-source-namespaces+
  '[hara.common.grammar-spec
    hara.common.grammar-macro])

(defn op-table-vars
  ([ns-sym]
   (op-table-vars ns-sym "+op-"))
  ([ns-sym prefix]
   (->> (ns-publics ns-sym)
        (keep (fn [[sym v]]
                (when (.startsWith (name sym) prefix)
                  v))))))

(defn op-entries
  ([ns-sym]
   (op-entries ns-sym "+op-"))
  ([ns-sym prefix]
   (->> (op-table-vars ns-sym prefix)
        (mapcat (fn [v]
                  (let [value @v]
                    (if (sequential? value)
                      value
                      []))))
        vec)))

(defn entry-targets
  [{:keys [target symbol]}]
  (if target
    [target]
    (->> symbol
         (filter (fn [sym]
                   (and (symbol? sym)
                        (not (#{"&" "as"} (name sym)))
                        (not-any? #(.contains (name sym) %)
                                  ["&" "|"]))))
         (sort-by str)
         vec)))

(defn expand-entry-targets
  [entry]
  (mapv (fn [target]
          (assoc entry :target target))
        (entry-targets entry)))

(defn entry-type-form
  [{:keys [op-spec]}]
  (or (:type op-spec)
      (when (= 1 (count (:types op-spec)))
        (first (:types op-spec)))))

(defn entry-arglists
  [{:keys [arglists op-spec]}]
  (or (:arglists op-spec)
      arglists))

(defn literal-arglist?
  [arglist]
  (->> (tree-seq coll? seq arglist)
       (remove coll?)
       (not-any? #{'& :as})))

(def +late-macro-targets+
  '#{fn
     def
     defn
     defn-
     let
     letfn
     for
     while
     if
     cond
     when
     case
     do
     do*
     quote
     comment
     and
     or
     not
     ->
     ->>
     doto
     try})

(defn entry-render-order
  [{:keys [target]}]
  [(cond
     (= 'fn target) 2
     (contains? +late-macro-targets+ target) 1
     :else 0)
   (str target)])

(defn primitive-entries
  []
  (->> +primitive-source-namespaces+
       (mapcat op-entries)
       (mapcat expand-entry-targets)
       (sort-by entry-render-order)
       vec))

(def +primitive-entries+
  (primitive-entries))


(def ^:private GENERATE_COMMON_FOR_TEMPLATE
  "
(defmacro.xt ^{:style/indent 1}
  for:array
  ([[e arr] & body]
   (clojure.core/apply list 'for:array [e arr] body)))

(defmacro.xt ^{:style/indent 1}
  for:object
  ([[[k v] obj] & body]
   (clojure.core/apply list 'for:object [[k v] obj] body)))

(defmacro.xt ^{:style/indent 1}
  for:index
  ([[i [start stop step]] & body]
   (clojure.core/apply list 'for:index [i [start stop step]] body)))

(defmacro.xt ^{:style/indent 1}
  for:iter
  ([[e it] & body]
   (apply list 'for:iter [e it] body)))

(defmacro.xt ^{:style/indent 1}
  return-run
  ([[resolve reject] & body]
   (list 'x:return-run
         (clojure.core/apply list 'fn [resolve reject] body))))

(defmacro.xt ^{:style/indent 1}
  for:return
  ([[[ok err] statement] {:keys [success error final]}]
   (list 'for:return [[ok err] statement]
         {:success success
          :error error
          :final final})))

(defmacro.xt ^{:style/indent 1}
  for:try
  ([[[ok err] statement] {:keys [success error]}]
   (list 'for:try [[ok err] statement]
         {:success success
          :error error})))

(defmacro.xt ^{:style/indent 1}
  for:async
  ([[[ok err] statement] {:keys [success error finally]}]
   (list 'for:async [[ok err] statement]
         {:success success
          :error error
          :finally finally})))")

;;
;; TMPL.NAMESPACE
;;

(def ^:private GENERATE_COMMON_NS_TEMPLATE
  "
(ns ~namespace
  (:require [hara.lang :as l :refer [defspec.xt]]))

(l/script :xtalk)
")

(defn generate-common-ns-template
  [namespace]
  (str/trim
   (str/replace GENERATE_COMMON_NS_TEMPLATE
                "~namespace"
                (str namespace))))

;;
;; TMPL.TYPES
;;

(def ^:private GENERATE_COMMON_TYPE_TEMPLATE
  "(defspec.xt ~target ~type)")

(defn generate-common-type-template
  [{:keys [op-spec] :as e}]
  (when-let [type-form (entry-type-form e)]
    {'target   (or (:target e)
                   (first (entry-targets e)))
     'type     type-form}))

(def ^:private +generate-common-type+
  (gen/get-template GENERATE_COMMON_TYPE_TEMPLATE
                    generate-common-type-template))

(defn generate-common-type
  [e]
  (when-let [template-data (generate-common-type-template e)]
    (gen/fill-template +generate-common-type+ template-data)))

;;
;; TMPL.MACROS
;;

(def ^:private GENERATE_COMMON_MACRO_TEMPLATE
  "
(defmacro.xt ^{:standalone ~standalone} 
  ~target
  ~@forms)")

(defn generate-common-macro-template
  [{:keys [op-spec] :as e}]
  (let [target    (or (:target e)
                      (first (entry-targets e)))
        arglists  (entry-arglists e)
        variadic  (:variadic op-spec)
        direct?   (and (seq arglists)
                       (every? literal-arglist? arglists))
        forms     (cond
                    (and variadic (seq arglists))
                    (list (conj (first arglists)
                                '& 'more)
                          (apply list 'apply 'list (list 'quote target)
                                 (conj (first arglists) 'more)))

                    direct?
                    (->> arglists
                         (map (fn [arglist]
                                (list arglist
                                      (apply list 'list (list 'quote target) arglist))))
                         (interpose [(std.block/newline)])
                         (mapcat (fn [x]
                                   (if (vector? x) x [x]))))

                    :else
                    (list '[x & more]
                          (list 'apply 'list (list 'quote target) 'x 'more)))]
    {'target     target
     'forms      forms
     'standalone true}))

(def ^:private +generate-common-macro+
  (gen/get-template GENERATE_COMMON_MACRO_TEMPLATE
                    generate-common-macro-template))

(defn generate-common-macro
  [e]
  (gen/fill-template +generate-common-macro+ e))

(comment

  (generate-common-macro
   {:op :x-offset         :symbol #{'x:offset}                :emit :macro
    :op-spec {:arglists '([] [n])
              :type [:fn [:xt/int] :xt/int]}})
  
  )

;;
;; TMPL.FUNCTIONS
;;

(def ^:private GENERATE_COMMON_FUNCTION_TEMPLATE
  "
(deffunction.xt ^{:standalone ~standalone} 
  ~target
  ~arglist
  ~form)")

(defn generate-common-function-template
  [{:keys [op-spec] :as e}]
  (let [target   (or (:target e)
                     (first (entry-targets e)))
        arglist  (first (entry-arglists e))
        form     (apply list 'list (list 'quote target) arglist)]
    {'arglist  arglist  
     'target   target
     'form     form
     'standalone true}))

(def ^:private +generate-common-function+
  (gen/get-template GENERATE_COMMON_FUNCTION_TEMPLATE
                    generate-common-function-template))

(defn generate-common-function
  [e]
  (gen/fill-template +generate-common-function+ e))


;;
;; LANG.SPEC
;;

(defn namespace-source-path
  [namespace]
  (str "src/"
       (-> (str namespace)
           (str/replace "." "/")
           (str/replace "-" "_"))
       ".clj"))

(defn render-lang-spec
  [namespace entries & [prelude]]
  (str/join "\n\n"
            (concat [(generate-common-ns-template namespace)]
                    prelude
                    (mapcat (fn [entry]
                              (remove nil?
                                      [(generate-common-type entry)
                                       (generate-common-macro entry)]))
                            entries))))

(defn render-lang-common-spec
  ([] (render-lang-common-spec 'xt.lang.spec-base))
  ([namespace]
   (render-lang-spec namespace
                     +xtalk-entries+
                     [GENERATE_COMMON_FOR_TEMPLATE])))

(defn render-lang-primitive-spec
  ([] (render-lang-primitive-spec 'xt.lang.spec-primitive))
  ([namespace]
   (render-lang-spec namespace
                     +primitive-entries+)))

(defn create-lang-common-spec
  "Generate complete xtalk common spec file content."
  ([] (create-lang-common-spec 'xt.lang.spec-base))
  ([namespace]
   (spit (namespace-source-path namespace)
         (render-lang-common-spec namespace))))

(defn create-lang-primitive-spec
  "Generate the primitive xtalk spec layer from grammar spec and macro ops."
  ([] (create-lang-primitive-spec 'xt.lang.spec-primitive))
  ([namespace]
   (spit (namespace-source-path namespace)
         (render-lang-primitive-spec namespace))))




(comment
  (f/template-entries [xtalk/tmpl-fragment-fn]
                      xtalk/+xt-common-basic+
                      xtalk/+xt-common-index+
                      xtalk/+xt-common-nil+
                      xtalk/+xt-common-number+
                      xtalk/+xt-common-primitives+
                      xtalk/+xt-common-print+))
