(ns documentation.main-contributing)

[[:hero {:title "Contributing"
          :subtitle "Setup, targeted tests, documentation, and pull requests."
          :lead "Contributions are easiest to review when they stay focused on one subsystem, include targeted verification, and update the matching documentation and examples."
          :actions [{:label "Read CONTRIBUTING.md" :href "https://github.com/zcaudate-xyz/foundation-base/blob/main/CONTRIBUTING.md" :variant :primary}
                    {:label "Open issues" :href "https://github.com/zcaudate-xyz/foundation-base/issues"}]}]]

[[:chapter {:title "Recommended workflow"}]]

[[:code {:lang "bash"}
  "git clone git@github.com:zcaudate-xyz/foundation-base.git\ncd foundation-base\nlein deps\nlein test :only <namespace-test>"]]

[[:card-grid {:items [{:meta "1"
                       :title "Keep the change focused"
                       :text "Avoid mixing unrelated refactors, dependency upgrades, generated output, and feature work."}
                      {:meta "2"
                       :title "Run targeted verification"
                       :text "State the exact namespace, runtime, or documentation command used."}
                      {:meta "3"
                       :title "Keep examples linked"
                       :text "Include authored source, build definitions, tests, generated output, and reproduction commands."}
                      {:meta "4"
                       :title "Update both entry points"
                       :text "When changing project positioning or navigation, update README.md and src-doc/documentation/main_index.clj together."}]}]]

[[:chapter {:title "Documentation"}]]

[[:code {:lang "bash"}
  "lein publish\nlein serve"]]

[[:callout {:tone :info
             :title "Wiki-ready pages"
             :content "The repository keeps GitHub Wiki-ready Markdown under `wiki/`. After the Wiki is initialized, those pages can be synchronized to the separate `foundation-base.wiki` repository."}]]
