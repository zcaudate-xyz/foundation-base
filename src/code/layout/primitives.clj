(ns code.layout.primitives
  (:require [std.lib.zip :as zip]
            [code.edit :as edit]
            [std.lib :as h]))

(defn fn-multiline-let
  "multiline check for let"
  {:added "4.0"}
  [args]
  (let [binding-vector (first args)]
    (boolean (> (count binding-vector) 0))))

(def +inline-types+
  #{:inline/auto
    :inline/square
    :inline/function})


{:spec/function }

{:spec/call- } 

(def +layout-vector+
  {:vector/default  {:block      -1
                     :layout     {:default {:type :vector
                                            :inline :inline/auto
                                            :align true}}}
   :%               {:block      -1
                     :layout      {:default {:type :vector
                                             :inline :inline/function}}}}) 

(def +layout-list+
  {:list/default {:block     -1}
   'let     {:block     1     ;; Binding vector and body indented 1 space from 'l'
             :args {:arg/bindings {:at 1
                                   :inline true
                                   :layout {:type :vector
                                            :columns 2
                                            :align-col true}}
                    :arg/default  {}}
             :fn   {:fn/multiline fn-multiline-let}}
   'assoc   {:block     -1     
             :args {:arg/input {:at 1
                                :inline true}
                    :arg/keys  {:at even?}
                    :arg/vals  {:at odd?
                                :inline    true
                                :align-col true}
                    :arg/default  {}}
             :fn    {}}})

(defn get-layout-spec
  "get the layout specification"
  {:added "4.0"}
  [type op]
  (let [op (cond (symbol? op) op
                 (vector? op)  :type/vector
                 (map? op)     :type/map
                 (set? op)     :type/set
                 (h/form? op)  :type/list
                 :else :type/token)]
    (assoc (case type
             :list   (or (get +layout-list+ op)
                         (get +layout-list+ :list/default))
             :vector (or (get +layout-vector+ op)
                         (get +layout-vector+ :vector/default))
             :set    {:block     -1}
             :map    {:block     -1})
           :type type
           :op op)))

(defn arg-label-sort
  "orders the order for label functions"
  {:added "4.0"}
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
    (vec sorted)))

(defn arg-label-idx
  "labels the type of arg"
  {:added "4.0"}
  [idx [label {:keys [at]}]]
  (cond (= idx at)
         label

        (and (fn? at)
             (at idx))
         label

        (and (coll? at)
             (get at idx))
         label))

(defn arg-label-fn
  "creates a labelling function"
  {:added "4.0"}
  [params]
  (let [sorted (arg-label-sort params)]
    (fn [idx arg]
      (or (first (keep (fn [[label param]]
                         (arg-label-idx idx [label param]))
                       sorted))
          :arg/default))))

(defn stack-entry
  "creates a stack entry from type and op"
  {:added "4.0"}
  [type op & [opts]]
  (let [spec (get-layout-spec type op)
        label-fn  (or (get-in spec [:fn :fn/label])
                      (arg-label-fn (:args spec)))]
    (-> spec
        (update-in [:fn] assoc :fn/label label-fn)
        (merge opts))))

(defn stack-entry-root
  "creates the root stack entry"
  {:added "4.0"}
  [opts]
  (merge {:type :root
          :op :type/root
          :indent 0
          :position 0}
         opts))

;;
;;
;;
;;

(comment
  (defn layout-form-initial
    "generates the initial state"
    {:added "4.0"}
    [opts]
    {:code    (edit/parse-string "")
     :stack   [{:type :root :indent (or (:indent opts) 0)
                :position 0}]
     :options opts})
  )




(comment
  

  '(let [a 1 b 2] (+ a 1))
  '([a 1 b 2] (+ a 1))
  
  {:type :form
   :op 'let
   :block 1,
   :args
   {:arg/bindings
    {:at 1,
     :inline true,
     :layout {:type :vector, :columns 2, :align-col true}},
    :arg/default {}},
   :fn {:fn/label (fn [idx arg]
                    (cond (= idx 1) :bindings
                          :else     :default))
        :fn/multiline let-check-multiline}}

  )



(comment
  (args-check
   {:bindings
    {:at 1,
     :inline true,
     :layout {:type :vector, :columns 2, :align-col true}},
    :default {}})


  (create-arg-check
   {:bindings
    {:at 1,
     :inline true,
     :layout {:type :vector, :columns 2, :align-col true}},
    :default {}}))

