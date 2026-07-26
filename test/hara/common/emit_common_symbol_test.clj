tahto/common/emit_common_symbol_test.clj:1:(ns tahto.common.emit-common-symbol-test
tahto/common/emit_common_symbol_test.clj:2:  (:require [tahto.common.emit-common :as common :refer :all]
tahto/common/emit_common_symbol_test.clj:3:            [tahto.common.emit-helper :as helper]
tahto/common/emit_common_symbol_test.clj:4:            [tahto.common.grammar :as grammar])
  (:use code.test))

(def +reserved+
  (-> (grammar/build)
      (grammar/to-reserved)))

(def +grammar+
  (grammar/grammar :test +reserved+ helper/+default+))

tahto/common/emit_common_symbol_test.clj:14:^{:refer tahto.common.emit-common/emit-symbol :adopt true :added "4.0"}
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
