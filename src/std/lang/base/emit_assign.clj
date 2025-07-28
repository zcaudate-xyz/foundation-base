(ns std.lang.base.emit-assign
  (:require [std.string :as str]
            [std.lib :as h]
            [std.lang.base.util :as ut]
            [std.lang.base.emit-common :as common]
            [std.lang.base.emit-preprocess :as preprocess]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.emit-data :as data]
            [std.lang.base.emit-block :as block]
            [std.lang.base.emit-fn :as fn]))

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
                     (h/error "Cannot find entry" {:lang  lang
                                                   :input link}))
        _        (or (empty? (:deps entry))
                     (h/error "Inline cannot have additional dependencies." {:lang  lang
                                                                             :input link}))
        [_ _ args & body] (:form entry)
        return-ref (volatile! nil)
        body       (h/postwalk (fn [form]
                                 (if (h/form? form)
                                   (cond (= 'return (first form))
                                         (if @return-ref
                                           (h/error "Inline cannot have multiple returns." {:input @return-ref})
                                           (do (vreset! return-ref (second form))
                                               '<RETURN>))
                                         
                                         :else
                                         (remove (fn [x]
                                                   (= x '<RETURN>))
                                                 form))
                                   form))
                               body)
        _          (or (not (coll? @return-ref))
                       (h/error "Return should be a token." {:input @return-ref}))
        assign-ref (volatile! nil)
        -          (h/postwalk (fn [form]
                                 (do (when (and (h/form? form)
                                                (= 'var (first form)))
                                       (let [asym (first (filter symbol? (rest form)))]
                                         (or (= @return-ref asym)
                                             (h/error "Inlined with unaccounted for declarations"
                                                      {:link link
                                                       :form body}))
                                         (vreset! assign-ref asym)))
                                     form))
                               body)
        asym?      @assign-ref
        rsym?      (symbol? @return-ref) 
        smap       (cond-> (zipmap args input)
                     (and rsym? asym?) (assoc @return-ref sym))
        body       (h/prewalk-replace smap body)
        #_#_
        _          (h/prn smap @assign-ref @return-ref
                          body)]
    (if (and rsym? asym?)
      (apply list 'do* body)
      (apply list 'do* (concat body [(list 'var sym := (if rsym?
                                                         (get smap @return-ref)
                                                         @return-ref))])))))

(defn emit-def-assign
  "emits a declare expression"
  {:added "3.0"}
  ([_ {:keys [raw] :as props} [_ & args] grammar mopts]
   (let [{:keys [sep space assign]} (helper/get-options grammar [:default :define])
         args     (helper/emit-typed-args args grammar)
         
         argstrs  (map (fn [{:keys [value symbol] :as arg}]
                         (let [{:assign/keys [inline template] :as aopts} (meta value)
                               custom (cond (:assign/fn aopts) [:raw  ((:assign/fn aopts) symbol)]
                                            template [:template (h/prewalk-replace {template symbol} value)]
                                            inline   [:inline   (emit-def-assign-inline symbol
                                                                                        value
                                                                                        grammar
                                                                                        mopts)])]
                           (if custom
                             (common/*emit-fn* (second custom) grammar mopts)
                             (fn/emit-input-default arg assign grammar mopts))))
                       args)
         vstr    (str/join (str sep space) argstrs)
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
