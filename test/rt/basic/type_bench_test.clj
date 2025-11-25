(ns rt.basic.type-bench-test
  (:use code.test)
  (:require [rt.basic.type-bench :refer :all]
            [std.lib :as h]
            [std.fs :as fs]
            [rt.basic.type-oneshot :as oneshot]))

^{:refer rt.basic.type-bench/bench? :added "4.0"}
(fact "checks if object is a bench"
  (bench? (map->RuntimeBench {}))
  => true)

^{:refer rt.basic.type-bench/get-bench :added "4.0"}
(fact "gets an active bench given port"
  (with-redefs [rt.basic.type-bench/*active* (atom {1234 :bench})]
    (get-bench 1234))
  => :bench)

^{:refer rt.basic.type-bench/create-bench-process :added "4.0"}
(fact "creates the bench process"
  ^:hidden
  (let [port (h/port:check-available 0)
        p (create-bench-process
           :python port
           {:root-dir (str (fs/create-tmpdir))}
           ["echo"]
           "hello")]
    (try
      p => bench?
      (finally
        (stop-bench-process port)))))

^{:refer rt.basic.type-bench/start-bench-process :added "4.0"}
(fact "starts a bench process"
  ^:hidden
  
  (start-bench-process :python
                       {:exec ["echo"]
                        :bootstrap (fn [port opts] "hello")}
                       0
                       {:root-dir (str (fs/create-tmpdir))})
  => bench?

  (stop-bench-process 0)
  => bench?)

^{:refer rt.basic.type-bench/stop-bench-process :added "4.0"}
(fact "stops the bench process"
  (with-redefs [rt.basic.type-bench/*active* (atom {1234 {:process nil}})]
    (stop-bench-process 1234))
  => map?)

^{:refer rt.basic.type-bench/start-bench :added "4.0"}
(fact "starts a test bench process"
  (with-redefs [oneshot/rt-oneshot-setup (fn [& _] [:program {:exec "echo"} ["echo"]])
                rt.basic.type-bench/start-bench-process (fn [& _] :started)]
    (start-bench :python {:program :python} 1234 {}))
  => :started)

^{:refer rt.basic.type-bench/stop-bench :added "4.0"}
(fact "stops a test bench process"
  (with-redefs [stop-bench-process (fn [_] :stopped)
                fs/delete (fn [_] nil)]
    (stop-bench {:port 1234}))
  => :stopped)
