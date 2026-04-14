(ns code.manage.ns-format-sym-test
  (:require [code.manage.ns-format-sym :refer :all]
            [std.block.navigate :as nav])
  (:use code.test))

^{:refer code.manage.ns-format-sym/alias:expand-tb :added "4.1"}
(fact "expands the tb alias in ns forms"
  (-> "(ns sample (:require [szndb.core.type-base :as tb]))"
      nav/parse-string
      alias:expand-tb
      nav/root-string
      read-string)
  => '(ns sample (:require [szndb.core.type-base :as type-base])))

^{:refer code.manage.ns-format-sym/usage:expand-tb :added "4.1"}
(fact "expands tb/ usages while leaving other symbols unchanged"
  (with-redefs [code.query/modify
                (fn [nav matcher transform]
                  (with-redefs [std.block.navigate/value (fn [_] 'tb/foo)
                                std.block.navigate/replace (fn [_ value] value)]
                    [nav
                     (matcher 'tb/foo)
                     (transform :zloc)]))]
    (usage:expand-tb :nav))
  => [:nav true 'type-base/foo])

^{:refer code.manage.ns-format-sym/ns-format-sym :added "4.1"}
(fact "passes the symbol expansion edits to refactor-code"
  (with-redefs [code.framework/refactor-code
                (fn [_ params _ _]
                  (:edits params))]
    (let [[alias-edit usage-edit] (ns-format-sym 'sample.ns {} nil nil)]
      [(= alias-edit alias:expand-tb)
       (= usage-edit usage:expand-tb)
       (-> "(ns sample (:require [szndb.core.type-base :as tb]))"
           nav/parse-string
           alias-edit
           nav/root-string
           read-string)]))
  => [true
      true
      '(ns sample (:require [szndb.core.type-base :as type-base]))])
