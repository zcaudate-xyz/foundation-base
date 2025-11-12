(ns js.react.compile
  (:require [std.lib.walk :as walk]
            [std.string :as str]
            [std.lib :as h]
            [std.lang :as l]
            [js.react.compile-components :as c]
            [js.react.compile-directives :as d]))

(defn check-valid-variables
  [states form]
  (walk/postwalk
   (fn [x]
     (if (and (keyword? x)
              (if-let [ns (namespace x)]
                (= "var" ns))
              (not (get states x)))
       (h/error "Invalid variable"
                {:keyword x
                 :valid (sort (keys states))}))
     x)
   form))

(defn compile-states-deps
  "get dependencies for states"
  {:added "4.0"}
  [states]
  (let [ks    (set (keys states))]
    (h/map-vals
     (fn [state]
       (let [deps (volatile! #{})
             _    (walk/postwalk
                   (fn [x]
                     (if (and (keyword? x)
                              (if-let [ns (namespace x)]
                                (= "var" ns)))
                       (vswap! deps conj x))
                     x)
                   state)]
         @deps))
     states)))

(defn compile-states
  "compiles to react code"
  {:added "4.0"}
  [states]
  (let [deps  (compile-states-deps states)
        deps-order (h/topological-sort deps)
        forms (h/map-entries
               (fn [[k e]]
                 [k (cond (and (map? e)
                               (:% e))
                          (list 'var (c/getter-symbol k)
                                (c/compile-walk-variables (:% e)))

                          (and (map? e)
                               (:%/args e))
                          (list 'var [(c/getter-symbol k)
                                      (c/setter-symbol k)]
                                (apply list
                                       (or (:%/fn e)
                                           'React.useState) (:%/args e)))
                          
                          :else
                          (list 'var [(c/getter-symbol k)
                                      (c/setter-symbol k)]
                                (list 'React.useState e)))])
               states)]
    (map forms deps-order)))

(defn compile-layout-raw
  [layout components]
  (walk/postwalk
   (fn [elem]
     (cond (and (vector? elem)
                (keyword? (first elem))
                (= "*" (namespace (first elem))))
           (d/compile-directives elem components)
           
           
           (and (vector? elem)
                (keyword? (first elem))
                (namespace (first elem)))
           (if (not (#{"%"
                       "var"
                       "props"} (namespace (first elem))) )
             (c/compile-element-loop elem components compile-layout-raw)
             elem)           
           
           :else
           elem))
   layout))

(defn compile-replace-actions
  "compiles to react code"
  {:added "4.0"}
  [layout actions]
  (walk/postwalk
   (fn [x]
     (if (and (keyword? x)
              (if-let [ns (namespace x)]
                (= "action" ns)))
       (or (get actions x)
           (h/error "Invalid variable"
                    {:keyword x
                     :valid (sort (keys actions))}))
       x))
   layout))

(defn compile-triggers
  "compiles to react code"
  {:added "4.0"}
  [triggers]
  (map (fn [[_ t]]
         (let [action-form (c/compile-element-actions (:%/action t))]
           (list 'React.useEffect
                 action-form
                 (mapv c/getter-symbol (:%/watch t)))))
       triggers))

(defn compile-layout
  "compiles the ui"
  {:added "4.0"}
  [layout components]
  (let [components (-> components
                       (c/components-resolve)
                       (c/components-expand))]
    (-> (compile-layout-raw layout components)
        (c/compile-walk-variables))))

(defn compile-full
  [{:keys [triggers actions states layout components]}]
  (let [triggers   (compile-replace-actions triggers actions)
        layout     (compile-replace-actions layout actions)
        components (-> components
                       (c/components-resolve)
                       (c/components-expand))
        components (compile-replace-actions components actions)
        form-states   (compile-states   (check-valid-variables states states))
        form-triggers (compile-triggers (check-valid-variables states triggers))
        form-layout   (compile-layout   (check-valid-variables states layout)
                                        (check-valid-variables states components))]
    (concat '[do]
            form-states
            form-triggers
            [(list 'return
                   form-layout)])))

(comment
  
  '{:states {:var/html-code   ""
             :var/dsl-code    {:%/args  [""]}
             
             :var/history     {:%  (useLocalHistory "task.translate-html")}
             :var/history-idx 0
             :var/data        {:errored false
                               :warning false}
             :var/combined    {:%  (+ :var/dsl-code
                                      :var/html-code)}}
    
    :layout [:#/container
             [:#/toolbar
              [:ui/toolbar.button
               {:%/action [{:%/set :var/html-code
                            :from ""}]}]]
             [:*/for [[i p] pages]
              [:ui/button
               {:%/action [{:%/run (do (:var/history-idx 2)
                                       (return 1))}
                           {:%/set :var/history-idx
                            :from i}
                           {:%/set.async :var/history-idx
                            :from (async-fn :var/dsl-code)
                            :pending :var/pending
                            :error   :var/error
                            :transform (fn [])}]}
               p.text]]]
    
    :components
    {:#/toolbar.action.reset
     [:ui/toolbar.button
      {:%/action [[:%/set :var/html-code ""]]}
      [:ui/icon {:name "Clear"}]]}}
  
  [:ui/text-area
   {:%/value :var/html-code}]
  
  
  {:ui/icon  {:tag Icon}
   :ui/toolbar.button [:ui/button
                       {:class ["p-1"]}
                       [:ui/icon
                        {:name :props/name}]]
   
   :ui/button
   {:tag  :button
    :view {:type :action
           :key :onClick}}
   :ui/text-area
   {:tag  :textarea
    :view {:type :input
           :get :value
           :set :onChange}}}
  
  {}
  
  [:ui/button
   {:%/action [{:%/set :var/history-idx
                :from i}
               {:%/set-async :var/history-idx
                :from (async-fn :var/dsl-code)
                :pending :var/pending
                :error   :var/error
                :transform (fn [])}]}
   p.text]

  #_#_
  :var/history     {:%/fn    useLocalHistory
                    :%/args  ["task.translate-html"]}

  )
  
