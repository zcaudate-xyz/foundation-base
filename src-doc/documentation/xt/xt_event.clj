(ns documentation.xt-event
  (:use code.test))

[[:hero {:title "xt.event"
         :subtitle "Event, model, route, form, log, and validation layers."
         :lead "`xt.event` is a portable event/application layer for boxes, forms, models, routes, listeners, logs, animation, validation, decoration, tasks, and throttling."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"Application state and UI flows need common event structures across runtimes. The event layer gives generated systems a shared model for routing, forms, logs, listeners, animation, and validation."

[[:chapter {:title "Internal usage" :link "internal"}]]

"Tests under `test-lang/xt/event` cover each base and utility namespace. React and substrate examples use these concepts when coordinating routes, models, and UI events."

[[:chapter {:title "API" :link "api"}]]

