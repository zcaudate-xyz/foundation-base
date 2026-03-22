(ns math.stats.kmeans-test
  (:require [math.stats.kmeans :refer :all])
  (:use code.test))

^{:refer math.stats.kmeans/distance2 :added "3.0"}
(fact "sum of squared distances between two points"

  (distance2 [1 3 5] [2 4 6])
  => 3)

^{:refer math.stats.kmeans/min-index :added "3.0"}
(fact "finds the index of the centroid closest to `p0`"

  (min-index [0 0] [[-1 -1] [0.5 0.5] [1 1]])
  => 1)

^{:refer math.stats.kmeans/cluster :added "3.0"}
(fact "takes an array centroids and groups points based upon closest distance"

  (cluster [[0 0] [1 1] [2 2] [3 3] [4 4] [5 5] [6 6]]
           [[1 1] [4 4]])
  => [[[0 0] [1 1] [2 2]]
      [[3 3] [4 4] [5 5] [6 6]]])

^{:refer math.stats.kmeans/centroid :added "3.0"}
(fact "finds the central position of  a group of points"

  (centroid [[3 3] [4 4] [5 5] [6 6]])
  => [9/2 9/2])

^{:refer math.stats.kmeans/initial-centroids :added "3.0"}
(comment "finds a set of centroids from the initial set of points")

^{:refer math.stats.kmeans/k-means :added "3.0"}
(fact "clusters points together based on the number of groups specified"

  (set (k-means [[0.2 0.4] [0.2 0.3] [0.3 0.4]
                 [1.2 0.4] [1.1 0.3] [2.3 0.4]]
                2))
  => (any #{[[0.2 0.4] [0.2 0.3] [0.3 0.4]]
            [[1.2 0.4] [1.1 0.3] [2.3 0.4]]}
          #{[[0.2 0.4] [0.2 0.3] [0.3 0.4] [1.2 0.4] [1.1 0.3]]
            [[2.3 0.4]]}))

(comment

  (require 'lucid.unit)
  (lucid.unit/import))
