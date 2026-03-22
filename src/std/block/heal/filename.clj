(ns std.block.heal.filename
  (:require [std.fs :as fs]
            [std.string.case]
            [std.string.prose :as prose]))

(defn heal-snake-case-filenames
  [root]
  (doall (for [[orig dir new] (map (juxt identity fs/parent (comp std.string.case/snake-case str fs/file-name))
                                   (keys (fs/list root
                                                  {:recursive true
                                                   :include [".clj$"]})))]
           (fs/move orig (fs/path dir new)))))
