(ns documentation.code-tool
  (:use code.test))

[[:hero {:title "code.tool"
         :subtitle "Build, Java, Maven, and measurement tooling."
         :lead "`code.tool` groups lower-level development utilities used to build artifacts, inspect Java sources, package Maven outputs, and measure code changes."}]]

[[:chapter {:title "Motivation" :link "motivation"}]]

"These namespaces are not the primary user API, but they are important for repository operations and release work. They turn project metadata and package manifests into concrete build or measurement tasks."

[[:chapter {:title "Internal usage" :link "internal"}]]

"Build tooling uses `code.framework.link` and `code.project` to collect package entries and dependencies. Maven tooling converts those manifests into installable or deployable artifacts."

[[:chapter {:title "API" :link "api"}]]

