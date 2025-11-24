(ns std.lib.version-test
  (:use [code.test :exclude [run]])
  (:require [std.lib.version :as version :refer :all])
  (:refer-clojure :exclude [clojure-version]))

^{:refer std.lib.version/parse-number :added "3.0"}
(fact "parse a number from string input"

  (parse-number "1") => 1)

^{:refer std.lib.version/parse-qualifier :added "3.0"}
(fact "parses a qualifier from string input"

  (parse-qualifier "" "") => 6
  
  (parse-qualifier "alpha" "") => 0 ^:hidden

  (parse-qualifier "beta" "") => 1

  (parse-qualifier "" "build") => -1)

^{:refer std.lib.version/parse :added "3.0"}
(fact "parses a version input"
  (parse "1.0.0-final")
  => {:major 1, :minor 0, :incremental 0, :qualifier 6, :release "final", :build ""}

  (parse "1.0.0-alpha+build.123")
  => {:major 1,
      :minor 0,
      :incremental 0,
      :qualifier 0,
      :release "alpha",
      :build "build.123"} ^:hidden

  (parse "9.1-901.jdbc4")
  => {:major 9,
      :minor 1,
      :incremental nil,
      :qualifier -1,
      :release "901.jdbc4",
      :build ""})

^{:refer std.lib.version/version :added "3.0"}
(fact "like parse but also accepts maps"

  (version "1.0-RC5")
  => {:major 1, :minor 0, :incremental nil, :qualifier 3, :release "rc5", :build ""})

^{:refer std.lib.version/equal? :added "3.0"}
(fact "compares if two versions are the same"

  (equal? "1.2-final" "1.2")
  => true)

^{:refer std.lib.version/newer? :added "3.0"}
(fact "returns true if the the first argument is newer than the second"

  (newer? "1.2" "1.0")
  => true

  (newer? "1.2.2" "1.0.4")
  => true)

^{:refer std.lib.version/older? :added "3.0"}
(fact "returns true if first argument is older than the second"

  (older? "1.0-alpha" "1.0-beta")
  => true

  (older? "1.0-rc1" "1.0")
  => true)

^{:refer std.lib.version/not-equal? :added "3.0"}
(fact "returns true if first argument is not older than the second"

  (not-equal? "3.0" "3.0")
  => false)

^{:refer std.lib.version/not-newer? :added "3.0"}
(fact "returns true if first argument is not older than the second"

  (not-newer? "3.0" "3.0")
  => true)

^{:refer std.lib.version/not-older? :added "3.0"}
(fact "returns true if first argument is not older than the second"

  (not-older? "3.0" "3.0")
  => true)

^{:refer std.lib.version/clojure-version :added "3.0"}
(fact "returns the current clojure version"
  (clojure-version)
  => (contains
      {:major anything,
       :minor anything,
       :incremental anything
       :qualifier anything}))

^{:refer std.lib.version/java-version :added "3.0"}
(fact "returns the current java version"
  (java-version)
  => (contains
      {:major anything,
       :minor anything,
       :incremental anything
       :qualifier anything}))

^{:refer std.lib.version/system-version :added "3.0"}
(fact "alternate way of getting clojure and java version"
  (system-version :clojure)
  => (clojure-version)

  (system-version :java)
  => (java-version))

^{:refer std.lib.version/satisfied :added "3.0"}
(fact "checks to see if the current version satisfies the given constraints"
  (satisfied [:java    :newer {:major 1 :minor 7}]
             {:major 1  :minor 8})
  => true

  (satisfied [:java  :older {:major 1 :minor 7}]
             {:major 1  :minor 7})
  => false

  (satisfied [:java  :not-newer  {:major 30 :minor 0}])
  => true)

^{:refer std.lib.version/init :added "3.0"}
(fact "only attempts to load the files when the minimum versions have been met"

  (version/init [[:java    :newer {:major 1 :minor 8}]
                 [:clojure :newer {:major 1 :minor 6}]]
                (:import java.time.Instant))
  (resolve 'init) => var?)

^{:refer std.lib.version/run :added "3.0"}
(fact "only runs the following code is the minimum versions have been met"

  (version/run [[:java    :newer {:major 1 :minor 8}]
                [:clojure :newer {:major 1 :minor 6}]]
               (eval '(Instant/ofEpochMilli 0)))
  (resolve 'run) => var?)
