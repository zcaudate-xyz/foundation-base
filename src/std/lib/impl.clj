(ns std.lib.impl
  (:require [std.lib.foundation :as h]
            [std.lib.collection :as c]
            [std.lib.walk :as walk]
            [clojure.set :as set]))

(def ^:dynamic *self* false)

(def ^:dynamic *override* nil)

(defn split-body
  "splits a body depending on keyword pairs
 
   (split-body [:a 1 :b 2 '[hello] '[there]])
   => [{:a 1, :b 2} '([hello] [there])]"
  {:added "3.0"}
  ([body]
   (let [params (->> (partition 2 body)
                     (take-while (comp keyword? first))
                     (map vec))
         len (* 2 (count params))
         body (drop len body)]
     [(into {} params) body])))

(defn split-single
  "splits out a given entry"
  {:added "3.0"}
  ([forms]
   (split-single forms :tag))
  ([forms tag-key]
   (let [ptl (first forms)
         [params body] (split-body (rest forms))]
     [(assoc params tag-key ptl) body])))

(defn split-all
  "splits all entries
 
   (split-all '[ITest
                :include [-test]
                :body {-val 3}
                IMore
                IProtocols
                :exclude []]
              :protocol
              {:prefix \"test/\"})
  => '[{:prefix \"test/\", :include [-test], :body {-val 3}, :protocol ITest}
        {:prefix \"test/\", :protocol IMore}
        {:prefix \"test/\", :exclude [], :protocol IProtocols}]"
  {:added "3.0"}
  ([forms]
   (split-all forms :tag))
  ([forms tag-key]
   (split-all forms tag-key {}))
  ([forms tag-key params]
   (cond (empty? forms)
         []

         :else
         (loop [forms forms
                acc []]
           (let [[ptl more] (split-single forms tag-key)
                 ptl (merge params ptl)
                 acc (conj acc ptl)]
             (if (empty? more)
               acc
               (recur more acc)))))))

(defn impl:unwrap-sym
  "unwraps the protocol symbol
 
   (impl:unwrap-sym {:name '-val :prefix \"test/\" :suffix \"-mock\"})
   => 'test/val-mock"
  {:added "3.0"}
  ([{:keys [name prefix suffix]
     :or {prefix "" suffix ""}}]
   (symbol (str prefix (subs (h/strn name) 1) suffix))))

(defn impl:wrap-sym
  "wraps the protocol symbol
 
   (impl:wrap-sym {:protocol 'protocol.test/ITest :name '-val})
   => 'protocol.test/-val"
  {:added "3.0"}
  ([{:keys [protocol name]}]
   (symbol (namespace protocol) (h/strn name))))

(defn standard-body-input-fn
  "creates a standard input function"
  {:added "3.0"}
  ([_]
   (fn [{:keys [arglist]}] arglist)))

(defn standard-body-output-fn
  "creates a standard output function"
  {:added "3.0"}
  ([{:keys [body-sym-fn body-arg-fn]
     :or {body-sym-fn :name
          body-arg-fn identity}}]
   (fn [{:keys [arglist] :as signature}]
     (let [body `(~(body-sym-fn signature)
                  ~(body-arg-fn (first arglist))
                  ~@(rest arglist))]
       (if *self*
         `(do ~body ~(first arglist))
         body)))))

(defn create-body-fn
  "creates a body function
 
   ((create-body-fn {:body-sym-fn impl:unwrap-sym})
    {:name '-val :arglist '[cache val] :prefix \"test/\"})
   => '([cache val] (test/val cache val))"
  {:added "3.0"}
  ([{:keys [body-fn] :as fns}]
   (cond body-fn
         body-fn

         :else
         (let [{:keys [body-input-fn body-output-fn]
                :or {body-input-fn  (standard-body-input-fn fns)
                     body-output-fn (standard-body-output-fn fns)}} fns]
           (fn [{:keys [self] :as signature}]
             (let [input  (body-input-fn signature)
                   output (body-output-fn signature)]
               (if self
                 `(~input ~output ~(first input))
                 `(~input ~output))))))))

(defn template-signatures
  "finds template signatures for a protocol"
  {:added "3.0"}
  ([protocol]
   (template-signatures protocol {}))
  ([protocol params]
   (let [sigs (if-let [var (-> protocol resolve)]
                (-> var deref :sigs)
                (throw (ex-info "Cannot find protocol" {:input protocol})))]
     (mapcat (fn [{:keys [arglists] :as m}]
               (let [m (-> (merge params m)
                           (dissoc :arglists))]
                 (map (fn [arglist]
                        (assoc m :arglist arglist))
                      arglists)))
             (vals sigs)))))

(defn template-transform
  "transforms all functions"
  {:added "3.0"}
  ([signatures {:keys [template-fn] :as fns
                :or {template-fn identity}}]
   (let [body-fn  (create-body-fn fns)
         sym-fn   (fn [{:keys [name] :as m}] (with-meta name m))
         all (map (juxt sym-fn body-fn)
                  signatures)]
     (template-fn all))))

(defn parse-impl
  "parses the different transform types
 
   (parse-impl (template-signatures 'IHello {:prefix \"impl/\"})
               {:body '{-hi \"Hi There\"}})
   => '{:method #{},
        :body #{-hi},
        :default #{-delete}}"
  {:added "3.0"}
  ([signatures {:keys [method body include exclude] :as params}]
   (let [include-syms (if include (set include))
         exclude-syms (set exclude)
         method-syms  (set (keys method))
         body-syms    (set (keys body))
         default-syms (set/difference (or include-syms
                                          (set (map :name signatures)))
                                      (set/union method-syms
                                                 body-syms
                                                 exclude-syms))]
     {:method method-syms
      :body body-syms
      :default default-syms})))

(defn template-gen
  "generates forms given various formats
 
   (template-gen protocol-fns [:default]
                 (template-signatures 'ITest) {} {})
   => '([-val ([obj] (val obj))]
        [-val ([obj k] (val obj k))]
        [-get ([obj] (get obj))])"
  {:added "3.0"}
  ([type-fn types signatures params fns]
   (let [syms (parse-impl signatures params)]
     (mapcat (fn [type]
               (let [fns (merge (type-fn type (get params type))
                                fns
                                (get *override* type))]
                 (h/->> signatures
                        (filter (fn [{:keys [name]}]
                                  ((get syms type) name)))
                        (template-transform % (or (get fns type)
                                                  fns)))))
             types))))

(defn protocol-fns
  "helpers for protocol forms
 
   (protocol-fns :body {})
   => (contains {:body-output-fn fn?})"
  {:added "3.0"}
  ([type template]
   (case type
     :default {:body-sym-fn impl:unwrap-sym}
     :method  {:body-sym-fn (fn [{:keys [name]}] (get template name))}
     :body    {:body-output-fn (fn [{:keys [name]}] (get template name))})))

(defn dimpl-template-fn
  "helper function for defimpl"
  {:added "3.0"}
  ([inputs]
   (map (fn [[name body]]
          (apply list name body))
        inputs)))

(defn dimpl-template-protocol
  "coverts the entry into a template
 
   (dimpl-template-protocol {:protocol 'ITest :prefix \"impl/\" :suffix \"-test\"})
   => '(ITest (-val [obj] (impl/val-test obj))
              (-val [obj k] (impl/val-test obj k))
              (-get [obj] (impl/get-test obj)))"
  {:added "3.0"}
  ([{:keys [protocol prefix suffix fns custom] :as params}]
   (let [signatures (template-signatures protocol {:prefix prefix :suffix suffix})
         types [:default :method :body]]
     (h/-> (template-gen protocol-fns types signatures params
                         (merge {:template-fn dimpl-template-fn}
                                (c/map-vals eval fns)))
           (concat custom)
           (cons protocol %)))))

(defn interface-fns
  "helper for interface forms
 
   (interface-fns :body {})
   => (contains {:body-output-fn fn?})"
  {:added "3.0"}
  ([type template]
   (case type
     :method  {:body-sym-fn (fn [{:keys [name arglist]}] (get-in template [name arglist]))}
     :body    {:body-output-fn (fn [{:keys [name arglist]}]
                                 (get-in template [name arglist]))})))

(defn dimpl-template-interface
  "creates forms for the interface
 
   (dimpl-template-interface {:interface 'ITest
                              :method '{invoke {[entry] submit-invoke}}
                              :body   '{bulk? {[entry] false}}})
   => '(ITest (invoke [entry] (submit-invoke entry))
              (bulk? [entry] false))"
  {:added "3.0"}
  ([{:keys [interface method body] :as params}]
   (let [all  (merge-with merge method body)
         signatures (mapcat (fn [[name methods]]
                              (map (fn [[args _]]
                                     (merge {:arglist args
                                             :name name}
                                            (dissoc params :method body)))
                                   methods))
                            all)
         body-sym    (fn [{:keys [name arglist] :as params}]
                       (get-in method [name arglist]))
         body-output (fn [{:keys [name arglist] :as params}]
                       (get-in body [name arglist]))
         types [:method :body]]
     (->> (template-gen interface-fns types signatures params {:template-fn dimpl-template-fn})
          (cons interface)))))

(defn dimpl-print-method
  "creates a print method form"
  {:added "3.0"}
  ([sym]
   `(defmethod print-method (Class/forName ~(str (munge *ns*) "." sym))
      ([~'v ~(with-meta 'w {:tag 'java.io.Writer})]
       (.write ~'w (str ~'v))))))

(defonce +dimpl-fn-args+ 21)

(defn dimpl-fn-invoke
  "creates an invoke method"
  {:added "3.0"}
  ([method n]
   (let [args (map #(symbol (str "a" %)) (range n))]
     `(~'invoke [~'obj ~@args] (~method ~'obj ~@args)))))

(defn dimpl-fn-forms
  "creates the `IFn` forms"
  {:added "3.0"}
  ([invoke]
   (let [[invoke num] (c/seqify invoke)
         num (or num +dimpl-fn-args+)]
     `[clojure.lang.IFn
       ~@(map (fn [n] (dimpl-fn-invoke invoke n)) (range num))
       (~'applyTo ~'[obj args]
                  (~'apply ~invoke ~'obj ~'args))])))

(defn dimpl-form
  "helper for `defimpl`"
  {:added "3.0"}
  ([sym bindings body]
   (let [[params body] (split-body body)
         {:keys [type prefix suffix string protocols interfaces invoke fns final]
          :or {type 'defrecord prefix "" suffix "" interfaces ()}} params
         protocols   (split-all protocols :protocol {:prefix prefix
                                                     :suffix suffix
                                                     :fns fns})
         protocol-forms (mapcat dimpl-template-protocol protocols)
         interfaces  (if string
                       (concat ['Object :method {'toString {'[obj] string}}] interfaces)
                       interfaces)
         interfaces  (split-all interfaces :interface {:prefix prefix
                                                       :suffix suffix
                                                       :fns fns})
         interfaces-forms (mapcat dimpl-template-interface interfaces)
         invoke-forms  (if invoke (dimpl-fn-forms invoke))]
     (if-not (and final
                  (resolve sym))
       `[(~type ~sym ~bindings ~@(concat protocol-forms
                                         (keep identity interfaces-forms)
                                         invoke-forms
                                         body))
         ~@(if string
             [(dimpl-print-method sym)]
             [])]))))

(defmacro defimpl
  "creates a high level `deftype` or `defrecord` interface"
  {:added "3.0"}
  ([sym bindings & body]
   (dimpl-form sym bindings body)))

;;;;
;;;;
;;;;

(defn eimpl-template-fn
  "creates forms compatible with `extend-type` and `extend-protocol`
 
   (eimpl-template-fn '([-val ([obj] (val obj))]
                        [-val ([obj k] (val obj k))]
                        [-get ([obj] (get obj))]))
   => '((-val ([obj] (val obj))
              ([obj k] (val obj k)))
        (-get ([obj] (get obj))))"
  {:added "3.0"}
  ([inputs]
   (->> (group-by first inputs)
        (map (fn [[name arr]]
               `(~name ~@(map second arr)))))))

(defn eimpl-template-protocol
  "helper for eimpl-form
 
   (eimpl-template-protocol {:protocol 'ITest :prefix \"impl/\" :suffix \"-test\"})
   => '(ITest
        (-val ([obj] (impl/val-test obj))
              ([obj k] (impl/val-test obj k)))
        (-get ([obj] (impl/get-test obj))))"
  {:added "3.0"}
  ([{:keys [protocol prefix suffix fns custom] :as params}]
   (let [signatures (template-signatures protocol {:prefix prefix :suffix suffix})
         types [:default :method :body]]
     (h/-> (template-gen protocol-fns types signatures params
                         (merge {:template-fn eimpl-template-fn}
                                (c/map-vals eval fns)))
           (concat custom)
           (cons protocol %)))))

(defn eimpl-print-method
  "creates a print method form"
  {:added "3.0"}
  ([type string]
   `(defmethod print-method ~type
      ([~'v ~(with-meta 'w {:tag 'java.io.Writer})]
       (.write ~'w ~(with-meta `(~string ~'v) {:tag 'String}))))))

(defn eimpl-form
  "creates the extend-impl form"
  {:added "3.0"}
  ([class body]
   (let [[params _] (split-body body)
         {:keys [prefix suffix string protocols fns]
          :or {prefix "" suffix ""}} params
         protocols   (split-all protocols :protocol {:prefix prefix
                                                     :suffix suffix
                                                     :class class
                                                     :fns fns})
         protocol-forms (mapcat eimpl-template-protocol protocols)]
     `[(extend-type ~class ~@protocol-forms)
       ~@(if string
           [(eimpl-print-method class string)])])))

(defmacro extend-impl
  "extends a class with the protocols"
  {:added "3.0" :style/indent 1}
  ([type & body]
   (cond (vector? type)
         (mapv #(eimpl-form % body) type)

         :else
         (eimpl-form type body))))

;;;;
;;;;
;;;;

(defn build-with-opts-fn
  "builds a function with an optional component"
  {:added "3.0"}
  ([fsym arr]
   (let [arr (sort-by (comp count first) arr)
         [arglist & body] (last arr)
         optsym (last arglist)
         full  `(~arglist ~@body)
         sbody (walk/postwalk-replace {optsym {}}
                                      body)]
     `(defn ~fsym
        ~@(butlast arr)
        (~(vec (butlast arglist)) ~@sbody)
        ~full))))

(defn build-variadic-fn
  "builds a variadic function if indicated"
  {:added "3.0"}
  ([fsym arr]
   (let [arr (sort-by (comp count first) arr)
         [arglist & body] (last arr)
         arglist (vec (concat (butlast arglist)
                              ['& (last arglist)]))]
     `(defn ~fsym
        ~@(rest arr)
        (~arglist ~@body)))))

(defn build-template-fn
  "contructs a template from returned vals with support for variadic
 
   ((build-template-fn {}) '([-val ([obj] (val obj))]
                             [-val ([obj k] (val obj k))]
                             [-get ([obj] (get obj))]))
   => '((clojure.core/defn val
          ([obj] (val obj))
          ([obj k] (val obj k)))
        (clojure.core/defn get ([obj] (get obj))))
 
   ((build-template-fn {:variadic '#{-mul}}) '([-mul ([obj ks] (mul obj ks))]))
   => '((clojure.core/defn
          mul
          ([obj & ks] (mul obj ks))))"
  {:added "3.0"}
  ([{:keys [variadic with-opts] :as opts}]
   (fn [inputs]
     (->> (group-by first inputs)
          (map (fn [[name arr]]
                 (let [fsym (impl:unwrap-sym (assoc opts :name name))]
                   (cond (and variadic
                              (or (= variadic :all)
                                  (variadic name)))
                         (build-variadic-fn fsym (map second arr))

                         (and with-opts
                              (or (= with-opts :all)
                                  (with-opts name)))
                         (build-with-opts-fn fsym (map second arr))

                         :else
                         `(defn ~fsym ~@(map second arr))))))))))

(defn build-template-protocol
  "helper for build
 
   (build-template-protocol '{:protocol ITest
                              :outer {:suffix \"-outer\"}
                              :inner {:prefix \"inner-\"}
                              :fns {:default {:body-sym-fn impl:unwrap-sym}}})
 
   => '((clojure.core/defn val-outer
          ([obj] (inner-val obj))
          ([obj k] (inner-val obj k)))
        (clojure.core/defn get-outer ([obj] (inner-get obj))))"
  {:added "3.0"}
  ([{:keys [protocol inner outer fns variadic with-opts] :as params}]
   (let [variadic  (if (sequential? variadic)  (set variadic) variadic)
         with-opts (if (sequential? with-opts) (set with-opts) with-opts)
         inner (merge inner {:protocol protocol :variadic variadic :with-opts with-opts})
         outer (merge outer {:protocol protocol :variadic variadic :with-opts with-opts})
         signatures (template-signatures protocol inner)
         types [:default :method :body]
         fns (c/map-vals (fn [val] (cond (symbol? val)
                                         @(resolve val)

                                         :else
                                         (eval val)))
                         fns)
         template-fn (build-template-fn outer)
         forms (template-gen protocol-fns types signatures params
                             (c/merge-nested {:template-fn template-fn
                                              :default {:body-sym-fn impl:wrap-sym
                                                        :template-fn template-fn}}
                                             fns))]
     forms)))

(defn build-form
  "allows multiple forms to be built"
  {:added "3.0"}
  ([body]
   (let [[global body] (if (map? (first body))
                         [(first body) (rest body)]
                         [{} body])
         protocols  (split-all body :protocol global)
         forms      (mapcat build-template-protocol protocols)]
     forms)))

(defmacro build-impl
  "build macro for generating functions from protocols"
  {:added "3.0" :style/indent 1}
  ([& body]
   (vec (build-form body))))

(defn impl:proxy
  "creates a proxy template given a symbol
 
   ((impl:proxy :<state>)
    '[(-add-channel ([mq] (add-channel-atom mq) mq))])
   => '((-add-channel [mq] (add-channel-atom :<state>) mq))"
  {:added "3.0"}
  ([sym]
   (fn [inputs]
     (map (fn [[name body]]
            (let [[arglist form & more] body
                  [f self & rest] form
                  form (apply list f sym rest)]
              (apply list name
                     arglist form more)))
          inputs))))

(defn impl:doto
  "operates on a proxy and returns object
 
   ((impl:doto :<state>)
    '[(-add-channel ([mq] (add-channel-atom mq)))])
   => '((-add-channel [mq] (do (add-channel-atom :<state>) mq)))"
  {:added "3.0"}
  ([sym]
   (fn [inputs]
     (map (fn [[name body]]
            (let [[arglist form & more] body
                  [f self & rest] form
                  form (list 'do (apply list f sym rest) self)]
              (apply list name
                     arglist form more)))
          inputs))))
