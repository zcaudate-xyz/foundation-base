(ns lang.model.spec-lua.variant-nginx-test
  (:use code.test)
  (:require [lang.model.spec-lua.variant-nginx :refer :all]))

^{:refer lang.model.spec-lua.variant-nginx/tf-for-async :added "4.1"}
(fact "transforms for:async loops")

^{:refer lang.model.spec-lua.variant-nginx/lua-tf-x-socket-connect :added "4.1"}
(fact "connects sockets")

^{:refer lang.model.spec-lua.variant-nginx/lua-tf-x-with-delay :added "4.1"}
(fact "delays execution")

^{:refer lang.model.spec-lua.variant-nginx/lua-tf-x-async-run :added "4.1"}
(fact "runs asynchronously")
