(ns code.doc.prepare
  (:require [code.doc.collect.api :as collect.api]
            [code.doc.collect.base :as collect.base]
            [code.doc.collect.reference :as collect.reference]
            [code.doc.link.anchor :as link.anchor]
            [code.doc.link.api :as link.api]
            [code.doc.link.chapter :as link.chapter]
            [code.doc.link.manage :as link.manage]
            [code.doc.link.namespace :as link.namespace]
            [code.doc.link.number :as link.number]
            [code.doc.link.reference :as link.reference]
            [code.doc.link.stencil :as link.stencil]
            [code.doc.link.tag :as link.tag]
            [code.doc.link.test :as link.test]
            [code.doc.parse :as parse]
            [std.lib :as h]))

(defn prepare
  "processes a single meta to generate an interim structure
   (let [project (publish/make-project)
         lookup  (executive/all-pages project)]
     (sort (keys (prepare 'hara/index {} lookup project))))
   => '(:anchors :anchors-lu :articles :global :namespaces :project :references)"
  {:added "3.0"}
  ([key params lookup {:keys [root] :as project}]
   (let [{:keys [name input] :as meta} (lookup key)
         elements (if input (parse/parse-file input {:root root}) [])]
     (-> meta
         (assoc :project project)
         (assoc-in [:articles name :elements] elements)
         (assoc-in [:articles name :meta] meta)
         (collect.base/collect-global-metas name)
         (collect.base/collect-article-metas name)
         (collect.base/collect-namespaces name)
         (collect.reference/collect-references name)
         (collect.api/collect-apis name)
         (collect.base/collect-tags name)
         (collect.base/collect-citations name)
         (link.namespace/link-namespaces name)
         (link.reference/link-references name)
         (link.api/link-apis name)
         (link.chapter/link-chapters name)
         (link.test/link-tests name)
         (link.number/link-numbers name)
         (link.tag/link-tags name)
         (link.manage/link-manage name)
         (link.anchor/link-anchors-lu name)
         (link.anchor/link-anchors name)
         (link.stencil/link-stencil name)))))

(comment
  (def +output+
    (prepare 
     'core/std-lib-foundation
     {:print {:item true, :result true, :summary true},
      :return :summary,
      :title "PUBLISHING HTML FILES",
      :parallel true,
      :write true}
     +lookup+
     +project+))
  
  (keys +output+)
  (:references +output+))
