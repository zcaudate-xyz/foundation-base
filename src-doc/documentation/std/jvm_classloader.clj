(ns documentation.jvm-classloader
  (:use code.test))

[[:hero {:title "jvm.classloader"
         :subtitle "classpath inspection and dynamic class support"
         :lead "Inspect classloader delegation, manage classpath URLs, construct loaders, and read classes from supported sources."
         :actions [{:label "All libraries" :href "index.html"}]}]]

[[:chapter {:title "Overview" :link "overview"}]]

"The public namespace adapts system, URL, dynamic, and base classloaders to a shared protocol."

[[:chapter {:title "Walkthrough" :link "walkthrough"}]]

[[:section {:title "Inspect the current classpath"}]]

(comment
  (require '[jvm.classloader :as classloader])

  (classloader/classpath)
  (classloader/all-jars)
  (classloader/all-paths)
  (classloader/delegation classloader/+base+))

[[:section {:title "Create a loader"}]]

(comment
  (def loader
    (classloader/dynamic-classloader
     ["target/classes"]
     classloader/+base+))

  (classloader/add-url loader "target/generated")
  (classloader/has-url? loader "target/generated")
  (classloader/remove-url loader "target/generated"))

[[:section {:title "Read a class source"}]]

(comment
  (classloader/load-class
   "target/classes/example/Generated.class"
   {:name "example.Generated"}
   loader))

[[:chapter {:title "API" :link "api"}]]

[[:api {:namespace "jvm.classloader"}]]
[[:api {:namespace "jvm.classloader.common"}]]
[[:api {:namespace "jvm.classloader.base-classloader"}]]
[[:api {:namespace "jvm.classloader.url-classloader"}]]
[[:api {:namespace "jvm.classloader.system-classloader"}]]
