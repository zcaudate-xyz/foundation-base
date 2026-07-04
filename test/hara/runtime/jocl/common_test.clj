(ns hara.runtime.jocl.common-test
  (:refer-clojure :exclude [to-array])
  (:use code.test)
  (:require [hara.runtime.jocl :refer :all]
            [hara.runtime.jocl.env :as jocl-env]))

(jocl-env/with-stubs parse-spec entry-spec with-error cl-context
                     cl-queue cl-program cl-kernel device:gpu)

(fact:global
 {:skip (not (jocl-env/opencl-available?))})

^{:refer hara.runtime.jocl.common/parse-spec :added "3.0"}
(fact "parses a kernel arglist"

  (parse-spec {:modifiers #{:__global :const :float :*}, :symbol 'a}
              '{c {:output true}})
  => '{:key :a, :symbol a, :type :float,
       :dsize 4, :buffer true, :const true, :input true}
  
  (parse-spec {:modifiers #{:__global :long :*}, :symbol 'c}
              '{c {:output true}})
  => '{:key :c, :symbol c, :type :long, :dsize 8,
       :buffer true, :const false, :output true})

^{:refer hara.runtime.jocl.common/entry-spec :added "3.0"}
(fact "converts a form entry into a spec"
  
  (entry-spec {:form '(defn sym [:__global :const :float :* a
                                 :__global :const :float :* b
                                 :__global :float :* c])}
              '{c {:output true}})
  => '({:key :a, :symbol a, :type :float, :dsize 4, :buffer true, :const true, :input true}
       {:key :b, :symbol b, :type :float, :dsize 4, :buffer true, :const true, :input true}
       {:key :c, :symbol c, :type :float, :dsize 4, :buffer true, :const false, :output true}))

^{:refer hara.runtime.jocl.common/with-error :added "3.0"}
(fact "helper function for creation within context")

^{:refer hara.runtime.jocl.common/cl-context :added "3.0"}
(fact "creates a cl context"

  (cl-context)
  => org.jocl.cl_context)

^{:refer hara.runtime.jocl.common/cl-queue :added "3.0"}
(fact "creates a command queue (within a context)"

  (cl-queue (cl-context) (device:gpu))
  => org.jocl.cl_command_queue)

^{:refer hara.runtime.jocl.common/cl-program :added "3.0"}
(fact "creates a program from source"
  ^:hidden
  
  (cl-program (cl-context)
              (device:gpu)
              "__kernel void simple(__global const float * a){\n  int gid = get_global_id(0);\n}")
  => org.jocl.cl_program)

^{:refer hara.runtime.jocl.common/cl-kernel :added "3.0"}
(fact "creates a kernel from the program"
  ^:hidden
  
  (cl-kernel (cl-program (cl-context)
                         (device:gpu)
                         "__kernel void simple(__global const float * a){\n  int gid = get_global_id(0);\n}")
             "simple")
  => org.jocl.cl_kernel)
