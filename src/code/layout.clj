(ns code.layout
  (:require [std.lib.zip :as zip]
            [code.edit :as edit]
            [code.layout.primitives :as p]
            [std.lib :as h])
  (:refer-clojure :exclude [next replace type]))

(def *max-row-length* 80)

(def +special-forms+
  {'defn    {:block 0
             :args    [{:type :symbol}] ; Docstring, args, body align with opening paren
             :multiline (fn [args] true)} 
   
   'cond    {:block 0
             :multiline (fn [args]
                          (not-empty args))}
   
   'do      {:block 0
             :multiline (fn [args]
                          (> (count args) 1))}
   'if      {:block 1
             :multiline  (fn [args]
                           (some coll? (rest args)))}
   'assoc   {:block -1
             :multiline  (fn [args]
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



(declare layout-form-insert)


(defn layout-form-insert-special
  [{:keys [stack code options]
    :as state} loc]
  (let [elem       (zip/get loc)
        code       ]
    [state ]))

(defn layout-form-insert-call
  [{:keys [stack code flags options]
    :as state} loc]
  (let [elem       (zip/get loc)
        special?   (and (list? elem)
                        (+special-forms+ (first elem)))]
    (cond (not special?)
          (edit/insert-empty))))

(defn layout-form-classify
  [state])


(defn layout-form-insert-multiline
  [{:keys [stack code options]
    :as state} loc]
  (let [elem       (zip/get loc)
        special?   (and (list? elem)
                        (+special-forms+ (first elem)))]
    (h/prn :multiline code elem)
    (cond (not special?)
          (let [[state loc] (layout-form-insert
                             {:code (-> code
                                        (edit/insert ())
                                        (edit/down)
                                        (edit/insert (first elem)))
                              :flags {:in-multiline true}}
                             (zip/step-next))])
          
          :else
          (layout-form-insert-special state loc))))

(defn layout-form-get-indent
  [{:keys [indent]
    :as flags}]
  indent)

(defn layout-form-insert
  "inserts a single element"
  {:added "4.0"}
  [{:keys [stack code options]
    :as state} loc]
  (let [{:keys [indent
                in-multiline]
         :as flags}  (last stack)
        elem         (zip/get loc)
        max-width    (get-max-width elem)
        multiline?   (estimate-multiline elem)
        code         (if in-multiline
                       (-> code
                           (edit/insert-newline)
                           (edit/insert-space (layout-form-get-indent flags)))
                       code)]
    (h/prn code elem)
    (cond (not multiline?)
          [(assoc state :code   (edit/insert-token  code elem))
           (zip/step-next loc)]

          :else
          (layout-form-insert-multiline state loc))))

(defn layout-form-initial
  "generates the initial state"
  {:added "4.0"}
  [opts]
  {:code    (edit/parse-string "")
   :stack   [{:type :root :indent (or (:indent opts) 0)
              :position 0}]
   :options opts})

(defn layout-form
  "layout a form"
  {:added "4.0"}
  [form & [{:keys [indent ruleset]
            :as opts}]]
  (loop [state    (layout-form-initial opts)
         loc      (zip/form-zip form)]
    (let [[new-state new-loc] (layout-form-insert state loc)]
      (if (nil? new-loc)
        (recur new-state new-loc)
        (:code new-state)))))

(comment
  (layout-form '(let [a 1
                      b 2]
                  (+ a b))
               {}))

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
