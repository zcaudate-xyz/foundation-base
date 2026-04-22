(ns web3.lib.example-bookstore-test
  (:require [rt.solidity]
            [std.lang :as l])
  (:use code.test))

(l/script- :solidity
  {:runtime :web3
   :require [[rt.solidity :as s]
             [web3.lib.example-bookstore :as book]]})

(fact:global
 {:setup    [(s/rt:stop-ganache-server)
             (Thread/sleep 1000)
             (s/rt:start-ganache-server)
             (Thread/sleep 500)
             (l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer web3.lib.example-bookstore/createBook :added "4.0"
  :setup [(s/with:measure
           (s/rt:deploy book/+default-contract+))]}
(fact "creates a book"

  (s/with:measure
   (book/createBook "The Hobbit"
                    "A Unexpected Journey"
                    "Tolkien"
                    100000000000))
  => (contains-in
      [(approx 0.575 0.3) map?])

  (s/with:measure
   (book/createBook "Journey to the West"
                    "Monkey Magic"
                    "-"
                    100000000000))
  => (contains-in
      [(approx 0.489 0.3) map?])

  (book/totalSupply)
  => 2)

^{:refer web3.lib.example-bookstore/transferTo :added "4.0"}
(fact "best way of transfering ether")

^{:refer web3.lib.example-bookstore/sendTo :added "4.0"}
(fact "another way of transfering ether")

^{:refer web3.lib.example-bookstore/payTo :added "4.0"}
(fact "third way of transfering ether")

^{:refer web3.lib.example-bookstore/payForBook :added "4.0"}
(fact "pays for book"

  (s/with:measure
   (s/with:caller-payment [100000000000]
     (book/payForBook 1)))
  => (contains-in
      [(approx 0.6 0.3) map?]))
