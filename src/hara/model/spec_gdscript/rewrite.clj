(ns hara.model.spec-gdscript.rewrite)

(defn gdscript-rewrite-stage
  "GDScript-specific staging rewrites.

   Currently a no-op placeholder; add GDScript-specific form
   canonicalisation here as the backend matures."
  {:added "4.1"}
  [form {:keys [stage grammar mopts]}]
  form)
