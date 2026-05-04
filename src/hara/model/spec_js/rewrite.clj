(ns hara.model.spec-js.rewrite)

(defn js-rewrite-stage
  "Provides a consistent JavaScript staging rewrite hook.

   JavaScript does not currently need additional lowering here, but keeping
   the hook in the grammar makes the staging pipeline align with languages
   such as Lua and Ruby."
  [form _opts]
  form)
