(ns hara.runtime.jocl.type
  (:require [std.lib.class :as class]
            [std.lib.foundation :as h]
            [std.lib.bin.buffer :as b])
  (:refer-clojure :exclude [to-array])
  (:import (java.nio Buffer)))

(defn buffer-type
  "outputs type information for buffers"
  {:added "3.0"}
  ([input]
   (let [[len unit] (cond (instance? Buffer input)
                          [(.capacity ^Buffer input) (b/buffer-primitive input)]
                          
                          (class/primitive:array? (type input))
                          [(count input) (keyword (class/primitive (type input) :string))]

                          :else
                          (h/error "Not valid array input" {:input input}))
         dsize (quot (class/primitive unit :size) 8)]
     {:buffer true
      :unit   unit
      :dsize  dsize
      :length len})))

(defn unit-type
  "outputs type information for unit inputs"
  {:added "3.0"}
  ([input]
   (let [^Class t (type input)
         _    (if (.isArray t)
                (h/error "Cannot be an array" {:input input}))
         unit (keyword (or (class/primitive t :string)
                           (h/error "Not a valid unit input" {:input input})))]
     {:unit unit
      :dsize (quot (class/primitive unit :size) 8)})))

(defn type-args
  "returns and checks type information of inputs"
  {:added "3.0"}
  ([spec args]
   (mapv (fn [{:keys [type dsize buffer output] :as entry} arg]
           (if buffer
             (let [ret (cond-> (buffer-type arg)
                         output (assoc :output true))]
               (if (not= dsize (:dsize ret))
                 (h/error "Buffer type mismatch" {:spec entry
                                                  :actual ret
                                                  :arg  arg})
                 ret))
             (unit-type arg)))
         spec args)))

(defn to-array
  "converts a value to an array
 
   (str (type (to-array 10)))
   => \"class [J\""
  {:added "3.0"}
  ([obj]
   ((class/primitive (type obj) :array-fn) [obj])))
