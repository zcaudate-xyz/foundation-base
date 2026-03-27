(ns std.lang.typed.xtalk-intrinsic-test
  (:use code.test)
  (:require [std.lang.typed.xtalk-common :as types]
            [std.lang.typed.xtalk-intrinsic :refer :all]))

(defn callbacks
  []
  {:result (fn [type errors]
             {:type type :errors (vec errors)})
   :infer-type (fn [form _]
                 {:type (case form
                          numbers {:kind :array :item types/+int-type+}
                          pair {:kind :tuple :types [types/+str-type+ types/+int-type+]}
                          types/+str-type+)
                  :errors (if (= form :bad) [{:tag :bad-arg}] [])})
   :resolve-type (fn [type _] type)
   :arrayify-type (fn [type _] {:kind :array :item type})
   :infer-obj-assign (fn [_ _] {:type {:kind :record :fields [{:name "merged" :type types/+bool-type+ :optional? false}]} :errors []})
   :infer-make-container (fn [_ _] {:type {:kind :named :name 'sample/Container} :errors []})
   :infer-blank-container (fn [_ _] {:type {:kind :named :name 'sample/BlankContainer} :errors []})})

^{:refer std.lang.typed.xtalk-intrinsic/intrinsic-sym :added "4.1"}
(fact "builds intrinsic namespace symbols"
  (intrinsic-sym "const-fn")
  => 'std.lang.typed.xtalk-intrinsic/const-fn)

^{:refer std.lang.typed.xtalk-intrinsic/intrinsic-result :added "4.1"}
(fact "uses callback result builder"
  (intrinsic-result (callbacks) types/+str-type+ [{:tag :x}])
  => '{:type {:kind :primitive :name :xt/str}
       :errors [{:tag :x}]})

^{:refer std.lang.typed.xtalk-intrinsic/unary-bool :added "4.1"}
(fact "infers unary bool outputs"
  (:type (unary-bool '(intrinsic flag) {} (callbacks)))
  => '{:kind :primitive :name :xt/bool})

^{:refer std.lang.typed.xtalk-intrinsic/unary-int :added "4.1"}
(fact "infers unary int outputs"
  (:type (unary-int '(intrinsic count) {} (callbacks)))
  => '{:kind :primitive :name :xt/int})

^{:refer std.lang.typed.xtalk-intrinsic/str-returning :added "4.1"}
(fact "returns strings for string builders"
  (:type (str-returning '(intrinsic "a" "b") {} (callbacks)))
  => '{:kind :primitive :name :xt/str})

^{:refer std.lang.typed.xtalk-intrinsic/array-of-strings :added "4.1"}
(fact "returns arrays of strings"
  (:type (array-of-strings '(intrinsic "a" "b") {} (callbacks)))
  => '{:kind :array :item {:kind :primitive :name :xt/str}})

^{:refer std.lang.typed.xtalk-intrinsic/obj-keys :added "4.1"}
(fact "models object key lists as string arrays"
  (:type (obj-keys '(intrinsic route) {} (callbacks)))
  => '{:kind :array :item {:kind :primitive :name :xt/str}})

^{:refer std.lang.typed.xtalk-intrinsic/arrayify :added "4.1"}
(fact "arrayifies inferred input types"
  (:type (arrayify '(intrinsic "x") {} (callbacks)))
  => '{:kind :array :item {:kind :primitive :name :xt/str}})

^{:refer std.lang.typed.xtalk-intrinsic/nth-like :added "4.1"}
(fact "reads nth-like values from arrays and tuples"
  [(-> (nth-like '(intrinsic numbers) {} (callbacks) 0) :type)
   (-> (nth-like '(intrinsic pair) {} (callbacks) 1) :type)]
  => '[{:kind :maybe :item {:kind :primitive :name :xt/int}}
        {:kind :primitive :name :xt/int}])

^{:refer std.lang.typed.xtalk-intrinsic/first-item :added "4.1"}
(fact "returns first tuple item"
  (:type (first-item '(intrinsic pair) {} (callbacks)))
  => '{:kind :primitive :name :xt/str})

^{:refer std.lang.typed.xtalk-intrinsic/second-item :added "4.1"}
(fact "returns second tuple item"
  (:type (second-item '(intrinsic pair) {} (callbacks)))
  => '{:kind :primitive :name :xt/int})

^{:refer std.lang.typed.xtalk-intrinsic/const-fn :added "4.1"}
(fact "creates zero-arg constant functions"
  (:type (const-fn '(intrinsic "ok") {} (callbacks)))
  => '{:kind :fn
       :inputs []
       :output {:kind :primitive :name :xt/str}})

^{:refer std.lang.typed.xtalk-intrinsic/obj-assign :added "4.1"}
(fact "delegates object assign inference"
  (:type (obj-assign '(intrinsic left right) {} (callbacks)))
  => '{:kind :record
       :fields [{:name "merged" :type {:kind :primitive :name :xt/bool} :optional? false}]})

^{:refer std.lang.typed.xtalk-intrinsic/make-container :added "4.1"}
(fact "delegates container construction inference"
  (:type (make-container '(intrinsic init type opts) {} (callbacks)))
  => '{:kind :named :name sample/Container})

^{:refer std.lang.typed.xtalk-intrinsic/blank-container :added "4.1"}
(fact "delegates blank container inference"
  (:type (blank-container '(intrinsic type opts) {} (callbacks)))
  => '{:kind :named :name sample/BlankContainer})

^{:refer std.lang.typed.xtalk-intrinsic/infer-intrinsic :added "4.1"}
(fact "dispatches intrinsic rules by symbol"
  [(-> (infer-intrinsic '(std.lang.typed.xtalk-intrinsic/arrayify numbers) {} (callbacks)) :type)
   (infer-intrinsic '(std.lang.typed.xtalk-intrinsic/missing numbers) {} (callbacks))]
  => '[{:kind :array :item {:kind :array :item {:kind :primitive :name :xt/int}}}
        nil])
