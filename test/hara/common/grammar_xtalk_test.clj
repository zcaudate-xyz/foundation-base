tahto/common/grammar_xtalk_test.clj:1:(ns tahto.common.grammar-xtalk-test
  (:require [clojure.string :as str]
tahto/common/grammar_xtalk_test.clj:3:            [tahto.common.grammar-xtalk :as xtalk :refer :all])
  (:use code.test))

(fact "all xtalk grammar map entries expose op-spec contracts"
  (vec
tahto/common/grammar_xtalk_test.clj:8:   (for [[sym v] (sort-by key (ns-publics 'tahto.common.grammar-xtalk))
         :when (str/starts-with? (name sym) "+xt-")
         entry @v
         :when (and (map? entry)
                    (not (:op-spec entry)))]
     (:op entry)))
  => [])

tahto/common/grammar_xtalk_test.clj:16:^{:refer tahto.common.grammar-xtalk/tf-str-lt :added "4.1"}
(fact "checks string ordering ascending"
  (tf-str-lt '(x:str-lt "a" "b"))
  => '(x:str-comp "a" "b"))

tahto/common/grammar_xtalk_test.clj:21:^{:refer tahto.common.grammar-xtalk/tf-str-gt :added "4.1"}
(fact "checks string ordering descending"
  (tf-str-gt '(x:str-gt "a" "b"))
  => '(x:str-comp "b" "a"))

tahto/common/grammar_xtalk_test.clj:26:^{:refer tahto.common.grammar-xtalk/tf-throw :added "4.0"}
(fact "wrapper for throw transform"
  (tf-throw '(x:throw "error"))
  => '(throw "error"))

tahto/common/grammar_xtalk_test.clj:31:^{:refer tahto.common.grammar-xtalk/tf-add :added "4.1"}
(fact "wrapper for add transform"
  (tf-add '(x:add a b))
  => '(+ a b))

tahto/common/grammar_xtalk_test.clj:36:^{:refer tahto.common.grammar-xtalk/tf-sub :added "4.1"}
(fact "wrapper for sub transform"
  (tf-sub '(x:sub a b))
  => '(- a b))

tahto/common/grammar_xtalk_test.clj:41:^{:refer tahto.common.grammar-xtalk/tf-mul :added "4.1"}
(fact "wrapper for mul transform"
  (tf-mul '(x:mul a b))
  => '(* a b))

tahto/common/grammar_xtalk_test.clj:46:^{:refer tahto.common.grammar-xtalk/tf-div :added "4.1"}
(fact "wrapper for div transform"
  (tf-div '(x:div a b))
  => '(/ a b))

tahto/common/grammar_xtalk_test.clj:51:^{:refer tahto.common.grammar-xtalk/tf-neg :added "4.1"}
(fact "wrapper for neg transform"
  (tf-neg '(x:neg a))
  => '(- a))

tahto/common/grammar_xtalk_test.clj:56:^{:refer tahto.common.grammar-xtalk/tf-inc :added "4.1"}
(fact "wrapper for inc transform"
  (tf-inc '(x:inc a))
  => '(+ a 1))

tahto/common/grammar_xtalk_test.clj:61:^{:refer tahto.common.grammar-xtalk/tf-dec :added "4.1"}
(fact "wrapper for dec transform"
  (tf-dec '(x:dec a))
  => '(- a 1))

tahto/common/grammar_xtalk_test.clj:66:^{:refer tahto.common.grammar-xtalk/tf-eq :added "4.1"}
(fact "wrapper for eq transform"
  (tf-eq '(x:eq a b))
  => '(== a b))

tahto/common/grammar_xtalk_test.clj:71:^{:refer tahto.common.grammar-xtalk/tf-neq :added "4.1"}
(fact "wrapper for neq transform"
  (tf-neq '(x:neq a b))
  => '(not= a b))

tahto/common/grammar_xtalk_test.clj:76:^{:refer tahto.common.grammar-xtalk/tf-lt :added "4.1"}
(fact "wrapper for lt transform"
  (tf-lt '(x:lt a b))
  => '(< a b))

tahto/common/grammar_xtalk_test.clj:81:^{:refer tahto.common.grammar-xtalk/tf-lte :added "4.1"}
(fact "wrapper for lte transform"
  (tf-lte '(x:lte a b))
  => '(<= a b))

tahto/common/grammar_xtalk_test.clj:86:^{:refer tahto.common.grammar-xtalk/tf-gt :added "4.1"}
(fact "wrapper for gt transform"
  (tf-gt '(x:gt a b))
  => '(> a b))

tahto/common/grammar_xtalk_test.clj:91:^{:refer tahto.common.grammar-xtalk/tf-gte :added "4.1"}
(fact "wrapper for gte transform"
  (tf-gte '(x:gte a b))
  => '(>= a b))

tahto/common/grammar_xtalk_test.clj:96:^{:refer tahto.common.grammar-xtalk/tf-zero? :added "4.1"}
(fact "wrapper for zero? transform"
  (tf-zero? '(x:zero? a))
  => '(== a 0))

tahto/common/grammar_xtalk_test.clj:101:^{:refer tahto.common.grammar-xtalk/tf-pos? :added "4.1"}
(fact "wrapper for pos? transform"
  (tf-pos? '(x:pos? a))
  => '(> a 0))

tahto/common/grammar_xtalk_test.clj:106:^{:refer tahto.common.grammar-xtalk/tf-neg? :added "4.1"}
(fact "wrapper for neg? transform"
  (tf-neg? '(x:neg? a))
  => '(< a 0))

tahto/common/grammar_xtalk_test.clj:111:^{:refer tahto.common.grammar-xtalk/tf-even? :added "4.1"}
(fact "wrapper for even? transform"
  (tf-even? '(x:even? a))
  => '(== 0 (mod a 2)))

tahto/common/grammar_xtalk_test.clj:116:^{:refer tahto.common.grammar-xtalk/tf-odd? :added "4.1"}
(fact "wrapper for odd? transform"
  (tf-odd? '(x:odd? a))
  => '(not (== 0 (mod a 2))))

tahto/common/grammar_xtalk_test.clj:121:^{:refer tahto.common.grammar-xtalk/tf-eq-nil? :added "4.0"}
(fact "equals nil transform"
  (tf-eq-nil? '(x:nil? a))
  => '(== nil a))

tahto/common/grammar_xtalk_test.clj:126:^{:refer tahto.common.grammar-xtalk/tf-not-nil? :added "4.0"}
(fact "not nil transform"
  (tf-not-nil? '(x:not-nil? a))
  => '(not= nil a))

tahto/common/grammar_xtalk_test.clj:131:^{:refer tahto.common.grammar-xtalk/tf-has-key? :added "4.0"}
(fact "has key default transform"
  (tf-has-key? '(x:has-key? obj "a"))
  => '(not= (x:get-key obj "a") nil))

tahto/common/grammar_xtalk_test.clj:136:^{:refer tahto.common.grammar-xtalk/tf-get-path :added "4.0"}
(fact "lowers direct path access to chained indexing"

  (tf-get-path '(x:get-path obj ["a" "b" "c"]))
  => '(. obj ["a"] ["b"] ["c"]))

tahto/common/grammar_xtalk_test.clj:142:^{:refer tahto.common.grammar-xtalk/tf-get-key :added "4.0"}
(fact "get-key transform"

  (tf-get-key '(x:get-key obj "a"))
  => '(. obj ["a"])

  (tf-get-key '(x:get-key obj "a" "DEFAULT"))
  => '(:? (x:nil? (. obj ["a"])) "DEFAULT" (. obj ["a"])))

tahto/common/grammar_xtalk_test.clj:151:^{:refer tahto.common.grammar-xtalk/tf-set-key :added "4.0"}
(fact "set-key transform"

  (tf-set-key '(x:set-key obj "a" 1))
  => '(:= (. obj ["a"]) 1))

tahto/common/grammar_xtalk_test.clj:157:^{:refer tahto.common.grammar-xtalk/tf-del-key :added "4.0"}
(fact "del-key transform"

  (tf-del-key '(x:del-key obj "a"))
  => '(x:del (. obj ["a"])))

tahto/common/grammar_xtalk_test.clj:163:^{:refer tahto.common.grammar-xtalk/tf-copy-key :added "4.0"}
(fact "copy-key transform"

  (tf-copy-key '(x:copy-key obj src "a"))
  => '(:= (. obj ["a"]) (. src ["a"]))

  (tf-copy-key '(x:copy-key obj src ["a" "b"]))
  => '(:= (. obj ["a"]) (. src ["b"])))

tahto/common/grammar_xtalk_test.clj:172:^{:refer tahto.common.grammar-xtalk/tf-grammar-offset :added "4.0"}
(fact "del-key transform"

  (tf-grammar-offset)
  => 0)

tahto/common/grammar_xtalk_test.clj:178:^{:refer tahto.common.grammar-xtalk/tf-grammar-end-inclusive :added "4.0"}
(fact "gets the end inclusive flag"

  (tf-grammar-end-inclusive)
  => nil)

tahto/common/grammar_xtalk_test.clj:184:^{:refer tahto.common.grammar-xtalk/tf-offset-base :added "4.0"}
(fact "calculates the offset"

  (tf-offset-base 1 'hello)
  => '(+ hello 1)

  (tf-offset-base 0 'hello)
  => 'hello

  (tf-offset-base 1 1)
  => 2)

tahto/common/grammar_xtalk_test.clj:196:^{:refer tahto.common.grammar-xtalk/tf-offset :added "4.0"}
(fact "gets the offset"
  (tf-offset '(x:offset 10))
  => 10)

tahto/common/grammar_xtalk_test.clj:201:^{:refer tahto.common.grammar-xtalk/tf-offset-rev :added "4.0"}
(fact "gets the reverse offset"
  (tf-offset-rev '(x:offset-rev 10))
  => 9)

tahto/common/grammar_xtalk_test.clj:206:^{:refer tahto.common.grammar-xtalk/tf-offset-len :added "4.0"}
(fact "gets the length offset"
  (tf-offset-len '(x:offset-len 10))
  => 9)

tahto/common/grammar_xtalk_test.clj:211:^{:refer tahto.common.grammar-xtalk/tf-offset-rlen :added "4.0"}
(fact "gets the reverse length offset"
  (tf-offset-rlen '(x:offset-rlen 10))
  => 10)

tahto/common/grammar_xtalk_test.clj:216:^{:refer tahto.common.grammar-xtalk/tf-first :added "4.1"}
(fact "wrapper for first transform"
  (tf-first '(x:first arr))
  => '(x:get-idx arr (x:offset 0)))

tahto/common/grammar_xtalk_test.clj:221:^{:refer tahto.common.grammar-xtalk/tf-second :added "4.1"}
(fact "gets the second element"
  (tf-second '(x:second arr))
  => '(x:get-idx arr (x:offset 1)))

tahto/common/grammar_xtalk_test.clj:226:^{:refer tahto.common.grammar-xtalk/tf-last :added "4.1"}
(fact "gets the last element"
  (tf-last '(x:last arr))
  => '(x:get-idx arr (x:offset-len (x:len arr))))

tahto/common/grammar_xtalk_test.clj:231:^{:refer tahto.common.grammar-xtalk/tf-second-last :added "4.1"}
(fact "gets the second last element"
  (tf-second-last '(x:second-last arr))
  => '(x:get-idx arr (x:offset-len (- (x:len arr) 1))))

tahto/common/grammar_xtalk_test.clj:236:^{:refer tahto.common.grammar-xtalk/tf-str-lt :added "4.1"}
(fact "checks string ordering ascending"
  (tf-str-lt '(x:str-lt a b))
  => '(x:str-comp a b))

tahto/common/grammar_xtalk_test.clj:241:^{:refer tahto.common.grammar-xtalk/tf-str-gt :added "4.1"}
(fact "checks string ordering descending"
  (tf-str-gt '(x:str-gt a b))
  => '(x:str-comp b a))

tahto/common/grammar_xtalk_test.clj:246:^{:refer tahto.common.grammar-xtalk/tf-global-set :added "4.0"}
(fact "default global set transform"

  (tf-global-set '(x:global-set SYM 1))
  => '(x:set-key !:G "SYM" 1))

tahto/common/grammar_xtalk_test.clj:252:^{:refer tahto.common.grammar-xtalk/tf-global-has? :added "4.0"}
(fact  "default global has transform"

  (tf-global-has? '(x:global-has SYM))
  => '(not (x:nil? (x:get-key !:G "SYM"))))

tahto/common/grammar_xtalk_test.clj:258:^{:refer tahto.common.grammar-xtalk/tf-global-del :added "4.0"}
(fact "default global del transform"

  (tf-global-del '(x:global-del SYM))
  => '(x:set-key !:G "SYM" nil))

tahto/common/grammar_xtalk_test.clj:264:^{:refer tahto.common.grammar-xtalk/tf-lu-eq :added "4.0"}
(fact "lookup equals transform"

  (tf-lu-eq '(x:lu-eq o1 o2))
  => '(== o1 o2))

tahto/common/grammar_xtalk_test.clj:270:^{:refer tahto.common.grammar-xtalk/tf-bit-and :added "4.0"}
(fact "bit and transform"

  (tf-bit-and '(x:bit-and x y))
  => '(b:& x y))

tahto/common/grammar_xtalk_test.clj:276:^{:refer tahto.common.grammar-xtalk/tf-bit-or :added "4.0"}
(fact "bit or transform"

  (tf-bit-or '(x:bit-or x y))
  => '(b:| x y))

tahto/common/grammar_xtalk_test.clj:282:^{:refer tahto.common.grammar-xtalk/tf-bit-lshift :added "4.0"}
(fact "bit left shift transform"

  (tf-bit-lshift '(x:bit-lshift x y))
  => '(b:<< x y))

tahto/common/grammar_xtalk_test.clj:288:^{:refer tahto.common.grammar-xtalk/tf-bit-rshift :added "4.0"}
(fact "bit right shift transform"

  (tf-bit-rshift '(x:bit-rshift x y))
  => '(b:>> x y))

tahto/common/grammar_xtalk_test.clj:294:^{:refer tahto.common.grammar-xtalk/tf-bit-xor :added "4.0"}
(fact "bit xor transform"

  (tf-bit-xor '(x:bit-xor x y))
  => '(b:xor x y))
