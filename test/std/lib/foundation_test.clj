(ns std.lib.foundation-test
  (:use code.test)
  (:require [std.lib.foundation :refer :all])
  (:refer-clojure :exclude [keyword -> ->> reset! aget set!
                            parse-long parse-double]))

^{:refer std.lib.foundation/T :added "3.0"}
(fact "returns `true` for any combination of input `args`"

  (T) => true ^:hidden
  (T :hello) => true
  (T 1 2 3) => true
  1 => (capture odd?))

^{:refer std.lib.foundation/F :added "3.0"}
(fact "returns `false` for any combination of input `args`"

  (F) => false ^:hidden
  (F :hello) => false
  (F 1 2 3) => false)

^{:refer std.lib.foundation/NIL :added "3.0"}
(fact "returns `nil` for any combination of input `args`"

  (NIL) => nil ^:hidden
  (NIL :hello) => nil
  (NIL 1 2 3) => nil)

^{:refer std.lib.foundation/U :added "3.0"
  :let [factorial (fn [f]
                    (fn [n]
                      (if (zero? n)
                        1
                        (* n ((f f) (dec n))))))]}
(fact "U Combinator"

  ((U factorial) 5) => 120)

^{:refer std.lib.foundation/Z :added "3.0"
  :let [factorial (fn [f]
                    (fn [n]
                      (if (zero? n)
                        1
                        (* n (f (dec n))))))]}
(fact "Z combinator"

  ((Z factorial) 5) => 120
  ^:hidden

  ;; The body of Z can be defined as
  (defn- U1
    ([f] (fn [x] (f (U x)))))

  ;; For the record, the Y combinator
  ;; can be defined as follows

  (defn- U0
    ([f] (fn [x] (f (x x)))))

  (defn Y
    ([f] (let [A (U0 f)] (A A)))))

^{:refer std.lib.foundation/xor :added "3.0"}
(fact "performs xor comparison"

  (xor true false) => true ^:hidden
  (xor true true) => nil
  (xor false false) => nil)

^{:refer std.lib.foundation/sid :added "3.0"}
(fact "returns a short id string"

  (sid)
  ;; "t8a6euzk9usy"
  => string?)

