(ns rt.redis.eval-basic
  (:require [lib.redis.script :as script]
            [rt.basic.impl.process-lua :as lua]
            [std.concurrent :as cc]
            [std.json :as json]
            [std.lang :as l]
            [std.lang.base.emit-common :as common]
            [std.lang.base.impl :as impl]
            [std.lang.base.pointer :as ptr]
            [std.lang.base.runtime :as default]
            [std.lib.env :as env]
            [std.lib.foundation :as f]
            [xt.lang.base-repl :as k]))

(def ^:dynamic *runtime-opts* {})

(defn- rt-return
  ([f]
   (fn [res]
     (if (f/throwable? res)
       (throw res)
       (f res)))))

(defn rt-exception
  "processes an exception"
  {:added "4.0"}
  ([^Throwable e rt body]
   (let [{:keys [dev]} rt
         msg (.getMessage e)]
     (cond (or (.contains msg "ERR Error running script")
               (.contains msg "ERR Error compiling script"))
           (let [[start end] (->> (concat (re-seq #"user_script:(\d+)" msg)
                                          (re-seq #"line (\d+)" msg))
                                  (map (comp f/parse-long second))
                                  (sort)
                                  ((juxt first last)))
                 section (env/pl-add-lines body [(dec start) (inc end)] [2 1])
                 ex (ex-info (str msg "\n\n" section) {:type (type e)})]
             (when common/*explode*
               (env/pl body))
             (throw (doto ^Throwable ex
                      (.setStackTrace (.getStackTrace e)))))
           :else (throw e)))))

(defn redis-raw-eval
  "conducts a raw ewal
 
   (redis-raw-eval (l/rt :lua)
                   \"return 1 + 2\")
   => 3"
  {:added "4.0"}
  ([redis body]
   (let [process-fn  (fn [out]
                       (cond (instance? Throwable out)
                             (let [msg (.getMessage ^Throwable out)]
                               (rt-exception out redis body))
                             :else out))
         opts (cc/req:opts *runtime-opts* {:post [process-fn]})]
     (script/script:eval redis body 0 [] [] opts))))

(defn redis-body-transform
  "transform body into output form
 
   (redis-body-transform '(+ 1 2 3)
                         {})
   => '(return (return-wrap (fn [] (return (+ 1 2 3)))))"
  {:added "4.0"}
  [input mopts]
  (default/return-transform
   input mopts
   {:wrap-fn (fn [forms]
               `(~'return (~'return-wrap (~'fn [] ~@forms))))}))

(def ^{:arglists '([body])}
  redis-main-wrap
  (let [bootstrap (impl/emit-entry-deps
                   k/return-wrap
                   {:lang :lua
                    :layout :flat})]
    (fn [body]
      (str bootstrap "\n\n" body))))

(defn redis-invoke-ptr-basic
  "invokes pointer for redis eval
   
   (redis-invoke-ptr-basic
    (l/rt :lua)
    k/arr-map [[1 2 3 4 5] k/inc])
   => [2 3 4 5 6]"
  {:added "4.0"}
  ([{:keys [layout] :as redis
     :or {layout :flat}} ptr args]
   (default/default-invoke-script
    redis
    ptr args redis-raw-eval
    {:main {:in #'redis-main-wrap}
     :emit {:native {:suppress true}
            :body {:transform #'redis-body-transform}}
     :json :full
     :layout layout})))



