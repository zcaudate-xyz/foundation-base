(ns chisel.examples.db-join
  "A tiny direct-mapped hash join: build a table from build-side keys, then probe it.
   Reference + FIRRTL for both the build and probe modules, plus the same query as a
   composed DAG plan through the scheduler + pipeline."
  (:require [jvm.chisel :as ch]
            [jvm.chisel.db.join :as j]
            [jvm.chisel.db.schedule :as sched]
            [jvm.chisel.db.pipeline :as pipe]))

(def width 8)
(def buckets 16)
(def k 0x9E)

;; the same query as a DAG plan: scan source 0 -> build, scan source 1 -> probe -> count
(def join-count-plan
  {:width width :lanes 8 :sources 2
   :nodes [{:id :b0 :op :scan       :inputs [[:src 0]] :preds [[:gte 1]]}
           {:id :bt :op :join-build :inputs [:b0]      :buckets buckets :k k}
           {:id :p0 :op :scan       :inputs [[:src 1]] :preds [[:lte 250]]}
           {:id :jp :op :join-probe :inputs [:p0 :bt]}
           {:id :r  :op :reduce     :inputs [:jp]      :reduce-op :count}]})

(comment
  (def build-keys [3 7 11 42 99 200 0 1])
  (def table (j/join-build-ref build-keys 0xFF width buckets k))
  ;; => {:valid <bucket bitmask> :keys <length-16 vector, last-writer-wins>}

  ;; probe an inserted key and an absent key
  (j/join-probe-ref 42 table width buckets k)   ;; => true
  (j/join-probe-ref 60 table width buckets k)   ;; => false (bucket 2 empty)

  ;; elaborate both modules
  (println (ch/emit-firrtl
            (j/join-build-module {:lanes 8 :width width :buckets buckets :k k
                                  :name "JoinBuild8"})))
  (println (ch/emit-system-verilog
            (j/join-probe-module {:width width :buckets buckets :k k
                                  :name "JoinProbe8"})))

  ;; --- the query as a DAG plan: place, execute the reference, elaborate ---
  (sched/schedule join-count-plan {:scan 2 :join 2 :aggregate 1})
  ;; => {:ok? true :placement [.. :scan-0 :join-0 :scan-1 :join-1 :aggregate-0] :cost 40}

  (sched/run-plan join-count-plan
                  {:sources [{:values [3 7 11 42 99 200 0 1] :validMask 0xFF}
                             {:values [3 60 7 255 42 11 5 200] :validMask 0xFF}]})
  ;; => {:mask <probe hits> :result <join count> :env {...}}

  (println (ch/emit-firrtl
            (pipe/pipeline-module (assoc join-count-plan :name "JoinCount8")))))
