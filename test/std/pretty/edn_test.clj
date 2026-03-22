(ns std.pretty.edn-test
  (:require [std.pretty :as printer]
            [std.pretty.edn :refer :all])
  (:use code.test))

^{:refer std.pretty.edn/override? :added "3.0"}
(fact "implements `std.pretty.protocol/IOverride`")

^{:refer std.pretty.edn/edn :added "3.0"}
(fact "converts an object to a tagged literal"

  (edn 1)
  => 1

  (edn nil)
  => nil

  (edn (java.lang.ClassLoader/getPlatformClassLoader))
  => clojure.lang.TaggedLiteral)

^{:refer std.pretty.edn/class->edn :added "3.0"}
(fact "converts a type to edn"

  (class->edn (type (byte-array [])))
  => "[B"

  (class->edn (type (into-array String [])))
  => "[Ljava.lang.String;"

  (class->edn (type :keyword))
  => 'clojure.lang.Keyword)

^{:refer std.pretty.edn/tagged-object :added "3.0"}
(fact "converts a type to a tagged literal"

  (tagged-object (java.lang.ClassLoader/getPlatformClassLoader) :classloader)
  ;;=> #object [jdk.internal.loader.ClassLoaders$PlatformClassLoader "0x73698a00" :classloader]
  => clojure.lang.TaggedLiteral)

^{:refer std.pretty.edn/format-date :added "3.0"}
(fact "helper function for formatting date")

^{:refer std.pretty.edn/visit-seq :added "3.0"}
(fact "creates a form for a seq"

  (visit-seq (printer/canonical-printer)
             [1 2 3 4])
  => [:group "(" [:align ["1" " " "2" " " "3" " " "4"]] ")"])

^{:refer std.pretty.edn/visit-tagged :added "3.0"}
(fact "creates a form for a tagged literal"

  (visit-tagged (printer/canonical-printer)
                (tagged-literal 'hello [1 2 3]))
  => [:span "#hello" " " [:group "[" [:align ["1" " " "2" " " "3"]] "]"]])

^{:refer std.pretty.edn/visit-unknown :added "3.0"}
(fact "creatse a form for an unknown element"

  (visit-unknown (printer/canonical-printer)
                 (Thread/currentThread))
  => (throws))

^{:refer std.pretty.edn/visit-meta :added "3.0"}
(fact "creates a form for a meta"
  (visit-meta (printer/canonical-printer)
              {:a 1} {})
  => [:group "{" [:align ()] "}"])

^{:refer std.pretty.edn/visit-edn :added "3.0"}
(fact "creates a form for a non-edn element"

  (visit-edn (printer/canonical-printer)
             (doto (java.util.ArrayList.)
               (.add 1)
               (.add 2)
               (.add 3)))
  => [:group "[" [:align ["1" " " "2" " " "3"]] "]"])

^{:refer std.pretty.edn/visit :added "3.0"}
(fact "a extensible walker for printing `edn`` data"

  (visit (printer/canonical-printer)
         (Thread/currentThread))
  => (contains-in [:span "#object" " " [:group "[" [:align coll?] "]"]]))
