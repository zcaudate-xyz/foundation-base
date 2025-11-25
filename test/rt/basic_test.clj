(ns rt.basic-test
  (:use code.test)
  (:require [rt.basic :refer :all]
            [rt.basic.server-basic :as server-basic]
            [std.concurrent :as cc]))

^{:refer rt.basic/clean-relay :added "4.0"}
(fact "cleans the relay on the server"
  (with-redefs [server-basic/get-server (fn [_ _] {:id "test"})
                server-basic/get-relay (fn [_] :relay)
                cc/send (fn [relay msg]
                          (assert (= relay :relay))
                          (assert (= msg {:op :clean})))]
    (clean-relay {:id "test" :lang :lua}))
  => nil)
