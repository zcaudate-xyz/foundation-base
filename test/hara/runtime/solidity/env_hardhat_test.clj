(ns hara.runtime.solidity.env-hardhat-test
  (:require [hara.runtime.solidity.env-ganache :as env-ganache]
            [hara.runtime.solidity.env-hardhat :refer :all])
  (:use code.test))

^{:refer hara.runtime.solidity.env-hardhat/start-hardhat-server :added "4.0"}
(fact "starts the hardhat service"
  (with-redefs [env-ganache/start-hardhat-server (fn [] {:type "hardhat"})]
    (start-hardhat-server))
  => {:type "hardhat"})

^{:refer hara.runtime.solidity.env-hardhat/stop-hardhat-server :added "4.0"}
(fact "stops the hardhat service"
  (with-redefs [env-ganache/stop-hardhat-server (fn [] {:type "hardhat"})]
    (stop-hardhat-server))
  => {:type "hardhat"})


^{:refer hara.runtime.solidity.env-hardhat/start-ganache-server :added "4.0"}
(fact "starts the ganache service via the ganache wrapper"
  (with-redefs [env-ganache/start-ganache-server (fn [] {:type "ganache"})]
    (start-ganache-server))
  => {:type "ganache"})

^{:refer hara.runtime.solidity.env-hardhat/stop-ganache-server :added "4.0"}
(fact "stops the ganache service via the ganache wrapper"
  (with-redefs [env-ganache/stop-ganache-server (fn [] {:type "ganache"})]
    (stop-ganache-server))
  => {:type "ganache"})