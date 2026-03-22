(ns rt.solidity.env-ganache-test
  (:require [rt.solidity.env-ganache :refer :all]
            [std.lib.future :as future]
            [std.lib.network :as network]
            [std.lib.os :as os])
  (:use code.test))

^{:refer rt.solidity.env-ganache/start-ganache-server :added "4.0"}
(fact "starts the ganache service"
  (with-redefs [os/sh (fn [& _] {})
                network/wait-for-port (fn [& _] nil)
                future/future (fn [_] nil)
                future/on:complete (fn [_ _] nil)
                *server* (atom nil)]
    (start-ganache-server))
  => map?)

^{:refer rt.solidity.env-ganache/stop-ganache-server :added "4.0"}
(fact "stops the ganache service"
  (with-redefs [os/sh-close (fn [_] nil)
                os/sh-exit (fn [_] nil)
                os/sh-wait (fn [_] nil)
                *server* (atom {:process {}})]
    (stop-ganache-server))
  => map?)
