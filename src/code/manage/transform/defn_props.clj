(ns code.manage.transform.defn-props
  (:require [code.query :as query]
            [std.block.navigate :as nav]))

(defn transform-props
  "transforms #{[...]} props notation to {:# [...] :.. props}"
  [zloc]
  (let [node (nav/value zloc)]
    (if (and (set? node)
             (= 1 (count node))
             (vector? (first node)))
      (let [vec-content (first node)
            last-elem (peek vec-content)]
        (if (and (seq? last-elem)
                 (= :.. (first last-elem)))
          (let [props (second last-elem)
                new-vec (pop vec-content)
                replacement-map {:# new-vec :.. props}]
            (nav/replace zloc replacement-map))
          zloc))
      zloc)))

(defn transform
  "applies the transform to a zipper or source"
  [nav]
  (query/modify nav '#{_} transform-props))
