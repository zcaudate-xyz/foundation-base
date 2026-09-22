(ns melbourne.base-font-test
  (:use code.test)
  (:require [lang.core :as l]
            [melbourne.base-font :refer :all]))

^{:refer melbourne.base-font/fontFamily :added "4.1"}
(fact "uses an installed platform font without bundling a font asset"
  (let [form (pr-str (:form (l/sym-entry :js 'melbourne.base-font/fontFamily)))]
    (.contains form "system-ui") => true
    (.contains form "Lato") => false))

^{:refer melbourne.base-font/getFontStyle :added "4.0"}
(fact "gets font style gives size")
