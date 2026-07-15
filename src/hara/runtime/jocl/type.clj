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

(defn unit-coerce
  "coerces a scalar value to the kernel's declared primitive `unit`.

   Kernel scalars must be passed to `clSetKernelArg` with exactly the
   declared size — a Clojure long (8 bytes) for an `:int` parameter is
   rejected with `CL_INVALID_KERNEL_ARGS` by strict drivers (e.g. NVIDIA
   OpenCL 3.0 / CUDA 13)."
  {:added "4.1"}
  ([unit arg]
   (case unit
     :int     (int arg)
     :uint    (int arg)
     :long    (long arg)
     :ulong   (long arg)
     :short   (short arg)
     :ushort  (short arg)
     :char    (unchecked-byte arg)
     :uchar   (unchecked-byte arg)
     :bool    (unchecked-byte arg)
     :float   (float arg)
     :double  (double arg)
     :half    (short arg)
     (h/error "Cannot coerce to unit" {:unit unit :arg arg}))))

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
