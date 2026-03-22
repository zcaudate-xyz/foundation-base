(ns std.lib.return
  (:require [std.lib.foundation :as h]
            [std.lib.future :as f]
            [std.lib.impl :as impl :refer [defimpl]]
            [std.protocol.return :as protocol.return])
  (:import (java.util.concurrent CompletableFuture CompletionStage)))

(impl/build-impl {}
                 protocol.return/IReturn)

(impl/extend-impl nil
                  :protocols [std.protocol.return/IReturn
                              :body {-get-value     obj
                                     -get-error     nil
                                     -has-error?    true
                                     -get-status    :success
                                     -get-metadata  (if (h/iobj? obj) (meta obj))
                                     -is-container? false}])

(impl/extend-impl Object
                  :protocols [std.protocol.return/IReturn
                              :body {-get-value     obj
                                     -get-error     nil
                                     -has-error?    false
                                     -get-status    :success
                                     -get-metadata  (if (h/iobj? obj) (meta obj))
                                     -is-container? false}])

(impl/extend-impl Throwable
                  :protocols [std.protocol.return/IReturn
                              :body {-get-value     true
                                     -get-error     obj
                                     -has-error?    true
                                     -get-status    :error
                                     -get-metadata  (if (h/iobj? obj) (meta obj))
                                     -is-container? false}])

(impl/extend-impl CompletionStage
                  :protocols [std.protocol.return/IReturn
                              :body   {-get-metadata  nil
                                       -is-container? true}
                              :method {-get-value     f/future:value
                                       -get-error     f/future:exception
                                       -has-error?    f/future:exception?
                                       -get-status    f/future:status}])

(defn return-resolve
  "resolves encased futures
 
   (return-resolve (f/future (f/future 1)))
   => 1"
  {:added "3.0"}
  [res]
  (if (or (h/ideref? res)
          (f/future? res))
    (return-resolve @res)
    res))

(defn return-chain
  "chains a function if a future or resolves if not
 
   (return-chain 1 inc)
   => 2
 
   @(return-chain (f/future 1) inc)
   => 2"
  {:added "3.0"}
  ([out f]
   (cond (f/future? out)
         (f/on:success out f)

         :else
         (f out))))

