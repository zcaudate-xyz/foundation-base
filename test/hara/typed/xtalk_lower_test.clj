(ns hara.typed.xtalk-lower-test
  (:use code.test)
  (:require [hara.typed.xtalk-common :as types]
            [hara.typed.xtalk-infer :as infer]
            [hara.typed.xtalk-lower :refer :all]))

(def +ctx+ {:ns 'sample.route :aliases '{k xt.lang.common-lib}})

^{:refer hara.typed.xtalk-lower/intrinsic-sym :added "4.1"}
(fact "builds intrinsic symbols"
  (intrinsic-sym "arrayify")
  => 'hara.typed.xtalk-intrinsic/arrayify)

^{:refer hara.typed.xtalk-lower/resolve-op :added "4.1"}
(fact "resolves aliases and local symbols"
  [(resolve-op 'k/get-key +ctx+)
   (resolve-op '-/route +ctx+)]
  => '[xt.lang.common-lib/get-key sample.route/route])

^{:refer hara.typed.xtalk-lower/access-kind :added "4.1"}
(fact "classifies object and array receiver types"
  [(access-kind {:kind :record :fields []} {})
   (access-kind {:kind :dict :key types/+str-type+ :value types/+unknown-type+} {})
   (access-kind {:kind :array :item types/+unknown-type+} {})
   (access-kind {:kind :tuple :types [types/+str-type+]} {})]
  => [:key :key :idx :idx])

^{:refer hara.typed.xtalk-lower/lower-dot :id lower-dot-annotated :added "4.1"}
(fact "classifies annotated receivers without a full inference context"
  (lower-dot (list '.
                   (with-meta 'callbacks
                     {:- [:xt/dict :xt/str :xt/any]})
                   '[key])
             {:preserve-unknown true})
  => '(x:get-key callbacks key))

^{:refer hara.typed.xtalk-lower/lower-dot :added "4.1"}
(fact "lowers dot access to typed key, index, and path helpers"
  [(lower-dot '(. route ["tree"]))
   (lower-dot '(. arr [i]) {:infer infer/infer-type
                            :env '{arr {:kind :array
                                        :item {:kind :primitive :name :xt/int}}}})
   (lower-dot '(. route "tree" "leaf"))]
  => '[(x:get-key route "tree")
       (x:get-idx arr i)
       (x:get-path route ["tree" "leaf"] nil)])

^{:refer hara.typed.xtalk-lower/lower-dot :id lower-dot-method-call :added "4.1"}
(fact "preserves list-shaped native method calls"
  (lower-dot '(. arr (filter pred)))
  => '(. arr (filter pred)))

^{:refer hara.typed.xtalk-lower/lower-fn-shorthand :added "4.1"}
(fact "lowers fn:> shorthands"
  [(lower-fn-shorthand '(fn:>))
   (lower-fn-shorthand '(fn:> [x] x))
   (lower-fn-shorthand '(fn:> "ok"))]
  => '[(hara.typed.xtalk-intrinsic/const-fn nil)
       (fn [x] x)
       (hara.typed.xtalk-intrinsic/const-fn "ok")])

^{:refer hara.typed.xtalk-lower/lower-defaulted-target :added "4.1"}
(fact "applies defaulted targets"
  (lower-defaulted-target 'x:get-key '[obj "k" "fallback"])
  => '(x:get-key obj "k" "fallback"))

^{:refer hara.typed.xtalk-lower/lower-offset-index :added "4.1"}
(fact "builds offset index lookups"
  [(lower-offset-index '[items] 0)
   (lower-offset-index '[items] 2)]
  => '[(x:get-idx items (x:offset))
       (x:get-idx items (x:offset 2))])

^{:refer hara.typed.xtalk-lower/lower-list :added "4.1"}
(fact "lowers wrapper calls to canonical forms"
  [(lower-list '(k/get-key route "tree") +ctx+)
   (lower-list '(x:get-key route "leaf") +ctx+)
   (lower-list '(k/first items) +ctx+)
   (lower-list '(x:second items) +ctx+)
   (lower-list '(k/not-empty? items) +ctx+)]
  => '[(x:get-key route "tree" nil)
       (x:get-key route "leaf" nil)
       (x:get-idx items (x:offset))
       (x:get-idx items (x:offset 1))
       (xt.lang.common-lib/not-empty? items)])

^{:refer hara.typed.xtalk-lower/lower-list :id preserve-runtime-helpers :added "4.1"}
(fact "preserves runtime helpers for target resolution"
  [(lower-list '(xt.lang.common-lib/arrayify items) +ctx+)
   (lower-list '(xt.event.base-listener/make-container initializer "route" {}) +ctx+)
   (lower-list '(xt.event.base-listener/blank-container "route" {}) +ctx+)]
  => '[(xt.lang.common-lib/arrayify items)
       (xt.event.base-listener/make-container initializer "route" {})
       (xt.event.base-listener/blank-container "route" {})])

^{:refer hara.typed.xtalk-lower/lower-form :id preserve-structural-forms :added "4.1"}
(fact "does not rewrite structural forms as XTalk operations"
  (lower-form '(defn sample [x] (return x)) {:preserve-unknown true})
  => '(defn sample [x] (return x)))

^{:refer hara.typed.xtalk-lower/lower-list :id preserve-value-conditionals :added "4.1"}
(fact "leaves value conditionals for target-language staging"
  (lower-list '(:? test 1 2) {:preserve-unknown true})
  => '(:? test 1 2))

^{:refer hara.typed.xtalk-lower/lower-form :added "4.1"}
(fact "recursively lowers nested forms"
  (lower-form '{:route (x:get-key data "tree")
                :paths [(x:second items)]}
              +ctx+)
  => '{:route (x:get-key data "tree" nil)
       :paths [(x:get-idx items (x:offset 1))]})
