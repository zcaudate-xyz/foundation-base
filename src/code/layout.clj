(ns code.layout
  (:require [std.lib.zip :as zip]
            [code.edit :as edit])
  (:refer-clojure :exclude [next replace type]))

(def *max-row-length* 80)

(def +special-forms+
  {'let     {:args    [{:type :vector
                        :pair-multiline true}]
             :layout  [[:block 1]]} ; Binding vector and body indented 1 space from 'l'
   'defn    {:args    [{:type :symbol}]
             :layout  [[:inner 0]]} ; Docstring, args, body align with opening paren
   'cond    {:layout  [[:block 0]]} ; Clauses align with 'c'
   'do      {:layout  [[:block 0]]} ; Body aligns with 'd'
   'if      {:layout  [[:block 1]]} ; Test, then, else indented 1 space from 'i'
   ;; Add more special forms as needed
   })

(defn get-max-width-children
  [children]
  (let [child-widths (map get-max-width children)]
    (+ (apply + child-widths)
       (dec (count child-widths)))))

(defn get-max-width
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
  [form & [{:keys [readable-len] :as opts}]]
  (let [[form-name & args] form]
    (case form-name
      let   (let [binding-vector (first args)]
              (boolean (or (> (count binding-vector) 0) ; If many bindings
                           (> (get-max-width form) readable-len))))
      do    (if (> (count args) 1)
              true
              (> (get-max-width form) readable-len))
      if    (if (some coll? (rest args))
              true
              (> (get-max-width form) readable-len))
      cond  (not-empty args)
      (> (get-max-width form) readable-len))))

(defn estimate-multiline
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

(defn layout-form-insert
  [{:keys [stack code flags options]
    :as state} loc]
  (let [{:keys []} options
        {:keys [indent]
         :as parent} (last stack)
        elem       (zip/get loc)
        max-width  (get-max-width elem)
        multiline  (estimate-multiline elem)
        special?   (and (list? elem)
                        (+special-forms+ (first elem)))
        insert-fn  (if (:initial flags)
                     zip/insert-token-to-left
                     edit/insert-token-to-left)]
    (cond (not (coll? elem))
          [(assoc state
                  :code   (insert-fn  code elem)
                  :flags  (assoc flags :initial false))
           
           
           (zip/step-next loc)]

          :else
          [(assoc state
                  :code   (insert-fn code 'TODO)
                  :flags  (assoc flags :initial false))
           (zip/step-next loc)])))

(defn layout-form-initial
  [opts]
  {:code    (edit/parse-root "")
   :stack   [{:indent (or (:indent opts) 0)}]
   :flags   {:initial true
             :skip false}
   :options opts})

(defn layout-form
  [form & [{:keys [indent ruleset]
            :as opts}]]
  (loop [state    (layout-form-initial opts)
         loc      (zip/form-zip form)]
    (let [[new-state new-loc] (layout-form-insert state loc)]
      (if (nil? new-loc)
        (recur new-state new-loc)
        (:code new-state)))))




(comment
  (layout-form-insert
   (layout-form-initial {})
   (zip/form-zip '(let [a 1
                        b 2]
                    (+ a b)))))
(comment
  (defmulti layout-form-insert-raw
    (fn [type & args] type))

  (defmethod layout-form-insert-raw
    :token
    [type code value opts]
    )

  (defmethod layout-form-insert-raw
    :token
    [type code value opts]
    ))

(layout-form '(let [a 1
                    b 2]
                (+ a b))
             {})

(comment
  (zip/get 
   (-> (zip/form-zip '(let [a 1
                            b 2]
                        (+ a b)))
       (zip/step-next)
       ))

  (zip/get 
   (-> (zip/form-zip '(let [a 1
                            b 2]
                        (+ a b)))
       
       ))
  
  (defn ^{:hello true}
    hello [])

  (meta #'hello)

  (defn
    hello
    [])


  (comment
  (get-max-width
   '(let [a 1
          b 2]
      (+ a b)(+ a b)(+ a b)(+ a b)(+ a b)(+ a b)(+ a b)(+ a b)(+ a b)(+ a b)(+ a b)(+ a b)(+ a b)(+ a b)(+ a b)))
  (count
   (pr-str
    '(let [a 1
           b 2]
       (+ a b)(+ a b)(+ a b)(+ a b)(+ a b)(+ a b)(+ a b)(+ a b)(+ a b)(+ a b)(+ a b)(+ a b)(+ a b)(+ a b)(+ a b)
       )))))

(comment
  (map zip/status
       (take-while identity
                   (iterate
                    zip/step-next
                    (zip/form-zip '(let [a 1
                                         b 2]
                                     (+ a b)))))))






























(comment

  (block/parse-string
   "\"
\" ")

  (= (block/info (block/block
                  "

"))

     (block/info
      (block/block
       "\n\n")))

  (block/parse-string
   "\"\\n\""))
