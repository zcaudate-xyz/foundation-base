(ns code.doc.link.stencil
  (:require [std.lib.mustache :as mustache]
            [std.string.common :as common]))

(def full-citation-pattern
  (common/joinl ["\\[\\["
              "([^/^\\.^\\{^\\}^\\[^\\]]+)"
              "/"
              "([^/^\\.^\\{^\\}^\\[^\\]]+)"
              "\\]\\]"]))

(def short-citation-pattern
  (common/joinl ["\\[\\["
              "([^/^\\.^\\{^\\}^\\[^\\]]+)"
              "\\]\\]"]))

(def full-pattern
  (common/joinl ["\\{\\{"
              "([^/^\\.^\\{^\\}]+)"
              "/"
              "([^/^\\.^\\{^\\}]+)"
              "\\}\\}"]))

(def short-pattern
  (common/joinl ["\\{\\{"
              "([^/^\\.^\\{^\\}]+)"
              "\\}\\}"]))

(defn transform-stencil
  "creates a link to the given tags"
  {:added "3.0"}
  ([^String string name tags]
   (-> string
       (.replaceAll full-citation-pattern "[{{$1/$2}}]($1.html#$2)")
       (.replaceAll short-citation-pattern "[{{$1}}](#$1)")
       (.replaceAll full-pattern  "{{$1.$2.number}}")
       (.replaceAll short-pattern (str "{{" name ".$1.number}}"))
       (mustache/render tags))))

(defn link-stencil
  "creates links to all the other documents in the project"
  {:added "3.0"}
  ([interim name]
   (let [anchors (assoc (:anchors interim)
                        :PROJECT (:project interim)
                        :DOCUMENT (get-in interim [:articles name :meta]))]
     (update-in interim [:articles name :elements]
                (fn [elements]
                  (->> elements
                       (map (fn [element]
                              (cond (= :paragraph (:type element))
                                    (update-in element [:text]
                                               transform-stencil name anchors)

                                    (:stencil element)
                                    (update-in element [:code]
                                               transform-stencil name anchors)

                                    :else element)))))))))
