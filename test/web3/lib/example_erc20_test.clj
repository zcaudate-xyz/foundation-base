(ns web3.lib.example-erc20-test
  (:require [web3.lib.example-erc20 :refer :all])
  (:use code.test))

^{:refer web3.lib.example-erc20/make-payment-erc20 :added "4.0"}
(fact "makes a payment")

^{:refer web3.lib.example-erc20/makePayment :added "4.0"}
(fact "makes a payment with camelCase")

^{:refer web3.lib.example-erc20/get-account-balance :added "4.0"}
(fact "gets account balance")

^{:refer web3.lib.example-erc20/get-sender :added "4.0"}
(fact "gets the sender for a message")
