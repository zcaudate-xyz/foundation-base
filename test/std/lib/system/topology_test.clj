(ns std.lib.system.topology-test
  (:require [std.lib.system.topology :refer :all])
  (:use code.test))

^{:refer std.lib.system.topology/long-form-imports :added "3.0"}
(fact "converts short form imports to long form"

  (long-form-imports [:db [:file {:as :fs}]])
  => {:db   {:type :single, :as :db},
      :file {:type :single, :as :fs}}

  (long-form-imports [[:ids {:type :element :as :id}]])
  => {:ids {:type :element, :as :id}})

^{:refer std.lib.system.topology/long-form-entry :added "3.0"}
(fact "converts short form entry into long form" ^:hidden

  (long-form-entry [{:constructor :identity
                     :initialiser :identity}])
  => {:type :build
      :compile :single
      :constructor :identity
      :initialiser :identity
      :import {}, :dependencies ()}

  (long-form-entry [[identity]])
  => (contains {:compile :array,
                :type :build,
                :constructor fn?
                :import {},
                :dependencies ()})

  (long-form-entry [[identity] [:model {:as :raw}] [:ids {:type :element :as :id}]])
  => (contains {:compile :array,
                :type :build
                :constructor fn?
                :import {:model {:type :single, :as :raw},
                         :ids {:type :element, :as :id}},
                :dependencies [:model :ids]}))

^{:refer std.lib.system.topology/long-form :added "3.0"}
(fact "converts entire topology to long form" ^:hidden

  (long-form {:db [identity]
              :count [{:expose :count} :db]})
  => (contains-in {:db {:compile :single,
                        :type :build,
                        :constructor fn?,
                        :import {},
                        :dependencies ()},
                   :count {:compile :single,
                           :type :expose,
                           :in :db,
                           :function :count,
                           :dependencies [:db]}}))

^{:refer std.lib.system.topology/get-dependencies :added "3.0"}
(fact "get dependencies for long form" ^:hidden
  (-> (long-form {:model   [identity]
                  :ids     [[identity]]
                  :traps   [[identity] [:model {:as :raw}] [:ids {:type :element :as :id}]]
                  :entry   [identity :model :ids]
                  :nums    [[{:expose :id}] :traps]
                  :model-tag  [{:expose :tag
                                :setup identity}  :model]})
      get-dependencies)
  => {:model #{},
      :ids #{},
      :traps #{:ids :model},
      :entry #{:ids :model},
      :nums #{:traps},
      :model-tag #{:model}})

^{:refer std.lib.system.topology/get-exposed :added "3.0"}
(fact "get exposed keys for long form" ^:hidden
  (-> (long-form {:model   [identity]
                  :ids     [[identity]]
                  :traps   [[identity] [:model {:as :raw}] [:ids {:type :element :as :id}]]
                  :entry   [identity :model :ids]
                  :nums    [[{:expose :id}] :traps]
                  :model-tag  [{:expose :tag
                                :setup identity}  :model]})
      get-exposed)
  => [:nums :model-tag])

^{:refer std.lib.system.topology/all-dependencies :added "3.0"}
(fact "gets all dependencies for long form" ^:hidden

  (all-dependencies
   {1 #{4 2}
    2 #{3}
    3 #{5}
    4 #{}
    5 #{6}
    6 #{}})
  => {1 #{2 3 4 5 6}
      2 #{3 5 6}
      3 #{5 6}
      4 #{}
      5 #{6}
      6 #{}}

  (-> (long-form {:model   [identity]
                  :ids     [[identity]]
                  :traps   [[identity] [:model {:as :raw}] [:ids {:type :element :as :id}]]
                  :entry   [identity :model :ids]
                  :nums    [[{:expose :id}] :traps]
                  :model-tag  [{:expose :tag
                                :setup identity}  :model]})
      get-dependencies
      all-dependencies)
  => {:model #{},
      :ids #{},
      :traps #{:ids :model},
      :entry #{:ids :model},
      :nums #{:ids :traps :model},
      :model-tag #{:model}})

(comment
  (./import))
