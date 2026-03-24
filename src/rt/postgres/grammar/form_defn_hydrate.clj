(ns rt.postgres.grammar.form-defnhydrate)

(defn pg-defnhydrate-hook
  "Leaves postgres function entries unchanged.
   Infer reports are computed lazily through explicit typed APIs."
  {:added "4.1"}
  [entry]
  entry)
