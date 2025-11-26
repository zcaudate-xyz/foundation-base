(ns indigo.prompt.dsl-spec-test
  (:use code.test)
  (:require [indigo.prompt.dsl-spec :as spec]))

^{:refer indigo.prompt.dsl-spec/construct-item :added "4.0"}
(fact "constructs the item"
  ^:hidden
  
  (spec/construct-item '(cond (== x 1)
                              (do (:= a 1))
                              
                              :else
                              (do (:= b 1))))
  => {:op :convert,
      :desc nil,
      :dsl
      ["(cond (== x 1)\n      (do (:= a 1))\n      :else\n      (do (:= b 1)))"],
      :js ["if(x == 1){\n  a = 1;\n}\nelse{\n  b = 1;\n}"]})

^{:refer indigo.prompt.dsl-spec/construct-alts :added "4.0"}
(fact "constructs alternative forms that result in the same JS string"
  ^:hidden
  
  (spec/construct-alts '[(. this prop long [1] (call))
                         (. this.prop.long [1] (call))])
  => {:op :alternate,
      :desc nil,
      :dsl ["(. this prop long [1] (call))" "(. this.prop.long [1] (call))"],
      :js ["this.prop.long[1].call()"]})

^{:refer indigo.prompt.dsl-spec/create-spec-description :added "4.0"}
(fact "creates the descriptions for spec"
  ^:hidden
  
  (spec/create-spec-description
   (spec/construct-item 'undefined))
  => string?
  
  (spec/create-spec-description
   (spec/construct-item '(fn [(:= a 1)
                              (:= b 2)]
                           (return (* a b)))))
  => string?

  (spec/create-spec-description
   (spec/construct-item '(var x 1)
                        ["var x = 1"
                         "const x = 1"]))
  => string?)

^{:refer indigo.prompt.dsl-spec/create-spec-main :added "4.0"}
(fact "creates the actual spec"
  ^:hidden
  
  (spec/create-spec-main spec/+meta+
                         (spec/spec-example-forms)
                         []
                         [])
  => string?)

^{:refer indigo.prompt.dsl-spec/spec-examples :added "4.0"}
(fact "TODO")


^{:refer indigo.prompt.dsl-spec/spec-example-files :added "4.0"}
(fact "TODO")

^{:refer indigo.prompt.dsl-spec/spec-example-forms :added "4.0"}
(fact "TODO")

^{:refer indigo.prompt.dsl-spec/create-spec :added "4.0"}
(fact "TODO")