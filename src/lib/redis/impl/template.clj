(ns lib.redis.impl.template
  (:require [std.concurrent :as cc]
            [std.lib.collection :as collection]
            [std.lib.env :as env]
            [std.lib.foundation :as f]))

(defn redis-pipeline
  "constructs a pipeline for `opts`"
  {:added "3.0"}
  ([return {:keys [post pre chain raw string namespace]}]
   (let [post (if (and (symbol? return)
                       (not raw))
                (vec (cons `(fn [~'data] (~return ~'data ~'opts)) post))
                post)]
     (cond-> nil
       pre       (assoc :pre pre)
       post      (assoc :post post)
       chain     (assoc :chain chain)
       namespace (assoc :namespace namespace)
       (not (nil? string)) (assoc :string string)
       (= return :data) (assoc :deserialize true)
       (or (symbol? return)
           raw) (assoc :deserialize false)))))

(defn redis-template
  "creates a redis form from data"
  {:added "3.0"}
  ([fsym var]
   (redis-template fsym var {}))
  ([fsym var {:keys [post pre chain raw string default namespace inputs debug
                     allow-empty] :as custom}]
   (let [{:keys [arglists]
          :redis/keys [return multiple]} (meta var)
         return   (or (:return custom)
                      return)
         multiple (if-not (nil? (:multiple custom))
                    (:multiple custom)
                    multiple)
         [fargs dargs] arglists
         ilen  (count inputs)
         fargs (or (-> custom :args :symbols)
                   (vec (drop ilen fargs)))
         dargs (or (-> custom :args :display)
                   (vec (drop ilen dargs)))
         iargs (reduce-kv (fn [iargs i prepend]
                            (update iargs i #(concat (collection/seqify prepend) [%])))
                          fargs
                          (-> custom :args :format))
         pipeline (redis-pipeline return custom)
         form  `(cc/req ~'redis ~'cmd
                       ~(if pipeline
                          `(cc/req:opts ~'opts
                                       ~pipeline)
                          'opts))
         opts-pipeline (keep (fn [[k val]]
                               (if-not (nil? val) `(assoc ~k ~val)))
                             (select-keys pipeline [:namespace :format]))]
     `(defn ~fsym
        ([~'redis ~@fargs]
         (~fsym ~'redis ~@fargs {}))
        ([~'redis ~@(butlast dargs) ~'opts]
         (let [~@(if (not-empty opts-pipeline)
                   ['opts `(-> ~'opts ~@opts-pipeline)])
               ~'cmd  (~(f/var-sym var) ~@inputs ~@iargs ~'opts)]
           ~@(if debug
               [`(env/prn :REDIS
                        (quote ~fsym)
                        (quote ~pipeline)
                        ~return
                        (cc/req:opts ~'opts
                                    ~pipeline)
                        ~'opts ~'cmd)])
           ~(if (and multiple
                     (not allow-empty))
              (let [varg (if (integer? multiple)
                           (get fargs multiple)
                           (last fargs))]
                `(if (and (empty? ~varg))
                   (common/return-default ~default ~'opts)
                   ~form))
              form)))))))
