(ns js.lib.bitcoin-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script- :js
  {:runtime :basic
   :require  [[js.lib.bitcoin :as bc :include [:fn
                                               :ecc     
                                               :ecpair  
                                               :bip32
                                               :bip39
                                               :wif
                                               :message
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

(def +dogenet+
  {:messagePrefix "\\x19Dogecoin Signed Message:\\n",
   :bech32 "dc"
   :bip44 3
   :bip32 {:public 0x02facafd,
           :private 0x02fac398},
   :pubKeyHash 0x1e,
   :scriptHash 0x16,
   :wif 0x9e})

^{:refer js.lib.bitcoin/wif-encode :added "4.0" :unchecked true}
(fact "encodes a wallet interchange format"
  ^:hidden
  
  ;;
  ;; BITCOIN
  ;;
  
  (!.js
   (var privateKey
        (Buffer.from "0000000000000000000000000000000000000000000000000000000000000001"
                     "hex"))
   
   (bc/wif-encode 128 privateKey true))
  => "KwDiBf89QgGbjEhKnhXJuH7LrciVrZi3qYjgd9M7rFU73sVHnoWn"
  
  ;;
  ;; DOGECOIN TESTNET
  ;;
  
  (!.js
    (var privateKey
         (Buffer.from "5ef54e669cad68ae73ff6f25b80a28ac3be203db0fdcf6a7894f88b0f3b99c53"
                      "hex"))
    (bc/wif-encode 0xf1 privateKey true))
  => "chvYoVjuvRgDxFZrm4TZRKKh1hoRdMKb8XnaWk1gpYBq27FSwyK5")

^{:refer js.lib.bitcoin/wif-decode :added "4.0" :unchecked true}
(fact "decodes a wallet interchange format"
  ^:hidden
  
  ;;
  ;; BITCOIN
  ;;
  (!.js
   (var #{privateKey
          version
          compressed}

        (bc/wif-decode "KwDiBf89QgGbjEhKnhXJuH7LrciVrZi3qYjgd9M7rFU73sVHnoWn"))
   {:version version
    :compressed compressed
    :key (. privateKey (toString "hex"))})
  => {"key" "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1",
      "version" 128,
      "compressed" true}
  
  
  
  ;;
  ;; DOGECOIN TESTNET
  ;;
  
  (!.js
   (var #{privateKey
          version
          compressed}
        (bc/wif-decode "chvYoVjuvRgDxFZrm4TZRKKh1hoRdMKb8XnaWk1gpYBq27FSwyK5"))
   {:version version
    :compressed compressed
    :key (. privateKey (toString "hex"))})
  => {"key" "94,245,78,102,156,173,104,174,115,255,111,37,184,10,40,172,59,226,3,219,15,220,246,167,137,79,136,176,243,185,156,83",
      "version" 241,
      "compressed" true})

^{:refer js.lib.bitcoin/pair-from-wif :added "4.0" :unchecked true}
(fact "gets public/private pair from wif"
  ^:hidden
  
  ;;
  ;; BITCOIN
  ;;

  (!.js
   (var pair (bc/pair-from-wif "KwDiBf89QgGbjEhKnhXJuH7LrciVrZi3qYjgd9M7rFU73sVHnoWn"))
   (k/obj-map (Bitcoin.payments.p2pkh {:pubkey (. pair publicKey)})
              (fn:> [x]
                (:? (== "Buffer" (k/type-native x))
                    (. x (toString "hex"))
                    x))))
  => (contains-in
      {"output" "76a914751e76e8199196d454941c45d1b3a323f1433bd688ac",
       "hash" "751e76e8199196d454941c45d1b3a323f1433bd6",
       "name" "p2pkh",
       "network" {"bip32" {"private" 76066276, "public" 76067358},
                  "messagePrefix" "Bitcoin Signed Message:\n",
                  "pubKeyHash" 0, "scriptHash" 5, "wif" 128,
                  "bech32" "bc"},
       "address" "1BgGZ9tcN4rm9KBzDn7KprQz87SZ26SAMH"})
  
  
  
  ;;
  ;; DOGECOIN TESTNET
  ;;
  
  (!.js
   (var pair (bc/pair-from-wif "chvYoVjuvRgDxFZrm4TZRKKh1hoRdMKb8XnaWk1gpYBq27FSwyK5"
                               (@! +dogetestnet+)))

   
   (k/obj-map (Bitcoin.payments.p2pkh {:pubkey (. pair publicKey)
                                       :network (@! +dogetestnet+)})
              (fn:> [x]
                (:? (== "Buffer" (k/type-native x))
                    (. x (toString "hex"))
                    x))))
  => (contains-in
      {"output" "76a9141a355bfb57c5cabc104014713c3a68ff9277c2ff88ac",
       "hash" "1a355bfb57c5cabc104014713c3a68ff9277c2ff",
       "name" "p2pkh",
       "network" {"bip32" {"private" 70615956, "public" 70617039},
                  "messagePrefix" "\\x19Dogecoin Signed Message:\\n",
                  "pubKeyHash" 113, "scriptHash" 196, "wif" 241, "bip44" 3, "bech32" "td"},
       "address" "nWajkjBzFoB4rb646NWqDRop5nmyP6WHBU", })
  
  
  ;;
  ;; WIF TO HEX
  ;;
  
  (!.js
   (var pair (bc/pair-from-wif "chvYoVjuvRgDxFZrm4TZRKKh1hoRdMKb8XnaWk1gpYBq27FSwyK5"
                               (@! +dogetestnet+)))
   
   
   (. pair publicKey
      (toString "hex")))
  => "2,196,236,199,51,114,195,214,32,8,254,41,99,212,6,76,1,84,116,33,239,106,71,199,86,223,65,209,145,21,107,129,146")

^{:refer js.lib.bitcoin/pair-from-random :added "4.0" :unchecked true}
(fact "makes a random key"
  ^:hidden
  
  (!.js
   (var pair (bc/pair-from-random))
   [(. pair publicKey
       (toString "hex"))
    (. pair privateKey
       (toString "hex"))])
  => (contains-in
      [string? ;; "0338042bc49430622c792856213984b4b086c8c1b625715fd7a15edb0d991e214f"
       string? ;; "49d80a6f9116dabd33222b0afd3a0e99d06d3ec09df29fde460aee7fc9013cd4"
       ])

  ;;
  ;; TO AND FROM WIF
  ;;
  
  (!.js
   (var privateKey
        (Buffer.from "49d80a6f9116dabd33222b0afd3a0e99d06d3ec09df29fde460aee7fc9013cd4"
                     "hex"))
   (bc/wif-encode (@! (:wif +dogetestnet+)) privateKey true))
  => "chDWHF7Hg6tZzrn1XYi5VFrqidNY51Bo8LEhPmLkHh4syZV3xfTG"

  (!.js
   (. (bc/wif-decode "chDWHF7Hg6tZzrn1XYi5VFrqidNY51Bo8LEhPmLkHh4syZV3xfTG")
      privateKey
      (toString "hex")))
  => "73,216,10,111,145,22,218,189,51,34,43,10,253,58,14,153,208,109,62,192,157,242,159,222,70,10,238,127,201,1,60,212"
  
  (!.js
   (var pair (bc/pair-from-wif "chDWHF7Hg6tZzrn1XYi5VFrqidNY51Bo8LEhPmLkHh4syZV3xfTG"
                               (@! +dogetestnet+)))

   
   (k/obj-map (Bitcoin.payments.p2pkh {:pubkey (. pair publicKey)
                                       :network (@! +dogetestnet+)})
              (fn:> [x]
                (:? (== "Buffer" (k/type-native x))
                    (. x (toString "hex"))
                    x))))
  => (contains-in
      {"output" "76a91449af7a07cd4c366ab7419cede82db2650c05328588ac",
       "hash" "49af7a07cd4c366ab7419cede82db2650c053285",
       "name" "p2pkh",
       "network" {"bip32" {"private" 70615956, "public" 70617039},
                  "messagePrefix" "\\x19Dogecoin Signed Message:\\n", "pubKeyHash" 113,
                  "scriptHash" 196, "wif" 241, "bip44" 3, "bech32" "td"},
       "address" "naumocEu7HMf4z2CTQRp9NWpT8JGrYaYqp"}))

^{:refer js.lib.bitcoin/sign-message :added "4.0" :unchecked true}
(fact "signs a message given wif"
  ^:hidden
  
  (!.js
   (bc/sign-message "HELLO WORLD"
                    "chDWHF7Hg6tZzrn1XYi5VFrqidNY51Bo8LEhPmLkHh4syZV3xfTG"
                    (@! +dogetestnet+)
                    {}))
  => "H7LxS73/Apo2srauD1oKKHXjKks9m9zuYLGWLuq6Kd6HVP2zjL2+IVNKkpvA9gPZ/FphtvaHUWMvlQimJE7VOBg=")

^{:refer js.lib.bitcoin/verify-message :added "4.0" :unchecked true}
(fact "verifies a message given address and signature"
  ^:hidden
  
  (!.js
   (bc/verify-message "HELLO WORLD"
                      "naumocEu7HMf4z2CTQRp9NWpT8JGrYaYqp"
                      "H7LxS73/Apo2srauD1oKKHXjKks9m9zuYLGWLuq6Kd6HVP2zjL2+IVNKkpvA9gPZ/FphtvaHUWMvlQimJE7VOBg="
                      (@! +dogetestnet+)))
  => true)

^{:refer js.lib.bitcoin/account-from-random :added "4.0" :unchecked true}
(fact "generates an account from random"
  ^:hidden
  
  (!.js
   (bc/account-from-random (@! +dogetestnet+)))
  => map?
  
  (comment
    {"output" "76a9144c4038c315eb884b8f2946889a0b3f1131e8983a88ac",
     "hash"   "4c4038c315eb884b8f2946889a0b3f1131e8983a",
     "wif"    "cmYwCpkZaAi8zN4jUPJtXtHLEQVz4pM7xks1UmonnXVCWPLxUVFj",
     "name"   "p2pkh",
     "network" {"bip32" {"private" 70615956, "public" 70617039},
                "messagePrefix" "\\x19Dogecoin Signed Message:\\n",
                "pubKeyHash" 113,
                "scriptHash" 196,
                "wif" 241
                "bip44" 3,
                "bech32" "td"},
     "address" "nb9LZ46HTVzqrbSNz8PhFugVbPo9ExJ92p",
     "pubkey"  "027083bbfdca24bbc6ac789355e57d64b190651b3009241292edce6925d73a5792",
     "prvkey"  "cb305a6c1efeea83ccbc27a70badc2e7010da52ccba2ebf388f371cd0a7455a4"})

  (!.js
   (bc/account-from-random (@! +dogenet+)))
  => map?
  
  (comment
    {"output" "76a914120d3ff9417bef2451db0c5825e191342d3d604088ac",
     "hash" "120d3ff9417bef2451db0c5825e191342d3d6040",
     "wif" "QPyZSgf7Xf5SaQw9rtH9sdYGfEGr8JyBFeVfwP4qdtxTKwTkJzAV",
     "name" "p2pkh",
     "network"
     {"bip32" {"private" 49988504, "public" 49990397},
      "messagePrefix" "\\x19Dogecoin Signed Message:\\n",
      "pubKeyHash" 30,
      "scriptHash" 22,
      "wif" 158,
      "bip44" 3,
      "bech32" "dc"},
     "address" "D6nYaL7e5MiLivJZfbzofHA5Eaa6xys1Aw",
     "pubkey"
     "02f11070857eb33a8a0221e0bef24b04d206f339e0059d9cf98158a4d5c0335a88",
     "prvkey"
     "289b26b32378c7d66d7aab7e3d4119004889c4947427c27cfdd17a03c8d4225d"}))

^{:refer js.lib.bitcoin/build-sweep-tx :added "4.0" :unchecked true}
(fact "builds a sweep tx"
  ^:hidden
  
  (!.js
   (bc/build-sweep-tx
    {:network (@! +dogetestnet+)
     :fee-per-kb 0.01
     :amount 1
     :to-address "naumocEu7HMf4z2CTQRp9NWpT8JGrYaYqp"
     :from-wif    "chvYoVjuvRgDxFZrm4TZRKKh1hoRdMKb8XnaWk1gpYBq27FSwyK5"
     :from-address "naumocEu7HMf4z2CTQRp9NWpT8JGrYaYqp"
     :from-inputs
     [{:value "111.48200000"
       :txid "ac581026e62c56d03f7050bbe5c45b77c5658e8dc4f9d7bb3364188a6c5fa832"
       :output-no 1}]}))
  => "020000000132a85f6c8a186433bbd7f9c48d8e65c5775bc4e5bb50703fd0562ce6261058ac010000006b483045022100fdb4b408b49e13c44d8a75d503c497303d64417f464e40028b9c7899c30553c802205a902c4c78da0b482894fb1cd8c6f979a056d511f4f429b86d7955f8125cc145012102c4ecc73372c3d62008fe2963d4064c01547421ef6a47c756df41d191156b8192ffffffff01b8837998020000001976a91449af7a07cd4c366ab7419cede82db2650c05328588ac00000000")

^{:refer js.lib.bitcoin/build-payment-tx :added "4.0" :unchecked true}
(fact "builds a payment tx"
  ^:hidden
  
  (!.js
   (bc/build-payment-tx
    {:network (@! +dogetestnet+)
     :fee-per-kb 0.01
     :amount 1
     :to-address "nksoHPWRa81rYMyApnjeNEgRwzw48Tf5t5"
     :from-wif    "chvYoVjuvRgDxFZrm4TZRKKh1hoRdMKb8XnaWk1gpYBq27FSwyK5"
     :from-address "naumocEu7HMf4z2CTQRp9NWpT8JGrYaYqp"
     :from-inputs
     [{:value "111.48200000"
       :txid "ac581026e62c56d03f7050bbe5c45b77c5658e8dc4f9d7bb3364188a6c5fa832"
       :output-no 1}]}))
  => "020000000132a85f6c8a186433bbd7f9c48d8e65c5775bc4e5bb50703fd0562ce6261058ac010000006b483045022100e2b1203e1ecf1d2f58c6ef09e83da64f35d06d0678f732920e5ff6b3e74eaf1c0220501b138997df4e1428fa48c58e77c3046d039e49aeec15d8a88e6be7991bee52012102c4ecc73372c3d62008fe2963d4064c01547421ef6a47c756df41d191156b8192ffffffff02e0cb8292020000001976a91449af7a07cd4c366ab7419cede82db2650c05328588ac00e1f505000000001976a914b701231e48d370aeed09d261dee64d4261ee999d88ac00000000")

(comment

  (!.js
   (bc/account-from-random (@! +dogetestnet+)))
  (!.js
   (. ECPairFactory default)))
