(ns kmi.redis.sample-type-test
  (:use code.test)
  (:require [kmi.redis.sample-type :as type]))

^{:refer kmi.redis.sample-type/> :added "4.0"}
(fact "shortcut for data access"
  ^:hidden
  
  (type/> [:KEYS ["test"] [:unfilled]])
  => "kmi.redis.scan_sub('test' .. ':' .. 'unfilled')"

  (type/> [:GET ["test"] [:unfilled]])
  => "xt.lang.base_lib.arr_map(\n  kmi.redis.scan_level('test' .. ':' .. 'unfilled'),\n  kmi.redis.key_export\n)")

^{:refer kmi.redis.sample-type/P> :added "4.0"}
(fact "shortcut for data access")
