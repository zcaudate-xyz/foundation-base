(ns code.heal.filename
  (:require [std.lib :as h]
            [std.fs :as fs]
            [std.string :as str]
            [std.string.prose :as prose]))

(defn heal-snake-case-filenames
  [root]
  (doall (for [[orig dir new] (map (juxt identity fs/parent (comp str/snake-case str fs/file-name))
                                   (keys (fs/list root
                                                  {:recursive true
                                                   :include [".clj$"]})))]
           (fs/move orig (fs/path dir new)))))
