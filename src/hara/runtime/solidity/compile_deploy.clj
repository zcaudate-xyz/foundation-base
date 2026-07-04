(ns hara.runtime.solidity.compile-deploy
  (:require [js.lib.eth-bench :as eth-bench]
             [hara.runtime.basic :as basic]
             [hara.runtime.solidity.compile-common :as common]
             [hara.runtime.solidity.compile-solc :as solc]
             [hara.runtime.solidity.env-hardhat :as env]
             [hara.lang :as l]
             [std.lib.env :as env-lib]
             [std.lib.foundation :as f]
             [std.make.compile :as compile]
             [xt.lang.common-notify :as notify]))

;;
;; Deploys the contract
;;

(defn deploy-base
  "deploy abi"
  {:added "4.0"}
  [rt url contract initial-args]
  (let [{:keys [type id sha abi bytecode code]} contract
        form  (list `eth-bench/contract-deploy
                    url
                    (common/get-caller-private-key (:id rt))
                    abi
                    bytecode
                    initial-args
                    {})
        result (try (solc/compile-rt-eval rt form)
                    (catch clojure.lang.ExceptionInfo ex
                      
                      {:status false
                       :error (or (try
                                    (std.json/read
                                     (get-in (ex-data ex)
                                             [:err 0]))
                                    (catch Throwable t))
                                  (ex-data ex))})
                    (catch Throwable t t))
          {:strs [status
                  contractAddress]} result
         _ (cond (not status)
                 (do (not common/*suppress-errors*)
                     (env-lib/p url)
                     (env-lib/p code)
                     (env-lib/prn result)
                     (f/error "Compilation Error"
                              {:data result}))
                
                :else
                (do
                  (swap! env/+contracts+ assoc contractAddress [type id sha])
                  (when (not common/*temp*)
                    (common/update-rt-settings
                     (:id rt)
                     {:contract-address contractAddress}))))]
    result))

(defn deploy-pointer
  "deploys a pointer"
  {:added "4.0"}
  [rt url ptr]
  (let [contract (solc/create-pointer-entry rt ptr)]
    (deploy-base rt url contract [])))

(defn deploy-module
  "deploys a namespace on the blockchain"
  {:added "4.0"}
  [rt url & [input]]
  (let [input  (or input
                   (let [ns (.getName *ns*)]
                     (if-let [v (resolve (symbol (str ns) "+default-contract+"))]
                       @v
                       {:ns ns :name "Test" :file "test.sol"})))
        contract (solc/create-module-entry rt input)]
    (deploy-base rt url contract (or (:args input) []))))
