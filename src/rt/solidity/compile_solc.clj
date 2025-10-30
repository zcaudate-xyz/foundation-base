(ns rt.solidity.compile-solc
  (:require [std.lib :as h]
            [std.lang :as l]
            [std.string :as str]
            [std.make.compile :as compile]
            [rt.solidity.compile-common :as common]
            [rt.solidity.env-ganache :as env]
            [rt.basic :as basic]
            [js.lib.eth-solc :as eth-solc]
            [js.core :as j]
            [xt.lang.base-notify :as notify]
            [xt.lang.base-repl :as repl]))

(defn compile-base-emit
  "emits solidity given entries and interfaces"
  {:added "4.0"}
  [entries interfaces]
  (let [grammar    (l/grammar :solidity)

        emit-fn    (fn [entry]
                     (binding [std.lang.base.impl-entry/*cache-none* true]
                       (l/emit-entry grammar entry {:layout :flat})))
        body       (str/join
                    "\n\n"
                    (map emit-fn entries))
        prefix     (str/join
                    "\n\n"
                    (map emit-fn interfaces))]
    [body prefix]))

(defn compile-base-code
  "compiles base code"
  {:added "4.0"}
  [body opts]
  (let [{:keys [headers
                prefix
                name
                raw]
         :or {name    "Temp"
              headers ["// SPDX-License-Identifier: GPL-3.0"
                       "pragma solidity >=0.7.0 <0.9.0;"]}} opts]
    (cond raw body
          :else
          (str/join
           "\n"
           (concat headers
                   [""]
                   (if (not-empty prefix)
                     [prefix ""])
                   [(str "contract " name " {\n"
                         (str/indent body 2)
                         "\n}")])))))

;;
;; Compiles the contract
;;

(defn compile-ptr-prep-open-method
  "opens up a solidity method"
  {:added "4.0"}
  [entry]
  (let [{:keys [form]} entry
        [op sym & rest] form
        meta-fn (fn [sym]
                  (let [modifiers (:- (meta sym))
                        modifiers (if (some #{:private
                                              :public
                                              :internal
                                              :external} modifiers)
                                    (mapv (fn [k]
                                            (if (#{:private
                                                   :internal} k)
                                              :public
                                              k))
                                          modifiers)
                                    (conj (vec modifiers) :public))]
                    (with-meta sym (merge (meta sym)
                                          {:- modifiers}))))
        sym  (case op
               (defaddress defmapping def defn)  (meta-fn sym)
               sym)]
    (assoc entry :form (apply list op sym rest))))

(defn compile-ptr-prep
  "exports a ptr"
  {:added "4.0"}
  [ptr]
  (let [entries (std.lang.base.impl/emit-entry-deps-collect
                 ptr
                 {:lang :solidity})
        is-interface #(-> % :op-key (= :definterface))
        interfaces (filter is-interface entries)
        main       (compile-ptr-prep-open-method   (last entries))
        entries    (conj (vec (butlast (remove is-interface entries)))
                         main)
        [body prefix] (compile-base-emit entries interfaces)]
    [body {:name "Test" :file "test.sol" :prefix prefix}]))

(defn compile-ptr-code
  "compiles the pointer to code"
  {:added "4.0"}
  [ptr]
  (apply compile-base-code (compile-ptr-prep ptr)))

(defn compile-module-prep
  "preps a namespace or map for emit"
  {:added "4.0"}
  [m]
  (let [m   (or m
                (let [ns (.getName *ns*)]
                  (if-let [v (resolve (symbol (str ns) "+default-contract+"))]
                    @v
                    {:ns ns :name "Test" :file "test.sol"})))
        module  (l/get-module (l/runtime-library)
                              :solidity
                              (:ns m))
        entries    (sort-by
                    :line
                    (vals (:code module)))
        entries    (cond common/*open-methods*
                         (map compile-ptr-prep-open-method entries)

                         :else entries)
        interfaces (sort-by
                    :line
                    (vals (:header module)))
        [body prefix] (compile-base-emit entries interfaces)]
    [body (assoc m :prefix prefix)]))

(defn compile-module-code
  "compiles the contract code"
  {:added "4.0"}
  [m]
  (apply compile-base-code (compile-module-prep m)))

(defn compile-single-sol
  "compiles a solidity contract"
  {:added "4.0"}
  [{:keys [header footer main root target] :as opts}]
  (let [{:keys [name]} main
        opts   (assoc opts :file (str name ".sol"))
        body   (compile-module-code main)
        full   (compile/compile-fullbody body opts)
        output (compile/compile-out-path opts)]
    (compile/compile-write output full)))

(defn compile-all-sol
  "compiles multiple solidity contracts"
  {:added "4.0"}
  ([{:keys [header footer lang main root target] :as opts}]
   (let [files   (mapv (fn [single]
                         (compile-single-sol (assoc opts
                                                    :main single)))
                       main)]
     (compile/compile-summarise files))))

(def +install-contract-all-sol+
  (compile/types-add :contract.sol #'compile-all-sol))

;;
;; Compiles the bytecode
;;

(defn compile-rt-prep
  "creates a runtime"
  {:added "4.0"}
  []
  (let [rt-node (basic/rt-basic
                 {:lang :js
                  :runtime :basic
                  :layout :full})
        form (h/$ [(xt.lang.base-repl/notify
                    (:= (!:G solc) (require "solc")))])
        _    (notify/wait-on-fn
              rt-node
              form
              5000)]
    rt-node))

(defn compile-rt-eval
  "evals form in the runtime"
  {:added "4.0"}
  [rt form & [f]]
  (let [output (notify/wait-on-fn
                rt
                [(list 'js.core/notify form
                       (or f
                           (if common/*stringify*
                             'JSON.stringify
                             nil)))]
                60000)]
    (try 
      (std.json/read output)
      (catch Throwable t output))))

(defn compile-rt-abi
  "compiles the contract-abi"
  {:added "4.0"}
  [rt code & [file]]
  (let [file   (or file "temp.sol")
        form   (list `eth-solc/contract-compile code file)
        result (try (compile-rt-eval rt form)
                    (catch clojure.lang.ExceptionInfo ex
                      (do (h/prn ex)
                          {:status false
                           :error (or (try
                                        (std.json/read
                                         (get-in (ex-data ex)
                                                 [:err 0]))
                                        (catch Throwable t))
                                      (ex-data ex))}))
                    (catch Throwable t t))
        contracts (get-in result ["contracts" file])
        _      (when (not contracts)
                 (when (nil? common/*suppress-errors*)
                   (h/pl code))
                 (h/error "Compilation Error"
                          result))]
    contracts))

(defn compile-all-abi
  "compiles the abis"
  {:added "4.0"}
  ([{:keys [header footer lang main root target] :as opts}]
   (let [rt      (compile-rt-prep)
         abi-fn  (fn [[name abi]]
                   (let [opts   (assoc opts :file (str name ".json"))
                         body   (std.json/write-pp abi)
                         full   (compile/compile-fullbody body opts)
                         output (compile/compile-out-path opts)]
                     (compile/compile-write output full)))
         files   (vec (mapcat (fn [single]
                                (let [contracts (compile-rt-abi
                                                 rt
                                                 (compile-module-code single))]
                                  (map abi-fn contracts)))
                              main))
         _  (h/stop rt)]
     (compile/compile-summarise files))))

(def +install-contract-all-abi+
  (compile/types-add :contract.abi #'compile-all-abi))

(defn create-base-entry
  "creates either a pointer or module entry"
  {:added "4.0"}
  [rt f input path name & [refresh]]
  (let [[body opts] (f input)
        code  (compile-base-code body opts)
        sha   (h/sha1 code)
        [type id] path  
        contract (get-in @common/+compiled+ path)]
    (cond (and (= (:sha contract) sha)
               (not refresh))
          contract

          :else
          (let [compiled (get (compile-rt-abi rt code)
                              name)
                contract
                (common/map->Contract
                 {:type     type
                  :id       id
                  :bytecode (get-in compiled ["evm" "bytecode" "object"])
                  :abi      (get-in compiled ["abi"])
                  :code     code
                  :sha      sha
                  :args     (or (:args input) [])})]
            (swap! common/+compiled+ assoc-in path contract)
            contract))))

(defn create-pointer-entry
  "creates a pointer entry"
  {:added "4.0"}
  [rt ptr & [refresh]]
  (create-base-entry rt compile-ptr-prep ptr
                     [:entry (l/sym-full ptr)]
                     "Test"
                     refresh))

(defn create-module-entry
  "creates a compiled module contract entry"
  {:added "4.0"}
  [rt m & [refresh]]
  (create-base-entry rt compile-module-prep m
                     [:module
                      (or (:ns m) (.getName *ns*))
                      (boolean common/*open-methods*)]
                     (or (:name m)
                         "Test")
                     refresh))

(defn create-file-entry
  "creates a file entry
   
   (compile/create-file-entry +rt+
                              {:name \"USDT.sol\"
                               :file \"resources/assets/rt.solidity/example/USDT.sol\"})"
  {:added "4.0"}
  [rt m & [refresh]]
  (create-base-entry rt (fn [input]
                          [(slurp (:file input)) {:raw true}])
                     m
                     [:file
                      (:file m)
                      nil]
                     (or (:name m)
                         "Test")
                     true))


(comment
  "@openzeppelin/contracts/access/Ownable.sol"
  )
