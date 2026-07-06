(ns documentation.std-concurrent-relay
  (:use code.test))

[[:hero {:title "std.concurrent.relay"
         :subtitle "interactive process and socket streams over a shared message bus"
         :lead "Wrap a process or socket as a lifecycle-aware relay with structured read, write, flush, exit, and custom stream operations."
         :actions [{:label "Concurrency overview" :href "std-concurrent.html"}]}]]

[[:chapter {:title "Overview" :link "overview"}]]

"A relay pairs input and output streams with a shared bus. It can attach to a running `Process` or `Socket`, or create one from process and network options. The transport namespace implements the low-level stream commands."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Start a process relay"}]]

(comment
  (require '[std.concurrent.relay :as relay])
  (require '[std.lib.component :as component])

  (def shell
    (relay/relay
     {:type :process
      :args ["cat"]
      :options {:receive {:format identity}
                :send {:format identity}}}))

  (relay/send shell {:op :partial :line "hello"})
  (relay/send shell {:op :read-line})
  (relay/send shell {:op :flush})
  (component/stop shell))

[[:section {:title "Attach to an existing endpoint"}]]

"Supply `:attached` when another part of the application already owns the `Process` or `Socket`. `relay:create` constructs without starting, while `relay` constructs and starts."

(comment
  (def attached-relay
    (relay/relay:create
     {:type :socket
      :attached existing-socket
      :options {}}))

  (component/start attached-relay)
  (relay/send attached-relay {:op :read-some :length 1024})
  (component/stop attached-relay))

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "std.concurrent.relay"}]]
[[:api {:namespace "std.concurrent.relay.transport"}]]
