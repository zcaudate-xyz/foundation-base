(ns documentation.main-index)

[[:hero {:title "foundation"
         :subtitle "Libraries and tools for serious Clojure systems."
         :lead "Documentation is split into volumes by concern. The std volume is available now; code, hara, and guides volumes will follow."
         :badges ["Clojure" "Static Docs" "API Reference"]
         :actions [{:label "Browse std" :href "std/index.html" :variant :primary}]}]]

[[:callout {:tone :info
            :title "Volumes"
            :content "Docs are split by concern: **std.*** for standard libraries, **code.*** for development tools, **hara.*** for language tooling, and **guides** for contribution and styling."}]]

[[:card-grid {:title "Explore"
              :lead "Available volume."
              :items [{:meta "Std"
                       :title "std.*"
                       :text "Standard libraries: blocks, collections, atoms, streams, environments, and foundation helpers."
                       :href "std/index.html"}]}]]
