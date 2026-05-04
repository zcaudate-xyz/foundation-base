(ns hara.model.spec-lua.rewrite-test
  (:require [hara.lang :as l]
            [hara.lang.base.script :as script]
            [hara.model.spec-lua :as lua]
            [hara.model.spec-lua.rewrite :as rewrite])
  (:use code.test))

(script/script- :lua)

(fact "preserves multi-value returns during lua rewrite"
  (rewrite/lua-rewrite-stage
   '(return nil "OK")
   {:grammar lua/+grammar+})
  => '(return nil "OK")

  (rewrite/lua-rewrite-stage
   '(fn [cb]
      (return nil "OK"))
   {:grammar lua/+grammar+})
  => '(fn [cb]
        (return nil "OK"))

  (let [out (l/emit-as :lua '[(fn [cb] (return nil "OK"))])]
    (boolean (re-find #"return nil,\s*'OK'" out)))
  => true)

^{:refer hara.model.spec-lua.rewrite/lua-rewrite-form :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-lua.rewrite/lua-rewrite-statement :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-lua.rewrite/lua-rewrite-statements :added "4.1"}
(fact "TODO")

^{:refer hara.model.spec-lua.rewrite/lua-rewrite-stage :added "4.1"}
(fact "hoists named inline functions for lua"
  (rewrite/lua-rewrite-stage
   '(var out
         (hello
          (fn inc-fn [x]
            (return x))))
   {:grammar lua/+grammar+})
  => '(do*
        (var inc-fn
             (fn [x]
               (return x)))
        (var out
             (hello inc-fn)))

  (rewrite/lua-rewrite-stage
   '(var out
         (hello
          (fn [x]
            (return x))))
   {:grammar lua/+grammar+})
  => '(var out
            (hello
             (fn [x]
               (return x))))

  (rewrite/lua-rewrite-stage
   '(fn inc-fn [x]
      (return x))
   {:grammar lua/+grammar+})
  => '(fn inc-fn [x]
        (return x))

  (let [out (l/emit-as :lua '[(hello (fn inc-fn [x] (return x)))])]
    [(boolean (re-find #"hello\(inc_fn\)" out))
     (nil? (re-find #"hello\(function \(x\)" out))])
  => [true true])

^{:refer hara.model.spec-lua.rewrite/lua-rewrite-stage :added "4.1"}
(fact "marks runtime-eval helper defs as inner for lua without affecting normal staging"
  (let [plain (rewrite/lua-rewrite-stage
               '(do
                  (defn helper [] (return 1))
                  (helper))
               {:grammar lua/+grammar+})
        evald (rewrite/lua-rewrite-stage
               '(do
                  (defn helper [] (return 1))
                  (helper))
               {:grammar lua/+grammar+
                :mopts {:emit {:body {:transform identity}}}})]
    [(boolean (-> plain second second meta :inner))
     (boolean (-> evald second second meta :inner))])
  => [false true])
