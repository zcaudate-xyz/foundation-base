(ns math.stats
  (:require [math.stats.distribution :as distribution]
            [math.stats.normality :as normality]
            [std.lib.foundation :as f])
  (:refer-clojure :exclude [random-sample]))

(f/intern-in distribution/random-sample
             distribution/probability
             distribution/cumulative

             ;; -- distributions --

             distribution/ansari
             distribution/arcsine
             distribution/beta
             distribution/beta-binomial
             distribution/beta-prime
             distribution/binomial
             distribution/cauchy
             distribution/chi
             distribution/chi-square
             distribution/exponential
             distribution/extreme
             distribution/f
             distribution/fretchet
             distribution/gamma
             distribution/generalized-pareto
             distribution/geometric
             distribution/gev
             distribution/gumbel
             distribution/hyper-geometric
             distribution/inv-gamma
             distribution/inv-normal
             distribution/kendall
             distribution/kumaraswamy
             distribution/laplace
             distribution/levy
             distribution/log-normal
             distribution/logarithmic
             distribution/logistic
             distribution/nakagami
             distribution/neg-binomial
             distribution/non-central-beta
             distribution/non-central-chi-square
             distribution/non-central-f
             distribution/non-central-t
             distribution/normal
             distribution/order
             distribution/poisson
             distribution/rayleigh
             distribution/reverse-weibull
             distribution/sign-rank
             distribution/skewed-t
             distribution/spearman
             distribution/t
             distribution/tukey
             distribution/uniform
             distribution/weibull
             distribution/wilcoxon
             distribution/zipf

             normality/anderson-darling
             normality/cramer-vonmises
             normality/jarque-bera
             normality/kolmogorov-lilliefors
             normality/kolmogorov-smirnov
             normality/shapiro-francia
             normality/shapiro-wilk)
