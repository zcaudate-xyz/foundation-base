(ns std.lang.base.grammar-xtalk-test
  (:use code.test)
  (:require [std.lang.base.grammar-xtalk :refer :all]))

^{:refer std.lang.base.grammar-xtalk/tf-throw :added "4.0"}
(fact "wrapper for throw transform"
  (tf-throw '(x:throw "error"))
  => '(throw "error"))

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
