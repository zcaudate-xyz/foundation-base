(ns js.react.compile-components
  (:require [std.lib.walk :as walk]
            [std.lib :as h]
            [std.string :as str]))

(defn classify-tagged
  "classifies the hiccup form"
  {:added "4.0"}
  [elem & [is-template]]
  (let [[tag props? & children] elem
        [tag props? children] (if (= tag :%)
                                [props? (first children) (rest children)]
                                [tag props? children])
        [props children] (if (map? props?)
                           [props? (vec children)]
                           [{} (vec (filter identity (cons props? children)))])
        children  (if (and is-template
                           (empty? children))
                    [:props/children]
                    children)]
    {:tag tag
     :props props
     :children children}))

(defn components-resolve
  "resolve component map"
  {:added "4.0"}
  [inputs]
  (cond (symbol? inputs)
        (or (and (resolve inputs)
                 @(resolve inputs))
            inputs)

        (h/form? inputs)
        (eval
         (map (fn [x]
                (if (map? x)
                  (list 'quote x)
                  x))
              inputs))
        
        :else
        inputs))

(defn components-find-deps
  "creates a dependency tree for component map"
  {:added "4.0"}
  [inputs]
  (let [ks    (set (keys inputs))]
    (h/map-vals
     (fn [{:keys [tag props children]}]
       (let [deps (volatile! #{})
             _    (walk/postwalk
                   (fn [x]
                     (if (and (keyword? x)
                              (if-let [ns (namespace x)]
                                (not (#{"props"
                                        "*"
                                        "%"
                                        "var"} ns)))
                              (get ks x))
                       (vswap! deps conj x))
                     x)
                   [tag props children])]
         {:deps @deps}))
     inputs)))

(defn components-expand
  "expands a vector component to standardised form"
  {:added "4.0"}
  [inputs]
  (let [inputs (h/map-vals
                (fn [tmpl]
                  (cond (vector? tmpl)
                        (classify-tagged tmpl true)

                        (map? tmpl)
                        (merge {:props {}
                                :children [:props/children]}
                               tmpl)))
                inputs)
        deps   (components-find-deps inputs)]
    (h/merge-nested inputs deps)))

;;
;;
;;

(defn compile-replace
  "compiles the layout with parameters"
  {:added "4.0"}
  [template props children]
  (walk/postwalk
   (fn [x]
     (cond (= x :props/children)
           (case (count children)
             0 nil
             1 (first children)
             (apply vector :<> children))

           (and (keyword? x)
                (if-let [ns (namespace x)]
                  (= "props" ns)))
           (get props x)

           #_#_(vector? x)
           (vec (filter identity x))
           
           :else x))
   template))

(defn find-namespaced-props
  "finds all the :prop/<val> keys"
  {:added "4.0"}
  [form]
  (let [deps (volatile! #{})
        _    (walk/postwalk
              (fn [x]
                (if (and (keyword? x)
                         (if-let [ns (namespace x)]
                           (= "props" ns)))
                  (vswap! deps conj x))
                x)
              form)]
    @deps))

(defn get-tmpl-props
  "gets template and body props given input"
  {:added "4.0"}
  [input-props ns-props]
  (let [{:keys [tmpl body]}
        (reduce (fn [{:keys [tmpl body]
                      :as out} tk]
                  (let [bk (keyword (name tk))]
                    (if-let [v (get body bk)]
                      {:tmpl (assoc tmpl tk v)
                       :body (dissoc body bk)}
                      out)))
                {:tmpl {}
                 :body input-props}
                (seq ns-props))]
    [tmpl body]))

(defn getter-symbol
  "creates the getter symbol"
  {:added "4.0"}
  [kw]
  (symbol (str/camel-case (name kw))))

(defn setter-symbol
  "creates the setter symbol"
  {:added "4.0"}
  [kw]
  (symbol (str/camel-case (str "set-" (name kw)))))

(defn compile-walk-variables
  "replace :var/<name> as react states"
  {:added "4.0"}
  [form]
  (let [is-var (fn [x]
                 (and (keyword? x)
                      (if-let [ns (namespace x)]
                        (= "var" ns))))]
    (walk/prewalk
     (fn [x]
       (cond (and (list? x)
                  (is-var (first x)))
             (cons (setter-symbol (first x)) (rest x))
             
             (is-var x)
             (getter-symbol x)
             
             :else x))
     form)))

(defn compile-element-action-do
  "does a do block"
  {:added "4.0"}
  [form {:keys []}]
  (compile-walk-variables form))

(defn compile-element-action-set
  "compiles the :%/set action"
  {:added "4.0"}
  [var {:keys [to
               transform]}]
  (list (setter-symbol var)
        (cond->> (compile-walk-variables to)
          transform (list transform))))

(defn compile-element-action-inc
  "compiles the :%/set action"
  {:added "4.0"}
  [var {:keys [val mod]}]
  (list (setter-symbol var)
        (cond->> (list '+  (getter-symbol var)
                       (or val 1))
          mod (list (h/$
                      (fn [val]
                        (return (:? (<= 0 val)
                                    (mod val ~mod)
                                    (+ (mod val ~mod) ~mod)))))))))

(defn compile-element-action-dec
  "compiles the :%/set action"
  {:added "4.0"}
  [var {:keys [val mod]}]
  (list (setter-symbol var)
        (cond->> (list '-  (getter-symbol var)
                       (or val 1))
          mod (list (h/$
                      (fn [val]
                        (return (:? (<= 0 val)
                                    (mod val ~mod)
                                    (+ (mod val ~mod) ~mod)))))))))

(defn compile-element-action-set-async
  "compiles the :%/set-async action"
  {:added "4.0"}
  [var {:keys [to
               error
               pending
               transform]}]
  (let [to (compile-walk-variables to)
        form-pending-start (if pending
                             (list (js.react.compile-components/setter-symbol pending) true))
        form-pending-end   (if pending
                             (h/$ (finally (fn []
                                             (~(js.react.compile-components/setter-symbol pending) false)))))
        form-error         (if error
                             (h/$ (catch (fn [err]
                                           (~(js.react.compile-components/setter-symbol error) err)))))
        form-transform     (if transform
                             (h/$ (then (fn [res]
                                          (return
                                           (~transform res))))))
        form-set           (h/$ (then (fn [res]
                                        (~(js.react.compile-components/setter-symbol var) res))))]
    (h/$ (do ~@(if pending
                 [form-pending-start])
             (. ~to
                ~@(if transform
                    [form-transform])
                ~form-set
                ~@(if error
                    [form-error])
                ~@(if pending
                    [form-pending-end]))))))

(defn compile-element-actions
  "compiles the element actions"
  {:added "4.0"}
  [actions]
  (cond (list? actions)
        (compile-walk-variables actions)

        (vector? actions)
        (concat ['fn []]
                (keep (fn [action]
                        (cond (:%/set action)
                              (compile-element-action-set
                               (:%/set action)
                               action)
                              
                              (:%/set-async action)
                              (compile-element-action-set-async
                               (:%/set-async action)
                               action)

                              (:%/inc action)
                              (compile-element-action-inc
                               (:%/inc action)
                               action)                              
                              
                              (:%/dec action)
                              (compile-element-action-dec
                               (:%/dec action)
                               action)                              

                              (:%/do action)
                              (compile-element-action-do
                               (:%/do action)
                               action)))
                      actions))))

(defn compile-element-directives
  "compiles element directives"
  {:added "4.0"}
  [view props]
  (case (:type view)
    :input  (let [{:keys [get set key]} view]
              (cond-> props
                (:%/value props) (-> (dissoc :%/value)
                                     (assoc (:key get) (cond->> (getter-symbol (:%/value props))
                                                         (:transform get) (list (compile-walk-variables (:transform get))))
                                            (:key set) (let [set-fn  (setter-symbol (:%/value props))]
                                                         (if (:transform set)
                                                           (h/$ (fn [input]
                                                                  (~set-fn (~(:transform set)
                                                                            input))))
                                                           set-fn))))
                (:%/action props) (-> (dissoc :%/action)
                                       (assoc key (compile-element-actions (:%/action props))))))
    :action (let [{:keys [key]} view]
              (if (:%/action props)
                (-> props
                    (dissoc :%/action)
                    (assoc key (compile-element-actions (:%/action props))))))))

(defn compile-element
  "expands the template"
  {:added "4.0"}
  [elem components]
  (let [{:keys [tag
                props
                children]} (classify-tagged elem false)
        tmpl (get components tag)
        ns-props (find-namespaced-props [(:props tmpl)
                                         (:children tmpl)])
        [tmpl-props
         body-props]  (get-tmpl-props props ns-props)
        
        body-props (if (:view tmpl)
                     (compile-element-directives
                      (:view tmpl)
                      body-props)
                     body-props)
        body (cons (compile-replace (merge  (:props tmpl)
                                            body-props)
                                    tmpl-props
                                    [])
                   (compile-replace (:children tmpl)
                                    tmpl-props
                                    children))]
    (cond (h/pointer? (:tag tmpl))
          (apply vector :% (symbol (name (:module (:tag tmpl)))
                                   (name (:id (:tag tmpl))))
                 body)

          (symbol? (:tag tmpl))
          (apply vector :% (:tag tmpl) body)
          
          :else
          (apply vector (:tag tmpl) body))))

(defn compile-element-loop
  "will loop until there are no dependencies"
  {:added "4.0"}
  [elem components layout-fn]
  (let [[tag] elem
        tmpl  (or (get components tag)
                  (h/error "Tag not found: "
                           {:tag tag
                            :element elem
                            :components
                            (sort (keys components))}))
        {:keys [deps]} tmpl
        out (compile-element elem components)]
    (if (not-empty deps)
      (layout-fn out components)
      out)))

