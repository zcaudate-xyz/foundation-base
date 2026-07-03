(ns js.blessed.frame-status
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[xt.lang.spec-base :as xt]
             [js.react :as r]
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
        width  (Math.min (:? content (xt/x:len content) 0)
                         50)
        clearFn (fn:> (setStatus {:content ""
                                :type "info"}))]
    (r/init []
      (when autoClear
        (let [id (setTimeout
                  (fn []
                    (setStatus {:content ""
                                :type "info"}))
                  2500)]
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
