(ns web3.lib.example-counter-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :solidity
  {:runtime :web3
   :require [[rt.solidity :as s]
             [web3.lib.example-counter :as counter]]})

(fact:global
 {:setup [(do (s/rt:stop-ganache-server)
              (Thread/sleep 1000)
              (s/rt:start-ganache-server)
              (Thread/sleep 500)
              (l/rt:restart))]
  :teardown [(l/rt:stop)]})

^{:refer web3.lib.example-counter/CANARY :adopt true :added "4.0"}
(fact "determanistic gas fees"

  (s/with:measure
   (s/rt:deploy counter/+default-contract+))
  => (contains-in [(approx 0.7 0.5) map?]))

^{:refer web3.lib.example-counter/m:get-counter0 :added "4.0"
  :setup [(s/with:measure
           (s/rt:deploy counter/+default-contract+))]}
(fact "gets the 0 counter"

  (counter/g:Counter0)
  (counter/g:Counter1)

  (s/with:measure
   (counter/m:get-counter0))
  => [0.0 0]

  [(s/with:measure
    (do (counter/m:inc-counter0)
        (counter/m:get-counter0)))

   (s/with:measure
    (do (counter/m:inc-counter0)
        (counter/m:get-counter0)))
   (s/with:measure
    (do (counter/m:inc-counter0)
        (counter/m:get-counter0)))
   (s/with:measure
    (do (counter/m:inc-counter0)
        (counter/m:get-counter0)))]
  => (contains-in
      [[(approx 0.07 0.07) 1]
       [(approx 0.04 0.04) 2]
       [(approx 0.04 0.04) 3]
       [(approx 0.04 0.04) 4]]))

^{:refer web3.lib.example-counter/m:get-counter1 :added "4.0"
  :setup [(s/with:measure
           (s/rt:deploy counter/+default-contract+))]}
(fact "gets the 1 counter"

  (s/with:measure
   (counter/m:get-counter1))
  => [0.0 0]

  [(s/with:measure
    (do (counter/m:inc-counter1)
        (counter/m:get-counter1)))
   (s/with:measure
    (do (counter/m:inc-counter1)
        (counter/m:get-counter1)))
   (s/with:measure
    (do (counter/m:inc-counter1)
        (counter/m:get-counter1)))
   (s/with:measure
    (do (counter/m:inc-counter1)
        (counter/m:get-counter1)))]
  => (contains-in
      [[(approx 0.08 0.02) 1]
       [(approx 0.05 0.02) 2]
       [(approx 0.05 0.02) 3]
       [(approx 0.05 0.02) 4]]))

^{:refer web3.lib.example-counter/m:inc-counter0 :added "4.0"}
(fact "increments counter 0")

^{:refer web3.lib.example-counter/m:inc-counter1 :added "4.0"}
(fact "increments counter 1")

^{:refer web3.lib.example-counter/m:dec-counter0 :added "4.0"
  :setup [(s/with:measure
           (s/rt:deploy counter/+default-contract+))]}
(fact "decrements counter 0"

  [(s/with:measure
    (counter/m:inc-counter0)
    (counter/m:get-counter0))
   (s/with:measure
    (counter/m:dec-counter0)
    (counter/m:get-counter0))
   (s/with:measure
    (counter/m:dec-counter0)
    (counter/m:get-counter0))]
  => (contains-in
      [[(approx 0.07 0.02) 1]
       [(approx 0.04 0.02) 0]
       [(approx 0.04 0.02) 0]]))

^{:refer web3.lib.example-counter/m:dec-counter1 :added "4.0"}
(fact "decrements counter 1")

^{:refer web3.lib.example-counter/m:inc-both :added "4.0"
  :setup [(s/with:measure
           (s/rt:deploy counter/+default-contract+))]}
(fact "increments both"

  [(s/with:measure
    (do (counter/m:inc-both)
        (counter/m:get-counter1)))
   (s/with:measure
    (do (counter/m:inc-both)
        (counter/m:get-counter1)))
   (s/with:measure
    (do (counter/m:inc-both)
        (counter/m:get-counter1)))
   (s/with:measure
    (do (counter/m:inc-both)
        (counter/m:get-counter1)))]
  => (contains-in
      [[(approx 0.1 0.02) 1]
       [(approx 0.05 0.02) 2]
       [(approx 0.05 0.02) 3]
       [(approx 0.05 0.02) 4]]))

^{:refer web3.lib.example-counter/m:add-both :added "4.0"
  :setup [(s/with:measure
           (s/rt:deploy counter/+default-contract+))]}
(fact "performs add operation"

  [(s/with:measure
    (do (counter/m:add-both 10)
        (counter/m:get-counter1)))
   (s/with:measure
    (do (counter/m:add-both 10)
        (counter/m:get-counter1)))
   (s/with:measure
    (do (counter/m:add-both 10)
        (counter/m:get-counter1)))
   (s/with:measure
    (do (counter/m:add-both 10)
        (counter/m:get-counter1)))]
  => (contains-in
      [[(approx 0.1 0.02) 10]
       [(approx 0.05 0.02) 20]
       [(approx 0.05 0.02) 30]
       [(approx 0.05 0.02) 40]]))

(comment
  (s/rt:deploy-ptr s/ut:str-comp)
  (s/rt:get-contract-address)
  (s/with:measure
   (counter/m:add-both 1000000)))
