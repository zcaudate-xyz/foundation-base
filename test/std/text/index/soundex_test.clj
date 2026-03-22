(ns std.text.index.soundex-test
  (:require [std.text.index.soundex :refer :all])
  (:use code.test))

^{:refer std.text.index.soundex/stem :added "3.0"}
(fact "classifies a word based on its stem"

  (stem "hello")
  => "H400"

  (stem "hellah")
  => "H400"

  (stem "supercalifragilistiic")
  => "S162")
