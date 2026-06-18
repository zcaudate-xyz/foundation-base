(ns lib.godot.bench-test
  (:use code.test)
  (:require [lib.godot.bench :refer :all]
            [std.lib.network :as network]
            [std.lib.os :as os]
            [hara.runtime.basic.type-common :as common]))

^{:refer lib.godot.bench/godot-exec :added "4.1"}
(fact "resolves the godot executable"
  (godot-exec) => string?
  (with-redefs [common/program-exists? (fn [cmd] (= cmd "godot"))]
    (godot-exec))
  => "godot"
  (with-redefs [common/program-exists? (constantly false)]
    (godot-exec))
  => "godot-4")

^{:refer lib.godot.bench/start-godot-server :added "4.1"}
(fact "starts a headless Godot server and registers it"
  (with-redefs [*active* (atom {})
                network/port:check-available identity
                lib.godot.bench/write-godot-project! (fn [dir] nil)
                lib.godot.bench/start-godot-process (fn [exec root-dir port] :fake-process)
                lib.godot.bench/wait-for-ready-file (fn [root-dir timeout] true)
                os/sh-wait (fn [p] p)]
    (let [server (start-godot-server {:port 52345} :test "/tmp/hara_godot_test")]
      (while (get @*active* 52345) (Thread/sleep 10))
      server))
  => (contains {:type :test
                :port 52345
                :root-dir "/tmp/hara_godot_test"
                :process :fake-process}))

^{:refer lib.godot.bench/stop-godot-server :added "4.1"}
(fact "stops the Godot server for the matching type"
  (let [calls (atom [])]
    (binding [*active* (atom {52345 {:type :test :port 52345 :process :fake-process}})]
      (with-redefs [os/sh-exit (fn [p] (swap! calls conj [:exit p]) p)
                    os/sh-kill (fn [p] (swap! calls conj [:kill p]) p)
                    os/sh-wait (fn [p] (swap! calls conj [:wait p]) p)]
        [(stop-godot-server 52345 :test)
         @calls])))
  => [{:type :test :port 52345 :process :fake-process}
      [[:exit :fake-process] [:kill :fake-process] [:wait :fake-process]]])

^{:refer lib.godot.bench/bench-start :added "4.1"}
(fact "starts the bench and assigns a port"
  (with-redefs [start-godot-server (fn [godot type root-dir] {:port 12345 :type type :root-dir root-dir})]
    (bench-start {:host "127.0.0.1"} :default))
  => (contains {:host "127.0.0.1" :port 12345})

  (with-redefs [start-godot-server (fn [godot type root-dir] {:port 12346 :type type :root-dir root-dir})
                lib.godot.bench/scratch-root (fn [] "/tmp/scratch")]
    (bench-start {} :scratch))
  => (contains {:port 12346}))

^{:refer lib.godot.bench/bench-stop :added "4.1"}
(fact "stops the bench and returns the godot config"
  (let [called (atom nil)]
    (binding [*active* (atom {12345 {:type :mybench :process :proc}})]
      (with-redefs [stop-godot-server (fn [port stop-type] (reset! called [port stop-type]))]
        [(bench-stop {:port 12345 :bench :mybench} nil)
         @called])))
  => [{:port 12345 :bench :mybench} [12345 :mybench]])

^{:refer lib.godot.bench/all-godot-ports :added "4.1"}
(fact "returns sorted active ports"
  (binding [*active* (atom {4001 {} 3001 {} 5001 {}})]
    (all-godot-ports))
  => [3001 4001 5001])
