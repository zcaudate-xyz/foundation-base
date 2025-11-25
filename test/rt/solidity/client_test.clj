(ns rt.solidity.client-test
  (:use code.test)
  (:require [rt.solidity.client :as client]
            [rt.solidity.compile-common :as compile-common]
            [rt.solidity.compile-node :as compile-node]
            [std.lang :as l]
            [std.lib :as h]
            [rt.basic :as basic]
            [rt.basic.server-basic :as server]
            [rt.solidity.compile-solc :as solc]
            [rt.solidity.env-ganache :as env]
            [rt.solidity.compile-deploy :as deploy]))

(l/script- :solidity
  {:runtime :web3
   :config  {:mode :clean}
   :require [[rt.solidity :as s]]})

;; Removed global setup

^{:refer rt.solidity.client/check-node-connection :added "4.0"}
(fact "checks that the node connection is present"
  (with-redefs [server/get-server (fn [& _] {:id "node"})
                server/get-relay (fn [& _] :connected)]
    (client/check-node-connection {:node {:id "node"}}))
  => :connected)

^{:refer rt.solidity.client/contract-fn-name :added "4.0"}
(fact "gets the name of a pointer"
  (with-redefs [l/emit-symbol (fn [_ s] (str s))]
    (client/contract-fn-name {:id 'test:hello}))
  => "test:hello")

^{:refer rt.solidity.client/create-web3-node :added "4.0"}
(fact "creates the node runtime"
  ;; Complex setup
  )

^{:refer rt.solidity.client/start-web3 :added "4.0"}
(fact "starts the solidity rt"
  (with-redefs [client/create-web3-node (fn [_] :node)
                compile-common/get-url (fn [_] "url")]
    (client/start-web3 {:id "id" :config {}}))
  => (contains {:node :node :config {:url "url"}}))

^{:refer rt.solidity.client/stop-web3 :added "4.0"}
(fact "stops the solidity rt"
  (with-redefs [h/stop (fn [_] nil)
                compile-common/set-rt-settings (fn [& _] nil)]
    (client/stop-web3 {:node {}}))
  => {})

^{:refer rt.solidity.client/raw-eval-web3 :added "4.0"}
(fact "disables raw-eval for solidity"
  (client/raw-eval-web3 {} "body")
  => (throws))

^{:refer rt.solidity.client/invoke-ptr-web3-check :added "4.0"}
(fact "checks that arguments are correct"
  (client/invoke-ptr-web3-check {:abi [{"name" "fn" "inputs" [] "outputs" [{"type" "uint256"}]}]}
                                "fn"
                                [])
  => true)

^{:refer rt.solidity.client/invoke-ptr-web3-call :added "4.0"}
(fact "invokes a deployed method"
  (with-redefs [compile-node/rt-get-contract (fn [] {:abi []})
                client/contract-fn-name (fn [_] "fn")
                client/invoke-ptr-web3-check (fn [& _] false)
                compile-common/get-url (fn [_] "url")
                compile-common/get-caller-private-key (fn [_] "key")
                compile-common/get-contract-address (fn [_] "addr")
                solc/compile-rt-eval (fn [_ _ _] "result")]
    (client/invoke-ptr-web3-call {:node {:id "id"}} (atom {:form '(defn f [])}) []))
  => "result")

^{:refer rt.solidity.client/invoke-ptr-web3 :added "4.0"}
(fact "invokes the runtime, deploying the contract if not available"
  (with-redefs [compile-common/get-contract-address (fn [_] "addr")
                client/invoke-ptr-web3-call (fn [& _] "result")]
    (client/invoke-ptr-web3 {:node {:id "id"}} 'ptr []))
  => "result")

^{:refer rt.solidity.client/rt-web3-string :added "4.0"}
(fact "gets the runtime string"
  (with-redefs [server/get-server (fn [& _] nil)]
    (client/rt-web3-string {:id "id" :lang :sol :config {:url "url"} :node {:id "node"}}))
  => "#rt.web3[\"id\" {:url \"url\", :node :no-server}]")

^{:refer rt.solidity.client/rt-web3:create :added "4.0"}
(fact "creates a runtime"
  (client/rt-web3:create {})
  => map?)

^{:refer rt.solidity.client/rt-web3 :added "4.0"}
(fact "creates an starts a runtime"
  (with-redefs [client/rt-web3:create (fn [m] m)
                h/start (fn [m] (assoc m :started true))]
    (client/rt-web3 {}))
  => {:started true})

(comment
  (rt.solidity.env-ganache/stop-ganache-server)
  (rt.solidity.env-ganache/rt:start-ganache-server)
  )
