tahto/runtime/basic_test.clj:1:(ns tahto.runtime.basic-test
tahto/runtime/basic_test.clj:2:  (:require [tahto.runtime.basic :refer :all]
tahto/runtime/basic_test.clj:3:            [tahto.runtime.basic.server-basic :as server-basic]
            [std.concurrent :as cc])
  (:use code.test))

tahto/runtime/basic_test.clj:7:^{:refer tahto.runtime.basic/clean-relay :added "4.0"}
(fact "cleans the relay on the server"
  (with-redefs [server-basic/get-server (fn [_ _] {:id "test"})
                server-basic/get-relay (fn [_] :relay)
                cc/send (fn [relay msg]
                          (assert (= relay :relay))
                          (assert (= msg {:op :clean})))]
    (clean-relay {:id "test" :lang :lua}))
  => nil)
