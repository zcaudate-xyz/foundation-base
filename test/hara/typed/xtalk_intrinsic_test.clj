tahto/typed/xtalk_intrinsic_test.clj:1:(ns tahto.typed.xtalk-intrinsic-test
  (:use code.test)
tahto/typed/xtalk_intrinsic_test.clj:3:  (:require [tahto.typed.xtalk-common :as types]
tahto/typed/xtalk_intrinsic_test.clj:4:            [tahto.typed.xtalk-intrinsic :refer :all]))

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

tahto/typed/xtalk_intrinsic_test.clj:22:^{:refer tahto.typed.xtalk-intrinsic/intrinsic-sym :added "4.1"}
(fact "builds intrinsic namespace symbols"
  (intrinsic-sym "const-fn")
tahto/typed/xtalk_intrinsic_test.clj:25:  => 'tahto.typed.xtalk-intrinsic/const-fn)

tahto/typed/xtalk_intrinsic_test.clj:27:^{:refer tahto.typed.xtalk-intrinsic/intrinsic-result :added "4.1"}
(fact "uses callback result builder"
  (intrinsic-result (callbacks) types/+str-type+ [{:tag :x}])
  => '{:type {:kind :primitive :name :xt/str}
       :errors [{:tag :x}]})

tahto/typed/xtalk_intrinsic_test.clj:33:^{:refer tahto.typed.xtalk-intrinsic/unary-bool :added "4.1"}
(fact "infers unary bool outputs"
  (:type (unary-bool '(intrinsic flag) {} (callbacks)))
  => '{:kind :primitive :name :xt/bool})

tahto/typed/xtalk_intrinsic_test.clj:38:^{:refer tahto.typed.xtalk-intrinsic/unary-int :added "4.1"}
(fact "infers unary int outputs"
  (:type (unary-int '(intrinsic count) {} (callbacks)))
  => '{:kind :primitive :name :xt/int})

tahto/typed/xtalk_intrinsic_test.clj:43:^{:refer tahto.typed.xtalk-intrinsic/str-returning :added "4.1"}
(fact "returns strings for string builders"
  (:type (str-returning '(intrinsic "a" "b") {} (callbacks)))
  => '{:kind :primitive :name :xt/str})

tahto/typed/xtalk_intrinsic_test.clj:48:^{:refer tahto.typed.xtalk-intrinsic/array-of-strings :added "4.1"}
(fact "returns arrays of strings"
  (:type (array-of-strings '(intrinsic "a" "b") {} (callbacks)))
  => '{:kind :array :item {:kind :primitive :name :xt/str}})

tahto/typed/xtalk_intrinsic_test.clj:53:^{:refer tahto.typed.xtalk-intrinsic/obj-keys :added "4.1"}
(fact "models object key lists as string arrays"
  (:type (obj-keys '(intrinsic route) {} (callbacks)))
  => '{:kind :array :item {:kind :primitive :name :xt/str}})

tahto/typed/xtalk_intrinsic_test.clj:58:^{:refer tahto.typed.xtalk-intrinsic/arrayify :added "4.1"}
(fact "arrayifies inferred input types"
  (:type (arrayify '(intrinsic "x") {} (callbacks)))
  => '{:kind :array :item {:kind :primitive :name :xt/str}})

tahto/typed/xtalk_intrinsic_test.clj:63:^{:refer tahto.typed.xtalk-intrinsic/nth-like :added "4.1"}
(fact "reads nth-like values from arrays and tuples"
  [(-> (nth-like '(intrinsic numbers) {} (callbacks) 0) :type)
   (-> (nth-like '(intrinsic pair) {} (callbacks) 1) :type)]
  => '[{:kind :maybe :item {:kind :primitive :name :xt/int}}
        {:kind :primitive :name :xt/int}])

tahto/typed/xtalk_intrinsic_test.clj:70:^{:refer tahto.typed.xtalk-intrinsic/first-item :added "4.1"}
(fact "returns first tuple item"
  (:type (first-item '(intrinsic pair) {} (callbacks)))
  => '{:kind :primitive :name :xt/str})

tahto/typed/xtalk_intrinsic_test.clj:75:^{:refer tahto.typed.xtalk-intrinsic/second-item :added "4.1"}
(fact "returns second tuple item"
  (:type (second-item '(intrinsic pair) {} (callbacks)))
  => '{:kind :primitive :name :xt/int})

tahto/typed/xtalk_intrinsic_test.clj:80:^{:refer tahto.typed.xtalk-intrinsic/const-fn :added "4.1"}
(fact "creates zero-arg constant functions"
  (:type (const-fn '(intrinsic "ok") {} (callbacks)))
  => '{:kind :fn
       :inputs []
       :output {:kind :primitive :name :xt/str}})

tahto/typed/xtalk_intrinsic_test.clj:87:^{:refer tahto.typed.xtalk-intrinsic/obj-assign :added "4.1"}
(fact "delegates object assign inference"
  (:type (obj-assign '(intrinsic left right) {} (callbacks)))
  => '{:kind :record
       :fields [{:name "merged" :type {:kind :primitive :name :xt/bool} :optional? false}]})

tahto/typed/xtalk_intrinsic_test.clj:93:^{:refer tahto.typed.xtalk-intrinsic/make-container :added "4.1"}
(fact "delegates container construction inference"
  (:type (make-container '(intrinsic init type opts) {} (callbacks)))
  => '{:kind :named :name sample/Container})

tahto/typed/xtalk_intrinsic_test.clj:98:^{:refer tahto.typed.xtalk-intrinsic/blank-container :added "4.1"}
(fact "delegates blank container inference"
  (:type (blank-container '(intrinsic type opts) {} (callbacks)))
  => '{:kind :named :name sample/BlankContainer})

tahto/typed/xtalk_intrinsic_test.clj:103:^{:refer tahto.typed.xtalk-intrinsic/infer-intrinsic :added "4.1"}
(fact "dispatches intrinsic rules by symbol"
tahto/typed/xtalk_intrinsic_test.clj:105:  [(-> (infer-intrinsic '(tahto.typed.xtalk-intrinsic/arrayify numbers) {} (callbacks)) :type)
tahto/typed/xtalk_intrinsic_test.clj:106:   (infer-intrinsic '(tahto.typed.xtalk-intrinsic/missing numbers) {} (callbacks))]
  => '[{:kind :array :item {:kind :array :item {:kind :primitive :name :xt/int}}}
        nil])
