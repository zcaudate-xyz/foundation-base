(ns code.layout.primitives
  (:require [std.lib.zip :as zip]
            [code.edit :as edit]
            [std.lib :as h]))

(comment
  'defn    {:block 0
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
                           (> (count args) 3))})

(defn let-check-multiline
  [args]
  (let [binding-vector (first args)]
    (boolean (> (count binding-vector) 0))))

(def +layout-vector+
  {:default  {:block       -1
              :layout      {:default {:type :vector
                                      :columns 2
                                      :align true}}
              :checks      {:multiline let-check-multiline}}})

(def +layout-form+
  {:default {:block     -1}
   'let     {:block     1     ;; Binding vector and body indented 1 space from 'l'
             :args {:bindings {:at 1
                               :inline true
                               :layout {:type :vector
                                        :columns 2
                                        :align-col true}}
                    :default  {}}
             :fn   {:multiline let-check-multiline}}
   'assoc   {:block     -1     
             :args {:input {:at 1
                            :inline true}
                    :keys  {:at even?}
                    :vals  {:at odd?
                            :inline    true
                            :align-col true}
                    :default  {}}
             :fn    {}}})



(defn get-layout-spec
  [type op]
  (assoc (case type
           :form   (or (get +layout-form+ op)
                       (get +layout-form+ :default))
           :vector (or (get +layout-vector+ op)
                       (get +layout-vector+ :default))
           :set    {:block     -1}
           :map    {:block     -1})
         :type type
         :op op))

(get-layout-spec :form 'let)


(defn args-check-sort
  [params]
  (let [sorted (sort-by (fn [[k {:keys [at]}]]
                          (cond (integer? at)
                                1

                                (fn? at)
                                2

                                (nil? at)
                                10
                                
                                :else
                                3))
                        (seq params))]
    sorted))

(args-check-sort
 {:bindings
  {:at 1,
   :inline true,
   :layout {:type :vector, :columns 2, :align-col true}},
  :default {}})
([:bindings {:at 1, :inline true, :layout {:type :vector, :columns 2, :align-col true}}]
 [:default {}])

(create-arg-check
 {:bindings
  {:at 1,
   :inline true,
   :layout {:type :vector, :columns 2, :align-col true}},
  :default {}})

(defn create-stack-entry
  [type op & [opts]]
  (let [spec (get-layout-spec type op)]
    ))

{:type :form
 :op 'let
 :block 1,
 :args
 {:bindings
  {:at 1,
   :inline true,
   :layout {:type :vector, :columns 2, :align-col true}},
  :default {}},
 :fn {:multiline let-check-multiline}}


{:type :form
 :op 'let
 :args        {1 :bindings
               :default :body}
 :indentation {:body 2 ;; this is calculated through block 1
               :bindings 5}
 :layout      {:bindings {:type :vector
                          :columns 2
                          :align true}}
 :checks      {:multiline let-check-multiline}
 :current 1}





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
