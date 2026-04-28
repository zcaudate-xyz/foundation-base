(ns std.lang.model.spec-xtalk.promise-support-test
  (:use code.test)
  (:require [std.lang :as l]
            [xt.lang.spec-base :as xt]))

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
