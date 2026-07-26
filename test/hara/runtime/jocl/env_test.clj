tahto/runtime/jocl/env_test.clj:1:(ns tahto.runtime.jocl.env-test
  (:use code.test)
tahto/runtime/jocl/env_test.clj:3:  (:require [tahto.runtime.jocl.env :refer :all]))

tahto/runtime/jocl/env_test.clj:5:^{:refer tahto.runtime.jocl.env/opencl-available? :added "4.1"}
(fact "returns a boolean indicating OpenCL availability"
  (boolean? (opencl-available?))
  => true)

tahto/runtime/jocl/env_test.clj:10:^{:refer tahto.runtime.jocl.env/cl-field :added "4.1"}
(fact "reads a static field from org.jocl.CL, or returns nil when unavailable"
  (if (opencl-available?)
    (some? (cl-field "CL_DEVICE_TYPE_GPU"))
    true)
  => true)

tahto/runtime/jocl/env_test.clj:17:^{:refer tahto.runtime.jocl.env/with-stubs :added "4.1"}
(fact "expands based on OpenCL availability"
  (let [expanded (macroexpand-1 '(with-stubs foo bar))]
    (or (nil? expanded)
        (and (seq? expanded)
             (= 'do (first expanded)))))
  => true)

tahto/runtime/jocl/env_test.clj:25:^{:refer tahto.runtime.jocl.env/with-script-stubs :added "4.1"}
(fact "expands based on OpenCL availability"
  (let [expanded (macroexpand-1 '(with-script-stubs))]
    (or (nil? expanded)
        (and (seq? expanded)
             (= 'do (first expanded)))))
  => true)

tahto/runtime/jocl/env_test.clj:33:^{:refer tahto.runtime.jocl.env/when-available :added "4.1"}
(fact "expands based on OpenCL availability"
  (let [expanded (macroexpand-1 '(when-available 1 2 3))]
    (or (nil? expanded)
        (and (seq? expanded)
             (= 'do (first expanded)))))
  => true)