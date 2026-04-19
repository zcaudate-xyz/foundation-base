(ns js.lib.bitcoin-transfer-test
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
              [xt.lang.common-lib :as k]]
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
   (var dtx (new Bitcoin.TransactionBuilder (@! +dogetestnet+)))
   (var fee (* 0.01 0.3 100000000 ))
   (. dtx (addInput "57c449f551e6d790a289c0a548ae735d4c96f7366d01c60530121f105eed0c3c"
                    0))
   (. dtx (addOutput "2NAJVYPCZ4CFbTucwZ457YLR34RwijqtCLL"
                     50000000))
   (. dtx (addOutput "naumocEu7HMf4z2CTQRp9NWpT8JGrYaYqp"
                     (- (* (Number "10.00000000")
                           100000000)
                        50000000
                        fee)))
   (var pair (Bitcoin.ECPair.fromWIF (@! (get +test-address+
                                              "wif"))
                                     (@! +dogetestnet+)))
   (. dtx (sign 0 pair))
   (. dtx (build) (toHex)))
  
  
  "02000000013c0ced5e101f123005c6016d36f7964c5d73ae48a5c089a290d7e651f549c457000000006b4830450221008ef3a9ee28c2a2a452c337cd0488a8e170e2004a43e8739e9b1dfd616b4b0bd0022038ef24a37327937fd7d69bae264f9457ce2c0574aee4d089899df09bbc2358fa01210338042bc49430622c792856213984b4b086c8c1b625715fd7a15edb0d991e214fffffffff0280f0fa020000000017a914bb185e9c0e9afd1955ebfe7f695d994bbf966b3387a0459b38000000001976a91449af7a07cd4c366ab7419cede82db2650c05328588ac00000000"



  
  (!.js
   (var pair (Bitcoin.ECPair.fromWIF (@! (get +test-address+
                                              "wif"))
                                     (@! +dogetestnet+)))
   pair.network)

  

  (!.js
   (var dtx (new Bitcoin.TransactionBuilder (@! +dogetestnet+)))
   (var fee (* 0.01 0.3 100000000))
   (. dtx (addInput "8c9b8da5073decf49c6c6822ef7250252e96ddfe9f3a494ddefb07ffb8e55712"
                    1))
   (. dtx (addOutput "2NAJVYPCZ4CFbTucwZ457YLR34RwijqtCLL"
                     50000000))
   (. dtx (addOutput "naumocEu7HMf4z2CTQRp9NWpT8JGrYaYqp"
                     (- (* (Number "9.49700000")
                           100000000)
                        50000000
                        fee)))
   (var pair (Bitcoin.ECPair.fromWIF (@! (get +test-address+
                                              "wif"))
                                     (@! +dogetestnet+)))
   (. dtx (sign 0 pair))
   (. dtx (build) (toHex)))
  "02000000011257e5b8ff07fbde4d493a9ffedd962e255072ef22686c9cf4ec3d07a58d9b8c010000006b4830450221009ef27cb4dd20e90762c84a08228805c45117382d9cf6ec4e5b007b0400a60f5b02202dd96af56625aa8e08f03738bb468d34b4a31bfce384d87f739aa35cbbb9fca401210338042bc49430622c792856213984b4b086c8c1b625715fd7a15edb0d991e214fffffffff0280f0fa020000000017a914bb185e9c0e9afd1955ebfe7f695d994bbf966b338740c19b35000000001976a91449af7a07cd4c366ab7419cede82db2650c05328588ac00000000"
  
  
  
  )
