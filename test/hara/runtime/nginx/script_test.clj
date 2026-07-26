tahto/runtime/nginx/script_test.clj:1:(ns tahto.runtime.nginx.script-test
tahto/runtime/nginx/script_test.clj:2:  (:require [tahto.runtime.nginx.script :refer :all])
  (:use code.test))

tahto/runtime/nginx/script_test.clj:5:^{:refer tahto.runtime.nginx.script/emit-block :added "4.0"}
(fact  "emits a block"

  (emit-block [[:- "hello \nwhere"]])
  => "hello \nwhere"

  (emit-block {:label [[:- "hello \nwhere"]]})
  => "label {\n  hello \n  where\n}")

tahto/runtime/nginx/script_test.clj:14:^{:refer tahto.runtime.nginx.script/write :added "4.0"}
(fact "link to `std.make.compile`"
  (write {:a 1})
  => "a 1;")
