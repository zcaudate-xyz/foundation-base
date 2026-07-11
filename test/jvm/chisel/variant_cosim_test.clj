(ns jvm.chisel.variant-cosim-test
  "Co-simulation of the `jvm.chisel.variant` kernels against their Clojure
   reference models, using Chisel's native `chisel3.simulator` (Verilator).

   Skipped unless `verilator` is on PATH. Each `ct/simulate` recompiles the
   design, so every fact drives its whole oracle table inside one body."
  (:use code.test)
  (:require [std.lib.env :as env]
            [jvm.chisel.testing :as ct]
            [jvm.chisel.variant.measure :as measure]
            [jvm.chisel.variant.accumulate :as acc]
            [jvm.chisel.variant.squash :as sq]
            [jvm.chisel.variant.window :as win]))

(fact:global
 {:skip (not (env/program-exists? "verilator"))})

(defn- rand-bits [n] (vec (repeatedly n #(rand-int 2))))

(defn- expect-measure
  "Expect the five k-measure outputs of `ctx` to equal `[p [k0 k1 k2 k3]]`."
  [ctx [p [k0 k1 k2 k3]]]
  (let [{:keys [port expect]} ctx]
    (expect (port "p") p)
    (expect (port "k0") k0)
    (expect (port "k1") k1)
    (expect (port "k2") k2)
    (expect (port "k3") k3)))

^{:refer jvm.chisel.variant.measure/k-measure-module :added "4.1"}
(fact "k-measure co-sim matches k-measure-ref over fixed and random inputs"
  (let [vecs (concat [[1 0 1 0 1 1 0 0]
                      [1 1 1 1 1 1 1 1]
                      [0 0 0 0 0 0 0 0]
                      [1 0 1 0 1 0 1 0]]
                     (repeatedly 4 #(rand-bits 8)))]
    (ct/simulate
     (measure/k-measure-module {:n 8 :name "KMeasure8Co"})
     (fn [ctx]
       (doseq [v vecs]
         ((:poke ctx) ((:port ctx) "bits") (ct/pack v))
         ((:step ctx))
         (expect-measure ctx (measure/k-measure-ref v))))))
  => nil)

^{:refer jvm.chisel.variant.measure/c-measure-module :added "4.1"}
(fact "c-measure co-sim matches c-measure-ref"
  (let [vecs (concat [[1 0 1 0 1 1 0 0] [1 1 1 1 1 1 1 1] [0 0 0 0 0 0 0 0]]
                     (repeatedly 3 #(rand-bits 8)))]
    (ct/simulate
     (measure/c-measure-module {:n 8 :name "CMeasure8Co"})
     (fn [ctx]
       (let [{:keys [port poke expect]} ctx]
         (doseq [v vecs
                 :let [[p c] (measure/c-measure-ref v)]]
           (poke (port "bits") (ct/pack v))
           ((:step ctx))
           (expect (port "p") p)
           (expect (port "c") c))))))
  => nil)

^{:refer jvm.chisel.variant.squash/k-squash-module :added "4.1"}
(fact "k-squash co-sim matches k-squash-ref (blocks=2, R=3)"
  (let [vecs (concat [[1 0 1 1 1 1] [0 1 0 1 0 1] [1 1 0 0 0 1]]
                     (repeatedly 4 #(rand-bits 6)))]
    (ct/simulate
     (sq/k-squash-module {:blocks 2 :resolution 3 :name "KSq2x3Co"})
     (fn [ctx]
       (let [{:keys [port poke expect]} ctx]
         (doseq [v vecs]
           (poke (port "bits") (ct/pack v))
           ((:step ctx))
           (expect (port "out") (ct/pack (sq/k-squash-ref v 3))))))))
  => nil)

^{:refer jvm.chisel.variant.accumulate/k-accumulate-module :added "4.1"}
(fact "k-accumulate co-sim matches k-accumulate-ref per prefix"
  (let [streams [[1 0 1 1 1 1 0 1] [1 0 1 0 1 1 0 0 1 1] (rand-bits 12)]]
    (ct/simulate
     (acc/k-accumulate-module {:n-max 16 :name "KAcc16Co"})
     (fn [ctx]
       (let [{:keys [port poke step reset!]} ctx]
         (doseq [bits streams
                 :let [prefs (acc/k-accumulate-ref bits)]]
           (reset!)
           (dotimes [i (count bits)]
             (poke (port "bit") (nth bits i))
             (poke (port "valid") true)
             (step)
             (expect-measure ctx (nth prefs i))))))))
  => nil)

^{:refer jvm.chisel.variant.window/k-window-module :added "4.1"}
(fact "k-window co-sim matches k-window-ref after warm-up"
  (let [L 4
        streams [[1 0 1 1 1 1 0 1] [1 0 1 0 1 1 0 0 1 1] (rand-bits 10)]]
    (ct/simulate
     (win/k-window-module {:length L :name "KWin4Co"})
     (fn [ctx]
       (let [{:keys [port poke step reset!]} ctx]
         (doseq [bits streams
                 :let [wins (win/k-window-ref bits L)]]
           (reset!)
           (dotimes [i (count bits)]
             (poke (port "bit") (nth bits i))
             (poke (port "valid") true)
             (step)
             (when (>= i (dec L))
               (expect-measure ctx (nth wins (- i (dec L)))))))))))
  => nil)
