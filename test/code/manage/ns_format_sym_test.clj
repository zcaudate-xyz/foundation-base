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
  (-> "(let [x tb/foo y other/bar] [x y])"
      nav/parse-string
      usage:expand-tb
      nav/root-string
      read-string)
  => '(let [x type-base/foo y other/bar] [x y]))

^{:refer code.manage.ns-format-sym/ns-format-sym :added "4.1"}
(fact "passes the symbol expansion edits to refactor-code"
  (with-redefs [code.framework/refactor-code
                (fn [_ params _ _]
                  (mapv str (:edits params)))]
    (ns-format-sym 'sample.ns {} nil nil))
  => ["code.manage.ns-format-sym/alias:expand-tb"
      "code.manage.ns-format-sym/usage:expand-tb"])
