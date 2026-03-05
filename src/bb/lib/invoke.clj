(ns bb.lib.invoke
  (:require [bb.protocol-invoke :as protocol.invoke]
            [bb.lib.function :as fn]
            [clojure.core :as clojure]
            [clojure.set :as set])
  (:import (clojure.lang MultiFn))
  (:refer-clojure :exclude [fn]))

(def ^:dynamic *force* false)

(def +default-packages+
  '{:fn        {:ns clojure.core}
    :multi     {:ns clojure.core}
    :method    {:ns clojure.core}
    :dynamic   {:ns clojure.core}
    :protocol  {:ns clojure.core}
    :compose   {:ns bb.lib.invoke}
    :recent    {:ns bb.lib.invoke}
    :memoize   {:ns bb.lib.memoize}
    :element   {:ns std.object.query}
    :task      {:ns std.task}
    :opencl    {:ns hara.lib.opencl}})

(defn multi?
  "returns `true` if `obj` is a multimethod

   (multi? print-method) => true

   (multi? println) => false"
  {:added "3.0"}
  ([obj]
   (instance? MultiFn obj)))

(defn multi:clone
  "creates a multimethod from an existing one"
  {:added "3.0"}
  ([^MultiFn source name]
   (let [table (.getMethodTable source)
         clone (MultiFn. name
                         (.dispatchFn source)
                         (.defaultDispatchVal source)
                         (.hierarchy source))]
     (doseq [[dispatch-val method] table]
       (.addMethod clone dispatch-val method))
     clone)))

(defn multi:match?
  "checks if the multi dispatch matches the arguments

   (multi:match? (.dispatchFn ^MultiFn hello) (clojure.core/fn [_]))
   => true"
  {:added "3.0"}
  ([multi method]
   (multi:match? multi method false))
  ([multi method throw?]
   true))

(defn multi:get
  "returns all entries in the multimethod

   (multi:get hello :a)
   => fn?"
  {:added "3.0"}
  ([^MultiFn multi dispatch]
   (get (methods multi) dispatch)))

(defn multi:add
  "adds an entry to the multimethod

   (multi:add hello :c (clojure.core/fn [m] (assoc m :c 3)))
   => hello"
  {:added "3.0"}
  ([^MultiFn multi dispatch-val method]
   (let [dispatch-fn (.-dispatchFn multi)]
     (if (multi:match? dispatch-fn method true)
       (do (defmethod multi dispatch-val [m] (method m))
           multi)))))

(defn multi:list
  "returns all entries in the multimethod

   (keys (multi:list world))
   => (contains [:a :b]
                :in-any-order)"
  {:added "3.0"}
  ([^MultiFn multi]
   (methods multi)))

(defn multi:remove
  "removes an entry

   (multi:remove world :b)
   => ifn?"
  {:added "3.0"}
  ([^MultiFn multi val]
   (remove-method multi val)))

