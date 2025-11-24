(ns code.dev.client.page-index-test
  (:use code.test)
  (:require [code.dev.client.page-index :refer :all]))

^{:refer code.dev.client.page-index/App :added "4.0"}
(fact "TODO")

^{:refer code.dev.client.page-index/main :added "4.0"}
(fact "TODO")


(comment
  :app/header  [:div
                {:class ["navbar" "bg-base-100" "shadow-sm"]}]
  
  :app/footer  [:nav
                {:class ["bg-white" "shadow-md" "p-4" "sticky" "top-0" "z-50"]}
                [:div {:class "flex items-center"}
                 [:a {:class "text-2xl font-display text-brand-dark font-bold"}
                  :*/children]]]
  
  (:form @App)
  (std.block/layout
   (std.lib.walk/postwalk
    (fn [x]
      (if (map? x)
        (let [v (or (:class x)
                    (:classname x))
              v (if (string? v)
                  [v]
                  v)]
          (-> x
              (assoc :class v)
              (dissoc :class :classname)))
        x))
    (std.html/tree
     (slurp "src/code/dev/server/transform.txt")))))


^{:refer code.dev.client.page-index/AppIndex :added "4.0"}
(fact "TODO")