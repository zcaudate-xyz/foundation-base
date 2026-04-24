(ns std.lang.base.preprocess-assign-test
  (:use code.test)
  (:require [std.lang.base.emit-prep-lua-test :as prep]
            [std.lang.base.preprocess-assign :refer :all]
            [std.lang.model.spec-js :as js]))

^{:refer std.lang.base.preprocess-assign/process-inline-assignment :added "4.1"}
(fact "prepares the form for inline assignment"
  (let [form (process-inline-assignment '(var a := (u/identity-fn 1) :inline)
                                        (:modules prep/+book-min+)
                                        '{:module {:link {u L.core}}}
                                        true)]
    form
    => '(var a := (L.core/identity-fn 1))

    (meta (last form))
    => {:assign/inline true}))

^{:refer std.lang.base.preprocess-assign/protect-reserved-head :added "4.1"}
(fact "protects reserved heads by wrapping them in a volatile"
  (let [out (protect-reserved-head (with-meta '(return value) {:line 10}))]
    [(volatile? (first out))
     @(first out)
     (rest out)
     (meta out)])
  => '[true return (value) {:line 10}])

^{:refer std.lang.base.preprocess-assign/process-template-assignment :added "4.1"}
(fact "rewrites template-only xtalk macros in assignment position"
  (process-template-assignment
   '(var a := (x:type-native obj))
   js/+grammar+
   '{:module {:id JS.core
              :link {- JS.core}}})
  => '(do
        (var* :let a := nil)
        (do
          (when (== obj nil)
            (return nil))
          (var t := (typeof obj))
          (if (== t "object")
            (cond
              (Array.isArray obj)
              (:= a "array")
              :else
              (do
                (var tn := (. obj ["constructor"] ["name"]))
                (if (== tn "Object")
                  (:= a "object")
                  (:= a tn))))
            (:= a t))))

  (process-template-assignment
   '(:= a (x:type-native obj))
   js/+grammar+
   '{:module {:id JS.core
              :link {- JS.core}}})
  => '(do
        (when (== obj nil)
          (return nil))
        (var t := (typeof obj))
        (if (== t "object")
          (cond
            (Array.isArray obj)
            (:= a "array")
            :else
            (do
              (var tn := (. obj ["constructor"] ["name"]))
              (if (== tn "Object")
                (:= a "object")
                (:= a tn))))
          (:= a t)))))
