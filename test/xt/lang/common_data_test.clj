(ns xt.lang.common-data-test
 (:require [std.lang :as l])
 (:use code.test))

(l/script- :lua
 {:runtime :basic,
  :require [[xt.lang.common-data :as xtd]
            [xt.lang.common-spec :as xt]]})

(fact:global
 {:setup [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.common-data/not-empty? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/lu-create :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/lu-del :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/lu-get :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/lu-set :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/lu-eq :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/first :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/second :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/nth :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/last :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/second-last :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-empty? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-not-empty? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arrayify :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-lookup :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-omit :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-reverse :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-zip :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-clone :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-append :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-slice :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-rslice :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-tail :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-range :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-intersection :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-difference :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-union :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-shuffle :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-pushl :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-pushr :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-interpose :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-random :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-sample :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/obj-empty? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/obj-not-empty? :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/obj-first-key :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/obj-first-val :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/obj-assign-nested :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/obj-assign-with :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/obj-del :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/obj-del-all :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/obj-pick :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/obj-omit :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/obj-transpose :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/obj-nest :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/obj-keys :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/obj-vals :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/obj-pairs :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/obj-clone :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/obj-assign :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/obj-from-pairs :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/get-in :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/set-in :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/obj-intersection :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/obj-keys-nested :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/obj-difference :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/swap-key :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/to-flat :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/from-flat :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/eq-nested-basic :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/eq-nested-loop :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/eq-nested-obj :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/eq-nested-arr :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/eq-nested :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/eq-shallow :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/tree-walk :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/tree-diff :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/tree-diff-nested :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-every :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-some :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-each :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-find :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-map :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-mapcat :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-partition :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-filter :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-keep :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-keepf :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-juxt :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-foldl :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-foldr :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-pipel :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-piper :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-group-by :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-repeat :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-normalise :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-sort :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/arr-sorted-merge :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/obj-map :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/obj-filter :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/obj-keep :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/obj-keepf :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/clone-shallow :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/clone-nested-loop :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/clone-nested :added "4.1"}
(fact "TODO")

^{:refer xt.lang.common-data/memoize-key :added "4.1"}
(fact "TODO")
