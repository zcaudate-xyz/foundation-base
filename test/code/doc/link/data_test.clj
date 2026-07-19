(ns code.doc.link.data-test
  (:require [code.doc.link.data :as data])
  (:use code.test))

^{:refer code.doc.link.data/select-items :added "4.1"}
(fact "selects entries by group, only and exclude"

  (data/select-items [{:name "a" :group :x}
                      {:name "b" :group :y}
                      {:name "c" :group :x}]
                     {:group :x})
  => [{:name "a" :group :x} {:name "c" :group :x}]

  (data/select-items [{:name "a"} {:name "b"}] {:only ["a"]})
  => [{:name "a"}]

  (data/select-items [{:name "a"} {:name "b"}] {:exclude ["b"]})
  => [{:name "a"}]

  (data/select-items [{:label "x" :group :doc} {:label "y" :group :project}]
                     {:group :project})
  => [{:label "y" :group :project}])

^{:refer code.doc.link.data/ns-group :added "4.1"}
(fact "groups namespaces by prefix"

  (data/ns-group 'jvm.monitor)
  => "jvm"

  (data/ns-group 'std.lib.collection)
  => "std.lib"

  (data/ns-group 'std.concurrent.queued)
  => "std.concurrent"

  (data/ns-group 'hara.lang)
  => "hara.lang")

^{:refer code.doc.link.data/attach-items :added "4.1"}
(fact "attaches items or an error when empty"

  (data/attach-items {:type :related :group :x}
                     [{:name "a" :group :x}])
  => {:type :related :group :x :items [{:name "a" :group :x}]}

  (data/attach-items {:type :related :group :z}
                     [{:name "a" :group :x}])
  => (contains {:type :related :error string?}))
