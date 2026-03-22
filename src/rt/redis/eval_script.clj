(ns rt.redis.eval-script
  (:require [lib.redis.script :as script]
            [rt.basic.impl.process-lua :as lua]
            [rt.redis.eval-basic :as eval-basic]
            [std.concurrent :as cc]
            [std.json :as json]
            [std.lang :as l]
            [std.lang.base.emit :as emit]
            [std.lang.base.impl :as impl]
            [std.lang.base.pointer :as ptr]
            [std.lang.base.util :as ut]
            [std.lib.env :as env]
            [std.lib.foundation :as f]
            [std.lib.invoke :as invoke]
            [std.lib.security :as security]))

(defn raw-compile-form
  "converts a ptr into a form"
  {:added "4.0"}
  ([ptr]
   (let [{:keys [form id]
          :rt/keys [redis] :as entry
          module-id :module} (ptr/get-entry ptr)
         _ (and (or entry (f/error "Entry not found" {:input ptr}))
                (or redis (f/error  "Needs to have a redis entry" {:input entry})))
         
         {:keys [nkeys nargs vargs encode]
          :or {nkeys 0}} redis
         function (ut/sym-full module-id id)
         nargs    (or nargs (- (count (nth form 2))
                               nkeys))
         {enc-in :in enc-out :out} encode
         gen-forms  (fn [sym n]
                      (mapv (fn [k]
                              (list '. sym [k]))
                            (map inc (range n))))
         dec-forms  (fn [forms enc-in]
                      (if (not enc-in)
                        forms
                        (let [enc-in  (set enc-in)]
                          (map-indexed (fn [i form]
                                         (if (enc-in (+ nkeys i))
                                           (list 'cjson.decode form)
                                           form))
                                       forms))))
         keys-forms (cond (or (not nkeys) (zero? nkeys))[]
                          (pos? nkeys) (gen-forms 'KEYS nkeys)
                          (neg? nkeys) '[(unpack KEYS)])
         argv-forms (cond (neg? nargs)
                          '[(unpack ARGV)]

                          :else
                          (let [argv-forms (gen-forms 'ARGV nargs)]
                            (if vargs
                              (dec-forms argv-forms (conj enc-in nargs))
                              (dec-forms argv-forms enc-in))))
         form        (cond->> `(~function ~@keys-forms ~@argv-forms)
                       enc-out (list 'cjson.encode)
                       :then   (list 'return))]
     form)))

(invoke/definvoke raw-compile
  "compiles a function as body and sha"
  {:added "4.0"}
  [:recent {:key ut/sym-full
            :compare f/hash-id}]
  ([ptr]
   (let [form (raw-compile-form ptr)
         body (impl/emit-script
               form
               {:lang :lua
                :layout :flat})]
     {:body body
      :sha  (security/sha1 body)})))

(defn raw-prep-in-fn
  "prepares the arguments for entry"
  {:added "4.0"}
  ([{:rt/keys [redis] :as entry} args]
   (let [{:keys [nkeys encode]
          :or {nkeys 0}} (or redis
                             (f/error "Needs to have a redis entry"))
         {enc-in :in} encode
         enc-in (set enc-in)
         args   (map-indexed (fn [i arg]
                               (if (enc-in i)
                                 (json/write arg)
                                 arg))
                             args)]
     [(take nkeys args) (drop nkeys args)])))

(defn raw-prep-out-fn
  "prepares arguments out"
  {:added "4.0"}
  ([{:rt/keys [redis] :as entry} out]
   (let [enc-out (get-in (or redis
                             (f/error "Needs to have a redis entry"))
                         [:encode :out])]
     (if (instance? Throwable out)
       (throw out)
       (cond-> out
         enc-out (json/read json/+keyword-spear-mapper+))))))

(defn rt-install-fn
  "retries the function if not installed"
  {:added "4.0"}
  [redis sha body keys args]
  (fn [out]
    (cond (instance? Throwable out)
          (let [msg (.getMessage ^Throwable out)]
            (if (.startsWith msg "NOSCRIPT")
              (do (script/script:load redis body)
                  (script/script:evalsha redis sha (count keys) keys args))
              (eval-basic/rt-exception out redis body)))
          :else out)))

(defn redis-invoke-sha
  "creates a sha call
 
   (redis-invoke-sha -client-
                     redis/call-fn
                     [\"PING\"]
                     true)
   => (throws)
 
   
   (redis-invoke-sha -client-
                     redis/call-fn
                     [\"PING\"])
   => \"PONG\""
  {:added "4.0"}
  ([redis ptr args]
   (redis-invoke-sha redis ptr args false))
  ([redis ptr args no-install]
   (let [entry (ptr/get-entry ptr)
         {:keys [body sha]} (raw-compile ptr)
         _  (if (or (:input ptr/*print*)
                    (:raw-input ptr/*print*))
              (env/pl body))
         [keys args] (raw-prep-in-fn entry args)
         install-fn  (if no-install
                       identity
                       (rt-install-fn redis sha body keys args))
         opts (cc/req:opts eval-basic/*runtime-opts*
                          {:post [install-fn
                                  (fn [args]
                                    (raw-prep-out-fn entry args))]})]
     (script/script:evalsha redis sha (count keys) keys args opts))))
