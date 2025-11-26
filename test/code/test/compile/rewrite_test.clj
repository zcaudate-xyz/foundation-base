(ns code.test.compile.rewrite-test
  (:use code.test)
  (:require [code.test.compile.rewrite :refer :all]))

^{:refer code.test.compile.rewrite/rewrite-list :added "4.0"}
(fact "TODO"
  ^:hidden

  (let [a 1]
    a => 1)
  
  #_#_#_(inc 0) => 1
  
  
  #_(let [a 1]
    (let [b 3]
      (+ a b)
      => 4))

  (let [a 1]
    (let [b 3]
      (binding [*err* nil]
        (+ a b)
        => 4))))

^{:refer code.test.compile.rewrite/rewrite-list :added "4.0"}
(fact "TODO"
  ^:hidden

  (throw 1)
  
  (throw 1)
  => 12)

^{:refer code.test.compile.rewrite/rewrite-list :added "4.0"
  :timeout 100}
(fact "TODO"
  ^:hidden


  (do (Thread/sleep 1000)
      1)
  
  (do (Thread/sleep 1000)
      1)
  => 1

  
  (let [b 1]
    (let [a 1]
      (do (Thread/sleep 1000)
          1)
      => 1

      (do (Thread/sleep 1000)
          1)
      => 1)))


^{:refer code.test.compile.rewrite/rewrite-list :added "4.0"
  :timeout 100}
(fact "GO TO Sleep"
  ^:hidden

  
  (let [b 1]
    (let [a 1]
      (do (Thread/sleep 1000)
          1)
      => 1))
  )

^{:refer code.test.compile.rewrite/rewrite-list :added "4.0"}
(fact "TODO"
  ^:hidden

  (let [b 1]
    (let [a 1]
      (let [a 1]
        (+ 1 2)
        => 1)
      (+ 1 2)
      => 1)))


^{:refer code.test.compile.rewrite/rewrite-list :added "4.0"
  :timeout 100}
(fact "TODO"
  ^:hidden


  (throw 1)
  )


^{:refer code.test.compile.rewrite/rewrite-list :added "4.0"}
(fact "TODO"
  ^:hidden

  (do (Thread/sleep 1000)
      (a 1))
  => 1)





^{:refer code.test.compile.rewrite/rewrite-list :added "4.0"
  :timeout 100}
(fact "TODO"
  ^:hidden

  (do (std.lib/prn "START" (Thread/currentThread))
      (Thread/sleep 1000)
      (std.lib/prn "END" (Thread/currentThread)))
  => nil)

^{:refer code.test.compile.rewrite/rewrite-list :added "4.0"
  :timeout 100}
(fact "TODO"
  ^:hidden

  (do (std.lib/prn "START" (Thread/currentThread))
      (Thread/sleep 1000)
      (std.lib/prn "END" (Thread/currentThread))))


(fact "TODO"
  ^:hidden

  (do)
  => nil)

(fact "TODO"
  ^:hidden

  (do)
  => 1)

(fact "TODO"
  ^:hidden

  (do 1)
  => nil)


(fact "TODO"
  ^:hidden

  (throw 1))

(fact "TODO"
  ^:hidden

  [1 2 3]
  [4 5 6])


(comment
  (s/run)
  
  )
