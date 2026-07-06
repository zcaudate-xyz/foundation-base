(ns documentation.std-concurrent-queue
  (:use code.test))

[[:hero {:title "std.concurrent.queue"
         :subtitle "blocking queues, deques, timeouts, and bulk draining"
         :lead "Use JVM blocking collections through a small Clojure API with consistent timeout units and queue-processing helpers."
         :actions [{:label "Concurrency overview" :href "std-concurrent.html"}]}]]

[[:chapter {:title "Overview" :link "overview"}]]

"The namespace constructs unbounded, fixed, limited, and double-ended blocking queues. Operations accept keyword time units such as `:ms`, `:s`, and `:m`."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Produce and consume"}]]

(comment
  (require '[std.concurrent.queue :as queue])

  (def jobs (queue/queue:fixed 10))

  (queue/put jobs {:id 1})
  (queue/put jobs {:id 2} 100 :ms)
  (queue/peek jobs)
  (queue/take jobs 1 :s)
  (queue/remaining-capacity jobs))

[[:section {:title "Drain work in batches"}]]

(comment
  (queue/drain (queue/queue 1 2 3 4) 2)

  (queue/process-bulk
   (fn [batch]
     (println "processing" batch))
   (queue/queue 1 2 3 4 5)
   3))

[[:section {:title "Use both ends"}]]

"A deque supports access through `put-first`, `put-last`, `take-first`, and `take-last`."

(comment
  (def work (queue/deque :middle))
  (queue/put-first work :urgent)
  (queue/put-last work :normal)
  [(queue/take-first work)
   (queue/take-last work)])

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "std.concurrent.queue"}]]
