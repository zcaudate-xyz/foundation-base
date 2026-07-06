(ns documentation.lib-docker
  (:use code.test))

[[:hero {:title "lib.docker"
         :subtitle "container lifecycle helpers for managed runtimes and tests"
         :lead "Start, inspect, and stop Docker containers, with optional automatic cleanup for temporary resources."
         :actions [{:label "All libraries" :href "index.html"}]}]]

[[:chapter {:title "Overview" :link "overview"}]]

"The public namespace exposes common container operations and composes them into runtime lifecycle hooks. Temporary containers can be associated with the cleanup service used by development and test workflows."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Inspect containers"}]]

(comment
  (require '[lib.docker :as docker])

  (docker/list-containers)
  (docker/has-container? "example-service")
  (docker/get-ip "example-service"))

[[:section {:title "Start and stop a container"}]]

(comment
  (def container
    (docker/start-container
     {:id "example-service"
      :image "redis:7"
      :ports {6379 6379}}))

  (docker/stop-container container))

[[:section {:title "Attach a runtime"}]]

"`start-runtime` adds container identity and networking to a runtime map. `stop-runtime` respects permanent and secondary container options."

(comment
  (def runtime
    (docker/start-runtime
     {:lang :redis :tag :cache}
     {:image "redis:7" :suffix "dev"}))

  (docker/stop-runtime runtime (:container runtime)))

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "lib.docker"}]]
[[:api {:namespace "lib.docker.common"}]]
[[:api {:namespace "lib.docker.compose"}]]
[[:api {:namespace "lib.docker.ryuk"}]]
