(ns std.object.framework.proxy-test
  (:require [std.object :as object])
  (:use code.test)
  (:import (test.flight Airline Body Body$BodyType Plane Radar Tail Tail$TailFlap Wing) (java.util List)))

(object/map-like
 Tail
 {:tag "tail"
  :write {:methods :class
          :construct {:fn :reflect
                      :default [{:label ""} "white"]
                      :params [:flap :color]}}})

(object/map-like
 Tail$TailFlap
 {:tag "flap"
  :write {:methods :class
          :construct {:fn (fn [label] (Tail$TailFlap. nil label))
                      :params [:label]}}})

(object/map-like
 Wing
 {:tag  "wing"
  :write {:empty :reflect
          :methods :class}})

(object/map-like
 Body
 {:tag  "body"
  :read  {:custom {:a {:fn (fn [i] [])
                       :type List}}
          :methods :class}
  :write {:construct {:fn :reflect
                      :params [:type :wings :tail]}
          :methods :class
          :custom  {:wings {:element  Wing}}}})

(object/map-like
 Plane
 {:tag  "plane"
  :proxy {:body [:wings :tail]}
  :write {:methods :class
          :construct {:fn :reflect
                      :params [:body]}}})

(comment
  (object/meta-read Wing)
  (object/meta-read Plane)
  (object/meta-write Plane)

  (object/meta-read Body)

  (object/meta-write Tail)
  (object/meta-write Wing)
  (object/meta-write-exact Body)

  (object/from-data {:color "yellow"} Wing)

  (object/from-data {:color "yellow"} Wing)

  (-> (object/from-data {:type :small
                         :tail {:color "white"
                                :flap {:label "ericsson"}}
                         :wings [{:color "blue"}]}
                        Body)
      (.getWings))

  (-> (object/from-data {:body {:type :small
                                :tail {:color "white"
                                       :flap {:label "ericsson"}}
                                :wings [{:color "blue"}]}
                         :name "hello"}
                        Plane)
      (object/get [:wings :tail]))

  (.getWings (object/from-data {:wings [(object/from-data {:color "blue"} Wing)]}
                               Body))

  (object/from-data {:color "white"
                     :flap {:label "ericsson"}}
                    Tail)

  (object/from-data {}
                    Tail)

  (object/from-data {:label "ericsson"}
                    Tail$TailFlap)
  (object/query-class Tail$TailFlap ["new"])
  (object/meta-write Tail$TailFlap)

  (object/from-data :small Body$BodyType)
  (object/to-data Body$BodyType/SMALL)
  (object/meta-write Body)
  (object/meta-read Tail$TailFlap)
  (object/meta-read Tail)
  (object/meta-write Tail)
  (object/meta-read Body)

  (comment
    (object/from-data {:color "ee"} Tail)

    (Tail.)
    (./reset)
    (./javac '[test.flight.Plane]
             {:reload true})
    (./javac '[test.flight.Tail]
             {:reload true})))
