(ns std.timeseries.types
  (:require [std.contract :as c :refer [defspec defmultispec defcase]]))

(defspec <range-point>
  [:or
   number?
   keyword?
   inst?])

(defspec <range>
  [:or [:= :all]
   [:tuple
    (c/as:schema  <range-point>)
    (c/as:schema <range-point>)]])

(defspec <time>
  {:key keyword?
   :unit  '(:ns :us :ms :s :m :h :d)
   :order '(:asc :desc)
   :format string?})

(defspec <journal>
  {:id   [:or string? keyword?]
   :meta (c/lax {:hide set?
                 :entry (c/lax {:flatten boolean? :pre-flattened boolean?})
                 :head  (c/lax {:range <range>})
                 :time  (c/lax <time> [:key :unit :order :format])})}
  (c/lax {:limit number?
          :template volatile?
          :entries  [:or list? vector?]
          :previous [:or list? vector?]}))

(defspec <interval>
  [:or number? keyword?])

(defspec <sample>
  [:or  (c/as:schema <range>)
   number?
   keyword?
   vector?
   map?])

(defspec <series>
  [:or
   keyword?
   vector?
   list?
   map?])

(defspec <compute> map?)

(defspec <transform-opt>
  (c/lax {:aggregate [:or keyword? [:fn fn?]]
          :sample <sample>
          :fn [:fn fn?]}))

(defspec <transform-opt-custom>
  {:keys [:sequential keyword?]}
  <transform-opt>)

(defspec <transform>
  (c/lax {:interval <interval>
          :sample   <sample>
          :time     <transform-opt>
          :default  <transform-opt>
          :custom  [:sequential (c/as:schema <transform-opt-custom>)]
          :skip boolean?}))

(defspec <process>
  {:time <time>}
  (c/lax {:range <range>
          :sample <sample>
          :transform <transform>
          :compute <compute>
          :series <series>
          :template map?}))
