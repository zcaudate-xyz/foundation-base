(ns hara.runtime.jocl.type-test
  (:refer-clojure :exclude [to-array])
  (:use code.test)
  (:require [hara.runtime.jocl :refer :all]
            [hara.runtime.jocl.env :as jocl-env]))

(jocl-env/with-stubs buffer-type unit-type type-args to-array)

(fact:global
 {:skip (not (jocl-env/opencl-available?))})

^{:refer hara.runtime.jocl.type/buffer-type :added "3.0"}
(fact "outputs type information for buffers"

  (buffer-type (float-array [1 2 3]))
  => {:buffer true :unit :float :dsize 4 :length 3})

^{:refer hara.runtime.jocl.type/unit-type :added "3.0"}
(fact "outputs type information for unit inputs"

  (unit-type 1)
  => {:unit :long :dsize 8})

^{:refer hara.runtime.jocl.type/type-args :added "3.0"}
(fact "returns and checks type information of inputs"

  (type-args '[{:type :float :dsize 4 :buffer true}
               {:type :long :dsize 8}]
             [(float-array [1 2 3]) 1])
  => [{:buffer true :unit :float :dsize 4 :length 3}
      {:unit :long :dsize 8}])

^{:refer hara.runtime.jocl.type/to-array :added "3.0"}
(fact "converts a value to an array"
 
  (str (type (to-array 10)))
  => "class [J")
