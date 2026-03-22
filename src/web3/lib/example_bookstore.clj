(ns web3.lib.example-bookstore
  (:require [std.lang :as l]
            [std.lib.env :as env]))

(l/script :solidity
  {:require [[rt.solidity :as s]]})


;;
;; Schema
;;

(def.sol ^{:- [:uint256]}
  taxFee)

(def.sol ^{:- [:address :immutable]}
  taxAccount)

(def.sol ^{:- [:uint8 :public]}
  totalSupply 0)

(defstruct.sol
  BookStruct
  [:uint8 id]
  [:address seller]
  [:string title]
  [:string description]
  [:string author]
  [:uint256 cost]
  [:uint256 timestamp])

(def.sol ^{:- [-/BookStruct []]}
  books)

(defmapping.sol booksOf
  [:address (:% -/BookStruct [])])

(defmapping.sol ^{:- [:public]}
  sellerOf
  [:uint8 :address])

(defmapping.sol
  bookExists
  [:uint8 :bool])

(defevent.sol Sale
  [:uint8 id]
  [:address :indexed buyer]
  [:address :indexed seller]
  [:uint256 cost]
  [:uint256 timestamp])

(defevent.sol Created
  [:uint8 id]
  [:address :indexed seller]
  [:uint256 timestamp])

;;
;; Methods
;;

(defconstructor.sol
  __init__
  [:uint256 _taxFee]
  (:= -/taxAccount s/msg-sender)
  (:= -/taxFee _taxFee))

(defn.sol ^{:- [:public]
            :static/returns [:bool]}
  createBook
  "creates a book"
  {:added "4.0"}
  [:string :memory title
   :string :memory description
   :string :memory author
   :uint256 cost]
  (s/require (< 0 (. (s/bytes title) length))
             "Title is empty")
  (s/require (< 0 (. (s/bytes description) length))
             "Description is empty")
  (s/require (< 0 (. (s/bytes author) length))
             "Author is empty")
  (s/require (< 0 cost)
             "Price cannot be zero")
  (. -/books
     (push (-/BookStruct
            (:++ -/totalSupply)
            s/msg-sender
            title
            description
            author
            cost
            s/block-timestamp)))
  (:= (. -/sellerOf [-/totalSupply]) s/msg-sender)

  (emit (-/Created
         -/totalSupply
         s/msg-sender
         s/block-timestamp))
  (return true))

(defn.sol ^{:- [:internal]
            :static/returns [:bool]}
  transferTo
  "best way of transfering ether"
  {:added "4.0"}
  [:address to
   :uint256 amount]
  (. (payable to) (transfer amount))
  (return true))

(defn.sol ^{:- [:internal]
            :static/returns [:bool]}
  sendTo
  "another way of transfering ether"
  {:added "4.0"}
  [:address to
   :uint256 amount]
  (s/require (. (payable to)
                (send amount))
             "Payment failed")
  (return true))

(defn.sol ^{:- [:internal]
            :static/returns [:bool]}
  payTo
  "third way of transfering ether"
  {:added "4.0"}
  [:address to
   :uint256 amount]
  (var '((:bool success) _) (. (payable to)
                               (^{:value amount}
                                call "")
                               ))
  (s/require success "Payment failed")
  (return true))

(defn.sol ^{:- [:public :payable]
            :static/returns [:bool]}
  payForBook
  "pays for book"
  {:added "4.0"}
  [:uint8 id]
  (s/require (< 0 (. -/books [(- id 1)] id))
             "Book does not exist")
  (s/require (>= s/msg-value
                 (. -/books [(- id 1)] cost))
             "Ethers too small")
  (var (:address seller) (. -/sellerOf [id]))
  (var (:uint256 tax) (* (/ s/msg-value 100)
                         -/taxFee))
  (var (:uint256 payment) (- s/msg-value tax))

  (-/payTo seller payment)
  (-/payTo taxAccount tax)
  
  (. -/booksOf [msg.sender]
     (push (. books [(- id 1)])))
  (emit (-/Sale id s/msg-sender seller payment s/block-timestamp))
  (return true))

(def +default-contract+
  {:ns   (env/ns-sym)
   :name "Bookstore"
   :args [10]})
