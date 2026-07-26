tahto/runtime/basic/type_bench_test.clj:1:(ns tahto.runtime.basic.type-bench-test
tahto/runtime/basic/type_bench_test.clj:2:  (:require [tahto.runtime.basic.type-bench :refer :all]
tahto/runtime/basic/type_bench_test.clj:3:            [tahto.runtime.basic.type-oneshot :as oneshot]
            [std.fs :as fs]
            [std.lib.network :as network])
  (:use code.test))

tahto/runtime/basic/type_bench_test.clj:8:^{:refer tahto.runtime.basic.type-bench/bench? :added "4.0"}
(fact "checks if object is a bench"
  (bench? (map->RuntimeBench {}))
  => true)

tahto/runtime/basic/type_bench_test.clj:13:^{:refer tahto.runtime.basic.type-bench/get-bench :added "4.0"}
(fact "gets an active bench given port"
tahto/runtime/basic/type_bench_test.clj:15:  (with-redefs [tahto.runtime.basic.type-bench/*active* (atom {1234 :bench})]
    (get-bench 1234))
  => :bench)

tahto/runtime/basic/type_bench_test.clj:19:^{:refer tahto.runtime.basic.type-bench/create-bench-process :added "4.0"}
(fact "creates the bench process"
  (let [port (network/port:check-available 0)
        p (create-bench-process
           :python port
           {:root-dir (str (fs/create-tmpdir))}
           ["echo"]
           "hello")]
    (try
      p => bench?
      (finally
        (stop-bench-process port)))))

tahto/runtime/basic/type_bench_test.clj:32:^{:refer tahto.runtime.basic.type-bench/start-bench-process :added "4.0"}
(fact "starts a bench process"

  (start-bench-process :python
                       {:exec ["echo"]
                        :bootstrap (fn [port opts] "hello")}
                       0
                       {:root-dir (str (fs/create-tmpdir))})
  => bench?

  (stop-bench-process 0)
  => (any bench? nil?))

tahto/runtime/basic/type_bench_test.clj:45:^{:refer tahto.runtime.basic.type-bench/stop-bench-process :added "4.0"}
(fact "stops the bench process"
tahto/runtime/basic/type_bench_test.clj:47:  (with-redefs [tahto.runtime.basic.type-bench/*active* (atom {1234 {:process nil}})]
    (stop-bench-process 1234))
  => map?)

tahto/runtime/basic/type_bench_test.clj:51:^{:refer tahto.runtime.basic.type-bench/start-bench :added "4.0"}
(fact "starts a test bench process"
  (with-redefs [oneshot/rt-oneshot-setup (fn [& _] [:program {:exec "echo"} ["echo"]])
tahto/runtime/basic/type_bench_test.clj:54:                tahto.runtime.basic.type-bench/start-bench-process (fn [& _] :started)]
    (start-bench :python {:program :python} 1234 {}))
  => :started)

tahto/runtime/basic/type_bench_test.clj:58:^{:refer tahto.runtime.basic.type-bench/stop-bench :added "4.0"}
(fact "stops a test bench process"
  (with-redefs [stop-bench-process (fn [_] :stopped)
                fs/delete (fn [_] nil)]
    (stop-bench {:port 1234}))
  => :stopped)
