(ns js.react.layout
  (:require [std.lang :as l]
            [std.lib.collection]
            [std.lib.context.pointer]
            [std.lib.foundation]
            [std.lib.sort]
            [std.lib.template]
            [std.lib.walk :as walk]
            [std.string.common]))

(defn ui-template-classify
  [elem fill-empty]
  (let [[tag props? & children] elem
        [tag props? children] (if (= tag :%)
                                [props? (first children) (rest children)]
                                [tag props? children])
        [props children] (if (map? props?)
                           [props? (vec children)]
                           [{} (vec (filter identity (cons props? children)))])
        children  (if (and fill-empty
                           (empty? children))
                    [:*/children]
                    children)]
    {:tag tag
     :props props
     :children children}))

(defn ui-template-controls-layout
  [elem classes]
  (let [{:keys [tag
                props
                children]} (ui-template-classify elem false)
        pclasses (if (string? (:class props))
                  (std.string.common/split (:class props) #" ")
                  (:class props))
        tclasses (map (fn [[k v]]
                        (cond (boolean? v)
                              (name k)
                              
                              :else
                              (str (name k) "-" (std.lib.foundation/strn v))))
                      (dissoc props :style :class))
        ;; media query classes
        qclasses []]
    [:div (merge {:class (vec (concat classes
                                      pclasses
                                      tclasses))}
                 (select-keys props [:style]))]))

(defn ui-template-controls
  [elem components]
  (let [[op control & more] elem]
    (case (name op)
      "pad" (ui-template-controls-layout elem ["grow"])
      "v"   (ui-template-controls-layout elem ["flex" "flex-col" "grow"])
      "h"   (ui-template-controls-layout elem ["flex" "flex-row" "grow"])
      "for" (let [[[idx val] array]  control]
              (std.lib.template/$
               (. ~array (map (fn [~val ~idx]
                                (return
                                 [:<>
                                  {:key ~idx}
                                  ~@more]))))))
      "input"    op
      "children" op)))

(defn ui-template-replace
  [template children]
  (walk/postwalk
   (fn [x]
     (if (= x :*/children)
       (if (seq children)
         (apply vector :<> children)
         nil)
       x))
   template))

(defn ui-template-namespaced
  [elem components]
  (let [{:keys [tag
                props
                children]} (ui-template-classify elem false)
        tmpl (or (get components tag)
                 (std.lib.foundation/error "Tag not found: "
                          {:tag tag
                           :element elem
                           :components components}))
        body (cons (merge (:props tmpl) props)
                   (ui-template-replace (:children tmpl)
                                        children))]
    (cond (std.lib.context.pointer/pointer? (:tag tmpl))
          (apply vector :% (l/sym-full (:tag tmpl)) body)

          (symbol? (:tag tmpl))
          (apply vector :% (:tag tmpl) body)
          
          :else
          (apply vector (:tag tmpl) body))))

(defn ui-template-components-resolve
  [components]
  (cond (symbol? components)
        (or (and (resolve components)
                 @(resolve components))
            components)

        (list? components)
        (eval components)

        :else
        components))

(defn ui-template-components-classify
  [components]
  (let [ks    (set (keys components))
        deps  (std.lib.collection/map-vals
               (fn [form]
                 (let [deps (volatile! #{})
                       _    (walk/postwalk
                             (fn [x]
                               (if (and (keyword? x)
                                        (namespace x)
                                        (get ks x))
                                 (vswap! deps conj x))
                               x))]
                   deps))
               components)]
    (std.lib.sort/topological-sort deps)))

(defn ui-template-components-expand
  [components]
  )



(defn ui-template
  [input components]
  (let [components (ui-template-components-resolve components)
        components (std.lib.collection/map-vals
                  (fn [tmpl]
                    (if (vector? tmpl)
                      (ui-template-classify tmpl true)
                      tmpl))
                  components)]
    (walk/postwalk
     (fn [elem]
       (cond (and (vector? elem)
                  (keyword? (first elem))
                  (= "*" (namespace (first elem))))
             (ui-template-controls elem components)
             

             (and (vector? elem)
                  (keyword? (first elem))
                  (namespace (first elem)))
             (ui-template-namespaced elem components)           
             
             :else
             elem))
     input)))
