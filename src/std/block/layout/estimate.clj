(ns std.block.layout.estimate
  (:require [std.lib.foundation :as h]
            [std.lib.collection :as c]))

(def ^:dynamic *readable-len* 20)

(defn get-max-width
  "gets the max width of whole form"
  {:added "4.0"}
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
  "TODO"
  {:added "4.0"}
  [form {:keys [readable-len]
         :or {readable-len *readable-len*}}]
  (> (get-max-width form) readable-len))

(defn estimate-multiline-data
  "TODO"
  {:added "4.0"}
  [form opts]
  (estimate-multiline-basic form opts))

(defn estimate-multiline-vector
  "TODO"
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

        (c/form? form)
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
