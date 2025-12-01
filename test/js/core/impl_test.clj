(ns js.core.impl-test
  (:use code.test)
  (:require [std.lang :as l]
            [xt.lang.base-notify :as notify]))

(l/script- :js
  {:runtime :basic
   :require [[js.core :as j]
             [xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer js.core.impl/primitives :added "4.0" :unchecked true :adopt true}
(fact "Runtime primitives"

  (!.js (typeof undefined))
  => "undefined"

  (!.js undefined)
  => nil
  
  (!.js (typeof nil))
  => "object"

  (!.js (JSON.stringify nil))
  => "null"

  (!.js nil)
  => nil
  
  (!.js (void 1))
  => nil

  (!.js 1)
  => 1

  (!.js true)
  => true

  (!.js "hello")
  => "hello"

  @(!.js (fn []))
  => "function (){\n    \n  }"
  
  @(!.js #"abc")
  => "/abc/"
  
  @(!.js (:% #"abc" y))
  => "/abc/y"
  
  @(!.js (:% #"abc" g))
  => "/abc/g")

^{:refer js.core.impl/global :added "4.0" :unchecked true :adopt true}
(fact "Major runtime primitives"
  
  @(!.js j/JSObject)
  => "function Object() { [native code] }"
  
  @(!.js j/JSFunction)
  => "function Function() { [native code] }"
  
  @(!.js j/JSArray)
  => "function Array() { [native code] }"
  
  @(!.js j/JSArrayBuffer)
  => "function ArrayBuffer() { [native code] }"

  @(!.js j/JSBoolean)
  => "function Boolean() { [native code] }"

  @(!.js j/JSNumber)
  => "function Number() { [native code] }"
  
  @(!.js j/JSSet)
  => "function Set() { [native code] }"
  
  @(!.js j/JSMap)
  => "function Map() { [native code] }"
  
  @(!.js j/JSError)
  => "function Error() { [native code] }"
  
  @(!.js j/JSDate)
  => "function Date() { [native code] }"

  @(!.js j/JSString)
  => "function String() { [native code] }"
  
  @(!.js j/JSSymbol)
  => "function Symbol() { [native code] }"
 
  @(!.js j/Proxy)
  => "function Proxy() { [native code] }"

  @(!.js j/Promise)
  => "function Promise() { [native code] }"
 
  (!.js j/JSMath)
  => {}
  
  (!.js j/JSON)
  => {}
  
  (!.js j/Intl)
  => {}
  
  (!.js j/Reflect)
  => {}

  ;;
  ;; No Support for Atomics, Async, Generators and Webassembly
  ;;
  
  @(!.js j/JSBigInt)
  => "function BigInt() { [native code] }"
  
  (!.js j/Atomics)
  => (any {} nil Exception)

  (!.js j/AsyncFunction)
  => (any {} nil Exception)

  (!.js j/GeneratorFunction)
  => (any {} nil Exception)

  (!.js j/WebAssembly)
  => (any {} nil Exception))

^{:refer js.core.impl/types :added "4.0" :unchecked true :adopt true}
(fact "math functions"

  (!.js (typeof nil))
  => "object"
  
  (!.js (typeof (new j/JSMap)))
  => "object"

  (!.js (typeof (new j/JSSet)))
  => "object"

  (!.js (typeof (new j/JSDate)))
  => "object"

  (!.js (typeof []))
  => "object"
  
  (!.js (typeof #"eo"))
  => "object"

  (!.js (typeof ""))
  => "string"

  (!.js (typeof nil))
  => "object"
  
  (!.js (== "undefined" (typeof undefined)))
  => true

  (!.js (== "undefined" (typeof BLAH)))
  => true

  (!.js (typeof false))
  => "boolean"

  (!.js (typeof NaN))
  => "number"

  (!.js (typeof 1))
  => "number"

  (!.js (typeof (j/JSSymbol "hello")))
  => "symbol"

  (!.js (typeof (fn [])))
  => "function")

^{:refer js.core.impl/eval :added "4.0" :unchecked true :adopt true}
(fact "eval"

  (!.js (j/getOwnPropertyNames (new Set)))
  => []
  
  @(!.js (j/eval (new String "2 + 2")))
  => "2 + 2"

  (j/eval "2 + 2")
  => 4)

^{:refer js.core.impl/math :added "4.0" :unchecked true :adopt true}
(fact "math functions"

  (!.js j/LOG2E)
  => 1.4426950408889634

  (!.js j/PI)
  => 3.141592653589793
  
  (!.js j/E)
  => 2.718281828459045
  
  (!.js j/EPSILON)
  => 2.220446049250313E-16

  (!.js (. j/POSITIVE-INFINITY
           (toString)))
  => "Infinity"

  (!.js (. j/NEGATIVE-INFINITY
           (toString)))
  => "-Infinity"

  (j/toFixed 1.23456 2)
  => "1.23"
  
  (j/toRadix ''(10) 16)
  => "a"
    
  (j/toRadix ''(10) 2)
  => "1010"
    
  (j/toRadix ''(10) 12)
  => "a")

^{:refer js.core.impl/now :added "4.0" :unchecked true :adopt true}
(fact "gets the current time"

  (j/now)
  => number?)

^{:refer js.core.impl/concat :added "4.0" :unchecked true :adopt true}
(fact "concat arrays"
  ^:hidden
  
  (j/concat [1 2 3]
             [4 5 6])
  => [1 2 3 4 5 6])

^{:refer js.core.impl/slice :added "4.0" :unchecked true :adopt true}
(fact "raw array slice"
  ^:hidden
  
  (j/slice [1 2 3 4 5] 2 4)
  => [3 4])

^{:refer js.core.impl/fill :added "4.0" :unchecked true :adopt true}
(fact "fills array given index"
  ^:hidden
  
  (j/fill [1 2 3] 0)
  => [0 0 0]

  (j/fill [1 1 1 1 1 1 1] 0 1 4)
  => [1 0 0 0 1 1 1])

^{:refer js.core.impl/findIndex :added "4.0" :unchecked true :adopt true}
(fact "gets the index of a function"
  ^:hidden

  (j/findIndex [1 2 3 4] k/even?)
  => 1)

^{:refer js.core.impl/flat :added "4.0" :unchecked true :adopt true}
(fact "flattens array to first level"
  ^:hidden
  
  (j/flat [[1 1] [2 3]] 1)
  => [1 1 2 3]

  (j/flat [[1 [1]] [[2] 3]] 1)
  => [1 [1] [2] 3])

^{:refer js.core.impl/flattenDeep :added "4.0" :unchecked true :adopt true}
(fact "flattens nested arrays"
  ^:hidden
  
  (j/flatMap [[1 [1]] [[2] 3]] k/identity)
  => [1 [1] [2] 3])

^{:refer js.core.impl/indexOf :added "4.0" :unchecked true :adopt true}
(fact "gets the first index of an element"
  ^:hidden
  
  (j/indexOf [1 2 3 4] 3)
  => 2

  (j/indexOf [3 2 3 4] 3)
  => 0)

^{:refer js.core.impl/lastIndexOf :added "4.0" :unchecked true :adopt true}
(fact "gets the last index of an element"
  ^:hidden
  
  (j/lastIndexOf [3 2 3 4] 3)
  => 2)

^{:refer js.core.impl/map :added "4.0" :unchecked true :adopt true}
(fact "maps function to array"
  ^:hidden
  
  (j/map [1 2 3 4 5] k/inc)
  => [2 3 4 5 6])

^{:refer js.core.impl/reduce :added "4.0" :unchecked true :adopt true}
(fact "reduces function given array"
  ^:hidden

  (j/reduce [1 2 3 4 5]
             k/add
             0)
  => 15)

^{:refer js.core.impl/reduceRight :added "4.0" :unchecked true :adopt true}
(fact "reduces function from the right"
  ^:hidden

  (j/reduceRight [1 2 3 4 5]
                  k/add
                  0)
  => 15)

^{:refer js.core.impl/filter :added "4.0" :unchecked true :adopt true}
(fact "filter array given a function"
  ^:hidden

  (!.js
   (j/filter [1 2 3 4 5]
             k/even?))
  => [2 4])

^{:refer js.core.impl/every :added "4.0" :unchecked true :adopt true}
(fact "checks if every element conforms to function"

  (!.js
   (j/every [1 2 3 4 5]
            k/is-number?))
  => true)

^{:refer js.core.impl/some :added "4.0" :unchecked true :adopt true}
(fact "checks if any element conforms to function"

  (!.js
   (j/some [1 2 3 4 5]
            (fn:> [x] (== 3 x))))
  => true)

^{:refer js.core.impl/delayed :added "4.0" :unchecked true}
(fact "constructs a setTimeout function"

  (notify/wait-on :js
   (j/delayed [10]
     (repl/notify 1)))
  => 1)

^{:refer js.core.impl/repeating :added "4.0" :unchecked true}
(fact "constructs a setInterval function"

  ((:template @j/repeating) [10]
   '(doSomething))
  => '(setInterval (fn [] (new Promise (fn [] (doSomething)))) 10))


^{:refer js.core.impl/postMessage :added "4.0" :unchecked true}
(fact "post message"

  ((:template @j/postMessage) 'worker [1 2 3])
  => '(. worker (postMessage [1 2 3])))
