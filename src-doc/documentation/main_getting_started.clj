(ns documentation.main-getting-started)

[[:hero {:title "Getting Started"
          :subtitle "Install Foundation Base and choose one focused workflow."
          :lead "The repository spans many libraries and runtimes. Start with Java, Leiningen, and one target area; optional tools can be added later."
          :actions [{:label "Full setup guide" :href "https://github.com/zcaudate-xyz/foundation-base/blob/main/GETTING_STARTED.md" :variant :primary}
                    {:label "Contributor guide" :href "contributing.html"}]}]]

[[:chapter {:title "Install"}]]

[[:code {:lang "bash"}
  "git clone git@github.com:zcaudate-xyz/foundation-base.git\ncd foundation-base\nlein deps\nlein install\nlein repl"]]

[[:chapter {:title "Choose a first task"}]]

[[:card-grid {:items [{:meta "std"
                       :title "Use a Clojure library"
                       :text "Start with std.lib and a focused guide."
                       :href "std/index.html"}
                      {:meta "hara"
                       :title "Generate target code"
                       :text "Emit a JavaScript or Lua form, then follow the language walkthroughs."
                       :href "hara/introduction.html"}
                      {:meta "code.test"
                       :title "Run one test namespace"
                       :text "Use a targeted run rather than configuring every optional runtime."
                       :href "https://github.com/zcaudate-xyz/foundation-base/blob/main/src-doc/documentation/code/code_test.clj"}
                      {:meta "examples"
                       :title "Inspect generated projects"
                       :text "Compare authored Clojure source with generated repositories."
                       :href "examples.html"}]}]]

[[:section {:title "Targeted tests"}]]

[[:code {:lang "bash"}
  "lein test :only std.lib.collection-test\nlein test :with std.lib"]]

[[:callout {:tone :warning
             :title "Optional environments"
             :content "Some test groups require Node.js, Python, R, Docker, PostgreSQL, OpenResty, native compilers, browser drivers, or other external programs. Install only what your selected subsystem needs."}]]
