(ns documentation.std-concurrent-request
  (:use code.test))

[[:hero {:title "std.concurrent.request"
         :subtitle "single, bulk, asynchronous, and transactional request orchestration"
         :lead "Define one client request protocol and reuse it across synchronous calls, futures, batching, transformations, and transactions."
         :actions [{:label "Concurrency overview" :href "std-concurrent.html"}]}]]

[[:chapter {:title "Overview" :link "overview"}]]

"Request clients implement `std.protocol.request/IRequest`. The orchestration layer prepares options, invokes single or bulk transport functions, processes outputs, and applies pre, post, chain, timing, and error handlers. Plain functions also implement the request protocol."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Make a single request"}]]

(comment
  (require '[std.concurrent.request :as request])

  ;; Functions are valid clients; each command is an argument vector.
  (request/req + [1 2 3])

  (request/req + [1 2 3]
               {:pre [(fn [args] (conj args 4))]
                :post [inc]
                :chain [str]}))

[[:section {:title "Run asynchronously"}]]

"Set `:async` to receive a future. `:measure` can be a callback, atom, or map of start, stop, and timer handlers."

(comment
  (def pending
    (request/req + [1 2 3]
                 {:async true
                  :measure println}))
  @pending)

[[:section {:title "Batch and transact"}]]

(comment
  (request/req:bulk [+ {:async false}]
    (request/req + [1 2])
    (request/req + [3 4]))

  (request/req:transact [+ {}]
    (request/req + [1 2])
    (request/req + [3 4]))

  (request/bulk:map + request/req-fn [[1 2] [3 4]]))

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "std.concurrent.request"}]]
