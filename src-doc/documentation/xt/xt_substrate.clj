(ns documentation.xt-substrate
  (:use code.test))

[[:hero {:title "xt.substrate"
         :subtitle "Frames, pubsub, requests, routers, spaces, pages, and transports."
         :lead "`xt.substrate` provides the message and transport foundation used by portable clients, workers, websocket servers, pages, and proxy layers."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"A generated application often needs to send frames, route requests, multiplex spaces, publish events, and move messages through memory, browser, or websocket transports. The substrate layer makes those concerns explicit and reusable."

[[:chapter {:title "How to use it" :link "usage"}]]

"Start with frames and spaces, add request/router or pubsub behavior, then choose a transport. Page and proxy utilities are higher-level helpers for browser and worker usage."

[[:chapter {:title "API" :link "api"}]]

