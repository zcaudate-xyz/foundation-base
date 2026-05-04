(ns hara.common.emit-common-symbol-test
  (:require [hara.common.emit-common :as common :refer :all]
            [hara.common.emit-helper :as helper]
            [hara.common.grammar :as grammar])
  (:use code.test))

(def +reserved+
  (-> (grammar/build)
      (grammar/to-reserved)))

(def +grammar+
  (grammar/grammar :test +reserved+ helper/+default+))

^{:refer hara.common.emit-common/emit-symbol :adopt true :added "4.0"}
(fact "emit symbol"

  (emit-symbol 'hello/hello +grammar+ {:layout :full
                                       :module {:link '{hello hello}}})
  => "hello____hello"

  (emit-symbol 'hello/hello +grammar+ {:layout :module
                                       :module {:link '{hello hello}}})
  => "hello.hello"

  (emit-symbol 'hello/hello +grammar+ {:layout :host
                                       :module {:link '{hello hello}}})
  => "hello____hello"

  (emit-symbol 'hello/hello +grammar+ {:layout :flat
                                       :module {:link '{hello hello}}})
  => "hello"

  (emit-symbol 'hello/hello +grammar+ {:layout :full
                                       :module {:link '{hello hello.world.again}}})
  => "hello_world_again____hello")
