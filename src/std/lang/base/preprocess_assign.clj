(ns std.lang.base.preprocess-assign
  (:require [std.lang.base.util :as ut]
            [std.lib.foundation :as f]))

(defn process-inline-assignment
  "prepares the form for inline assignment"
  {:added "4.0"}
  [form modules mopts & [unwrapped]]
  (let [[_ bind-form & rdecl] (reverse form)
        [f & args] bind-form
        [sym-ns sym-id] (ut/sym-pair f)
        {:keys [module]} mopts
        f-module (or (if (= '- sym-ns) (:id module))
                     (get (:link module) sym-ns)
                     (if (get modules sym-ns) sym-ns))
        _ (or f-module
              (f/error "Cannot resolve Module." {:input f
                                                 :current module
                                                 :modules (keys modules)}))
        _ (or (get-in modules [f-module :code sym-id])
              (f/error "Code entry not found:" {:input f
                                                :form form}))]
    (concat (reverse rdecl)
            [(with-meta (cons (cond-> (ut/sym-full f-module sym-id)
                                (not unwrapped) (volatile!))
                             args)
               {:assign/inline true})])))

(defn protect-reserved-head
  [form]
  (with-meta (cons (volatile! (first form))
                   (rest form))
    (meta form)))
