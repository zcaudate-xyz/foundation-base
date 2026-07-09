(ns documentation.main-examples
  (:use code.test))

[[:hero {:title "Examples"
          :subtitle "Walkthroughs, generated projects, source, tests, and runnable repositories."
          :lead "Examples are a primary way to understand Foundation Base. Each entry keeps the authored Clojure source, build definition, generated repository, and relevant walkthrough close together."
          :badges ["Walkthroughs" "Generated projects" "Source links" "Runnable output"]
          :actions [{:label "Hara walkthroughs" :href "hara/walkthrough-basic.html" :variant :primary}
                    {:label "Repository examples index" :href "https://github.com/zcaudate-xyz/foundation-base/blob/main/wiki/Examples.md"}]}]]

[[:chapter {:title "Language walkthroughs"}]]

[[:card-grid {:items [{:meta "00"
                       :title "Basic authoring"
                       :text "Install language contexts, emit forms, and work with generated pointers."
                       :href "https://github.com/zcaudate-xyz/foundation-base/blob/main/src-doc/walkthrough/std_lang_00_basic.clj"}
                      {:meta "01"
                       :title "Multiple languages"
                       :text "Use the shared authoring model across more than one target grammar."
                       :href "https://github.com/zcaudate-xyz/foundation-base/blob/main/src-doc/walkthrough/std_lang_01_multi.clj"}
                      {:meta "02"
                       :title "Live evaluation"
                       :text "Connect emitted code to runtime adapters for interactive execution."
                       :href "https://github.com/zcaudate-xyz/foundation-base/blob/main/src-doc/walkthrough/std_lang_02_live.clj"}]}]]

[[:chapter {:title "Generated project examples"}]]

[[:card-grid {:lead "Open the generated repository to see the target project, or open the source and build links to see how Foundation Base produces it."
              :items [{:meta "C / pthreads"
                       :title "Hello from native threads"
                       :text "A small generated C project using pthreads."
                       :href "https://github.com/hoebat/play.c-000-pthreads-hello"}
                      {:meta "OpenResty / Lua"
                       :title "NGX hello"
                       :text "A generated OpenResty project demonstrating Lua emission and server integration."
                       :href "https://github.com/hoebat/play.ngx-000-hello"}
                      {:meta "OpenResty / runtime"
                       :title "NGX live evaluation"
                       :text "A generated server for evaluating Lua through the runtime workflow."
                       :href "https://github.com/hoebat/play.ngx-001-eval"}
                      {:meta "Terminal UI"
                       :title "Counter"
                       :text "A generated Blessed-style terminal counter application."
                       :href "https://github.com/hoebat/play.tui-000-counter"}
                      {:meta "Terminal UI"
                       :title "Fetch"
                       :text "A generated terminal application demonstrating data fetching."
                       :href "https://github.com/hoebat/play.tui-001-fetch"}
                      {:meta "Terminal UI"
                       :title "Game of Life"
                       :text "A generated terminal Game of Life project."
                       :href "https://github.com/zcaudate/play.tui-002-game-of-life"}]}]]

[[:section {:title "Source and build links"}]]

"Generated repositories are outputs. The authored source and project-generation definitions live in `src-build/play`."

[[:card-grid {:items [{:meta "C pthreads"
                       :title "Source"
                       :text "The Hara-authored program."
                       :href "https://github.com/zcaudate-xyz/foundation-base/blob/main/src-build/play/c_000_pthreads_hello/main.clj"}
                      {:meta "C pthreads"
                       :title "Build"
                       :text "The std.make project definition."
                       :href "https://github.com/zcaudate-xyz/foundation-base/blob/main/src-build/play/c_000_pthreads_hello/build.clj"}
                      {:meta "NGX hello"
                       :title "Source"
                       :text "The authored Lua/OpenResty program."
                       :href "https://github.com/zcaudate-xyz/foundation-base/blob/main/src-build/play/ngx_000_hello/main.clj"}
                      {:meta "NGX hello"
                       :title "Build"
                       :text "The generated project definition."
                       :href "https://github.com/zcaudate-xyz/foundation-base/blob/main/src-build/play/ngx_000_hello/build.clj"}
                      {:meta "NGX eval"
                       :title "Source"
                       :text "The authored live-evaluation server."
                       :href "https://github.com/zcaudate-xyz/foundation-base/blob/main/src-build/play/ngx_001_eval/main.clj"}
                      {:meta "NGX eval"
                       :title "Build"
                       :text "The generated project definition."
                       :href "https://github.com/zcaudate-xyz/foundation-base/blob/main/src-build/play/ngx_001_eval/build.clj"}]}]]

[[:chapter {:title "Run from this repository"}]]

"The project aliases and Make targets generate or push several examples. Review prerequisites before running runtime-dependent examples."

[[:code {:lang "bash"}
  "lein push-c-000-pthreads\nlein push-ngx-000-hello\nlein push-ngx-001-eval\nlein push-tui-000-counter\nlein push-tui-001-fetch\nlein push-tui-002-game-of-life"]]

[[:callout {:tone :info
             :title "Keep links with examples"
             :content "When adding an example, include links to the authored source, build definition, tests where available, generated output repository, and the command used to reproduce it."}]]
