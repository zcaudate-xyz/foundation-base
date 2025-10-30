(ns rt.solidity.client
  (:require [std.protocol.component :as protocol.component]
            [std.protocol.context :as protocol.context]
            [std.lang.base.runtime :as default]
            [std.lib :as h :refer [defimpl]]
            [std.json :as json]
            [std.string :as str]
            [std.lang :as l]
            [std.fs :as fs]
            [std.lang.interface.type-shared :as shared]
            [js.lib.eth-bench :as eth-bench]
            [xt.lang.base-notify :as notify]
            [xt.lang.base-repl :as base-repl]
            [rt.basic :as basic]
            [rt.basic.server-basic :as server]
            [rt.solidity.compile-common :as common]
            [rt.solidity.compile-node :as node]
            [rt.solidity.compile-solc :as solc]
            [rt.solidity.compile-deploy :as deploy]))

;;
;;
;;


(defn check-node-connection
  "checks that the node connection is present"
  {:added "4.0"}
  [{:keys [node] :as rt}]
  (or (server/get-relay
       (server/get-server (:id node) :js))
      (h/error "Not Connected")))

(defn contract-fn-name
  "gets the name of a pointer"
  {:added "4.0"}
  [ptr]
  (let [fn-name  (str (or (:id ptr)
                          (h/error "Free pointers not supported")))]
    (l/emit-symbol :solidity (symbol fn-name))))

;;
;;
;;

(defn create-web3-node
  "creates the node runtime"
  {:added "4.0"}
  [{:keys [id lang url] :as rt}]
  (let [rt-node (basic/rt-basic
                 {:id (h/sid)
                  :lang :js
                  :runtime :basic
                  :layout :full})
        url     (common/get-url rt)
        form  (h/$ [(:= solc (require "solc"))])
        _     (notify/wait-on-fn rt-node form 5000)]
    rt-node))

(defn start-web3
  "starts the solidity rt"
  {:added "4.0"}
  ([{:keys [id lang config] :as rt}]
   (assoc rt
          :node   (create-web3-node rt)
          :config (assoc config :url (common/get-url rt)))))

(defn stop-web3
  "stops the solidity rt"
  {:added "4.0"}
  [{:keys [id lang bench container] :as rt}]
  (let [_ (h/stop (:node rt))
        _ (common/set-rt-settings id nil)]
    (dissoc rt :node)))

(defn raw-eval-web3
  "disables raw-eval for solidity"
  {:added "4.0"}
  ([{:keys [id lang] :as rt} body]
   (h/error "NOT AVAILABLE" {})))


;;
;;
;;


