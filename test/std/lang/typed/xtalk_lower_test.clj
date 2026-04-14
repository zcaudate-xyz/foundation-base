(ns std.lang.typed.xtalk-lower-test
  (:use code.test)
  (:require [std.lang.typed.xtalk-lower :refer :all]))

(def +ctx+ {:ns 'sample.route :aliases '{k xt.lang.common-lib}})

^{:refer std.lang.typed.xtalk-lower/intrinsic-sym :added "4.1"}
(fact "builds intrinsic symbols"
  (intrinsic-sym "arrayify")
  => 'std.lang.typed.xtalk-intrinsic/arrayify)

^{:refer std.lang.typed.xtalk-lower/resolve-op :added "4.1"}
(fact "resolves aliases and local symbols"
  [(resolve-op 'k/get-key +ctx+)
   (resolve-op '-/route +ctx+)]
  => '[xt.lang.common-lib/get-key sample.route/route])

^{:refer std.lang.typed.xtalk-lower/lower-dot :added "4.1"}
(fact "lowers dot access to key and path helpers"
  [(lower-dot '(. route ["tree"]))
   (lower-dot '(. route "tree" "leaf"))]
  => '[(x:get-key route "tree")
       (x:get-path route ["tree" "leaf"] nil)])

^{:refer std.lang.typed.xtalk-lower/lower-fn-shorthand :added "4.1"}
(fact "lowers fn:> shorthands"
  [(lower-fn-shorthand '(fn:>))
   (lower-fn-shorthand '(fn:> [x] x))
   (lower-fn-shorthand '(fn:> "ok"))]
  => '[(std.lang.typed.xtalk-intrinsic/const-fn nil)
       (fn [x] x)
       (std.lang.typed.xtalk-intrinsic/const-fn "ok")])

^{:refer std.lang.typed.xtalk-lower/lower-defaulted-target :added "4.1"}
(fact "applies defaulted targets"
  (lower-defaulted-target 'x:get-key '[obj "k" "fallback"])
  => '(x:get-key obj "k" "fallback"))

^{:refer std.lang.typed.xtalk-lower/lower-offset-index :added "4.1"}
(fact "builds offset index lookups"
  [(lower-offset-index '[items] 0)
   (lower-offset-index '[items] 2)]
  => '[(x:get-idx items (x:offset))
       (x:get-idx items (x:offset 2))])

^{:refer std.lang.typed.xtalk-lower/lower-list :added "4.1"}
(fact "lowers wrapper calls to canonical forms"
  [(lower-list '(k/get-key route "tree") +ctx+)
   (lower-list '(k/first items) +ctx+)
   (lower-list '(k/not-empty? items) +ctx+)]
  => '[(x:get-key route "tree" nil)
       (x:get-idx items (x:offset))
       (std.lang.typed.xtalk-intrinsic/not-empty? items)])

^{:refer std.lang.typed.xtalk-lower/lower-form :added "4.1"}
(fact "recursively lowers nested forms"
  (lower-form '{:route (k/get-key data "tree")
                :paths [(k/second items)]}
              +ctx+)
  => '{:route (x:get-key data "tree" nil)
       :paths [(x:get-idx items (x:offset 1))]})
