(ns refactor.id-001-2025-12-05
  (:require [std.lib :as h]
            [std.block :as b]
            [std.block.navigate :as e]
            [code.manage :as manage]
            [code.query :as q]))

(comment

  (manage/refactor-code
   '[indigo.client.app]
   {:print {:function true}
    :write true
    :edits [(fn [nav]
              (q/modify
               nav
               [(fn [form]
                  (= '[js.react.dnd :as dnd] form))]
               (fn [nav]
                 (e/replace
                  nav
                  '[js.lib.react-dnd :as dnd]))))]})
  
  
  ;; replace != with not=
  (manage/refactor-code
   '[indigo.client.app]
   {:print {:function true}
    :write true
    :edits [(fn [nav]
              (q/modify
               nav
               [(fn [form]
                  (= '!= form))]
               (fn [nav]
                 (e/replace
                  nav
                  'not=))))]})
  
  (manage/refactor-code
   '[indigo.client.app]
   {:print {:function true}
    :write true
    :edits [(fn [nav]
              (q/modify
               nav
               [(fn [form]
                  (= '!== form))]
               (fn [nav]
                 (e/replace
                  nav
                  'not==))))]})
  

  ;; exclude do blocks
  (manage/refactor-code
   '[indigo.client.app]
   {:print {:function true}
    :write true
    :edits [(fn [nav]
              (q/modify
               nav
               [(fn [form]
                  (and (list? form)
                       (= 'do (first form))
                       (= 2 (count form))))]
               (fn [nav]
                 (e/replace
                  nav
                  (b/layout (second (e/value nav))
                            {:indent (second (:position nav))})))))]}))
  
