(ns rt.solidity.compile-common
  (:require [rt.solidity.env-ganache :as env-ganache]
            [std.lang :as l]
            [std.lib.impl :as impl]
            [std.lib.network :as network]))

;;
;; TYPES
;;

(defn- contract-string
  ([{:keys [type id sha]}]
   (str "#contract" {:id id
                     :sha sha})))

(impl/defimpl Contract [type id abi sha bytecode]
  :prefix "contract:"
  :string contract-string)


;;
;; SCAFFOLD
;;

(defonce +current+  (atom {}))

(defonce +compiled+ (atom {:module  {}
                           :entry   {}}))

(defn clear-compiled
  "clears all compiled entries"
  {:added "4.0"}
  []
  (reset! +compiled+ {:module  {}
                      :entry   {}}))

;;
;; DEFAULTS
;;

(def ^:dynamic *default-url* (str "http://" (network/local-ip) ":" env-ganache/+default-port+))

(def ^:dynamic *default-caller-private-key* (first env-ganache/+default-private-keys+))

(def ^:dynamic *default-caller-address* (first env-ganache/+default-addresses+))

;;
;; OVERRIDES
;;

(def ^:dynamic *temp* nil)

(def ^:dynamic *clean* nil)

(def ^:dynamic *url* nil)

(def ^:dynamic *stringify* nil)

(def ^:dynamic *gas-limit* 1000000)

(def ^:dynamic *caller-address* nil)

(def ^:dynamic *caller-private-key* nil)

(def ^:dynamic *caller-payment* 0)

(def ^:dynamic *contract-address* nil)

(def ^:dynamic *open-methods* nil)

(def ^:dynamic *suppress-errors* nil)

(def ^:dynamic *temp-runtime* nil)

;;
;;
;;

(defmacro with:open-methods
  "forces the open methods flag"
  {:added "4.0"}
  [& body]
  `(binding [*open-methods* true]
     ~@body))


(defmacro with:closed-methods
  "turns off the open methods flag"
  {:added "4.0"}
  [& body]
  `(binding [*open-methods* false]
     ~@body))


(defmacro with:suppress-errors
  "suppresses printing of errors"
  {:added "4.0"}
  [& body]
  `(binding [*suppress-errors* true]
     ~@body))

(defmacro ^{:style/indent 0}
  with:stringify
  "stringifies the output"
  {:added "4.0"}
  ([& body]
   `(binding [*stringify* true]
      ~@body)))

(defmacro ^{:style/indent 0}
  with:temp
  "deploy and run temp contract"
  {:added "4.0"}
  ([& body]
   `(binding [*temp* true]
      ~@body)))

(defmacro ^{:style/indent 1}
  with:clean
  "with clean flag"
  {:added "4.0"}
  ([[clean] & body]
   `(binding [*clean* (not= ~clean false)]
      ~@body)))

(defmacro ^{:style/indent 1}
  with:url
  "overrides api url"
  {:added "4.0"}
  ([[url] & body]
   `(binding [*url* ~url]
      ~@body)))

(defmacro ^{:style/indent 1}
  with:caller-address
  "overrides caller address"
  {:added "4.0"}
  ([[address] & body]
   `(binding [*caller-address* ~address]
      ~@body)))

(defmacro ^{:style/indent 1}
  with:caller-private-key
  "overrides caller private key"
  {:added "4.0"}
  ([[private-key] & body]
   `(binding [*caller-private-key* ~private-key]
      ~@body)))

(defmacro ^{:style/indent 1}
  with:caller-payment
  "overrides the caller payment"
  {:added "4.0"}
  ([[payment] & body]
   `(binding [*caller-payment* ~payment]
      ~@body)))

(defmacro ^{:style/indent 1}
  with:contract-address
  "overrides contract address"
  {:added "4.0"}
  ([[address] & body]
   `(binding [*contract-address* ~address]
      ~@body)))

(defmacro ^{:style/indent 1}
  with:gas-limit
  "sets the gas limit"
  {:added "4.0"}
  ([[gas-limit] & body]
   `(binding [*gas-limit* ~gas-limit]
      ~@body)))

(defmacro ^{:style/indent 1}
  with:params
  "overrides all parameters"
  {:added "4.0"}
  ([{:keys [clean
            url
            contract-address
            caller-address
            caller-private-key
            caller-payment
            gas-limit
            temp
            stringify
            open-methods
            suppress-errors]} & body]
   `(binding [~@(if suppress-errors  [`*suppress-errors* true])
              ~@(if open-methods  [`*open-methods* true])
              ~@(if temp  [`*temp* true])
              ~@(if stringify  [`*stringify* true])
              ~@(if clean [`*clean* clean])
              ~@(if url   [`*url* url])
              ~@(if contract-address [`*contract-address* contract-address])
              ~@(if caller-address [`*caller-address* caller-address])
              ~@(if caller-payment [`*caller-payment* caller-payment])
              ~@(if caller-private-key [`*caller-private-key* caller-private-key])
              ~@(if gas-limit [`*gas-limit* gas-limit])]
      ~@body)))

(defn get-rt-settings
  "gets saves rt settings"
  {:added "4.0"}
  [id]
  (get @+current+ id))

(defn set-rt-settings
  "sets rt settings"
  {:added "4.0"}
  [id settings]
  (swap! +current+
         (fn [m]
           (if settings
             (assoc m id settings)
             (dissoc m id)))))

(defn update-rt-settings
  "updates rt settings"
  {:added "4.0"}
  [id settings]
  (swap! +current+
         (fn [m]
           (update m id merge settings))))

(defn get-caller-address
  "gets the caller address"
  {:added "4.0"}
  [id]
  (or *caller-address*
      (:caller-address
       (get-rt-settings id))
      *default-caller-address*))

(defn get-caller-private-key
  "gets the caller private key"
  {:added "4.0"}
  [id]
  (or *caller-private-key*
      (:caller-private-key
       (get-rt-settings id))
      *default-caller-private-key*))

(defn get-contract-address
  "gets the contract address"
  {:added "4.0"}
  [id]
  (or *contract-address*
      (:contract-address
       (get-rt-settings id))))

(defn get-url
  "get sthe url for a rt"
  {:added "4.0"}
  [rt]
  (let [{:keys [url] :as rt} (or rt
                                 (l/rt (.getName *ns*) :solidity))]
    (or *url*
        url
        (:url (get-rt-settings (:id (:node rt))))
        *default-url*)))
