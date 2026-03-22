(ns math.stats.distribution-test
  (:require [math.stats.distribution :refer :all])
  (:use code.test)
  (:refer-clojure :exclude [random-sample]))

^{:refer math.stats.distribution/fn-name :added "3.0"}
(fact "creates a function name based on class"

  (fn-name net.sourceforge.jdistlib.evd.Order)
  => "order")

^{:refer math.stats.distribution/create-distribution-form :added "3.0"}
(fact "creates the `defn` form for a particular distribution"

  (create-distribution-form [net.sourceforge.jdistlib.Wilcoxon [:m :n]])
  => '(defn wilcoxon [m n]
        (net.sourceforge.jdistlib.Wilcoxon. m n)))

^{:refer math.stats.distribution/create-distribution-forms :added "3.0"}
(fact "helper for `create-distributions`"

  (create-distribution-forms)
  => vector?)

^{:refer math.stats.distribution/create-distributions :added "3.0"}
(comment "creates all methods for constructing distributions"

  (create-distributions))

^{:refer math.stats.distribution/random-sample :added "3.0"}
(fact "gives a random sample from distribution"

  (random-sample (laplace 1 2))
  ;; 1.9528692108205805
  => number?)

^{:refer math.stats.distribution/probability :added "3.0"}
(fact "returns the probability density for the distribution"

  (probability (laplace 1 2) 4)
  => 0.05578254003710745

  (probability (binomial 10 0.3) [1 2 3])
  => (contains [(approx 0.1210608)
                (approx 0.2334744)
                (approx 0.2668279)]))

^{:refer math.stats.distribution/cumulative :added "3.0"}
(fact "returns the cumulative density for the distribution"

  (cumulative (laplace 1 2) 4)
  => 0.888434919925785

  (cumulative (binomial 10 0.3) [1 2 3])
  => [0.14930834590000003 0.38278278639999974 0.6496107184000002])

(comment

  (require 'lucid.unit)
  (lucid.unit/import)

  (require '[lucid.mind :refer :all])
  (.? Ansari "density" :static))
