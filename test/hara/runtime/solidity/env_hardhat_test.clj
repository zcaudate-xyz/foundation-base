(ns hara.runtime.solidity.env-hardhat-test
  (:require [clojure.string :as string]
            [hara.runtime.solidity.env-hardhat :refer :all]
            [std.fs :as fs]
            [std.lib.env :as env]
            [std.lib.future :as future]
            [std.lib.network :as network]
            [std.lib.os :as os])
  (:use code.test))

(defn- fake-sh
  "Mocks os/sh. Returns a fake process for hardhat startup and an empty
   string for lsof listener queries (used during teardown)."
  ([m]
   (fake-sh m nil))
  ([m captured]
   (let [args (:args m)
         cmd  (nth args 2 nil)]
     (cond
       (and (string? cmd) (string/includes? cmd "lsof"))
       ""

       (and captured (string? cmd) (string/includes? cmd "npx hardhat node"))
       (do (reset! captured args) {})

       :else {}))))

(fact:global
 {:skip (not (env/program-exists? "npx"))
  :setup    []
  :teardown [(reset! *server* nil)]})

^{:refer hara.runtime.solidity.env-hardhat/start-hardhat-server :added "4.0"}
(fact "starts the hardhat service and constructs the node command"
  (let [captured (atom nil)]
    (with-redefs [fs/create-directory (fn [_] nil)
                  os/sh (fn [m] (fake-sh m captured))
                  network/wait-for-port (fn [& _] nil)
                  future/future:run (fn [& _] nil)
                  future/on:complete (fn [_ _] nil)
                  *server* (atom nil)]
      (start-hardhat-server))
    => (contains {:type "hardhat"
                  :port 8545
                  :root string?
                  :process {}
                  :thread nil})
    @captured
    => (fn [[shell flag cmd]]
         (and (= shell "/bin/bash")
              (= flag "-c")
              (string? cmd)
              (string/includes? cmd "npx hardhat node")
              (string/includes? cmd "--hostname 0.0.0.0")
              (string/includes? cmd (str "--port " +default-port+))))))

^{:refer hara.runtime.solidity.env-hardhat/stop-hardhat-server :added "4.0"}
(fact "stops the hardhat service and returns server entry"
  (with-redefs [fs/create-directory (fn [_] nil)
                os/sh fake-sh
                os/sh-close (fn [_] nil)
                os/sh-exit (fn [_] nil)
                os/sh-wait (fn [_] nil)
                *server* (atom {:type "hardhat"
                                :process {}})]
    (stop-hardhat-server))
  => (contains {:type "hardhat"
                :process {}}))
