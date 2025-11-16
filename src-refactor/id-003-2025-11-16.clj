(ns refactor.id-001-2025-12-05)

(comment

  (code.manage/refactor-code
   '[js]
   {:print {:function true}
    :write true
    :edits [(fn [nav]
              (code.query/modify
               nav
               [(fn [form]
                  (= '(def.js MODULE (!:module))))]
               (fn [nav]
                 (-> nav
                     (code.edit/delete)))))]})
  
  (code.manage/locate-test
   :all
   {:query [(fn [form]
              (and (vector? form)
                   (= :% (first form))
                   (= 'n/Enclosed (second form))))]
    :print {:function true :item true :result true :summary true}}))


