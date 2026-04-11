(ns xtgen.gen-common-spec
  (:require [std.string.prose :as prose]
            [std.string.common :as str]
            [std.block.template :as gen]
            [std.lang.base.grammar-xtalk :as xtalk]))

(defn xtalk-entries
  []
  (->> (the-ns 'std.lang.base.grammar-xtalk)
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
    xtalk/+xt-functional-future+
    xtalk/+xt-functional-invoke+
    xtalk/+xt-functional-iter+
    xtalk/+xt-functional-return+
    xtalk/+xt-lang-bit+
    xtalk/+xt-lang-global+
    xtalk/+xt-lang-proto+
    xtalk/+xt-lang-random+
    xtalk/+xt-lang-throw+
    xtalk/+xt-lang-time+
    xtalk/+xt-lang-unpack+
    xtalk/+xt-network-client-basic+
    xtalk/+xt-network-client-ws+
    xtalk/+xt-network-socket+
    xtalk/+xt-network-ws+
    xtalk/+xt-notify-http+
    xtalk/+xt-notify-socket+
    xtalk/+xt-runtime-b64+
    xtalk/+xt-runtime-cache+
    xtalk/+xt-runtime-file+
    xtalk/+xt-runtime-js+
    xtalk/+xt-runtime-shell+
    xtalk/+xt-runtime-thread+
    xtalk/+xt-runtime-uri+]))


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
  (:require [std.lang :as l :refer [defspec.xt]]))

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
  
  {'target   (first (:symbol e))
   'type     (:type op-spec)})

(def ^:private +generate-common-type+
  (gen/get-template GENERATE_COMMON_TYPE_TEMPLATE
                    generate-common-type-template))

(defn generate-common-type
  [e]
  (gen/fill-template +generate-common-type+ e))

;;
;; TMPL.MACROS
;;

(def ^:private GENERATE_COMMON_MACRO_TEMPLATE
  "
(defmacro.xt ^{:standalone ~standalone :is-template ~is-template} 
  ~target
  ~@forms)")

(defn generate-common-macro-template
  [{:keys [op-spec] :as e}]
  (let [target    (first (:symbol e))
        {:keys [arglists
                variadic
                template-only]} op-spec
        forms     (if variadic
                    (list (conj (first arglists)
                                '& 'more)
                          (apply list 'apply 'list (list 'quote target) (conj (first arglists) 'more)))
                    (->> arglists
                         (map (fn [arglist]
                                (list arglist
                                      (apply list 'list (list 'quote target) arglist))))
                         (interpose [(std.block/newline)])
                         (mapcat (fn [x]
                                   (if (vector? x) x [x])))))]
    {'target   target
     'forms     forms
     'standalone true
     'is-template (boolean template-only)}))

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
  (let [target   (first (:symbol e))
        arglist  (first (:arglists op-spec))
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

(defn create-lang-common-spec
  "Generate complete RPC file content for given namespaces"
  [namespace]
  (spit "src/xt/lang/common_spec.clj"
        (str/join "\n\n"
                  (concat [(generate-common-ns-template 'xt.lang.common-spec)
                           GENERATE_COMMON_FOR_TEMPLATE]
                          (interleave (map generate-common-type +xtalk-entries+)
                                      (map generate-common-macro +xtalk-entries+))))))




(comment
  (f/template-entries [xtalk/tmpl-fragment-fn]
                      xtalk/+xt-common-basic+
                      xtalk/+xt-common-index+
                      xtalk/+xt-common-nil+
                      xtalk/+xt-common-number+
                      xtalk/+xt-common-primitives+
                      xtalk/+xt-common-print+))
