(ns refactor.id-001-2022-12-05)

(comment

  (code.manage/refactor-test
   '[js]
   {:print {:function true}
    :write true
    :edits [(fn [nav]
              (code.query/modify
               nav
               [(fn [form]
                  (and (vector? form)
                       (= :% (first form))
                       (= 'n/Enclosed (second form))))]
               (fn [nav]
                 (let [forms  (->> nav
                                   (std.block.navigate/children)
                                   (filter (comp not std.block.type/void-block?))
                                   (drop 2))]
                   (-> nav
                       (std.block.navigate/replace '())
                       (std.block.navigate/down)
                       (std.block.navigate/insert 'n/EnclosedCode)
                       (h/-> (reduce (fn [nav elem]
                                       (-> nav
                                           (std.block.navigate/insert elem)
                                           (std.block.navigate/insert-newline)))
                                     %
                                     forms))
                       (std.block.navigate/up))))))]})
  
  (code.manage/locate-test
   :all
   {:query [(fn [form]
              (and (vector? form)
                   (= :% (first form))
                   (= 'n/Enclosed (second form))))]
    :print {:function true :item true :result true :summary true}}))


