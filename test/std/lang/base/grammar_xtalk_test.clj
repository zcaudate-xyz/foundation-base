(ns std.lang.base.grammar-xtalk-test
  (:require [clojure.string :as str]
            [std.lang.base.grammar-xtalk :as xtalk :refer :all]
            [xtgen.lang :as xtgen])
  (:use code.test))
;
^{:refer std.lang.base.grammar-xtalk/tf-throw :added "4.0"}
(fact "wrapper for throw transform"
  (tf-throw '(x:throw "error"))
  => '(throw "error"))

^{:refer std.lang.base.grammar-xtalk/tf-add :added "4.1"}
(fact "wrapper for add transform"
  (tf-add '(x:add a b))
  => '(+ a b))

^{:refer std.lang.base.grammar-xtalk/tf-first :added "4.1"}
(fact "wrapper for first transform"
  (tf-first '(x:first arr))
  => '(x:get-idx arr 0))

^{:refer std.lang.base.grammar-xtalk/tf-eq-nil? :added "4.0"}
(fact "equals nil transform"
  (tf-eq-nil? '(x:nil? a))
  => '(== nil a))

^{:refer std.lang.base.grammar-xtalk/tf-not-nil? :added "4.0"}
(fact "not nil transform"
  (tf-not-nil? '(x:not-nil? a))
  => '(not= nil a))

^{:refer std.lang.base.grammar-xtalk/tf-proto-create :added "4.0"}
(fact "creates the prototype map"
  (tf-proto-create '(x:proto-create {}))
  => '(return {}))

^{:refer std.lang.base.grammar-xtalk/tf-has-key? :added "4.0"}
(fact "has key default transform"
  (tf-has-key? '(x:has-key? obj "a"))
  => '(not= (x:get-key obj "a") nil))

^{:refer std.lang.base.grammar-xtalk/tf-get-path :added "4.0"}
(fact "get-in transform"
  ^:hidden
  
  (tf-get-path '(x:get-path obj ["a" "b" "c"]))
  => '(. obj ["a"] ["b"] ["c"]))

^{:refer std.lang.base.grammar-xtalk/tf-get-key :added "4.0"}
(fact "get-key transform"
  ^:hidden
  
  (tf-get-key '(x:get-key obj "a"))
  => '(. obj ["a"])

  (tf-get-key '(x:get-key obj "a" "DEFAULT"))
  => '(or (. obj ["a"]) "DEFAULT"))

^{:refer std.lang.base.grammar-xtalk/tf-set-key :added "4.0"}
(fact "set-key transform"
  ^:hidden
  
  (tf-set-key '(x:set-key obj "a" 1))
  => '(:= (. obj ["a"]) 1))

^{:refer std.lang.base.grammar-xtalk/tf-del-key :added "4.0"}
(fact "del-key transform"
  ^:hidden
  
  (tf-del-key '(x:del-key obj "a"))
  => '(x:del (. obj ["a"])))

^{:refer std.lang.base.grammar-xtalk/tf-copy-key :added "4.0"}
(fact "copy-key transform"
  ^:hidden
  
  (tf-copy-key '(x:copy-key obj src "a"))
  => '(:= (. obj ["a"]) (. src ["a"]))

  (tf-copy-key '(x:copy-key obj src ["a" "b"]))
  => '(:= (. obj ["a"]) (. src ["b"])))

^{:refer std.lang.base.grammar-xtalk/tf-grammar-offset :added "4.0"}
(fact "del-key transform"
  ^:hidden
  
  (tf-grammar-offset)
  => 0)

^{:refer std.lang.base.grammar-xtalk/tf-grammar-end-inclusive :added "4.0"}
(fact "gets the end inclusive flag"
  ^:hidden
  
  (tf-grammar-end-inclusive)
  => nil)

^{:refer std.lang.base.grammar-xtalk/tf-offset-base :added "4.0"}
(fact "calculates the offset"
  ^:hidden
  
  (tf-offset-base 1 'hello)
  => '(+ hello 1)

  (tf-offset-base 0 'hello)
  => 'hello

  (tf-offset-base 1 1)
  => 2)

^{:refer std.lang.base.grammar-xtalk/tf-offset :added "4.0"}
(fact "gets the offset"
  (tf-offset '(x:offset 10))
  => 10)

^{:refer std.lang.base.grammar-xtalk/tf-offset-rev :added "4.0"}
(fact "gets the reverse offset"
  (tf-offset-rev '(x:offset-rev 10))
  => 9)

^{:refer std.lang.base.grammar-xtalk/tf-offset-len :added "4.0"}
(fact "gets the length offset"
  (tf-offset-len '(x:offset-len 10))
  => 9)

^{:refer std.lang.base.grammar-xtalk/tf-offset-rlen :added "4.0"}
(fact "gets the reverse length offset"
  (tf-offset-rlen '(x:offset-rlen 10))
  => 10)

^{:refer std.lang.base.grammar-xtalk/tf-global-set :added "4.0"}
(fact "default global set transform"
  ^:hidden
  
  (tf-global-set '(x:global-set SYM 1))
  => '(x:set-key !:G "SYM" 1))

^{:refer std.lang.base.grammar-xtalk/tf-global-has? :added "4.0"}
(fact  "default global has transform"
  ^:hidden
  
  (tf-global-has? '(x:global-has SYM))
  => '(not (x:nil? (x:get-key !:G "SYM"))))

^{:refer std.lang.base.grammar-xtalk/tf-global-del :added "4.0"}
(fact "default global del transform"
  ^:hidden
  
  (tf-global-del '(x:global-del SYM))
  => '(x:set-key !:G "SYM" nil))

^{:refer std.lang.base.grammar-xtalk/tf-lu-eq :added "4.0"}
(fact "lookup equals transform"
  ^:hidden
  
  (tf-lu-eq '(x:lu-eq o1 o2))
  => '(== o1 o2))

^{:refer std.lang.base.grammar-xtalk/tf-bit-and :added "4.0"}
(fact "bit and transform"
  ^:hidden
  
  (tf-bit-and '(x:bit-and x y))
  => '(b:& x y))

^{:refer std.lang.base.grammar-xtalk/tf-bit-or :added "4.0"}
(fact "bit or transform"
  ^:hidden
  
  (tf-bit-or '(x:bit-or x y))
  => '(b:| x y))

^{:refer std.lang.base.grammar-xtalk/tf-bit-lshift :added "4.0"}
(fact "bit left shift transform"
  ^:hidden
  
  (tf-bit-lshift '(x:bit-lshift x y))
  => '(b:<< x y))

^{:refer std.lang.base.grammar-xtalk/tf-bit-rshift :added "4.0"}
(fact "bit right shift transform"
  ^:hidden
  
  (tf-bit-rshift '(x:bit-rshift x y))
  => '(b:>> x y))

^{:refer std.lang.base.grammar-xtalk/tf-bit-xor :added "4.0"}
(fact "bit xor transform"
  ^:hidden
  
  (tf-bit-xor '(x:bit-xor x y))
  => '(b:xor x y))


^{:refer std.lang.base.grammar-xtalk/tf-sub :added "4.1"}
(fact "wrapper for sub transform"
  (tf-sub '(x:sub a b))
  => '(- a b))

^{:refer std.lang.base.grammar-xtalk/tf-mul :added "4.1"}
(fact "wrapper for mul transform"
  (tf-mul '(x:mul a b))
  => '(* a b))

^{:refer std.lang.base.grammar-xtalk/tf-div :added "4.1"}
(fact "wrapper for div transform"
  (tf-div '(x:div a b))
  => '(/ a b))

^{:refer std.lang.base.grammar-xtalk/tf-neg :added "4.1"}
(fact "wrapper for neg transform"
  (tf-neg '(x:neg a))
  => '(- a))

^{:refer std.lang.base.grammar-xtalk/tf-inc :added "4.1"}
(fact "wrapper for inc transform"
  (tf-inc '(x:inc a))
  => '(+ a 1))

^{:refer std.lang.base.grammar-xtalk/tf-dec :added "4.1"}
(fact "wrapper for dec transform"
  (tf-dec '(x:dec a))
  => '(- a 1))

^{:refer std.lang.base.grammar-xtalk/tf-eq :added "4.1"}
(fact "wrapper for eq transform"
  (tf-eq '(x:eq a b))
  => '(== a b))

^{:refer std.lang.base.grammar-xtalk/tf-neq :added "4.1"}
(fact "wrapper for neq transform"
  (tf-neq '(x:neq a b))
  => '(not= a b))

^{:refer std.lang.base.grammar-xtalk/tf-lt :added "4.1"}
(fact "wrapper for lt transform"
  (tf-lt '(x:lt a b))
  => '(< a b))

^{:refer std.lang.base.grammar-xtalk/tf-lte :added "4.1"}
(fact "wrapper for lte transform"
  (tf-lte '(x:lte a b))
  => '(<= a b))

^{:refer std.lang.base.grammar-xtalk/tf-gt :added "4.1"}
(fact "wrapper for gt transform"
  (tf-gt '(x:gt a b))
  => '(> a b))

^{:refer std.lang.base.grammar-xtalk/tf-gte :added "4.1"}
(fact "wrapper for gte transform"
  (tf-gte '(x:gte a b))
  => '(>= a b))

^{:refer std.lang.base.grammar-xtalk/tf-zero? :added "4.1"}
(fact "wrapper for zero? transform"
  (tf-zero? '(x:zero? a))
  => '(== a 0))

^{:refer std.lang.base.grammar-xtalk/tf-pos? :added "4.1"}
(fact "wrapper for pos? transform"
  (tf-pos? '(x:pos? a))
  => '(> a 0))

^{:refer std.lang.base.grammar-xtalk/tf-neg? :added "4.1"}
(fact "wrapper for neg? transform"
  (tf-neg? '(x:neg? a))
  => '(< a 0))

^{:refer std.lang.base.grammar-xtalk/tf-even? :added "4.1"}
(fact "wrapper for even? transform"
  (tf-even? '(x:even? a))
  => '(== 0 (mod a 2)))

^{:refer std.lang.base.grammar-xtalk/tf-odd? :added "4.1"}
(fact "wrapper for odd? transform"
  (tf-odd? '(x:odd? a))
  => '(not (== 0 (mod a 2))))

^{:refer std.lang.base.grammar-xtalk/tf-second :added "4.1"}
(fact "gets the second element"
  (tf-second '(x:second arr))
  => '(x:get-idx arr 1))

^{:refer std.lang.base.grammar-xtalk/tf-last :added "4.1"}
(fact "gets the last element"
  (tf-last '(x:last arr))
  => '(x:get-idx arr (x:offset-len (x:len arr))))

^{:refer std.lang.base.grammar-xtalk/tf-second-last :added "4.1"}
(fact "gets the second last element"
  (tf-second-last '(x:second-last arr))
  => '(x:get-idx arr (+ (x:len arr) (x:offset -2))))

^{:refer std.lang.base.grammar-xtalk/tf-str-lt :added "4.1"}
(fact "checks string ordering ascending"
  (tf-str-lt '(x:str-lt a b))
  => '(x:arr-str-comp a b))

^{:refer std.lang.base.grammar-xtalk/tf-str-gt :added "4.1"}
(fact "checks string ordering descending"
  (tf-str-gt '(x:str-gt a b))
  => '(x:arr-str-comp b a))

(fact "all xtalk grammar map entries expose op-spec contracts"
  (vec
   (for [[sym v] (sort-by key (ns-publics 'std.lang.base.grammar-xtalk))
         :when (str/starts-with? (name sym) "+xt-")
         entry @v
         :when (and (map? entry)
                    (not (:op-spec entry)))]
     (:op entry)))
  => [])


(fact "generator-backed fragment spec emits the expected form"
  (xtalk/xtgen.fragment-spec
   {:symbol  '[x:arr-push]
    :op-spec {:type '[:fn [:xt/arr :xt/any] :xt/self]}})
  => '(defspec.xt x:arr-push
        [:fn [:xt/arr :xt/any] :xt/self]))

(fact "common-lib generator emits def$.xt aliases"
  (xtgen/generate-common-lib
   {:symbol  '[x:arr-push]
    :op-spec {:arglists '([arr val])}})
  => '(def$.xt arr-push x:arr-push))

(fact "generator-backed fragment fn emits macro wrapper"
  (let [form (xtalk/xtgen.fragment-fn
              {:symbol  '[x:arr-push]
               :op-spec {:arglists '([arr val])}})]
    [(first form)
     (second form)
     (nth form 2)
     (nth form 3)])
  => '[defmacro.xt
       x:arr-push
       ([arr val])
       (x:arr-push arr val)])

^{:refer std.lang.base.grammar-xtalk/tmpl-fragment-fn :added "4.1"}
(fact "compatibility wrapper delegates to xtgen fragment fn"
  (let [entry {:symbol  '[x:arr-push]
               :op-spec {:arglists '([arr val])}}]
    (tmpl-fragment-fn entry))
  => (xtalk/xtgen.fragment-fn
      {:symbol  '[x:arr-push]
       :op-spec {:arglists '([arr val])}}))


^{:refer std.lang.base.grammar-xtalk/xtgen-fragment-spec-input :added "4.1"}
(fact "maps fragment spec entries into template input"
  (xtgen-fragment-spec-input
   {:symbol '[x:arr-push]
    :op-spec {:type '[:fn [:xt/arr :xt/any] :xt/self]}})
  => {'sym-name 'x:arr-push
      'type '[:fn [:xt/arr :xt/any] :xt/self]})

^{:refer std.lang.base.grammar-xtalk/xtgen.fragment-spec :added "4.1"}
(fact "emits a fragment spec form from the template generator"
  (xtgen.fragment-spec
   {:symbol '[x:arr-push]
    :op-spec {:type '[:fn [:xt/arr :xt/any] :xt/self]}})
  => '(defspec.xt x:arr-push
        [:fn [:xt/arr :xt/any] :xt/self]))

^{:refer std.lang.base.grammar-xtalk/xtgen-fragment-fn-input :added "4.1"}
(fact "maps fragment fn entries into template input"
  (xtgen-fragment-fn-input
   {:symbol '[x:arr-push]
    :op-spec {:arglists '([arr val])}})
  => {'sym-name 'x:arr-push
      'arglists '([arr val])
      'call-form '(x:arr-push arr val)})

^{:refer std.lang.base.grammar-xtalk/xtgen.fragment-fn :added "4.1"}
(fact "emits a fragment fn wrapper from the template generator"
  (xtgen.fragment-fn
   {:symbol '[x:arr-push]
    :op-spec {:arglists '([arr val])}})
  => '(defmacro.xt ^{:template :standalone}
        x:arr-push
        ([arr val])
        (x:arr-push arr val)))

^{:refer std.lang.base.grammar-xtalk/tmpl-fragment-spec :added "4.1"}
(fact "compatibility wrapper delegates to xtgen fragment spec"
  (tmpl-fragment-spec
   {:symbol '[x:arr-push]
    :op-spec {:type '[:fn [:xt/arr :xt/any] :xt/self]}})
  => '(defspec.xt x:arr-push
        [:fn [:xt/arr :xt/any] :xt/self]))

^{:refer std.lang.base.grammar-xtalk/tmpl-defn-fn :added "4.1"}
(fact "the legacy defn wrapper is currently not exposed"
  (resolve 'std.lang.base.grammar-xtalk/tmpl-defn-fn)
  => nil)
