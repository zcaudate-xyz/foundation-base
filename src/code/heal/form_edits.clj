(ns code.heal.form-edits
  (:require [code.edit :as edit]
            [code.query :as query]
            [std.lib :as h]))

(defn fix:namespaced-symbol-no-dot
  [nav]
  (query/modify
   nav
   [(fn [form]
      (and (symbol? form)
           (= "-" (namespace form))
           (.contains (name form) ".")))]
   (fn [nav] 
     (let [form      (code.edit/value nav)
           sym-ns    (namespace form)
           sym-name  (name form)
           sym-parts (std.string/split sym-name #"\.")]
       (code.edit/replace
        nav
        (apply list '. (symbol sym-ns (first sym-parts))
               (map symbol (rest sym-parts))))))))
  
