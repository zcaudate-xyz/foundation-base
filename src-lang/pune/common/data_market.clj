(ns pune.common.data-market
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :xtalk
  {:require [[xt.lang.spec-base :as xt]
             [xt.lang.common-lib :as lib]
             [xt.lang.common-data :as data]
             [xt.lang.common-string :as string]
             [xt.lang.common-math :as math]
             [xt.lang.common-tree :as tree]
             [xt.lang.common-sort-by :as sort-by]
             [xt.lang.common-trace :as trace]]
   :export [MODULE]})

(defn.xt price-to-float
  "converts price to float"
  {:added "4.0"}
  [price]
  (if (lib/is-string? price)
    (return (lib/to-number price))
    (return price)))

(defn.xt frac-to-decimal
  "converts frac to decimal"
  {:added "4.0"}
  [frac]
  (return (xt/x:m-floor (+ 0.5 (- (xt/x:m-log10 frac))))))

(defn.xt decimal-to-frac
  "converts decimal to frac"
  {:added "4.0"}
  [decimal]
  (return (xt/x:m-pow 10 (- decimal))))

(defn.xt position-to-rate
  "converts position to rate"
  {:added "0.1"}
  [prediction
   allotment
   position]
  (return (:? (== prediction "yes") position (- allotment position))))

(defn.xt position-to-price
  "converts position to price"
  {:added "4.0"}
  [prediction
   allotment
   frac
   decimal
   position]
  (return (string/to-fixed (:? (== prediction "yes") (* position frac) (* (- allotment position) frac))
                      decimal)))

(defn.xt price-to-position
  "converts price to position"
  {:added "4.0"}
  [prediction
   allotment
   frac
   price]
  (if (== prediction "yes")
    (return (xt/x:m-floor (+ 0.5 (/ (-/price-to-float price) frac))))
    (return (- allotment (xt/x:m-floor (+ 0.5  (/ (-/price-to-float price) frac)))))))

