(ns code.dev.prompt.dsl-spec-test
  (:use code.test)
  (:require [code.dev.prompt.dsl-spec :as spec]))

^{:refer code.dev.prompt.dsl-spec/construct-item :added "4.0"}
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

^{:refer code.dev.prompt.dsl-spec/construct-alts :added "4.0"}
(fact "constructs alternative forms that result in the same JS string"
  ^:hidden
  
  (spec/construct-alts '[(. this prop long [1] (call))
                         (. this.prop.long [1] (call))]))

^{:refer code.dev.prompt.dsl-spec/create-description :added "4.0"}
(fact "creates the descriptions for spec"
  ^:hidden
  
  (spec/create-description
   (spec/construct-item 'undefined))
  => string?
  
  (spec/create-description
   (spec/construct-item '(fn [(:= a 1)
                              (:= b 2)]
                           (return (* a b)))))
  => string?

  (spec/create-description
   (spec/construct-item '(var x 1)
                        ["var x = 1"
                         "const x = 1"]))
  => string?)


^{:refer code.dev.prompt.dsl-spec/create-spec :added "4.0"}
(fact "creates the actual spec"
  ^:hidden

  (spec/create-spec spec/+meta+
                    (spec/spec-examples))
  => string?)

^{:refer code.dev.prompt.dsl-spec/spec-examples :added "4.0"}
(fact "TODO")
