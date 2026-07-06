(ns documentation.xt-index)

[[:hero {:title "xt"
         :subtitle "Cross-target libraries, substrates, and runtime building blocks."
         :lead "`xt.*` is the portable application layer built on hara.lang and xtalk. It contains language-common libraries, database builders, event helpers, network adapters, and substrate transports that can be emitted or exercised across runtimes."
         :badges ["xtalk" "portable" "runtime" "substrate"]
         :actions [{:label "Back to home" :href "../index.html" :variant :primary}]}]]

[[:card-grid {:title "Areas"
              :lead "The section follows the `src-lang/xt` layout and the `[xt.]` test split in the CI workflow."
              :items [{:meta "Language"
                       :title "xt.lang"
                       :text "Portable common libraries, specs, promises, resources, traces, trees, and parser utilities."
                       :href "xt-lang.html"}
                      {:meta "Database"
                       :title "xt.db"
                       :text "Text builders, system implementations, node clients/kernels/proxies, and walkthroughs."
                       :href "xt-db.html"}
                      {:meta "Network"
                       :title "xt.net"
                       :text "HTTP fetch, SQL and Redis connections, websocket helpers, and Supabase integration."
                       :href "xt-net.html"}
                      {:meta "Events"
                       :title "xt.event"
                       :text "Event, route, model, form, log, validation, animation, and throttle helpers."
                       :href "xt-event.html"}
                      {:meta "Substrate"
                       :title "xt.substrate"
                       :text "Frame, JSON, pubsub, request, router, space, page, proxy, and transport layers."
                       :href "xt-substrate.html"}
                      {:meta "Examples"
                       :title "xt examples"
                       :text "POCs, substrate walkthroughs, xtbench coverage, and generated xtalk projects."
                       :href "xt-examples.html"}]}]]
