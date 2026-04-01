(ns xt.lang.base-lib-test (:require [std.lang :as l] [std.string.prose :as prose]) (:use code.test))

^{:refer xt.lang.base-lib/id-fn, :added "4.0"} (fact "gets the id for an object")

^{:refer xt.lang.base-lib/key-fn, :added "4.0"} (fact "creates a key access function")

^{:refer xt.lang.base-lib/eq-fn, :added "4.0"} (fact "creates a eq comparator function")

^{:refer xt.lang.base-lib/inc-fn, :added "4.0"} (fact "creates a increment function by closure")

^{:refer xt.lang.base-lib/step-nil, :added "4.0"} (fact "nil step for fold")

^{:refer xt.lang.base-lib/step-thrush, :added "4.0"} (fact "thrush step for fold")

^{:refer xt.lang.base-lib/step-call, :added "4.0"} (fact "call step for fold")

^{:refer xt.lang.base-lib/step-push, :added "4.0"} (fact "step to push element into arr")

^{:refer xt.lang.base-lib/step-set-key, :added "4.0"} (fact "step to set key in object")

^{:refer xt.lang.base-lib/step-set-pair, :added "4.0"} (fact "step to set key value pair in object")

^{:refer xt.lang.base-lib/step-del-key, :added "4.0"} (fact "step to delete key in object")

^{:refer xt.lang.base-lib/sort, :added "4.0"} (fact "dumb version of arr-sort")

^{:refer xt.lang.base-lib/sort-edges-visit, :added "4.0"} (fact "walks over the list of edges")

^{:refer xt.lang.base-lib/clone-nested-loop, :added "4.0"} (fact "clone nested objects loop")

^{:refer xt.lang.base-lib/proto-spec, :added "4.0"}
(fact "creates the spec map from interface definitions")

^{:refer xt.lang.base-lib/with-delay, :added "4.0"} (fact "sets a delay")

^{:refer xt.lang.base-lib/meta:info-fn, :added "4.0"} (fact "the function to get meta info")

^{:refer xt.lang.base-lib/meta:info, :added "4.0"} (fact "macro to inject meta info")

^{:refer xt.lang.base-lib/LOG!, :added "4.0"} (fact "logging with meta info")

^{:refer xt.lang.base-lib/trace-run, :added "4.0"} (fact "run helper for `RUN!` macro")

^{:refer xt.lang.base-lib/str-rand, :added "4.0"} (fact "generates a random string of length n")

