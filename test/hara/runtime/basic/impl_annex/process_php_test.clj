(ns hara.runtime.basic.impl-annex.process-php-test
  (:require [hara.runtime.basic.impl-annex.process-php :refer :all]
            [std.lib.env :as env]
            [hara.runtime.basic.type-oneshot :as p]
            [std.json :as json]
            [hara.lang :as l])
  (:use code.test))

(fact:global {:skip (not (env/program-exists? "php"))})

^{:refer hara.runtime.basic.impl-annex.process-php-test/CANARY-PHP :adopt true :added "4.0"}
(fact "EVALUATE php code"

  (-> (p/rt-oneshot {:lang :php})
      (p/raw-eval-oneshot (default-oneshot-wrap '(+ 1 2 3 4)))
      (json/read json/+keyword-mapper+)
      :value)
  => 10)

^{:refer hara.runtime.basic.impl-annex.process-php/default-oneshot-wrap :added "4.1"}
(fact "captures php eval errors without crashing the wrapper"
  (let [out (-> (p/rt-oneshot {:lang :php})
                (p/raw-eval-oneshot (default-oneshot-wrap '(not_a_real_php_fn)))
                (json/read json/+keyword-mapper+))]
    [(:type out) (string? (:value out))])
  => ["error" true])

^{:refer hara.runtime.basic.impl-annex.process-php/default-oneshot-wrap :added "4.0"}
(fact "creates the oneshot bootstrap form"

  (default-oneshot-wrap 1)
  => string?)

^{:refer hara.runtime.basic.impl-annex.process-php/default-body-transform :added "4.1"}
(fact "transforms oneshot forms for return-eval"
  (default-body-transform '[1 2 3] {})
  => '((quote ((fn [] (return [1 2 3])))))

  (default-body-transform '[1 2 3] {:bulk true})
  => '((quote ((fn [] 1 2 (return 3)))))

  (l/emit-as :php [(default-body-transform '[1 2 3] {})])
  => #"\(function \(\)\{\s+return \[1,2,3\];\s+\}\)\(\)"

  (l/emit-as :php [(default-body-transform '[1 2 3] {:bulk true})])
  => #"\(function \(\)\{\s+1;\s+2;\s+return 3;\s+\}\)\(\)")

^{:refer hara.runtime.basic.impl-annex.process-php/default-basic-client :added "4.0"}
(fact "creates the basic client bootstrap"

  (default-basic-client 19000)
  => string?)


^{:refer hara.runtime.basic.impl-annex.process-php/php-body-source :added "4.1"}
(fact "TODO")

^{:refer hara.runtime.basic.impl-annex.process-php/default-basic-body-transform :added "4.1"}
(fact "TODO")
