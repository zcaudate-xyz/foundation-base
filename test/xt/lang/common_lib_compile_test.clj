(ns xt.lang.common-lib-compile-test
  (:require [std.lang :as l]
            [xt.lang.common-lib])
  (:use code.test))

^{:refer xt.lang.common-lib/type-native :added "4.1"}
(fact "emits lowered native type helpers for linked xt macros"
  (let [js-out  (l/emit-script '(xt.lang.common-lib/type-native "hello") {:lang :js})
        lua-out (l/emit-script '(xt.lang.common-lib/type-native "hello") {:lang :lua})
        py-out  (l/emit-script '(xt.lang.common-lib/type-native "hello") {:lang :python})]
    [(boolean (re-find #"return if" js-out))
     (boolean (re-find #"return local" lua-out))
     (boolean (re-find #"return if" py-out))
     (boolean (re-find #"typeof obj" js-out))
     (boolean (re-find #"local t = type\(obj\)" lua-out))
     (boolean (re-find #"if isinstance\(obj,\(dict\)\):" py-out))])
  => [false false false true true true])

^{:refer xt.lang.common-lib/type-class :added "4.1"}
(fact "emits lowered class type helpers for linked xt macros"
  (let [js-out (l/emit-script '(xt.lang.common-lib/type-class "hello") {:lang :js})]
    [(boolean (re-find #"let ntype = if" js-out))
     (boolean (re-find #"let ntype = null" js-out))
     (boolean (re-find #"x\[\"::\"\]" js-out))])
  => [false true true])
