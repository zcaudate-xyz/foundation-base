(ns std.lang.model.spec-rust-more-test
  (:use code.test)
  (:require [std.lang.model.spec-rust :refer :all]
            [std.lang.base.script :as script]
            [std.lang.base.util :as ut]
            [std.lang :as l]
            [std.lib :as h]
            [std.fs :as fs]
            [std.string]))

(script/script- :rust
  {;;:require [[std :as h]]
   :import  [["std::mem"]]
   })

(defstruct.rs ^{:% ["#[allow(dead_code)]"
                    "#[derive(Debug, Clone, Copy)]"]}
  Point
  [[:f64 x]
   [:f64 y]])

(defstruct.rs ^{:% [["#[allow(dead_code)]"]]}
  Rectangle
  [[:% -/Point top-left]
   [:% -/Point bottom-right]])

(defn.rs ^{:- [-/Point]}
  origin
  []
  (new -/Point :x 0.0 :y 0.0))

(defn.rs ^{:- [[:> Box -/Point]]}
  origin-boxed
  []
  ($ Box "new" (new -/Point :x 0.0 :y 0.0)))

(defn.rs ^{:- [[:> Box -/Point]]}
  main
  []
  (var :% -/Point     point (-/origin))
  (var :% -/Rectangle rect
       (new -/Rectangle
            :top-left (-/origin)
            :bottom-right (new -/Point :x 3.0 :y -4.0))))


(fact "compiles"
  1 => 1)

