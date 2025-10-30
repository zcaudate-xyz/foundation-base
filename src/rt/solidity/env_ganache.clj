(ns rt.solidity.env-ganache
  (:require [std.lib :as h :refer [defimpl]]
            [std.lib.network :as network]
            [std.json :as json]
            [std.string :as str]
            [std.lang :as l]
            [std.fs :as fs]
            [rt.basic.impl.process-js :as process-js]))

(defonce +contracts+ (atom {}))

(defn- clear-contracts
  []
  (reset! +contracts+ {}))

(def +default-port+ 8545)

(def +default-dir+ "test-bench/ganache/default")

(def +default-mnemonic+
  "taxi dash nation raw first art ticket more useful mosquito include true")

(def +default-addresses+
  ["0x94e3361495bD110114ac0b6e35Ed75E77E6a6cFA"
   "0xE195c59BCF26fD36c82d1C720860127A5c1c4040"
   "0xB0d070485a1bfb782A5B1B9F095A48CCF20F11A6"
   "0xA25C1786c6AB40dF71b6D08b9E964126A3649c19"
   "0x24450d687eF785d8499dDdd060D108E0106D77F9"
   "0x4F8378836CAdB1c714fCaad97899d45AC2C8BF60"
   "0xD3f9bc804d1F7cC5E64a1896AB45fE60105006Aa"
   "0xD1Eba2338a5Ad0E5c2c98396a0bB200724d1cAc8"
   "0x9240775F530d9aA9E162F4dDC5d353084d40C1b1"
   "0x001Dc339B1E9B9D443bcd39F9d5114390Bc43aCD"])

(def +default-addresses-raw+
  ["0x94e3361495bD110114ac0b6e35Ed75E77E6a6cFA"
   "0xE195c59BCF26fD36c82d1C720860127A5c1c4040"
   "0xB0d070485a1bfb782A5B1B9F095A48CCF20F11A6"
   "0xA25C1786c6AB40dF71b6D08b9E964126A3649c19"
   "0x24450d687eF785d8499dDdd060D108E0106D77F9"
   "0x4F8378836CAdB1c714fCaad97899d45AC2C8BF60"
   "0xD3f9bc804d1F7cC5E64a1896AB45fE60105006Aa"
   "0xD1Eba2338a5Ad0E5c2c98396a0bB200724d1cAc8"
   "0x9240775F530d9aA9E162F4dDC5d353084d40C1b1"
   "0x001Dc339B1E9B9D443bcd39F9d5114390Bc43aCD"])

(def +default-addresses+
  ["0x94E3361495BD110114AC0B6E35ED75E77E6A6CFA"
   "0xE195C59BCF26FD36C82D1C720860127A5C1C4040"
   "0xB0D070485A1BFB782A5B1B9F095A48CCF20F11A6"
   "0xA25C1786C6AB40DF71B6D08B9E964126A3649C19"
   "0x24450D687EF785D8499DDDD060D108E0106D77F9"
   "0x4F8378836CADB1C714FCAAD97899D45AC2C8BF60"
   "0xD3F9BC804D1F7CC5E64A1896AB45FE60105006AA"
   "0xD1EBA2338A5AD0E5C2C98396A0BB200724D1CAC8"
   "0x9240775F530D9AA9E162F4DDC5D353084D40C1B1"
   "0x001DC339B1E9B9D443BCD39F9D5114390BC43ACD"])

(def +default-private-keys+
  ["6f1313062db38875fb01ee52682cbf6a8420e92bfbc578c5d4fdc0a32c50266f"
   "61e2db80d80fef89b7a5fa748cf46471cb2fa91f0248ee36675d5e28a84d932b"
   "c784a885f7e4045e862e35dacbc05861b9b93582d2f992b63004b759094953df"
   "205d135d3410063c7736f6ab4dcf2530158eafc2769dad8ad7068444e14e22e3"
   "a0976fcc56a6d893116108398fa1ed83ba46f9fc49fbeec9703e836e04cae7b5"
   "60722fb124b2e36a01c4d306aad8e4ecea12c2ff7d025ce4624ed5f72df8e6d1"
   "95965cae2950eeafd87eac625ea642e21dc563e3747b18e4d71dcab3622dcfb5"
   "a1a5f790fad4dedb53fc9688ffd464a86872f51033565444a63fa6e93eaace5a"
   "d4732a06cefcfc9e89336a29db7038cd61143128bddea69309646b45dd3d406c"
   "0ac431843af62030d596973c914ff3eafa6edde5dd261af63a827beb28ace826"])

(defonce ^:dynamic *server* (atom nil))

(defn start-ganache-server
  "starts the ganache service"
  {:added "4.0"}
  []
  (or @*server*
      (when (not (h/suppress
                  (h/wait-for-port "127.0.0.1" +default-port+
                                   {:timeout 1000})))
        (let [_    (fs/create-directory +default-dir+)
              _    (clear-contracts)]
          (-> (if (not @*server*)
                (swap! *server*
                       (fn [m]
                         (let [process (h/sh {:args ["/bin/bash" "-c"
                                                     "ganache --wallet.seed 'test' --host '0.0.0.0'"]
                                              :wait false
                                              :root +default-dir+})
                               thread  (-> (h/future
                                             (h/sh-wait process))
                                           (h/on:complete
                                            (fn [_ _]
                                              (try (let [out (h/sh-output process)]
                                                     (when (not= 0 (:exit out))
                                                       (h/prn out)))
                                                   (catch Throwable t))
                                              (reset! *server* nil))))]
                           (h/wait-for-port (network/local-ip) +default-port+
                                            {:timeout 10000})
                           {:type "ganache"
                            :port +default-port+
                            :root +default-port+
                            :process process
                            :thread thread})))
                @*server*))))))

(defn stop-ganache-server
  "stops the ganache service"
  {:added "4.0"}
  []
  (let [{:keys [type process] :as entry} @*server*]
    (when process
      (doto process
        (h/sh-close)
        (h/sh-exit)
        (h/sh-wait)))
    entry))
