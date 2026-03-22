(ns code.framework.cache
  (:require [code.framework.common :as common]
            [std.block :as block]
            [std.fs :as fs]
            [std.lib.atom]
            [std.lib.collection])
  (:refer-clojure :exclude [update]))

(defonce +cache-dir+ ".hara/code/framework")

(defonce +cache-suffix+ ".cache")

(defonce ^:dynamic *memory-loaded* (atom nil))

(defonce ^:dynamic *memory* (atom {}))

(declare init-memory!)

(defn prepare-out
  "prepares code block for output
 
   (prepare-out
    {'ns {'var
          {:test {:code [(block/block '(1 2 3))]}}}})
   => '{ns {var {:test {:code \"(1 2 3)\"}}}}"
  {:added "3.0"}
  ([data]
   (std.lib.collection/map-vals
    (fn [ns-entry]
      (std.lib.collection/map-vals
       (fn [entry]
         (if (-> entry :test :code)
           (update-in entry
                      [:test :code]
                      (comp block/string block/root))
           entry))
       ns-entry))
    data)))

(defn prepare-in
  "prepares input string to code blocks
 
   (-> (prepare-in
        '{ns {var
              {:test {:code \"(+ 1 2 3)\"}}}})
       (get-in '[ns var :test :code])
       (first)
       (block/value))
   => '(+ 1 2 3)"
  {:added "3.0"}
  ([data]
   (std.lib.collection/map-vals
    (fn [ns-entry]
      (std.lib.collection/map-vals
       (fn [entry]
         (if (-> entry :test :code)
           (update-in entry
                      [:test :code]
                      (comp vec block/children block/parse-root))
           entry))
       ns-entry))
    data)))

(defn file-modified?
  "checks if code file has been modified from the cache
 
   (file-modified? 'code.manage
                   \"src/code/manage.clj\")
   => boolean?"
  {:added "3.0"}
  ([ns file-path]
   (file-modified? ns file-path *memory* +cache-dir+))
  ([ns file-path current cache-dir]
   (if (and file-path (fs/exists? file-path))
     (let [file-modified (fs/last-modified file-path)
           entry (get @current ns)
           changed (not= file-modified
                         (:file-modified entry))]
       changed))))

(defn fetch
  "reads a the file or gets it from cache
 
   (fetch 'code.manage
          \"src/code/manage.clj\")
   => (any map? nil?)"
  {:added "3.0"}
  ([ns file-path]
   (fetch ns file-path *memory* +cache-dir+))
  ([ns file-path current cache-dir]
   (if-not @*memory-loaded* (init-memory!))
   (let [entry (get @current ns)]
     (if (and entry
              (not (file-modified? ns file-path current cache-dir)))
       (:data entry)))))

(defn update
  "updates values to the cache"
  {:added "3.0"}
  ([ns file-path data]
   (update ns file-path data *memory* +cache-dir+ +cache-suffix+))
  ([ns file-path data current cache-dir cache-suffix]
   (if (and ns
            (file-modified? ns file-path current cache-dir))
     (let [file-modified (fs/last-modified file-path)
           cache-path (fs/path cache-dir (str ns cache-suffix))
           _  (spit cache-path (assoc (prepare-out data)
                                      :meta {:file-modified file-modified
                                             :namespace ns}))
           cache-modified (fs/last-modified cache-path)]
       (swap! current assoc ns {:cache-modified cache-modified
                                :file-modified file-modified
                                :data data})))))

(defn init-memory!
  "initialises memory with cache files"
  {:added "3.0"}
  ([] (init-memory! *memory* +cache-dir+))
  ([current cache-dir]
   (if (not (fs/exists? cache-dir))
     (fs/create-directory cache-dir))
   (let [files  (fs/select cache-dir {:include [fs/file?]})
         [run? output] (std.lib.atom/swap-return!
                        *memory-loaded*
                        (fn [v] (if (nil? v)
                                  [true (promise)]
                                  [false v]))
                        true)]

     (if run?
       (->> (pmap (fn [file]
                    (let [{:keys [meta] :as data}  (try (read-string (slurp file))
                                                        (catch Throwable t))
                          data  (if data
                                  (common/entry (prepare-in (dissoc data :meta))))
                          {:keys [file-modified namespace]} meta
                          cache-modified (fs/last-modified file)]
                      (when data
                        (swap! current assoc namespace {:cache-modified cache-modified
                                                        :file-modified file-modified
                                                        :data data})
                        namespace)))
                  files)
            (vec)
            (deliver output)))
     @output)))

(defn purge
  "clears the cache"
  {:added "3.0"}
  ([]
   (purge *memory*))
  ([current]
   (reset! *memory* {})
   (reset! *memory-loaded* nil)))
