(ns net.openapi.params-test
  (:use code.test)
  (:require [net.openapi.params :as params]))

^{:refer net.openapi.params/format-date :added "4.0"}
(fact "Format the given Date object with the :date-format defined in *api-options*.
  NOTE: The UTC time zone is used.")

^{:refer net.openapi.params/parse-date :added "4.0"}
(fact "Parse the given string to a Date object with the :date-format defined in *api-options*.
  NOTE: The UTC time zone is used.")

^{:refer net.openapi.params/param->str :added "4.0"}
(fact "Format the given parameter value to string.")

^{:refer net.openapi.params/normalize-array-param :added "4.0"}
(fact "Normalize array parameter according to :collection-format specified in the parameter's meta data.
  When the parameter contains File, a seq is returned so as to keep File parameters.
  For :multi collection format, a seq is returned which will be handled properly by clj-http.
  For other cases, a string is returned.")

^{:refer net.openapi.params/normalize-param :added "4.0"}
(fact "Normalize parameter value, handling three cases:
  for sequential value, apply `normalize-array-param` which handles collection format;
  for File value, use current value;
  otherwise, apply `param->str`.")

^{:refer net.openapi.params/normalize-params :added "4.0"}
(fact "Normalize parameters values: remove nils, format to string with `param->str`.")

^{:refer net.openapi.params/make-url :added "4.0"}
(fact "Make full URL by adding base URL and filling path parameters.")
