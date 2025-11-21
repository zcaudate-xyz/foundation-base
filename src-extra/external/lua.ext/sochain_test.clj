(ns lua.ext.sochain-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]
            [std.json :as json]
            [xt.lang.base-notify :as notify]))

(l/script- :lua
  {:runtime :basic
   :config {:program :resty}
   :require [[xt.lang.base-lib :as k]
             [xt.lang.event-log :as event-log]
             [xt.lang.base-repl :as repl]
             [lua.nginx :as n]
             [lua.nginx.task :as t]
             [lua.nginx.http-client :as http]
             [lua.ext.sochain :as sochain]]})

(fact:global
 {:setup    [(l/rt:restart)
             (l/rt:scaffold :lua)]
  :teardown [(l/rt:stop)]})

^{:refer lua.ext.sochain/create-pusher-defaults :added "0.1"}
(fact "creates connection defaults"
  ^:hidden
  
  (sochain/create-pusher-defaults)
  => {"host" "pusher.chain.so",
      "port" 443,
      "app_key" "e9f5cc20074501ca7395"})

^{:refer lua.ext.sochain/create-pusher :added "0.1"
  :setup [(l/rt:restart)]}
(fact "creates the connection"
  ^:hidden

  (sochain/create-pusher)
  => (contains-in
      {"::" "pusher",
       "history" map?,
       "active" false,
       "options" {"protocol" "7",
                  "host" "pusher.chain.so",
                  "port" 443,
                  "version" "4.0.1",
                  "client" "lua-pusher",
                  "scheme" "wss",
                  "app_key" "e9f5cc20074501ca7395"}}))

^{:refer lua.ext.sochain/test-pusher :added "4.0"}
(fact "tests that pusher is working"
  ^:hidden
  
  (sochain/test-pusher)
  => true)

^{:refer lua.ext.sochain/subscribe-address :added "0.1"
  :setup [(l/rt:restart)]}
(fact "subscribes to a events on an address"
  ^:hidden

  (notify/wait-on [:lua 5000]
   (var conn (sochain/create-pusher true))
   (sochain/subscribe-address conn
                                   "DOGETEST"
                                   "naumocEu7HMf4z2CTQRp9NWpT8JGrYaYqp")
   (event-log/add-listener (. conn history)
                           "test"
                           (fn [id m t]
                             (when (== (. m event)
                                       "pusher_internal:subscription_succeeded")
                               (repl/notify [id m])))))
  => (contains-in
      ["test"
       {"event" "pusher_internal:subscription_succeeded",
        "t" integer?
        "type" "event",
        "channel"
        "address_dogetest_naumocEu7HMf4z2CTQRp9NWpT8JGrYaYqp",
        "data" {}}]))

^{:refer lua.ext.sochain/subscribe-network :added "0.1"
  :setup [(l/rt:restart)]}
(fact "subscribes to events on the network"
  ^:hidden
  
  (notify/wait-on [:lua 10000]
    (var conn (sochain/create-pusher true))
    (sochain/subscribe-network conn "BTC")
    (sochain/set-network-listener conn "BTC"
                                       nil
                                       (repl/>notify)))
  => map?)

^{:refer lua.ext.sochain/get-response :added "0.1"}
(fact "helper function for response")

^{:refer lua.ext.sochain/get-request :added "0.1"}
(fact "helper function for sochain request")

^{:refer lua.ext.sochain/get-block-info :added "0.1"
  :setup [(l/rt:restart)]}
(fact "gets the block info"
  ^:hidden
  
  (notify/wait-on [:lua 5000]
    (repl/notify (sochain/get-block-info "DOGETEST"
                                         "b69d00af58d622b1e1e313757cd77d117b38639e9a502bb43db30d0b97f47f83")))
  => (contains-in
      {"status" "success",
       "data"
       {"txs" vector?
        "network" "DOGETEST",
        "blockhash" string?
        "is_orphan" false,
        "previous_blockhash" string?
        "mining_difficulty" string?
        "merkleroot" string?
        "block_no" 4029232}}))

^{:refer lua.ext.sochain/get-network-info :added "0.1"}
(fact "gets the current network info"
  ^:hidden
  
  (notify/wait-on [:lua 5000]
    (repl/notify (sochain/get-network-info "DOGETEST")))
  => (contains-in
      {"status" "success",
       "data"
       {"unconfirmed_txs" integer?
        "url" "https://dogecoin.com",
        "acronym" "DOGETEST",
        "blocks" integer?
        "name" "Dogecoin Testnet",
        "network" "DOGETEST",
        "symbol_htmlcode" "&#270;T",
        "mining_difficulty" string?
        "price_base" "BTC",
        "price_update_time" integer?
        "price" "0.00000000",
        "hashrate" string?}}))

