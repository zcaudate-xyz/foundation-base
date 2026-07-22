(ns hara.lang.rewrite.common)

(defn with-form-meta
  [source out]
  (if (instance? clojure.lang.IObj out)
    (with-meta out (meta source))
    out))

(defn stable-symbol
  "returns a deterministic generated symbol for a source form"
  [prefix form]
  (let [source [(select-keys (meta form) [:file :line :column]) form]
        suffix (bit-and (hash source) 0xffffffff)]
    (symbol (str prefix suffix))))
