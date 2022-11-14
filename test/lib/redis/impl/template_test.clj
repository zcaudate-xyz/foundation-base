(ns lib.redis.impl.template-test
  (:use code.test)
  (:require [lib.redis.impl.template :refer :all]
            [lib.redis.script :as script]))

^{:refer lib.redis.impl.template/redis-pipeline :added "3.0"}
(fact "constructs a pipeline for `opts`"
  ^:hidden

  (redis-pipeline :data {})
  => {:deserialize true}

  (redis-pipeline :ack '{:pre [pos?]})
  => {:pre '[pos?]})

^{:refer lib.redis.impl.template/redis-template :added "3.0"}
(fact "creates a redis form from data"
  ^:hidden

  (redis-template 'eval-redis #'script/in:eval)
  => '(clojure.core/defn eval-redis
        ([redis script numkeys keys args] (eval-redis redis script numkeys keys args {}))
        ([redis script numkeys keys args opts]
         (clojure.core/let [cmd (lib.redis.script/in:eval script numkeys keys args opts)]
           (if (clojure.core/and (clojure.core/empty? args))
             (common/return-default nil opts)
             (std.concurrent/req redis cmd
                                 (std.concurrent/req:opts opts {:deserialize true})))))))
