(ns rt.basic.impl-annex.process-rust-test
  (:require [rt.basic.impl-annex.process-rust :refer :all]
            [std.lang :as l])
  (:use code.test))

(l/script- :rust
  {:runtime :twostep})

^{:refer rt.basic.impl-annex.process-rust/transform-form :added "4.0"}
(fact "transforms the rust form"
  ^:hidden

  (transform-form '[(+ 1 2 3)]
                  (l/rt :rust)
                  )
  => '(:- "fn main() {\n " (do ((:- "println!") "{}" (+ 1 2 3))) "\n}")
  
  (!.rs
   (+ 1 2 3))
  => 6)
