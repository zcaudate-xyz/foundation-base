(ns std.lang.model.spec-xtalk-optional-test
  (:require [std.lang :as l]
            [std.lang.model.spec-xtalk.fn-js :refer [js-tf-x-str-substring]]
            [std.lang.model.spec-xtalk.fn-python :refer [python-tf-x-str-substring]])
  (:use code.test))

(fact "js substring emits open-ended ranges"
  (l/emit-as :js [(js-tf-x-str-substring '[_ s 0])])
  => "s.substring(0)")

(fact "python substring emits open-ended slices"
  (l/emit-as :python [(python-tf-x-str-substring '[_ s 0])])
  => "s[(0 - 0):]")
