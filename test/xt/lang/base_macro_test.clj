(ns xt.lang.base-macro-test
  (:use code.test)
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.json :as json]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-macro :as k]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.base-macro :as k]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.base-macro :as k]]})

(l/script- :r
  {:runtime :basic
   :require [[xt.lang.base-macro :as k]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.base-macro/for:array :added "4.0"}
(fact "helper function to `for:array`"
  ^:hidden

  (!.js
   (var out := [])
   (k/for:array [e [1 2 3 4]]
     (if (> e 3)
       (break))
     (x:arr-push out e))
   out)
  => [1 2 3]

  (!.py
   (var out := [])
   (k/for:array [e [1 2 3 4]]
     (if (> e 3)
       (break))
     (x:arr-push out e))
   out)
  => [1 2 3]

  (!.R
   (var out := [])
   (k/for:array [e [1 2 3 4]]
     (if (> e 3)
       (break))
     (x:arr-push out e))
   out)
  => [1 2 3]

  (!.lua
   (var out := [])
   (k/for:array [e [1 2 3 4]]
     (if (> e 3)
       (break))
     (x:arr-push out e))
   out)
  => [1 2 3]
  )

^{:refer xt.lang.base-macro/for:object :added "4.0"}
(fact "helper function to `for:object`"
   ^:hidden

  (set (!.js
        (var out := [])
        (var obj := {:a 1 :b 2})
        (k/for:object [[k v] obj]
          (x:arr-push out [k v]))
        out))
   => #{["b" 2] ["a" 1]}

   (set (!.lua
        (var out := [])
        (var obj := {:a 1 :b 2})
        (k/for:object [[k v] obj]
          (x:arr-push out [k v]))
        out))
   => #{["b" 2] ["a" 1]}

   (set (!.py
        (var out := [])
        (var obj := {:a 1 :b 2})
        (k/for:object [[k v] obj]
          (x:arr-push out [k v]))
        out))
   => #{["b" 2] ["a" 1]}

   (set (!.R
        (var out := [])
        (var obj := {:a 1 :b 2})
        (k/for:object [[k v] obj]
          (x:arr-push out [k v]))
        out))
   => #{["b" 2] ["a" 1]})

^{:refer xt.lang.base-macro/for:index :added "4.0"}
(fact "for index call"
  ^:hidden
  
  (!.js
   (var out := [])
   (k/for:index [i [0 10 2]]
     (x:arr-push out i))
   out)
  => [0 2 4 6 8]

  (!.lua
   (var out := [])
   (k/for:index [i [0 10 2]]
     (x:arr-push out i))
   out)
  => [0 2 4 6 8 10]

  (!.py
   (var out := [])
   (k/for:index [i [0 10 2]]
     (x:arr-push out i))
   out)
  => [0 2 4 6 8]

  (!.R
   (var out := [])
   (k/for:index [i [0 10 2]]
     (x:arr-push out i))
   out)
  => [0 2 4 6 8 10])

^{:refer xt.lang.base-macro/for:return :added "4.0"}
(fact "defines a return construct"
  ^:hidden
  
  [(!.js
    (var out)
    (var success (fn [cb]
                   (cb nil "OK")))
    (k/for:return [[ret err] (success (x:callback))]
      {:success (:= out ret)
       :error   (:= out err)})
    out)
   
   (!.js
    (var out)
    (var success (fn [cb]
                   (cb "ERR" nil)))
    (k/for:return [[ret err] (success (x:callback))]
      {:success (:= out ret)
       :error   (:= out err)})
    out)]
  => ["OK" "ERR"]
  
  [(!.lua
    (var out)
    (k/for:return [[ret err] (unpack ["OK" nil])]
      {:success (:= out ret)
       :error   (:= out err)})
    out)

   (!.lua
    (var out)
    (k/for:return [[ret err] (unpack [nil "ERR"])]
      {:success (:= out ret)
       :error   (:= out err)})
    out)]
  => ["OK" "ERR"]

  [(!.py
    (var out := nil)
    (k/for:return [[ret err] "OK"]
      {:success (:= out ret)
       :error   (:= out err)})
    out)
   
   (!.py
    (var out := nil)
    (var f (fn [] (x:err "ERR")))
    (k/for:return [[ret err] (f)]
      {:success (:= out ret)
       :error   (:= out (str err))})
    out)]
  => ["OK" "ERR"]

  [(!.R
    (var out := nil)
    (k/for:return [[ret err] "OK"]
      {:success (:= out ret)
       :error   (:= out err)})
    out)
   
   (!.R
    (var out := nil)
    (k/for:return [[ret err] (stop)]
      {:success (:= out ret)
       :error   (:= out (str err))})
    out)]
  => ["OK" nil])

^{:refer xt.lang.base-macro/for:try :added "4.0"}
(fact "performs try/catch block"
  ^:hidden
  
  (!.js
   (var out := nil)
   (k/for:try [[ret err] (do:> (x:err "hello"))]
     {:success (:= out ret)
      :error   (:= out err)})
   out)
  => "hello"

  (!.lua
   (var out := nil)
   (k/for:try [[ret err] (do:> (x:err "hello"))]
     {:success (:= out ret)
      :error   (:= out err)})
   out)
  => "[string \"local out = nil...\"]:4: hello"

  (!.py ;; TODO
   (var out := nil)
   (k/for:try [[ret err] (do:> (x:err "hello"))]
     {:success (:= out ret)
      :error   (:= out err)})
   out)
  => (throws)

  (!.R ;; TODO
   (var out := nil)
   (k/for:try [[ret err] (do:> (x:err "hello"))]
     {:success (:= out ret)
      :error   (:= out err)})
   out)
  => (throws))

^{:refer xt.lang.base-macro/for:async :added "4.0"}
(fact "performs an async call"
  ^:hidden
  
  (l/with:input
    (!.js
     (var out := nil)
     (k/for:async [[ret err] (+ 1 2 3)]
       {:success (:= out ret)
        :error (:= out err)})))
  => (std.string/|
      "(function (){"
      "  let out = null;"
      "  return new Promise(function (resolve,reject){"
      "    resolve(1 + 2 + 3);"
      "  }).then(function (ret){"
      "    out = ret;"
      "  }).catch(function (err){"
      "    out = err;"
      "  });"
      "})()")

  (l/with:input
    (!.lua
     (var out := nil)
     (k/for:async [[ret err] (+ 1 2 3)]
       {:success (:= out ret)
        :error (:= out err)})))
  => (std.string/|
   "local out = nil"
   "return ngx.thread.spawn(function ()"
   "  local ok,out = pcall(function ()"
   "    return 1 + 2 + 3"
   "  end)"
   "  if ok then"
   "    local ret = out"
   "    out = ret"
   "  else"
   "    local err = out"
   "    out = err"
   "  end"
   "end)"))

^{:refer xt.lang.base-macro/invoke :added "4.0"}
(fact "calls the function on rest of arguments"
  ^:hidden
  
  (!.js (k/invoke k/add 1 2))
  => 3

  (!.lua (k/invoke k/add 1 2))
  => 3

  (!.py (k/invoke k/add 1 2))
  => 3

  (!.R (k/invoke k/add 1 2))
  => 3)

^{:refer xt.lang.base-macro/add :added "4.0"}
(fact "performs add operation"
  ^:hidden
  
  (!.js (k/add 1 2))
  => 3
  
  (!.lua (k/add 1 2))
  => 3
  
  (!.py (k/add 1 2))
  => 3

  (!.R (k/add 1 2))
  => 3)

^{:refer xt.lang.base-macro/sub :added "4.0"}
(fact "performs sub operation"
  ^:hidden
  
  (!.js (k/sub 1 2))
  => -1

  (!.lua (k/sub 1 2))
  => -1

  (!.py (k/sub 1 2))
  => -1

  (!.R (k/sub 1 2))
  => -1)

^{:refer xt.lang.base-macro/mul :added "4.0"}
(fact "perform multiply operation"
  ^:hidden
  
  (!.js (k/mul 10 10))
  => 100

  (!.lua (k/mul 10 10))
  => 100

  (!.py (k/mul 10 10))
  => 100

  (!.R (k/mul 10 10))
  => 100)

^{:refer xt.lang.base-macro/div :added "4.0"}
(fact "perform divide operation"
  ^:hidden
  
  (!.js (k/div 10 2))
  => 5

  (!.lua (k/div 10 2))
  => 5

  (!.py (k/div 10 2))
  => 5.0

  (!.R (k/div 10 2))
  => 5)

^{:refer xt.lang.base-macro/gt :added "4.0"}
(fact "greater than"
  ^:hidden
  
  (!.js [(k/gt 2 2) (k/gt 2 1)])
  => [false true]

  (!.lua [(k/gt 2 2) (k/gt 2 1)])
  => [false true]

  (!.py [(k/gt 2 2) (k/gt 2 1)])
  => [false true]

  (!.R [(k/gt 2 2) (k/gt 2 1)])
  => [false true])

^{:refer xt.lang.base-macro/lt :added "4.0"}
(fact "less than"
  ^:hidden
  
  (!.js [(k/lt 2 2)  (k/lt 1 2)])
  => [false true]

  (!.lua [(k/lt 2 2) (k/lt 1 2)])
  => [false true]

  (!.py [(k/lt 2 2)  (k/lt 1 2)])
  => [false true]

  (!.R [(k/lt 2 2)  (k/lt 1 2)])
  => [false true])

^{:refer xt.lang.base-macro/gte :added "4.0"}
(fact "greater than or equal to"
  ^:hidden
  
  (!.js [(k/gte 2 2) (k/gte 2 1) (k/gte 1 2)])
  => [true true false]

  (!.lua [(k/gte 2 2) (k/gte 2 1) (k/gte 1 2)])
  => [true true false]
  
  (!.py [(k/gte 2 2) (k/gte 2 1) (k/gte 1 2)])
  => [true true false]

  (!.R [(k/gte 2 2) (k/gte 2 1) (k/gte 1 2)])
  => [true true false])

^{:refer xt.lang.base-macro/lte :added "4.0"}
(fact "less than or equal to"
  ^:hidden
  
  (!.js [(k/lte 2 2) (k/lte 2 1) (k/lte 1 2)])
  => [true false true]

  (!.lua [(k/lte 2 2) (k/lte 2 1) (k/lte 1 2)])
  => [true false true]

  (!.py [(k/lte 2 2) (k/lte 2 1) (k/lte 1 2)])
  => [true false true]

  (!.R [(k/lte 2 2) (k/lte 2 1) (k/lte 1 2)])
  => [true false true])

^{:refer xt.lang.base-macro/eq :added "4.0"}
(fact "equal to"
  ^:hidden
  
  (!.js [(k/eq 2 2) (k/eq 2 1)])
  => [true false]

  (!.lua [(k/eq 2 2) (k/eq 2 1)])
  => [true false]

  (!.py [(k/eq 2 2) (k/eq 2 1)])
  => [true false]

  (!.R [(k/eq 2 2) (k/eq 2 1)])
  => [true false])

^{:refer xt.lang.base-macro/neq :added "4.0"}
(fact "not equal to"
  ^:hidden
  
  (!.js [(k/neq 2 2) (k/neq 2 1)])
  => [false true]

  (!.lua [(k/neq 2 2) (k/neq 2 1)])
  => [false true]

  (!.py [(k/neq 2 2) (k/neq 2 1)])
  => [false true]

  (!.R [(k/neq 2 2) (k/neq 2 1)])
  => [false true])

^{:refer xt.lang.base-macro/neg :added "4.0"}
(fact "negative function"
  ^:hidden
  
  (!.js (k/neg 1))
  => -1

  (!.lua (k/neg 1))
  => -1

  (!.py (k/neg 1))
  => -1

  (!.R (k/neg 1))
  => -1)

^{:refer xt.lang.base-macro/inc :added "4.0"}
(fact "increment function"
  ^:hidden
  
  (!.js (k/inc 1))
  => 2

  (!.lua (k/inc 1))
  => 2

  (!.py (k/inc 1))
  => 2

  (!.R (k/inc 1))
  => 2)

^{:refer xt.lang.base-macro/dec :added "4.0"}
(fact "decrement function"
  ^:hidden
  
  (!.js (k/dec 1))
  => 0

  (!.lua (k/dec 1))
  => 0

  (!.py (k/dec 1))
  => 0

  (!.R (k/dec 1))
  => 0)

^{:refer xt.lang.base-macro/zero? :added "4.0"}
(fact "zero check"
  ^:hidden
  
  (!.js  [(k/zero? 1)
          (k/zero? 0)])
  => [false true]
  
  (!.lua [(k/zero? 1)
          (k/zero? 0)])
  => [false true]
  
  (!.py [(k/zero? 1)
         (k/zero? 0)])
  => [false true]

  (!.R [(k/zero? 1)
         (k/zero? 0)])
  => [false true])

^{:refer xt.lang.base-macro/pos? :added "4.0"}
(fact "positive check"
  ^:hidden
  
  (!.js [(k/pos? 1)
         (k/pos? 0)])
  => [true false]

  (!.lua [(k/pos? 1)
         (k/pos? 0)])
  => [true false]

  (!.py [(k/pos? 1)
         (k/pos? 0)])
  => [true false]

  (!.R [(k/pos? 1)
        (k/pos? 0)])
  => [true false])

^{:refer xt.lang.base-macro/neg? :added "4.0"}
(fact "negative check"
  ^:hidden

  (!.js [(k/neg? -1)
         (k/neg? 0)])
  => [true false]

  (!.lua [(k/neg? -1)
         (k/neg? 0)])
  => [true false]

  (!.py [(k/neg? -1)
         (k/neg? 0)])
  => [true false]

  (!.R [(k/neg? -1)
        (k/neg? 0)])
  => [true false])

^{:refer xt.lang.base-macro/even? :added "4.0"}
(fact "even check"
  ^:hidden
  
  (!.js [(k/even? 2)
         (k/even? 1)])
  => [true false]

  (!.lua [(k/even? 2)
          (k/even? 1)])
  => [true false]

  (!.py [(k/even? 2)
         (k/even? 1)])
  => [true false]

  (!.R [(k/even? 2)
        (k/even? 1)])
  => [true false])

^{:refer xt.lang.base-macro/odd? :added "4.0"}
(fact "odd check"
  ^:hidden
  
  (!.js [(k/odd? 2)
         (k/odd? 1)])
  => [false true]

  (!.lua [(k/odd? 2)
          (k/odd? 1)])
  => [false true]

  (!.py [(k/odd? 2)
         (k/odd? 1)])
  => [false true]
  
  (!.R [(k/odd? 2)
         (k/odd? 1)])
  => [false true])

^{:refer xt.lang.base-macro/lt-string :added "4.0"}
(fact "checks if a is ordered before b"
  ^:hidden
  
  (!.js [(k/lt-string "a" "b")
         (k/lt-string "A" "a")])
  => [true false]

  (!.lua [(k/lt-string "a" "b")
          (k/lt-string "a" "A" )])
  => [true false]
  
  (!.py [(k/lt-string "a" "b")
         (k/lt-string "a" "A")])
  => [true false])

^{:refer xt.lang.base-macro/gt-string :added "4.0"}
(fact "checks if a is ordered before b"
  ^:hidden
  
  (!.js [(k/gt-string "a" "b")
         (k/gt-string "A" "a")])
  => [false true]

  (!.lua [(k/gt-string "a" "b")
          (k/gt-string "a" "A" )])
  => [false true]
  
  (!.py [(k/gt-string "a" "b")
         (k/gt-string "a" "A")])
  => [false true])

^{:refer xt.lang.base-macro/this :added "4.0"}
(fact "gets the current "
  ^:hidden
  
  (!.js
   (var mt (xt.lang.base-lib/proto-create
            {:hello (fn:> [v]
                      (. v world))}))
   (var a {:world "hello"})
   (k/set-proto a mt)
   (. a (hello)))
  => "hello"
  
  (!.lua
   (var mt (xt.lang.base-lib/proto-create
            {:hello (fn:> [v]
                      (. v world))}))
   (var a {:world "hello"})
   (k/set-proto a mt)
   (. a (hello)))
  => "hello")

^{:refer xt.lang.base-macro/set-proto :added "4.0"}
(fact "sets the prototype")

^{:refer xt.lang.base-macro/get-proto :added "4.0"}
(fact "gets the prototype")

^{:refer xt.lang.base-macro/abs :added "4.0"}
(fact "gets the absolute value"
  ^:hidden
  
  (!.js
   [(k/abs -1) (k/abs 1)])
  => [1 1]
  
  (!.lua
   [(k/abs -1) (k/abs 1)])
  => [1 1]

  (!.py
   [(k/abs -1) (k/abs 1)])
  => [1 1]

  (!.R
   [(k/abs -1) (k/abs 1)])
  => [1 1])

^{:refer xt.lang.base-macro/acos :added "4.0"}
(fact "gets the arc cos value"
  ^:hidden
  
  (!.js (k/acos 0.4))
  => 1.1592794807274085

  (!.lua (k/acos 0.4))
  => 1.1592794807274

  (!.py (k/acos 0.4))
  => 1.1592794807274085

  (!.R (k/acos 0.4))
  => 1.1593)

^{:refer xt.lang.base-macro/asin :added "4.0"}
(fact "gets the arc sin value"
  ^:hidden
  
  (!.js (k/asin 0.4))
  => 0.41151684606748806

  (!.lua (k/asin 0.4))
  => 0.41151684606749

  (!.py (k/asin 0.4))
  => 0.41151684606748806

  (!.R (k/asin 0.4))
  => 0.4115)

^{:refer xt.lang.base-macro/atan :added "4.0"}
(fact "gets the arc tan value"
  ^:hidden
  
  (!.js (k/atan 0.4))
  => 0.3805063771123649
  
  (!.lua (k/atan 0.4))
  => 0.38050637711236
  
  (!.py (k/atan 0.4))
  => 0.3805063771123649

  (!.R (k/atan 0.4))
  => 0.3805)

^{:refer xt.lang.base-macro/ceil :added "4.0"}
(fact "gets the ceiling"
  ^:hidden
  
  (!.js (k/ceil 1.1))
  => 2

  (!.lua (k/ceil 1.1))
  => 2

  (!.py (k/ceil 1.1))
  => 2

  (!.R (k/ceil 1.1))
  => 2)

^{:refer xt.lang.base-macro/cos :added "4.0"}
(fact "gets the cos value"
  ^:hidden
  
  (!.js (k/cos 1.1))
  => 0.4535961214255773

  (!.lua (k/cos 1.1))
  => 0.45359612142558

  (!.py (k/cos 1.1))
  => 0.4535961214255773

  (!.R (k/cos 1.1))
  => 0.4536)

^{:refer xt.lang.base-macro/cosh :added "4.0"}
(fact "gets the cosh value"
  ^:hidden
  
  (!.js (k/cosh 1.1))
  => 1.6685185538222564

  (!.lua (k/cosh 1.1))
  => 1.6685185538223

  (!.py (k/cosh 1.1))
  => 1.6685185538222564

  (!.R (k/cosh 1.1))
  => 1.6685)

^{:refer xt.lang.base-macro/exp :added "4.0"}
(fact "gets the `e^x` value"
  ^:hidden
  
  (!.js (k/exp 1.1))
  => 3.0041660239464334

  (!.lua (k/exp 1.1))
  => 3.0041660239464

  (!.py (k/exp 1.1))
  => 3.0041660239464334

  (!.R (k/exp 1.1))
  => 3.0042)

^{:refer xt.lang.base-macro/floor :added "4.0"}
(fact "gets the floor"
  ^:hidden
  
  (!.js (k/floor 1.1))
  => 1

  (!.lua (k/floor 1.1))
  => 1

  (!.py (k/floor 1.1))
  => 1

  (!.R (k/floor 1.1))
  => 1)

^{:refer xt.lang.base-macro/loge :added "4.0"}
(fact "gets the natural log"
  ^:hidden
  
  (!.js (k/loge 3.0041660239464334))
  => 1.1

  (!.lua (k/loge 3.0041660239464334))
  => 1.1

  (!.py (k/loge 3.0041660239464334))
  => 1.1

  (!.R (k/loge 3.0041660239464334))
  => 1.1)

^{:refer xt.lang.base-macro/log10 :added "4.0"}
(fact "gets the log base 10"
  ^:hidden
  
  (!.js (k/log10 3.0041660239464334))
  => 0.47772393009357705

  (!.lua (k/log10 3.0041660239464334))
  => 0.47772393009358

  (!.py (k/log10 3.0041660239464334))
  => 0.47772393009357705

  (!.R (k/log10 3.0041660239464334))
  => 0.4777)

^{:refer xt.lang.base-macro/max :added "4.0"}
(fact "gets the maximum value"
  ^:hidden
  
  (!.js (k/max 1 2 3 2))
  => 3

  (!.lua (k/max 1 2 3 2))
  => 3

  (!.py (k/max 1 2 3 2))
  => 3

  (!.R (k/max 1 2 3 2))
  => 3)

^{:refer xt.lang.base-macro/min :added "4.0"}
(fact "gets the minimum value"
  ^:hidden
  
  (!.js (k/min 1 2 1 2))
  => 1

  (!.lua (k/min 1 2 1 2))
  => 1

  (!.py (k/min 1 2 1 2))
  => 1

  (!.R (k/min 1 2 1 2))
  => 1)

^{:refer xt.lang.base-macro/mod :added "4.0"}
(fact "gets the mod"
  ^:hidden
  
  (!.js (k/mod 2 4))
  => 2

  (!.lua (k/mod 2 4))
  => 2

  (!.py (k/mod 2 4))
  => 2

  (!.R (k/mod 2 4))
  => 2)

^{:refer xt.lang.base-macro/quot :added "4.0"}
(fact "gets the quotient"
  ^:hidden
  
  (!.js [(k/quot 111 4)
         (k/quot -111 4)])
  => [27 -28]
  
  (!.lua [(k/quot 111 4)
          (k/quot -111 4)])
  => [27 -28]

  (!.py [(k/quot 111 4)
         (k/quot -111 4)])
  => [27 -28]

  (!.R [(k/quot 111 4)
        (k/quot -111 4)])
  => [27 -28])

^{:refer xt.lang.base-macro/pow :added "4.0"}
(fact "gets the power"
  ^:hidden
  
  (!.js (k/pow 2 4))
  => 16

  (!.lua (k/pow 2 4))
  => 16

  (!.py (k/pow 2 4))
  => 16

  (!.R (k/pow 2 4))
  => 16)

^{:refer xt.lang.base-macro/sin :added "4.0"}
(fact "gets the sin"
  ^:hidden
  
  (!.js (k/sin 0.3))
  => 0.29552020666133955

  (!.lua (k/sin 0.3))
  => 0.29552020666134

  (!.py (k/sin 0.3))
  => 0.29552020666133955)

^{:refer xt.lang.base-macro/sinh :added "4.0"}
(fact "gets the sinh"
  ^:hidden
  
  (!.js (k/sinh 0.3))
  => 0.3045202934471426

  (!.lua (k/sinh 0.3))
  => 0.30452029344714
  
  (!.py (k/sinh 0.3))
  => 0.3045202934471426

  (!.R (k/sinh 0.3))
  => 0.3045)

^{:refer xt.lang.base-macro/sqrt :added "4.0"}
(fact "gets the square root"
  ^:hidden
  
  (!.js (k/sqrt 0.3))
  => 0.5477225575051661

  (!.lua (k/sqrt 0.3))
  => 0.54772255750517
  
  (!.py (k/sqrt 0.3))
  => 0.5477225575051661

  (!.R (k/sqrt 0.3))
  => 0.5477)

^{:refer xt.lang.base-macro/tan :added "4.0"}
(fact "gets the tan"
  ^:hidden
  
  (!.js (k/tan 0.3))
  => 0.30933624960962325

  (!.lua (k/tan 0.3))
  => 0.30933624960962

  (!.py (k/tan 0.3))
  => (approx 0.309336249609623)

  (!.R (k/tan 0.3))
  => 0.3093)

^{:refer xt.lang.base-macro/tanh :added "4.0"}
(fact "gets the tanh"
  ^:hidden
  
  (!.js (k/tanh 0.3))
  => 0.2913126124515909

  (!.lua (k/tanh 0.3))
  => 0.29131261245159

  (!.py (k/tanh 0.3))
  => 0.2913126124515909

  (!.R (k/tanh 0.3))
  => 0.2913)

^{:refer xt.lang.base-macro/cat :added "4.0"}
(fact "concat strings together"
  ^:hidden
  
  (!.js
   (k/cat "1" "2" "3" "4" "5"))
  => "12345"

  (!.lua
   (k/cat "1" "2" "3" "4" "5"))
  => "12345"

  (!.py
   (k/cat "1" "2" "3" "4" "5"))
  => "12345"

  (!.R
   (k/cat "1" "2" "3" "4" "5"))
  => "12345")

^{:refer xt.lang.base-macro/len :added "4.0"}
(fact "gets the length of an array"
  ^:hidden
    
  (!.js
   (k/len [1 2 3 4 5]))
  => 5
  
  (!.lua
   (k/len [1 2 3 4 5]))
  => 5
  
  (!.py
   (k/len [1 2 3 4 5]))
  
  (!.R
   (k/len [1 2 3 4 5]))
  => 5)

^{:refer xt.lang.base-macro/err :added "4.0"}
(fact "throws an error"
  ^:hidden
  
  (!.js
   (k/err "There is an error")
   true)
  => (throws)
  
  (!.lua
   (k/err "There is an error")
   true)
  => (throws)
  
  (!.py
   (k/err "There is an error")
   true)
  => (throws)

  (!.R
   (k/err "There is an error")
   true)
  => (throws))

^{:refer xt.lang.base-macro/throw :added "4.0"}
(fact "base version of throw")

^{:refer xt.lang.base-macro/eval :added "4.0"}
(fact "evaluates a string"
  ^:hidden
  
  (!.js
   (k/eval "1+2"))
  => 3

  (!.lua
   (k/eval "1+2"))
  => 3
  
  (!.py
   (k/eval "1+2"))
  => 3

  (!.R
   (k/eval "1+2"))
  => 3)

^{:refer xt.lang.base-macro/apply :added "4.0"}
(fact "applies a function to an array"
  ^:hidden
  
  (!.js
   (k/apply (fn:> [a b] (+ a b))
              [1 2]))
  => 3

  (!.lua
   (k/apply '((fn:> [a b] (+ a b)))
              [1 2]))
  => 3

  (!.py
   (k/apply (fn:> [a b] (+ a b))
              [1 2]))
  => 3

  (!.R
   (k/apply (fn:> [a b] (+ a b))
              [1 2]))
  => 3)

^{:refer xt.lang.base-macro/print :added "4.0"}
(fact "prints a string (for debugging)"
  ^:hidden
  
  (!.js
   (k/print "hello"))
  => nil
  
  (!.lua
   (k/print "hello"))
  => nil
  
  (!.py
   (k/print "hello"))
  => nil
  
  (!.R
   (k/print "hello"))
  => "hello")

^{:refer xt.lang.base-macro/unpack :added "4.0"}
(fact "unpacks an array into another"
  ^:hidden
  
  (!.js
   [(k/unpack [1 2 3]) (k/unpack [4 5 6])])
  => [1 2 3 4 5 6]

  (!.lua ;; ONLY ALLOWS SINGLE UNPACK
   [(k/unpack [1 2 3]) (k/unpack [4 5 6])])
  => [1 4 5 6]

  (!.py
   [(k/unpack [1 2 3]) (k/unpack [4 5 6])])
  => [1 2 3 4 5 6])

^{:refer xt.lang.base-macro/now-ms :added "4.0"}
(fact "gets the current millisecond time"
  ^:hidden
  
  (count
   (str (!.js
         (k/now-ms))))
  => 13

  (count
   (str (!.lua
         (k/now-ms))))
  => 13

  (count
   (str (!.py
         (k/now-ms))))
  => 13

  (count
   (str (!.R
         (k/now-ms))))
  => 13)

^{:refer xt.lang.base-macro/random :added "4.0"}
(fact "generates a random float"
  ^:hidden
  
  (!.js (k/random))
  => float?
  
  (!.lua (k/random))
  => float?
  
  (!.py (k/random))
  => float?

  (!.R (k/random))
  => float?)

^{:refer xt.lang.base-macro/not-nil? :added "4.0"}
(fact "checks that value is not nil"
  ^:hidden
  
  (!.js
   [(k/not-nil? 1) (k/not-nil? nil)])
  => [true false]
  
  (!.lua
   [(k/not-nil? 1) (k/not-nil? nil)])
  => [true false]
  
  (!.py
   [(k/not-nil? 1) (k/not-nil? nil)])
  => [true false]

  (!.R
   [(k/not-nil? 1) (k/not-nil? nil)])
  => [true false])

^{:refer xt.lang.base-macro/nil? :added "4.0"}
(fact "checks that value is nil"
  ^:hidden
  
  (!.js
   [(k/nil? 1) (k/nil? nil)])
  => [false true]
  
  (!.lua
   [(k/nil? 1) (k/nil? nil)])
  => [false true]
  
  (!.py
   [(k/nil? 1) (k/nil? nil)])
  => [false true]

  (!.R
   [(k/nil? 1) (k/nil? nil)])
  => [false true])

^{:refer xt.lang.base-macro/to-string :added "4.0"}
(fact "converts an object into a string"
  ^:hidden
  
  (!.js
   [1 (k/to-string 1)])
  => [1 "1"]

  (!.lua
   [1 (k/to-string 1)])
  => [1 "1"]

  (!.py
   [1 (k/to-string 1)])
  => [1 "1"]

  (!.R
   [1 (k/to-string 1)])
  => [1 "1"])

^{:refer xt.lang.base-macro/to-number :added "4.0"}
(fact "converts a string to a number"
  ^:hidden
  
  (!.js
   [(k/to-number "1") (k/to-number "1.1")])
  => [1 1.1]

  (!.lua
   [(k/to-number "1") (k/to-number "1.1")])
  => [1 1.1]

  (!.py
   [(k/to-number "1") (k/to-number "1.1")])
  => [1.0 1.1]

  (!.R
   [(k/to-number "1") (k/to-number "1.1")])
  => [1 1.1])

^{:refer xt.lang.base-macro/is-string? :added "4.0"}
(fact "checks if object is a string"
  ^:hidden
  
  (!.js
   [(k/is-string? "a") (k/is-string? 1)])
  => [true false]

  (!.lua
   [(k/is-string? "a") (k/is-string? 1)])
  => [true false]

  (!.py
   [(k/is-string? "a") (k/is-string? 1)])
  => [true false]

  (!.R
   [(k/is-string? "a") (k/is-string? 1)])
  => [true false])

^{:refer xt.lang.base-macro/is-number? :added "4.0"}
(fact "checks if object is a number"
  ^:hidden
  
  (!.js
   [(k/is-number? 1) (k/is-number? 1.1) (k/is-number? "a")])
  => [true true false]
  
  (!.lua
   [(k/is-number? 1) (k/is-number? 1.1) (k/is-number? "a")])
  => [true true false]

  (!.py
   [(k/is-number? 1) (k/is-number? 1.1) (k/is-number? "a")])
  => [true true false]

  (!.R
   [(k/is-number? 1) (k/is-number? 1.1) (k/is-number? "a")])
  => [true true false])

^{:refer xt.lang.base-macro/is-integer? :added "4.0"}
(fact "checks that number is an integer"
  ^:hidden
  
  (!.js
   [(k/is-integer? 1.1) (k/is-integer? 1)])
  => [false true]

  (!.lua
   [(k/is-integer? 1.1) (k/is-integer? 1)])
  => [false true]

  (!.py
   [(k/is-integer? 1.1) (k/is-integer? 1)])
  => [false true])

^{:refer xt.lang.base-macro/is-boolean? :added "4.0"}
(fact "checks if object is a boolean"
  ^:hidden
  
  (!.js
   [(k/is-boolean? true) (k/is-boolean? 1)])
  => [true false]

  (!.lua
   [(k/is-boolean? true) (k/is-boolean? 1)])
  => [true false]

  (!.py
   [(k/is-boolean? true) (k/is-boolean? 1)])
  => [true false]

  (!.R
   [(k/is-boolean? true) (k/is-boolean? 1)])
  => [true false])

^{:refer xt.lang.base-macro/is-function? :added "4.0"}
(fact "checks if object is a function type"
  ^:hidden
  
  (!.js
   [(k/is-function? (fn:>)) (k/is-function? k/first) (k/is-function? 1)])
  => [true true false]
  
  (!.lua
   [(k/is-function? (fn:>)) (k/is-function? k/first) (k/is-function? 1)])
  => [true true false]
  
  (!.py
   [(k/is-function? (fn:>)) (k/is-function? k/first) (k/is-function? 1)])
  => [true true false]

  (!.R
   [(k/is-function? (fn:>)) (k/is-function? k/first) (k/is-function? 1)])
  => [true true false])

^{:refer xt.lang.base-macro/is-array? :added "4.0"}
(fact "checks if object is an is-arrayay"
  ^:hidden
  
  (!.js
   [(k/is-array? [1 2 3]) (k/is-array? {:a 1})])
  => [true false]

  (!.lua
   [(k/is-array? [1 2 3]) (k/is-array? {:a 1})])
  => [true false]

  (!.py
   [(k/is-array? [1 2 3]) (k/is-array? {:a 1})])
  => [true false]

  (!.R
   [(k/is-array? [1 2 3]) (k/is-array? {:a 1})])
  => [true false])

^{:refer xt.lang.base-macro/is-object? :added "4.0"}
(fact "checks if is-objectect is a map type"
  ^:hidden
  
  (!.js
   [(k/is-object? {:a 1}) (k/is-object? [1 2 3])])
  => [true false]

  (!.lua
   [(k/is-object? {:a 1}) (k/is-object? [1 2 3])])
  => [true false]

  (!.py
   [(k/is-object? {:a 1}) (k/is-object? [1 2 3])])
  => [true false]

  (!.R
   [(k/is-object? {:a 1}) (k/is-object? [1 2 3])])
  => [true false])

^{:refer xt.lang.base-macro/first :added "4.0"}
(fact "gets the first item"
  ^:hidden
  
  (!.js
   (k/first [1 2 3 4]))
  => 1
  
  (!.lua
   (k/first [1 2 3 4]))
  => 1

  (!.py
   (k/first [1 2 3 4]))
  => 1)

^{:refer xt.lang.base-macro/second :added "4.0"}
(fact "gets the second item"
  ^:hidden
  
  (!.js
   (k/second [1 2 3 4]))
  => 2
  
  (!.lua
   (k/second [1 2 3 4]))
  => 2

  (!.py
   (k/second [1 2 3 4]))
  => 2

  (!.R
   (k/second [1 2 3 4]))
  => 2)

^{:refer xt.lang.base-macro/nth :added "4.0"}
(fact "gets the nth item (index 0)"
  ^:hidden
  
  (!.js
   (k/nth [1 2 3 4] 0))
  => 1

  (!.lua
   (k/nth [1 2 3 4] 0))
  => 1

  (!.py
   (k/nth [1 2 3 4] 0))
  => 1

  (!.R
   (k/nth [1 2 3 4] 0))
  => 1)

^{:refer xt.lang.base-macro/last :added "4.0"}
(fact "gets the last item"
  ^:hidden
  
  (!.js
   (k/last [1 2 3 4]))
  => 4

  (!.lua
   (k/last [1 2 3 4]))
  => 4
  
  (!.py
   (k/last [1 2 3 4]))
  => 4

  (!.R
   (k/last [1 2 3 4]))
  => 4)

^{:refer xt.lang.base-macro/second-last :added "4.0"}
(fact "gets the second-last item"
  ^:hidden
  
  (!.js
   (k/second-last [1 2 3 4]))
  => 3

  (!.lua
   (k/second-last [1 2 3 4]))
  => 3
  
  (!.py
   (k/second-last [1 2 3 4]))
  => 3

  (!.R
   (k/second-last [1 2 3 4]))
  => 3)

^{:refer xt.lang.base-macro/bit-and :added "4.0"}
(fact "bit and operation"
  ^:hidden
  
  (!.js
   (k/bit-and 7 4))
  => 4

  (!.lua
   (k/bit-and 7 4))
  => 4

  (!.py
   (k/bit-and 7 4))
  => 4)

^{:refer xt.lang.base-macro/bit-or :added "4.0"}
(fact "bit or operation"
  ^:hidden
  
  (!.js
   (k/bit-or 3 4))
  => 7

  (!.lua
   (k/bit-or 3 4))
  => 7

  (!.py
   (k/bit-or 3 4))
  => 7)

^{:refer xt.lang.base-macro/bit-xor :added "4.0"}
(fact "bit xor operation"
  ^:hidden
  
  (!.js
   (k/bit-xor 3 5))
  => 6

  (!.lua
   (k/bit-xor 3 5))
  => 6

  (!.py
   (k/bit-xor 3 5))
  => 6)

^{:refer xt.lang.base-macro/bit-lshift :added "4.0"}
(fact "bit left shift"
  ^:hidden
  
  (!.js
   (k/bit-lshift 7 1))
  => 14

  (!.lua
   (k/bit-lshift 7 1))
  => 14

  (!.py
   (k/bit-lshift 7 1))
  => 14)

^{:refer xt.lang.base-macro/bit-rshift :added "4.0"}
(fact "bit right shift"
  ^:hidden
  
  (!.js
   (k/bit-rshift 7 1))
  => 3

  (!.lua
   (k/bit-rshift 7 1))
  => 3

  (!.py
   (k/bit-rshift 7 1))
  => 3)

^{:refer xt.lang.base-macro/lu-create :added "4.0"}
(fact "creates a lookup")

^{:refer xt.lang.base-macro/lu-eq :added "4.0"}
(fact "equality of objects"
  ^:hidden
  
  (!.js
   (var a {})
   [(k/lu-eq {} a)
    (k/lu-eq a a)])
  => [false true]

  (!.lua
   (var a {})
   [(k/lu-eq {} a)
    (k/lu-eq a a)])
  => [false true]
  
  (!.py
   (var a {})
   [(k/lu-eq {} a)
    (k/lu-eq a a)])
  => [false true]

  (!.R
   (var a {})
   [(k/lu-eq {} a)
    (k/lu-eq a a)])
  => [false true])

^{:refer xt.lang.base-macro/lu-get :added "4.0"}
(fact "gets value given an object"
  ^:hidden
  
  (!.js
   (var a {})
   (var b {})
   (var c {})
   (var lu (k/lu-create))
   (k/lu-set lu a 1)
   (k/lu-set lu b 2)
   (k/lu-set lu c 3)
   (var cv (k/lu-get lu c))
   (k/lu-del lu c)
   [(k/lu-get lu a)
    (k/lu-get lu b)
    cv
    (x:nil? (k/lu-get lu c))])
  => [1 2 3 true]

  (!.lua
   (var a {})
   (var b {})
   (var c {})
   (var lu (k/lu-create))
   (k/lu-set lu a 1)
   (k/lu-set lu b 2)
   (k/lu-set lu c 3)
   (var cv (k/lu-get lu c))
   (k/lu-del lu c)
   [(k/lu-get lu a)
    (k/lu-get lu b)
    cv
    (x:nil? (k/lu-get lu c))])
  => [1 2 3 true]

  (!.py
   (var a {})
   (var b {})
   (var c {})
   (var lu (k/lu-create))
   (k/lu-set lu a 1)
   (k/lu-set lu b 2)
   (k/lu-set lu c 3)
   (var cv (k/lu-get lu c))
   (k/lu-del lu c)
   [(k/lu-get lu a)
    (k/lu-get lu b)
    cv
    (x:nil? (k/lu-get lu c))])
  => [1 2 3 true]

  
  (!.R
   (var a {})
   (var b {})
   (var c {})
   (var lu (k/lu-create))
   (k/lu-set lu a 1)
   (k/lu-set lu b 2)
   (k/lu-set lu c 3)
   (var cv (k/lu-get lu c))
   (k/lu-del lu c)
   [(k/lu-get lu a)
    (k/lu-get lu b)
    cv
    (x:nil? (k/lu-get lu c))])
  => [1 2 3 true])

^{:refer xt.lang.base-macro/lu-set :added "4.0"}
(fact "sets value given an object")

^{:refer xt.lang.base-macro/lu-del :added "4.0"}
(fact "deletes value given an object")

^{:refer xt.lang.base-macro/global-set :added "4.0"}
(fact "sets the global variable"
  ^:hidden
  
  (!.js
   (k/global-del "HELLO")
   (var old := (k/global-has? "HELLO"))
   (k/global-set "HELLO" 1)
   [old (k/global-has? "HELLO") (!:G HELLO)])
  => [false true 1]

  (!.lua
   (k/global-del "HELLO")
   (var old := (k/global-has? "HELLO"))
   (k/global-set "HELLO" 1)
   [old (k/global-has? "HELLO") (!:G HELLO)])
  => [false true 1]

  (!.py
   (k/global-del "HELLO")
   (var old := (k/global-has? "HELLO"))
   (k/global-set "HELLO" 1)
   [old (k/global-has? "HELLO") (!:G HELLO)])
  => [false true 1]

  (!.R
   (k/global-del "HELLO")
   (var old := (k/global-has? "HELLO"))
   (k/global-set "HELLO" 1)
   [old (k/global-has? "HELLO") (!:G HELLO)])
  => [false true 1])

^{:refer xt.lang.base-macro/global-del :added "4.0"}
(fact "deletes a global var")

^{:refer xt.lang.base-macro/global-has? :added "4.0"}
(fact "has a global var")

^{:refer xt.lang.base-macro/has-key? :added "4.0"}
(fact "checks that key in contained in object"
  ^:hidden
  
  (!.js
   [(k/has-key? {:a 1} "a")
    (k/has-key? {:a 1} "b")])
  => [true false]

  (!.lua
   [(k/has-key? {:a 1} "a")
    (k/has-key? {:a 1} "b")])
  => [true false]

  (!.py
   [(k/has-key? {:a 1} "a")
    (k/has-key? {:a 1} "b")])
  => [true false]

  (!.R
   [(k/has-key? {:a 1} "a")
    (k/has-key? {:a 1} "b")])
  => [true false])

^{:refer xt.lang.base-macro/del-key :added "4.0"}
(fact "deletes a key from object"
  ^:hidden
  
  (!.js
   (var out := {:a 1 :b 2})
   (var rout := out)
   (k/del-key out "a")
   rout)
  => {"b" 2}

  (!.lua
   (var out := {:a 1 :b 2})
   (var rout := out)
   (k/del-key out "a")
   rout)
  => {"b" 2}

  (!.py
   (var out := {:a 1 :b 2})
   (var rout := out)
   (k/del-key out "a")
   rout)
  => {"b" 2}

  ^:fails
  (!.R
   (var out := {:a 1 :b 2})
   (var rout := out)
   (k/del-key out "a")
   [out rout])
  => [{"b" 2} {"a" 1, "b" 2}])

^{:refer xt.lang.base-macro/get-key :added "4.0"}
(fact "gets a value"
  ^:hidden
  
  (!.js
   [(k/get-key {:a 1} "a")
    (k/get-key {} "a")])
  => [1 nil]
  
  (!.lua
   [(k/get-key {:a 1} "a")
    (k/get-key {} "a")])
  => [1]

  (!.py
   [(k/get-key {:a 1} "a")
    (k/get-key {} "a")])
  => [1 nil]

  (!.R
   [(k/get-key {:a 1} "a")
    (k/get-key {} "a")])
  => [1 nil])

^{:refer xt.lang.base-macro/get-path :added "4.0"}
(fact "gets the value in the path"
  ^:hidden

  (!.js
   [(k/get-path {:a {:b {:c 1}}} ["a" "b"])
    (k/get-path {:a {:b {:c 1}}} ["a" "b" "c"])])
  => [{"c" 1} 1]

  (!.lua
   [(k/get-path {:a {:b {:c 1}}} ["a" "b"])
    (k/get-path {:a {:b {:c 1}}} ["a" "b" "c"])])
  => [{"c" 1} 1]

  (!.py
   [(k/get-path {:a {:b {:c 1}}} ["a" "b"])
    (k/get-path {:a {:b {:c 1}}} ["a" "b" "c"])])
  => [{"c" 1} 1]

  (!.R
   [(k/get-path {:a {:b {:c 1}}} ["a" "b"])
    (k/get-path {:a {:b {:c 1}}} ["a" "b" "c"])])
  => [{"c" 1} 1])

^{:refer xt.lang.base-macro/get-idx :added "4.0"}
(fact "gets a value in the array (no offsets)"
  ^:hidden
  
  (!.js
   [(k/get-idx [1 2 3] 1)
    (k/get-idx [1 2 3] 2)])
  => [2 3]

  (!.lua
   [(k/get-idx [1 2 3] 1)
    (k/get-idx [1 2 3] 2)])
  => [1 2]

  (!.py
   [(k/get-idx [1 2 3] 1)
    (k/get-idx [1 2 3] 2)])
  => [2 3]

  (!.R
   [(k/get-idx [1 2 3] 1)
    (k/get-idx [1 2 3] 2)])
  => [1 2])

^{:refer xt.lang.base-macro/set-key :added "4.0"}
(fact "sets a key in the object"
  ^:hidden
  
  (!.js
   (var obj {:b 2})
   (var robj := obj)
   (k/set-key obj "a" 1)
   robj)
  => {"a" 1 "b" 2}

  (!.lua
   (var obj {:b 2})
   (var robj := obj)
   (k/set-key obj "a" 1)
   robj)
  => {"a" 1 "b" 2}

  (!.py
   (var obj {:b 2})
   (var robj := obj)
   (k/set-key obj "a" 1)
   robj)
  => {"a" 1 "b" 2}

  ^:self-reassignment
  (!.R
   (var obj {:b 2})
   (var robj := obj)
   (k/set-key obj "a" 1)
   [obj robj])
  => [{"a" 1, "b" 2} {"b" 2}])

^{:refer xt.lang.base-macro/set-idx :added "4.0"}
(fact "sets an index in the array"
  ^:hidden

  (!.js
   (var arr := [1 2 3])
   (var rarr := arr)
   (k/set-idx arr 1 "a")
   rarr)
  => [1 "a" 3]

  (!.lua
   (var arr := [1 2 3])
   (var rarr := arr)
   (k/set-idx arr 1 "a")
   rarr)
  => ["a" 2 3]

  (!.py
   (var arr := [1 2 3])
   (var rarr := arr)
   (k/set-idx arr 1 "a")
   rarr)
  => [1 "a" 3]

  ^:self-reassignment
  (!.R
   (var arr := [1 2 3])
   (var rarr := arr)
   (k/set-idx arr 1 "a")
   [arr rarr])
  => [["a" 2 3] [1 2 3]])

^{:refer xt.lang.base-macro/copy-key :added "4.0"}
(fact "copies a key"
  ^:hidden
  
  [(!.js
    (var obj {})
    (k/copy-key obj {:b 1}  "b")
    obj)
   (!.js
    (var obj {})
    (k/copy-key obj {:b 1} ["a" "b"])
    obj)]
  => [{"b" 1} {"a" 1}]

  [(!.lua
    (var obj {})
    (k/copy-key obj {:b 1}  "b")
    obj)
   (!.lua
    (var obj {})
    (k/copy-key obj {:b 1} ["a" "b"])
    obj)]
  => [{"b" 1} {"a" 1}]

  [(!.py
    (var obj {})
    (k/copy-key obj {:b 1}  "b")
    obj)
   (!.py
    (var obj {})
    (k/copy-key obj {:b 1} ["a" "b"])
    obj)]
  => [{"b" 1} {"a" 1}]

  ^:self-reassignment
  [(!.R
    (var obj {})
    (k/copy-key obj {:b 1}  "b")
    obj)
   (!.R
    (var obj {})
    (k/copy-key obj {:b 1} ["a" "b"])
    obj)]
  => [{"b" 1} {"a" 1}])

^{:refer xt.lang.base-macro/swap-key :added "4.0"}
(fact "swaps a value in the key with a function"
  ^:hidden
  
  (!.js
   (var obj {:a 1})
   (k/swap-key obj "a" k/inc)
   obj)
  => {"a" 2}

  (!.lua
   (var obj {:a 1})
   (k/swap-key obj "a" k/inc)
   obj)
  => {"a" 2}

  (!.py
   (var obj {:a 1})
   (k/swap-key obj "a" k/inc)
   obj)
  => {"a" 2}

  (!.R
   (var obj {:a 1})
   (k/swap-key obj "a" k/inc)
   obj)
  => {"a" 2})

^{:refer xt.lang.base-macro/get-char :added "4.0"}
(fact "gets a char from string"
  ^:hidden
  
  (!.js
   (k/get-char "abc" 0))
  => 97
  
  (!.lua
   (k/get-char "abc" 1))
  => 97

  (!.py
   (k/get-char "abc" 0))
  => 97)

^{:refer xt.lang.base-macro/split :added "4.0"}
(fact "splits a string using a token"
  ^:hidden
  
  (!.js
   (k/split "hello/world" "/"))
  => ["hello" "world"]

  (!.lua
   (k/split "hello/world" "/"))
  => ["hello" "world"]
  
  (!.py
   (k/split "hello/world" "/"))
  => ["hello" "world"]

  (!.R
   (k/split "hello/world" "/"))
  => ["hello" "world"])

^{:refer xt.lang.base-macro/join :added "4.0"}
(fact "joins an array using a seperator"
  ^:hidden
  
  (!.js
   (k/join "/" ["hello" "world"]))
  => "hello/world"

  (!.lua
   (k/join "/" ["hello" "world"]))
  => "hello/world"

  (!.py
   (k/join "/" ["hello" "world"]))
  => "hello/world"

  (!.R
   (k/join "/" ["hello" "world"]))
  => "hello/world")

^{:refer xt.lang.base-macro/replace :added "4.0"}
(fact "replaces a string with another"
  ^:hidden
  
  (!.js
   (k/replace "hello/world" "/" "_"))
  => "hello_world"

  (!.lua
   (k/replace "hello/world" "/" "_"))
  => "hello_world"

  (!.py
   (k/replace "hello/world" "/" "_"))
  => "hello_world"

  (!.R
   (k/replace "hello/world" "/" "_"))
  => "hello_world")

^{:refer xt.lang.base-macro/index-of :added "4.0"}
(fact "returns index of character in string"
  ^:hidden
  
  (!.js
   (k/index-of "hello/world" "/"))
  => 5

  (!.lua
   (k/index-of "hello/world" "/"))
  => 5

  (!.py
   (k/index-of "hello/world" "/"))
  => 5

  (!.R
   (k/index-of "hello/world" "/"))
  => 5)

^{:refer xt.lang.base-macro/substring :added "4.0"}
(fact "gets the substring"
  ^:hidden
  
  (!.js
   [(k/substring "hello/world" 3)
    (k/substring "hello/world" 3 8)])
  => ["lo/world" "lo/wo"]

  
  (!.lua
   [(k/substring "hello/world" 3)
    (k/substring "hello/world" 3 8)])
  => ["lo/world" "lo/wo"]

  (!.py
   [(k/substring "hello/world" 3)
    (k/substring "hello/world" 3 8)])
  => ["lo/world" "lo/wo"]

  (!.R
   [(k/substring "hello/world" 3)
    (k/substring "hello/world" 3 8)])
  => ["lo/world" "lo/wo"])

^{:refer xt.lang.base-macro/to-uppercase :added "4.0"}
(fact "converts string to uppercase"
  ^:hidden
  
  (!.js
   (k/to-uppercase "hello"))
  => "HELLO"

  (!.lua
   (k/to-uppercase "hello"))
  => "HELLO"

  (!.py
   (k/to-uppercase "hello"))
  => "HELLO"

  (!.R
   (k/to-uppercase "hello"))
  => "HELLO")

^{:refer xt.lang.base-macro/to-lowercase :added "4.0"}
(fact"converts string to lowercase"
  ^:hidden

  (!.js
   (k/to-lowercase "HELLO"))
  => "hello"

  (!.lua
   (k/to-lowercase "HELLO"))
  => "hello"

  (!.py
   (k/to-lowercase "HELLO"))
  => "hello"

  (!.R
   (k/to-lowercase "HELLO"))
  => "hello")

^{:refer xt.lang.base-macro/to-fixed :added "4.0"}
(fact "to fixed decimal places"
  ^:hidden
  
  (!.js
   (k/to-fixed 1.2 3))
  => "1.200"

  (!.lua
   (k/to-fixed 1.2 3))
  => "1.200")

^{:refer xt.lang.base-macro/trim :added "4.0"}
(fact "trims a string"
  ^:hidden
  
  (!.js
   (k/trim " \n  hello \n  "))
  => "hello"

  (!.lua
   (k/trim " \n  hello \n  "))
  => "hello")

^{:refer xt.lang.base-macro/trim-left :added "4.0"}
(fact "trims a string on left"
  ^:hidden
  
  (!.js
   (k/trim-left " \n  hello \n  "))
  => "hello \n  "

  (!.lua
   (k/trim-left " \n  hello \n  "))
  => "hello \n  ")

^{:refer xt.lang.base-macro/trim-right :added "4.0"}
(fact "trims a string on right"
  ^:hidden
  
  (!.js
   (k/trim-right " \n  hello \n  "))
  => " \n  hello"

  (!.lua
   (k/trim-right " \n  hello \n  "))
  => " \n  hello")

^{:refer xt.lang.base-macro/b64-encode :added "4.0"}
(fact "base64 encode"
  ^:hidden
  
  (!.js
   (k/b64-encode "hello"))
  => "aGVsbG8="
  
  (!.py
   (k/b64-encode "hello"))
  => "aGVsbG8=")

^{:refer xt.lang.base-macro/b64-decode :added "4.0"}
(fact "base64 decode"
  ^:hidden
  
  (!.js
   (k/b64-decode (k/b64-encode "hello")))
  => "hello"
  
  (!.py
   (k/b64-decode (k/b64-encode "hello")))
  => "hello")

^{:refer xt.lang.base-macro/uri-encode :added "4.0"}
(fact "encodes string as uri"
  ^:hidden
  
  (!.js
   (k/uri-encode "+.\n "))
  => "%2B.%0A%20")

^{:refer xt.lang.base-macro/uri-decode :added "4.0"}
(fact "decodes string as uri"
  ^:hidden
  
  (!.js
   (k/uri-decode (k/uri-encode "+.\n ")))
  => "+.\n ")

^{:refer xt.lang.base-macro/json-encode :added "4.0"}
(fact "encodes object to json"
  ^:hidden
  
  (json/read
   (!.js
    (k/json-encode [1 2 {:a [{:b 3}]}])))
  => [1 2 {"a" [{"b" 3}]}]

  (json/read
   (!.lua
    (var cjson := (require "cjson"))
    (k/json-encode [1 2 {:a [{:b 3}]}])))
  => [1 2 {"a" [{"b" 3}]}]

  (json/read
   (!.py
    (k/json-encode [1 2 {:a [{:b 3}]}])))
  => [1 2 {"a" [{"b" 3}]}]

  (json/read
   (!.R
    (k/json-encode [1 2 {:a [{:b 3}]}])))
  => [1 2 {"a" [{"b" 3}]}])

^{:refer xt.lang.base-macro/json-decode :added "4.0"}
(fact "decodes json to object"
  ^:hidden

  (!.js
   (k/json-decode (k/json-encode [1 2 {:a [{:b 3}]}])))
  => [1 2 {"a" [{"b" 3}]}]

  (!.lua
   (var cjson := (require "cjson"))
   (k/json-decode (k/json-encode [1 2 {:a [{:b 3}]}])))
  => [1 2 {"a" [{"b" 3}]}]

  (!.py
   (k/json-decode (k/json-encode [1 2 {:a [{:b 3}]}])))
  => [1 2 {"a" [{"b" 3}]}]

  (!.R
   (k/json-decode (k/json-encode [1 2 {:a [{:b 3}]}])))
  => [1 2 {"a" [{"b" 3}]}])

^{:refer xt.lang.base-macro/json-push :added "4.0"}
(fact "pushes an element into a json string"
  ^:hidden
  
  (!.js
   (k/json-push "[1,2,3]" "4"))
  => "[1,2,3,4]"

  (!.lua
   (k/json-push "[1,2,3]" "4"))
  => "[1,2,3,4]"

  (!.py
   (k/json-push "[1,2,3]" "4"))
  => "[1,2,3,4]"

  (!.R
   (k/json-push "[1,2,3]" "4"))
  => "[1,2,3,4]")

^{:refer xt.lang.base-macro/json-push-first :added "4.0"}
(fact "pushes an element as the first element of a json string"
  ^:hidden
  
  (!.js
   (k/json-push-first "[1,2,3]" "4"))
  => "[4,1,2,3]"

  (!.lua
   (k/json-push-first "[1,2,3]" "4"))
  => "[4,1,2,3]"

  (!.py
   (k/json-push-first "[1,2,3]" "4"))
  => "[4,1,2,3]"

  (!.R
   (k/json-push-first "[1,2,3]" "4"))
  => "[4,1,2,3]")

^{:refer xt.lang.base-macro/x:del :added "4.0"}
(fact  "deletes a value"
  ^:hidden
  
  (!.js
   (var out {:a 1})
   (k/x:del (. out ["a"]))
   out)
  => {}

  (!.lua
   (var out {:a 1})
   (k/x:del (. out ["a"]))
   out)
  => {}
  
  (!.py
   (var out {:a 1})
   (k/x:del (. out ["a"]))
   out)
  => {}

  (!.R
   (var out {:a 1})
   (k/x:del (. out ["a"]))
   out)
  => {})

^{:refer xt.lang.base-macro/x:shell :added "4.0"}
(fact "calls a shell command"
  ^:hidden
  
  (!.js
   (var cb {})
   (defn ^:inner call []
     (k/x:shell "ls" cb))
   (call))
  => ["async"]
  
  (!.lua
   (var cb {})
   (defn ^:inner call []
     (k/x:shell "ls" cb))
   (call))
  => ""
  
  (!.py
   (var cb {})
   (defn ^:inner call []
     (k/x:shell "ls" cb))
   (call))
  => 0

  (!.R
   (var cb {})
   (defn ^:inner call []
     (k/x:shell "ls" cb))
   (call))
  => 0)

^{:refer xt.lang.base-macro/x:type-native :added "4.0"}
(fact "gets the type native call"
  ^:hidden
  
  (!.js
   (defn ^:inner call [x]
     (k/x:type-native x))
   (call "hello"))
  => "string"

  (!.lua
   (defn ^:inner call [x]
     (k/x:type-native x))
   (call "hello"))
  => "string"

  (!.py
   (defn ^:inner call [x]
     (k/x:type-native x))
   (call "hello"))
  => "string"

  (!.R
   (defn ^:inner call [x]
     (k/x:type-native x))
   (call "hello"))
  => "string")

^{:refer xt.lang.base-macro/x:offset :added "4.0"}
(fact "gets the index offset"
  ^:hidden
  
  (!.js
   (k/x:offset))
  => 0
  
  (!.lua
   (k/x:offset))
  => 1
  
  (!.py
   (k/x:offset))
  => 0
  
  (!.R
   (k/x:offset))
  => 1)

^{:refer xt.lang.base-macro/x:offset-rev :added "4.0"}
(fact "gets the reverse offset"
  ^:hidden
  
  (!.js
   (k/x:offset-rev))
  => -1
  
  (!.lua
   (k/x:offset-rev))
  => 0
  
  (!.py
   (k/x:offset-rev))
  => -1
  
  (!.R
   (k/x:offset-rev))
  => 0)

^{:refer xt.lang.base-macro/x:offset-len :added "4.0"}
(fact "gets the offset length"
  ^:hidden
  
  (!.js
   (k/x:offset-len 10))
  => 9
  
  (!.lua
   (k/x:offset-len 10))
  => 10
  
  (!.py
   (k/x:offset-len 10))
  => 9
  
  (!.R
   (k/x:offset-len 10))
  => 10)

^{:refer xt.lang.base-macro/x:offset-rlen :added "4.0"}
(fact "gets the reverse offset length"
  ^:hidden
  
  (!.js
   (k/x:offset-rlen 10))
  => 10
  
  (!.lua
   (k/x:offset-rlen 10))
  => 9
  
  (!.py
   (k/x:offset-rlen 10))
  => 10
  
  (!.R
   (k/x:offset-rlen 10))
  => 9)

^{:refer xt.lang.base-macro/x:callback :added "4.0"}
(fact "gets the callback token"
  ^:hidden
  
  (!.js
   (k/x:callback))
  => (throws)

  (!.lua
   (k/x:callback))
  => nil
  
  (!.py
   (k/x:callback))
  => nil
  
  (!.R
   (k/x:callback))
  => nil)

^{:refer xt.lang.base-macro/x:arr-push :added "4.0"}
(fact "pushes an element into the end of the array"
  ^:hidden
  
  (!.js
   (var out := [1 2 3])
   (var oref := out)
   (k/x:arr-push out 4)
   oref)
  => [1 2 3 4]

  (!.lua
   (var out := [1 2 3])
   (var oref := out)
   (k/x:arr-push out 4)
   oref)
  => [1 2 3 4]

  (!.py
   (var out := [1 2 3])
   (var oref := out)
   (k/x:arr-push out 4)
   oref)
  => [1 2 3 4]

  (!.py
   (var out := [1 2 3])
   (var oref := out)
   (k/x:arr-push out 4)
   oref)
  => [1 2 3 4])

^{:refer xt.lang.base-macro/x:arr-pop :added "4.0"}
(fact "pops an element out from end of the array"
  ^:hidden

  (!.js
   (var out := [1 2 3])
   (var oref := out)
   (k/x:arr-pop out)
   oref)
  => [1 2]

  (!.lua
   (var out := [1 2 3])
   (var oref := out)
   (k/x:arr-pop out)
   oref)
  => [1 2]

  (!.py
   (var out := [1 2 3])
   (var oref := out)
   (k/x:arr-pop out)
   oref)
  => [1 2]

  ^:self-reassignment
  (!.R
   (var out := [1 2 3])
   (var oref := out)
   (k/x:arr-pop out)
   [oref out])
  => [[1 2 3] [1 2]])

^{:refer xt.lang.base-macro/x:arr-push-first :added "4.0"}
(fact "pushes an element into the start of the array"
  ^:hidden

  (!.js
   (var out := [1 2 3])
   (var oref := out)
   (k/x:arr-push-first out 5)
   oref)
  => [5 1 2 3]

  (!.lua
   (var out := [1 2 3])
   (var oref := out)
   (k/x:arr-push-first out 5)
   oref)
  => [5 1 2 3]

  (!.py
   (var out := [1 2 3])
   (var oref := out)
   (k/x:arr-push-first out 5)
   oref)
  => [5 1 2 3]

  ^:self-reassignment
  (!.R
   (var out := [1 2 3])
   (var oref := out)
   (k/x:arr-push-first out 5)
   [oref out])
  => [[1 2 3] [5 1 2 3]])

^{:refer xt.lang.base-macro/x:arr-pop-first :added "4.0"}
(fact "pops an element out from end of the array"
  ^:hidden

  (!.js
   (var out := [1 2 3])
   (var oref := out)
   (k/x:arr-pop-first out)
   oref)
  => [2 3]

  (!.lua
   (var out := [1 2 3])
   (var oref := out)
   (k/x:arr-pop-first out)
   oref)
  => [2 3]

  (!.py
   (var out := [1 2 3])
   (var oref := out)
   (k/x:arr-pop-first out)
   oref)
  => [2 3]

  (!.R
   (var out := [1 2 3])
   (var oref := out)
   (k/x:arr-pop-first out)
   [oref out])
  => [[1 2 3] [2 3]])

^{:refer xt.lang.base-macro/x:arr-insert :added "4.0"}
(fact "inserts an element into the array"
  ^:hidden
  
  (!.js
   (var out := [1 2 3])
   (var oref := out)
   (k/x:arr-insert out (x:offset 2) "a")
   oref)
  => [1 2 "a" 3]
  
  (!.lua
   (var out := [1 2 3])
   (var oref := out)
   (k/x:arr-insert out (x:offset 2) "a")
   oref)
  => [1 2 "a" 3]

  (!.py
   (var out := [1 2 3])
   (var oref := out)
   (k/x:arr-insert out (x:offset 2) "a")
   oref)
  => [1 2 "a" 3]
  
  (!.R
   (k/x:arr-insert [] 1 1))
  => (throws))

^{:refer xt.lang.base-macro/thread-spawn :added "4.0"}
(fact "spawns a thread")
