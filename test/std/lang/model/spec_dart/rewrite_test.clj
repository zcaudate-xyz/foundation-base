(ns std.lang.model.spec-dart.rewrite-test
  (:require [std.lang :as l]
            [std.lang.model.spec-dart :as dart]
            [std.lang.model.spec-dart.rewrite :as rewrite]
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

(fact "keeps boolean ternaries intact"
  (rewrite/dart-rewrite-stage
   '(return (:? (x:not-nil? a) out {}))
   {:grammar dart/+grammar+})
  => '(return (:? (x:not-nil? a) out {})))

(fact "adds dart-specific nil-coalesce ops"
  (dart/dart-tf-ternary '(dart:ternary a out {}))
  => '(:? (x:not-nil? a) out {})

  (l/emit-as :dart ['(var a (dart:or b {}))])
  => "var a = b ?? <dynamic, dynamic>{}"

  (l/emit-as :dart ['(return (dart:ternary a out {}))])
  => "return (null != a) ? out : <dynamic, dynamic>{}")

(fact "emits rewritten Dart defaults without iifes"
  (let [or-out      (l/emit-as :dart ['(var a (or a b c))])
        ternary-out (l/emit-as :dart ['(return (:? a out {}))])]
    [(str/includes? or-out "??")
     (str/includes? or-out "runtimeType")
     (str/includes? ternary-out "(null != a) ? out : <dynamic, dynamic>{}")
     (str/includes? ternary-out "runtimeType")])
  => [true false true false])
