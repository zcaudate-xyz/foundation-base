(ns std.lang.base.preprocess-assign-test
  (:use code.test)
  (:require [std.lang.base.emit-prep-lua-test :as prep]
            [std.lang.base.preprocess-assign :refer :all]))

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
