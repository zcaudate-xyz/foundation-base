(ns rt.solidity.env-ganache-test
  (:use code.test)
  (:require [rt.solidity.env-ganache :refer :all]
            [std.lib :as h]))

^{:refer rt.solidity.env-ganache/start-ganache-server :added "4.0"}
(fact "starts the ganache service"
  (with-redefs [h/sh (fn [& _] {})
                h/wait-for-port (fn [& _] nil)
                h/future (fn [_] nil)
                h/on:complete (fn [_ _] nil)
                *server* (atom nil)]
    (start-ganache-server))
  => map?)

^{:refer rt.solidity.env-ganache/stop-ganache-server :added "4.0"}
(fact "stops the ganache service"
  (with-redefs [h/sh-close (fn [_] nil)
                h/sh-exit (fn [_] nil)
                h/sh-wait (fn [_] nil)
                *server* (atom {:process {}})]
    (stop-ganache-server))
  => map?)
