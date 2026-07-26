tahto/runtime/jocl/type_test.clj:1:(ns tahto.runtime.jocl.type-test
  (:refer-clojure :exclude [to-array])
  (:use code.test)
tahto/runtime/jocl/type_test.clj:4:  (:require [tahto.runtime.jocl :refer :all]
tahto/runtime/jocl/type_test.clj:5:            [tahto.runtime.jocl.env :as jocl-env]))

(jocl-env/with-stubs buffer-type unit-type type-args to-array)

(fact:global
 {:skip (not (jocl-env/opencl-available?))})

tahto/runtime/jocl/type_test.clj:12:^{:refer tahto.runtime.jocl.type/buffer-type :added "3.0"}
(fact "outputs type information for buffers"

  (buffer-type (float-array [1 2 3]))
  => {:buffer true :unit :float :dsize 4 :length 3})

tahto/runtime/jocl/type_test.clj:18:^{:refer tahto.runtime.jocl.type/unit-type :added "3.0"}
(fact "outputs type information for unit inputs"

  (unit-type 1)
  => {:unit :long :dsize 8})

tahto/runtime/jocl/type_test.clj:24:^{:refer tahto.runtime.jocl.type/type-args :added "3.0"}
(fact "returns and checks type information of inputs"

  (type-args '[{:type :float :dsize 4 :buffer true}
               {:type :long :dsize 8}]
             [(float-array [1 2 3]) 1])
  => [{:buffer true :unit :float :dsize 4 :length 3}
      {:unit :long :dsize 8}])

tahto/runtime/jocl/type_test.clj:33:^{:refer tahto.runtime.jocl.type/to-array :added "3.0"}
(fact "converts a value to an array"
 
  (str (type (to-array 10)))
  => "class [J")


tahto/runtime/jocl/type_test.clj:40:^{:refer tahto.runtime.jocl.type/unit-coerce :added "4.1"}
(fact "coerces scalars to the exact primitive width declared by a kernel"
  [(class (unit-coerce :int 7))
   (class (unit-coerce :long 7))
   (class (unit-coerce :short 7))
   (class (unit-coerce :char 7))
   (class (unit-coerce :float 7))
   (class (unit-coerce :double 7))]
  => [Integer Long Short Byte Float Double]
  (unit-coerce :unsupported 1)
  => (throws-info {:unit :unsupported :arg 1}))
