(ns code.refactor.base.format
  (:require [code.edit :as nav]
            [code.query.walk :as walk]
            [std.lib.zip :as zip]
            [std.block.base :as base]
            [std.block.type :as type]
            [std.block.construct :as construct]
            [std.block.grid :as grid]
            [std.block.layout.common :as layout]))

(defn remove-surrounding-whitespace
  "Removes whitespace surrounding inner forms.
   (nav/string
    (remove-surrounding-whitespace (nav/parse-root \"(  foo  )\")))
   => \"(foo)\""
  {:added "3.0"}
  [zloc]
  (walk/matchwalk
   zloc
   [(fn [nav] (base/container? (zip/get nav)))]
   (fn [nav]
     (cond-> nav
       (nav/down nav)
       (-> nav/down nav/tighten-left nav/up)

       (nav/down nav)
       (-> nav/down nav/right-most nav/tighten-right nav/up)))
   {}))

(defn- step-dfs [zip]
  ;; Depth-first step
  (cond (and (zip/can-step-inside? zip)
             (not (zip/is-empty-container? zip)))
        (zip/step-inside zip)

        (zip/can-step-right? zip)
        (zip/step-right zip)

        :else
        (loop [z zip]
          (cond (zip/can-step-outside? z)
                (let [up (zip/step-outside z)]
                  (if (zip/can-step-right? up)
                    (zip/step-right up)
                    (recur up)))
                :else nil))))

(defn remove-consecutive-blank-lines
  "Collapses consecutive blank lines.
   (nav/string
    (remove-consecutive-blank-lines (nav/parse-root \"(foo)\n\n\n(bar)\")))
   => \"(foo)\n\n(bar)\""
  {:added "3.0"}
  [zloc]
  (loop [zip zloc]
    (let [n1 (zip/get zip)
          n2 (zip/right-element zip)
          n3 (if n2 (zip/right-element (zip/step-right zip)))]
      (if (and (type/linebreak-block? n1)
               (type/linebreak-block? n2)
               (type/linebreak-block? n3))
        (recur (zip/delete-right zip)) ;; Delete n2, stay at n1. Recurse.

        (let [next-zip (step-dfs zip)]
          (if next-zip
            (recur next-zip)
            (zip/step-outside-most zip)))))))

(defn insert-missing-whitespace
  "Inserts whitespace missing from between elements.
   (nav/string
    (insert-missing-whitespace (nav/parse-root \"(foo(bar))\")))
   => \"(foo (bar))\""
  {:added "3.0"}
  [zloc]
  (loop [zip zloc]
    (let [curr (zip/get zip)
          next (zip/right-element zip)]
      ;;(println "DEBUG" (base/block-string curr) (if next (base/block-string next) "nil"))
      (if (and (base/expression? curr)
               (not (type/modifier-block? curr))
               next
               (base/expression? next)
               (not (type/linespace-block? next)))
        (let [nzip (-> zip
                       (zip/insert-right (construct/space))
                       (zip/step-right) ;; At space
                       (zip/step-right) ;; At next
                       )]
          (recur nzip))

        (let [next-zip (step-dfs zip)]
          (if next-zip
            (recur next-zip)
            (zip/step-outside-most zip)))))))

(declare default-indent-rules)

(defn align-keys
  "Aligns map keys vertically using std.block.layout."
  {:added "3.0"}
  [zloc]
  (walk/matchwalk
   zloc
   [(fn [nav] (= :map (base/block-tag (zip/get nav))))]
   (fn [nav]
     (let [block (zip/get nav)
           children (base/block-children block)
           exprs (filter base/expression? children)
           pairs (vec (partition 2 exprs))
           nblock (layout/layout-multiline-hashmap pairs {:spec {:col-align true :col-sort false}
                                                          :indents (second (:position nav))})]
       (zip/replace-right nav nblock)))
   {}))

(defn sort-keys
  "Sorts map keys using std.block.layout."
  {:added "3.0"}
  [zloc]
  (walk/matchwalk
   zloc
   [(fn [nav] (= :map (base/block-tag (zip/get nav))))]
   (fn [nav]
     (let [block (zip/get nav)
           children (base/block-children block)
           exprs (filter base/expression? children)
           pairs (vec (partition 2 exprs))
           nblock (layout/layout-multiline-hashmap pairs {:spec {:col-align false :col-sort true}
                                                          :indents (second (:position nav))})]
       (zip/replace-right nav nblock)))
   {}))

(def default-indent-rules
  '{do    {:indent 1 :bind 0 :scope [0]}
    let   {:indent 1 :bind 1 :scope [0]}
    letfn {:indent 1 :bind 0 :scope [0 [0 2]]}
    doseq {:indent 1 :bind 1 :scope [0]}
    if    {:indent 1 :bind 0 :scope []}
    if-let {:indent 1 :bind 1 :scope []}
    when  {:indent 1 :bind 0 :scope []}
    defn  {:indent 1 :bind 0 :scope [0]}
    fn    {:indent 1 :bind 0 :scope [0]}
    defmacro {:indent 1 :bind 0 :scope [0]}
    try   {:indent 1 :bind 0 :scope []}
    catch {:indent 1 :bind 1 :scope []}
    finally {:indent 1 :bind 0 :scope []}
    case  {:indent 1 :bind 0 :scope []}
    cond  {:indent 1 :bind 0 :scope []}
    cond-> {:indent 1 :bind 0 :scope []}
    cond->> {:indent 1 :bind 0 :scope []}
    as-> {:indent 1 :bind 0 :scope []}})

(defn align-to-indent
  "Aligns code to indent.
   Uses default rules."
  {:added "3.0"}
  [zloc]
  (walk/matchwalk
   zloc
   [(fn [nav] (base/container? (zip/get nav)))]
   (fn [nav]
     (let [block (zip/get nav)
           [_ anchor] (:position nav)
           nblock (grid/grid block anchor {:rules default-indent-rules})]
       (zip/replace-right nav nblock)))
   {}))
