(ns math.stats.distribution
  (:require [std.string :as str]
            [std.object.query :as query])
  (:import net.sourceforge.jdistlib.generic.GenericDistribution
           [net.sourceforge.jdistlib
            Ansari Arcsine Beta BetaBinomial
            BetaPrime Binomial Cauchy Chi ChiSquare
            Exponential F Gamma Geometric HyperGeometric
            InvGamma InvNormal Kendall Kumaraswamy
            Laplace Levy LogNormal Logarithmic Logistic
            Nakagami NegBinomial NonCentralBeta NonCentralChiSquare
            NonCentralF NonCentralT Normal
            Poisson SignRank SkewedT Spearman
            T Tukey Uniform Weibull Wilcoxon Wishart Zipf]
           [net.sourceforge.jdistlib.evd
            Extreme Fretchet GeneralizedPareto
            GEV Gumbel Order Rayleigh ReverseWeibull])
  (:refer-clojure :exclude [random-sample]))

(defonce +distributions-map+
  {Ansari          [:m :n]
   Arcsine         [:a :b]
   Beta            [:a :b]
   BetaBinomial    [:mu :sigma :bd]
   BetaPrime       [:a :b]
   Binomial        [:n :p]
   Cauchy          [:location :scale]
   Chi             [:df]
   ChiSquare       [:df]
   Exponential     [:scale]
   Extreme         [:dist :mlen :largest]
   F               [:df1 :df2]
   Fretchet        [:loc :scale :shape]
   Gamma           [:scale :shape]
   GeneralizedPareto [:loc :scale :shape]
   Geometric       [:p]
   GEV             [:loc :scale :shape]
   Gumbel          [:loc :scale]
   HyperGeometric  [:r :b :n]
   InvGamma        [:shape :scale]
   InvNormal       [:mu :sigma]
   Kendall         [:n]
   Kumaraswamy     [:a :b]
   Laplace         [:location :scale]
   Levy            [:mu :sigma]
   Logarithmic     [:mu]
   Logistic        [:location :scale]
   LogNormal       [:meanlog :sdlog]
   Nakagami        [:m :omega]
   NegBinomial     [:prob :size]
   NonCentralBeta  [:a :b :ncp]
   NonCentralChiSquare [:df :ncp]
   NonCentralF     [:df1 :df2 :ncp]
   NonCentralT     [:df :ncp]
   Normal          [:mu :sigma]
   Order           [:dist :mlen :j :largest]
   Poisson         [:lambda]
   Rayleigh        [:scale]
   ReverseWeibull  [:loc :scale :shape]
   SignRank        [:n]
   SkewedT         [:df :gamma]
   Spearman        [:n]
   T               [:df]
   Tukey           [:rr :cc :df]
   Uniform         [:a :b]
   Weibull         [:shape :scale]
   Wilcoxon        [:m :n]
   Zipf            [:N :s]})

(defn fn-name
  "creates a function name based on class
 
   (fn-name net.sourceforge.jdistlib.evd.Order)
   => \"order\""
  {:added "3.0"}
  ([cls]
   (str/spear-case (last (str/split (.getName cls) #"\.")))))

(defmethod print-method GenericDistribution
  ([v w]
   (let [cls  (type v)
         data (select-keys (into {} (query/delegate v))
                           (+distributions-map+ cls))]
     (.write w (str "#" (fn-name cls) data)))))

(defn create-distribution-form
  "creates the `defn` form for a particular distribution
 
   (create-distribution-form [net.sourceforge.jdistlib.Wilcoxon [:m :n]])
   => '(defn wilcoxon [m n]
         (net.sourceforge.jdistlib.Wilcoxon. m n))"
  {:added "3.0"}
  ([[cls params]]
   (let [fn-sym  (symbol (fn-name cls))
         params (mapv (comp symbol name) params)
         cstr   (apply list (symbol (str (.getName cls) ".")) params)]
     (list 'defn fn-sym params cstr))))

(defn create-distribution-forms
  "helper for `create-distributions`
 
   (create-distribution-forms)
   => vector?"
  {:added "3.0"}
  ([] (create-distribution-forms +distributions-map+))
  ([dists]
   (mapv create-distribution-form dists)))

(defmacro create-distributions
  "creates all methods for constructing distributions
 
   (create-distributions)"
  {:added "3.0"}
  ([] (create-distribution-forms +distributions-map+)))

(create-distributions)

(defonce +distributions+
  (sort-by (comp :name meta) (create-distributions)))

(defn random-sample
  "gives a random sample from distribution
 
   (random-sample (laplace 1 2))
   ;; 1.9528692108205805
   => number?"
  {:added "3.0"}
  ([dist]
   (.random dist)))

(defn probability
  "returns the probability density for the distribution
 
   (probability (laplace 1 2) 4)
   => 0.05578254003710745
 
   (probability (binomial 10 0.3) [1 2 3])
   => (contains [(approx 0.1210608)
                 (approx 0.2334744)
                 (approx 0.2668279)])"
  {:added "3.0"}
  ([dist x]
   (cond (number? x)
         (.density dist (double x) false)

         (coll? x)
         (vec (.density dist (double-array x)))

         :else
         (throw (ex-info "Unknown type." {:input x})))))

(defn cumulative
  "returns the cumulative density for the distribution
 
   (cumulative (laplace 1 2) 4)
   => 0.888434919925785
 
   (cumulative (binomial 10 0.3) [1 2 3])
   => [0.14930834590000003 0.38278278639999974 0.6496107184000002]"
  {:added "3.0"}
  ([dist x]
   (cond (number? x)
         (.cumulative dist (double x))

         (coll? x)
         (vec (.cumulative dist (double-array x)))

         :else
         (throw (ex-info "Unknown type." {:input x})))))
