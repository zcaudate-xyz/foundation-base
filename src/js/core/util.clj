(ns js.core.util
  (:require [std.lang :as l]))

(l/script :js)

(defn.js pass-callback
  "node style callback"
  {:added "4.0"}
  [err res]
  (when err (throw err))
  (return res))

(defn.js wrap-callback
  "wraps promise with node style callback"
  {:added "4.0"}
  [p callback]
  (return (. p (then 
                (fn:> [res] (callback nil res))
                (fn:> [err] (callback err nil))))))
