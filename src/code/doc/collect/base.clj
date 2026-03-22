(ns code.doc.collect.base
  (:require [std.lib.collection]
            [std.print :as print]))

(defn collect-namespaces
  "combines `:ns-form` directives into a namespace map for easy referral
 
   (collect-namespaces
    {:articles
     {\"example\"
      {:elements [{:type :ns-form
                   :ns    'clojure.core}]}}}
    \"example\")
   => '{:articles {\"example\" {:elements () :meta {}}}
        :namespaces {clojure.core {:type :ns-form :ns clojure.core}}}"
  {:added "3.0"}
  ([{:keys [articles] :as interim} name]
   (let [all    (->> (get-in articles [name :elements])
                     (filter #(-> % :type (= :ns-form))))
         meta   (-> all first :meta)
         namespaces (std.lib.collection/map-juxt [:ns identity] all)]
     (-> interim
         (update-in [:articles name :meta] (fnil std.lib.collection/merge-nested {}) meta)
         (update-in [:namespaces] (fnil std.lib.collection/merge-nested {}) namespaces)
         (update-in [:articles name :elements]
                    (fn [elements] (filter #(-> % :type (not= :ns-form)) elements)))))))

(defn collect-article-metas
  "shunts `:article` directives into a separate `:meta` section
 
   (collect-article-metas
    {:articles {\"example\" {:elements [{:type :article
                                       :options {:color :light}}]}}}
    \"example\")
   => '{:articles {\"example\" {:elements []
                              :meta {:options {:color :light}}}}}"
  {:added "3.0"}
  ([{:keys [articles] :as interim} name]
   (let [articles (->> (get-in articles [name :elements])
                       (filter #(-> % :type (= :article)))
                       (apply std.lib.collection/merge-nested {}))]
     (-> interim
         (update-in [:articles name :meta] (fnil std.lib.collection/merge-nested {}) (dissoc articles :type))
         (update-in [:articles name :elements]
                    (fn [elements] (filter #(-> % :type (not= :article)) elements)))))))

(defn collect-global-metas
  "shunts `:global` directives into a globally available `:meta` section
 
   (collect-global-metas
    {:articles {\"example\" {:elements [{:type :global
                                       :options {:color :light}}]}}}
    \"example\")
   => {:articles {\"example\" {:elements ()}}
       :global {:options {:color :light}}}"
  {:added "3.0"}
  ([{:keys [articles] :as interim} name]
   (let [global (->> (get-in articles [name :elements])
                     (filter #(-> % :type (= :global)))
                     (apply std.lib.collection/merge-nested {}))]
     (-> interim
         (update-in [:global] (fnil std.lib.collection/merge-nested {}) (dissoc global :type))
         (update-in [:articles name :elements]
                    (fn [elements] (filter #(-> % :type (not= :global)) elements)))))))

(defn collect-tags
  "puts any element with `:tag` attribute into a separate `:tag` set
 
   (collect-tags
    {:articles {\"example\" {:elements [{:type :chapter :tag  \"hello\"}
                                      {:type :chapter :tag  \"world\"}]}}}
    \"example\")
   => {:articles {\"example\" {:elements [{:type :chapter :tag \"hello\"}
                                        {:type :chapter :tag \"world\"}]
                             :tags #{\"hello\" \"world\"}}}}"
  {:added "3.0"}
  ([{:keys [articles] :as interim} name]
   (->> (get-in articles [name :elements])
        (reduce (fn [m {:keys [tag] :as ele}]
                  (cond (nil? tag) m

                        (get m tag)
                        (do (print/println "There is already an existing tag for" ele)
                            m)
                        :else (conj m tag)))
                #{})
        (assoc-in interim [:articles name :tags]))))

(defn collect-citations
  "shunts `:citation` directives into a separate `:citation` section
 
   (collect-citations
    {:articles {\"example\" {:elements [{:type :citation :author \"Chris\"}]}}}
    \"example\")
   => {:articles {\"example\" {:elements [],
                             :citations [{:type :citation, :author \"Chris\"}]}}}"
  {:added "3.0"}
  ([{:keys [articles] :as interim} name]
   (let [citations (->> (get-in articles [name :elements])
                        (filter #(-> % :type (= :citation))))]
     (-> interim
         (assoc-in  [:articles name :citations] citations)
         (update-in [:articles name :elements]
                    (fn [elements] (filter #(-> % :type (not= :citation)) elements)))))))