^{:refer std.lib.foundation/uuid :added "3.0"}
(fact "returns a `java.util.UUID` object"

  (uuid) => #(instance? java.util.UUID %)

  (uuid "00000000-0000-0000-0000-000000000000")
  => #uuid "00000000-0000-0000-0000-000000000000")

^{:refer std.lib.foundation/uuid-nil :added "4.0"}
(fact "constructs a nil uuid"
  (uuid-nil)
  => #uuid "00000000-0000-0000-0000-000000000000")

^{:refer std.lib.foundation/instant :added "3.0"}
(fact "returns a `java.util.Date` object"

  (instant) => #(instance? java.util.Date %)

  (instant 0) => #inst "1970-01-01T00:00:00.000-00:00")

^{:refer std.lib.foundation/uri :added "3.0"}
(fact "returns a `java.net.UrI` object"

  (uri "http://www.google.com")
  => java.net.URI)

^{:refer std.lib.foundation/url :added "3.0"}
(fact "retruns a `java.net.URL` object"

  (url "http://www.google.com")
  => java.net.URL)

^{:refer std.lib.foundation/date :added "3.0"}
(fact "creates a new date"

  (date 1000)
  => #inst "1970-01-01T00:00:01.000-00:00")

^{:refer std.lib.foundation/strn :added "3.0"}
(fact "returns the strn value"

  (strn :hello) => "hello"

  (strn "hello") => "hello")

^{:refer std.lib.foundation/lsubs :added "4.0"}
(fact "substring from last"

  (lsubs "hello-world" 4)
  => "hello-w")

^{:refer std.lib.foundation/keyword :added "3.0"}
(fact "returns the keyword value"

  (keyword :hello) => :hello

  (keyword "hello") => :hello)

^{:refer std.lib.foundation/string :added "3.0"}
(fact "creates a string from a byte array"

  (string (.getBytes "Hello"))
  => "Hello")

^{:refer std.lib.foundation/concatv :added "4.0"}
(fact "concats two seqs as vector"
  ^:hidden
  
  (concatv [1 2 3]
           [4 5 6])
  => [1 2 3 4 5 6])

^{:refer std.lib.foundation/edn :added "3.0"}
(fact "prints then reads back the string"

  ((juxt type
         (comp type edn)) (symbol ":hello"))
  => [clojure.lang.Symbol
      clojure.lang.Keyword])

^{:refer std.lib.foundation/counter :added "3.0"}
(fact "creates a counter"

  (pr-str (counter))
  => "#counter{-1}")

^{:refer std.lib.foundation/inc! :added "3.0"}
(fact "increments the counter"

  @(doto (counter)
     (inc!)
     (inc! 10))
  => 10)

^{:refer std.lib.foundation/dec! :added "3.0"}
(fact "decrements the counter"

  @(doto (counter 10)
     (dec! 10))
  => 0)

^{:refer std.lib.foundation/reset! :added "3.0"}
(fact "resets a counter to the given value"

  (reset! (counter 10) 5)
  => 5)

^{:refer std.lib.foundation/flake :added "3.0"}
(fact "returns a unique, time incremental id"

  (flake)
  ;; "4Wv-T47LftnVlHhhh00JYuU15NCuNLcr"
  => string?)

^{:refer std.lib.foundation/invoke :added "3.0"}
(fact "provides a caller for the `IFn`"

  (invoke (fn [] (+ 1 2 3)))
  => 6)

^{:refer std.lib.foundation/call :added "3.0"}
(fact "like `invoke` but reverses the function and first argument"

  (call 2) => 2

  (call 2 + 1 2 3) => 8)

^{:refer std.lib.foundation/const :added "3.0"}
(fact "converts an expression into a constant at compile time"

  (const (+ 1 2)) => 3

  (macroexpand '(const (+ 1 2))) => 3)

^{:refer std.lib.foundation/applym :added "3.0"}
(fact "Allow macros to be applied to arguments just like functions"

  (applym const '((+ 1 2))) => 3

  (macroexpand '(applym const '((+ 1 2))))
  => 3)

^{:refer std.lib.foundation/code-ns :added "3.0"}
(fact "returns the current namespace the code is in"

  (code-ns)  
  => 'std.lib.foundation-test)

^{:refer std.lib.foundation/code-line :added "3.0"}
(fact "gets the current code line"

  (code-line)
  => number?)

^{:refer std.lib.foundation/code-column :added "3.0"}
(fact "gets the current code column"

  (code-column)
  => 3)

^{:refer std.lib.foundation/thread-form :added "3.0"}
(fact "helper function to '->' and '->>'"

  (thread-form '[(+ 1) dec]
               (fn [form] (concat form ['%])))
  => '(% (+ 1 %) % (dec %)))

^{:refer std.lib.foundation/-> :added "3.0"}
(fact "like -> but with % as placeholder"

  (-> 1
      inc
      (- 10 %))
  => 8)

^{:refer std.lib.foundation/->> :added "3.0"}
(fact "like ->> but with % as placeholder"

  (-> 1
      inc
      (- % 10))
  => -8)

^{:refer std.lib.foundation/var-sym :added "3.0"}
(fact "converts a var to a symbol"

  (var-sym #'var-sym)
  => 'std.lib.foundation/var-sym)

^{:refer std.lib.foundation/unbound? :added "3.0"}
(fact "checks if a variable is unbound"

  (declare -lost-)

  (unbound? -lost-)
  => true)

^{:refer std.lib.foundation/set! :added "3.0"}
(fact "sets a var without changing current meta"

  (def -lost- 1)
  (std.lib.foundation/set! -lost- 2)
  -lost- => 2)

^{:refer std.lib.foundation/suppress :added "3.0"}
(fact "Suppresses any errors thrown in the body."

  (suppress (throw (ex-info "Error" {}))) => nil

  (suppress (throw (ex-info "Error" {})) :error) => :error

  (suppress (throw (ex-info "Error" {}))
            (fn [^Throwable e]
              (.getMessage e))) => "Error")

^{:refer std.lib.foundation/with-thrown :added "4.0"}
(fact "gets the exception data in a form"

  (ex-data (with-thrown (throw (ex-info "Error" {:hello "1"}))))
  => {:hello "1"})

^{:refer std.lib.foundation/with-ex :added "4.0"}
(fact "gets the exception data in a form"

  (with-ex (throw (ex-info "Error" {:hello "1"})))
  => {:hello "1"})

^{:refer std.lib.foundation/with-retry-fn :added "4.0"}
(fact "with a retry function"
  ^:hidden
  
  (with-retry-fn
    3 100 (fn []
            (Thread/sleep 10)
            (throw (ex-info "hello" {}))))
  => (throws))

^{:refer std.lib.foundation/with-retry :added "4.0"}
(fact "with retry macro"
  ^:hidden
  
  (with-retry [3 100]
    (Thread/sleep 10)
    (throw (ex-info "hello" {})))
  => (throws))

^{:refer std.lib.foundation/error :added "3.0"}
(fact "throws an error with message"

  (error "Error")
  => (throws))

^{:refer std.lib.foundation/trace :added "3.0"}
(fact "prints out the item and a program trace"

  (trace "Error")
  => Throwable)

^{:refer std.lib.foundation/throwable? :added "3.0"}
(fact "checks that object is a `Throwable`"

  (throwable? (ex-info "hello" {}))
  => true)

^{:refer std.lib.foundation/byte? :added "3.0"}
(fact "Returns `true` if `x` is of type `java.lang.Byte`"

  (byte? (byte 1)) => true)

^{:refer std.lib.foundation/short? :added "3.0"}
(fact "Returns `true` if `x` is of type `java.lang.Short`"

  (short? (short 1)) => true)

^{:refer std.lib.foundation/long? :added "3.0"}
(fact "Returns `true` if `x` is of type `java.lang.Long`"

  (long? 1)          => true
  (long? 1N)         => false)

^{:refer std.lib.foundation/bigint? :added "3.0"}
(fact "Returns `true` if `x` is of type `clojure.lang.BigInt`."

  (bigint? 1N)       => true
  (bigint? 1)        =>  false)

^{:refer std.lib.foundation/bigdec? :added "3.0"}
(fact "Returns `true` if `x` is of type `java.math.BigDecimal`."

  (bigdec? 1M)       => true
  (bigdec? 1.0)      => false)

^{:refer std.lib.foundation/regexp? :added "3.0"}
(fact "Returns `true` if `x` implements `clojure.lang.IPersistentMap`."

  (regexp? #"\d+") => true)

^{:refer std.lib.foundation/iobj? :added "3.0"}
(fact "checks if a component is instance of `clojure.lang.IObj`"

  (iobj? 1) => false

  (iobj? {}) => true)

^{:refer std.lib.foundation/iref? :added "3.0"}
(fact "Returns `true` if `x` is of type `clojure.lang.IRef`."

  (iref? (atom 0))  => true ^:hidden
  (iref? (ref 0))   => true
  (iref? (agent 0)) => true
  (iref? (volatile! 0)) => false
  (iref? (promise)) => false
  (iref? (clojure.core/future))  => false)

^{:refer std.lib.foundation/ideref? :added "3.0"}
(fact "checks if "

  (ideref? (volatile! 0)) => true
  (ideref? (promise)) => true)

^{:refer std.lib.foundation/thread? :added "3.0"}
(fact "Returns `true` is `x` is a thread"

  (thread? (Thread/currentThread)) => true)

^{:refer std.lib.foundation/url? :added "3.0"}
(fact "Returns `true` if `x` is of type `java.net.URL`."

  (url? (java.net.URL. "file:/Users/chris/Development")) => true)

^{:refer std.lib.foundation/atom? :added "3.0"}
(fact "Returns `true` if `x` is of type `clojure.lang.Atom`."

  (atom? (atom nil)) => true)

^{:refer std.lib.foundation/comparable? :added "3.0"}
(fact "Returns `true` if `x` and `y` both implements `java.lang.Comparable`."

  (comparable? 1 1) => true)

^{:refer std.lib.foundation/array? :added "3.0"}
(fact "checks if object is a primitive array"

  (array? (into-array []))
  => true

  (array? String (into-array ["a" "b" "c"]))
  => true)

^{:refer std.lib.foundation/parse-long :added "3.0"}
(fact "parses a long from string"

  (parse-long "100")
  => 100)

^{:refer std.lib.foundation/parse-double :added "3.0"}
(fact "parses a double from string"

  (parse-double "100")
  => 100.0

  (parse-double "100.123")
  => 100.123)

^{:refer std.lib.foundation/edn? :added "3.0"}
(fact "checks if an entry is valid edn"

  (edn? 1) => true

  (edn? {}) => true

  (edn? (java.util.Date.))
  => false)

^{:refer std.lib.foundation/hash-id :added "3.0"}
(fact "returns the actual memory related id for an object" ^:hidden

  (hash-id {})
  => (hash-id {})

  (= (hash-id {:a 1})
     (hash-id {:a 1}))
  => false

  (hash-id "1")
  => (hash-id "1"))

^{:refer std.lib.foundation/hash-code :added "3.0"}
(fact "returns the hash code of an object"
  (hash-code "hello") => number?)

^{:refer std.lib.foundation/aget :added "3.0"}
(fact "typesafe aget"

  (aget (int-array [1 2 3]) 2)
  => 3)

^{:refer std.lib.foundation/demunge :added "3.0"}
(fact "demunges an input to be nicer looking"

  (demunge ;;; (munge "+test!")
   "_PLUS_test_BANG_")
  => "+test!")

^{:refer std.lib.foundation/intern-var :added "3.0"}
(fact "interns a var in sym"

  (intern-var (symbol "std.lib.foundation-test/hello") "world")
  (resolve 'hello) => #'hello)

^{:refer std.lib.foundation/intern-form :added "3.0"}
(fact "creates base form for `intern-in` and `intern-all`"
  ^:hidden
  
  (intern-form 'std.lib 'std.lib.foundation/unfold)
  => '(std.lib.foundation/intern-var
       'std.lib
       'unfold
       #'std.lib.foundation/unfold
       'nil))

^{:refer std.lib.foundation/intern-in :added "3.0"}
(fact "adds a function to current"
  ^:hidden
  
  (intern-in std.lib.foundation/T)
  => #'T)

^{:refer std.lib.foundation/intern-all :added "3.0"}
(fact "adds a namespace to current"
  ^:hidden
  
  (intern-all std.lib.foundation)
  => vector?)

^{:refer std.lib.foundation/with:template-meta :added "3.0"}
(fact "binds the template meta (for testing purposes)"
  ^:hidden
  
  (with:template-meta {:a 1} (template-meta))
  => {:a 1})

^{:refer std.lib.foundation/template-meta :added "3.0"}
(fact "returns the intern template meta"
  ^:hidden
  
  (template-meta)
  => nil?)

^{:refer std.lib.foundation/template-vars :added "3.0"
  :style/indent 1}
(fact "adds a function to current given var and arguments"
  ^:hidden

  (defn tmpl-fn
    ([sym var opts]
     `(def ~sym [~(str (var-sym var)) ~opts ~(template-meta)])))

  (-> (template-vars [tmpl-fn {:meta "meta"}]
        (add-sym + {:opts [1 2 3]}))
      first
      deref)
  => ["clojure.core/+" {:opts [1 2 3]} {:meta "meta"}])

^{:refer std.lib.foundation/template-entries :added "3.0"
  :style/indent 1}
(fact "uses a template function to produce entries"
  ^:hidden

  (defn tmpl-entry
    ([{:keys [sym var opts]}]
     `(def ~sym [~(str (var-sym var)) ~opts ~(dissoc (template-meta)
                                                     :line
                                                     :column)])))

  (def +entries+
    [{:sym 'sub :var #'- :opts [4 5 6]}])

  (-> (template-entries [tmpl-entry {:meta "meta"}]
                        +entries+)
      first
      deref)
  => ["clojure.core/-" [4 5 6] {:meta "meta"}])

^{:refer std.lib.foundation/template-bulk :added "4.0"}
(fact "template-entries but for heavy usage"
  (template-bulk [tmpl-fn {:meta "meta"}]
                 [[+ {:opts [1 2 3]}]])
  => vector?)

^{:refer std.lib.foundation/template-ensure :added "4.0"}
(fact "ensures that the templated entries are the same as the input"
  (template-ensure {} [])
  => nil)

^{:refer std.lib.foundation/wrapped :added "4.0"}
(fact "object to display shell result"
  ^:hidden
  
  (str (wrapped "Hello"))
  "Hello")

^{:refer std.lib.foundation/wrapped? :added "4.0"}
(fact "checks that an object is wrapped"
  ^:hidden
  
  (wrapped? (wrapped "Hello"))
  => true)

^{:refer std.lib.foundation/resolve-namespaced :added "4.0"}
(fact "resolves a namespaced symbol"
  ^:hidden
  
  (resolve-namespaced 'clojure.core/+)
  => 'clojure.core/+)

^{:refer std.lib.foundation/def.m :added "4.0"}
(fact "defs multiple vars"
  ^:hidden
  
  (def.m [-a- -b-] [1 2])
  [-a- -b-] => [1 2])
