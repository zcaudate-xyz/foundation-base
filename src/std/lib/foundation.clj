(ns std.lib.foundation
  (:require [clojure.set])
  (:import (hara.lib.foundation Clock Flake Counter)
           (java.util Date))
  (:refer-clojure :exclude [-> ->> keyword reset! aget set! assert
                            parse-long parse-double]))

(def +init+ find-ns)

(def ^:dynamic *sep* "/")

(defn T
  "returns `true` for any combination of input `args`
 
   (T) => true"
  {:added "3.0"}
  ([& args]
   true))

(defn F
  "returns `false` for any combination of input `args`
 
   (F) => false"
  {:added "3.0"}
  ([& args]
   false))

(defn NIL
  "returns `nil` for any combination of input `args`
 
   (NIL) => nil"
  {:added "3.0"}
  ([& args]
   nil))

(defn U
  "U Combinator
 
   ((U factorial) 5) => 120"
  {:added "3.0"}
  ([f] (fn [x] ((f f) x))))

(defn Z
  "Z combinator
 
   ((Z factorial) 5) => 120"
  {:added "3.0"}
  ([f] (let [A (fn [x] (f (U x)))] (A A))))

(defmacro xor
  "performs xor comparison
 
   (xor true false) => true"
  {:added "3.0"}
  ([] nil)
  ([a] a)
  ([a b]
   `(let [a# ~a
          b# ~b]
      (if a#
        (if b# nil a#)
        (if b# b# nil)))))

(defn sid
  "returns a short id string
 
   (sid)
   ;; \"t8a6euzk9usy\"
   => string?"
  {:added "3.0"}
  ([]
   (clojure.core/-> (str (java.util.UUID/randomUUID))
                    (.getBytes)
                    (java.nio.ByteBuffer/wrap)
                    (.getLong)
                    (Long/toString Character/MAX_RADIX))))


(def ^{:arglists '[[tag]]}
  sid-tag
  (memoize (fn [tag]
             (sid))))

(defn uuid
  "returns a `java.util.UUID` object
 
   (uuid) => #(instance? java.util.UUID %)
 
   (uuid \"00000000-0000-0000-0000-000000000000\")
   => #uuid \"00000000-0000-0000-0000-000000000000\""
  {:added "3.0"}
  ([] (java.util.UUID/randomUUID))
  ([id]
   (cond (string? id)
         (java.util.UUID/fromString id)

         (instance? (Class/forName "[B") id)
         (java.util.UUID/nameUUIDFromBytes id)

         (keyword? id)
         (java.util.UUID. (.hashCode ^Object id)
                          (.hashCode (name id)))

         :else
         (throw (ex-info (str id " can only be a string or byte array")))))
  ([^Long msb ^Long lsb]
   (java.util.UUID. msb lsb)))

(defn uuid-nil
  "constructs a nil uuid"
  {:added "4.0"}
  ([]
   (java.util.UUID/fromString "00000000-0000-0000-0000-000000000000")))

(defn instant
  "returns a `java.util.Date` object
 
   (instant) => #(instance? java.util.Date %)
 
   (instant 0) => #inst \"1970-01-01T00:00:00.000-00:00\""
  {:added "3.0"}
  ([] (java.util.Date.))
  ([^Long val] (java.util.Date. val)))

(defn uri
  "returns a `java.net.UrI` object
 
   (uri \"http://www.google.com\")
   => java.net.URI"
  {:added "3.0"}
  ([path]
   (java.net.URI/create path)))

(defn url
  "retruns a `java.net.URL` object
 
   (url \"http://www.google.com\")
   => java.net.URL"
  {:added "3.0"}
  ([path]
   (java.net.URL. path)))

(defn date
  "creates a new date
 
   (date 1000)
   => #inst \"1970-01-01T00:00:01.000-00:00\""
  {:added "3.0"}
  ([]  (Date.))
  ([^long t] (Date. t)))

(defn strn
  "returns the strn value
 
   (strn :hello) => \"hello\"
 
   (strn \"hello\") => \"hello\""
  {:added "3.0"}
  (^String
   [obj]
   (cond (bytes? obj)
         (String. ^bytes obj)

         (keyword? obj)
         (subs (str obj) 1)

         (string? obj)
         obj

         :else
         (pr-str obj)))
  (^String
   [obj & more]
   (apply str (strn obj) (map strn more))))

(defn lsubs
  "substring from last
 
   (lsubs \"hello-world\" 4)
   => \"hello-w\""
  {:added "4.0"}
  [s n]
  (let [l (count s)]
    (subs s 0 (- l n))))

(defn keyword
  "returns the keyword value
 
   (keyword :hello) => :hello
 
   (keyword \"hello\") => :hello"
  {:added "3.0"}
  ([obj]
   (cond (keyword? obj)
         obj

         (string? obj)
         (clojure.core/keyword obj)

         :else
         (throw (ex-info "Cannot process obj" {:input obj})))))

(defn string
  "creates a string from a byte array
 
   (string (.getBytes \"Hello\"))
   => \"Hello\""
  {:added "3.0"}
  ([obj]
   (cond (bytes? obj)
         (String. ^bytes obj)

         :else
         (str obj))))

(defn concatv
  "concats two seqs as vector"
  {:added "4.0"}
  [& args]
  (vec (apply concat args)))

(defn edn
  "prints then reads back the string
 
   ((juxt type
          (comp type edn)) (symbol \":hello\"))
   => [clojure.lang.Symbol
       clojure.lang.Keyword]"
  {:added "3.0"}
  ([obj]
   (read-string (pr-str obj))))

(defn counter
  "creates a counter
 
   (pr-str (counter))
   => \"#counter{-1}\""
  {:added "3.0"}
  ([] (counter -1))
  ([n]
   (Counter. n)))

(defmethod print-method Counter
  ([v ^java.io.Writer w]
   (.write w (format "#counter{%d}" @v))))

(defmacro inc!
  "increments the counter
 
   @(doto (counter)
      (inc!)
      (inc! 10))
   => 10"
  {:added "3.0"}
  ([counter]
   `(.inc ~(with-meta counter {:tag 'hara.lib.foundation.Counter})))
  ([counter n]
   `(.inc ~(with-meta counter {:tag 'hara.lib.foundation.Counter}) ~n)))

(defmacro dec!
  "decrements the counter
 
   @(doto (counter 10)
      (dec! 10))
   => 0"
  {:added "3.0"}
  ([counter]
   `(.dec ~(with-meta counter {:tag 'hara.lib.foundation.Counter})))
  ([counter n]
   `(.dec ~(with-meta counter {:tag 'hara.lib.foundation.Counter}) ~n)))

(defn reset!
  "resets a counter to the given value
 
   (reset! (counter 10) 5)
   => 5"
  {:added "3.0"}
  ([^Counter counter n]
   (.reset counter n)))

(defn flake
  "returns a unique, time incremental id
 
   (flake)
   ;; \"4Wv-T47LftnVlHhhh00JYuU15NCuNLcr\"
   => string?"
  {:added "3.0"}
  ([]
   (str (Flake/flake))))

(defn invoke
  "provides a caller for the `IFn`
 
   (invoke (fn [] (+ 1 2 3)))
   => 6"
  {:added "3.0"}
  ([^clojure.lang.IFn f]
   (.invoke f))
  ([^clojure.lang.IFn f & args]
   (.applyTo f args)))

(defn call
  "like `invoke` but reverses the function and first argument
 
   (call 2) => 2
 
   (call 2 + 1 2 3) => 8"
  {:added "3.0"}
  ([obj] obj)
  ([obj f] (if (nil? f) obj (f obj)))
  ([obj f v1] (if (nil? f) obj (f obj v1)))
  ([obj f v1 v2] (if (nil? f) obj (f obj v1 v2)))
  ([obj f v1 v2 v3] (if (nil? f) obj (f obj v1 v2 v3)))
  ([obj f v1 v2 v3 v4] (if (nil? f) obj (f obj v1 v2 v3 v4)))
  ([obj f v1 v2 v3 v4 & vs] (if (nil? f) obj (apply f obj v1 v2 v3 v4 vs))))

(defmacro const
  "converts an expression into a constant at compile time
 
   (const (+ 1 2)) => 3
 
   (macroexpand '(const (+ 1 2))) => 3"
  {:added "3.0"}
  ([body]
   (eval body)))

(defmacro applym
  "Allow macros to be applied to arguments just like functions
 
   (applym const '((+ 1 2))) => 3
 
   (macroexpand '(applym const '((+ 1 2))))
   => 3"
  {:added "3.0"}
  ([macro & args]
   (cons macro (#'clojure.core/spread (map eval args)))))

(defmacro code-ns
  "returns the current namespace the code is in
 
   (code-ns)
   => 'std.lib.foundation_test"
  {:added "3.0"}
  ([]
   `(clojure.core/-> (re-find #"^.*?(?=\$|$)"
                              (str (fn [])))
                     (.replaceAll "_" "-")
                     symbol)))

(defmacro code-line
  "gets the current code line
 
   (code-line)
   => number?"
  {:added "3.0"}
  ([]
   (:line (meta &form))))

(defmacro code-column
  "gets the current code column
 
   (code-column)
   => 3"
  {:added "3.0"}
  ([]
   (:column (meta &form))))

(defn thread-form
  "helper function to '->' and '->>'
 
   (thread-form '[(+ 1) dec]
                (fn [form] (concat form ['%])))
   => '(% (+ 1 %) % (dec %))"
  {:added "3.0"}
  ([forms transform-fn]
   (interleave (repeat '%)
               (map (fn [form]
                      (cond (list? form)
                            (if (some (fn [v] (= v '%)) (flatten form))
                              form
                              (transform-fn form))

                            :else
                            (transform-fn (list form))))
                    forms))))

(defmacro ->
  "like -> but with % as placeholder
 
   (-> 1
       inc
       (- 10 %))
   => 8"
  {:added "3.0"}
  ([expr & forms]
   `(let [~'% ~expr
          ~@(thread-form forms (fn [form] (concat [(first form) '%] (rest form))))]
      ~'%)))

(defmacro ->>
  "like ->> but with % as placeholder
 
   (-> 1
       inc
       (- % 10))
   => -8"
  {:added "3.0"}
  ([expr & forms]
   `(let [~'% ~expr
          ~@(thread-form forms (fn [form] (concat form ['%])))]
      ~'%)))

(defn var-sym
  "converts a var to a symbol
 
   (var-sym #'var-sym)
   => 'std.lib.foundation/var-sym"
  {:added "3.0"}
  ([^clojure.lang.Var var]
   (symbol (str (.getName (.ns var)))
           (str (.sym var)))))

(defn unbound?
  "checks if a variable is unbound
 
   (declare -lost-)
 
   (unbound? -lost-)
   = true"
  {:added "3.0"}
  ([obj]
   (instance? clojure.lang.Var$Unbound obj)))

(defmacro set!
  "sets a var without changing current meta
 
   (std.lib.foundation/set! -lost- 1)"
  {:added "3.0"}
  ([sym val]
   (let [mt (meta sym)]
     (if-not (resolve sym)
       `(def ~(with-meta sym mt) ~val)
       `(do (alter-var-root (var ~sym) (constantly ~val))
            ~@(if (not-empty mt) [`(alter-meta! (var ~sym) merge ~mt)])
            (var ~sym))))))

(defmacro suppress
  "Suppresses any errors thrown in the body.
 
   (suppress (throw (ex-info \"Error\" {}))) => nil
 
   (suppress (throw (ex-info \"Error\" {})) :error) => :error
 
   (suppress (throw (ex-info \"Error\" {}))
             (fn [^Throwable e]
               (.getMessage e))) => \"Error\""
  {:added "3.0"}
  ([body]
   `(try ~body (catch Throwable ~'t)))
  ([body catch-val]
   `(try ~body (catch Throwable ~'t
                 (cond (fn? ~catch-val)
                       (~catch-val ~'t)
                       :else ~catch-val)))))

(defmacro with-thrown
  "gets the exception data in a form
 
   (ex-data (with-thrown (throw (ex-info \"Error\" {:hello \"1\"}))))
   => {:hello \"1\"}"
  {:added "4.0"}
  [& body]
  `(try ~@body (catch Throwable ~'t ~'t)))

(defmacro with-ex
  "gets the exception data in a form
 
   (with-ex (throw (ex-info \"Error\" {:hello \"1\"})))
   => {:hello \"1\"}"
  {:added "4.0"}
  [& body]
  `(ex-data (try ~@body (catch Throwable ~'t ~'t))))

(defn with-retry-fn
  "with a retry function"
  {:added "4.0"}
  [limit sleep f]
  (loop [limit limit
         cause nil]
    (when (< 0 limit)
      (throw (ex-info "Retries all failed" {} cause)))
    (let [[res ex] (try [(f) nil] 
                        (catch Throwable t [nil t]))]
      (cond res res

            :else
            (do (Thread/sleep (long (or sleep 500)))
                (recur (- limit 1) ex))))))

(defmacro with-retry
  "with retry macro"
  {:added "4.0"}
  [[limit sleep] & body]
  (list `with-retry-fn limit sleep (apply list 'fn [] body)))

(defmacro error
  "throws an error with message
 
   (error \"Error\")
   => (throws)"
  {:added "3.0"}
  ([message]
   `(throw (ex-info ~message {})))
  ([message data]
   `(throw (ex-info ~message ~data))))

(defmacro trace
  "prints out the item and a program trace
 
   (trace \"Error\")
   => Throwable"
  {:added "3.0"}
  ([]
   '(try (ex-info "TRACE" {})
         (catch Throwable t t)))
  ([val]
   (list 'try (list 'throw (list 'ex-info "TRACE" {:val val}))
         '(catch Throwable t t))))

(defn throwable?
  "checks that object is a `Throwable`
 
   (throwable? (ex-info \"hello\" {}))
   => true"
  {:added "3.0"}
  ([obj]
   (instance? Throwable obj)))

(defn byte?
  "Returns `true` if `x` is of type `java.lang.Byte`
 
   (byte? (byte 1)) => true"
  {:added "3.0"}
  ([x] (instance? java.lang.Byte x)))

(defn short?
  "Returns `true` if `x` is of type `java.lang.Short`
 
   (short? (short 1)) => true"
  {:added "3.0"}
  ([x]
   (instance? java.lang.Short x)))

(defn long?
  "Returns `true` if `x` is of type `java.lang.Long`
 
   (long? 1)          => true
   (long? 1N)         => false"
  {:added "3.0"}
  ([x]
   (instance? java.lang.Long x)))

(defn bigint?
  "Returns `true` if `x` is of type `clojure.lang.BigInt`.
 
   (bigint? 1N)       => true
   (bigint? 1)        =>  false"
  {:added "3.0"}
  ([x]
   (instance? clojure.lang.BigInt x)))

(defn bigdec?
  "Returns `true` if `x` is of type `java.math.BigDecimal`.
 
   (bigdec? 1M)       => true
   (bigdec? 1.0)      => false"
  {:added "3.0"}
  ([x]
   (instance? java.math.BigDecimal x)))

(defn regexp?
  "Returns `true` if `x` implements `clojure.lang.IPersistentMap`.
 
   (regexp? #\"\\d+\") => true"
  {:added "3.0"}
  ([x] (instance? java.util.regex.Pattern x)))

(defn iobj?
  "checks if a component is instance of `clojure.lang.IObj`
 
   (iobj? 1) => false
 
   (iobj? {}) => true"
  {:added "3.0"}
  ([x]
   (instance? clojure.lang.IObj x)))

(defn iref?
  "Returns `true` if `x` is of type `clojure.lang.IRef`.
 
   (iref? (atom 0))  => true"
  {:added "3.0"}
  ([obj]
   (instance? clojure.lang.IRef obj)))

(defn ideref?
  "checks if 
 
   (ideref? (volatile! 0)) => true
   (ideref? (promise)) => true"
  {:added "3.0"}
  ([obj]
   (instance? clojure.lang.IDeref obj)))

(defn thread?
  "Returns `true` is `x` is a thread
 
   (thread? (Thread/currentThread)) => true"
  {:added "3.0"}
  ([obj]
   (instance? java.lang.Thread obj)))

(defn url?
  "Returns `true` if `x` is of type `java.net.URL`.
 
   (url? (java.net.URL. \"file:/Users/chris/Development\")) => true"
  {:added "3.0"}
  ([x] (instance? java.net.URL x)))

(defn atom?
  "Returns `true` if `x` is of type `clojure.lang.Atom`.
 
   (atom? (atom nil)) => true"
  {:added "3.0"}
  ([obj]
   (instance? clojure.lang.Atom obj)))

(defn comparable?
  "Returns `true` if `x` and `y` both implements `java.lang.Comparable`.
 
   (comparable? 1 1) => true"
  {:added "3.0"}
  ([x y]
   (and (instance? Comparable x)
        (instance? Comparable y)
        (= (type x) (type y)))))

(defn array?
  "checks if object is a primitive array
 
   (array? (into-array []))
   => true
 
   (array? String (into-array [\"a\" \"b\" \"c\"]))
   => true"
  {:added "3.0"}
  ([x]
   (.isArray ^Class (type x)))
  ([cls x]
   (and (array? x)
        (= cls (.getComponentType ^Class (type x))))))

(defn parse-long
  "parses a long from string
 
   (parse-long \"100\")
   => 100"
  {:added "3.0"}
  ([s]
   (Long/parseLong s)))

(defn parse-double
  "parses a double from string
 
   (parse-double \"100\")
   => 100.0
 
   (parse-double \"100.123\")
   => 100.123"
  {:added "3.0"}
  ([s]
   (Double/parseDouble s)))

(defn edn?
  "checks if an entry is valid edn
 
   (edn? 1) => true
 
   (edn? {}) => true
 
   (edn? (java.util.Date.))
   => false"
  {:added "3.0"}
  ([x]
   (or (nil? x)
       (boolean? x)
       (string? x)
       (char? x)
       (symbol? x)
       (keyword? x)
       (number? x)
       (seq? x)
       (vector? x)
       (record? x)
       (map? x)
       (set? x)
       (tagged-literal? x)
       (var? x)
       (regexp? x))))

(defn hash-id
  "returns the actual memory related id for an object"
  {:added "3.0"}
  ([obj]
   (System/identityHashCode obj)))

(defn hash-code
  "returns the hash code of an object"
  {:added "3.0"}
  ([^Object obj]
   (.hashCode obj)))

(defn aget
  "typesafe aget
 
   (aget (int-array [1 2 3]) 2)
   => 3"
  {:added "3.0"}
  ([arr i]
   (case (.getName ^Class (type arr))
     "[J" (clojure.core/aget ^longs arr i)
     "[I" (clojure.core/aget ^ints arr i)
     "[S" (clojure.core/aget ^shorts arr i)
     "[B" (clojure.core/aget ^bytes arr i)
     "[Z" (clojure.core/aget ^booleans arr i)
     "[F" (clojure.core/aget ^floats arr i)
     "[D" (clojure.core/aget ^doubles arr i))))

(defn demunge
  "demunges an input to be nicer looking
 
   (demunge ;;; (munge \"+test!\")
    \"_PLUS_test_BANG_\")
   => \"+test!\""
  {:added "3.0"}
  ([name]
   (clojure.lang.Compiler/demunge name)))

(def re-create
  (memoize (fn [^String s]
             (if (regexp? s)
               s
               (-> (str s)
                   (.replaceAll "\\." "\\\\\\.")
                   (.replaceAll "\\*" "\\\\\\*")
                   (.replaceAll "\\?" "\\\\\\?")
                   (re-pattern))))))

(defn intern-var
  "interns a var in sym"
  {:added "3.0"}
  ([ns sym var]
   (intern-var ns sym var nil))
  ([ns sym var ext]
   (let [out (intern ns sym  (deref var))]
     (alter-meta! out merge (meta var) ext)
     out)))

(defn intern-form
  "creates base form for `intern-in` and `intern-all`
 
   (intern-form 'std.lib 'std.lib.foundation/unfold)"
  {:added "3.0"}
  ([ns sym]
   (intern-form ns sym nil))
  ([ns sym meta]
   (let [[to from] (if (vector? sym)
                     sym
                     [sym sym])]
     `(intern-var (quote ~ns) (quote ~(symbol (name to)))
                  (var ~from)
                  (quote ~meta)))))

(defmacro intern-in
  "adds a function to current"
  {:added "3.0"}
  ([ns? & syms]
   (let [[ns syms] (if (or (vector? ns?)
                           (namespace ns?))
                     [(.getName clojure.core/*ns*) (cons ns? syms)]
                     [ns? syms])]
     (mapv (partial intern-form ns) syms))))

(defmacro intern-all
  "adds a namespace to current"
  {:added "3.0"}
  [& nss]
  (->> nss
       (mapcat (fn [ns]
                 (->> (ns-publics ns)
                      (vals)
                      (map (fn [^clojure.lang.Var var]
                             (let [sym (.toSymbol var)]
                               (intern-form (.getName clojure.core/*ns*)
                                            sym)))))))
       (vec)))

(def ^:private ^:dynamic *template-meta* nil)

(defmacro ^{:style/indent 1}
  with:template-meta
  "binds the template meta (for testing purposes)"
  {:added "3.0"}
  [meta & body]
  `(binding [*template-meta* ~meta]
     ~@body))

(defn template-meta
  "returns the intern template meta"
  {:added "3.0"}
  ([]
   *template-meta*))

(defmacro template-vars
  "adds a function to current given var and arguments"
  {:added "3.0" :style/indent 1}
  ([[tmpl-fn & [tmpl-meta]] & syms]
   (let [tmpl-fn (resolve tmpl-fn)]
     (binding [*template-meta* tmpl-meta]
       (mapv (fn [sym]
               (let [[to from & args] (if (symbol? sym)
                                        [(symbol (name sym)) sym]
                                        sym)]
                 (apply tmpl-fn to
                        (if from (resolve from)) args)))
             syms)))))

(defmacro ^{:style/indent 1}
  template-entries
  "uses a template function to produce entries"
  {:added "3.0" :style/indent 1}
  ([[tmpl-fn & [tmpl-meta]] & entries]
   (let [tmpl-fn (cond (symbol? tmpl-fn)
                       (resolve tmpl-fn)
                       
                       (list? tmpl-fn)
                       (eval tmpl-fn))
         entries (mapcat (fn [entry]
                           (cond (symbol? entry)
                                 @(resolve entry)

                                 (list? entry)
                                 (eval entry)

                                 :else entry))
                         entries)]
     (binding [*template-meta* (merge tmpl-meta (meta &form))]
       (mapv (fn [e]
               (try (tmpl-fn e)
                    (catch Throwable t
                      (eval (list 'std.lib/prn (list 'quote e)))
                      (throw t))))
             entries)))))

(defmacro ^{:style/indent 1}
  template-bulk
  "template-entries but for heavy usage"
  {:added "4.0"}
  [[tmpl-fn & [tmpl-meta]]
   entries]
  (let [tmeta (meta &form)]
    `(binding [*template-meta* (merge ~tmpl-meta ~tmeta)]
       (mapv (comp eval ~tmpl-fn) ~entries))))

(defn template-ensure
  "ensures that the templated entries are the same as the input"
  {:added "4.0"}
  [syms vars]
  (let [diff (clojure.set/difference (set (map (fn [[sym _]]
                                                 (symbol (str (.getName *ns*))
                                                         (name sym)))
                                               syms))
                                     (set (map var-sym vars)))]
    (when (not-empty diff)
      (eval (list 'do
                  (list 'std.lib/beep)
                  (list 'std.lib/prn diff))))
    vars))

(deftype Wrapped [val show type display]
  java.lang.Object
  (toString [_]
    (cond display
          (display val)

          :else
          (str (if type
                 (str "<" type ">"))
               "\n"
               ((or show identity) (str val)))))
  
  clojure.lang.IDeref
  (deref [_] val))

(defmethod print-method Wrapped
  [v ^java.io.Writer w]
  (.write w (str v)))

(defn wrapped
  "object to display shell result"
  {:added "4.0"}
  ([val]
   (wrapped val identity nil))
  ([val show]
   (wrapped val show nil nil))
  ([val show type]
   (Wrapped. val show type nil))
  ([val show type display]
   (Wrapped. val show type display)))

(defn wrapped?
  "checks that an object is wrapped"
  {:added "4.0"}
  [val]
  (instance? Wrapped val))

(defn resolve-namespaced
  "resolves a namespaced symbol"
  {:added "4.0"}
  [x]
  (if (and (symbol? x)
           (namespace x))
    (var-sym (resolve x))
    x))

(defmacro def.m
  "defs multiple vars"
  {:added "4.0"}
  [syms call]
  `(let [~'out ~call]
     ~@(map-indexed (fn [i sym]
                      `(def ~sym (nth ~'out ~i)))
                    syms)))
