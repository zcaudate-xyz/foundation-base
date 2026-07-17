(ns jvm.chisel.testing-simulate-test
  (:use code.test)
  (:require [std.lib.env :as env]
            [jvm.chisel :as ch]
            [jvm.chisel.testing :as ct]))

(fact:global {:skip (not (env/program-exists? "verilator"))})

(defn- vector-module []
  (ch/module {:name "TestingVectorFixture"}
    (fn []
      (let [io (ch/io (ch/bundle [[:values (ch/input (ch/vec 4 (ch/uint 8)))]
                                  [:echo (ch/output (ch/vec 4 (ch/uint 8)))]]))]
        (doseq [i (range 4)]
          (ch/connect! (ch/index (ch/field io :echo) i)
                       (ch/index (ch/field io :values) i)))))))

(def ^:private simulation
  (delay
    (ct/simulate
     (vector-module)
     (fn [{:keys [port poke expect peek step reset!] :as ctx}]
       (ct/poke-vec! ctx "values" [3 5 8 13])
       (step)
       (ct/expect-vec! ctx "echo" [3 5 8 13])
       {:port? (instance? chisel3.Vec (port "values"))
        :element? (instance? chisel3.UInt (ct/vec-el (port "values") 2))
        :peek (peek (ct/vec-el (port "echo") 2))
        :reset (do (reset!) :done)
        :return :simulation-complete}))))

^{:refer jvm.chisel.testing/port-data :added "4.1"}
(fact "port-data resolves a named elaborated IO field"
  (:port? @simulation) => true)

^{:refer jvm.chisel.testing/vec-el :added "4.1"}
(fact "vec-el resolves the requested static lane"
  (:element? @simulation) => true
  (:peek @simulation) => 8)

^{:refer jvm.chisel.testing/poke-vec! :added "4.1"}
(fact "poke-vec! drives every input lane"
  (:peek @simulation) => 8)

^{:refer jvm.chisel.testing/expect-vec! :added "4.1"}
(fact "expect-vec! accepts the exact echoed lane vector"
  (:return @simulation) => :simulation-complete)

^{:refer jvm.chisel.testing/simulate :added "4.1"}
(fact "simulate returns the body result and exposes clock/reset helpers"
  (:return @simulation) => :simulation-complete
  (:reset @simulation) => :done)
