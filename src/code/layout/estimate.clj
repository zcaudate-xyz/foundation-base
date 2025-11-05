(ns code.layout.estimate
  (:require [std.lib.zip :as zip]
            [code.edit :as edit]
            [code.layout.primitives :as p]
            [std.lib :as h]))

(defn get-max-width
  [form]
  (count (pr-str form)))

(defn get-max-width-children
  "gets the max with of the children"
  {:added "4.0"}
  [children]
  (let [child-widths (map get-max-width children)]
    (+ (apply + child-widths)
       (dec (count child-widths)))))

;;
;;
;;

(defn estimate-multiline-basic
  [form {:keys [readable-len]
         :or {readable-len 30}}]
  (> (get-max-width form) readable-len))

(defn estimate-multiline-data
  [form opts]
  (estimate-multiline-basic form opts))

(defn estimate-multiline-vector
  "estimates if special forms are multilined"
  {:added "4.0"}
  [form opts]
  (estimate-multiline-basic form opts))

(defn estimate-multiline-list
  "estimates if special forms are multilined"
  {:added "4.0"}
  [form opts]
  (estimate-multiline-basic form opts))

(defn estimate-multiline
  "creates multiline function"
  {:added "4.0"}
  [form opts]
  (cond (and (coll? form)
             (empty? form))
        false

        (h/form? form)
        (estimate-multiline-list form opts)

        (vector? form)
        (estimate-multiline-vector form opts)
        
        
        (or (map? form)
            (set? form))
        (estimate-multiline-data form opts)        
        
        :else
        (estimate-multiline-basic form opts)))

(comment
  (count "(+special-lists+ (first form))"))
