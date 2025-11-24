(ns std.lang.model.spec-xtalk.com-r-test
  (:use code.test)
  (:require [std.lang.model.spec-xtalk.com-r :refer :all]
            [std.lang :as l]
            [std.lib :as h]))

^{:refer std.lang.model.spec-xtalk.com-r/r-tf-x-return-encode :added "4.0"}
(fact "encodes return value"
  (l/emit-as :r [(r-tf-x-return-encode '[_ "hello" "id" "key"])])
  => #"toJSON")

^{:refer std.lang.model.spec-xtalk.com-r/r-tf-x-return-wrap :added "4.0"}
(fact "wraps return value"
  (l/emit-as :r [(r-tf-x-return-wrap '[_ (fn [] (return 1)) (fn [x] (return x))])])
  => #"tryCatch")

^{:refer std.lang.model.spec-xtalk.com-r/r-tf-x-return-eval :added "4.0"}
(fact "evals return value"
  (l/emit-as :r [(r-tf-x-return-eval '[_ "1 + 1" (fn [x] (return x))])])
  => #"eval")

^{:refer std.lang.model.spec-xtalk.com-r/r-tf-x-socket-connect :added "4.0"}
(fact "connects socket"
  (l/emit-as :r [(r-tf-x-socket-connect '[_ "localhost" 8080 {}])])
  => #"socketConnection")

^{:refer std.lang.model.spec-xtalk.com-r/r-tf-x-socket-send :added "4.0"}
(fact "sends socket"
  (l/emit-as :r [(r-tf-x-socket-send '[_ conn "hello"])])
  => #"writeLines")

^{:refer std.lang.model.spec-xtalk.com-r/r-tf-x-socket-close :added "4.0"}
(fact "closes socket"
  (l/emit-as :r [(r-tf-x-socket-close '[_ conn])])
  => #"close")

^{:refer std.lang.model.spec-xtalk.com-r/r-tf-x-print :added "4.0"}
(fact "prints"
  (l/emit-as :r [(r-tf-x-print '[_ "hello"])])
  => #"print")

^{:refer std.lang.model.spec-xtalk.com-r/r-tf-x-shell :added "4.0"}
(fact "shell"
  (l/emit-as :r [(r-tf-x-shell '[_ "ls" cm])])
  => #"system")
