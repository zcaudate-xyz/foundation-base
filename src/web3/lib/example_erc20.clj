(ns web3.lib.example-erc20
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :solidity
  {:require [[rt.solidity :as s]]
   :static  {:contract ["ExampleSample"]}})

(definterface.sol IERC20
  [^{:- [:external]
     :static/returns :bool}
   transfer [:address to
             :uint value]
   ^{:- [:external :view]
     :static/returns :uint}
   balanceOf [:address owner]])

(defevent.sol EventPayment
  [:address from-address]
  [:address to-address]
  [:uint    amount]
  [:address token-address])

(defaddress.sol ^{:- [:public]}
  g:SiteAuthority)

;;
;; CONTRUCTOR
;;

(defconstructor.sol
  __init__
  []
  (:= -/g:SiteAuthority s/msg-sender))

(defn.sol ^{:- [:external :payable]}
  make-payment-erc20
  "makes a payment"
  {:added "4.0"}
  [:address token-address :uint amount]
  (s/require (not= s/msg-value amount)
             "Cannot be zero")
  (var (-/IERC20 erc20) (-/IERC20 token-address))
  (. erc20 (transfer -/g:SiteAuthority
                     amount))
  (emit (-/EventPayment {:from-address s/msg-sender
                         :to-address -/g:SiteAuthority
                         :amount amount
                         :token-address token-address})))

(defn.sol ^{:- [:external :payable]}
  makePayment
  "makes a payment with camelCase"
  {:added "4.0"}
  [:address token-address :uint amount]
  (s/require (not= s/msg-value amount)
             "Cannot be zero")
  (var (-/IERC20 erc20) (-/IERC20 token-address))
  (. erc20 (transfer -/g:SiteAuthority
                     amount))
  (emit (-/EventPayment {:from-address s/msg-sender
                         :to-address -/g:SiteAuthority
                         :amount amount
                         :token-address token-address})))

(defn.sol ^{:- [:external :view]
            :static/returns :uint}
  get-account-balance
  "gets account balance"
  {:added "4.0"}
  [:address token-address]
  (var (-/IERC20 erc20) (-/IERC20 token-address))
  (return (. erc20 (balanceOf s/msg-sender))))

(defn.sol ^{:- [:external :view]
            :static/returns :address}
  get-sender
  "gets the sender for a message"
  {:added "4.0"}
  []
  (return s/msg-sender))

(def +default-contract+
  {:ns   (h/ns-sym)
   :name "ExampleSample"
   :args []
   :interfaces [-/IERC20]})

(comment

  (std.string/join
   "\n"
   ["// SPDX-License-Identifier: GPL-3.0"
    "pragma solidity >=0.7.0 <0.9.0;"
    ""
    (+ "contract " name " {")
    
    "}"])
  
  (s/rt:print)
  (s/rt:start-ganache-server)
  (l/rt:restart))


(comment
  (s/with:temp
    (add-stuff 1 2)))
