(ns std.lib.invoke-test
  (:use code.test)
  (:require [std.lib.invoke :refer :all]
            [std.protocol.invoke :as protocol.invoke])
  (:import (clojure.lang MultiFn))
  (:refer-clojure :exclude [fn]))

^{:refer std.lib.invoke/multi? :added "3.0"}
(fact "returns `true` if `obj` is a multimethod"

  (multi? print-method) => true

  (multi? println) => false)

^{:refer std.lib.invoke/multi:clone :added "3.0"}
(fact "creates a multimethod from an existing one" ^:hidden

  (defmulti hello :type)

  (defmethod hello :a
    [m] (assoc m :a 1))

  (def world (multi:clone hello "world"))

  (defmethod world :b
    [m] (assoc m :b 2))

  (world {:type :b})
  => {:type :b :b 2}

  ;; original method should not be changed
  (hello {:type :b})
  => (throws))

^{:refer std.lib.invoke/multi:match? :added "3.0"}
(fact "checks if the multi dispatch matches the arguments"

  (multi:match? (.dispatchFn ^MultiFn hello) (clojure.core/fn [_]))
  => true)

^{:refer std.lib.invoke/multi:get :added "3.0"}
(fact "returns all entries in the multimethod"

  (multi:get hello :a)
  => fn?)

^{:refer std.lib.invoke/multi:add :added "3.0"}
(fact "adds an entry to the multimethod"

  (multi:add hello :c (clojure.core/fn [m] (assoc m :c 3)))
  => hello)

^{:refer std.lib.invoke/multi:list :added "3.0"}
(fact "returns all entries in the multimethod"

  (keys (multi:list world))
  => (contains [:a :b]
               :in-any-order))

^{:refer std.lib.invoke/multi:remove :added "3.0"}
(fact "removes an entry"

  (multi:remove world :b)
  => ifn?)

