(ns example.build.cache-brain-test
  (:require [example.build.cache-brain :as build])
  (:use code.test))

(fact "declares the example cache/brain specializations"
  (mapv :id build/+brain-specializations+)
  => [:brain-custom :brain-local :brain-redis])

(fact "exposes a std.make project for the example build"
  (select-keys @(:instance build/PROJECT)
               [:tag :build])
  => {:tag "example.cache-brain"
      :build ".build/example-cache-brain"})