(defn invoke:arglists
  "returns the arglists of a form

   (invoke:arglists '([x] x))
   => '(quote ([x]))

   (invoke:arglists '(([x] x) ([x y] (+ x y))))
   => '(quote ([x] [x y]))"
  {:added "3.0"}
  ([body]
   (cond (list? (first body))
         `(quote ~(map first body))

         (vector? (first body))
         `(quote ~(list (first body)))

         :else
         (throw (ex-info "Cannot find arglists." {:body body})))))

(defn invoke-intern-method
  "creates a `:method` form, similar to `defmethod`

   (defmulti -hello-multi- identity)
   (invoke-intern-method '-hello-method-
                         {:multi '-hello-multi-
                          :val :apple}
                         '([x] x))"
  {:added "3.0"}
  ([name {:keys [multi val] :as config} body]
   (let [arglists (invoke:arglists body)
         [mm-name method-name] (cond (nil? multi)
                                     (if (resolve name)
                                       [name name]
                                       (throw (ex-info "Cannot resolve multimethod." {:name name})))

                                     :else
                                     (if (or (not (symbol? multi)) (resolve multi))
                                       [multi name]
                                       (throw (ex-info "Cannot resolve multimethod." {:name multi}))))]
     (if-not (= mm-name method-name)
       (let [method-name (with-meta method-name (merge config {:arglists arglists}))]
         `(let [~'v (def ~method-name (clojure/fn ~method-name ~@body))]
            [(multi:add ~mm-name ~val ~method-name)
             ~'v]))
       `[(multi:add ~mm-name ~val (clojure/fn ~@body))
         nil]))))

(defmethod bb.protocol-invoke/-invoke-intern :method
  [label name {:keys [multi val] :as config} body]
  (invoke-intern-method name config body))

(defn resolve-method
  "resolves a package related to a label

   (resolve-method bb.protocol-invoke/-invoke-intern
                   bb.protocol-invoke/-invoke-package
                   :fn
                   +default-packages+)
   => nil"
  {:added "3.0"}
  ([mmethod pkgmethod label lookup]
   (let [exists? (multi:get mmethod label)]
     (when (not exists?)
       (let [{:keys [ns]} (or (get lookup label)
                              (if (multi:get pkgmethod label)
                                (pkgmethod label)))]
         (if ns
           (require ns)
           (throw (ex-info "resolve package does not exist" {:label label}))))))))

(defn invoke-intern
  "main function to call for `definvoke`

   (invoke-intern :method
                  '-hello-method-
                  {:multi '-hello-multi-
                   :val :apple}
                  '([x] x))
   => '(clojure.core/let [v (def -hello-method- (clojure.core/fn -hello-method- [x] x))]
         [(bb.lib.invoke/multi:add -hello-multi- :apple -hello-method-) v])"
  {:added "3.0"}
  ([label name config body]
   (let [_ (resolve-method bb.protocol-invoke/-invoke-intern
                           bb.protocol-invoke/-invoke-package
                           label
                           +default-packages+)]
     (bb.protocol-invoke/-invoke-intern label name config body))))

(defmacro definvoke
  "customisable invocation forms

   (definvoke -another-
     [:compose {:val (partial + 10)
               :arglists '([& more])}])"
  {:added "3.0"}
  ([name doc? & [attrs? & [params & body :as more]]]
   (let [[doc attrs [label {:keys [refresh stable] :as config}] & body]
         (fn/fn:create-args (concat [doc? attrs?] more))]
     (if (or refresh
             (not (true? stable))
             (not (resolve name)))
       `(invoke-intern ~label '~name ~(assoc (merge config attrs) :doc doc) '~body)))))

;; Using definvoke

(definvoke invoke-intern-fn
  "method body for `:fn` invoke

   (invoke-intern-fn :fn '-fn-form- {} '([x] x))"
  {:added "3.0"}
  [:method {:multi bb.protocol-invoke/-invoke-intern
            :val :fn}]
  ([_ name config body]
   (let [arglists (invoke:arglists body)
         name (with-meta name (merge config {:arglists arglists}))]
     `(def ~name (clojure/fn ~(symbol (str name)) ~@body)))))

(definvoke invoke-intern-dynamic
  "constructs a body for the :dynamic keyword

   (invoke-intern-dynamic nil '-hello- {:val :world} nil)"
  {:added "3.0"}
  [:method {:multi bb.protocol-invoke/-invoke-intern
            :val :dynamic}]
  ([_ name config _]
   (let [earmuff  (with-meta (symbol (str "*" name "*"))
                    {:dynamic true})
         name     (with-meta name
                    (dissoc config :val))]
     `(do
        (def ~earmuff ~(:val config))
        (defn ~name
          ([] ~earmuff)
          ([~'v]
           (alter-var-root (var ~earmuff) (clojure.core/fn [~'_] ~'v))))))))

(definvoke invoke-intern-multi
  "method body for `:multi` form

   (invoke-intern-multi :multi '-multi-form- {} '([x] x))"
  {:added "3.0"}
  [:method {:multi bb.protocol-invoke/-invoke-intern
            :val :multi}]
  ([_ ^clojure.lang.Symbol name {:keys [refresh default hierarchy] :as config} body]
   (let [[dispatch arglists]
         (if (seq body)
           [`(clojure/fn ~(symbol (str name)) ~@body)
            (invoke:arglists body)]
           [(:dispatch config)
            (or (:arglists config) ())])
         options   (select-keys config [:default :hierarchy])
         default   (or default :default)
         hierarchy (or hierarchy '(var clojure.core/global-hierarchy))]
     `(do (@#'clojure.core/check-valid-options ~options :default :hierarchy)
          (let [~'v (def ~name)]
            (if (or (not (and (.hasRoot ~'v)
                              (instance? clojure.lang.MultiFn (deref ~'v))))
                    ~refresh)
              (doto (def ~name
                      (new clojure.lang.MultiFn ~(.getName name) ~dispatch ~default ~hierarchy))
                (alter-meta! merge ~config {:arglists ~arglists}))))))))

(definvoke invoke-intern-compose
  "method body for `:compose` form

   (invoke-intern-compose :compose
                          '-compose-form-
                          {:val '(partial + 1 2)
                          :arglists ''([& more])} nil)"
  {:added "3.0"}
  [:method {:multi bb.protocol-invoke/-invoke-intern
            :val :compose}]
  ([_ name {:keys [val arglists] :as config} _]
   (let [name (with-meta name
                (dissoc config :val))]
     `(def ~name ~val))))

(definvoke invoke-intern-macro
  "method body for `:macro` form

   (defn -macro-fn- [] '[(clojure.core/fn [x] x)
                         {:arglists ([x])}])

   (invoke-intern-macro :macro
                        '-macro-form-
                        {:fn '-macro-fn-
                         :args []}
                        nil)"
  {:added "3.0"}
  [:method {:multi bb.protocol-invoke/-invoke-intern
            :val :macro}]
  ([_ name {:keys [args fn] :as config} _]
   (let [func (resolve fn)
         [val meta] (apply func args)
         name (with-meta name (dissoc config :fn :args))]
     `(def ~name ~val))))

(defn recent-fn
  "creates a recent function"
  {:added "3.0"}
  ([{:keys [key compare op cache]}]
   (clojure.core/fn
     [obj & args]
     (let [k   (key obj)
           tag (compare obj)
           do-update (clojure.core/fn
                       []
                       (let [return (op obj)]
                         (swap! cache assoc k {:tag tag :return return})
                         return))]
       (cond *force*
             (do-update)

             :else
             (let [rec (get @cache k)]
               (if  (or (nil? rec)
                        (not= (:tag rec) tag))
                 (do-update)
                 (:return rec))))))))

(definvoke invoke-intern-recent
  "creates a body for `recent`"
  {:added "3.0"}
  [:method {:multi bb.protocol-invoke/-invoke-intern
            :val :recent}]
  ([_ name {:keys [key compare function cache] :as config} body]
   (let [arglists (if (seq body)
                    (invoke:arglists body)
                    (or (:arglists config) ()))

         [cache-name cache-form]
         (if (or (map? cache)
                 (nil? cache))
           (let [prefix (or (:prefix cache) "+")
                 cache-name (symbol (str prefix name))]
             [cache-name [`(def ~cache-name (atom {}))]])
           [cache []])

         [function-name function-form]
         (if (or (map? function)
                 (nil? function))
           (let [suffix (or (:suffix function) "-raw")
                 function-name (symbol (str name suffix))]
             [function-name [`(defn ~function-name
                                ~(str "helper function for " *ns* "/" name)
                                ~@body)]])
           [function []])

         name (with-meta name (assoc (dissoc config :key :compare :op :cache)
                                     :arglists arglists))]
     `(do ~@cache-form
          ~@function-form
          (def ~name (recent-fn {:key ~(or key `identity)
                                 :compare ~(or compare `identity)
                                 :op ~function-name
                                 :cache ~cache-name}))))))


;;
;; redefine fn
;;


(def +default-fn+
  '{:function   {:ns bb.lib.lamdba}
    :predicate  {:ns bb.lib.lamdba}
    :scala      {:ns std.lang.scala.function}})

(defn fn-body
  "creates a function body

   (fn-body :clojure '([] (+ 1 1)))
   => '(clojure.core/fn [] (+ 1 1))"
  {:added "3.0"}
  ([label body]
   (let [_ (resolve-method protocol.invoke/-fn-body
                           protocol.invoke/-fn-package
                           label
                           +default-fn+)]
     (protocol.invoke/-fn-body label body))))

(definvoke fn-body-clojure
  "creates a clojure function body

   (fn-body-clojure nil '([] (+ 1 1)))
   => '(clojure.core/fn [] (+ 1 1))"
  {:added "3.0"}
  [:method {:multi protocol.invoke/-fn-body
            :val :clojure}]
  ([body] (fn-body-clojure nil body))
  ([_ body] `(clojure.core/fn ~@body)))

(defmacro fn
  "macro body for extensible function

   (fn [x] x)
   => fn?"
  {:added "3.0"}
  ([& body]
   (let [type (or (:type (meta &form))
                  :clojure)]
     (protocol.invoke/-fn-body type body))))
