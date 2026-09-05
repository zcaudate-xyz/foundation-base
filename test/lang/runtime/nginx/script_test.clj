(ns lang.runtime.nginx.script-test
  (:require [lang.runtime.nginx.script :refer :all])
  (:use code.test))

^{:refer lang.runtime.nginx.script/emit-block :added "4.0"}
(fact  "emits a block"

  (emit-block [[:- "hello \nwhere"]])
  => "hello \nwhere"

  (emit-block {:label [[:- "hello \nwhere"]]})
  => "label {\n  hello \n  where\n}")

^{:refer lang.runtime.nginx.script/write :added "4.0"}
(fact "link to `std.make.compile`"
  (write {:a 1})
  => "a 1;")
