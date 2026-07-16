(ns hara.model.spec-dart.rewrite-test
  (:require [hara.lang :as l]
             [hara.model.spec-dart :as dart]
             [hara.model.spec-dart.rewrite :as rewrite]
             [xt.lang.common-data]
             [std.string :as str])
  (:use code.test))

(fact "rewrites value-default ors into dart:or"
  (rewrite/dart-rewrite-stage
   '(var a (or a b c))
   {:grammar dart/+grammar+})
  => '(var a (dart:or a b c))

  (rewrite/dart-rewrite-stage
   '(:= a (or b {}))
   {:grammar dart/+grammar+})
  => '(:= a (dart:or b {})))

(fact "keeps boolean ors as boolean ors"
  (rewrite/dart-rewrite-stage
   '(if (or (== kind "int")
            (== kind "double"))
      (return kind)
      (return nil))
   {:grammar dart/+grammar+})
  => '(if (or (== kind "int")
              (== kind "double"))
        (return kind)
        (return nil)))

(fact "rewrites non-bool ternaries into dart:ternary"
  (rewrite/dart-rewrite-stage
   '(return (:? a out {}))
   {:grammar dart/+grammar+})
  => '(return (dart:ternary a out {}))

  (rewrite/dart-rewrite-stage
   '(var endpoint (:? path "/" path))
   {:grammar dart/+grammar+})
  => '(var endpoint (dart:ternary path "/" path)))

(fact "rewrites unpack invokes for Dart"
  (rewrite/dart-rewrite-stage
   '(return (f (x:unpack xs) y))
   {:grammar dart/+grammar+})
  => '(return (Function.apply f [(:.. xs) y])))

(fact "keeps boolean ternaries intact"
  (rewrite/dart-rewrite-stage
   '(return (:? (x:not-nil? a) out {}))
   {:grammar dart/+grammar+})
  => '(return (:? (x:not-nil? a) out {})))

(fact "rewrites cond tests and keeps branch bodies as statements"
  (rewrite/dart-rewrite-stage
   '(cond curr
      (return 1)
      :else
      (return 2))
   {:grammar dart/+grammar+})
  => '(cond (and (x:not-nil? curr)
                 (not= false curr))
       (return 1)
       :else
       (return 2))

  (rewrite/dart-rewrite-stage
   '(defgen sample [n seq]
      (var i 0)
      (for:iter [e seq]
        (if (< i n)
          (do (:= i (+ i 1))
              (yield e))
          (return))))
   {:grammar dart/+grammar+})
  => '(defgen sample [n seq]
        (do
          (var i 0)
          (for:iter [e seq]
            (if (< i n)
              (do
                (:= i (+ i 1))
                (yield e))
              (return))))))

(fact "rewrites explicit boolean control contexts"
  (rewrite/dart-rewrite-stage
   '(if curr
      (return 1)
      (return 2))
   {:grammar dart/+grammar+})
  => '(if (and (x:not-nil? curr)
               (not= false curr))
        (return 1)
        (return 2))

  (rewrite/dart-rewrite-stage
   '(when curr
      (return 1))
   {:grammar dart/+grammar+})
  => '(when (and (x:not-nil? curr)
                 (not= false curr))
        (return 1))

  (rewrite/dart-rewrite-stage
   '(while curr
      (return 1))
   {:grammar dart/+grammar+})
  => '(while (and (x:not-nil? curr)
                  (not= false curr))
        (return 1))

  (rewrite/dart-rewrite-stage
   '(br* (if curr (return 1))
         (elseif ready (return 2))
         (else (return 3)))
   {:grammar dart/+grammar+})
  => '(br* (if (and (x:not-nil? curr)
                    (not= false curr))
             (return 1))
          (elseif (and (x:not-nil? ready)
                       (not= false ready))
             (return 2))
          (else (return 3))))

(fact "adds dart-specific nil-coalesce ops"
  (dart/dart-tf-ternary '(dart:ternary a out {}))
  => '(:? (and (x:not-nil? a)
               (not= false a))
           out
           {})

  (l/emit-as :dart ['(var a (dart:or b {}))])
  => "var a = b ?? <dynamic, dynamic>{}"

  (let [out (l/emit-as :dart ['(return (dart:ternary a out {}))])]
    [(str/includes? out "null != a")
     (str/includes? out "false != a")
     (str/includes? out "runtimeType")])
  => [true true false])

(fact "emits rewritten Dart defaults without iifes"
  (let [or-out      (l/emit-as :dart ['(var a (or a b c))])
         ternary-out (l/emit-as :dart ['(return (:? a out {}))])]
    [(str/includes? or-out "??")
     (str/includes? or-out "runtimeType")
     (str/includes? ternary-out "null != a")
     (str/includes? ternary-out "false != a")
     (str/includes? ternary-out "runtimeType")])
  => [true false true true false])

(fact "emits cond, do expressions and loop bodies safely for Dart"
  (let [cond-out (l/emit-as :dart ['(cond curr
                                      (return 1)
                                      :else
                                      (return 2))])
        do-out   (l/emit-as :dart ['(do (print 1) 2)])
        ret-out  (l/emit-as :dart ['(return (do (print 1) 2))])
        gen-out  (l/emit-as :dart ['(defgen sample [n seq]
                                     (var i 0)
                                     (for:iter [e seq]
                                       (if (< i n)
                                         (do (:= i (+ i 1))
                                             (yield e))
                                         (return))))])
        loop-out (l/emit-as :dart ['(defn sample [m path]
                                     (var out [])
                                     (for:object [[k v] m]
                                       (var npath [(x:unpack path)])
                                       (x:arr-push npath k)
                                       (cond (x:is-object? v)
                                             (do (for:array [e (xt.lang.common-data/obj-keys-nested v npath)]
                                                   (x:arr-push out e)))
                                             :else
                                             (x:arr-push out [npath v])))
                                     (return out))])]
    [(str/includes? cond-out "null != curr")
     (str/includes? cond-out "false != curr")
     (str/includes? do-out "return 2;")
     (str/includes? ret-out "return 2;")
     (str/includes? gen-out "yield e;")
     (not (str/includes? gen-out "(()"))
     (not (str/includes? loop-out "return var"))])
  => [true true true true true true true])


(fact "emits XT vector literals as dynamic Dart lists"
  (let [out (l/emit-as :dart ['(defn sample []
                                  (var out [{"::" "sql/count"}])
                                  (x:arr-push out "id")
                                  (return out))])]
    [(str/includes? out "var out = <dynamic>[")
     (str/includes? out "out.add(\"id\")")])
  => [true true])

^{:refer hara.model.spec-dart.rewrite/dart-rewrite-conditional-expression :added "4.1"}
(fact "rewrites dart conditional expressions")

^{:refer hara.model.spec-dart.rewrite/dart-rewrite-expression :added "4.1"}
(fact "rewrites dart expressions")

^{:refer hara.model.spec-dart.rewrite/dart-rewrite-statement :added "4.1"}
(fact "rewrites dart statements")

^{:refer hara.model.spec-dart.rewrite/dart-rewrite-statements :added "4.1"}
(fact "rewrites dart statement blocks")

^{:refer hara.model.spec-dart.rewrite/dart-rewrite-stage :added "4.1"}
(fact "rewrites dart stages")
