(ns jvm.artifact-test
  (:require [jvm.artifact :refer :all]
            [jvm.artifact.common :as base])
  (:use code.test))

^{:refer jvm.artifact/rep->coord :added "3.0"}
(fact "encodes the rep to a coordinate"

  (-> {:group "tahto" :artifact "tahto" :version "2.4.0"}
      (map->Rep)
      (rep->coord))
  => '[tahto/tahto "2.4.0"])

^{:refer jvm.artifact/rep->path :added "3.0"}
(fact "encodes the rep to a path"

  (-> {:group "tahto" :artifact "tahto" :version "2.4.0"}
      (map->Rep)
      (rep->path))
  => #"/tahto/tahto/2.4.0/tahto-2.4.0.jar")

^{:refer jvm.artifact/rep->string :added "3.0"}
(fact "encodes the rep to a string"

  (-> {:group "tahto" :artifact "tahto" :version "2.4.0"}
      (map->Rep)
      (rep->string))
  => "tahto:tahto:2.4.0")

^{:refer jvm.artifact/rep? :added "3.0"}
(fact "checks if an object is of type `jvm.artifact.Rep`"

  (rep? (rep "tahto:tahto:2.4.0"))
  => true)

^{:refer jvm.artifact/coord->rep :added "3.0"}
(fact "converts a coord to a rep instance"

  (coord->rep '[tahto/tahto "2.4.0"])
  => (contains {:group "tahto"
                :artifact "tahto"
                :version "2.4.0"}))

^{:refer jvm.artifact/path->rep :added "3.0"}
(fact "converts a path to a rep instance"

  (path->rep (str base/*local-repo* "/tahto/tahto/2.4.0/tahto-2.4.0.jar"))
  => (contains {:group "tahto"
                :artifact "tahto"
                :version "2.4.0"}))

^{:refer jvm.artifact/string->rep :added "3.0"}
(fact "converts a string to a rep instance"

  (string->rep "tahto:tahto:2.4.0")
  => (contains {:group "tahto"
                :artifact "tahto"
                :version "2.4.0"}))

^{:refer jvm.artifact/coord? :added "3.0"}
(fact "checks for a valid coordinate"

  (coord? '[org.clojure/clojure "1.8.0"]) => true

  (coord? '[1 2 3]) => false)

^{:refer jvm.artifact/rep :added "3.0"}
(fact "converts various formats to a rep"

  (str (rep '[tahto/tahto "2.4.0"]))
  => "tahto:tahto:jar:2.4.0"

  (str (rep "tahto:tahto:2.4.0"))
  => "tahto:tahto:jar:2.4.0")

^{:refer jvm.artifact/rep-default :added "3.0"}
(fact "creates the default representation of a artifact"

  (into {} (rep-default "tahto:tahto:2.4.0"))
  => {:properties {},
      :group "tahto",
      :classifier nil,
      :file nil,
      :exclusions nil,
      :scope nil,
      :extension "jar",
      :artifact "tahto",
      :version "2.4.0"})

^{:refer jvm.artifact/artifact :added "3.0"}
(fact "converts various artifact formats"

  (artifact :string '[tahto/tahto "2.4.0"])
  => "tahto:tahto:jar:2.4.0"

  (artifact :path "tahto:tahto:2.4.0")
  => (str base/*local-repo*
          "/tahto/tahto/2.4.0/tahto-2.4.0.jar"))

^{:refer jvm.artifact/artifact-default :added "3.0"}
(fact "converts an artifact in any format to the default representation"

  (artifact-default '[tahto/tahto "2.4.0"])
  => rep?)

^{:refer jvm.artifact/artifact-string :added "3.0"}
(fact "converts an artifact in any format to the string representation"

  (artifact-string '[tahto/tahto "2.4.0"])
  => "tahto:tahto:jar:2.4.0")

^{:refer jvm.artifact/artifact-symbol :added "3.0"}
(fact "converts an artifact in any format to the symbol representation"

  (artifact-symbol '[tahto/tahto "2.4.0"])
  => 'tahto/tahto)

^{:refer jvm.artifact/artifact-path :added "3.0"}
(fact "converts an artifact in any format to the path representation"
 
  (artifact-path '[tahto/tahto "2.4.0"])
  => (str base/*local-repo*
          "/tahto/tahto/2.4.0/tahto-2.4.0.jar"))

^{:refer jvm.artifact/artifact-coord :added "3.0"}
(fact "converts an artifact in any format to the coord representation"

  (artifact-coord "tahto:tahto:jar:2.4.0")
  => '[tahto/tahto "2.4.0"])
