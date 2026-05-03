(ns js.react.compile-directives
  (:require [clojure.string]
            [js.react.compile-components :as c]
            [std.lib.collection :as collection]
            [std.lib.foundation :as f]
            [std.lib.template :as template]
            [std.lib.walk :as walk]))

(defn compile-ui-tailwind
  "templates the layout controls"
  {:added "4.0"}
  [elem classes]
  (let [{:keys [tag
                props
                children]} (c/classify-tagged elem false)
        pclasses (if (string? (:class props))
                   (clojure.string/split (:class props) #" ")
                   (:class props))
        tclasses (map (fn [[k v]]
                        (cond (boolean? v)
                              (name k)
                              
                              :else
                              (str (name k) "-" (f/strn v))))
                      (dissoc (collection/unqualified-keys props) :style :class))
        ;; media query classes
        qclasses []]
    (apply vector :div (merge {:class (vec (concat classes
                                                   pclasses
                                                   tclasses))}
                              (select-keys props [:style]))
           children)))

(defn compile-directives
  "templates the control directives"
  {:added "4.0"}
  [elem components]
  (let [[op control & more] elem]
    (case (name op)
      "pad" (compile-ui-tailwind elem ["flex" "grow"])
      "v"   (compile-ui-tailwind elem ["flex" "flex-col"])
      "h"   (compile-ui-tailwind elem ["flex" "flex-row"])
      "for" (let [[[idx val] array]  control]
              (template/$
               (. ~array (map (fn [~val ~idx]
                                (return
                                 [:<>
                                  {:key ~idx}
                                  ~@more]))))))
      "input"    op
      "children" op)))

