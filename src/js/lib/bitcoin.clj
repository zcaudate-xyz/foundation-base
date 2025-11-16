(ns js.lib.bitcoin
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:bundle {:default  [["bitcoinjs-lib" :as Bitcoin]]
            :message  [["bitcoinjs-message" :as BitcoinMessage]]
            :ecc      [["tiny-secp256k1" :as TinySecp256k1]]
            :ecpair   [["ecpair" :as ECPairFactory]]
            :bip32    [["bip32" :as BIP32Factory]]
            :bip39    [["bip39" :as BIP39]]
            :wif      [["wif" :as WIF]]
            :safe-buffer [["safe-buffer" :as SafeBuffer]]}
   :require [[xt.lang.base-lib :as k]
             [js.core :as j]]
   :import  [["bitcoinjs-lib" :as Bitcoin]
             ["bitcoinjs-message" :as BitcoinMessage]
             ["tiny-secp256k1" :as TinySecp256k1]
             ["ecpair" :as ECPairFactory]
             ["bip32" :as BIP32Factory]
             ["bip39" :as BIP39]
             ["wif" :as WIF]
             ["safe-buffer" :as SafeBuffer]]})

(def +networks+
  {:citycoin ["main" "test"],
   :groestlcoin ["main" "test" "regtest"],
   :namecoin ["main" "test"],
   :bitcoin_gold ["main" "test"],
   :decred ["main" "test"],
   :qtum ["main"],
   :digibyte ["main"],
   :bitcoin ["main" "test" "regtest" "simnet"],
   :peercoin ["main" "test"],
   :c0ban ["main" "test" "regtest"],
   :dash ["main" "test"],
   :monacoin ["main" "test"],
   :nubits ["main"],
   :blackcoin ["main" "test"],
   :zcash ["main" "test"],
   :litecoin ["main" "test"],
   :x42 ["main" "test"],
   :dogecoin ["main" "test"],
   :vertcoin ["main" "test" "regtest"],
   :bitcoincash ["main" "test" "regtest"],
   :viacoin ["main" "test"],
   :reddcoin ["main" "test"],
   :ravencoin ["main" "test"],
   :denarius ["main" "test"]})

