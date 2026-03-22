(ns std.lib.generate
  (:require [std.lib.walk :as walk]))

(defn- quoted? [x] (boolean (and (seq? x) ('#{quote clojure.core/quote} (first x)))))

(defn- postwalk-code [f expr]
  (if (quoted? expr)
    expr
    (walk/walk (partial postwalk-code f) f expr)))

(defn macroexpand-code
  "macroexpand code, keeping the original meta"
  {:added "3.0"}
  ([form]
   (walk/prewalk
    (fn [x]
      (if (quoted? x)
        x
        (let [e (macroexpand x)]
          (if (instance? clojure.lang.IObj e)
            (with-meta e (meta x))
            e))))
    form)))

(defn tag-visited
  "appends `:form/yield` to the form meta"
  {:added "3.0"}
  ([e]
   (vary-meta e merge {:form/yield true})))

(defn visit-sym
  "returns the visitor dispatch
 
   (visit-sym '(if :a 1 2))
   => 'if
 
   (visit-sym '(yield 1))
   => 'yield
 
   (visit-sym '(std.lib.generate/yield 1))
   => 'yield"
  {:added "3.0"}
  [[x :as form]]
  (if (symbol? x)
    (let [v   (resolve x)
          sym (if v (.toSymbol ^clojure.lang.Var v))]
      (if (#{'std.lib.generate/yield
             'std.lib.generate/yield-all} sym)
        (symbol (name sym))
        x))
    x))

(defn visited?
  "checks if form has been visted
 
   (-> (tag-visited '(loop (+ 1 2 3)))
       visited?)
   => true"
  {:added "3.0"}
  ([e]
   (-> e meta :form/yield boolean)))

(defmulti visit
  "testing inputs for visit"
  {:added "3.0"}
  (fn [e] (if (seq? e) (visit-sym e))))

(defmethod visit :default
  ([e] e))

(defmethod visit 'do
  ([[do & bodies]]
   (assert (= 'do do)) (let [rs (map visit bodies)]
                         (if-not (some visited? rs)
                           `(do ~@bodies nil)
                           (tag-visited
                            (if (= 1 (count rs))
                              (first rs)
                              `(concat ~@(doall (for [r rs]
                                                  (if (visited? r)
                                                    (list 'lazy-seq r)
                                                    (list 'lazy-seq (list do r nil))))))))))))

(def ^:dynamic *loop-id*)

(defmethod visit 'loop*
  ([[loop exprs & bodies]]
   (assert (= 'loop* loop)) (assert (vector? exprs)) (binding [*loop-id* (gensym "loop")]
                                                       (let [body (visit (cons 'do bodies))]
                                                         (if-not (visited? body)
                                                           (list 'do (list* 'loop* exprs bodies) nil)
                                                           (tag-visited
                                                            (list*
                                                             (list 'fn *loop-id* (mapv first (partition 2 exprs)) body)
                                                             (map second (partition 2 exprs)))))))))

(defmethod visit 'recur
  ([[_ & args]]
   (tag-visited `(lazy-seq (~*loop-id* ~@args)))))

(defmethod visit 'if
  ([[_ cond then else]]
   (let [then (visit then)
         else (visit else)]
     (if-not (or (visited? then) (visited? else))
       (list 'if cond then else)
       (tag-visited
        (list 'if
              cond
              (if (visited? then)
                then
                (list 'do then nil))
              (if (visited? else)
                else
                (list 'do else nil))))))))

(defmethod visit 'let*
  ([[_ bindings & bodies]]
   (let [body (visit (cons 'do bodies))]
     (cond-> `(let* ~bindings ~body)
       (visited? body) (tag-visited)))))

(defmethod visit 'letfn*
  ([[_ bindings & bodies]]
   (let [body (visit (cons 'do bodies))]
     (cond-> `(letfn* ~bindings ~body)
       (visited? body) (tag-visited)))))

(defmethod visit 'case*
  ([[_ e shift mask default m & args]]
   (let [default (visit default)

         {:keys [any-clause-rewritten]
          m :result}
         (reduce (fn [acc [minhash [c then]]]
                   (let [then (visit then)]
                     (if (visited? then)
                       (-> acc
                           (assoc-in [:result minhash] [c then])
                           (assoc :any-clause-rewritten true))
                       (assoc-in acc
                                 [:result minhash]
                                 [c `(do ~then nil)]))))
                 {:any-clause-rewritten false
                  :result {}}
                 m)]
     (cond-> `(case* ~e
                     ~shift
                     ~mask
                     ~(if (visited? default)
                        default
                        `(do ~default nil))
                     ~m
                     ~@args)
       (or (visited? default)
           any-clause-rewritten) (tag-visited)))))

(defmethod visit 'yield
  ([e]
   (assert (= 2 (count e)) "Call to (yield ..) must have 1 parameter!") (tag-visited (list 'list (second e)))))

(defmethod visit 'yield-all
  ([e]
   (assert (= 2 (count e)) "Call to (yield-all ..) must have 1 parameter!") (tag-visited `(lazy-seq ~(second e)))))

;;
;; seq generator
;;

(defmacro gen
  "returns a generator iteratively using yield
 
   (gen (loop [i 10]
          (if-not (zero? i)
            (do (yield i)
                (recur (dec i))))))
   => '(10 9 8 7 6 5 4 3 2 1)
 
   (->> (gen (loop [i 0]
               (if (even? i)
                 (yield i))
               (recur (inc i))))
        (take 5))
   => [0 2 4 6 8]"
  {:added "3.0"}
  ([& bodies]
   (let [result (visit (macroexpand-code (list* 'do bodies)))]
     (if-not (visited? result)
       (throw (ex-info "Call to (seq-expr ...) should have at least one yield or yield-all call in it!"
                       {:e result}))
       `(lazy-seq ~result)))))

(defn yield
  "yields single value has to be used to within gen form
 
   (yield 1)
   => (throws)"
  {:added "3.0"}
  ([e]
   (throw (ex-info "Can not use yield outside of gen-seq form!" {:expr e}))))

(defn yield-all
  "same as yield but returns entire seq"
  {:added "3.0"}
  ([e]
   (throw (ex-info "Can not use yield-all outside of gen-seq form!" {:expr e}))))

