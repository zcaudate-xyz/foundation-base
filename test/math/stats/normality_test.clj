(ns math.stats.normality-test
  (:require [math.stats.normality :refer :all])
  (:use code.test))

^{:refer math.stats.normality/anderson-darling :added "3.0"}
(fact "Performs the Anderson-Darling test for normality"

  (anderson-darling [0. 0. 0. 0. 0. 0. 0. 100. 100. 100. 100. 100. 100.])
  => 2.1947640982019347)

^{:refer math.stats.normality/cramer-vonmises :added "3.0"}
(fact "Performs the Cramer-vonMises test for normality"

  (cramer-vonmises [0. 0. 0. 0. 0. 0. 0. 100. 100. 100. 100. 100. 100.])
  => 0.3625667515860767)

^{:refer math.stats.normality/jarque-bera :added "3.0"}
(fact "Performs the Jarque-Bera test for normality"

  (jarque-bera [0. 0. 0. 0. 0. 0. 0. 100. 100. 100. 100. 100. 100.])
  => 2.1669737339380206)

^{:refer math.stats.normality/kolmogorov-lilliefors :added "3.0"}
(fact "Performs the Kolmogorov-Lilliefors test for normality"

  (-> [0. 0. 0. 0. 0. 0. 0. 100. 100. 100. 100. 100. 100.]
      kolmogorov-lilliefors)
  => 0.3515941066052153)

^{:refer math.stats.normality/kolmogorov-smirnov :added "3.0"}
(fact "Performs the Kolmogorov-Smirnov test for normality"

  (-> [0. 0. 0. 0. 0. 0. 0. 100. 100. 100. 100. 100. 100.]
      (kolmogorov-smirnov)
      seq)
  => (contains [0.5 (approx 7.86899E-4)]))

^{:refer math.stats.normality/shapiro-francia :added "3.0"}
(fact "Performs the Shapiro-Francia test for normality"

  (-> [0. 0. 0. 0. 0. 0. 0. 100. 100. 100. 100. 100. 100.]
      shapiro-francia)
  => 0.6756836312861318)

^{:refer math.stats.normality/shapiro-wilk :added "3.0"}
(fact "Performs the Shapiro-Wilk test for normality"

  (-> [0. 0. 0. 0. 0. 0. 0. 100. 100. 100. 100. 100. 100.]
      shapiro-wilk)
  => 0.6457043943928288)

(comment

  (lucid.unit/import))
