(ns std.lib.stream-test
  (:use code.test)
  (:require [std.lib.stream :refer :all]
            [std.lib.function :as fn]
            [std.lib.stream.xform :as x])
  (:import (java.util.stream Collectors)))

^{:refer std.lib.stream/produce :added "3.0"}
(fact "produces a seq from an object"

  (produce (range 5))
  => '(0 1 2 3 4))

^{:refer std.lib.stream/collect :added "3.0"}
(fact "collection function given a seq supply"

  (collect [] (range 5))
  => [0 1 2 3 4])

^{:refer std.lib.stream/seqiter :added "3.0"}
(fact "creates an non-chunking iterator from a transducer (sink)"

  (seqiter (map inc)
           (range 5))
  => (range 1 6))

^{:refer std.lib.stream/unit :added "3.0"}
(fact "applies a transducer to single input"

  (unit (map inc) 1)
  => 2)

^{:refer std.lib.stream/extend-stream-form :added "3.0"}
(fact "extend protocols for a type"

  (extend-stream-form [nil '{:produce 'produce-nil
                             :collect 'collect-nil}])
  => any?)

^{:refer std.lib.stream/extend-stream :added "3.0"}
(fact "extends the stream protocol for a map of types"
  (resolve 'extend-stream) => var?)

^{:refer std.lib.stream/add-transforms :added "3.0"}
(fact "adds a transform to the list"

  (add-transforms :map x/x:map)
  => '(:map))

^{:refer std.lib.stream/pipeline-transform :added "3.0"}
(fact "transforms a pipeline to transducer form"

  (pipeline-transform [[:map inc]
                       [:map inc]
                       [:window 5]])
  => (contains [fn? fn? fn?]))

^{:refer std.lib.stream/pipeline :added "3.0"}
(fact "creates a transducer pipeline"

  (pipeline [[:map inc]
             [:map inc]
             [:window 5]])
  => fn?)

^{:refer std.lib.stream/pipe :added "3.0"}
(fact "pipes data from one source into another"

  (pipe (range 5)
        [[:map inc]
         [:map inc]]
        ())
  => '(6 5 4 3 2))

^{:refer std.lib.stream/producer :added "3.0"}
(fact "creates a source seq"

  (producer (range 5)
            [[:map inc]])
  => seq?)

^{:refer std.lib.stream/collector :added "3.0"}
(fact "creates a collection function"

  (collector [[:map inc]]
             [])
  => fn? ^:hidden

  (pipe (producer (range 5)
                  [[:map inc]])
        []
        (collector [[:map inc]]
                   []))
  => [2 3 4 5 6]

  ((collector [[:map inc]]
              [])
   (producer (range 5)
             [[:map inc]]))
  => [2 3 4 5 6]

  ((collector [[:map inc]]
              [])
   [[:map inc]]
   (producer (range 5)
             [[:map inc]]))
  => [3 4 5 6 7])

^{:refer std.lib.stream/<*> :added "3.0"}
(fact "denoting a missing connector"

  (<*>) => :stream/<*>)

^{:refer std.lib.stream/stream :added "3.0"}
(fact "constructs a stream operation" ^:hidden

  ;; pipe action
  (stream (range 5)
          [:map inc]
          [:map inc]
          [])
  => [2 3 4 5 6]

  ;; producer
  (stream (range 5)
          [:map inc]
          [:map inc]
          (<*>))
  => seq?


  ;; collector


  (stream (<*>)
          [:map inc]
          [:map inc])
  => fn?

  ;; stage
  (stream (<*>)
          [:map inc]
          [:map inc]
          (<*>))
  => fn?)

^{:refer std.lib.stream/*> :added "3.0"}
(fact "shortcut for `stream`"

  (*> (range 5)
      (x/x:map inc)
      [])
  => [1 2 3 4 5])

^{:refer std.lib.stream/produce-nil :added "3.0"}
(fact "produce for nil"

  (produce-nil nil) => ())

^{:refer std.lib.stream/produce-object :added "3.0"}
(fact "default implementation of produce"

  (produce-object 1) => '(1))

^{:refer std.lib.stream/produce-ifn :added "3.0"}
(fact "produce for functions"

  (produce-ifn (fn [] 1)) => '(1)

  (produce-ifn (fn [] (range 5)))
  => '(0 1 2 3 4))

^{:refer std.lib.stream/collect-nil :added "3.0"}
(fact "collect for nil"

  (collect-nil nil (map inc)
               (range 5))
  => seq?)

^{:refer std.lib.stream/collect-ifn :added "3.0"}
(fact "collect outputs to a source"

  (-> (collect-ifn int-array (map inc)
                   (range 5))
      seq)
  => '(1 2 3 4 5))

^{:refer std.lib.stream/produce-callable :added "3.0"}
(fact "produce for callable"

  (produce-callable (fn [] 1))
  => '(1))

^{:refer std.lib.stream/produce-supplier :added "3.0"}
(fact "produce for supplier"

  (produce-supplier (fn/fn:supplier [] 1))
  => 1)

^{:refer std.lib.stream/primitive-form :added "3.0"}
(fact "creates the primitive forms"

  (primitive-form '["[Z"
                    {:produce seq :collect collect-booleans}]))

^{:refer std.lib.stream/gen:primitives :added "3.0"}
(fact "generate primitive forms"

  (produce (byte-array 5))
  => '(0 0 0 0 0))

^{:refer std.lib.stream/collect-transient :added "3.0"}
(fact "collect for transients"

  (-> (collect-transient (transient [])
                         (map inc)
                         (range 5))
      (persistent!))
  => [1 2 3 4 5])

^{:refer std.lib.stream/collect-collection :added "3.0"}
(fact "collect for java collections"

  (-> (collect-collection (java.util.ArrayList.)
                          (map inc)
                          (range 5)))
  => [1 2 3 4 5])

^{:refer std.lib.stream/to-stream :added "3.0"}
(fact "converts a seq to a stream")

^{:refer std.lib.stream/produce-stream :added "3.0"}
(fact "produces values from a stream"

  (->> (to-stream (range 5))
       (produce-stream)
       (map inc))
  => '(1 2 3 4 5))

^{:refer std.lib.stream/collect-collector :added "3.0"}
(fact "collects values from seq given a collector"

  (collect-collector (Collectors/toList)
                     (map inc)
                     (range 5))
  => [1 2 3 4 5])

^{:refer std.lib.stream/produce-deref :added "3.0"}
(fact "produces from a deref"

  (produce-deref (volatile! 1))
  => '(1))

^{:refer std.lib.stream/collect-promise :added "3.0"}
(fact "collects given a promise"

  @(doto (promise)
     (collect (map inc) (range 5)))
  => 1)

^{:refer std.lib.stream/atom-seq :added "3.0"}
(fact "constructs an atom-seq"

  (take 5 (atom-seq (atom -1) inc))
  => '(0 1 2 3 4))

^{:refer std.lib.stream/object-seq :added "3.0"}
(fact "constructs an object-seq"

  (take 5 (object-seq (volatile! -1)
                      #(vswap! % inc)))
  => '(0 1 2 3 4))
