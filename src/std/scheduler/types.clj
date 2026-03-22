(ns std.scheduler.types
  (:require [std.contract :as c :refer [defspec]]
            [std.lib.foundation]))

(def +runtime+
  (-> {:type        '(:basic :constant)
       :policy      '(:first :last)
       :interval    (comp not neg-int?)
       :delay       (comp not neg-int?)}
      (c/lax [:delay])))

(def +spawn+
  (c/lax {:max         pos-int?
          :min         pos-int?}))

(def +program+
  (c/lax {:full        boolean?
          :global      boolean?
          :state       std.lib.foundation/atom?
          :args-fn     (c/fn [])
          :create-fn   (c/fn [])
          :time-fn     (c/fn [t])
          :merge-fn    (c/fn [aggregate result])
          :continue-fn (c/fn [props])
          :success-fn  (c/fn [result])
          :error-fn    (c/fn [exception])}))

(defspec <program>
  {:id          keyword?
   :main-fn     (c/fn [args t])}
  +runtime+
  +spawn+
  +program+)