^{:refer lua.ext.sochain/get-network-block :added "0.1"}
(fact "gets the current block on the network"
  ^:hidden
  
  (notify/wait-on [:lua 5000]
    (repl/notify (sochain/get-network-block "DOGETEST")))
  => (contains-in
      {"status" "success",
       "data"
       {"next_blockhash" nil,
        "confirmations" integer?
        "txs" vector?
        "network" "DOGETEST",
        "blockhash" string?
        "time" integer?
        "is_orphan" false,
        "previous_blockhash" string?
        "mining_difficulty" string?
        "merkleroot" string?
        "block_no" integer?
        "size" integer?}}))

^{:refer lua.ext.sochain/get-address-info :added "0.1"}
(fact "gets address information"
  ^:hidden
  
  (notify/wait-on [:lua 5000]
    (repl/notify (sochain/get-address-info "DOGETEST"
                                                "naumocEu7HMf4z2CTQRp9NWpT8JGrYaYqp")))
  => (contains-in
      {"status" "success",
       "data" {"network" "DOGETEST",
               "address" "naumocEu7HMf4z2CTQRp9NWpT8JGrYaYqp",
               "confirmed_balance" string?
               "unconfirmed_balance" string?}}))

^{:refer lua.ext.sochain/get-tx-info :added "0.1"}
(fact "gets transaction info"
  ^:hidden
  
  (notify/wait-on [:lua 5000]
    (repl/notify
     (sochain/get-tx-info
      "DOGETEST"
      "2c6900ee6aa8d2db8b2ec73624d8f171c5cda493a784e3a4ae11b2edfbde735c")))
  =>
  (contains-in
   {"status" "success",
    "data"
    {"network_fee" "0.00336000",
     "tx_hex"
     "01000000018beb8400b1508fadc0989e7028aef7c12a093338277eaf20a1c1240923eec71101000000d90047304402207c965afe8a28b0378b61b6c3fbd21f0a9e30f13deaa79db709a9b87fbafac5ef022044586a8df58c0f6b20eaa6326d22b677b9e1749662e5a0e901a488f1016d2f0901473044022038da17f82b557fbef190e6e3ec3a21a273413957e97b5b19c51b3c7b787e9e960220511ff4628d4459e12311fe37c5949d0f77de26bdeb7fe017d53c2bc7f676d8b30147522102eb26cd768f612070c544d96c78e3bfd8425473494f8eb23d5de464b0fe5bd7d92102c4ecc73372c3d62008fe2963d4064c01547421ef6a47c756df41d191156b819252aeffffffff0200e1f505000000001976a91449af7a07cd4c366ab7419cede82db2650c05328588acf07e56761400000017a914bb185e9c0e9afd1955ebfe7f695d994bbf966b338700000000",
     "confirmations" integer?
     "vsize" 334,
     "network" "DOGETEST",
     "outputs"
     [{"value" "1.00000000",
       "script"
       "OP_DUP OP_HASH160 49af7a07cd4c366ab7419cede82db2650c053285 OP_EQUALVERIFY OP_CHECKSIG",
       "address" "naumocEu7HMf4z2CTQRp9NWpT8JGrYaYqp",
       "type" "pubkeyhash",
       "output_no" 0}
      {"value" "878.84726000",
       "script"
       "OP_HASH160 bb185e9c0e9afd1955ebfe7f695d994bbf966b33 OP_EQUAL",
       "address" "2NAJVYPCZ4CFbTucwZ457YLR34RwijqtCLL",
       "type" "scripthash",
       "output_no" 1}],
     "blockhash"
     "d8788e46ab8ee516506d31638024fb34fd56b9ce4ae0ea795578e19ff414e02c",
     "locktime" 0,
     "time" 1662952439,
     "version" 1,
     "inputs"
     [{"witness" nil,
       "sequence" 4294967295,
       "value" "879.85062000",
       "script"
       "0 304402207c965afe8a28b0378b61b6c3fbd21f0a9e30f13deaa79db709a9b87fbafac5ef022044586a8df58c0f6b20eaa6326d22b677b9e1749662e5a0e901a488f1016d2f0901 3044022038da17f82b557fbef190e6e3ec3a21a273413957e97b5b19c51b3c7b787e9e960220511ff4628d4459e12311fe37c5949d0f77de26bdeb7fe017d53c2bc7f676d8b301 522102eb26cd768f612070c544d96c78e3bfd8425473494f8eb23d5de464b0fe5bd7d92102c4ecc73372c3d62008fe2963d4064c01547421ef6a47c756df41d191156b819252ae",
       "from_output"
       {"txid"
        "11c7ee230924c1a120af7e273833092ac1f7ae28709e98c0ad8f50b10084eb8b",
        "output_no" 1},
       "address" "2NAJVYPCZ4CFbTucwZ457YLR34RwijqtCLL",
       "input_no" 0,
       "type" "scripthash"}],
     "size" 334,
     "txid"
     "2c6900ee6aa8d2db8b2ec73624d8f171c5cda493a784e3a4ae11b2edfbde735c"}}))

