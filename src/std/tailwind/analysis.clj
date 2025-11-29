(ns std.tailwind.analysis
  (:require [std.lib :as h]
            [std.string :as str]
            [std.tailwind :as tailwind]
            [std.lang :as l]
            [std.lang.base.book :as book]
            [std.lang.base.library :as lib]))

(defn find-returns
  "Finds all return expressions in a form"
  [form]
  (let [returns (atom [])]
    (h/postwalk (fn [x]
                  (when (and (seq? x) (= (first x) 'return))
                    (swap! returns conj (second x)))
                  x)
                form)
    @returns))

(defn normalize-props [props]
  (let [cls (or (:className props) (:class props))]
    (if cls
      (dissoc (assoc props :class cls) :className)
      props)))

(defn get-book [lang]
  (try (lib/get-book (l/default-library) lang)
       (catch Exception _ nil)))

(defn resolve-symbol [book module-id sym]
  (if-let [module (and book module-id (book/get-module book module-id))]
    (let [ns-part (if (namespace sym) (symbol (namespace sym)))
          name-part (symbol (name sym))]
      (if ns-part
        (if-let [resolved-mod (get-in module [:link ns-part])]
          (symbol (str resolved-mod) (str name-part))
          sym)
        ;; No namespace? Check if it's a local definition or standard import?
        ;; For now, assume local to module if simple symbol, unless it matches a known alias pattern?
        (symbol (str module-id) (str sym))))
    sym))

(defn fetch-component-form [book sym]
  (try
    (:form (book/get-code-entry book sym))
    (catch Exception _ nil)))

(defn to-hiccup
  "Converts a DSL form to a Hiccup-like structure for layout analysis"
  ([form] (to-hiccup form {}))
  ([form ctx]
   (cond
     (vector? form)
     (vec (map #(to-hiccup % ctx) form))

     (seq? form)
     (let [[tag & args] form]
       (if (symbol? tag)
         (let [[props children] (if (map? (first args))
                                  [(first args) (rest args)]
                                  [{} args])
               props (normalize-props props)

               ;; Dependency resolution
               expanded-child (when (and (:book ctx) (:module ctx) (< (:depth ctx 0) (:max-depth ctx 3)))
                                (let [resolved (resolve-symbol (:book ctx) (:module ctx) tag)]
                                  (when-let [def-form (fetch-component-form (:book ctx) resolved)]
                                    ;; Analyze definition
                                    (let [returns (find-returns def-form)
                                          ret (first returns)]
                                      (when ret
                                        (to-hiccup ret (update ctx :depth (fnil inc 0))))))))]

           (into [(keyword (str tag)) props]
                 (concat (map #(to-hiccup % ctx) children)
                         (if expanded-child [expanded-child] []))))
         form))

     :else form)))

(defn estimate-layout
  "Takes a DSL form (e.g. defn.js), extracts returns, and renders layout.
   Options: :module (symbol) - context module ID for resolution."
  ([form] (estimate-layout form {}))
  ([form opts]
   (let [book-val (or (:book opts) (get-book :js))
         ctx (merge {:book book-val} opts)
         returns (find-returns form)]
     (map (fn [ret]
            (let [hiccup (to-hiccup ret ctx)]
              (tailwind/render hiccup)))
          returns))))
