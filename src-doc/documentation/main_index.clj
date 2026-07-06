(ns documentation.main-index)

[[:hero {:title "foundation"
         :subtitle "Libraries and tools for serious Clojure systems."
         :lead "Documentation is organized around four primary areas: `std` for standard libraries, `hara` for language tooling, `code` for development tooling, and `xt` for cross-target application layers."
         :badges ["std" "hara" "code" "xt"]
         :actions [{:label "Browse std" :href "std/index.html" :variant :primary}
                   {:label "Browse xt" :href "xt/index.html"}]}]]

[[:card-grid {:title "Sections"
              :lead "Each section maps to a major repository and test workflow family."
              :items [{:meta "Standard libraries"
                       :title "std"
                       :text "Core utilities, concurrency, filesystem, strings, time, dispatch, tasks, schedules, and std.lib references."
                       :href "std/index.html"}
                      {:meta "Language tooling"
                       :title "hara"
                       :text "hara.lang, language models, runtimes, typed xtalk, common emitters, seed generation, and generated examples."
                       :href "hara/index.html"}
                      {:meta "Development tooling"
                       :title "code"
                       :text "Custom tests, code management, structural queries, framework analysis, docs, project metadata, tools, and MCP."
                       :href "code/index.html"}
                      {:meta "Cross-target libraries"
                       :title "xt"
                       :text "xt.lang, xt.db, xt.net, xt.event, xt.substrate, walkthroughs, and parity examples."
                       :href "xt/index.html"}]}]]