^{:refer lua.ext.sochain/get-tx-unspent :added "0.1"
  :setup [(l/rt:restart)]}
(fact "gets the unspent transactions"
  ^:hidden
  
  (notify/wait-on [:lua 5000]
    (repl/notify
     (sochain/get-tx-unspent "DOGETEST"
                                  "naumocEu7HMf4z2CTQRp9NWpT8JGrYaYqp")))
  => (contains-in
      {"status" "success",
       "data"
       {"txs" vector?
        "network" "DOGETEST",
        "address" "naumocEu7HMf4z2CTQRp9NWpT8JGrYaYqp"}}))

^{:refer lua.ext.sochain/get-tx-received :added "4.0"
  :setup [(l/rt:restart)]}
(fact "gets all received txs")

^{:refer lua.ext.sochain/get-tx-last :added "4.0"}
(fact "gets last unspent tx")

^{:refer lua.ext.sochain/send-tx :added "0.1"}
(fact "sends a transaction to the network"
  ^:hidden
  
  (notify/wait-on [:lua 5000]
    (repl/notify
     (sochain/send-tx "DOGETEST"
                      "00000000")))
  => {"status" "fail",
      "data"
      {"tx_hex"
       "A valid signed transaction hexadecimal string is required. Please check if all inputs in the given transactions are still available to spend. See the \"Is Tx Output Spent?\" API call for reference.",
       "network" "Network is required (DOGE, DOGETEST, ...)"}})

^{:refer lua.ext.sochain/set-network-listener :added "0.1"
  :setup [(l/rt:restart)]}
(fact "sets the network listener"
  ^:hidden

  (notify/wait-on [:lua 10000]
    (var conn (sochain/create-pusher true))
    (sochain/subscribe-network conn "BTC")
    (sochain/set-network-listener conn "BTC"
                                       nil
                                       (repl/>notify)))
  => (contains-in
      {"event" "tx_update",
       "t" integer?
       "type" "event",
       "channel" "blockchain_update_btc",
       "data"
       {"value"
        {"unconfirmed_txs" integer?
         "total_inputs" string?
         "txpm" integer?
         "total_outputs" string?
         "time" integer?
         "size" integer?
         "txid" string?
         "sent_value" number?
         "hashrate" number?},
        "type" "tx"}})

  #_#_#_(do (l/rt:restart)
      (notify/wait-on [:lua 10000]
        (var conn (sochain/create-pusher true))
        (sochain/subscribe-network conn "DOGE")
        (sochain/set-network-listener conn "DOGE"
                                      nil
                                      (repl/>notify))))
  => (contains-in
      {"event" "tx_update",
       "t" integer?
       "type" "event",
       "channel" "blockchain_update_doge",
       "data"
       {"value"
        {"unconfirmed_txs" integer?
         "total_inputs" string?
         "txpm" integer?
         "total_outputs" string?
         "time" integer?
         "size" integer?
         "txid" string?
         "sent_value" number?
         "hashrate" number?},
        "type" "tx"}}))

^{:refer lua.ext.sochain/set-address-listener :added "0.1"
  :setup [(l/rt:restart)]}
(fact "sets the address listener")

(comment

  (!.lua
   (:= CONN (sochain/create-pusher true))
   (sochain/subscribe-balance-address
    CONN "DOGETEST" "naumocEu7HMf4z2CTQRp9NWpT8JGrYaYqp")
   (sochain/subscribe-balance-address
    CONN "DOGETEST" "nksoHPWRa81rYMyApnjeNEgRwzw48Tf5t5")
   (sochain/subscribe-balance-address
    CONN "DOGETEST" "np4H9Lv3N8MaEX99MJ9wUjsLfxs26zRxwa"))
  
  (!.lua
   )
  
  (k/trace-log)
  
  (!.lua
   (. CONN history))
  (!.lua
   (. CONN connection status))
  (!.lua
   (k/obj-keys (. CONN connection threads)))
  (!.lua
   (k/get-data (. CONN connection)))
  (!.lua
   (event-log/get-tail (. CONN history)
                       10))
  
  
  
  (!.lua
   (k/get-spec (. CONN connection)))
  {"threads" {"listener" "thread", "heartbeat" "thread"},
   "last_received" "number",
   "status" "string",
   "timeout" "boolean",
   "socket_id" "string",
   "ws" {"sock" ["userdata"], "max_payload_len" "number"}}
  
  (!.lua
   (+ 1 2 3))
  
  )
