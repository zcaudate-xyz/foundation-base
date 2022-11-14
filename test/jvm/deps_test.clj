(ns jvm.deps-test
  (:use code.test)
  (:require [jvm.deps :refer :all]
            [std.fs :as fs])
  (:refer-clojure :exclude [load resolve]))

^{:refer jvm.deps/resolve-classloader :added "3.0"}
(fact "resolves a class or namespace to a physical location"

  (resolve-classloader String)
  => (contains
      [anything #"java/lang/String.class"])

  (resolve-classloader 'code.test)
  => (contains
      [nil (str (fs/path "src/code/test.clj"))]))

^{:refer jvm.deps/resolve-jar-entry :added "3.0"}
(fact "resolves a class or namespace within a jar"

  (resolve-jar-entry 'clojure.core
                     ['org.clojure/clojure
                      (current-version 'org.clojure/clojure)])
  => (contains-in [string?
                   "clojure/core.clj"]))

^{:refer jvm.deps/resolve :added "3.0"}
(fact "resolves a class or namespace within a context"

  (resolve 'clojure.core
           ['org.clojure/clojure
            (current-version 'org.clojure/clojure)])
  => (contains [string?
                "clojure/core.clj"]))

^{:refer jvm.deps/loaded-artifact? :added "3.0"}
(comment "checks if artifact has been loaded"

  (loaded-artifact? '[org.clojure/clojure "1.9.0"])
  => true)

^{:refer jvm.deps/load-artifact :added "3.0"}
(comment "loads an artifact into the system"

  (load-artifact '[org.clojure/clojure "1.9.0"]))

^{:refer jvm.deps/unload-artifact :added "3.0"}
(comment "unloads an artifact from the system"

  (unload-artifact '[org.clojure/clojure "1.9.0"]))

^{:refer jvm.deps/all-loaded-artifacts :added "3.0"}
(fact "returns all loaded artifacts"

  (all-loaded-artifacts) ^:hidden
  => sequential?)

^{:refer jvm.deps/all-loaded :added "3.0"}
(fact "returns all the loaded artifacts of the same group and name"

  (all-loaded 'org.clojure/clojure)
  ;;=> ('org.clojure:clojure:jar:<version>)
  => sequential?)

^{:refer jvm.deps/unload :added "3.0"}
(fact "unloads all artifacts in list")

^{:refer jvm.deps/load :added "3.0"}
(fact "loads all artifacts in list, unloading previous versions of the same artifact")

^{:refer jvm.deps/clean :added "3.0"}
(fact "cleans the maven entries for the artifact, `:full` deletes all the versions"

  (clean '[org.clojure/clojure "2.4.8"]
         {:full true
          :simulate true})
  => set?)

^{:refer jvm.deps/version-map :added "3.0"}
(fact "returns all the loaded artifacts and their versions"
  (version-map)
  => map?)

^{:refer jvm.deps/current-version :added "3.0"}
(fact "finds the current artifact version for a given classloader"

  (current-version 'org.clojure/clojure)
  => string?)
