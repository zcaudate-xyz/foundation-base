(ns hara.lang.model.spec-js.html-test
  (:require [std.html :as html]
            [hara.lang.model.spec-js.html :refer :all])
  (:use code.test))

^{:refer hara.lang.model.spec-js.html/wrap-indent-inner :added "4.0"}
(fact "increase indentation in walk inner")

^{:refer hara.lang.model.spec-js.html/wrap-indent-outer :added "4.0"}
(fact "decrese indentation in walk outer")

^{:refer hara.lang.model.spec-js.html/prewalk-indent :added "4.0"}
(fact "preserves indentations")

^{:refer hara.lang.model.spec-js.html/prepare-html :added "4.0"}
(fact "prepares the html, embedding any new scripts")

^{:refer hara.lang.model.spec-js.html/emit-html :added "4.0"}
(fact "emits the html"

  (html/tree (emit-html [:a [:b [:c]]]
                        {} {}))
  => [:a [:b [:c]]])
