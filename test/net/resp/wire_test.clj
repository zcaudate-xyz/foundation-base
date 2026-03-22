(ns net.resp.wire-test
  (:require [net.resp.wire :refer :all]
            [std.lib.foundation :as f])
  (:use [code.test])
  (:refer-clojure :exclude [read]))

^{:refer net.resp.wire/call :added "3.0"}
(fact "completes a request on the wire")

^{:refer net.resp.wire/as-input :added "3.0"}
(fact "creates an input from value"

  (as-input {:a 1} :json)
  => "{\"a\":1}")

^{:refer net.resp.wire/serialize-bytes :added "3.0"}
(fact "serializes objects (data) to bytes"

  (f/string (serialize-bytes "HELLO" :string))
  => "HELLO" ^:hidden

  (f/string (serialize-bytes "HELLO" :edn))
  => "\"HELLO\""

  (f/string (serialize-bytes {:a 1} :string))
  => "{:a 1}"

  (f/string (serialize-bytes {:a 1} :edn))
  => "{:a 1}"

  (f/string (serialize-bytes {:a 1} :json))
  => "{\"a\":1}")

^{:refer net.resp.wire/deserialize-bytes :added "3.0"}
(fact "converts bytes back to data structure"

  (deserialize-bytes (serialize-bytes {:a 1} :json)
                     :json)
  => {:a 1})

^{:refer net.resp.wire/coerce-bytes :added "3.0"}
(fact "coerces bytes to data"

  (coerce-bytes (.getBytes "OK") :json)
  => "OK"

  (coerce-bytes (serialize-bytes {:a 1} :json) :json)
  => {:a 1})

^{:refer net.resp.wire/coerce :added "3.0"}
(fact "coerces redis return to bytes" ^:hidden

  (coerce [(.getBytes "OK") [(.getBytes "OK") (.getBytes "OK")]]
          :string)
  => ["OK" ["OK" "OK"]])
