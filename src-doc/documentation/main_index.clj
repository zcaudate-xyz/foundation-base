(ns documentation.main-index)

[[:hero {:title "Foundation Base"
          :subtitle "A Clojure-first toolkit for building, generating, testing, and operating polyglot systems."
          :lead "Foundation Base combines reusable Clojure libraries, developer tooling, language generation, and runtime integration in one repository. Start with the area that matches what you want to do; you do not need to understand the entire codebase first."
          :badges ["std" "code" "hara + rt" "xt"]
          :actions [{:label "Getting started" :href "getting-started.html" :variant :primary}
                    {:label "Browse examples" :href "examples.html"}
                    {:label "Contribute" :href "contributing.html"}]}]]

[[:section {:title "Choose your path"}]]

[[:card-grid {:lead "Foundation Base supports several distinct workflows. Pick one entry point and expand from there."
              :items [{:meta "Standard libraries"
                       :title "Use reusable Clojure infrastructure"
                       :text "Collections, concurrency, filesystems, strings, time, tasks, scheduling, configuration, data handling, and system utilities."
                       :href "std/index.html"}
                      {:meta "Language generation"
                       :title "Generate code for other languages"
                       :text "Use Hara books and grammars to emit JavaScript, Lua, Python, Go, SQL, Solidity, and other targets from a shared Clojure authoring model."
                       :href "hara/introduction.html"}
                      {:meta "Examples"
                       :title "Compare source with generated projects"
                       :text "Follow walkthroughs and open authored Clojure source, build definitions, generated repositories, and reproduction commands."
                       :href "examples.html"}
                      {:meta "Developer tooling"
                       :title "Test, document, query, and maintain code"
                       :text "Use code.test, code.doc, code.query, code.manage, project metadata, build tools, and analysis utilities."
                       :href "code-tools.html"}
                      {:meta "Cross-target libraries"
                       :title "Build portable application layers"
                       :text "Explore xt.lang, xt.db, xt.net, xt.event, xt.substrate, walkthroughs, and parity examples built on top of the language tooling."
                       :href "xt/index.html"}
                      {:meta "Contributing"
                       :title "Work on Foundation Base"
                       :text "Set up the repository, run targeted tests, update documentation, and prepare focused pull requests."
                       :href "contributing.html"}]}]]

[[:chapter {:title "Repository map"}]]

[[:card-grid {:items [{:meta "std.*"
                       :title "Standard libraries"
                       :text "Reusable infrastructure and application-level utilities."
                       :href "std/index.html"}
                      {:meta "code.*"
                       :title "Development tools"
                       :text "Testing, documentation, structural queries, maintenance, project tooling, and analysis."
                       :href "code-tools.html"}
                      {:meta "hara.* and rt.*"
                       :title "Languages and runtimes"
                       :text "Grammar-driven code generation, typing, target models, and runtime adapters."
                       :href "hara/index.html"}
                      {:meta "xt.*"
                       :title "Portable libraries"
                       :text "Cross-target libraries and application layers built with the language tooling."
                       :href "xt/index.html"}]}]]

[[:chapter {:title "Quick start"}]]

"Foundation Base currently uses a local Maven installation workflow. Install Java 21 and Leiningen, then clone and install the project."

[[:code {:lang "bash"}
  "git clone git@github.com:zcaudate-xyz/foundation-base.git\ncd foundation-base\nlein install\nlein repl"]]

[[:section {:title "Try a standard-library helper"}]]

[[:code {:lang "clojure"}
  "(require '[std.lib :as h])\n\n(h/time-ms)\n(h/pl \"Hello Foundation!\")"]]

[[:section {:title "Generate target-language code"}]]

"`hara.lang` stores code in a reusable intermediate form and emits it through a target grammar. The language tooling also supports modules, dependency tracking, inspection, testing, and runtime execution."

[[:code {:lang "clojure"}
  "(require '[hara.lang :as l])\n\n(l/emit-as :js '(+ 1 2 3))\n;; => \"1 + 2 + 3\""]]

[[:section {:title "Run a targeted test"}]]

"Foundation Base uses `code.test`, a fact-based test framework. Targeted namespace runs are the recommended development workflow because some test groups require optional runtimes and external services."

[[:code {:lang "bash"}
  "lein test :only std.lib.collection-test\nlein test :with std.lib"]]

[[:chapter {:title "How to interact with the project"}]]

[[:card-grid {:items [{:meta "Library consumer"
                       :title "Depend on the modules you need"
                       :text "Install locally, use documented public namespaces, and check the relevant guide and tests before adopting an unfamiliar subsystem."}
                      {:meta "Explorer"
                       :title "Follow one focused workflow"
                       :text "Emit a small JS or Lua form, run one code.test namespace, inspect a generated project, and browse the matching documentation page."
                       :href "examples.html"}
                      {:meta "Contributor"
                       :title "Make targeted changes"
                       :text "Use the contributor guide for environment setup, conventions, documentation generation, and pull-request expectations."
                       :href "contributing.html"}
                      {:meta "Wiki reader"
                       :title "Browse topic-oriented pages"
                       :text "Wiki-ready Markdown pages are kept in the repository and can be synchronized to GitHub Wiki after it is initialized."
                       :href "https://github.com/zcaudate-xyz/foundation-base/tree/main/wiki"}]}]]

[[:chapter {:title "Project status"}]]

"Foundation Base contains production-used libraries, evolving developer tooling, and experimental language/runtime integrations. Documentation should identify subsystems as **Stable**, **Usable**, or **Experimental** so readers can understand the expected level of compatibility and support."

[[:callout {:tone :info
             :title "Start small"
             :content "The complete repository spans many languages and runtime environments. Begin with one namespace, guide, walkthrough, or generated example rather than trying to configure every optional dependency at once."}]]
