(ns std.concurrent.print-test
  (:use code.test)
  (:require [std.concurrent.print :as print]))

^{:refer std.concurrent.print/print-handler :added "3.0"}
(fact "handler for local print")

^{:refer std.concurrent.print/get-executor :added "3.0"}
(fact "gets the print executor")

^{:refer std.concurrent.print/submit :added "3.0"}
(fact "submits an entry for printing")

^{:refer std.concurrent.print/print :added "3.0"}
(fact "prints using local handler")

^{:refer std.concurrent.print/println :added "3.0"}
(fact "convenience function for println")

^{:refer std.concurrent.print/prn :added "3.0"}
(fact "convenience function for prn")

^{:refer std.concurrent.print/pprint-str :added "3.0"}
(fact "convenience function for pprint-str")

^{:refer std.concurrent.print/pprint :added "3.0"}
(fact "cenvenience function for pprint")

^{:refer std.concurrent.print/with-system :added "3.0"}
(fact "with system print instead of local")
