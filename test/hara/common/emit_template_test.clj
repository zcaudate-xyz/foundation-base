(ns hara.common.emit-template-test
  (:use code.test)
  (:require [hara.common.emit-template :refer :all]))

^{:refer hara.common.emit-template/entry-reserved :added "4.1"}
(fact "gets the reserved grammar entry for a code entry"
  (entry-reserved {:reserved {:add {:emit :macro}}}
                  {:op :add})
  => {:emit :macro}

  (entry-reserved {:reserved {:add {:emit :macro}}}
                  {:op :sub})
  => nil)

^{:refer hara.common.emit-template/materialize-code-entry :added "4.1"}
(fact "returns the entry unchanged when it cannot be materialized"
  (materialize-code-entry {:grammar {}
                           :modules {:math {}}}
                          {:op :add})
  => {:op :add}

  (materialize-code-entry {:grammar {:reserved {:add {:emit :macro}}}
                           :modules nil}
                          {:op :add})
  => {:op :add})
