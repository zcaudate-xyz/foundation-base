(ns rt.postgres.grammar.form-defn-hydrate)

(defn pg-defn-hydrate-hook
  "Leaves postgres function entries unchanged.
   Infer reports are computed lazily through explicit typed APIs."
  {:added "4.1"}
  [entry]
  entry)
