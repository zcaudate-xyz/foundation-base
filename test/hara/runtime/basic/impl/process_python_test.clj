(ns hara.runtime.basic.impl.process-python-test
  (:require [hara.runtime.basic.impl.process-python :refer :all]
            [std.concurrent :as cc]
            [hara.lang :as l])
  (:use code.test))

(l/script- :python
  {:runtime :oneshot})

(l/script+ [:py.0 :python]
  {:runtime :basic})

(fact:global
 {:setup    [(l/annex:start-all)]
  :teardown [(l/annex:stop-all)]})

^{:refer hara.runtime.basic.impl.process-python/CANARY :adopt true :added "4.0"}
(fact "EVALUATE python code"

  (!.py (+ 1 2 3 4))
  => 10

  (l/! [:py.0]
    (+ 1 2 3 4))
  => 10)

^{:refer hara.runtime.basic.impl.process-python/default-oneshot-wrap :adopt true :added "4.0"}
(fact "creates the ws client connect code"

  (default-oneshot-wrap 1)
  => string?)

^{:refer hara.runtime.basic.impl.process-python/default-basic-client :added "4.1"}
(fact "buffers utf-8 bytes before decoding in the basic client loop"

  (let [out (default-basic-client 19000)]
    [(boolean (re-find #"buf = bytearray\(\)" out))
     (boolean (re-find #"buf\.extend\(ch\)" out))
     (boolean (re-find #"buf\.decode\(\"utf-8\"\)" out))
     (boolean (re-find #"out\.encode\(\"utf-8\"\)" out))])
  => [true true true true])

^{:refer hara.runtime.basic.impl.process-python/default-body-wrap :added "4.0"}
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
           (:= (. (globals) (setitem "OUT")) (OUT-FN))))

(fact "preserves utf-8 output across the basic runtime bridge"

  (l/! [:py.0]
    "Δ")
  => "Δ")

^{:refer hara.runtime.basic.impl.process-python/default-body-transform :added "4.0"}
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
          (:= (. (globals) (setitem "OUT")) (OUT-FN)))

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
          (:= (. (globals) (setitem "OUT")) (OUT-FN)))

  (default-body-transform '(do 1 2 3) {})
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
          (:= (. (globals) (setitem "OUT")) (OUT-FN))))
