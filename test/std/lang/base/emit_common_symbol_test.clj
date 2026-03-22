(ns std.lang.base.emit-common-symbol-test
  (:require [std.lang.base.emit-common :as common :refer :all]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.grammar :as grammar])
  (:use code.test))

(def +reserved+
  (-> (grammar/build)
      (grammar/to-reserved)))

(def +grammar+
  (grammar/grammar :test +reserved+ helper/+default+))

^{:refer std.lang.base.emit-common/emit-symbol :adopt true :added "4.0"}
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