(defn.xt book-enrich
  "gets the book max value"
  {:added "4.0"}
  ([book]
   (var #{decimal allotment} book)
   (var frac (xt/x:m-pow 10 (- decimal)))
   (var max  (* allotment frac))
   (return (data/obj-assign {:frac frac
                          :max  max}
                         book))))

(defn.xt live-summary
  "creates a summary of the book value"
  {:added "4.0"}
  ([live book]
   (var #{frac max decimal allotment} book)
   (var #{ask bid} live)
   (var all-pos (data/arr-mapcat [(or (xt/x:get-key bid "volume") [])
                               (or (xt/x:get-key ask "volume") [])]
                              lib/identity))
   (var volume (data/arr-foldl all-pos
                            (fn:> [acc e] (+ acc (or (data/second e)
                                                     0)))
                            0))
   (var ask-pos  (data/first  (or (xt/x:get-key ask "range") [])))
   (var bid-pos  (data/second (or (xt/x:get-key bid "range") [])))
   (var yes-sell (:? ask-pos (string/to-fixed (* frac ask-pos) decimal) "-"))
   (var yes-buy  (:? bid-pos (string/to-fixed (* frac bid-pos) decimal) "-"))
   (var no-sell  (:? bid-pos (string/to-fixed (- max (* frac bid-pos)) decimal) "-"))
   (var no-buy   (:? ask-pos (string/to-fixed (- max (* frac ask-pos)) decimal) "-"))
   
   (return {:no-sell no-sell
            :no-buy no-buy
            :yes-sell yes-sell
            :yes-buy yes-buy
            :volume volume
            :ask-pos ask-pos
            :bid-pos bid-pos})))

(defn.xt live-offers-raw
  "gets the raw offers"
  {:added "0.1"}
  [live
   prediction
   retrieve]
  (:= retrieve (or retrieve 7))
  (var #{ask bid} live)
  (var avol (data/arr-sort (or (xt/x:get-key ask "volume") [])
                        data/first lib/lt))
  (var bvol (data/arr-sort (or (xt/x:get-key bid "volume") [])
                        data/first lib/lt))
  (var buy-offers    (:? (== prediction "yes")
                         (data/arr-rslice avol 0 (Math.min retrieve (xt/x:len avol)))
                         (data/arr-slice bvol (Math.max 0 (- (xt/x:len bvol) retrieve)) (xt/x:len bvol))))
  (var sell-offers  (:? (== prediction "yes")
                        (data/arr-rslice bvol (Math.max 0 (- (xt/x:len bvol) retrieve)) (xt/x:len bvol))
                        (data/arr-slice avol 0 (Math.min retrieve (xt/x:len avol)))))
  (return {:buy  buy-offers
           :sell sell-offers}))

(defn.xt live-offers-rate
  "gets the raw offers"
  {:added "0.1"}
  [live
   allotment 
   prediction
   retrieve]
  (var rate-fn
       (fn [pair]
         (var [pos vol] pair)
         (return [(-/position-to-rate prediction allotment pos)
                  vol])))
  (var raw (-/live-offers-raw live
                              prediction
                              retrieve))
  (var #{buy sell} raw)
  (return {:buy  (data/arr-map buy rate-fn)
           :sell (data/arr-map sell rate-fn)}))

(defn.xt live-offers-price
  "converts live positions to price offers (reverse order)"
  {:added "4.0"}
  [live
   book 
   prediction
   retrieve]
  (var #{frac max decimal allotment} book)
  (var price-fn
       (fn [pair]
         (var [pos vol] pair)
         (return [(-/position-to-price prediction allotment frac decimal pos)
                  vol])))
  (var raw (-/live-offers-raw live
                              prediction
                              retrieve))
  (var #{buy sell} raw)
  (return {:buy  (data/arr-map buy price-fn)
           :sell (data/arr-map sell price-fn)}))

(defn.xt segment-price
  "classifies order price given live offers"
  {:added "4.0"}
  ([price offers]
   (:= price (-/price-to-float price))
   (var #{buy sell} offers)
   (var higher-than  (fn:> [offers]
                       (> price
                          (lib/to-number (data/first (data/first offers))))))
   (var lower-than   (fn:> [offers]
                       (< price
                          (lib/to-number (data/first (data/last offers))))))
   (cond (and (== 0 (xt/x:len buy))
              (== 0 (xt/x:len sell)))
         (return "center")

         (== 0 (xt/x:len sell))
         (cond (higher-than buy)
               (return "top")

               (lower-than buy)
               (return "center")

               :else
               (return "none"))

         (== 0 (xt/x:len buy))
         (cond (higher-than sell)
               (return "center")

               (lower-than sell)
               (return "bottom")

               :else
               (return "none"))

         :else
         (cond (higher-than buy)
               (return "top")

               (lower-than sell)
               (return "bottom")

               (and (lower-than buy)
                    (higher-than sell))
               (return "center")

               :else
               (return "none")))))

(defn.xt position-can-trade
  "relative positions for trading"
  {:added "4.0"}
  ([pos trade prediction
    book
    summary]
   (var #{allotment} book)
   (var #{ask-pos bid-pos} summary)
   (if (== trade "buy")
     (return (:? (== prediction "yes")
                 (>= pos (or ask-pos allotment))
                 (>= pos (- allotment (or bid-pos 0)))))
     (return (:? (== prediction "yes")
                 (<= pos (or bid-pos 0))
                 (<= pos (- allotment (or ask-pos allotment))))))))

(defn.xt position-estimate
  "gets the relative positions for estimate"
  {:added "4.0"}
  ([trade prediction
    book
    summary]
   (var #{allotment} book)
   (var #{ask-pos bid-pos} summary)
   (return
    (:? (== trade "buy") (:? (== prediction "yes")
                             (or ask-pos bid-pos (/ allotment 2))
                             (- allotment (or bid-pos ask-pos (/ allotment 2))))
        (:? (== prediction "yes")
            (or bid-pos ask-pos (/ allotment 2))
            (- allotment (or ask-pos bid-pos (/ allotment 2))))))))

(defn.xt price-can-trade
  "checks that the current price can be traded"
  {:added "4.0"}
  ([price trade prediction
    book
    summary]
   (var #{allotment frac} book)
   (var pos (xt/x:m-floor (+ 0.5  (/ (-/price-to-float price) frac))))
   (return (-/position-can-trade pos trade prediction
                                 book
                                 summary))))

(defn.xt price-estimate
  "gets the price estimate"
  {:added "4.0"}
  [trade prediction
   book
   summary]
  (var #{decimal frac} book)
  (return
   (string/to-fixed (* frac (-/position-estimate trade prediction
                                            book
                                            summary))
               decimal)))

(defn.xt calc-rake
  "calculates the rake"
  {:added "4.0"}
  ([rake type-key value-key frac amount spend]
   (let [type  (xt/x:get-key rake type-key)
         value (xt/x:get-key rake value-key)])
   (cond (== type "none")         (return 0)
         (== type "per_trade")    (return (* value frac))
         (== type "per_contract") (return (* value amount frac))
         (== type "percentage")   (return (* spend value 0.01)))))

(def.xt MODULE (!:module))
