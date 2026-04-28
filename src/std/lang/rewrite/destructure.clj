(ns std.lang.rewrite.destructure)

(defn destructure-target?
  [form]
  (and (set? form)
       (seq form)
       (every? symbol? form)))

(defn destructure-symbols
  ([target]
   (destructure-symbols target name))
  ([target sym->str]
   (sort-by sym->str target)))

(defn destructure-value
  ([temp sym]
   (destructure-value temp sym name))
  ([temp sym sym->str]
   (list 'x:get-key temp (sym->str sym) nil)))

(defn destructure-bindings
  ([target temp]
   (destructure-bindings target temp name))
  ([target temp sym->str]
   (map (fn [sym]
          [sym (destructure-value temp sym sym->str)])
        (destructure-symbols target sym->str))))
