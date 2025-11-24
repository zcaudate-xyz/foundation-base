(ns std.lib.bin.buffer-test
  (:use code.test)
  (:require [std.lib.bin.buffer :refer :all]))

^{:refer std.lib.bin.buffer/byte-buffer :added "3.0" :adopt true}
(fact "creates a byte buffer"

  (byte-buffer 5 {:direct true
                  :endian :little})
  => java.nio.ByteBuffer)

^{:refer std.lib.bin.buffer/char-buffer :added "3.0" :adopt true}
(fact "creates a byte buffer"

  (char-buffer 5 {:direct true
                  :endian :little})
  => java.nio.CharBuffer)

^{:refer std.lib.bin.buffer/short-buffer :added "3.0" :adopt true}
(fact "creates a short buffer"

  (short-buffer 5 {:direct true
                   :endian :little})
  => java.nio.ShortBuffer)

^{:refer std.lib.bin.buffer/int-buffer :added "3.0" :adopt true}
(fact "creates a int buffer"

  (int-buffer 5 {:direct true
                 :endian :little})
  => java.nio.IntBuffer)

^{:refer std.lib.bin.buffer/long-buffer :added "3.0" :adopt true}
(fact "creates a long buffer"

  (long-buffer 5 {:direct true
                  :endian :little})
  => java.nio.LongBuffer)

^{:refer std.lib.bin.buffer/float-buffer :added "3.0" :adopt true}
(fact "creates a float buffer"

  (float-buffer 5 {:direct true
                   :endian :little})
  => java.nio.FloatBuffer)

^{:refer std.lib.bin.buffer/double-buffer :added "3.0" :adopt true}
(fact "creates a double buffer"

  (double-buffer 5 {:direct true
                    :endian :little})
  => java.nio.DoubleBuffer)

^{:refer std.lib.bin.buffer/byte-order :added "3.0"}
(fact "converts keyword to ByteOrder"

  (byte-order :little)
  => java.nio.ByteOrder/LITTLE_ENDIAN)

^{:refer std.lib.bin.buffer/create-buffer-records :added "3.0"}
(fact "creates records for buffer types"

  (macroexpand-1 '(create-buffer-records ([Byte identity]))) ^:hidden
  => '{:byte {:buffer java.nio.ByteBuffer
              :convert identity
              :allocate (clojure.core/fn [n] (java.nio.ByteBuffer/allocate n))
              :wrap (clojure.core/fn [arr] (java.nio.ByteBuffer/wrap arr))}})

^{:refer std.lib.bin.buffer/create-buffer-functions :added "3.0"}
(fact "creates functions for buffer types"

  (macroexpand-1 '(create-buffer-functions (:byte))) ^:hidden
  => '[(clojure.core/defn byte-buffer
         ([len-or-elems] (byte-buffer len-or-elems {}))
         ([len-or-elems {:keys [type direct endian convert] :as opts}]
          (std.lib.bin.buffer/buffer len-or-elems (clojure.core/assoc opts :type :byte))))])

^{:refer std.lib.bin.buffer/buffer-convert :added "3.0"}
(fact "converts an nio ByteBuffer to another type"

  (buffer-convert (java.nio.ByteBuffer/allocate 8)
                  :double)
  => java.nio.DoubleBuffer)

^{:refer std.lib.bin.buffer/buffer-type :added "3.0"}
(fact "returns the corresponding type associated with the class"

  (buffer-type java.nio.FloatBuffer :type)
  => :float

  (buffer-type java.nio.FloatBuffer :array-fn)
  => float-array)

^{:refer std.lib.bin.buffer/buffer-primitive :added "3.0"}
(fact "returns the corresponding type associated with the instance"

  (buffer-primitive (double-buffer 0) :class)
  => Double/TYPE)

^{:refer std.lib.bin.buffer/buffer-put :added "3.0"}
(fact "utility for putting arrays into buffers"

  (buffer-put (double-buffer 2) (double-array [1 2]))
  => java.nio.DoubleBuffer)

^{:refer std.lib.bin.buffer/buffer-get :added "3.0"}
(fact "utility for getting arrays from buffers"
  (def -arr- (double-array 2))

  (buffer-get (double-buffer [1 2]) -arr-)
  (seq -arr-)
  => [1.0 2.0])

^{:refer std.lib.bin.buffer/buffer-write :added "3.0"}
(fact "writes primitive array to a buffer"

  (def ^java.nio.ByteBuffer -buf- (buffer 16))
  (buffer-write -buf- (int-array [1 2 3 4]))

  [(.get -buf- 3) (.get -buf- 7) (.get -buf- 11) (.get -buf- 15)]
  => [1 2 3 4])

^{:refer std.lib.bin.buffer/buffer-create :added "3.0"}
(fact "creates a byte buffer"

  (buffer-create :double (double-array [1 2 3 4]) 4 true :little true)
  => java.nio.DoubleBuffer

  (buffer-create :double (double-array [1 2 3 4]) 4 false :big false)
  => java.nio.ByteBuffer)

^{:refer std.lib.bin.buffer/buffer :added "3.0"}
(fact "either creates or wraps a byte buffer of a given type"

  (buffer 10)
  => java.nio.ByteBuffer

  (buffer 10 {:type :float
              :convert false})
  => java.nio.ByteBuffer

  (buffer 10 {:type :float
              :direct true})
  => java.nio.FloatBuffer ^:hidden

  (def farr (float-array [1 2 3 4]))
  (= (.array ^java.nio.FloatBuffer (buffer farr {:type :float :wrap true}))
     farr)
  => true

  (not= (.array ^java.nio.FloatBuffer (buffer farr {:type :float}))
        farr)
  => true)

^{:refer std.lib.bin.buffer/buffer-read :added "3.0"}
(fact "reads primitive array from buffer"

  (def -buf- (buffer 4))
  (def -out- (int-array 1))

  (do (.put ^java.nio.ByteBuffer -buf- 3 (byte 1))
      (buffer-read -buf- -out-)
      (first -out-))
  => 1)
