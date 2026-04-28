(ns std.lang.rewrite.common)

(defn with-form-meta
  [source out]
  (if (instance? clojure.lang.IObj out)
    (with-meta out (meta source))
    out))
