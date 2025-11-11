(ns js.react.compile-directives
  (:require [std.lib.walk :as walk]
            [std.lib :as h]
            [std.string :as str]
            [js.react.compile-components :as c]))

(defn compile-ui-tailwind
  "templates the layout controls"
  {:added "4.0"}
  [elem classes]
  (let [{:keys [tag
                props
                children]} (c/classify-tagged elem false)
        pclasses (if (string? (:class props))
                  (str/split (:class props) #" ")
                  (:class props))
        tclasses (map (fn [[k v]]
                        (cond (boolean? v)
                              (name k)
                              
                              :else
                              (str (name k) "-" (h/strn v))))
                      (dissoc props :style :class))
        ;; media query classes
        qclasses []]
    [:div (merge {:class (vec (concat classes
                                      pclasses
                                      tclasses))}
                 (select-keys props [:style]))]))

(defn compile-directives
  "templates the control directives"
  {:added "4.0"}
  [elem components]
  (let [[op control & more] elem]
    (case (name op)
      "pad" (compile-ui-tailwind elem ["grow"])
      "v"   (compile-ui-tailwind elem ["flex" "flex-col" "grow"])
      "h"   (compile-ui-tailwind elem ["flex" "flex-row" "grow"])
      "for" (let [[[idx val] array]  control]
              (h/$
               (. ~array (map (fn [~val ~idx]
                                (return
                                 [:<>
                                  {:key ~idx}
                                  ~@more]))))))
      "input"    op
      "children" op)))

