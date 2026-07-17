(ns jvm.chisel.testing-test
  (:use code.test)
  (:require [std.lib.env :as env]
            [jvm.chisel :as ch]
            [jvm.chisel.testing :as ct]))

^{:refer jvm.chisel.testing/pack :added "4.1"}
(fact "pack uses lane zero as the least-significant bit"
  (ct/pack []) => 0
  (ct/pack [1 0 1 1]) => 2r1101
  (ct/pack [0 1 0 0 1]) => 2r10010)

^{:refer jvm.chisel.testing/unpack :added "4.1"}
(fact "unpack returns exactly the requested low bits in lane order"
  (ct/unpack 2r1101 4) => [1 0 1 1]
  (ct/unpack 2r1101 2) => [1 0]
  (ct/unpack 0 0) => [])

^{:refer jvm.chisel.testing/popcount-int :added "4.1"}
(fact "popcount-int counts set bits in a machine integer"
  (ct/popcount-int 0) => 0
  (ct/popcount-int 2r10110100) => 4
  (ct/popcount-int -1) => 64)


(defn- canonical-simulation-module []
  (ch/module {:name "CanonicalTestingFixture"}
    (fn []
      (let [io (ch/io (ch/bundle [[:in (ch/input (ch/uint 8))]
                                  [:out (ch/output (ch/uint 8))]]))]
        (ch/connect! (ch/field io :out) (ch/field io :in))))))

^{:refer jvm.chisel.testing/port-data :added "4.1"}
(fact "port-data returns the requested named IO element"
  (with-redefs-fn {#'jvm.chisel.testing/elements
                   (fn [_] {"in" :input-port "out" :output-port})}
    #(ct/port-data :raw "out"))
  => :output-port)

^{:refer jvm.chisel.testing/vec-el :added "4.1"}
(fact "vec-el returns the requested statically indexed Vec element"
  (instance? chisel3.UInt (ct/vec-el (ch/vec 3 (ch/uint 8)) 1))
  => true)

^{:refer jvm.chisel.testing/poke-vec! :added "4.1"}
(fact "poke-vec! addresses every lane in order"
  (let [calls (atom [])
        ctx {:port (fn [name] [:port name])
             :poke (fn [port value] (swap! calls conj [port value]))}]
    (with-redefs [ct/vec-el (fn [port i] [port i])]
      (ct/poke-vec! ctx "values" [3 5 8]))
    @calls)
  => [[[[:port "values"] 0] 3]
      [[[:port "values"] 1] 5]
      [[[:port "values"] 2] 8]])

^{:refer jvm.chisel.testing/expect-vec! :added "4.1"}
(fact "expect-vec! checks every output lane in order"
  (let [calls (atom [])
        ctx {:port (fn [name] [:port name])
             :expect (fn [port value] (swap! calls conj [port value]))}]
    (with-redefs [ct/vec-el (fn [port i] [port i])]
      (ct/expect-vec! ctx "result" [13 21]))
    @calls)
  => [[[[:port "result"] 0] 13]
      [[[:port "result"] 1] 21]])

^{:refer jvm.chisel.testing/simulate :added "4.1"}
(fact "simulate returns its body result through a real available backend"
  (if (env/program-exists? "verilator")
    (ct/simulate
     (canonical-simulation-module)
     (fn [{:keys [port poke expect step]}]
       (poke (port "in") 42)
       (step)
       (expect (port "out") 42)
       :simulation-complete))
    :simulator-unavailable)
  => (if (env/program-exists? "verilator")
       :simulation-complete
       :simulator-unavailable))
