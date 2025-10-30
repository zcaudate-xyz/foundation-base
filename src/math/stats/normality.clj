(ns math.stats.normality
  (:import net.sourceforge.jdistlib.disttest.NormalityTest))

(defn anderson-darling
  "Performs the Anderson-Darling test for normality
 
   (anderson-darling [0. 0. 0. 0. 0. 0. 0. 100. 100. 100. 100. 100. 100.])
   => 2.1947640982019347"
  {:added "3.0"}
  ([arr]
   (NormalityTest/anderson_darling_statistic (double-array arr))))

(defn cramer-vonmises
  "Performs the Cramer-vonMises test for normality
 
   (cramer-vonmises [0. 0. 0. 0. 0. 0. 0. 100. 100. 100. 100. 100. 100.])
   => 0.3625667515860767"
  {:added "3.0"}
  ([arr]
   (NormalityTest/cramer_vonmises_statistic (double-array arr))))

(defn jarque-bera
  "Performs the Jarque-Bera test for normality
 
   (jarque-bera [0. 0. 0. 0. 0. 0. 0. 100. 100. 100. 100. 100. 100.])
   => 2.1669737339380206"
  {:added "3.0"}
  ([arr]
   (NormalityTest/jarque_bera_statistic (double-array arr))))

(defn kolmogorov-lilliefors
  "Performs the Kolmogorov-Lilliefors test for normality
 
   (-> [0. 0. 0. 0. 0. 0. 0. 100. 100. 100. 100. 100. 100.]
       kolmogorov-lilliefors)
   => 0.3515941066052153"
  {:added "3.0"}
  ([arr]
   (NormalityTest/kolmogorov_lilliefors_statistic (double-array arr))))

(defn kolmogorov-smirnov
  "Performs the Kolmogorov-Smirnov test for normality
 
   (-> [0. 0. 0. 0. 0. 0. 0. 100. 100. 100. 100. 100. 100.]
       (kolmogorov-smirnov)
       seq)
   => (contains [0.5 (approx 7.86899E-4)])"
  {:added "3.0"}
  ([arr]
   (NormalityTest/kolmogorov_smirnov_test (double-array arr))))

(defn shapiro-francia
  "Performs the Shapiro-Francia test for normality
 
   (-> [0. 0. 0. 0. 0. 0. 0. 100. 100. 100. 100. 100. 100.]
       shapiro-francia)
   => 0.6756836312861318"
  {:added "3.0"}
  ([arr]
   (NormalityTest/shapiro_francia_statistic (double-array arr))))

(defn shapiro-wilk
  "Performs the Shapiro-Wilk test for normality
 
   (-> [0. 0. 0. 0. 0. 0. 0. 100. 100. 100. 100. 100. 100.]
       shapiro-wilk)
   => 0.6457043943928288"
  {:added "3.0"}
  ([arr]
   (NormalityTest/shapiro_wilk_statistic (double-array arr) true)))

