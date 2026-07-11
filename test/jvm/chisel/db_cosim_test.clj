(ns jvm.chisel.db-cosim-test
  "Co-simulation of the `jvm.chisel.db` operators against their Clojure reference
   models, using Chisel's native `chisel3.simulator` (Verilator).

   Skipped unless `verilator` is on PATH. Each `ct/simulate` recompiles the
   design, so every fact drives its whole oracle table inside one body."
  (:use code.test)
  (:require [std.lib.env :as env]
            [jvm.chisel.testing :as ct]
            [jvm.chisel.db.reduce :as reduce]
            [jvm.chisel.db.scan :as scan]
            [jvm.chisel.db.hash :as hash]
            [jvm.chisel.db.bloom :as bloom]))

(fact:global
 {:skip (not (env/program-exists? "verilator"))})

(defn- rand-vals [n width]
  (vec (repeatedly n #(rand-int (bit-shift-left 1 width)))))

(defn- b01 [b] (if b 1 0))

^{:refer jvm.chisel.db.reduce/reduce-module :added "4.1"}
(fact "reduce co-sim matches reduce-ref for :sum :count :min :max"
  (let [lanes 8 width 8
        cases (concat [(mapv (fn [i] (inc (* i 3))) (range lanes))
                       [255 0 255 0 255 0 255 0]
                       [7 7 7 7 7 7 7 7]]
                      (repeatedly 4 #(rand-vals lanes width)))
        masks [0xFF 0x0F 0xF0 0xAA 0x55 0x00 0x01]]
    (doseq [op [:sum :count :min :max]]
      (ct/simulate
       (reduce/reduce-module {:lanes lanes :width width :op op
                              :name (str "Reduce" (name op) "Co")})
       (fn [ctx]
         (let [{:keys [port poke expect step]} ctx]
           (doseq [vals cases
                   mask masks
                   :let [want (reduce/reduce-ref vals mask op width)]]
             (ct/poke-vec! ctx "values" vals)
             (poke (port "validMask") (long mask))
             (step)
             (expect (port "result") (long want))))))))
  => nil)

^{:refer jvm.chisel.db.scan/scan-module :added "4.1"}
(fact "scan co-sim matches scan-ref (single and multi-predicate)"
  (let [lanes 8 width 8
        pred-sets [[[:eq 0]]
                   [[:eq 3] [:gte 2]]
                   [[:neq 0] [:lt 6]]]
        cases (concat [(mapv (fn [i] i) (range lanes))
                       [3 3 3 3 3 3 3 3]
                       [0 1 2 3 4 5 6 7]]
                      (repeatedly 3 #(rand-vals lanes width)))
        masks [0xFF 0x0F 0xAA 0x55]]
    (doseq [preds pred-sets]
      (ct/simulate
       (scan/scan-module {:lanes lanes :width width :preds preds
                          :name (str "Scan" (count preds) "Co")})
       (fn [ctx]
         (let [{:keys [port poke expect step]} ctx]
           (doseq [vals cases
                   mask masks
                   :let [want (scan/scan-ref vals mask preds)]]
             (ct/poke-vec! ctx "values" vals)
             (poke (port "validMask") (long mask))
             (doseq [[pi [_ c]] (map-indexed vector preds)]
               (poke (port (str "c" pi)) (long c)))
             (step)
             (expect (port "matchMask") (long want))))))))
  => nil)

^{:refer jvm.chisel.db.hash/hash-module :added "4.1"}
(fact "hash co-sim matches hash-ref over a sweep of keys"
  (let [width 8 buckets 16 log-n 4 k 0x9E
        keys (concat (range 32) (repeatedly 16 #(rand-int 256)))]
    (ct/simulate
     (hash/hash-module {:width width :buckets buckets :k k :name "HashCo"})
     (fn [ctx]
       (let [{:keys [port poke expect step]} ctx]
         (doseq [key keys
                 :let [want (hash/hash-ref key width k log-n)]]
           (poke (port "key") (long key))
           (step)
           (expect (port "bucket") (long want)))))))
  => nil)

^{:refer jvm.chisel.db.bloom/bloom-probe-module :added "4.1"}
(fact "bloom-probe co-sim matches bloom-probe-ref"
  (let [width 8 bits-count 16 ks [0x9E 0x5D]
        ;; build a few bit-vectors by inserting some keys into the reference
        inserted (reduce (fn [b key] (bloom/bloom-insert-ref key b width bits-count ks))
                         0 [3 7 11 42])
        cases (concat (map (fn [k] [k inserted]) [3 7 11 42 0 1 2 4 5 99])
                      (repeatedly 16 #(vector (rand-int 256) (rand-int (bit-shift-left 1 bits-count)))))]
    (ct/simulate
     (bloom/bloom-probe-module {:width width :bits-count bits-count :ks ks :name "BloomProbeCo"})
     (fn [ctx]
       (let [{:keys [port poke expect step]} ctx]
         (doseq [[key bits] cases
                 :let [want (bloom/bloom-probe-ref key bits width bits-count ks)]]
           (poke (port "key") (long key))
           (poke (port "bits") (long bits))
           (step)
           (expect (port "hit") (long (b01 want))))))))
  => nil)

^{:refer jvm.chisel.db.bloom/bloom-insert-ref :added "4.1"}
(fact "bloom probe hits every ref-inserted key (insert -> probe round trip)"
  (let [width 8 bits-count 16 ks [0x9E 0x5D]
        insert-keys [3 7 11 42 99 200]
        bits (reduce (fn [b k] (bloom/bloom-insert-ref k b width bits-count ks))
                     0 insert-keys)]
    (ct/simulate
     (bloom/bloom-probe-module {:width width :bits-count bits-count :ks ks
                                :name "BloomRoundTripCo"})
     (fn [ctx]
       (let [{:keys [port poke expect step]} ctx]
         (poke (port "bits") (long bits))
         (doseq [k insert-keys]
           (assert (bloom/bloom-probe-ref k bits width bits-count ks)
                   "reference must consider inserted keys present")
           (poke (port "key") (long k))
           (step)
           (expect (port "hit") (long 1)))))))
  => nil)
