(ns xtgen.gen-common-spec
  (:require [std.string.prose :as prose]
            [std.block.template :as gen]))

(defn HEADER
  "Generate ns declaration for RPC file"
  [namespaces ns]
  (prose/join-lines
   (concat
    [(str "(ns " ns)
     "  (:require [std.lang :as l]))"
     ""
     "(l/script :xtalk)"])))

(def ^:private GENERATE_COMMON_NS_TEMPLATE
  "
(ns ~namespace
  (:require [std.lang :as l :refer [defspec.xt]]))

(l/script :xtalk)
")

;;
;; GEN AUTH
;;

(defn make-gen-auth-file
  "Generate complete RPC file content for given namespaces"
  [namespaces ns header-fn]
  (let [_ (init-namespaces namespaces)
        query-syms  (get-namespace-symbols namespaces :sb/query)
        fn-syms     (get-namespace-symbols namespaces :sb/auth)
        query-forms (mapv (fn [sym]
                            (gen/fill-template db-export/TMPL_PUBLIC_QUERY sym))
                          query-syms)
        fn-forms     (mapv (fn [sym]
                             (gen/fill-template db-export/TMPL_PUBLIC_AUTH_FN sym))
                           fn-syms)]
    (str/join "\n\n"
              (concat [(header-fn namespaces ns)]
                      query-forms
                      fn-forms))))


(def ^:private GENERATE_COMMON_LIB_TEMPLATE
  "
(defmacro.xt ^{:standalone ~standalone} 
  ~target
  ~arglist
  ~form)")

(defn generate-common-lib-input
  [{:keys [op-spec] :as e}]
  (let [target   (first (:symbol e))
        arglist (first (:arglists op-spec))
        form     (apply list (list 'quote target) arglist)]
    {'arglist  arglist  
     'target   target
     'form     form
     'standalone true}))

(def ^:private +generate-common-lib+
  (gen/get-template GENERATE_COMMON_LIB_TEMPLATE
                    generate-common-lib-input))

(defn generate-common-lib
  [e]
  (gen/fill-template +generate-common-lib+ e))



4
