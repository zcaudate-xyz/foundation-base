(ns js.blessed.frame-status
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.core :as j]
             [js.react   :as r]
             [js.lib.chalk :as chalk]]})

(defn.js Status
  "displays status"
  {:added "4.0"}
  [#{[busy
      setBusy
      (:= status {:content ""
                  :type "info"})
      setStatus
      autoClear
      (:.. rprops)]}]
  (let [#{content type} status
        width  (j/min [(:? content (k/len content) 0)
                        50])
        clearFn (fn:> (setStatus {:content ""
                                :type "info"}))]
    (r/init []
      (when autoClear
        (let [id (j/delayed [2500]
                   (setStatus {:content ""
                               :type "info"}))]
          (return (fn:> (clearTimeout id))))))
    (return [:box #{[:height 1
                     :shrink true
                     :bg "black"
                     (:.. rprops)]}
             [:button {:style (:? busy {:bg "white" :bold true :fg "black"} {:bg "black" :bold true :fg "white"})
                       :left 0 :width 3
                       :mouse true
                       :on-click (fn [] (setBusy false))
                       :content (:? busy " ! " " * ")}]
             (:? content
                 [:button {:content (+ " " content " ")
                           :left 3 :mouse true
                           :on-click (fn []
                                       (setStatus {:content "" :type "info"}))
                           :style {:bg (or (. {:error "red" :info "blue" :warn "yellow"} [type]) "blue")
                                   :bold (not= type "info") :fg (:? (== type "info") "white" "black")}}])])))
