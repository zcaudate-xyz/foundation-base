(ns lib.supabase.template
  (:require [clojure.pprint :as pprint]))

(defn form-string
  [form]
  (with-out-str
    (pprint/pprint form)))

(defn supabase-call-form
  "Returns the filled baseline wrapper form."
  {:added "4.1.4"}
  [input]
  (let [fsym (or (get input 'fsym)
                 (:fsym input))
        doc (or (get input 'doc)
                (:doc input))
        args (or (get input 'args)
                 (:args input)
                 [])
        call-sym (or (get input 'call-sym)
                     (:call-sym input)
                     'lib.supabase/supabase-call)
        method (or (get input 'method)
                   (:method input))
        route (or (get input 'route)
                  (:route input))
        defaults (or (get input 'defaults)
                     (:defaults input)
                     {})
        body-form (if (contains? input 'body-form)
                    (get input 'body-form)
                    (get input :body-form))
        base-args (vec (concat ['client] args))
        opts-args (conj base-args 'opts)]
    `(defn ~fsym
       ~doc
       ([~@base-args] (~fsym ~@base-args {}))
       ([~@opts-args]
        (~call-sym
         (merge {:client ~'client
                 :method ~method
                 :route ~route}
                ~defaults
                ~'opts)
         ~body-form)))))

(defn supabase-call-string
  "Fills the baseline `(defn ... (supabase-call ...))` template."
  {:added "4.1.4"}
  [input]
  (form-string (supabase-call-form input)))

(defn supabase-defn-form
  "Returns a filled generic Supabase wrapper `defn` form."
  {:added "4.1.4"}
  [input]
  (let [fsym (or (get input 'fsym)
                 (:fsym input))
        doc (or (get input 'doc)
                (:doc input))
        base-args (or (get input 'base-args)
                      (:base-args input)
                      ['client])
        opts-args (or (get input 'opts-args)
                      (:opts-args input)
                      ['client 'opts])
        body-form (if (contains? input 'body-form)
                    (get input 'body-form)
                    (get input :body-form))]
    `(defn ~fsym
       ~doc
       ([~@base-args] (~fsym ~@base-args {}))
       ([~@opts-args]
        ~body-form))))

(defn supabase-defn-string
  "Fills the generic Supabase wrapper `defn` template."
  {:added "4.1.4"}
  [input]
  (form-string (supabase-defn-form input)))
