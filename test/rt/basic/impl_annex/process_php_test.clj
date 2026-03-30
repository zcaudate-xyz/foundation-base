(ns rt.basic.impl-annex.process-php-test
  (:require [rt.basic.impl-annex.process-php :refer :all]
            [rt.basic.type-common :as common]
            [rt.basic.type-oneshot :as p]
            [std.json :as json]
            [std.lang :as l])
  (:use code.test))

(def CANARY-PHP
  (common/program-exists? "php"))

^{:refer rt.basic.impl-annex.process-php/CANARY :adopt true :added "4.0"}
(fact "EVALUATE php code"
  ^:hidden

  (if CANARY-PHP
    (-> (p/rt-oneshot {:lang :php})
        (p/raw-eval-oneshot (default-oneshot-wrap '(+ 1 2 3 4)))
        (json/read json/+keyword-mapper+)
        :value)
    :php-unavailable)
  => (any 10 :php-unavailable))

^{:refer rt.basic.impl-annex.process-php/default-oneshot-wrap :added "4.0"}
(fact "creates the oneshot bootstrap form"

  (default-oneshot-wrap 1)
  => string?)

^{:refer rt.basic.impl-annex.process-php/default-body-transform :added "4.1"}
(fact "transforms oneshot forms for return-eval"
  (default-body-transform '[1 2 3] {})
  => '((quote ((fn [] (return [1 2 3])))))

  (default-body-transform '[1 2 3] {:bulk true})
  => '((quote ((fn [] 1 2 (return 3)))))

  (l/emit-as :php [(default-body-transform '[1 2 3] {})])
  => "(function () {\nreturn [1, 2, 3];\n})()"

  (l/emit-as :php [(default-body-transform '[1 2 3] {:bulk true})])
  => "(function () {\n1;\n2;\nreturn 3;\n})()")

^{:refer rt.basic.impl-annex.process-php/default-basic-client :added "4.0"}
(fact "creates the basic client bootstrap"

  (default-basic-client 19000)
  => string?)
