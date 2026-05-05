(ns solidity.core
  (:require [solidity.core.builtin]
            [solidity.core.util]
            [hara.lang :as l]
            [std.lib.foundation :as f])
  (:refer-clojure :exclude [assert require bytes]))

(f/intern-all solidity.core.builtin
              solidity.core.util)

(comment
  (l/get-book (l/default-library)
              :solidity))
