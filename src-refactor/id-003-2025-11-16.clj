(ns refactor.id-001-2025-12-05)

(comment


  ;; Get rid of the (def.<> MODULE (!:module)) form
  (do (code.manage/refactor-code
       '[js]
       {:print {:function true}
        :write true
        :edits [(fn [nav]
                  (code.query/modify
                   nav
                   [(fn [form]
                      (= form '(def.js MODULE (!:module))))]
                   (fn [nav]
                     (code.edit/delete nav))))]})
      
      
      (code.manage/refactor-code
       '[xt]
       {:print {:function true}
        :write true
        :edits [(fn [nav]
                  (code.query/modify
                   nav
                   [(fn [form]
                      (= form '(def.xt MODULE (!:module))))]
                   (fn [nav]
                     (code.edit/delete nav))))]})
      
      (code.manage/refactor-code
       '[lua]
       {:print {:function true}
        :write true
        :edits [(fn [nav]
                  (code.query/modify
                   nav
                   [(fn [form]
                      (= form '(def.lua MODULE (!:module))))]
                   (fn [nav]
                     (code.edit/delete nav))))]}))


  ;; Get rid of the :export [MODULE] entry
  (code.manage/refactor-code
   '[lua js xt]
   {:print {:function true}
    :write true
    :edits [(fn [nav]
              (code.query/modify
               nav
               [(fn [form]
                  (= form '[MODULE]))]
               (fn [nav]
                 (-> nav
                     (code.edit/delete)
                     (code.edit/delete-left)
                     (code.edit/delete-spaces-left)))))]})

  ;; Get rid of the :macro-only true entry
  (code.manage/refactor-code
   '[js lua xt python]
   {:print {:function true}
    :write true
    :edits [(fn [nav]
              (code.query/modify
               nav
               [#_(fn [form]
                  (= (first form) 'l/script))
                (fn [form]
                  (= :macro-only form))]
               (fn [nav]
                 (-> nav
                     (code.edit/delete)
                     (code.edit/delete)
                     (code.edit/delete-spaces-right)))))]})
  
  ;; Rewrite the bundle tag
  (code.manage/refactor-code
   '[js lua xt python]
   {:print {:function true}
    ;;:write true
    :edits [(fn [nav]
              (code.query/modify
               nav
               [(fn [form]
                  (and (map? form)
                       (map? (:bundle form))))]
               (fn [nav]
                 (let [form (code.edit/value nav)
                       {:keys [bundle
                               import]} form
                       bimports (mapcat identity (vals bundle))]
                   (-> nav
                       (code.edit/replace
                        (std.block/layout
                         (-> form
                             (dissoc :bundle)
                             (assoc :import
                                    (with-meta
                                      (vec (concat import
                                                   bimports))
                                      {:spec {:columns 1}}))))))))))]})

  (code.manage/refactor-code
   '[lua.nginx.ws-client]
   {:print {:function true}
    ;;:write true
    :edits [(fn [nav]
              (code.query/modify
               nav
               [(fn [form]
                  (and (map? form)
                       (map? (:bundle form))))]
               (fn [nav]
                 (let [form (code.edit/value nav)
                       {:keys [bundle
                               import]} form
                       bimports (mapcat identity (vals bundle))]
                   (-> nav
                       (code.edit/replace
                        (std.block/layout
                         (-> form
                             (dissoc :bundle)
                             (assoc :import
                                    (with-meta
                                      (vec (concat import
                                                   bimports))
                                      {:spec {:columns 1}}))))))))))]})
  )


