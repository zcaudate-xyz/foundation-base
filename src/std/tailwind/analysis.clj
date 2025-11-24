(ns std.tailwind.analysis
  (:require [std.lib :as h]
            [std.string :as str]
            [std.tailwind :as tailwind]))

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

(defn to-hiccup
  "Converts a DSL form to a Hiccup-like structure for layout analysis"
  [form]
  (cond
    (vector? form)
    (vec (map to-hiccup form))

    (seq? form)
    (let [[tag & args] form]
      (if (symbol? tag)
        (let [[props children] (if (map? (first args))
                                 [(first args) (rest args)]
                                 [{} args])
              props (normalize-props props)]
          (into [(keyword (str tag)) props]
                (map to-hiccup children)))
        form))

    :else form))

(defn estimate-layout
  "Takes a DSL form (e.g. defn.js), extracts returns, and renders layout."
  [form]
  (let [returns (find-returns form)]
    (map (fn [ret]
           (let [hiccup (to-hiccup ret)]
             (tailwind/render hiccup)))
         returns)))
