(ns documentation.std-concurrent-atom
  (:use code.test))

[[:hero {:title "std.concurrent.atom"
         :subtitle "batched atom queues and trackable asynchronous hubs"
         :lead "Use atom-backed queues when producers should submit quickly and a single executor should process entries in controlled batches."
         :actions [{:label "Concurrency overview" :href "std-concurrent.html"}]}]]

[[:chapter {:title "Overview" :link "overview"}]]

"`std.concurrent.atom` provides two related patterns. `aq:*` functions maintain a simple vector-backed queue, while `hub:*` functions attach a future ticket to each batch so callers can observe completion."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Create a batched submitter"}]]

"Create an executor with a target, a bulk handler, a maximum batch size, and a short collection interval. The returned map contains the queue, executor, and submission function."

(comment
  (require '[std.concurrent.atom :as atom])

  (def worker
    (atom/aq:executor
     {:target :events
      :handler (fn [target entries]
                 {:target target :entries entries})
      :max-batch 100
      :interval 25}))

  ((:submit worker) {:id 1} {:id 2}))

[[:section {:title "Wait for a tracked batch"}]]

"Use a hub executor when the caller needs a future representing the batch result. `hub:wait` blocks only while queued work remains."

(comment
  (def tracked
    (atom/hub:executor
     nil
     {:handler (fn [_ entries] entries)
      :max-batch 100
      :interval 25}))

  (let [[ticket start count submitted?]
        ((:submit tracked) :a :b :c)]
    {:result @ticket
     :range [start count]
     :submitted? submitted?})

  (atom/hub:wait (:queue tracked)))

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "std.concurrent.atom"}]]
