(ns std.lib.impl-test
  (:use code.test)
  (:require [std.lib.impl :refer :all]
            [std.protocol.dispatch :as protocol.dispatch]))

(defprotocol ITest
  (-val [obj] [obj k])
  (-get [obj]))

(defprotocol IMulti
  (^:variadic -mul [obj] [obj ks]))

(defn seed-fn [{:keys [function]}]
  (fn [& args]
    (apply function args)))

^{:refer std.lib.impl/split-body :added "3.0"}
(fact "splits a body depending on keyword pairs"

  (split-body [:a 1 :b 2 '[hello] '[there]])
  => [{:a 1, :b 2} '([hello] [there])])

^{:refer std.lib.impl/split-single :added "3.0"}
(fact "splits out a given entry" ^:hidden

  (split-single '[ITest
                  :include [-test]
                  :body {-val 3}
                  IMore
                  IProtocols
                  :exclude []])
  => '[{:include [-test],
        :body {-val 3},
        :tag ITest}
       (IMore IProtocols :exclude [])])

^{:refer std.lib.impl/split-all :added "3.0"}
(fact "splits all entries"

  (split-all '[ITest
               :include [-test]
               :body {-val 3}
               IMore
               IProtocols
               :exclude []]
             :protocol
             {:prefix "test/"})
  => '[{:prefix "test/", :include [-test], :body {-val 3}, :protocol ITest}
       {:prefix "test/", :protocol IMore}
       {:prefix "test/", :exclude [], :protocol IProtocols}])