(defn invoke-ptr-web3-check
  "checks that arguments are correct"
  {:added "4.0"}
  [contract fn-name args]
  (let [spec  (first (filter #(-> (get % "name")
                                              (= fn-name))
                                         (:abi contract)))
        types (get spec "inputs")
        retype (or (get-in spec ["outputs" 0 "type"])
                   "")]
    (when (not spec)
      (h/error "No spec found: " {:name fn-name
                                  :options (map #(get % "name") (:abi contract))}))
    (when (not= (count args)
                (count types))
      (h/error "Args not the same: " {:args args
                                      :types types}))
    (and (str/starts-with? retype "uint")
         (not (str/ends-with? retype "[]")))))

(defn invoke-ptr-web3-call
  "invokes a deployed method"
  {:added "4.0"}
  [rt ptr args]
  (let [contract (cond common/*temp*
                       (solc/create-pointer-entry (:node rt) ptr)

                       :else
                       (node/rt-get-contract))
        
        fn-name  (contract-fn-name ptr)
        to-string?  (invoke-ptr-web3-check contract fn-name args)
        {:keys [form]} @ptr
        node-id (:id (:node rt))
        readonly? (or (not (= 'defn (first form)))
                      (some #{:view :pure}
                            (:- (meta (second (:form @ptr))))))
        
        
        form-call (list `eth-bench/contract-run
                        (common/get-url rt)
                        (common/get-caller-private-key node-id)
                        (common/get-contract-address node-id)
                        (:abi contract)
                        fn-name
                        (vec args)
                        (if readonly?
                          {}
                          {:gasLimit common/*gas-limit*
                           :value    common/*caller-payment*}))]
    (solc/compile-rt-eval (:node rt)
                          form-call
                          (if to-string?
                            js.core/toString))))

(defn invoke-ptr-web3
  "invokes the runtime, deploying the contract if not available"
  {:added "4.0"}
  [{:keys [mode] :as rt} ptr args]
  (let [mode  (cond (= common/*clean* false)
                    nil
                    
                    :else mode)]
    (cond common/*temp*
          (let [{:strs [contractAddress]} (deploy/deploy-pointer (:node rt)
                                                                 (common/get-url rt)
                                                                 ptr)]
            (common/with:contract-address [contractAddress]
              (invoke-ptr-web3-call rt ptr args)))
          
          :else
          (do (when (or (#{:clean} mode)
                        (nil? (common/get-contract-address
                               (:id (:node rt)))))
                (deploy/deploy-pointer
                 (:node rt)
                 (common/get-url rt)
                 ptr))
              (invoke-ptr-web3-call rt ptr args)))))

(defn rt-web3-string
  "gets the runtime string"
  {:added "4.0"}
  [{:keys [id lang config node]}]
  (let [server (server/get-server (:id node)
                                  (:lang node))]
    (str "#rt.web3"
         [id {:url  (:url config)
              :node (if server
                      [(:id node) (:port server) @(:count server)]
                      :no-server)}])))

(defimpl RuntimeWeb3 [id]
  :string rt-web3-string
  :protocols [std.protocol.component/IComponent
              :suffix "-web3"
              :method {-kill stop-web3}
              protocol.context/IContext
              :prefix "default/default-"
              :method {-raw-eval    raw-eval-web3
                       -invoke-ptr  invoke-ptr-web3}])

(defn rt-web3:create
  "creates a runtime"
  {:added "4.0"}
  [{:keys [id
           lang] :as m}]
  (map->RuntimeWeb3 (merge  m
                            {:id (or id (h/sid))
                             :tag :web3
                             :runtime :web3})))

(defn rt-web3
  "creates an starts a runtime"
  {:added "4.0"}
  [{:keys [id
           lang] :as m}]
  (-> (rt-web3:create m)
      (h/start)))

(def +init+
  [(default/install-type!
    :solidity :web3
    {:type :hara/rt.web3.instance
     :config {:layout :flat}
     :instance {:create rt-web3:create}})
   
   (default/install-type!
    :solidity :web3.shared
    {:type :hara/rt.web3
     :config {:layout :flat}
     :instance
     {:create (fn [m]
                (-> {:rt/client {:type :hara/rt.web3 
                                 :constructor rt-web3:create}}
                    (merge m)
                    (shared/rt-shared:create)))}})])

(comment

  (defn invoke-ptr-web3-deploy
    "deploys a contract given code"
    {:added "4.0"}
    [{:keys [id module] :as rt}
     code
     {:keys [set-address
             name
             file
             interfaces
             args
             no-wrap] :as opts}]
    (let [prefix (if (empty? interfaces)
                   ""
                   (str/join
                    "\n\n"
                    (conj (mapv l/emit-ptr interfaces) "")))
          form   (list `web3/contract-test-prep
                       'web3
                       (list `xt.lang.base-lib/join
                             "\n"
                             (str/split-lines code))
                       (common/get-caller-private-key id)
                       {:args args :file file :name name
                        :no-wrap no-wrap :prefix prefix})
          result (try (compile/compile-rt-eval (:node rt) form)
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
                  (do (when (not compile/*suppress-errors*)
                        (h/pl (str "//\n\n\n\n" prefix code)))
                      (h/error "Compilation Error"
                               {:data result}))
                  
                  set-address
                  (common/update-rt-settings
                   id {:contract-address contractAddress}))]
      result)))

