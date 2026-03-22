(ns lib.lucene
  (:require [lib.lucene.impl :as impl]
            [lib.lucene.protocol :as protocol.search]
            [std.lib.component :as component]
            [std.protocol.component :as protocol.component]))

(defrecord LuceneSearch [type instance]

  Object
  (toString [engine]
    (str "#search.lucene" (-> (into {} engine)
                              (dissoc :instance)
                              (update :template keys))))

  protocol.component/IComponent
  (-start [engine]
    (impl/start-lucene engine))
  (-stop [engine]
    (impl/stop-lucene engine))

  protocol.search/IEngine
  (-index-add    [engine index entry opts]
    (impl/index-add-lucene engine index entry opts))
  (-index-remove [engine index terms opts]
    (impl/index-remove-lucene engine index terms opts))
  (-index-update [engine index terms entry opts]
    (impl/index-update-lucene engine index terms entry opts))
  (-search [engine index terms opts]
    (impl/search-lucene engine index terms opts)))

(defmethod print-method LuceneSearch
  ([v ^java.io.Writer w]
   (.write w (str v))))

(defmethod protocol.search/-create :lucene
  ([m]
   (map->LuceneSearch (assoc m :instance (atom {})))))

(defn lucene
  "constructs a lucene engine
 
   (lucene {:store :memory
            :template {:album {:analyzer {:type :standard}
                               :type  {:id {:stored false}}}}})
   => lib.lucene.LuceneSearch"
  {:added "3.0"}
  ([m]
   (-> (protocol.search/-create (assoc m :type :lucene))
       (component/start))))

(comment

  (index-add engine :topic {:id   "hello"
                            :code "stuff"}
             {})

  (index-update engine
                {:topic {:id   "hello"}}
                {:topic {:id   "hello"}}
                {})

  ;;:construct (fn [m])

  (def -search- (create {:type :lucenea :refresh false
                         :store :disk
                         :root  "search/root"
                         :template {:topic    {:analyzer {:type :standard}}
                                    :prospect {:type {:id {:tokenized true
                                                           :stored true
                                                           :index #{:doc :freq}}}
                                               :analyzer {:type :field
                                                          :default :standard
                                                          :fields {}}}}}))

  (def -search- (create-index! {:type :disk
                                :path "search/test-01"
                                :analyzer (analyzers/standard-analyzer)}))

  (index! -search- {:title-field "This is a title"
                    :abstract-field "This is an abstract of what is to follow"
                    :author-field "Lekhak Sampaadak"
                    :body-field "And here's the crux of the article with all the gory details"}
          {})

  (search -search- {:title-field ".*"})

  (defonce default-analyzer (analyzers/standard-analyzer))

  (analyzers/simple-analyzer))

