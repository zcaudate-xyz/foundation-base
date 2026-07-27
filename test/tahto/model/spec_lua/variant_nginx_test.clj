(ns tahto.model.spec-lua.variant-nginx-test
  (:use code.test)
  (:require [tahto.model.spec-lua.variant-nginx :refer :all]))

^{:refer tahto.model.spec-lua.variant-nginx/tf-for-async :added "4.1"}
(fact "transforms for:async loops")

^{:refer tahto.model.spec-lua.variant-nginx/lua-tf-x-socket-connect :added "4.1"}
(fact "connects sockets")

^{:refer tahto.model.spec-lua.variant-nginx/lua-tf-x-with-delay :added "4.1"}
(fact "delays execution")

^{:refer tahto.model.spec-lua.variant-nginx/lua-tf-x-async-run :added "4.1"}
(fact "runs asynchronously")
