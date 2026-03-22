(ns std.lib.apply
  (:require [std.lib.foundation :as h]
            [std.lib.future :as f]
            [std.lib.impl :as impl :refer [defimpl]]
            [std.lib.return :as r]
            [std.protocol.apply :as protocol.apply]))

(defn apply-in
  "runs the applicative within a context
 
   (apply-in (host-applicative {:form '+})
             nil
             [1 2 3 4 5])
   => 15"
  {:added "3.0"}
  ([app rt args]
   (let [input  (protocol.apply/-transform-in app rt args)
         output (protocol.apply/-apply-in app rt input)]
     (r/return-chain output (partial protocol.apply/-transform-out app rt args)))))

(defn apply-as
  "allows the applicative to auto-resolve its context
 
   (apply-as (host-applicative {:form '+})
             [1 2 3 4 5])
   => 15"
  {:added "3.0"}
  ([app args]
   (let [rt (or (:runtime app)
                (protocol.apply/-apply-default app))
         rt (if (fn? rt) (rt) rt)]
     (apply-in app rt args))))

(defn invoke-as
  "invokes the applicative to args
 
   (invoke-as (host-applicative {:form '+})
              1 2 3 4 5)
   => 15"
  {:added "3.0"}
  ([app & args]
   (apply-as app args)))

(defn host-apply-in
  "helper function for the `host` applicative"
  {:added "3.0"}
  ([{:keys [form function async] :as app} _ args]
   (let [f (or function (eval form))]
     (if async
       (f/future (apply f args))
       (apply f args)))))

(defimpl HostApplicative
  [function form async]
  :prefix   "host-"
  :invoke   invoke-as
  :protocols [std.protocol.apply/IApplicable
              :body {-apply-default  nil
                     -transform-in   args
                     -transform-out  return}])

(defn host-applicative
  "constructs an applicative that does not need a context
 
   @((host-applicative {:form '+ :async true}) 1 2 3 4 5)
   => 15"
  {:added "3.0"}
  ([{:keys [function form async] :as m}]
   (map->HostApplicative m)))

(comment
  (./create-tests)
  (apply-in host-fn)
  ((host-applicative {:form '+}) 1 2 3 4))

