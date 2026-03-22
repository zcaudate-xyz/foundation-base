(ns kmi.redis.compile
  (:require [kmi.redis :as r]
            [std.lib.collection]
            [std.lib.foundation]
            [std.lib.walk]
            [std.string.case]
            [xt.lang.base-lib :as k])
  (:refer-clojure :exclude [compile]))

(defn parse-map
  "parses the :map type from a spec and key"
  {:added "3.0"}
  ([spec k]
   (let [rec (or (get (:fields spec) k)
                 (second (:element spec)))
         type (cond (set? rec) :enum
                    (map? rec) (or (:impl rec) (:type rec))
                    :else (or rec
                              (std.lib.foundation/error "Path not valid" {:path k})))
         _    (if (= type :key)
                (or (= (:impl spec) :key)
                    (std.lib.foundation/error "Can only nest within :keys")))]
     (merge {:type type} (if (map? rec) (select-keys rec [:name]))))))

(defn parse-coll
  "parses a collection (:list, :set, :stream) type"
  {:added "3.0"}
  ([spec k]
   {:type :entry}))

(defn parse-zset
  "parses a :zset type"
  {:added "3.0"}
  ([spec k]
   (second (:element spec))))

(defn compile-path
  "creates a typed path"
  {:added "3.0"}
  ([spec path]
   (let [{:keys [impl type]} spec
         impl (cond (= type :map)
                    (or impl (std.lib.foundation/error "No implementation" {:spec spec}))

                    :else type)]
     (if (empty? path)
       ()
       (let [k     (first path)
             kmap  (case type
                     :map  (if (vector? k)
                             {:type :multi}
                             (parse-map spec k))
                     (:stream :list) (parse-coll spec k)
                     :zset (parse-zset spec k))]
         (cons (assoc kmap :path k)
               (compile-path (or (get (:fields spec) k)
                                 (second (:element spec)))
                             (next path))))))))

(defn to:str
  "conserve symbols, other types to string"
  {:added "3.0"}
  [{:keys [path]}]
  (if (symbol? path)
    path
    (std.string.case/snake-case (std.lib.foundation/strn path))))

