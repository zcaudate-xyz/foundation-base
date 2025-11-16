(ns refactor.id-001-2025-12-05
  (:require [std.lib :as h]
            [code.edit :as e]))

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

  


  ;; This puts the :bundle map into the :import directory
  (code.manage/refactor-code
   '[js lua xt python]
   {:print {:function true}
    :write true
    :edits [(fn [nav]
              (code.query/modify
               nav
               [(fn [form]
                  (and (map? form)
                       (map? (:bundle form))))]
               
               (fn [nav]
                 (let [form (code.edit/value nav)
                       {:keys [bundle
                               require
                               import]} form
                       bimports (mapcat identity (vals bundle))]
                   (-> nav
                       (code.edit/replace
                        (std.block/layout
                         (cond-> form
                           :then (dissoc :bundle)
                           :then (assoc :import
                                        (with-meta
                                          (vec
                                           (map (fn [x]
                                                  (with-meta x
                                                    {:tag :vector
                                                     :readable-len 100}))
                                                (concat import
                                                        bimports)))
                                          {:spec {:columns 1}}))
                           require (assoc :require
                                          (with-meta
                                            (vec
                                             (map (fn [x]
                                                    (with-meta x
                                                      {:tag :vector
                                                       :readable-len 100}))
                                                  require))
                                            {:spec {:columns 1}})))
                         {:indents (second (:position nav))})))))))]})

  ;; This gets rid of all the :require [... :include [:fn]] syntax
  (code.manage/refactor-code
   '[lua xt python]
   {:print {:function true}
    :write true
    :edits [(fn [nav]
              (code.query/modify
               nav
               ['l/script
                '|
                map?
                #_(fn [form]
                  (and (map? form)
                       (:require form)))]
               (fn [nav]
                 (let [cnav    (h/suppress (-> nav
                                               (code.edit/find-next-token :require)
                                               (code.edit/right)))
                       require (if cnav (code.edit/value cnav))]
                   (if (not require)
                     nav
                     (code.edit/replace
                      cnav
                      (std.block/layout
                       (with-meta
                         (vec
                          (map (fn [x]
                                 (with-meta
                                   (vec (->> (dissoc (apply hash-map (rest x))
                                                     :include
                                                     :supress)
                                             
                                             (mapcat identity)
                                             (cons (first x))))
                                   {:tag :vector
                                    :readable-len 100}))
                               require))
                         {:tag :vector
                          :spec {:columns 1}})
                       {:indents (second (:position cnav))})))))))]})


  ;; This standardizes the import statements
  (code.manage/refactor-code
   '[js lua python]
   {:print {:function true}
    :write true
    :edits [(fn [nav]
              (code.query/modify
               nav
               ['l/script
                map?
                '|
                (fn [form]
                  (= form :import))]
               (fn [nav]
                 (let [rnav (e/right nav)
                       v     (e/value rnav)]
                   (if (not (vector? (first v)))
                     (-> rnav
                         (e/replace [(e/right-expression rnav)]))
                     nav)))))]})
  
  )


