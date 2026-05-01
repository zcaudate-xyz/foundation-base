(ns std.lang.model.spec-xtalk.promise-support-test
  (:use code.test)
  (:require [std.lang :as l]
            [xt.lang.spec-base :as xt]
            [xt.lang.spec-promise :as spec-promise]))

(l/script- :python
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as spec-promise]
             [xt.lang.common-promise]]})

(l/script- :lua
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.spec-promise :as spec-promise]
             [xt.lang.common-promise]]})

^{:refer xt.lang.spec-promise/x:promise :added "4.1"}
(fact "promise helpers are exposed from xt.lang.spec-promise"
  [(boolean (find-var 'xt.lang.spec-promise/x:promise))
   (boolean (find-var 'xt.lang.spec-promise/x:promise-then))
   (boolean (find-var 'xt.lang.spec-promise/x:promise-catch))
   (boolean (find-var 'xt.lang.spec-promise/x:promise-finally))
   (boolean (find-var 'xt.lang.spec-promise/x:promise-native?))]
  => [true true true true true])

^{:refer xt.lang.spec-base/x:ex :added "4.1"}
(fact "exception helpers are exposed from xt.lang.spec-base"
  [(boolean (find-var 'xt.lang.spec-base/x:ex-native?))
   (boolean (find-var 'xt.lang.spec-base/x:ex))
   (boolean (find-var 'xt.lang.spec-base/x:ex-message))
   (boolean (find-var 'xt.lang.spec-base/x:ex-data))]
  => [true true true true])

^{:refer xt.lang.spec-base/x:ex :added "4.1"}
(fact "xtalk exception helpers emit catchable structured exceptions"
  (let [js-out (l/emit-as :js ['(try
                                 (throw (x:ex "boom" {:a 1}))
                                 (catch e
                                   (x:print (x:ex-message e))
                                   (x:print (x:ex-data e))))])
        py-out (l/emit-as :python ['(try
                                     (throw (x:ex "boom" {:a 1}))
                                     (catch [Exception :as e]
                                       (x:print (x:ex-message e))
                                       (x:print (x:ex-data e))))])
        lua-out (l/emit-as :lua ['(try
                                   (throw (x:ex "boom" {:a 1}))
                                   (catch e
                                     (x:print (x:ex-message e))
                                     (x:print (x:ex-data e))))])]
    [[(boolean (re-find #"Object\.assign\(new Error\(\"boom\"\)" js-out))
      (boolean (re-find #"\[\"message\"\]" js-out))
      (boolean (re-find #"\[\"data\"\]" js-out))
      (boolean (re-find #"console\.log" js-out))]
     [(boolean (re-find #"Exception\(\"boom\",\{\"a\":1\}\)" py-out))
      (boolean (re-find #"e\.args\[0\]" py-out))
      (boolean (re-find #"e\.args\[1\]" py-out))
      (boolean (re-find #"print" py-out))]
     [(boolean (re-find #"xt\.exception" lua-out))
      (boolean (re-find #"\['message'\]" lua-out))
      (boolean (re-find #"\['data'\]" lua-out))
      (boolean (re-find #"print" lua-out))]])
  => [[true true true true]
      [true true true true]
      [true true true true]])

^{:refer xt.lang.spec-base/x:ex-data :added "4.1"}
(fact "xtalk exception helpers compose with promise catches"
  (let [js-out (l/emit-as :js ['(x:promise-catch
                                  (x:promise (fn []
                                               (throw (x:ex "boom" {:a 1}))))
                                  (fn [err]
                                    (x:print (x:ex-message err))
                                    (x:print (x:ex-data err))))])
        py-out (l/emit-as :python ['(x:promise-catch
                                      (x:promise (fn []
                                                   (throw (x:ex "boom" {:a 1}))))
                                      (fn [err]
                                        (x:print (x:ex-message err))
                                        (x:print (x:ex-data err))))])
        lua-out (l/emit-as :lua ['(x:promise-catch
                                   (x:promise (fn []
                                                (throw (x:ex "boom" {:a 1}))))
                                   (fn [err]
                                     (x:print (x:ex-message err))
                                     (x:print (x:ex-data err))))])]
    [[(boolean (re-find #"\.catch" js-out))
      (boolean (re-find #"Object\.assign\(new Error\(\"boom\"\)" js-out))
      (boolean (re-find #"\[\"message\"\]" js-out))
      (boolean (re-find #"\[\"data\"\]" js-out))]
     [(boolean (re-find #"promise_catch" py-out))
      (boolean (re-find #"Exception\(\"boom\",\{\"a\":1\}\)" py-out))
      (boolean (re-find #"err\.args\[0\]" py-out))
      (boolean (re-find #"err\.args\[1\]" py-out))]
     [(boolean (re-find #"promise_catch" lua-out))
      (boolean (re-find #"xt\.exception" lua-out))
      (boolean (re-find #"\['message'\]" lua-out))
      (boolean (re-find #"\['data'\]" lua-out))]])
  => [[true true true true]
      [true true true true]
      [true true true true]])

^{:refer xt.lang.spec-base/x:async-run :added "4.1"}
(fact "xtalk async primitives emit host async operations"
  (let [js-out (l/emit-as :js ['(x:async-run thunk)])
        dart-out (l/emit-as :dart ['(x:async-run thunk)])
        py-out (l/emit-as :python ['(x:async-run thunk)])
        lua-out (l/emit-as :lua ['(x:async-run thunk)])]
    [[(boolean (re-find #"Promise\.resolve\(\)" js-out))
      (boolean (re-find #"\.then\(thunk\)" js-out))]
     [(boolean (re-find #"Future\.sync\(thunk\)" dart-out))]
     [(boolean (re-find #"threading" py-out))
      (boolean (re-find #"Thread" py-out))
      (boolean (re-find #"target=thunk|:target thunk" py-out))]
     [(boolean (re-find #"coroutine\.create" lua-out))
      (boolean (re-find #"coroutine\.resume" lua-out))]])
  => [[true true]
      [true]
      [true true true]
      [true true]])

^{:refer std.lang.model.spec-xtalk.fn-js/js-tf-x-promise :added "4.1"}
(fact "js xtalk promise ops emit native promise chains"
  (let [out (l/emit-as :js ['(do (x:promise thunk)
                                 (x:promise-then promise onValue)
                                 (x:promise-catch promise onError)
                                 (x:promise-finally promise onDone)
                                 (x:promise-native? value))])]
    [(boolean (re-find #"Promise\.resolve\(\)" out))
     (boolean (re-find #"\.then\(thunk\)" out))
     (boolean (re-find #"\.then\(onValue\)" out))
     (boolean (re-find #"\.catch\(onError\)" out))
     (boolean (re-find #"\.finally\(onDone\)" out))
     (boolean (re-find #"instanceof Promise" out))])
  => [true true true true true true])

^{:refer std.lang.model.spec-xtalk.fn-dart/dart-tf-x-promise :added "4.1"}
(fact "dart xtalk promise ops emit native future chains"
  (let [out (l/emit-as :dart ['(do (x:promise thunk)
                                   (x:promise-then promise onValue)
                                   (x:promise-catch promise onError)
                                   (x:promise-finally promise onDone)
                                   (x:promise-native? value))])]
    [(boolean (re-find #"Future\.sync\(thunk\)" out))
     (boolean (re-find #"\.then\(onValue\)" out))
     (boolean (re-find #"\.catchError\(onError\)" out))
     (boolean (re-find #"\.whenComplete\(onDone\)" out))
     (boolean (re-find #"runtimeType" out))
     (boolean (re-find #"startsWith\(\"Future<\"\)" out))])
  => [true true true true true true])
