(ns std.block.heal.filename-test
  (:use code.test)
  (:require [std.block.heal.filename :refer :all]
            [std.fs :as fs]
            [std.lib :as h]))

^{:refer std.block.heal.filename/heal-snake-case-filenames :added "4.0"}
(fact "renames clj files to snake case"
  (let [root (fs/create-tmpdir "heal-test")]
    (try
      (spit (str (fs/path root "myFile.clj")) "content")
      (spit (str (fs/path root "AnotherFile.clj")) "content")

      (heal-snake-case-filenames root)

      (keys (fs/list root))
      => (contains [(str (fs/path root "my_file.clj"))
                    (str (fs/path root "another_file.clj"))]
                   :in-any-order)

      (finally
        (fs/delete root)))))
