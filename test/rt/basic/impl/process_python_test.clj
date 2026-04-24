(ns rt.basic.impl.process-python-test
  (:require [rt.basic.impl.process-python :refer :all]
            [std.concurrent :as cc]
            [std.lang :as l])
  (:use code.test))

(l/script- :python
  {:runtime :oneshot})

(l/script+ [:py.0 :python]
  {:runtime :basic})

(fact:global
 {:setup    [(l/annex:start-all)]
  :teardown [(l/annex:stop-all)]})

^{:refer rt.basic.impl.process-python/CANARY :adopt true :added "4.0"}
(fact "EVALUATE python code"

  (!.py (+ 1 2 3 4))
  => 10

  (l/! [:py.0]
    (+ 1 2 3 4))
  => 10)

^{:refer rt.basic.impl.process-python/default-oneshot-wrap :adopt true :added "4.0"}
(fact "creates the ws client connect code"

  (default-oneshot-wrap 1)
  => string?)

^{:refer rt.basic.impl.process-python/default-body-wrap :added "4.0"}
(fact "creates the scaffolding for the runtime eval to work"

  (default-body-wrap ['(+ 1 2 3)])
  => '(do (defn OUT-FN
            []
            (:- :import traceback)
            (var err)
            (try
              (return (+ 1 2 3))
              (catch Exception (:= err (. traceback (format-exc)))))
            (throw (Exception err)))
          (:= (. (globals) ["OUT"]) (OUT-FN))))

^{:refer rt.basic.impl.process-python/default-body-transform :added "4.0"}
(fact "standard python transforms"

  (default-body-transform '[1 2 3] {})
  => '(do (defn OUT-FN
            []
            (:- :import traceback)
            (var err)
            (try
              (return [1 2 3])
              (catch Exception (:= err (. traceback (format-exc)))))
            (throw (Exception err)))
          (:= (. (globals) ["OUT"]) (OUT-FN)))

  (default-body-transform '[1 2 3] {:bulk true})
  => '(do (defn OUT-FN
            []
            (:- :import traceback)
            (var err)
             (try
               1
               2
               (return 3)
               (catch Exception (:= err (. traceback (format-exc)))))
             (throw (Exception err)))
          (:= (. (globals) ["OUT"]) (OUT-FN)))

  (let [out (l/emit-as
             :python
             [(default-body-transform
               '[(var iter-fn
                      (fn []
                        (return (x:iter-null))))
                 (x:iter-native? (iter-fn))]
               {:bulk true})])]
    [(boolean (re-find #"if\s+False:\s+yield" out))
     (boolean (re-find #"hasattr\s*\(iter_fn\(\),\"__next__\"\)" out))])
  => [true true])
