(ns hara.runtime.solidity.env-hardhat
  (:require [hara.runtime.solidity.env-ganache :as env-ganache]))

(def +contracts+ env-ganache/+contracts+)

(def +default-port+ env-ganache/+default-port+)

(def +default-dir+ env-ganache/+default-dir+)

(def +default-root+ env-ganache/+default-root+)

(def +default-mnemonic+ env-ganache/+default-mnemonic+)

(def +default-addresses-raw+ env-ganache/+default-addresses-raw+)

(def +default-addresses+ env-ganache/+default-addresses+)

(def +default-private-keys+ env-ganache/+default-private-keys+)

(def ^:dynamic *server* env-ganache/*server*)

(defn start-hardhat-server
  "starts the hardhat service"
  {:added "4.0"}
  []
  (env-ganache/start-hardhat-server))

(defn stop-hardhat-server
  "stops the hardhat service"
  {:added "4.0"}
  []
  (env-ganache/stop-hardhat-server))

(defn start-ganache-server
  "compatibility wrapper for the hardhat service"
  {:added "4.0"}
  []
  (env-ganache/start-ganache-server))

(defn stop-ganache-server
  "compatibility wrapper for the hardhat service"
  {:added "4.0"}
  []
  (env-ganache/stop-ganache-server))
