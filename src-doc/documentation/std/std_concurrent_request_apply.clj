(ns documentation.std-concurrent-request-apply
  (:use code.test))

[[:hero {:title "std.concurrent.request-apply"
         :subtitle "applicative functions backed by request clients"
         :lead "Package request construction, bulk behavior, retries, transforms, and default clients as invokable applicative values."
         :actions [{:label "Concurrency overview" :href "std-concurrent.html"}]}]]

[[:chapter {:title "Overview" :link "overview"}]]

"`ReqApplicative` bridges `std.lib.apply` and the request orchestration layer. Calling an applicative builds one or more commands, selects single, bulk, transactional, or retry execution, and optionally transforms inputs and outputs."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Construct an applicative"}]]

(comment
  (require '[std.concurrent.request-apply :as request-apply])

  (def add-request
    (request-apply/req:applicative
     {:type :single
      :client +
      :function (fn [args _opts] args)
      :options {}
      :transform {}}))

  (add-request 1 2 3))

[[:section {:title "Select an execution mode"}]]

"Use `:bulk` or `:transact` when the function returns several applicatives. Use `:retry` with a predicate and retry function to recover from selected failures."

(comment
  (request-apply/req:applicative
   {:type :bulk
    :client client
    :function build-commands
    :options {:bulk {:async true}}
    :transform {:in normalize-input
                :out normalize-output}}))

[[:chapter {:title "Extension point" :link "extension"}]]

"Add another `req-call` method when an application needs a request execution strategy beyond the built-in single, bulk, transaction, and retry modes."

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "std.concurrent.request-apply"}]]