^{:refer std.lib.invoke/invoke:arglists :added "3.0"}
(fact "returns the arglists of a form"

  (invoke:arglists '([x] x))
  => '(quote ([x]))

  (invoke:arglists '(([x] x) ([x y] (+ x y))))
  => '(quote ([x] [x y])))

^{:refer std.lib.invoke/invoke-intern-method :added "3.0"}
(fact "creates a `:method` form, similar to `defmethod`"

  (defmulti -hello-multi- identity)
  (invoke-intern-method '-hello-method-
                        {:multi '-hello-multi-
                         :val :apple}
                        '([x] x))
  => '(clojure.core/let
          [v (def -hello-method- (clojure.core/fn -hello-method- [x] x))]
        [(std.lib.invoke/multi:add -hello-multi- :apple -hello-method-) v]))

^{:refer std.lib.invoke/resolve-method :added "3.0"}
(fact "resolves a package related to a label"

  (resolve-method protocol.invoke/-invoke-intern
                  protocol.invoke/-invoke-package
                  :fn
                  +default-packages+)
  => nil ^:hidden

  (resolve-method protocol.invoke/-invoke-intern
                  protocol.invoke/-invoke-package
                  :error
                  +default-packages+)
  => (throws))

^{:refer std.lib.invoke/invoke-intern :added "3.0"}
(fact "main function to call for `definvoke`"

  (invoke-intern :method
                 '-hello-method-
                 {:multi '-hello-multi-
                  :val :apple}
                 '([x] x))
  => '(clojure.core/let [v (def -hello-method- (clojure.core/fn -hello-method- [x] x))]
        [(std.lib.invoke/multi:add -hello-multi- :apple -hello-method-) v]))

^{:refer std.lib.invoke/definvoke :added "3.0"}
(fact "customisable invocation forms"

  (definvoke -another-
    [:compose {:val (partial + 10)
               :arglists '([& more])}])
  => any?)

^{:refer std.lib.invoke/invoke-intern-fn :added "3.0"}
(fact "method body for `:fn` invoke"

  (invoke-intern-fn :fn '-fn-form- {} '([x] x)) ^:hidden
  => '(def -fn-form- (clojure.core/fn -fn-form- [x] x))

  (definvoke -add10-
    [:fn]
    ([& args]
     (apply + 10 args)))

  (-add10- 1 2 3)
  => 16)

^{:refer std.lib.invoke/invoke-intern-dynamic :added "3.0"}
(fact "constructs a body for the :dynamic keyword"

  (invoke-intern-dynamic nil '-hello- {:val :world} nil) ^:hidden
  => '(do (def *-hello-* :world)
          (clojure.core/defn -hello- ([] *-hello-*)
            ([v]
             (clojure.core/alter-var-root (var *-hello-*)
                                          (clojure.core/fn [_] v))))))

^{:refer std.lib.invoke/invoke-intern-multi :added "3.0"}
(fact "method body for `:multi` form"

  (invoke-intern-multi :multi '-multi-form- {} '([x] x)) ^:hidden
  => '(do ((clojure.core/deref (var clojure.core/check-valid-options)) {} :default :hierarchy)
          (clojure.core/let [v (def -multi-form-)]
            (if (clojure.core/or
                 (clojure.core/not
                  (clojure.core/and (.hasRoot v)
                                    (clojure.core/instance? clojure.lang.MultiFn (clojure.core/deref v)))) nil)
              (clojure.core/doto (def -multi-form- (new clojure.lang.MultiFn
                                                        "-multi-form-"
                                                        (clojure.core/fn -multi-form- [x] x)
                                                        :default (var clojure.core/global-hierarchy)))
                (clojure.core/alter-meta! clojure.core/merge {} {:arglists (quote ([x]))})))))

  (definvoke lookup-label
    [:multi {:refresh true}]
    [x] x)

  (definvoke lookup-label-ref
    [:method {:multi lookup-label
              :val :ref}]
    ([_] clojure.lang.Ref))

  (definvoke lookup-label

    [:method {:val :atom}]
    ([_] clojure.lang.Atom)))

^{:refer std.lib.invoke/invoke-intern-compose :added "3.0"}
(fact "method body for `:compose` form"

  (invoke-intern-compose :compose
                         '-compose-form-
                         {:val '(partial + 1 2)
                          :arglists ''([& more])} nil) ^:hidden
  => '(def -compose-form- (partial + 1 2))

  (definvoke ->symbol
    [:compose {:arglists '([k])
               :val (comp symbol name)}])

  (:arglists (meta #'->symbol))
  => '([k])

  (->symbol :hello)
  => 'hello)

^{:refer std.lib.invoke/invoke-intern-macro :added "3.0"}
(fact "method body for `:macro` form"

  (defn -macro-fn- [] '[(clojure.core/fn [x] x)
                        {:arglists ([x])}])

  (invoke-intern-macro :macro
                       '-macro-form-
                       {:fn '-macro-fn-
                        :args []}
                       nil) ^:hidden
  => '(def -macro-form- (clojure.core/fn [x] x)))

^{:refer std.lib.invoke/recent-fn :added "3.0"}
(fact "creates a recent function"
  ^:hidden

  ((recent-fn {:op #(* 2 %)
               :key str
               :compare identity
               :cache (atom {})})
   1)
  => 2)

^{:refer std.lib.invoke/invoke-intern-recent :added "3.0"}
(fact "creates a body for `recent`"
  ^:hidden

  (invoke-intern-recent :recent
                        'hello
                        {}
                        '([obj] (* 2 obj)))
  => '(do (def +hello (clojure.core/atom {}))
          (clojure.core/defn hello-raw
            "helper function for std.lib.invoke-test/hello"
            [obj] (* 2 obj))
          (def hello
            (std.lib.invoke/recent-fn
             {:key clojure.core/identity,
              :op hello-raw,
              :cache +hello,
              :compare clojure.core/identity}))))

^{:refer std.lib.invoke/fn-body :added "3.0"}
(fact "creates a function body"

  (fn-body :clojure '([] (+ 1 1)))
  => '(clojure.core/fn [] (+ 1 1)))

^{:refer std.lib.invoke/fn-body-clojure :added "3.0"}
(fact "creates a clojure function body"

  (fn-body-clojure nil '([] (+ 1 1)))
  => '(clojure.core/fn [] (+ 1 1)))

^{:refer std.lib.invoke/fn :added "3.0"}
(fact "macro body for extensible function"

  (fn [x] x)
  => fn?)
