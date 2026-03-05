(ns bb.string
  (:require [bb.string.coerce]
            [bb.string.common]
            [bb.string.case]
            [bb.string.path]
            [bb.string.prose]
            [bb.string.wrap :as wrap]
            [bb.lib.foundation :as h])
  (:refer-clojure :exclude [reverse replace]))

(h/intern-all bb.string.common
              bb.string.case
              bb.string.path
              bb.string.coerce
              bb.string.prose)

(h/intern-in wrap/wrap)

(defn |
  "shortcut for join lines

   (| \"abc\" \"def\")
   => \"abc\\ndef\""
  {:added "3.0"}
  [& args]
  (join "\n" args))

(defn lines
  "transforms string to seperated newlines

   (lines \"abc\\ndef\")
   => '(bb.string/| \"abc\" \"def\")"
  {:added "3.0"}
  [s]
  (cons `| (split-lines s)))
