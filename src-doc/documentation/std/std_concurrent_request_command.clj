(ns documentation.std-concurrent-request-command
  (:use code.test))

[[:hero {:title "std.concurrent.request-command"
         :subtitle "declarative command templates for request clients"
         :lead "Describe command construction, option selection, formatting, processing, and execution mode in a reusable value."
         :actions [{:label "Concurrency overview" :href "std-concurrent.html"}]}]]

[[:chapter {:title "Overview" :link "overview"}]]

"A `Command` separates public arguments from the command sent to a client. Input formatting runs before request execution; output formatting and process chains run afterward."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Define a command"}]]

(comment
  (require '[std.concurrent.request-command :as command])

  (def sum-command
    (command/req:command
     {:type :single
      :name :sum
      :arguments [:numbers]
      :function (fn [args _opts] args)
      :options {:default {} :select []}
      :format {}
      :process {}}))

  (command/req:run sum-command + [1 2 3]))

[[:section {:title "Choose an execution mode"}]]

"Use `:bulk` or `:transact` when the command function returns multiple requests. The `:retry` mode can rebuild and submit a command after a selected failure."

(comment
  (command/req:command
   {:type :bulk
    :function build-commands
    :options {:bulk {:async true}}
    :format {:input normalize-input
             :output normalize-output}
    :process {:chain [summarize]}}))

[[:chapter {:title "Extension point" :link "extension"}]]

"Implement another `run-request` method to add an application-specific execution mode while retaining the common formatting and processing pipeline."

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "std.concurrent.request-command"}]]