(defn to:array
  "creates a form for arrays"
  {:added "3.0"}
  [body]
  (let [forms (mapv (fn [form]
                      (if (vector? form)
                        (mapv (fn [s]
                                (if (symbol? s)
                                  s
                                  (std.lib.foundation/strn s)))
                              form)
                        `(k/to-flat ~form)))
                    body)]
    (if (= 1 (count forms))
      [(list 'unpack (first forms))]
      [(list 'unpack `(k/arr-mapcat ~(vec forms) k/identity))])))

(defn build-path
  "build path command"
  {:added "3.0"}
  [path parent]
  (let [kpath (->> (map to:str path)
                   (concat parent)
                   (vec))
        kform  (cond (= 1 (count kpath))
                     (first kpath)

                     (every? (comp not std.lib.collection/form?) kpath)
                     (apply list 'cat (interpose ":" kpath))
                     
                     :else
                     `(k/join ":" ~kpath))]
    kform))

(defn build-command
  "builds a redis command"
  {:added "3.0"}
  ([{:keys [wrap] :as cmd} path parent]
   (let [kform (build-path path parent)
         form   `(~'redis.call ~@(:prefix cmd) ~kform ~@(:suffix cmd))]
     (cond-> form wrap (wrap)))))

;;
;; GENERAL
;;


(defn run-command
  "command for run"
  {:added "3.0"}
  ([[e & rpath] parent body]
   (build-command {:prefix [(last body)] :suffix (vec (butlast body))}
                  (reverse (cons e rpath)) parent)))

(defn path-command
  "command for path"
  {:added "3.0"}
  ([path parent _]
   (build-path (reverse path) parent)))

;;
;; GET
;;

(defn get-all-command
  "creates a get all command"
  {:added "3.0"}
  ([e rpath parent]
   (let [cmd  (case (:type e)
                :hash   {:prefix ["HGETALL"] :suffix [] :wrap (fn [form] (list `r/flat-pairs-to-object form))}
                :set    {:prefix ["SMEMBERS"] :suffix []}
                :list   {:prefix ["LRANGE"] :suffix ["0" "-1"]}
                :zset   {:prefix ["ZRANGEBYSCORE"] :suffix ["-inf" "+inf" "WITHSCORES"]}
                :stream {:prefix ["XRANGE"] :suffix ["-" "+"]})]
     (build-command cmd (reverse (cons e rpath)) parent))))

(defn get-vals-command
  "command for set vals"
  {:added "3.0"}
  ([e rpath parent [keys]]
   (let [suffix  (if (vector? keys)
                   keys
                   [(list 'unpack keys)])
         cmd  (case (:type e)
                :hash   {:prefix ["HMGET"] :suffix suffix}
                :set    {:preifx ["SMISMEMBER"] :suffix suffix}
                :zset   {:prefix ["ZMSCORE"] :suffix suffix}
                :list   {:prefix ["LRANGE"] :suffix suffix}
                :stream {:prefix ["XRANGE"] :suffix suffix})]
     (build-command cmd (reverse (cons e rpath)) parent))))

(defn get-entry-command
  "command for get entry"
  {:added "3.0"}
  ([e rpath parent]
   (let [cmd (case (:type (first rpath))
               :hash   {:prefix ["HGET"] :suffix [(to:str e)]}
               :list   {:prefix ["LRANGE"] :suffix [(to:str e) (to:str e)]}
               :set    {:prefix ["SISMEMBER"] :suffix [(to:str e)]}
               :zset   {:prefix ["ZSCORE"] :suffix [(to:str e)]}
               :stream {:prefix ["XRANGE"] :suffix [(to:str e) "COUNT" "1"]})]
     (build-command cmd (reverse rpath) parent))))

(defn get-all-key
  "export all sub keys"
  {:added "3.0"}
  [e rpath parent]
  (let [path (build-path (reverse (cons e rpath)) parent)]
    `(k/arr-map (r/scan-level ~path) r/key-export)))

(defn get-vals-key
  "export input keys"
  {:added "3.0"}
  [e rpath parent body]
  (let [path (build-path (reverse (cons {:path '_k} (cons e rpath))) parent)]
    (list `k/arr-map (first body)
          (list 'fn '[_k]
                (list 'return (list `r/key-export path))))))

(defn get-command
  "command for get"
  {:added "3.0"}
  ([[e & rpath] parent body]
   (cond (#{:hash :list :zset :stream} (:type e))
         (if (empty? body)
           (get-all-command e rpath parent)
           (get-vals-command e rpath parent body))

         (#{:key} (:type e))
         (if (empty? body)
           (get-all-key e rpath parent)
           (get-vals-key e rpath parent body))

         :else
         (let [t (or (:type (first rpath)) :key)]
           (if (#{:key} t)
             (build-command {:prefix ["GET"] :suffix []} (reverse (cons e rpath)) parent)
             (get-entry-command e rpath parent))))))

;;
;; COUNT
;;

(defn len-command
  "command for length"
  {:added "3.0"}
  ([[e & rpath] parent _]
   (cond (= :key (:type e))
         (let [path (build-path (reverse (cons e rpath)) parent)]
           (list 'len (list `r/scan-level path)))

         :else
         (let [cmd  (case (:type e)
                      :hash   {:prefix ["HLEN"] :suffix []}
                      :list   {:prefix ["LLEN"] :suffix []}
                      :zset   {:prefix ["ZCOUNT"] :suffix ["-inf" "+inf"]}
                      :stream {:prefix ["XLEN"] :suffix []})]
           (build-command cmd (reverse (cons e rpath)) parent)))))

;;
;; KEYS
;;

(defn keys-command
  "command for keys"
  {:added "3.0"}
  ([[e & rpath] parent _]
   (cond (= :key (:type e))
         (let [path (build-path (reverse (cons e rpath)) parent)]
           (list `r/scan-sub path))

         :else
         (let [cmd  (case (:type e)
                      :hash   {:prefix ["HKEYS"] :suffix []}
                      :set    {:prefix ["SMEMBERS"] :suffix []}
                      :zset   {:prefix ["ZRANGEBYSCORE"] :suffix ["-inf" "+inf"]})]
           (build-command cmd (reverse (cons e rpath)) parent)))))

;;
;; HAS
;;

(defn has-command
  "command for has"
  {:added "3.0"}
  ([[e & rpath] parent _]
   (let [wrap-fn (fn [form] (list '== 1 form))]
     (cond (#{:hash :list :zset :stream} (:type e))
           (build-command {:prefix ["EXISTS"] :suffix []  :wrap wrap-fn}
                          (reverse (cons e rpath)) parent)

           :else
           (let [cmd (case (:type (first rpath))
                       :key    {:prefix ["EXISTS"] :suffix []  :wrap wrap-fn}
                       :hash   {:prefix ["HEXISTS"] :suffix [(to:str e)] :wrap wrap-fn}
                       :set    {:prefix ["SISMEMBER"] :suffix [(to:str e)] :wrap wrap-fn}
                       :zset   {:prefix ["ZSCORE"] :suffix [(to:str e)] :wrap (fn [form]
                                                                                (list 'not (list 'not form)))})]
             (build-command cmd (reverse rpath) parent))))))

;;
;; DEL
;;

(defn del-all-key
  "delete all for :key"
  {:added "3.0"}
  [e rpath parent]
  (let [path (build-path (reverse (cons e rpath)) parent)]
    (list 'redis.call "DEL" (list 'unpack (list `r/scan-level path)))))

(defn del-vals-key
  "delete for :key inputs"
  {:added "3.0"}
  [e rpath parent body]
  (let [path (build-path (reverse (cons {:path '_k} (cons e rpath))) parent)]
    (list 'redis.call "DEL" (list 'unpack
                                  (list `k/arr-map (first body)
                                        (list 'fn '[_k]
                                              (list 'return (list `r/key-export path))))))))

(defn del-vals-command
  "delete for data structures"
  {:added "3.0"}
  ([e rpath parent [keys]]
   (let [suffix  (if (vector? keys)
                   keys
                   [(list 'unpack keys)])
         cmd  (case (:type e)
                :hash   {:prefix ["HDEL"] :suffix suffix}
                :set    {:preifx ["SREM"] :suffix suffix}
                :zset   {:prefix ["ZREM"] :suffix suffix})]
     (build-command cmd (reverse (cons e rpath)) parent))))

(defn del-entry-command
  "deletes for single field values"
  {:added "3.0"}
  ([e rpath parent]
   (let [cmd (case (:type (first rpath))
               :hash   {:prefix ["HDEL"] :suffix [(to:str e)]}
               :set    {:prefix ["SREM"] :suffix [(to:str e)]}
               :zset   {:prefix ["ZREM"] :suffix [(to:str e)]})]
     (build-command cmd (reverse rpath) parent))))

(defn del-command
  "command for del"
  {:added "3.0"}
  ([[e & rpath] parent body]
   (cond (#{:hash :list :zset :stream} (:type e))
         (if (empty? body)
           (build-command {:prefix ["DEL"] :suffix []} (reverse (cons e rpath)) parent)
           (del-vals-command e rpath parent body))

         (#{:key} (:type e))
         (if (empty? body)
           (del-all-key e rpath parent)
           (del-vals-key e rpath parent body))

         :else
         (let [t (or (:type (first rpath)) :key)]
           (if (#{:key} t)
             (build-command {:prefix ["DEL"] :suffix []} (reverse (cons e rpath)) parent)
             (del-entry-command e rpath parent))))))

;;
;; SET
;;


(defn set-vals-command
  "command for set vals"
  {:added "3.0"}
  ([e rpath parent body]
   (let [cmd  (case (:type e)
                :hash   {:prefix ["HMSET"] :suffix (to:array body)}
                :set    {:prefix ["SADD"] :suffix body}
                :zset   {:prefix ["ZADD"] :suffix (vec (mapcat reverse body))})]
     (build-command cmd (reverse (cons e rpath)) parent))))

(defn set-entry-command
  "command for set entry"
  {:added "3.0"}
  ([e rpath parent body]
   (let [cmd (case (:type (first rpath))
               :hash   {:prefix ["HSET"] :suffix [(to:str e) (first body)]}
               :set    {:prefix ["SADD"] :suffix [(to:str e) (first body)]}
               :zset   {:prefix ["ZADD"] :suffix [(first body) (to:str e)]})]
     (build-command cmd (reverse rpath) parent))))

(defn set-command
  "command for set"
  {:added "3.0"}
  ([[e & rpath] parent body]
   (cond (#{:hash :list :zset :stream} (:type e))
         (set-vals-command e rpath parent body)

         :else
         (let [t (or (:type (first rpath)) :key)]
           (if (#{:key} t)
             (build-command {:prefix ["SET"] :suffix body} (reverse (cons e rpath)) parent)
             (set-entry-command e rpath parent body))))))

(defn incr-command
  "command for incr"
  {:added "3.0"}
  ([[e & rpath] parent [body]]
   (let [k (if (empty? rpath)
             :key
             (:type (first rpath)))
         wrap-fn (fn [form] (list 'tonumber form))]
     (cond (= :integer (:type e))
           (case k
             :key   (build-command
                     (if body
                       {:prefix ["INCRBY"] :suffix [body] :wrap wrap-fn}
                       {:prefix ["INCR"] :suffix [] :wrap wrap-fn})
                     (reverse (cons e rpath)) parent)

             :hash  (build-command
                     (if body
                       {:prefix ["HINCRBY"] :suffix [(to:str e) body]  :wrap wrap-fn}
                       {:prefix ["HINCR"] :suffix [(to:str e)] :wrap wrap-fn})
                     (reverse rpath) parent))

           :else
           (std.lib.foundation/error "Need to be an Integer")))))

(defn decr-command
  "command for decr"
  {:added "3.0"}
  ([[e & rpath] parent [body]]
   (let [k (if (empty? rpath)
             :key
             (:type (first rpath)))
         wrap-fn (fn [form] (list 'tonumber form))]
     (cond (= :integer (:type e))
           (case k
             :key   (build-command
                     (if body
                       {:prefix ["DECBY"] :suffix [body] :wrap wrap-fn}
                       {:prefix ["DECR"] :suffix [] :wrap wrap-fn})
                     (reverse (cons e rpath)) parent)

             :hash  (build-command
                     (if body
                       {:prefix ["HINCRBY"] :suffix [(to:str e) (list '- body)] :wrap wrap-fn}
                       {:prefix ["HINCRBY"] :suffix [(to:str e) -1] :wrap wrap-fn})
                     (reverse rpath) parent))

           :else
           (std.lib.foundation/error "Need to be an Integer")))))

;;
;; ADD
;;

(defn add-command
  "command for add
 
   (compile type/<SPEC>
            '[:ADD [\"test\"] [:events :_:error] {:id \"A\"}])
   => '(redis.call \"XADD\" (cat \"test\" \":\" \"events\" \":\" \"_:error\") \"*\" \"id\" \"A\")"
  {:added "4.0"}
  ([[e & rpath] parent [entry]]
   (let [cmd  (case (:type e)
                :list   {:prefix ["RPUSH"] :suffix [(list 'cjson.encode entry)]}
                :stream (let [entries (cond (map? entry)
                                            (mapcat (fn [[k v]] [(std.lib.foundation/strn k) v]) entry)
                                            
                                            :else (std.lib.foundation/error "Not Supported"))]
                          {:prefix ["XADD"] :suffix (cons "*" entries)}))]
     (build-command cmd (reverse (cons e rpath)) parent))))


;;
;; COMPILE
;;

(defn compile-cmd
  "compiles the command"
  {:added "3.0"}
  ([spec f body]
   (let [parent (std.lib.collection/seqify (first body))
         path   (second body)
         body   (drop 2 body)]
     (-> (compile-path spec path)
         (reverse)
         (f parent body)))))

(defn compile
  "compiles a body with spec"
  {:added "3.0"}
  ([spec & statements]
   (let [body (reduce (fn [acc [tag & body]]
                        (case tag
                          :DO     (apply list 'do (map (partial compile spec) body))
                          :ARR    (mapv (partial compile spec) body)
                          :RUN    (let [body (if (empty? body)
                                              (vec body)
                                              (conj (vec (drop 1 body))
                                                    (first body)))]
                                  (compile-cmd spec run-command body))
                          :ADD    (compile-cmd spec add-command body)
                          :PATH   (compile-cmd spec path-command body)
                          :GET    (compile-cmd spec get-command body)
                          :SET    (compile-cmd spec set-command body)
                          :KEYS   (compile-cmd spec keys-command body)
                          :LEN    (compile-cmd spec len-command body)
                          :DEL    (compile-cmd spec del-command body)
                          :HAS    (compile-cmd spec has-command body)
                          :INCR   (compile-cmd spec incr-command body)
                          :DECR   (compile-cmd spec decr-command body)
                          :EXP    (std.lib.walk/postwalk-replace {'% acc} (first body))
                          :RET    (if (empty? body)
                                    (list 'return acc)
                                    (list 'return (std.lib.walk/postwalk-replace {'% acc} (first body))))))
                      nil
                      statements)]
     body)))
