(ns std.lang.seedgen.form-common
  (:require [std.block.base :as block]
            [std.block.navigate :as nav]
            [std.lang.seedgen.common-util :as common]))

(defn target-normalize-langs
  ([lang]
   (target-normalize-langs lang nil))
  ([lang default-langs]
   (let [target-langs (cond
                        (= :all lang)
                        (or default-langs :all)

                        (keyword? lang)
                        [lang]

                        (vector? lang)
                        lang

                        (seq? lang)
                        (vec lang)

                        (nil? lang)
                        default-langs

                        :else
                        [lang])]
     (cond
       (= :all target-langs)
       :all

       (nil? target-langs)
       nil

       :else
       (->> target-langs
            (map common/seedgen-normalize-runtime-lang)
            distinct
            vec)))))

(defn nav-meta-block?
  [zloc]
  (= :meta (block/block-tag (nav/block zloc))))

(defn nav-meta
  [zloc]
  (when (nav-meta-block? zloc)
    (nav/down zloc)))

(defn nav-body
  [zloc]
  (if (nav-meta-block? zloc)
    (-> zloc nav/down nav/right)
    zloc))

(defn nav-top-levels
  [root]
  (loop [current (nav/down root)
         out     []]
    (if (nil? current)
      out
      (recur (nav/right current) (conj out current)))))

(defn nav-entry
  [zloc]
  {:form (nav/block zloc)
   :line (nav/line-info zloc)})

(defn nav-map-value
  [map-nav target-key]
  (loop [current (some-> map-nav nav/down)]
    (cond
      (nil? current)
      nil

      (= target-key (nav/value current))
      (nav/right current)

      :else
      (recur (some-> current nav/right nav/right)))))

(defn nav-vector-items
  [vector-nav]
  (loop [current (some-> vector-nav nav/down)
         out     []]
    (if (nil? current)
      out
      (recur (nav/right current) (conj out current)))))

(defn item-line-key
  [{:keys [row col end-row end-col]}]
  [row col end-row end-col])

(defn item-form
  [item]
  (:form item))

(defn item-line
  [item]
  (:line item))

(defn item-string
  [item]
  (some-> item item-form block/block-string))

(defn item-value
  [item]
  (some-> item item-form block/block-value))

(defn item-lang
  [item]
  (some-> item item-value common/seedgen-dispatch-lang))

(defn item-sort
  [items]
  (sort-by (comp item-line-key item-line) items))

(defn item-classify-langs
  [classification]
  (->> classification
       vals
       (mapcat identity)
       item-sort))

(defn item-runtime-map
  [classification]
  (->> (item-classify-langs classification)
       (keep (fn [item]
               (when-let [lang (item-lang item)]
                 [lang item])))
       (into {})))
