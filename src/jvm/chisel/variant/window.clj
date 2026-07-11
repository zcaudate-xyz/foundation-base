(ns jvm.chisel.variant.window
  "Streaming sliding-window k-measure (the O(1)/cycle incremental kernel).

   A length-`L` shift register holds the current window (newest bit at index 0).
   On each valid cycle the window slides by one bit: the leaving left-edge pair
   `(head, nhead)` is subtracted from the transition counters and the entering
   right-edge pair `(tail, ntail)` is added; `p` is updated by `-head + ntail`.
   This is the inner loop of `math.variant.algorithm.k-stream/k-frequencies-bitset`
   (and the OpenCL `k-frequencies` kernel) made concrete as a circuit.

   Outputs are registered and become meaningful once `L` valid bits have been
   shifted in (warm-up); before then the shift register still contains reset
   zeros."
  (:require [jvm.chisel :as ch]
            [jvm.chisel.variant :as v]
            [jvm.chisel.variant.measure :as m]))

;; reference model -----------------------------------------------------------

(defn k-window-ref
  "Reference: the k-measure of every length-`L` window of `bits` (seq of 0/1),
   computed with the same incremental recurrence the circuit uses
   (subtract leaving pair, add entering pair). Equivalent to mapping
   `k-measure-ref` over each window; see `variant-window-test`.

   First output is the measure of bits[0..L-1]; each following output slides
   the window by one bit. Returns (n - L + 1) `[p [k0 k1 k2 k3]]` pairs."
  [bits length]
  (let [v   (vec bits)
        n   (count v)
        end (inc (- n length))]
    (when (pos? end)
      (let [idx (fn [prev cur] (+ cur (bit-shift-left prev 1)))
            [p0 k0] (m/k-measure-ref (subvec v 0 length))]
        (loop [i 1, p p0, k k0, acc [[p0 k0]]]
          (if (= i end)
            acc
            (let [head  (v (dec i))
                  nhead (v i)
                  tail  (v (+ i length -2))
                  ntail (v (+ i length -1))
                  p     (-> p (- head) (+ ntail))
                  k     (-> k
                            (update (idx head nhead) dec)
                            (update (idx tail ntail) inc))]
              (recur (inc i) p k (conj acc [p k])))))))))

;; module --------------------------------------------------------------------

(defn k-window-module
  "Sequential sliding-window k-measure. opts {:keys [length name]} (`length >= 2`).
   IO: `bit : UInt<1>`, `valid : UInt<1>` inputs; `p : UInt<pw>`,
   `k0..k3 : UInt<kw>` registered outputs (widths sized for a window of
   `length` bits). While `valid` is low the state holds."
  [{:keys [length name] :or {name "KWindow"}}]
  (let [pw (v/pw length)
        kw (v/kw length)]
    (ch/module
     {:name name}
     (fn []
       (let [io (ch/io (ch/bundle [[:bit   (ch/input  (ch/uint 1))]
                                   [:valid (ch/input  (ch/bool))]
                                   [:p     (ch/output (ch/uint pw))]
                                   [:k0    (ch/output (ch/uint kw))]
                                   [:k1    (ch/output (ch/uint kw))]
                                   [:k2    (ch/output (ch/uint kw))]
                                   [:k3    (ch/output (ch/uint kw))]]))
             bit   (ch/field io :bit)
             valid (ch/field io :valid)
             srR (ch/reg-init (ch/u 0 length))    ;; newest bit at index 0
             pR  (ch/reg-init (ch/u 0 pw))
             k0R (ch/reg-init (ch/u 0 kw))
             k1R (ch/reg-init (ch/u 0 kw))
             k2R (ch/reg-init (ch/u 0 kw))
             k3R (ch/reg-init (ch/u 0 kw))
             prev (ch/reg-init (ch/u 0 1))
             fill (ch/reg-init (ch/u 0 pw))        ;; 0..length
             head  (ch/index srR (dec length))
             nhead (ch/index srR (- length 2))
             tail  (ch/index srR 0)
             ntail bit
             leave (v/pair-bools head nhead)
             enter (v/pair-bools tail ntail)
             acc   (v/pair-bools prev bit)         ;; pair used while the window fills
             bump  (fn [reg l e w]
                     (v/truncate (ch/add (ch/sub reg l) e) w))
             sr-next (ch/cat (ch/bits-at srR (- length 2) 0) ntail)
             filling (ch/lt fill (ch/u length pw))]
         (ch/when valid
           (ch/connect! srR sr-next)               ;; shift every valid cycle
           (ch/when-else filling
             ;; warm-up: accumulate like `k-accumulate` until the register is full
             (do
               (ch/connect! pR (v/truncate (ch/add pR bit) pw))
               (ch/when (ch/gt fill (ch/u 0 pw))
                 (ch/connect! k0R (v/truncate (ch/add k0R (:k00 acc)) kw))
                 (ch/connect! k1R (v/truncate (ch/add k1R (:k01 acc)) kw))
                 (ch/connect! k2R (v/truncate (ch/add k2R (:k10 acc)) kw))
                 (ch/connect! k3R (v/truncate (ch/add k3R (:k11 acc)) kw)))
               (ch/connect! prev bit)
               (ch/connect! fill (v/truncate (ch/add fill (ch/u 1 pw)) pw)))
             ;; steady state: drop the leaving left-edge pair, add the entering one
             (do
               (ch/connect! pR  (v/truncate (ch/add (ch/sub pR head) ntail) pw))
               (ch/connect! k0R (bump k0R (:k00 leave) (:k00 enter) kw))
               (ch/connect! k1R (bump k1R (:k01 leave) (:k01 enter) kw))
               (ch/connect! k2R (bump k2R (:k10 leave) (:k10 enter) kw))
               (ch/connect! k3R (bump k3R (:k11 leave) (:k11 enter) kw)))))
         (ch/connect! (ch/field io :p)  pR)
         (ch/connect! (ch/field io :k0) k0R)
         (ch/connect! (ch/field io :k1) k1R)
         (ch/connect! (ch/field io :k2) k2R)
         (ch/connect! (ch/field io :k3) k3R))))))
