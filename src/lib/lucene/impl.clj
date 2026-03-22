(ns lib.lucene.impl
  (:require [lib.lucene.impl.analyzer :as analyzer]
            [lib.lucene.impl.document :as doc]
            [lib.lucene.impl.index :as index]
            [lib.lucene.protocol :as protocol.search]
            [std.fs :as fs]))

(defn create-directories
  "create multiple lucene directories"
  {:added "3.0"}
  ([{:keys [store root template refresh] :as engine}]
   (let [ks (keys template)]
     (reduce (fn [out k]
               (let [path (str root "/" (name k))
                     _    (if refresh (fs/delete path))
                     directory (index/directory {:store store
                                                 :path path})]
                 (assoc out k directory)))
             {}
             ks))))

(defn create-analyzers
  "creates multiple analyzers"
  {:added "3.0"}
  ([{:keys [template] :as engine}]
   (let [ks (keys template)]
     (reduce (fn [out k]
               (let [analyser (analyzer/analyzer (get-in template [k :analyzer]))]
                 (assoc out k analyser)))
             {}
             ks))))

(defn start-lucene
  "starts the lucene engine"
  {:added "3.0"}
  ([{:keys [instance] :as engine}]
   (swap! instance assoc
          :directories (create-directories engine)
          :analyzers   (create-analyzers engine)) engine))

(defn stop-lucene
  "stops the lucene engine"
  {:added "3.0"}
  ([{:keys [instance] :as engine}]
   (let [{:keys [directories]} @instance]
     (doseq [[k directory] directories]
       (index/close directory))
     (reset! instance nil)
     engine)))

(defn get-index
  "gets a particular index"
  {:added "3.0"}
  ([{:keys [instance]} index]
   (let [directory (get-in @instance [:directories index])
         analyzer  (get-in @instance [:analyzers index])]
     (if (and directory analyzer)
       [directory analyzer]
       (throw (ex-info "Index not found" {:status :error
                                          :tag :index/not-found
                                          :data {:index index}}))))))

(defn index-add-lucene
  "adds an entry to the index"
  {:added "3.0"}
  ([{:keys [template] :as engine} index entry opts]
   (let [[directory analyzer] (get-index engine index)]
     (index/add-entry directory analyzer (get-in template [index :type]) entry opts))))

(defn index-update-lucene
  "updates an entry in the index"
  {:added "3.0"}
  ([{:keys [template] :as engine} index terms entry opts]
   (let [[directory analyzer] (get-index engine index)]
     (index/update-entry directory analyzer (get-in template [index :type]) terms entry opts))))

(defn index-remove-lucene
  "removes an entry from the index"
  {:added "3.0"}
  ([engine index terms opts]
   (let [[directory analyzer] (get-index engine index)]
     (index/remove-entry directory analyzer terms opts))))

(defn search-lucene
  "searches lucene"
  {:added "3.0"}
  ([engine index terms opts]
   (let [[directory analyzer] (get-index engine index)]
     (index/search directory analyzer terms opts))))