^{:refer std.lib.impl/impl:unwrap-sym :added "3.0"}
(fact "unwraps the protocol symbol"

  (impl:unwrap-sym {:name '-val :prefix "test/" :suffix "-mock"})
  => 'test/val-mock)

^{:refer std.lib.impl/impl:wrap-sym :added "3.0"}
(fact "wraps the protocol symbol"

  (impl:wrap-sym {:protocol 'protocol.test/ITest :name '-val})
  => 'protocol.test/-val)

^{:refer std.lib.impl/standard-body-input-fn :added "3.0"}
(fact "creates a standard input function"
  ((standard-body-input-fn nil) {:arglist '[cache val] :name '-val})
  => '[cache val])

^{:refer std.lib.impl/standard-body-output-fn :added "3.0"}
(fact "creates a standard output function"
  ((standard-body-output-fn {}) {:arglist '[cache val] :name '-val})
  => '(-val cache val))

^{:refer std.lib.impl/create-body-fn :added "3.0"}
(fact "creates a body function"

  ((create-body-fn {:body-sym-fn impl:unwrap-sym})
   {:name '-val :arglist '[cache val] :prefix "test/"})
  => '([cache val] (test/val cache val)))

^{:refer std.lib.impl/template-signatures :added "3.0"}
(fact "finds template signatures for a protocol" ^:hidden

  (defprotocol IHello
    (^:notify -hi [cache] [cache a0] [cache a0 a1])
    (^:variadic -delete [cache keys]))

  (template-signatures 'IHello)
  => '({:notify true, :tag nil, :name -hi, :doc nil, :arglist [cache]}
       {:notify true, :tag nil, :name -hi, :doc nil, :arglist [cache a0]}
       {:notify true,
        :tag nil,
        :name -hi,
        :doc nil,
        :arglist [cache a0 a1]}
       {:variadic true,
        :tag nil,
        :name -delete,
        :doc nil,
        :arglist [cache keys]}))

^{:refer std.lib.impl/template-transform :added "3.0"}
(fact "transforms all functions" ^:hidden

  (template-transform
   (filter #(-> % :name (= '-delete))
           (template-signatures 'IHello {:prefix "impl/"}))
   {:body-sym-fn impl:unwrap-sym})
  => '([-delete ([cache keys] (impl/delete cache keys))])

  (template-transform
   (filter #(-> % :name (= '-hi))
           (template-signatures 'IHello {:prefix "impl/"}))
   {:body-output-fn #(get '{-hi "Hi There"} (:name %))})
  => '([-hi ([cache] "Hi There")]
       [-hi ([cache a0] "Hi There")]
       [-hi ([cache a0 a1] "Hi There")]))

^{:refer std.lib.impl/parse-impl :added "3.0"}
(fact "parses the different transform types"

  (parse-impl (template-signatures 'IHello {:prefix "impl/"})
              {:body '{-hi "Hi There"}})
  => '{:method #{},
       :body #{-hi},
       :default #{-delete}})

^{:refer std.lib.impl/template-gen :added "3.0"}
(fact "generates forms given various formats"

  (template-gen protocol-fns [:default]
                (template-signatures 'ITest) {} {})
  => '([-val ([obj] (val obj))]
       [-val ([obj k] (val obj k))]
       [-get ([obj] (get obj))]) ^:hidden

  (template-gen protocol-fns [:default]
                (template-signatures 'ITest {:prefix "hi-"})
                {}
                {:template-fn dimpl-template-fn})
  => '((-val [obj] (hi-val obj))
       (-val [obj k] (hi-val obj k))
       (-get [obj] (hi-get obj)))

  (template-gen protocol-fns [:default]
                (template-signatures 'ITest {:prefix "hi-"})
                {}
                {:template-fn eimpl-template-fn})
  => '((-val ([obj] (hi-val obj))
             ([obj k] (hi-val obj k)))
       (-get ([obj] (hi-get obj)))))

^{:refer std.lib.impl/protocol-fns :added "3.0"}
(fact "helpers for protocol forms"

  (protocol-fns :body {})
  => (contains {:body-output-fn fn?}))

^{:refer std.lib.impl/dimpl-template-fn :added "3.0"}
(fact "helper function for defimpl" ^:hidden

  (dimpl-template-fn '([-val ([obj] (val obj))]
                       [-val ([obj k] (val obj k))]
                       [-get ([obj] (get obj))]))
  => '((-val [obj] (val obj))
       (-val [obj k] (val obj k))
       (-get [obj] (get obj))))

^{:refer std.lib.impl/dimpl-template-protocol :added "3.0"}
(fact "coverts the entry into a template"

  (dimpl-template-protocol {:protocol 'ITest :prefix "impl/" :suffix "-test"})
  => '(ITest (-val [obj] (impl/val-test obj))
             (-val [obj k] (impl/val-test obj k))
             (-get [obj] (impl/get-test obj))))

^{:refer std.lib.impl/interface-fns :added "3.0"}
(fact "helper for interface forms"

  (interface-fns :body {})
  => (contains {:body-output-fn fn?}))

^{:refer std.lib.impl/dimpl-template-interface :added "3.0"}
(fact "creates forms for the interface"

  (dimpl-template-interface {:interface 'ITest
                             :method '{invoke {[entry] submit-invoke}}
                             :body   '{bulk? {[entry] false}}})
  => '(ITest (invoke [entry] (submit-invoke entry))
             (bulk? [entry] false)))

^{:refer std.lib.impl/dimpl-print-method :added "3.0"}
(fact "creates a print method form"
  (dimpl-print-method 'Type)
  => (any nil? seq?))

^{:refer std.lib.impl/dimpl-fn-invoke :added "3.0"}
(fact "creates an invoke method"
  (dimpl-fn-invoke 'invoke 2)
  => '(invoke [obj a0 a1] (invoke obj a0 a1)))

^{:refer std.lib.impl/dimpl-fn-forms :added "3.0"}
(fact "creates the `IFn` forms"
  (dimpl-fn-forms 'invoke)
  => (contains '[clojure.lang.IFn]))

^{:refer std.lib.impl/dimpl-form :added "3.0"}
(fact "helper for `defimpl`" ^:hidden

  (dimpl-form 'Test '[]
              '[:prefix "common/"
                :suffix "-test"
                :string common/to-string
                :interfaces [clojure.lang.IFn
                             :method {invoke {[executor entry] submit-executor}}]
                :protocols  [ITest
                             protocol.dispatch/IDispatch
                             :method  {-submit submit-executor}
                             :body  {-bulk?  false}]])
  => (contains '(clojure.core/deftype Test [])))

^{:refer std.lib.impl/defimpl :added "3.0"}
(fact "creates a high level `deftype` or `defrecord` interface"
  (defimpl Test [])
  => (contains '(clojure.core/deftype Test [])))

^{:refer std.lib.impl/eimpl-template-fn :added "3.0"}
(fact "creates forms compatible with `extend-type` and `extend-protocol`"

  (eimpl-template-fn '([-val ([obj] (val obj))]
                       [-val ([obj k] (val obj k))]
                       [-get ([obj] (get obj))]))
  => '((-val ([obj] (val obj))
             ([obj k] (val obj k)))
       (-get ([obj] (get obj)))))

^{:refer std.lib.impl/eimpl-template-protocol :added "3.0"}
(fact "helper for eimpl-form"

  (eimpl-template-protocol {:protocol 'ITest :prefix "impl/" :suffix "-test"})
  => '(ITest
       (-val ([obj] (impl/val-test obj))
             ([obj k] (impl/val-test obj k)))
       (-get ([obj] (impl/get-test obj)))))

^{:refer std.lib.impl/eimpl-print-method :added "3.0"}
(fact "creates a print method form"
  (eimpl-print-method 'Type "string")
  => seq?)

^{:refer std.lib.impl/eimpl-form :added "3.0"}
(fact "creates the extend-impl form" ^:hidden

  (eimpl-form
   'clojure.lang.Atom
   '[:prefix "common/"
     :suffix "-test"
     :protocols  [ITest
                  protocol.dispatch/IDispatch
                  :method  {-submit +}
                  :body  {-bulk?  false}]])
  => seq?)

^{:refer std.lib.impl/extend-impl
  :added "3.0"
  :style/indent 1}
(fact "extends a class with the protocols"
  (extend-impl String [:prefix "string/"])
  => seq?)

^{:refer std.lib.impl/build-with-opts-fn :added "3.0"}
(fact "builds a function with an optional component"
  (build-with-opts-fn 'test '((test [x] 1)))
  => seq?)

^{:refer std.lib.impl/build-variadic-fn :added "3.0"}
(fact "builds a variadic function if indicated"
  (build-variadic-fn 'test '((test [x] 1)))
  => seq?)

^{:refer std.lib.impl/build-template-fn :added "3.0"}
(fact "contructs a template from returned vals with support for variadic"

  ((build-template-fn {}) '([-val ([obj] (val obj))]
                            [-val ([obj k] (val obj k))]
                            [-get ([obj] (get obj))]))
  => '((clojure.core/defn val
         ([obj] (val obj))
         ([obj k] (val obj k)))
       (clojure.core/defn get ([obj] (get obj))))

  ((build-template-fn {:variadic '#{-mul}}) '([-mul ([obj ks] (mul obj ks))]))
  => '((clojure.core/defn
         mul
         ([obj & ks] (mul obj ks)))))

^{:refer std.lib.impl/build-template-protocol :added "3.0"}
(fact "helper for build"

  (build-template-protocol '{:protocol ITest
                             :outer {:suffix "-outer"}
                             :inner {:prefix "inner-"}
                             :fns {:default {:body-sym-fn impl:unwrap-sym}}})

  => '((clojure.core/defn val-outer
         ([obj] (inner-val obj))
         ([obj k] (inner-val obj k)))
       (clojure.core/defn get-outer ([obj] (inner-get obj)))) ^:hidden

  (dimpl-template-protocol {:prefix "test/"
                            :suffix "-hello"
                            :protocol 'std.protocol.dispatch/IDispatch})
  => '(std.protocol.dispatch/IDispatch
       (-submit [dispatch entry] (test/submit-hello dispatch entry))
       (-bulk? [dispatch] (test/bulk?-hello dispatch)))
  
  (dimpl-template-interface {:prefix "test/"
                             :suffix "-hello"
                             :interface 'clojure.lang.IFn
                             :body  '{-invoke {[cache arg] (submit cache arg)}}})
  => '(clojure.lang.IFn (-invoke [cache arg] (submit cache arg)))

  (dimpl-template-interface {:prefix "test/"
                             :suffix "-hello"
                             :interface 'clojure.lang.IFn
                             :method  '{-invoke {[cache arg] submit
                                                 [cache a0 a1] submit}}})
  => '(clojure.lang.IFn
       (-invoke [cache arg] (submit cache arg))
       (-invoke [cache a0 a1] (submit cache a0 a1))))

^{:refer std.lib.impl/build-form :added "3.0"}
(fact "allows multiple forms to be built"
  (build-form '[ITest])
  => seq?)

^{:refer std.lib.impl/build-impl :added "3.0" :style/indent 1}
(fact "build macro for generating functions from protocols"
  (build-impl {:prefix "test/"} ITest)
  => seq?)

^{:refer std.lib.impl/impl:proxy :added "3.0"}
(fact "creates a proxy template given a symbol"

  ((impl:proxy :<state>)
   '[(-add-channel ([mq] (add-channel-atom mq) mq))])
  => '((-add-channel [mq] (add-channel-atom :<state>) mq)))

^{:refer std.lib.impl/impl:doto :added "3.0"}
(fact "operates on a proxy and returns object"

  ((impl:doto :<state>)
   '[(-add-channel ([mq] (add-channel-atom mq)))])
  => '((-add-channel [mq] (do (add-channel-atom :<state>) mq))))