(def +api+
  {:payments ["embed" "p2ms" "p2pk" "p2pkh" "p2sh" "p2wpkh" "p2wsh"],
   :address  ["fromBase58Check"
              "fromBech32"
              "toBase58Check"
              "toBech32"
              "fromOutputScript"
              "toOutputScript"]
   :script   ["OPS"
              "isPushOnly"
              "compile"
              "decompile"
              "toASM"
              "fromASM"
              "toStack"
              "isCanonicalPubKey"
              "isDefinedHashType"
              "isCanonicalScriptSignature"
              "number"
              "signature"],
   :networks ["bitcoin" "regtest" "testnet"],
   :crypto   ["ripemd160" "sha1" "sha256" "hash160" "hash256" "taggedHash"],
   :Transaction ["DEFAULT_SEQUENCE"
                 "SIGHASH_DEFAULT"
                 "SIGHASH_ALL"
                 "SIGHASH_NONE"
                 "SIGHASH_SINGLE"
                 "SIGHASH_ANYONECANPAY"
                 "SIGHASH_OUTPUT_MASK"
                 "SIGHASH_INPUT_MASK"
                 "ADVANCED_TRANSACTION_MARKER"
                 "ADVANCED_TRANSACTION_FLAG"]
   :opcodes     ["OP_FALSE"
                 "OP_0"
                 "OP_PUSHDATA1"
                 "OP_PUSHDATA2"
                 "OP_PUSHDATA4"
                 "OP_1NEGATE"
                 "OP_RESERVED"
                 "OP_TRUE"
                 "OP_1"
                 "OP_2"
                 "OP_3"
                 "OP_4"
                 "OP_5"
                 "OP_6"
                 "OP_7"
                 "OP_8"
                 "OP_9"
                 "OP_10"
                 "OP_11"
                 "OP_12"
                 "OP_13"
                 "OP_14"
                 "OP_15"
                 "OP_16"
                 "OP_NOP"
                 "OP_VER"
                 "OP_IF"
                 "OP_NOTIF"
                 "OP_VERIF"
                 "OP_VERNOTIF"
                 "OP_ELSE"
                 "OP_ENDIF"
                 "OP_VERIFY"
                 "OP_RETURN"
                 "OP_TOALTSTACK"
                 "OP_FROMALTSTACK"
                 "OP_2DROP"
                 "OP_2DUP"
                 "OP_3DUP"
                 "OP_2OVER"
                 "OP_2ROT"
                 "OP_2SWAP"
                 "OP_IFDUP"
                 "OP_DEPTH"
                 "OP_DROP"
                 "OP_DUP"
                 "OP_NIP"
                 "OP_OVER"
                 "OP_PICK"
                 "OP_ROLL"
                 "OP_ROT"
                 "OP_SWAP"
                 "OP_TUCK"
                 "OP_CAT"
                 "OP_SUBSTR"
                 "OP_LEFT"
                 "OP_RIGHT"
                 "OP_SIZE"
                 "OP_INVERT"
                 "OP_AND"
                 "OP_OR"
                 "OP_XOR"
                 "OP_EQUAL"
                 "OP_EQUALVERIFY"
                 "OP_RESERVED1"
                 "OP_RESERVED2"
                 "OP_1ADD"
                 "OP_1SUB"
                 "OP_2MUL"
                 "OP_2DIV"
                 "OP_NEGATE"
                 "OP_ABS"
                 "OP_NOT"
                 "OP_0NOTEQUAL"
                 "OP_ADD"
                 "OP_SUB"
                 "OP_MUL"
                 "OP_DIV"
                 "OP_MOD"
                 "OP_LSHIFT"
                 "OP_RSHIFT"
                 "OP_BOOLAND"
                 "OP_BOOLOR"
                 "OP_NUMEQUAL"
                 "OP_NUMEQUALVERIFY"
                 "OP_NUMNOTEQUAL"
                 "OP_LESSTHAN"
                 "OP_GREATERTHAN"
                 "OP_LESSTHANOREQUAL"
                 "OP_GREATERTHANOREQUAL"
                 "OP_MIN"
                 "OP_MAX"
                 "OP_WITHIN"
                 "OP_RIPEMD160"
                 "OP_SHA1"
                 "OP_SHA256"
                 "OP_HASH160"
                 "OP_HASH256"
                 "OP_CODESEPARATOR"
                 "OP_CHECKSIG"
                 "OP_CHECKSIGVERIFY"
                 "OP_CHECKMULTISIG"
                 "OP_CHECKMULTISIGVERIFY"
                 "OP_NOP1"
                 "OP_NOP2"
                 "OP_CHECKLOCKTIMEVERIFY"
                 "OP_NOP3"
                 "OP_CHECKSEQUENCEVERIFY"
                 "OP_NOP4"
                 "OP_NOP5"
                 "OP_NOP6"
                 "OP_NOP7"
                 "OP_NOP8"
                 "OP_NOP9"
                 "OP_NOP10"
                 "OP_PUBKEYHASH"
                 "OP_PUBKEY"
                 "OP_INVALIDOPCODE"]})

(defmacro.js wif-encode
  "encodes a wallet interchange format"
  {:added "4.0"}
  [version private-key compressed]
  (list '. 'WIF (list 'encode {:version version
                               :privateKey  private-key
                               :compressed compressed})))

