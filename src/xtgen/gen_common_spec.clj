33(ns xtgen.gen-common-spec
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
    xtalk/+xt-network-server-basic+
    xtalk/+xt-network-server-ws+
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
  #_(when (nil? (:type op-spec))
      (std.lib/prn (str (first (:symbol e))
                        " - "
                        e)))
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
(defmacro.xt ^{:standalone ~standalone} 
  ~target
  ~arglist
  ~form)")

(defn generate-common-macro-template
  [{:keys [op-spec] :as e}]
  (let [target   (first (:symbol e))
        arglist (first (:arglists op-spec))
        form     (apply list 'list (list 'quote target) arglist)]
    {'arglist  arglist  
     'target   target
     'form     form
     'standalone true}))

(def ^:private +generate-common-macro+
  (gen/get-template GENERATE_COMMON_MACRO_TEMPLATE
                    generate-common-macro-template))

(defn generate-common-macro
  [e]
  (gen/fill-template +generate-common-macro+ e))


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
        arglist (first (:arglists op-spec))
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
                  (concat [(generate-common-ns-template 'xt.lang.common-spec)]
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
