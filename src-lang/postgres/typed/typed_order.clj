(ns postgres.typed.typed-order)

(defn ordered-map
  "Builds a recursively serializable insertion-ordered map from entries."
  [entries]
  (let [output (java.util.LinkedHashMap.)]
    (doseq [[key value] entries]
      (.put output key value))
    output))

(defn ordered-value
  "Recursively preserves ordered maps and deterministically orders plain maps."
  [value]
  (cond
    (instance? java.util.LinkedHashMap value)
    (ordered-map
     (map (fn [[key nested-value]]
            [key (ordered-value nested-value)])
          value))

    (map? value)
    (ordered-map
     (map (fn [[key nested-value]]
            [key (ordered-value nested-value)])
          (sort-by (comp str key) value)))

    (sequential? value)
    (mapv ordered-value value)

    :else
    value))
