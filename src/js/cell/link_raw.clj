(ns js.cell.link-raw
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]
            [js.cell.base-fn :as base-fn]))

(l/script :js
  {:require [[js.core :as j]
             [xt.lang.base-lib :as k]
             [js.cell.base-util :as util]]})

;;
;;
;;

(defn.js link-listener-call
  "resolves a call to the link"
  {:added "4.0"}
  [data active]
  (var #{op id status body} data)
  (var entry (k/get-key active id))
  (when (k/not-nil? entry)
    (var #{input time resolve reject} entry)
    (:= input (or input {}))
    (try
      (cond (== status "ok")
            (cond (== op "route")
                  (do (k/del-key active id) 
                      (return (resolve (util/arg-decode body))))
                  
                  (== op "eval")
                  (do (k/del-key active id)
                      (var out (k/json-decode body))
                      (var #{type value} out)
                      (cond (== type "data")
                            (return (resolve out.value))
                            
                            :else
                            (return (resolve out)))))
            :else
            (do (k/del-key active id)
                (return (reject (j/assign {:route (. input ["route"])
                                           :input (. input ["body"])
                                           :start-time time
                                           :end-time (k/now-ms)}
                                          data)))))
      (catch err (return (reject {:op op
                                  :id id
                                  :route (. input ["route"])
                                  :status "error"
                                  :message "Format Invalid"
                                  :input (. input ["body"])
                                  :start-time time
                                  :end-time (k/now-ms)
                                  :body body}))))))

(defn.js link-listener-event
  "notifies all registered callbacks"
  {:added "4.0"}
  [data callbacks]
  (var #{op id topic status body} data)
  (var out [])
  (k/for:object [[p-id callback] callbacks]
    (var #{pred handler} callback)
    (when (util/check-event pred topic data)
      (try (handler data topic)
           (x:arr-push out p-id)
           (catch err (k/LOG! {:stack   (. err ["stack"])
                               :message (. err ["message"])})))))
  (return out))

(defn.js link-listener
  "constructs a link listener"
  {:added "4.0"}
  [e active callbacks]
  (var #{data} e)
  (var #{op id} data)
  (cond  (or (== op "eval")
             (== op "route"))
         (return (-/link-listener-call data active))

         (or (== op "stream"))
         (return (-/link-listener-event data callbacks))))

(defn.js link-create-worker
  "helper function to create a worker"
  {:added "4.0"}
  [worker-url active callbacks]
  (cond (or (k/is-string? worker-url)
            (k/fn? worker-url))
        (do (var worker (new Worker worker-url))
            (. worker (addEventListener
                       "message"
                       (fn:> [e] (-/link-listener e active callbacks))))
            (return worker))

        :else
        (do (var #{create-fn} worker-url)
            (return
             (create-fn 
              (fn [data]
                (-/link-listener {:data data} active callbacks)))))))

(defn.js link-create
  "creates a link from url"
  {:added "4.0"}
  [worker-url]
  (var active {})
  (var callbacks {})
  (var worker (-/link-create-worker worker-url active callbacks))
  (return {"::" "cell.link"
           :id (util/rand-id "" 3)
           :worker worker
           :active active
           :callbacks callbacks}))

(defn.js link-active
  "gets the calls that are active"
  {:added "4.0"}
  [link]
  (return (k/get-key link "active")))

(defn.js add-callback
  "adds a callback to the link"
  {:added "4.0"}
  [link key pred handler]
  (var prev (. link ["callbacks"] [key]))
  (:= (. link ["callbacks"] [key]) {:key key
                                    :pred pred
                                    :handler handler})
  (return [prev]))

(defn.js list-callbacks
  "lists all callbacks on the link"
  {:added "4.0"}
  [link]
  (return (j/keys (. link ["callbacks"]))))

(defn.js remove-callback
  "removes a callback on the link"
  {:added "4.0"}
  [link key]
  (var prev (. link ["callbacks"] [key]))
  (del (. link ["callbacks"] [key]))
  (return [prev]))

;;
;; CALL
;;

(defn.js call-id
  "gets the call id"
  {:added "4.0"}
  [link]
  (var #{id active} link)
  (while true
    (var cid (util/rand-id (+ id "-") 3))
    (when (k/nil? (k/get-key active cid))
      (return cid))))

(defn.js call
  "calls the link with an event"
  {:added "4.0"}
  [link event]
  (var #{worker active} link)
  (var cid (or (. event ["id"])
               (-/call-id link)))
  (:= (. event ["id"]) cid)
  (var input (util/arg-encode event))
  (var p (new Promise (fn [resolve reject]
                        (:= (. active [cid])
                            {:resolve (fn [data]
                                        #_(when (== "stats/local" (. event route))
                                          (k/LOG! data event))
                                        (resolve data))
                             :reject (fn [data]
                                       #_(when (== "stats/local" (. event route))
                                          (k/LOG! data event))
                                       (reject data))
                             :input  input
                             :time   (k/now-ms)}))))
  (var #{postRequest} worker)
  (if postRequest
    (postRequest input)
    (. worker (postMessage input)))
  (return p))

;;
;; TESTING
;;

(def ^:dynamic *temp-id* nil)

(defmacro ^{:style/indent 1}
  wait-post
  "posts code to worker"
  {:added "4.0"}
  [worker body & [action opts id]]
  (let [[worker timeout] (if (vector? worker)
                           worker
                           [worker 1000])
        [op input] (cond (string? action)
                         [action body]
                         
                         :else
                         ["eval" (list '@! (list 'std.lang/with:input (list '!.js body)))])]
    (list 'xt.lang.base-notify/wait-on [:js timeout]
          (list '. worker (list 'postMessage (merge {:op op
                                                     :id id
                                                     :body input}
                                                    opts))))))

(defmacro async-post
  "helper for async post"
  {:added "4.0"}
  [value]
  (list 'postMessage
        {:op "eval"
         :id (or *temp-id* (h/error "No return id"))
         :status "ok"
         :body (list 'JSON.stringify {:type "data"
                                      :value value})}))

(defn- async-post-transform
  [body id]
  (h/prewalk (fn [form]
               (cond (and (h/form? form)
                          (symbol? (first form))
                          (resolve (first form))
                          (= (h/var-sym (resolve (first form)))
                             `async-post))
                     (list 'postMessage
                           {:op "eval"
                            :id id
                            :status "ok"
                            :body (list 'JSON.stringify {:type "data"
                                                         :value (second form)})})
                     (and (symbol? form)
                          (resolve form)
                          (= (h/var-sym (resolve form))
                             `async-post))
                     (list 'fn '[value]
                           (list 'postMessage
                                 {:op "eval"
                                  :id id
                                  :status "ok"
                                  :body (list 'JSON.stringify {:type "data"
                                                               :value 'value})}))
                     
                     :else form))
             body))  

(defmacro.js ^{:style/indent 1}
  post-eval
  "posts to worker, works in conjuction with async-post"
  {:added "4.0"}
  [link body & [async id]]
  (let [id    (or id (str "eval-" (rand-int 10000000)))
        body  (async-post-transform body id)
        input (eval (list 'std.lang/with:input (list '!.js body)))]
    (list `call link {:op "eval"
                      :id id
                      :async async
                      :body input})))

(defmacro ^{:style/indent 1}
  wait-eval
  "posts code to worker with eval"
  {:added "4.0"}
  [link body & [async id]]
  (let [[link timeout] (if (vector? link)
                         link
                         [link 1000])
        id    (or id (str "eval-" (rand-int 10000000)))
        body  (async-post-transform body id)
        input (list '@! (list 'std.lang/with:input (list '!.js body)))]  
    (list 'xt.lang.base-notify/wait-on [:js timeout]
          (list '. (list `call link {:op "eval"
                                     :id id
                                     :async async
                                     :body input})
                (list 'then '(xt.lang.base-repl/>notify))))))

