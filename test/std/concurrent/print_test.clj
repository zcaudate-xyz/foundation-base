(ns std.concurrent.print-test
  (:use code.test)
  (:require [std.concurrent.print :as print]))

^{:refer std.concurrent.print/print-handler :added "3.0"}
(fact "handler for local print"
  (with-out-str
    (print/print-handler nil ["hello" " " "world"]))
  => "hello world")

^{:refer std.concurrent.print/get-executor :added "3.0"}
(fact "gets the print executor"
  (print/get-executor)
  => map?)

^{:refer std.concurrent.print/submit :added "3.0"}
(fact "submits an entry for printing"
  (print/submit "hello")
  => nil?)

^{:refer std.concurrent.print/print :added "3.0"}
(fact "prints using local handler"
  (print/print "hello")
  => nil?)

^{:refer std.concurrent.print/println :added "3.0"}
(fact "convenience function for println"
  (print/println "hello")
  => nil?)

^{:refer std.concurrent.print/prn :added "3.0"}
(fact "convenience function for prn"
  (print/prn "hello")
  => nil?)

^{:refer std.concurrent.print/pprint-str :added "3.0"}
(fact "convenience function for pprint-str"
  (print/pprint-str {:a 1})
  => "{:a 1}")

^{:refer std.concurrent.print/pprint :added "3.0"}
(fact "cenvenience function for pprint"
  (print/pprint {:a 1})
  => nil?)

^{:refer std.concurrent.print/with-system :added "3.0"}
(fact "with system print instead of local")
