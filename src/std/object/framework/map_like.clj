(ns std.object.framework.map-like
  (:require [std.lib.collection]
            [std.lib.memoize]
            [std.object.framework.access :as access]
            [std.object.framework.print :as print]
            [std.object.framework.read :as read]
            [std.object.framework.write :as write]
            [std.protocol.object :as protocol.object]))

(defn key-selection
  "selects map based on keys
 
   (key-selection {:a 1 :b 2} [:a] nil)
   => {:a 1}
 
   (key-selection {:a 1 :b 2} nil [:a])
   => {:b 2}"
  {:added "3.0"}
  ([m include exclude]
   (cond-> m
     include (select-keys include)
     exclude (#(apply dissoc % exclude)))))

(defn read-proxy-functions
  "creates a proxy access through a field in the object
 
   (read-proxy-functions {:school [:name :raw]})"
  {:added "3.0"}
  ([proxy]
   (reduce-kv (fn [out accessor ks]
                (reduce (fn [out k]
                          (assoc out k `{:type Object
                                         :fn   (fn [~'obj]
                                                 (let [~'proxy (access/get ~'obj ~accessor)]
                                                   (access/get ~'proxy ~k)))}))
                        out
                        ks))
              {}
              proxy)))

(defn write-proxy-functions
  "creates a proxy access through a field in the object
 
   (write-proxy-functions {:school [:name :raw]})"
  {:added "3.0"}
  ([proxy]
   (reduce-kv (fn [out accessor ks]
                (reduce (fn [out k]
                          (assoc out k `{:type Object
                                         :fn (fn [~'obj ~'v]
                                               (let [~'proxy (access/get ~'obj ~accessor)]
                                                 (access/set ~'proxy ~k ~'v)))}))
                        out
                        ks))
              {}
              proxy)))

(defn extend-map-read
  "creates the forms for read methods
 
   (extend-map-read 'test.Cat {:read :fields})"
  {:added "3.0"}
  ([cls {:keys [read include exclude]}]
   (let [methods (:methods read)]
     (cond (and (map? read)
                (not (keyword? methods))
                (not (list? methods)))
           (update-in read [:methods]
                      #(list 'merge %
                             `(-> (merge (read/read-all-getters ~cls read/+read-get-opts+)
                                         (read/read-all-getters ~cls read/+read-is-opts+)
                                         (read/read-all-getters ~cls read/+read-has-opts+))
                                  (key-selection (or ~include []) ~exclude))))

           (or (= read :fields)
               (= methods :fields))
           `{:methods (key-selection (read/read-fields ~cls) ~include ~exclude)}

           (or (= read :all-fields)
               (= methods :all-fields))
           `{:methods (key-selection (read/read-all-fields ~cls) ~include ~exclude)}

           (or (= read :all)
               (= methods :all))
           `{:methods (-> (merge (read/read-all-getters ~cls read/+read-get-opts+)
                                 (read/read-all-getters ~cls read/+read-is-opts+)
                                 (read/read-all-getters ~cls read/+read-has-opts+))
                          (key-selection ~include ~exclude))}

           (or (nil? read)
               (= read :class)
               (= methods :class))
           `{:methods (-> (merge (read/read-getters ~cls read/+read-get-opts+)
                                 (read/read-getters ~cls read/+read-is-opts+)
                                 (read/read-getters ~cls read/+read-has-opts+))
                          (key-selection ~include ~exclude))}

           :else
           {:methods methods}))))

(defn extend-map-write
  "creates the forms for write methods
 
   (extend-map-write 'test.Cat {:write {:methods :class}})
   => '{:methods (std.object.framework.write/write-setters test.Cat)}"
  {:added "3.0"}
  ([cls {:keys [write]}]
   (let [construct  (:construct write)
         construct  (cond (and (map? construct)
                               (= :reflect (:fn construct)))
                          (assoc construct :fn `(write/write-constructor ~cls ~(:params construct)))

                          :else
                          construct)
         empty    (:empty write)
         empty    (if (= :reflect empty)
                    `(write/write-constructor ~cls []))
         methods  (:methods write)
         write    (cond-> write
                    construct (assoc :construct construct)
                    empty     (assoc :empty empty)

                    (= methods :fields)
                    (assoc :methods `(write/write-fields ~cls))

                    (= methods :all-fields)
                    (assoc :methods `(write/write-all-fields ~cls))

                    (= methods :all)
                    (assoc :methods `(write/write-all-setters ~cls))

                    (= methods :class)
                    (assoc :methods `(write/write-setters ~cls)))]
     write)))

(defmacro extend-map-like
  "creates an entry for map-like classes
 
   (extend-map-like test.DogBuilder
                    {:tag \"build.dog\"
                     :write {:empty (fn [] (test.DogBuilder.))
                            :methods :class}
                     :read :fields})"
  {:added "3.0"}
  ([^Class cls {:keys [read write proxy] :as opts}]
   `[(defmethod protocol.object/-meta-read ~cls
       ([~'_]
        ~(let [custom (:custom read)
               read (extend-map-read cls opts)
               read (print/assoc-print-vars read opts)
               read (update-in read [:methods] #(list 'merge % (read-proxy-functions proxy)))
               read (if custom
                      `(update-in ~read [:methods] std.lib.collection/merge-nested ~custom)
                      read)]
           read)))

     ~(when (and write (map? write))
        (assert (some write [:from-map :from-custom :empty :construct])
                "The :write entry requires a sub-entry for either :from-map, :from-custom, :construct or :empty")
        `(defmethod protocol.object/-meta-write ~cls
           ([~'_]
            ~(let [write (extend-map-write cls opts)
                   write (update-in write [:methods] #(list 'merge % (write-proxy-functions proxy)))
                   write (if-let [custom (:custom write)]
                           `(update-in ~(dissoc write :custom) [:methods] std.lib.collection/merge-nested ~custom)
                           write)]
               write))))

     (do (std.lib.memoize/memoize:remove read/meta-read-exact ~cls)
         (std.lib.memoize/memoize:remove write/meta-write-exact ~cls)
         (print/extend-print ~cls))]))
