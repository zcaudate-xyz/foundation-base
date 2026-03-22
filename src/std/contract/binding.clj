(ns std.contract.binding
  (:require [std.contract.type :as type]
            [std.lib.collection]
            [std.lib.impl :refer [defimpl]])
  (:refer-clojure :exclude [bound?]))

(defn contract-info
  "returns the contract info
 
   (-> (contract #'-add- [] nil)
       (contract-info))
   => map?"
  {:added "3.0"}
  ([{:keys [var] :as contract}]
   {:var var}))

(defn contract-invoke
  "invokes the contract
 
   (-> (contract #'-add- [] nil)
       (contract-invoke 1 2))
   => 3"
  {:added "3.0"}
  ([contract & args]
   (let [{:keys [function specs]} contract
         len (count args)
         _   (->> (:inputs specs)
                  (take len)
                  (mapv (fn [arg {:keys [spec opts] :as m}]
                          (if (nil? m)
                            arg
                            (spec arg #_opts)))
                        args))
         result (apply function args)]
     (if-let [{:keys [spec opts]} (:output specs)]
       (spec result #_opts)
       result))))

(defn- contract-string
  ([contract]
   (str "#contract" (contract-info contract))))

(defimpl Contract [var function specs]
  :string contract-string
  :invoke  contract-invoke
  :final true)

(defn contract?
  "checks if object is a contract"
  {:added "3.0"}
  ([obj]
   (instance? Contract obj)))

(defn contract-var?
  "checks if var contains a contract
 
   (contract-var? #'-add-)
   => false
 
   (contract-var? #'-contract-)
   => true"
  {:added "3.0"}
  ([^clojure.lang.Var var]
   (contract? @var)))

(defn bound?
  "checks if contract is bound to var
 
   (bound? #'-add-)
   => false
 
   (bound? #'-contract-)
   => false
 
   (bound? -contract-)
   => false"
  {:added "3.0"}
  ([obj]
   (cond (var? obj)
         (bound? @obj)

         (contract? obj)
         (= @(:var obj) obj)

         :else false)))

(defn unbind
  "unbinds the contract to its var
 
   (do (unbind #'-minus-)
       (bound? #'-minus-))
   => false"
  {:added "3.0"}
  ([obj]
   (cond (var? obj)
         (unbind @obj)

         :else
         (when (and (contract? obj)
                    (bound? obj))
           (let [{:keys [var function]} obj]
             (alter-var-root var (constantly function))
             var)))))

(defn bind
  "binds the contract to its var
 
   (defn -minus-
     ([x y]
      (- x y)))
 
   (do (bind (contract #'-minus- []))
       (bound? #'-minus-))
   => true"
  {:added "3.0"}
  ([^Contract contract]
   (when (not (bound? contract))
     (let [{:keys [var function]} contract]
       (if (contract-var? var) (unbind @var))
       (alter-var-root var (constantly contract))))))

(defn parse-arg
  "parses an input/output arg
 
   (type/defspec <number> number?)
 
   (parse-arg {<number> :strict})"
  {:added "3.0"}
  ([obj]
   (if obj
     (let [[obj opts] (cond (std.lib.collection/hash-map? obj)
                            (first obj)

                            :else [obj nil])
           spec (if (type/spec? obj)
                  obj
                  (throw (ex-info "Spec has to be a spec" {:input obj})))]
       {:spec spec
        :opts opts}))))

(defn contract
  "defines a contract given var and arguments"
  {:added "3.0"}
  ([var inputs]
   (contract var inputs nil))
  ([var inputs output]
   (let [function (if (contract-var? var)
                    (:function @var)
                    @var)
         inputs (mapv parse-arg inputs)
         output (parse-arg output)]
     (->Contract var function {:inputs inputs
                               :output output}))))

(defmacro defcontract
  "defines a contract
 
   (type/defspec <int> integer?)
 
   (defcontract -add-
     :inputs [<int> <int>])
 
   (-add- 0.5 0.5)
   => (throws)"
  {:added "3.0"}
  ([sym & body]
   (let [var (resolve sym)
         {:keys [inputs output]} (apply hash-map body)]
     `(let [c# (contract ~var ~inputs ~output)]
        (doto c# (bind))
        ~var))))
