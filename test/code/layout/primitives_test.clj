(ns code.layout.primitives-test
  (:use code.test)
  (:require [code.layout.primitives :as p]))

^{:refer code.layout.primitives/let-check-multiline :added "4.0"}
(fact "TODO")

^{:refer code.layout.primitives/get-layout-spec :added "4.0"}
(fact "get the layout specification"
  ^:hidden
  
  (p/get-layout-spec :form 'let)
  => (contains-in
      {:block 1, :args {:bindings {:at 1, :inline true,
                                   :layout {:type :vector,
                                            :columns 2,
                                            :align-col true}},
                        :default {}},
       :fn {:multiline fn?}}))

^{:refer code.layout.primitives/create-stack-entry :added "4.0"}
(fact "TODO")

^{:refer code.layout.primitives/get-special-check :added "4.0"}
(fact "TODO")

^{:refer code.layout.primitives/get-max-width-children :added "4.0"}
(fact "TODO")

^{:refer code.layout.primitives/get-max-width :added "4.0"}
(fact "TODO")

^{:refer code.layout.primitives/estimate-multiline-special :added "4.0"}
(fact "TODO")

^{:refer code.layout.primitives/estimate-multiline :added "4.0"}
(fact "TODO")
