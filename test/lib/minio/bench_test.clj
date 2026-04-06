(ns lib.minio.bench-test
  (:require [lib.minio.bench :refer :all]
            [std.fs :as fs]
            [std.lib.future :as future]
            [std.lib.network :as network]
            [std.lib.os :as os])
  (:use code.test))

^{:refer lib.minio.bench/all-minio-ports :added "4.0"}
(fact "gets all active minio ports"
  (with-redefs [os/sh (fn [& _]
                        "COMMAND PID USER FD TYPE DEVICE SIZE/OFF NODE NAME\nminio 101 user 1u IPv4 0 0 TCP *:9000 (LISTEN)\nminio 102 user 1u IPv4 0 0 TCP *:9000 (LISTEN)\nminio 103 user 1u IPv4 0 0 TCP *:9001 (LISTEN)")]
    (all-minio-ports))
  => {9000 #{101 102}
      9001 #{103}})

^{:refer lib.minio.bench/start-minio-server :added "4.0"}
(fact "starts the minio server in a given directory"
  (binding [*active* (atom {})]
    (with-redefs [network/port:check-available (constantly 9000)
                  fs/create-directory (fn [_] nil)
                  os/sh (fn [& _] :process)
                  future/future (fn [& _] :thread)
                  future/on:complete (fn [thread _] thread)
                  network/wait-for-port (fn [& _] true)]
      (start-minio-server {} :bench "/tmp/minio-root")))
  => (contains {:type :bench
                :port 9000
                :root "/tmp/minio-root"
                :process :process
                :thread :thread}))

^{:refer lib.minio.bench/stop-minio-server :added "4.0"}
(fact "stop the minio server"
  (binding [*active* (atom {9000 {:type :bench
                                  :process :process}})]
    (with-redefs [os/sh-close identity
                  os/sh-exit identity
                  os/sh-wait identity]
      (stop-minio-server 9000 :bench)))
  => {:type :bench
      :process :process})

^{:refer lib.minio.bench/bench-start :added "4.0"}
(fact "starts the bench"
  (with-redefs [fs/create-tmpdir (fn [] "/tmp/minio-scratch")
                start-minio-server (fn [minio type root-dir]
                                     {:port 9010 :type type :root root-dir})]
    (bench-start {} :scratch))
  => {:port 9010})

^{:refer lib.minio.bench/bench-stop :added "4.0"}
(fact "stops the bench"
  (binding [*active* (atom {9010 {:type :scratch :process :process}})]
    (with-redefs [stop-minio-server (fn [port bench] [port bench])]
      (bench-stop {:port 9010 :bench :scratch} nil)))
  => {:port 9010 :bench :scratch})

^{:refer lib.minio.bench/start-minio-array :added "4.0"}
(fact "starts a minio array"
  (with-redefs [start-minio-server (fn [m type root-dir]
                                     (assoc m :type type :root root-dir))
                network/wait-for-port (fn [& _] true)]
    (start-minio-array [9000 {:port 9001}]))
  => [{:port 9000 :type :array :root (str +bench-path+ "/9000")}
      {:port 9001 :type :array :root (str +bench-path+ "/9001")}])

^{:refer lib.minio.bench/stop-minio-array :added "4.0"}
(fact "stops a minio array"
  (with-redefs [stop-minio-server (fn [port type] [port type])]
    (doall (stop-minio-array [9000 9001])))
  => [[9000 :array] [9001 :array]])
