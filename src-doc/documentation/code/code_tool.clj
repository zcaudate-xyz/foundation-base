(ns documentation.code-tool
  (:require [code.tool.maven :as maven]
            [code.tool.measure :as measure])
  (:use code.test))

[[:hero {:title "code.tool"
         :subtitle "Build, Java, Maven, and measurement tooling."
         :lead "`code.tool` groups lower-level development utilities used to build artifacts, inspect Java sources, package Maven outputs, and measure code changes."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"These namespaces are not the primary user API, but they are important for repository operations and release work. They turn project metadata and package manifests into concrete build or measurement tasks."

[[:chapter {:title "Internal usage" :link "internal"}]]

"Build tooling uses `code.framework.link` and `code.project` to collect package entries and dependencies. Maven tooling converts those manifests into installable or deployable artifacts."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Maven lifecycle"}]]

"`code.tool.maven` wraps packaging, linkage, install, and clean tasks. Most tasks accept a package or site key and a `:tag` selector."

^{:refer code.tool.maven/linkage :added "3.0"}
(fact "create deployment linkages"
  (maven/linkage :all {:tag :all
                       :print {:item false :result false :summary false}})
  => map?)

^{:refer code.tool.maven/clean :added "3.0"}
(fact "clean interim package files"
  (maven/clean :all {:tag :all
                     :print {:item false :result false :summary false}})
  => map?)

^{:refer code.tool.maven/package :added "3.0"}
(fact "package interim files"
  (maven/package '[xyz.zcaudate]
                 {:tag :all
                  :print {:item false :result false :summary false}})
  => map?)

[[:section {:title "Measuring code"}]]

"`code.tool.measure` detects file types and produces complexity and surface metrics from source strings."

^{:refer code.tool.measure/detect-type :added "4.0"}
(fact "detect the language of a filename"
  (measure/detect-type "src/example.cljs")
  => :cljs

  (measure/detect-type "src/example.clj")
  => :clj)

^{:refer code.tool.measure/generate-metrics :added "4.0"}
(fact "generate metrics for a JavaScript snippet"
  (measure/generate-metrics "function add(a, b) { return a + b; }")
  => (contains {:complexity number?
                :surface number?}))

[[:section {:title "End-to-end: detect and measure a snippet"}]]

"Combine `detect-type` with `generate-metrics` to classify a file and score its contents in one pass."

^{:refer code.tool.measure/generate-metrics :added "4.0"}
(fact "classify and measure a JavaScript snippet"
  (let [code "function hello(x) { if (x) return 1; return 2; }"]
    {:type (measure/detect-type "hello.js")
     :metrics (measure/generate-metrics code)})
  => {:type :js
      :metrics (contains {:complexity number?
                          :surface number?})})

[[:chapter {:title "API" :link "api"}]]

