(ns jvm.chisel.db-pipeline-cosim-test
  "End-to-end co-simulation of the composed `jvm.chisel.db.pipeline` datapath
   against the golden `jvm.chisel.db.schedule/run-plan` reference, using
   `chisel3.simulator` (Verilator).

   Skipped unless `verilator` is on PATH. One `ct/simulate` per plan shape; each
   batches its whole input table inside the body."
  (:use code.test)
  (:require [std.lib.env :as env]
            [jvm.chisel.testing :as ct]
            [jvm.chisel.db.pipeline :as pipe]
            [jvm.chisel.db.schedule :as sched]))

(fact:global
 {:skip (not (env/program-exists? "verilator"))})

(def ^:private lanes 8)
(def ^:private width 8)
(def ^:private bits-count 16)
(def ^:private ks [0x9E 0x5D])
(def ^:private buckets 16)

(defn- rand-vals []
  (vec (repeatedly lanes #(rand-int (bit-shift-left 1 width)))))

(defn- plan-consts
  "Flat list of scan-stage constants in global c-index order (matches the
   pipeline's c0..cK assignment)."
  [stages]
  (vec (mapcat (fn [s] (when (= :scan (:op s)) (map second (:preds s)))) stages)))

(defn- drive
  "Run one input vector through `ctx` and check against the `run-plan` oracle.
   `has-bits?`/`has-hash?`/`has-reduce?` select which ports exist."
  [ctx plan input has-bits? has-hash? has-reduce?]
  (let [{:keys [port poke expect step]} ctx
        oracle (sched/run-plan plan input)]
    (ct/poke-vec! ctx "values" (:values input))
    (poke (port "validMask") (long (:validMask input)))
    (when has-bits? (poke (port "bits") (long (:bits input))))
    (doseq [[n c] (map-indexed vector (plan-consts (:stages plan)))]
      (poke (port (str "c" n)) (long c)))
    (step)
    (expect (port "matchMask") (long (:mask oracle)))
    (when has-hash?   (ct/expect-vec! ctx "buckets" (:buckets oracle)))
    (when has-reduce? (expect (port "result") (long (:result oracle))))))

(defn- inputs
  "Edge cases + random inputs. `with-bits?` adds a :bits field."
  [with-bits?]
  (let [fixed [{:values (vec (range lanes)) :validMask 0xFF}
               {:values (vec (repeat lanes 3)) :validMask 0xFF}
               {:values [255 0 255 0 255 0 255 0] :validMask 0xFF}
               {:values (vec (range lanes)) :validMask 0x00}
               {:values (vec (range lanes)) :validMask 0xAA}]
        rnd   (repeatedly 6 #(hash-map :values (rand-vals)
                                       :validMask (rand-int 256)))
        base  (concat fixed rnd)]
    (if with-bits?
      (map #(assoc % :bits (rand-int (bit-shift-left 1 bits-count))) base)
      (vec base))))

(defn- run-shape
  "Build + simulate one plan shape over its input table."
  [stages has-bits? has-hash? has-reduce? nm]
  (let [plan {:width width :lanes lanes :stages stages}]
    (ct/simulate
     (pipe/pipeline-module {:width width :lanes lanes :stages stages :name nm})
     (fn [ctx]
       (doseq [inp (inputs has-bits?)]
         (drive ctx plan inp has-bits? has-hash? has-reduce?))))))

^{:refer jvm.chisel.db.pipeline/pipeline-module :added "4.1"}
(fact "pipeline co-sim matches run-plan across composed plan shapes"
  ;; scan[:gte 20] -> reduce :sum   (exercises the widen fix for :sum)
  (run-shape [{:op :scan :preds [[:gte 20]]}
              {:op :reduce :reduce-op :sum}]
             false false true "PipeSumCo")
  ;; scan[:eq 3] -> reduce :count
  (run-shape [{:op :scan :preds [[:eq 3]]}
              {:op :reduce :reduce-op :count}]
             false false true "PipeCountCo")
  ;; scan -> bloom-probe -> reduce :sum
  (run-shape [{:op :scan :preds [[:lte 200]]}
              {:op :bloom-probe :bits-count bits-count :ks ks}
              {:op :reduce :reduce-op :sum}]
             true false true "PipeBloomSumCo")
  ;; scan -> hash -> reduce :count (also checks buckets Vec output)
  (run-shape [{:op :scan :preds [[:gte 1]]}
              {:op :hash :buckets buckets :k 0x9E}
              {:op :reduce :reduce-op :count}]
             false true true "PipeHashCo")
  ;; full chain: scan -> bloom -> hash -> reduce :sum
  (run-shape [{:op :scan :preds [[:gte 5] [:lte 250]]}
              {:op :bloom-probe :bits-count bits-count :ks ks}
              {:op :hash :buckets buckets :k 0x9E}
              {:op :reduce :reduce-op :sum}]
             true true true "PipeFullCo")
  => nil)
