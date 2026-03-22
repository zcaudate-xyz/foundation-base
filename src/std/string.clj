(ns std.string
  (:require [std.string.coerce]
            [std.string.common]
            [std.string.case]
            [std.string.path]
            [std.string.prose]
            [std.string.wrap :as wrap]
            [std.lib.foundation :as h])
  (:refer-clojure :exclude [reverse replace]))

(h/intern-all std.string.common
              std.string.case
              std.string.path
              std.string.coerce
              std.string.prose)

(h/intern-in wrap/wrap)
