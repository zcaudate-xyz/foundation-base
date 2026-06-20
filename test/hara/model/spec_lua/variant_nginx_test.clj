(ns hara.model.spec-lua.variant-nginx-test
  (:use code.test)
  (:require [hara.model.spec-lua.variant-nginx :refer :all]))

^{:refer hara.model.spec-lua.variant-nginx/tf-for-async :added "4.1"}
(fact "transforms for:async loops")

^{:refer hara.model.spec-lua.variant-nginx/lua-tf-x-socket-connect :added "4.1"}
(fact "connects sockets")

^{:refer hara.model.spec-lua.variant-nginx/lua-tf-x-with-delay :added "4.1"}
(fact "delays execution")

^{:refer hara.model.spec-lua.variant-nginx/lua-tf-x-async-run :added "4.1"}
(fact "runs asynchronously")
