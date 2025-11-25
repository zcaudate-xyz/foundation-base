(ns rt.nginx.script-test
  (:use code.test)
  (:require [rt.nginx.script :refer :all]))

^{:refer rt.nginx.script/emit-block :added "4.0"}
(fact  "emits a block"
  ^:hidden
  
  (emit-block [[:- "hello \nwhere"]])
  => "hello \nwhere"

  (emit-block {:label [[:- "hello \nwhere"]]})
  => "label {\n  hello \n  where\n}")

^{:refer rt.nginx.script/write :added "4.0"}
(fact "link to `std.make.compile`"
  (write {:a 1})
  => "a 1;")