(defmacro.js wif-decode
  "decodes a wallet interchange format"
  {:added "4.0"}
  [s]
  (list '. 'WIF (list 'decode s)))

(defmacro.js pair-from-wif
  "gets public/private pair from wif"
  {:added "4.0"}
  [s & [network]]
  (list '. (list '(. ECPairFactory default) 'TinySecp256k1)
        (list 'fromWIF s network)))

(defmacro.js pair-from-random
  "makes a random key"
  {:added "4.0"}
  []
  (list '. (list '(. ECPairFactory default) 'TinySecp256k1)
        (list 'makeRandom)))

(defn.js sign-message
  "signs a message given wif"
  {:added "4.0"}
  [message wif network options]
  (var pair (-/pair-from-wif wif network))
  (var signature (. BitcoinMessage (sign message
                                         (. pair privateKey)
                                         true
                                         (. network messagePrefix)
                                         options)))
  (return (. signature (toString "base64"))))

(defn.js verify-message
  "verifies a message given address and signature"
  {:added "4.0"}
  [message address signature network]
  (return (. BitcoinMessage (verify message
                                    address
                                    signature
                                    (. network messagePrefix)
                                    false))))

(defn.js account-from-random
  "generates an account from random"
  {:added "4.0"}
  [network]
  (var pair (-/pair-from-random))
  (var wif  (-/wif-encode (. network wif) (. pair privateKey) true))
  (return (-> (. Bitcoin payments
                 (p2pkh {:pubkey (. pair publicKey)
                         :network network}))
              (k/obj-map 
               (fn:> [x]
                 (:? (== "Buffer" (k/type-native x))
                     (. x (toString "hex"))
                     x)))
              (k/obj-assign {:prvkey (. pair privateKey (toString "hex"))
                             :wif wif}))))

(defn.js ^{:arglists '([{:keys [network
                                fee-per-kb
                                from-address
                                from-key
                                from-inputs
                                to-address]}])}
  build-sweep-tx
  "builds a sweep tx"
  {:added "4.0"}
  [m]
  (var #{network
         fee-per-kb
         signing-key
         from-wif
         from-address
         from-inputs
         to-address} m)
  (var est-kbs  (/ (* 55 (+ 1 (* 2 (k/len from-inputs))))
                   1000))
  (var multiple 100000000)
  (var fee      (j/round (* fee-per-kb est-kbs multiple)))
  (var to-total (j/round (- (* (k/arr-foldl
                                from-inputs
                                (fn [total input]
                                  (return
                                   (+ total (Number (. input value)))))
                                0)
                               multiple)
                            fee)))
  (var secret-pair (. Bitcoin ECPair (fromWIF from-wif network)))
  (var dtx (new Bitcoin.TransactionBuilder network))
  (k/for:array [[i input] from-inputs]
    (. dtx (addInput  (. input txid)
                      (. input output-no))))
  
  (. dtx (addOutput to-address to-total))

  (k/for:array [[i input] from-inputs]
    (. dtx (sign i secret-pair)))
  
  (return (. dtx (build) (toHex))))

(defn.js ^{:arglists '([{:keys [network
                                fee-per-kb
                                amount
                                from-address
                                from-key
                                from-inputs
                                to-address]}])}
  build-payment-tx
  "builds a payment tx"
  {:added "4.0"}
  [m]
  (var #{network
         fee-per-kb
         amount
         from-wif
         from-address
         from-inputs
         to-address} m)
  (var est-kbs  (/ (* 110 (+ 1 (k/len from-inputs)))
                   1000))
  (var multiple 100000000)
  (var fee       (j/round (* fee-per-kb est-kbs multiple)))
  (var to-total  (j/round (* amount multiple)))
  
  (var to-change (j/round (- (* (k/arr-foldl
                                 from-inputs
                                 (fn [total input]
                                   (return
                                    (+ total (Number (. input value)))))
                                 0)
                                multiple)
                             to-total
                             fee)))
  (var secret-pair (. Bitcoin ECPair (fromWIF from-wif network)))
  (var dtx (new Bitcoin.TransactionBuilder network))
  (k/for:array [[i input] from-inputs]
    (. dtx (addInput  (. input txid)
                      (. input output-no))))

  (. dtx (addOutput from-address to-change))
  (. dtx (addOutput to-address to-total))
  
  (k/for:array [[i input] from-inputs]
    (. dtx (sign i secret-pair)))
  
  (return (. dtx (build) (toHex))))



(comment
  in-address
  in-
  in-amount
  in-tx
  in-tx-output-no
  in-wif
  out-address
  out-amount
  )

(comment
  fee-per-kb
  (!.js
   Bitcoin.crypto.sha256)

  (!.js
   (k/obj-map Bitcoin k/obj-keys))

  (!.js
   (k/obj-map Coininfo k/obj-keys))
  
  
  ["address"
   "crypto"
   "networks"
   "payments" "script" "Block" "Psbt" "opcodes" "Transaction"]
  
  (!.js
   (:= Coininfo (require "coininfo")))
  
  (!.js
   (:= Bitcoin (require "bitcoinjs-lib"))))
