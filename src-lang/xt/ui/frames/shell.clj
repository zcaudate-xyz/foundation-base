(ns xt.ui.frames.shell
  "Reusable page and application shell composition."
  (:require [hara.lang :as l]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.ui.core :as ui]]})

(defn.xt view [frame content]
  (var opts (xt/x:get-key frame "opts"))
  (return
   (ui/node "ui/column" {"class" (or (xt/x:get-key opts "class")
                                      "mx-auto w-full gap-6 p-4 md:p-8")}
            [(ui/node "ui/column" {"class" "gap-1"}
                      [(ui/node "ui/title"
                                {"value" (or (xt/x:get-key opts "title") "")}
                                [])
                       (ui/node "ui/description"
                                {"value" (or (xt/x:get-key opts "description") "")}
                                [])])
             (ui/slot (xt/x:cat
                       (xt/x:get-key frame "id") "/content")
                      (or content []) {})])))
