(ns code.layout.primitives
  (:require [std.lib.zip :as zip]
            [code.edit :as edit]
            [std.lib :as h]))

(defn let-check-multiline
  [args]
  (let [binding-vector (first args)]
    (boolean (> (count binding-vector) 0))))

(def +special-forms+
  {'let     {:args    [{:type :vector
                        :pair-multiline true}]
             :multiline {:check  let-check-multiline
                         :layout nil}
             :layout  [[:block 1]]} ; Binding vector and body indented 1 space from 'l'
   
   'defn    {:args    [{:type :symbol}] ; Docstring, args, body align with opening paren
             :layout  [[:inner 0]]
             :multiline (fn [args] true)} 

   'cond    {:layout  [[:block 0]]      ; Clauses align with 'c'
             :multiline (fn [args]
                          (not-empty args))}
   
   'do      {:layout    [[:block 0]]
             :multiline (fn [args]
                          (> (count args) 1))}
   'if      {:layout     [[:block 1]]
             :multiline  (fn [args]
                           (some coll? (rest args)))}
   'assoc   {:multiline  (fn [args]
                           (> (count args) 3))}})

(defn get-special-check
  [form-name]
  (or (get-in +special-forms+ [form-name :multiline :check])
      h/F))

(declare get-max-width)

(defn get-max-width-children
  "gets the max with of the children"
  {:added "4.0"}
  [children]
  (let [child-widths (map get-max-width children)]
    (+ (apply + child-widths)
       (dec (count child-widths)))))

(defn get-max-width
  "gets the max width of whole form"
  {:added "4.0"}
  [form & [{:keys [ruleset]
            :as opts}]]
  (cond (coll? form)
        (cond (empty? form)
              (if (set? form) 3 2)
              
              :else
              (let [top-width    (if (set? form) 3 2)]
                (+ top-width    
                   (get-max-width-children form))))
        
        :else (count (pr-str form))))

(defn estimate-multiline-special
  "estimates if special forms are multilined"
  {:added "4.0"}
  [form & [{:keys [readable-len] :as opts}]]
  (let [[form-name & args] form
        check-fn (get-special-check form-name)]
    (or (check-fn args)
        (> (get-max-width form) readable-len))))

(defn estimate-multiline
  "creates multiline function"
  {:added "4.0"}
  [form & [{:keys [readable-len]
            :or {readable-len 30}
            :as opts}]]
  (cond (coll? form)
        (cond (empty? form)
              false

              (+special-forms+ (first form))
              (estimate-multiline-special form opts)

              :else
              (> (get-max-width form opts)
                 readable-len))
        
        :else false))
