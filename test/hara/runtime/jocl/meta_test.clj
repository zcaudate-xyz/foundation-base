tahto/runtime/jocl/meta_test.clj:1:(ns tahto.runtime.jocl.meta-test
  (:refer-clojure :exclude [to-array])
  (:use code.test)
  (:require [std.lib.foundation :as h]
tahto/runtime/jocl/meta_test.clj:5:            [tahto.runtime.jocl :refer :all]
tahto/runtime/jocl/meta_test.clj:6:            [tahto.runtime.jocl.env :as jocl-env]))

(jocl-env/with-stubs cl-size cl-types lookup:fn lookup:flags lookup:build
                     array-form to-ptr cl-call fn-call info-template:defn
                     info-template:print info-template:gen info-template
                     info-gen list-platforms platform:default list-devices
                     device-info device:cpu device:gpu)

(fact:global
 {:skip (not (jocl-env/opencl-available?))})

tahto/runtime/jocl/meta_test.clj:17:^{:refer tahto.runtime.jocl.meta/cl-size :added "3.0"}
(fact "helper for cl-types"

  (cl-size "float")
  => '(org.jocl.Sizeof/float))

tahto/runtime/jocl/meta_test.clj:23:^{:refer tahto.runtime.jocl.meta/cl-types :added "3.0"}
(fact "creates a lookup of all standard cl sizes"

  (cl-types))

tahto/runtime/jocl/meta_test.clj:28:^{:refer tahto.runtime.jocl.meta/lookup:fn :added "3.0"}
(fact "builds a lookup function"
  
  ((lookup:fn {:a 1 :b 2}) 1)
  => :a)

tahto/runtime/jocl/meta_test.clj:34:^{:refer tahto.runtime.jocl.meta/lookup:flags :added "3.0"}
(fact "builds a flags function"

  ((lookup:flags {:hello 1 :world 2 :foo 4 :bar 8}) 13)
  => #{:bar :hello :foo})

tahto/runtime/jocl/meta_test.clj:40:^{:refer tahto.runtime.jocl.meta/lookup:build :added "3.0"}
(fact "builds a lookup for the metadata"

  ((lookup:build [:enum :device-type]) [4])
  => :gpu)

tahto/runtime/jocl/meta_test.clj:46:^{:refer tahto.runtime.jocl.meta/array-form :added "3.0"}
(fact "creates a form given inputs"

  (array-form :int [1 2 3])
  => '(int-array [1 2 3])

  (array-form 'Object [1 2 3])
  => '(clojure.core/make-array Object [1 2 3]))

tahto/runtime/jocl/meta_test.clj:55:^{:refer tahto.runtime.jocl.meta/to-ptr :added "3.0"}
(fact "pointer for array inputs")

tahto/runtime/jocl/meta_test.clj:58:^{:refer tahto.runtime.jocl.meta/cl-call :added "3.0"}
(fact "creates a cl call given parameters"
  ^:hidden

  (cl-call
   {:sizet :long,
    :pointer true,
    :return h/string,
    :arrayt :byte,
    :prefix "CL_PLATFORM",
    :fn org.jocl.CL/clGetPlatformInfo,
    :key :platform,
    :class cl_platform_id}
   (platform:default)
   (jocl-env/cl-field "CL_PLATFORM_NAME"))
  => string?)

tahto/runtime/jocl/meta_test.clj:75:^{:refer tahto.runtime.jocl.meta/fn-call :added "3.0"}
(fact "creates a cl call given a lookup key"
  ^:hidden
  
  (fn-call :platform-info {} (platform:default) (jocl-env/cl-field "CL_PLATFORM_NAME"))
  => string?)

tahto/runtime/jocl/meta_test.clj:82:^{:refer tahto.runtime.jocl.meta/info-template:defn :added "3.0"}
(fact "creates a property template"
  ^:hidden

  (info-template:defn "platform" "CL_PLATFORM" [:name {}])
  => '[:name (defn platform-info:name
               ([platform]
                (fn-call :platform-info {} platform org.jocl.CL/CL_PLATFORM_NAME)))])

tahto/runtime/jocl/meta_test.clj:91:^{:refer tahto.runtime.jocl.meta/info-template:print :added "3.0"}
(fact "creates a print template"
  ^:hidden
  
  (info-template:print :platform 'cl_platform_id [])
  => '(clojure.core/defmethod
        clojure.core/print-method cl_platform_id
        ([obj w] (.write w (clojure.core/str "#cl." "platform" " "
                                             (clojure.core/dissoc (platform-info obj)))))))

tahto/runtime/jocl/meta_test.clj:101:^{:refer tahto.runtime.jocl.meta/info-template:gen :added "3.0"}
(fact "helper for `info-template`"

  (info-template:gen :platform nil))

tahto/runtime/jocl/meta_test.clj:106:^{:refer tahto.runtime.jocl.meta/info-template :added "3.0"}
(fact "generates the info functions")

tahto/runtime/jocl/meta_test.clj:109:^{:refer tahto.runtime.jocl.meta/info-gen :added "3.0"}
(fact "generates multiple info functions")

tahto/runtime/jocl/meta_test.clj:112:^{:refer tahto.runtime.jocl.meta/list-platforms :added "3.0"}
(fact "lists all platforms"

  (list-platforms))

tahto/runtime/jocl/meta_test.clj:117:^{:refer tahto.runtime.jocl.meta/platform:default :added "3.0"}
(fact "gets the default platform"

  (platform:default))

tahto/runtime/jocl/meta_test.clj:122:^{:refer tahto.runtime.jocl.meta/list-devices :added "3.0"}
(fact "lists all devices"

  (list-devices (platform:default)))

tahto/runtime/jocl/meta_test.clj:127:^{:refer tahto.runtime.jocl.meta/device-info :added "3.0"}
(fact "shows the device info"
  ^:hidden
  
  (device-info (device:cpu))
  => (any map?
          (throws)))

tahto/runtime/jocl/meta_test.clj:135:^{:refer tahto.runtime.jocl.meta/device:cpu :added "3.0"}
(fact "gets the default cpu device")

tahto/runtime/jocl/meta_test.clj:138:^{:refer tahto.runtime.jocl.meta/device:gpu :added "3.0"}
(fact "gets the default gpu device")
