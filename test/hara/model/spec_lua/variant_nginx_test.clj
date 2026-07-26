tahto/model/spec_lua/variant_nginx_test.clj:1:(ns tahto.model.spec-lua.variant-nginx-test
  (:use code.test)
tahto/model/spec_lua/variant_nginx_test.clj:3:  (:require [tahto.model.spec-lua.variant-nginx :refer :all]))

tahto/model/spec_lua/variant_nginx_test.clj:5:^{:refer tahto.model.spec-lua.variant-nginx/tf-for-async :added "4.1"}
(fact "transforms for:async loops")

tahto/model/spec_lua/variant_nginx_test.clj:8:^{:refer tahto.model.spec-lua.variant-nginx/lua-tf-x-socket-connect :added "4.1"}
(fact "connects sockets")

tahto/model/spec_lua/variant_nginx_test.clj:11:^{:refer tahto.model.spec-lua.variant-nginx/lua-tf-x-with-delay :added "4.1"}
(fact "delays execution")

tahto/model/spec_lua/variant_nginx_test.clj:14:^{:refer tahto.model.spec-lua.variant-nginx/lua-tf-x-async-run :added "4.1"}
(fact "runs asynchronously")
