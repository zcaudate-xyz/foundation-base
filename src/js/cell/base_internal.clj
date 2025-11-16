(ns js.cell.base-internal
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:require [[js.core :as j]
             [xt.lang.base-repl :as repl]
             [xt.lang.base-lib :as k]
             [js.cell.base-util :as util]
             [js.cell.base-fn :as base-fn]]})

(defn.js worker-handle-async
  "worker function for handling async tasks"
  {:added "4.0"}
  [worker f op id body]
  (return (. (f (:.. body))
             (then  (fn [ret]
                      (j/postMessage worker {:op op
                                             :id id
                                             :status "ok"
                                             :body (util/arg-encode ret)})))
             (catch (fn [ret]
                      (when (. ret ["stack"])
                        (k/TRACE! (. ret ["stack"]) "ERR"))
                      
                      (j/postMessage worker {:op op
                                             :id id
                                             :status "error"
                                             :body ret}))))))

(defn.js worker-process
  "processes various types of routes"
  {:added "4.0"}
  [worker input]
  (var #{op id body route} input)
  (var post-fn (fn:> [x] (j/postMessage worker x)) )
  (cond (== op "eval")
        (do (when (== false (. (base-fn/get-state worker)
                               ["eval"]))
              (j/postMessage worker {:op op
                                     :id id
                                     :status "error"
                                     :body (k/cat "Not enabled - EVAL")}))
            (var out (repl/return-eval body))
            (var f (:? (. input ["async"])
                       j/identity
                       post-fn))
            (return (f {:op op
                        :id id
                        :status "ok"
                        :body out})))
        
        (== op "route")
        (do (var route-entry  (. (base-fn/get-routes worker)
                                 [route]))
            (when (== nil route-entry)
              (return (j/postMessage worker {:op op
                                             :id id
                                             :status "error"
                                             :body (k/cat "Route not found - " route)})))
            
            (var route-async  (. route-entry ["async"]))
            (var route-fn     (. route-entry ["handler"]))
            (var f   (:? route-async
                         j/identity
                         post-fn))
            
            (try
              (:= body (util/arg-decode (or body [])))
              (var out (:? route-async
                           (-/worker-handle-async worker route-fn op id body)
                           (route-fn (:.. body))))
              (return (f {:op op
                          :id id
                          :status "ok"
                          :body (util/arg-encode out)}))
              (catch err
                  (return (f {:op op
                              :id id
                              :status "error"
                              :body err})))))
        
        :else
        (post-fn {:op op
                  :status "error"
                  :body input})))


(defn.js worker-init
  "initiates the worker routes"
  {:added "4.0"}
  [worker input-fn]
  (:= input-fn (or input-fn k/identity))
  (. worker (addEventListener
             "message"
             (fn [e]
               (cond (k/is-string? e.data)
                     (-/worker-process worker
                                       (input-fn
                                        {:op "eval"
                                         :id nil
                                         :body e.data}))
                     
                     :else
                     (-/worker-process worker (input-fn e.data))))
             false))
  (return true))

(defn.js worker-init-post
  "posts an init message"
  {:added "4.0"}
  [worker body]
  (return (j/postMessage worker {:op "stream"
                                 :status "ok"
                                 :topic util/EV_INIT
                                 :body body})))


;;
;; 
;;

(defn.js mock-send
  "sends a request to the mock worker"
  {:added "4.0"}
  [mock message]
  (try 
    (cond (k/is-string? message)
          (-/worker-process mock
                            {:op "eval"
                             :id nil
                             :body message})
          
          :else
          (-/worker-process mock message))
    (catch e (k/TRACE! (. e ["stack"]) "SEND.ERROR"))))

(defn.js new-mock
  "creates a new mock worker
 
   (!.js
    (internal/new-mock k/identity))
   => {\"::\" \"worker.mock\", \"listeners\" [nil]}"
  {:added "4.0"}
  [listener]
  (var mock {"::" "worker.mock"
             :listeners [listener]})
  (var postMessage (fn [event]
                     (var #{listeners} mock)
                     (k/for:array [listener listeners]
                       (listener event))))
  (var postRequest (fn:> [request]
                     (j/future (-/mock-send mock request))))
  (j/assign mock #{postMessage
                   postRequest})
  (return mock))

(defn.js mock-init
  "initialises the mock worker"
  {:added "4.0"}
  [listener routes suppress]
  (var mock (-/new-mock listener))
  (when routes
    (base-fn/routes-init routes))
  (when (not suppress)
    (-/worker-init-post mock {:done true}))
  (return mock))

