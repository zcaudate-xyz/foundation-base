(ns hara.runtime.jocl.type-test
  (:use code.test)
  (:require [hara.runtime.jocl.type :refer :all]
            [std.lib.bin.buffer :as bin])
  (:refer-clojure :exclude [to-array]))

^{:refer hara.runtime.jocl.type/buffer-type :added "3.0"}
(fact "outputs type information for buffers"
  ^:hidden
  
  (buffer-type (bin/byte-buffer 10))
  => {:buffer true, :unit :byte, :dsize 1, :length 10}

  (buffer-type (int-array 10))
  => {:buffer true, :unit :int, :dsize 4, :length 10})

^{:refer hara.runtime.jocl.type/unit-type :added "3.0"}
(fact "outputs type information for unit inputs"
  ^:hidden
  
  (unit-type 1)
  => {:unit :long, :dsize 8}
  
  (unit-type [])
  => (throws))

^{:refer hara.runtime.jocl.type/type-args :added "3.0"}
(fact "returns and checks type information of inputs"
  ^:hidden
  
  (type-args [{:buffer true :dsize 1 :output true}]
             [(bin/buffer 10)])
  => [{:buffer true, :unit :byte, :dsize 1, :length 10, :output true}]
  
  (type-args [{:buffer true :dsize 8 :output true}]
             [(long-array 10)])
  => [{:buffer true, :unit :long, :dsize 8, :length 10, :output true}]
  
  (type-args [{:buffer true :dsize 8 :output true}]
             [(int-array 10)])
  => (throws)
  
  (type-args [{:buffer true :output true}]
             [10])
  => (throws))

^{:refer hara.runtime.jocl.type/to-array :added "3.0"}
(fact "converts a value to an array"

  (str (type (to-array 10)))
  => "class [J")
