(ns std.block.heal.filename-test
  (:use code.test)
  (:require [std.block.heal.filename :refer :all]
            [std.fs :as fs]
            [std.lib :as h]))

^{:refer std.block.heal.filename/heal-snake-case-filenames :added "4.0"}
(fact "renames files to snake case recursively"
  (let [root (fs/create-tmpdir "heal-test")]
    (try
      (spit (fs/path root "FooBar.clj") "")
      (fs/create-directory (fs/path root "nested"))
      (spit (fs/path root "nested/BazQux.clj") "")

      (heal-snake-case-filenames root)

      (fs/list root {:recursive true :include [".clj$"]})
      => (contains {(str (fs/path root "foo_bar.clj")) anything
                    (str (fs/path root "nested/baz_qux.clj")) anything})
      (finally
        (fs/delete root)))))
