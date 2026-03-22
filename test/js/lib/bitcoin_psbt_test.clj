(ns js.lib.bitcoin-psbt-test
  (:require [std.lang :as l])
  (:use code.test))

(l/script- :js
  {:runtime :basic
   :require  [[js.lib.bitcoin :as bc :include [:ecc     
                                               :ecpair  
                                               :bip32
                                               :bip39
                                               :wif
                                               :safe-buffer]]
              [xt.lang.base-lib :as k]]
   :export  [MODULE]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:scaffold :js)
             (!.js
              (:= Buffer (. SafeBuffer Buffer)))]
  :teardown [(l/rt:stop)]})

(def +dogetestnet+
  {:messagePrefix "\\x19Dogecoin Signed Message:\\n",
   :bech32 "td"
   :bip44 3
   :bip32 {:public 0x043587cf,
           :private 0x04358394},
   :pubKeyHash 0x71,
   :scriptHash 0xc4,
   :wif 0xf1})

(def +test-address+
  {"wif"     "chDWHF7Hg6tZzrn1XYi5VFrqidNY51Bo8LEhPmLkHh4syZV3xfTG"
   "address" "naumocEu7HMf4z2CTQRp9NWpT8JGrYaYqp",
   "pubkey"  "0338042bc49430622c792856213984b4b086c8c1b625715fd7a15edb0d991e214f"})

(comment
  ;;
  ;; WORKING USING bitcoinjs-lib@5.2 with patch on 1011 line of transaction-builder.js
  ;; /usr/lib/node_modules/bitcoinjs-lib/src/transaction_builder.js 

  
  (!.js
   (var pair (Bitcoin.ECPair.fromWIF (@! (get +test-address+
                                              "wif"))
                                     (@! +dogetestnet+)))
   (var psbt (new Bitcoin.Psbt (@! +dogetestnet+)))
   (var fee (* 0.01 0.2 100000000))
   (. psbt (addInput
           {:hash "8c9b8da5073decf49c6c6822ef7250252e96ddfe9f3a494ddefb07ffb8e55712"
            :index 0
            :nonWitnessUtxo
            (Buffer.from "02000000013c0ced5e101f123005c6016d36f7964c5d73ae48a5c089a290d7e651f549c457000000006b4830450221008ef3a9ee28c2a2a452c337cd0488a8e170e2004a43e8739e9b1dfd616b4b0bd0022038ef24a37327937fd7d69bae264f9457ce2c0574aee4d089899df09bbc2358fa01210338042bc49430622c792856213984b4b086c8c1b625715fd7a15edb0d991e214fffffffff0280f0fa020000000017a914bb185e9c0e9afd1955ebfe7f695d994bbf966b3387a0459b38000000001976a91449af7a07cd4c366ab7419cede82db2650c05328588ac00000000" "hex")}))
   (. psbt (addOutput
            {:script (Buffer.from "2NAJVYPCZ4CFbTucwZ457YLR34RwijqtCLL" "hex")
             :value  50000000}))
   (. psbt (addOutput
            {:script (Buffer.from "naumocEu7HMf4z2CTQRp9NWpT8JGrYaYqp" "hex")
             :value  (- (* (Number "10.00000000")
                           100000000)
                        50000000
                        fee)}))
   (. psbt (signInput 0 pair))
   (. psbt (validateSignaturesOfInput 0))
   (. psbt (finalizeAllInputs))
   (. psbt (extractTransaction) (toHex)))
  
  
  )

(comment
  ;;
  ;; WORKING USING bitcoinjs-lib@5.2 with patch on 1011 line of transaction-builder.js
  ;; /usr/lib/node_modules/bitcoinjs-lib/src/transaction_builder.js 

  
  (!.js
   (var pair (Bitcoin.ECPair.fromWIF (@! (get +test-address+
                                              "wif"))
                                     (@! +dogetestnet+)))
   (var psbt (new Bitcoin.Psbt (@! +dogetestnet+)))
   (var fee (* 0.01 0.3 100000000 ))
   (. psbt (addInput
           {:hash "57c449f551e6d790a289c0a548ae735d4c96f7366d01c60530121f105eed0c3c"
            :index 0
            :nonWitnessUtxo
            (Buffer.from "010000000153780c081508aa92f6902b5193ae15967961f82c8a6f1ca012465ec61368a7b301000000d9004730440220078f30c73d2b6cd48cb09853dd1090cdf3800c5d1259029321df61580518dd37022042e59fc46304ed052290a885592c76df720d962b225e9e3373af7076b422b8dc01473044022014923558c9298b58c5a2d8684d6057a1f61e7ac84a1095cfdacb341c4ca0880502205c575e0ce8746c80a9cd9f633bdceb05dd3e7700a06fbe32c6ba9b08f9324c730147522102eb26cd768f612070c544d96c78e3bfd8425473494f8eb23d5de464b0fe5bd7d92102c4ecc73372c3d62008fe2963d4064c01547421ef6a47c756df41d191156b819252aeffffffff0300ca9a3b000000001976a91449af7a07cd4c366ab7419cede82db2650c05328588ac809698000000000017a9148f94135c7d879a57a6e8a2e7b14343479a9eeb268730f756e81600000017a914bb185e9c0e9afd1955ebfe7f695d994bbf966b338700000000" "hex")}))
   (. psbt (addOutput
            {:script (Buffer.from "2NAJVYPCZ4CFbTucwZ457YLR34RwijqtCLL" "hex")
             :value  50000000}))
   (. psbt (addOutput
            {:script (Buffer.from "naumocEu7HMf4z2CTQRp9NWpT8JGrYaYqp" "hex")
             :value  (- (* (Number "10.00000000")
                           100000000)
                        50000000
                        fee)}))
   (. psbt (signInput 0 pair))
   (. psbt (validateSignaturesOfInput 0))
   (. psbt (finalizeAllInputs))
   (. psbt (extractTransaction) (toHex)))
  "02000000013c0ced5e101f123005c6016d36f7964c5d73ae48a5c089a290d7e651f549c457000000006a4730440220137b63e16177d5a694ef25658511e335c5848f688b99693889d54b993bb7685002204a60fc99a8c7dedfbccf46b35912888dda6d4ccd1116387f51577a71836ae84901210338042bc49430622c792856213984b4b086c8c1b625715fd7a15edb0d991e214fffffffff0280f0fa020000000000a0459b38000000000000000000")
