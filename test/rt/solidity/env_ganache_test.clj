(ns rt.solidity.env-ganache-test
  (:require [rt.solidity.env-ganache :refer :all]
            [std.lib.future]
            [std.lib.network]
            [std.lib.os])
  (:use code.test))

^{:refer rt.solidity.env-ganache/start-ganache-server :added "4.0"}
(fact "starts the ganache service"
  (with-redefs [std.lib.os/sh (fn [& _] {})
                std.lib.network/wait-for-port (fn [& _] nil)
                std.lib.future/future (fn [_] nil)
                std.lib.future/on:complete (fn [_ _] nil)
                *server* (atom nil)]
    (start-ganache-server))
  => map?)

^{:refer rt.solidity.env-ganache/stop-ganache-server :added "4.0"}
(fact "stops the ganache service"
  (with-redefs [std.lib.os/sh-close (fn [_] nil)
                std.lib.os/sh-exit (fn [_] nil)
                std.lib.os/sh-wait (fn [_] nil)
                *server* (atom {:process {}})]
    (stop-ganache-server))
  => map?)
