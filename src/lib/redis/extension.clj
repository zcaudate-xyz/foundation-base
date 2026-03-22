(ns lib.redis.extension
  (:require [lib.redis.impl.common :as common]
            [lib.redis.impl.generator :as gen]
            [lib.redis.impl.reference :as ref]
            [net.resp.wire :as wire]
            [std.concurrent :as cc]
            [std.lib.foundation :as f]
            [std.string.plural :as plural]))

(defn optional:set
  "optional parameters for `set` command"
  {:added "4.0"}
  ([{:keys [expiry unit mode]} _]
   (cond-> []
     mode   (conj (case mode
                    :not-exist "NX"
                    :exists "XX"))
     expiry (concat (cond (= expiry :keep)
                          ["KEEPTTL"]

                          (= unit :ms)
                          ["PX" expiry]

                          :else
                          ["EX" expiry]))
     :then vec)))

(def +management+
  (gen/select-commands {:group [:server :cluster :connection]
                    :exclude [:hello]
                    :custom  {:acl-log      {:arguments [{:name "count"}]}
                              :shutdown     {:arguments [{:name "mode"}]}}}))

(def +generic+
  (gen/select-commands {:group :generic
                    :exclude [:sort :migrate]
                    :custom  {:keys {:arguments [{:name "pattern", :type :key}]
                                     :return `common/return:keys}}}))

(def +string+
  (gen/select-commands {:group :string
                    :exclude [:bitcount :bitfield]
                    :custom  {:append {:arguments {1 {:type :data}}}
                              :getset {:arguments {1 {:type :data}}}
                              :psetex {:arguments {2 {:type :data}}}
                              :setex  {:arguments {2 {:type :data}}}
                              :setnx  {:arguments {1 {:type :data}}}
                              :msetnx {:arguments [{:sym 'args :process `common/process:kv}]}
                              :mset   {:arguments [{:sym 'args :process `common/process:kv}]}}
                    :replace {:set  {:id :set
                                     :return :ack
                                     :arguments [{:type :key}
                                                 {:type :data}
                                                 {:type :optional
                                                  :sym 'optional
                                                  :display '{:keys [expiry unit mode] :as optional}
                                                  :process `optional:set}]}}}))

(def +data+
  (gen/select-commands {:group [:list :hash :set]
                    :custom  {:lpos    {:arguments {1 {:type :data}}}
                              :lpush   {:arguments {1 {:type :data}}}
                              :lpushx  {:arguments {1 {:type :data}}}
                              :lrem    {:arguments {2 {:type :data}}}
                              :lset    {:arguments {2 {:type :data}}}
                              :rpush   {:arguments {1 {:type :data}}}
                              :rpushx  {:arguments {1 {:type :data}}}

                              :hgetall {:return `common/return:kv-hash}
                              :hkeys   {:return `common/return:string}
                              :hmset   {:arguments {1 {:sym 'args :process `common/process:kv-hash}}}
                              :hsetnx  {:arguments {2 {:type :data}}}

                              :sadd      {:arguments {1 {:type :data}}}
                              :srem      {:arguments {1 {:type :data}}}
                              :sismember {:arguments {1 {:type :data}}}
                              :smove     {:arguments {2 {:type :data}}}}
                    :replace {:hset  {:id :hset
                                      :return :ack
                                      :arguments [{:type :key}
                                                  {:type :string :name "field"}
                                                  {:type :data}]}}}))

(def +additional+
  (gen/select-commands {:group [:pubsub :scripting :hyperloglog :transactions]
                    :custom {:pfadd      {:arguments {1 {:type :data}}}
                             :publish    {:arguments {1 {:type :data}}}
                             :psubscribe {:arguments [{:type :key :sym 'patterns}]}
                             :subscribe  {:arguments [{:type :key}]}}}))

(def +all+ (concat +management+
                   +generic+
                   +string+
                   +data+
                   +additional+))

(f/template-entries [gen/optional-tmpl]
                    (->> +all+
                         (filter :optionals)
                         (remove (comp #{:set} :id))))

(f/template-entries [gen/command-tmpl] +all+)
