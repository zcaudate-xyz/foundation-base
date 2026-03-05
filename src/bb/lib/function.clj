(ns bb.lib.function
  (:import (clojure.lang Fn)
           (java.lang.reflect Method)))

(def +specialized+
  '{int "Int"
    long "Long"
    double "Double"
    boolean "Boolean"})

(defn fn-form
  "creates a lambda form"
  {:added "3.0"}
  [cls method bindings body]
  `(reify ~(symbol (str "java.util.function." cls))
     (~(symbol method) [~'_ ~@(map #(with-meta % {}) bindings)] ~@body)))

(defn fn-tags
  "creates tags for fn

   (fn-tags ^{:tag 'long} [])
   => [\"Long\" nil nil]

   (fn-tags ^{:tag 'long} [^{:tag 'long} [] ^{:tag 'int} []])
   => [\"Long\" \"Long\" \"Int\"]"
  {:added "3.0"}
  [bindings]
  (let [tag-fn (fn [form]
                 (if form
                   (if-let [tag (get (meta form) :tag)]
                     (get +specialized+ tag))))]
    [(tag-fn bindings)
     (tag-fn (first bindings))
     (tag-fn (second bindings))]))

(defmacro fn:supplier
  "creates a java supplier"
  {:added "3.0" :style/indent 1}
  ([bindings & body]
   (let [[return] (fn-tags bindings)
         [cls method] (if return
                        [(str return "Supplier") (str "getAs" return)]
                        ["Supplier" "get"])]
     (fn-form cls method bindings body))))

(defmacro fn:predicate
  "creates a java predicate

   (-> (fn:predicate [^long a] (< a 10))
       (.test 3))
   => true"
  {:added "3.0" :style/indent 1}
  ([bindings & body]
   (let [len (count bindings)
         [_ a0] (fn-tags bindings)
         cls (case len
               1 (if a0
                   (str a0 "Predicate")
                   "Predicate")
               2 "BiPredicate")]
     (fn-form cls "test" bindings body))))

(defmacro fn:lambda
  "creates java unary/binary functions"
  {:added "3.0" :style/indent 1}
  ([bindings & body]
   (let [len (count bindings)
         [return a0] (fn-tags bindings)
         [cls method] (case len
                        1 (cond (and return a0)
                                [(str a0 "To" return "Function") (str "applyAs" return)]

                                return
                                [(str "To" return "Function") (str "applyAs" return)]

                                a0
                                [(str a0 "Function") "apply"]

                                :else ["Function" "apply"])
                        2 ["BiFunction" "apply"])]
     (fn-form cls method bindings body))))

(defmacro fn:consumer
  "creates a java unary function"
  {:added "3.0" :style/indent 1}
  ([bindings & body]
   (let [len (count bindings)
         [_ a0] (fn-tags bindings)
         cls (case len
               1 (if a0
                   (str a0 "Consumer")
                   "Consumer")
               2 "BiConsumer")]
     (fn-form cls "accept" bindings `[(do ~@body nil)]))))

(defn vargs?
  "checks that function contain variable arguments

   (vargs? (fn [x])) => false

   (vargs? (fn [x & xs])) => true"
  {:added "3.0"}
  ([^Fn f]
   (if (some (fn [^Method mthd]
               (= "getRequiredArity" (.getName mthd)))
             (.getDeclaredMethods (class f)))
     true
     false)))

(defn varg-count
  "counts the number of arguments types before variable arguments

   (varg-count (fn [x y & xs])) => 2

   (varg-count (fn [x])) => nil"
  {:added "3.0"}
  ([f]
   (if (some (fn [^Method mthd]
               (= "getRequiredArity" (.getName mthd)))
             (.getDeclaredMethods (class f)))
     (.getRequiredArity f))))

(defn arg-count
  "counts the number of non-varidic argument types

   (arg-count (fn [x])) => [1]

   (arg-count (fn [x & xs])) => []

   (arg-count (fn ([x]) ([x y]))) => [1 2]"
  {:added "3.0"}
  ([f]
   (let [ms (filter (fn [^Method mthd]
                      (= "invoke" (.getName mthd)))
                    (.getDeclaredMethods (class f)))
         ps (map (fn [^Method m]
                   (.getParameterTypes m)) ms)]
     (map alength ps))))

(defn arg-check
  "counts the number of non-varidic argument types

   (arg-check (fn [x]) 1) => true

   (arg-check (fn [x & xs]) 1) => true

   (arg-check (fn [x & xs]) 0)
   => (throws-info {:required 0 :actual [() 1]})"
  {:added "3.0"}
  ([f num]
   (arg-check f num "Wrong number of arguments"))
  ([f num message]
   (or (if-let [vc (varg-count f)]
         (<= vc num))
       (boolean (some #{num} (arg-count f)))
       (throw (ex-info message
                       {:function f
                        :required num
                        :actual [(arg-count f) (varg-count f)]})))))

(defn fn:init-args
  "creates init args

   (fn:init-args '[x] '(inc x) [])
   => '[\"\" {} ([x] (inc x))]"
  {:added "3.0"}
  [doc? attr? more]
  (let [[doc attr? more] (if (string? doc?)
                           [doc? attr? more]
                           (if (nil? attr?)
                             ["" doc? more]
                             ["" doc? (cons attr? more)]))
        [attr more] (if (map? attr?)
                      [attr? more]
                      [{} (cons attr? more)])]
    [doc attr more]))

(defn fn:create-args
  "creates args for the body

   (fn:create-args '[[x] (inc x) nil nil])
   => '(\"\" {} [x] (inc x))"
  {:added "3.0"}
  ([[doc? attr? & more :as arglist]]
   (let [[doc attr more] (fn:init-args doc? attr? more)]
     (->> more
          (cons attr)
          (cons doc)
          (keep identity)))))

(defn fn:def-form
  "creates a def form"
  {:added "3.0"}
  ([name attrs body]
   (let [name (with-meta name attrs)]
     `(def ~name ~body)))
  ([name doc attrs arglist body]
   (let [arglists (cond (nil? arglist)
                        nil

                        (vector? arglist)
                        `(quote ~(list arglist))

                        :else
                        `(quote ~arglist))]
     (fn:def-form name
                  (merge attrs
                         {:doc doc}
                         (if arglists {:arglists arglists}))
                  body))))
