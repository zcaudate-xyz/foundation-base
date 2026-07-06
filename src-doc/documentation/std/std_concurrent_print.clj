(ns documentation.std-concurrent-print
  (:use code.test))

[[:hero {:title "std.concurrent.print"
         :subtitle "serialized asynchronous printing for concurrent code"
         :lead "Route print operations through a single batched executor so output from concurrent tasks remains ordered and readable."
         :actions [{:label "Concurrency overview" :href "std-concurrent.html"}]}]]

[[:chapter {:title "Overview" :link "overview"}]]

"`std.concurrent.print` provides drop-in `print`, `println`, `prn`, and `pprint` functions. In local execution mode they enqueue output through a shared atom executor; outside that mode they fall back to Clojure's standard printing functions."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Use concurrent printing"}]]

(comment
  (require '[std.concurrent.print :as concurrent-print])

  (future (concurrent-print/println "worker-a" 1))
  (future (concurrent-print/println "worker-b" 2))
  (concurrent-print/pprint {:status :running :workers 2}))

[[:section {:title "Submit raw fragments"}]]

"`submit` accepts one or more fragments and places them directly onto the print queue. `get-executor` returns the shared resource and restarts it if its executor has stopped."

(comment
  (concurrent-print/submit "progress " 50 "%\n")
  (keys (concurrent-print/get-executor))
  (concurrent-print/pprint-str {:a 1 :b 2}))

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "std.concurrent.print"}]]
