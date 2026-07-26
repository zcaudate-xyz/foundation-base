tahto/model/spec_js/html_test.clj:1:(ns tahto.model.spec-js.html-test
  (:require [std.html :as html]
tahto/model/spec_js/html_test.clj:3:            [tahto.model.spec-js.html :refer :all])
  (:use code.test))

tahto/model/spec_js/html_test.clj:6:^{:refer tahto.model.spec-js.html/wrap-indent-inner :added "4.0"}
(fact "increase indentation in walk inner")

tahto/model/spec_js/html_test.clj:9:^{:refer tahto.model.spec-js.html/wrap-indent-outer :added "4.0"}
(fact "decrese indentation in walk outer")

tahto/model/spec_js/html_test.clj:12:^{:refer tahto.model.spec-js.html/prewalk-indent :added "4.0"}
(fact "preserves indentations")

tahto/model/spec_js/html_test.clj:15:^{:refer tahto.model.spec-js.html/prepare-html :added "4.0"}
(fact "prepares the html, embedding any new scripts")

tahto/model/spec_js/html_test.clj:18:^{:refer tahto.model.spec-js.html/emit-html :added "4.0"}
(fact "emits the html"

  (html/tree (emit-html [:a [:b [:c]]]
                        {} {}))
  => [:a [:b [:c]]])
