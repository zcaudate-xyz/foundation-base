(ns std.lang.base.emit-assign
  (:require [clojure.string]
            [std.lang.base.emit-block :as block]
            [std.lang.base.emit-common :as common]
            [std.lang.base.emit-data :as data]
            [std.lang.base.emit-fn :as fn]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.emit-preprocess :as preprocess]
            [std.lang.base.util :as ut]
            [std.lib.collection :as collection]
            [std.lib.env :as env]
            [std.lib.foundation :as f]
            [std.lib.walk :as walk]))

(def +assign-types+
  #{:assign/inline
    :assign/template
    :assign/fn})

;;
;; All will compile to a function :assign/fn that will then get executed on the symbol
;;
;; :assign/inline will work with functions
;; - it will check if function is inlineable (one var declaration, one return statement, return statement is symbol)
;; - it will do template substitution for the form
;; - preprocessor will exclude the first element of the list from deps
;; - it will always link to a code entry so if a macro in needed, tough.
;;
;; :assign/template works with macros, which will compile to a single form and have :assign out
;; as the replacement
;;
;; :assign/fn works as well but it will bypass the dependency checker so be careful.
;;

(defn emit-def-assign-inline
  "assigns an inline form directly"
  {:added "4.0"}
  [sym [link & input] grammar {:keys [lang book snapshot] :as mopts}]
  (let [[link-module link-id] (ut/sym-pair link)
        book     (or book (get-in snapshot [lang :book]))
        entry    (or (get-in book [:modules link-module :code link-id])
                     (f/error "Cannot find entry" {:lang  lang
                                                   :input link}))
        _        (or (empty? (:deps entry))
                     (f/error "Inline cannot have additional dependencies." {:lang  lang
                                                                             :input link}))
        [_ _ args & body] (:form entry)
        return-ref (volatile! nil)
        body       (walk/postwalk (fn [form]
                                 (if (collection/form? form)
                                   (cond (= 'return (first form))
                                         (if @return-ref
                                           (f/error "Inline cannot have multiple returns." {:input @return-ref})
                                           (do (vreset! return-ref (second form))
                                               '<RETURN>))
                                         
                                         :else
                                         (remove (fn [x]
                                                   (= x '<RETURN>))
                                                 form))
                                   form))
                               body)
        _          (or (not (coll? @return-ref))
                       (f/error "Return should be a token." {:input @return-ref}))
        assign-ref (volatile! nil)
        -          (walk/postwalk (fn [form]
                                 (do (when (and (collection/form? form)
                                                (= 'var (first form)))
                                       (let [asym (first (filter symbol? (rest form)))]
                                         (or (= @return-ref asym)
                                             (f/error "Inlined with unaccounted for declarations"
                                                      {:link link
                                                       :form body}))
                                         (vreset! assign-ref asym)))
                                     form))
                               body)
        asym?      @assign-ref
        rsym?      (symbol? @return-ref) 
        smap       (cond-> (zipmap args input)
                     (and rsym? asym?) (assoc @return-ref sym))
        body       (walk/prewalk-replace smap body)
        #_#_
        _          (env/prn smap @assign-ref @return-ref
                          body)]
    (if (and rsym? asym?)
      (apply list 'do* body)
      (apply list 'do* (concat body [(list 'var sym := (if rsym?
                                                         (get smap @return-ref)
                                                         @return-ref))])))))

(defn assign-options
  "gets assignment options either from value metadata or from a reserved macro entry"
  {:added "4.1"}
  [value grammar]
  (let [meta-opts (select-keys (meta value) +assign-types+)
        reserved-opts (when (and (collection/form? value)
                                 (symbol? (first value)))
                        (let [reserved (get-in grammar [:reserved (first value)])]
                          (select-keys reserved +assign-types+)))]
    (merge reserved-opts meta-opts)))

(defn assign-value
  "prepares an assignment override payload for a value"
  {:added "4.1"}
  [symbol value grammar mopts]
  (let [{inline :assign/inline
         template :assign/template
         assign-fn :assign/fn} (assign-options value grammar)
        reserved (when (and (collection/form? value)
                            (symbol? (first value)))
                   (get-in grammar [:reserved (first value)]))
        expanded (if (and reserved
                          (or assign-fn
                              template
                              inline
                              (:expand/assign reserved)))
                   (or (preprocess/expand-reserved-assign value grammar mopts)
                       value)
                   value)]
    (cond assign-fn [:raw      (assign-fn symbol)]
          template [:template (walk/prewalk-replace {template symbol} expanded)]
          inline   [:inline   (emit-def-assign-inline symbol
                                                     expanded
                                                     grammar
                                                     mopts)])))

(defn emit-def-assign
  "emits a declare expression"
  {:added "3.0"}
  ([_ {:keys [raw] :as props} [_ & args] grammar mopts]
   (let [{:keys [sep space assign]} (helper/get-options grammar [:default :define])
         args     (helper/emit-typed-args args grammar)
         
         argstrs  (map (fn [{:keys [value symbol] :as arg}]
                         (let [custom (assign-value symbol value grammar mopts)]
                           (if custom
                             (common/*emit-fn* (second custom) grammar mopts)
                             (fn/emit-input-default arg assign grammar mopts))))
                       args)
         vstr    (clojure.string/join (str sep space) argstrs)
         rawstr  (if (not-empty raw) (str raw space))]
     (str rawstr vstr))))

;;
;;
;;

(defn test-assign-loop
  "emit do"
  {:added "4.0" :adopt true}
  [form grammar mopts]
  (common/emit-common-loop form
                           grammar
                           mopts
                           (assoc common/+emit-lookup+
                                  :data data/emit-data
                                  :block block/emit-block)
                           (fn [key form grammar mopts]
                             (case key
                               :fn (fn/emit-fn :function form grammar mopts)
                               (common/emit-op key form grammar mopts
                                               {:def-assign emit-def-assign
                                                :quote data/emit-quote
                                                :table data/emit-table})))))

(defn test-assign-emit
  "emit assign forms
 
   (assign/test-assign-loop (list 'var 'a := (with-meta ()
                                               {:assign/fn (fn [sym]
                                                             (list sym :as [1 2 3]))}))
                            +grammar+
                            {})
   => \"(a :as [1 2 3])\"
 
   (assign/test-assign-loop (list 'var 'a := (with-meta '(sym :as [1 2 3])
                                               {:assign/template 'sym}))
                            +grammar+
                            {})
   => \"(a :as [1 2 3])\"
 
   (assign/test-assign-loop (list 'var 'a := (with-meta '(x.core/identity-fn 1)
                                               {:assign/inline 'x.core/identity-fn}))
                            +grammar+
                            {:lang :x
                             :snapshot +snap+})
   => \"(do* (var a := 1))\"
 
   (assign/test-assign-loop (list 'var 'a := (with-meta '(x.core/complex-fn 1)
                                               {:assign/inline 'x.core/complex-fn}))
                            +grammar+
                            {:lang :x
                             :snapshot +snap+})
   => \"(do* (var a := 1) (:= a (+ a 1)))\""
  {:added "4.0"}
  [form grammar mopts]
  (binding [common/*emit-fn* test-assign-loop]
    (test-assign-loop form grammar mopts)))
