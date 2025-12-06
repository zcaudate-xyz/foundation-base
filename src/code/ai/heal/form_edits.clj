(ns code.ai.heal.form-edits
  (:require [std.block.navigate :as edit]
            [code.query :as query]
            [std.lib :as h]
            [std.block :as b]
            [std.string :as str]))

(defn fix:namespaced-symbol-no-dot
  [nav]
  (query/modify
   nav
   [(fn [form]
      (and (symbol? form)
           (namespace form)
           (.contains (name form) ".")))]
   (fn [nav] 
     (let [form      (std.block.navigate/value nav)
           sym-ns    (namespace form)
           sym-name  (name form)
           sym-parts (std.string/split sym-name #"\.")]
       (std.block.navigate/replace
        nav
        (apply list '. (symbol sym-ns (first sym-parts))
               (map symbol (rest sym-parts))))))))

(defn fix:dash-indexing
  [nav]
  (query/modify
   nav
   [(fn [form]
      (and (h/form? form)
           (and (str/starts-with? (str (last form))
                                  "-")
                (not (str/starts-with? (str (last form))
                                       "-/")))))]
   (fn [nav] 
     (let [form      (std.block.navigate/value nav)]
       (std.block.navigate/replace
        nav
        (concat (butlast form)
                [(symbol (subs (str (last form))
                               1))]))))))

(defn fix:set-arg-destructuring
  [nav]
  (query/modify
   nav
    [(fn [form]
       (boolean
        (and (set? form)
             (:# form))))]
   (fn [nav] 
     (let [val    (std.block.navigate/value nav)]
       (std.block.navigate/replace
        nav
        (b/block {:# (first (filter vector? (disj val :#)))}))))))

(defn fix:remove-fg-extra-references
  [nav]
  (query/modify
   nav
   [(fn [form]
      (and (vector? form)
           (or (str/ends-with? (str (first form))
                               "components.figma.image-with-fallback")
               (= (first form)
                  'js.lib.sonner))))]
   edit/delete))

(defn fix:replace-fg-extra-namepspaces
  [nav]
  (query/modify
   nav
   [(fn [form]
      (and (symbol? form)
           (or (= (namespace form) "imf")
               (= (namespace form) "snr"))))]
   (fn [nav] 
     (let [form      (std.block.navigate/value nav)]
       (std.block.navigate/replace
        nav
        (symbol "fg" (name form)))))))

(defn fix:remove-mistranslated-syms
  [nav]
  (query/modify
   nav
   [(fn [form]
      (and (symbol? form)
           (= "</>" (str form))))]
   edit/delete))
