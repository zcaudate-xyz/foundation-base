(ns hara.runtime.jocl.meta-test
  (:refer-clojure :exclude [to-array])
  (:use code.test)
  (:require [std.lib.foundation :as h]
            [hara.runtime.jocl :refer :all]
            [hara.runtime.jocl.env :as jocl-env]))

(jocl-env/with-stubs cl-size cl-types lookup:fn lookup:flags lookup:build
                     array-form to-ptr cl-call fn-call info-template:defn
                     info-template:print info-template:gen info-template
                     info-gen list-platforms platform:default list-devices
                     device-info device:cpu device:gpu)

(fact:global
 {:skip (not (jocl-env/opencl-available?))})

^{:refer hara.runtime.jocl.meta/cl-size :added "3.0"}
(fact "helper for cl-types"

  (cl-size "float")
  => '(org.jocl.Sizeof/float))

^{:refer hara.runtime.jocl.meta/cl-types :added "3.0"}
(fact "creates a lookup of all standard cl sizes"

  (cl-types))

^{:refer hara.runtime.jocl.meta/lookup:fn :added "3.0"}
(fact "builds a lookup function"
  
  ((lookup:fn {:a 1 :b 2}) 1)
  => :a)

^{:refer hara.runtime.jocl.meta/lookup:flags :added "3.0"}
(fact "builds a flags function"

  ((lookup:flags {:hello 1 :world 2 :foo 4 :bar 8}) 13)
  => #{:bar :hello :foo})

^{:refer hara.runtime.jocl.meta/lookup:build :added "3.0"}
(fact "builds a lookup for the metadata"

  ((lookup:build [:enum :device-type]) [4])
  => :gpu)

^{:refer hara.runtime.jocl.meta/array-form :added "3.0"}
(fact "creates a form given inputs"

  (array-form :int [1 2 3])
  => '(int-array [1 2 3])

  (array-form 'Object [1 2 3])
  => '(clojure.core/make-array Object [1 2 3]))

^{:refer hara.runtime.jocl.meta/to-ptr :added "3.0"}
(fact "pointer for array inputs")

^{:refer hara.runtime.jocl.meta/cl-call :added "3.0"}
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

^{:refer hara.runtime.jocl.meta/fn-call :added "3.0"}
(fact "creates a cl call given a lookup key"
  ^:hidden
  
  (fn-call :platform-info {} (platform:default) (jocl-env/cl-field "CL_PLATFORM_NAME"))
  => string?)

^{:refer hara.runtime.jocl.meta/info-template:defn :added "3.0"}
(fact "creates a property template"
  ^:hidden

  (info-template:defn "platform" "CL_PLATFORM" [:name {}])
  => '[:name (defn platform-info:name
               ([platform]
                (fn-call :platform-info {} platform org.jocl.CL/CL_PLATFORM_NAME)))])

^{:refer hara.runtime.jocl.meta/info-template:print :added "3.0"}
(fact "creates a print template"
  ^:hidden
  
  (info-template:print :platform 'cl_platform_id [])
  => '(clojure.core/defmethod
        clojure.core/print-method cl_platform_id
        ([obj w] (.write w (clojure.core/str "#cl." "platform" " "
                                             (clojure.core/dissoc (platform-info obj)))))))

^{:refer hara.runtime.jocl.meta/info-template:gen :added "3.0"}
(fact "helper for `info-template`"

  (info-template:gen :platform nil))

^{:refer hara.runtime.jocl.meta/info-template :added "3.0"}
(fact "generates the info functions")

^{:refer hara.runtime.jocl.meta/info-gen :added "3.0"}
(fact "generates multiple info functions")

^{:refer hara.runtime.jocl.meta/list-platforms :added "3.0"}
(fact "lists all platforms"

  (list-platforms))

^{:refer hara.runtime.jocl.meta/platform:default :added "3.0"}
(fact "gets the default platform"

  (platform:default))

^{:refer hara.runtime.jocl.meta/list-devices :added "3.0"}
(fact "lists all devices"

  (list-devices (platform:default)))

^{:refer hara.runtime.jocl.meta/device-info :added "3.0"}
(fact "shows the device info"
  ^:hidden
  
  (device-info (device:cpu))
  => (any map?
          (throws)))

^{:refer hara.runtime.jocl.meta/device:cpu :added "3.0"}
(fact "gets the default cpu device")

^{:refer hara.runtime.jocl.meta/device:gpu :added "3.0"}
(fact "gets the default gpu device")
