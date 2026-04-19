(ns net.openapi.params-test
  (:require [net.openapi.params :as params])
  (:use code.test))

^{:refer net.openapi.params/format-date :added "4.0"}
(fact "Format the given Date object with the :date-format defined in *api-options*.
  NOTE: The UTC time zone is used."
  (params/format-date (java.util.Date. 0) "yyyy-MM-dd")
  => "1970-01-01")

^{:refer net.openapi.params/parse-date :added "4.0"}
(fact "Parse the given string to a Date object with the :date-format defined in *api-options*.
  NOTE: The UTC time zone is used."
  (.getTime (params/parse-date "1970-01-01" "yyyy-MM-dd"))
  => 0)

^{:refer net.openapi.params/param->str :added "4.0"}
(fact "Format the given parameter value to string."
  [(params/param->str (java.util.Date. 0) "yyyy-MM-dd")
   (params/param->str [1 2 3] nil)
   (params/param->str :hello nil)]
  => ["1970-01-01" "1,2,3" ":hello"])

^{:refer net.openapi.params/normalize-array-param :added "4.0"}
(fact "Normalize array parameter according to :collection-format specified in the parameter's meta data.
  When the parameter contains File, a seq is returned so as to keep File parameters.
  For :multi collection format, a seq is returned which will be handled properly by clj-http.
  For other cases, a string is returned."
  (let [files [(java.io.File. "/tmp/a")
               (java.io.File. "/tmp/b")]]
    (vec (params/normalize-array-param files)))
  => [(java.io.File. "/tmp/a")
      (java.io.File. "/tmp/b")])

^{:refer net.openapi.params/normalize-param :added "4.0"}
(fact "Normalize parameter value, handling three cases:
  for sequential value, apply `normalize-array-param` which handles collection format;
  for File value, use current value;
  otherwise, apply `param->str`."
  (let [file (java.io.File. "/tmp/example")]
    [(params/normalize-param file)
     (try
       (params/normalize-param :hello)
       (catch Throwable t
         :thrown))])
  => [(java.io.File. "/tmp/example") :thrown])

^{:refer net.openapi.params/normalize-params :added "4.0"}
(fact "Normalize parameters values: remove nils, format to string with `param->str`."
  (params/normalize-params {:a nil
                            :b nil})
  => {})

^{:refer net.openapi.params/make-url :added "4.0"}
(fact "Make full URL by adding base URL and filling path parameters."
  (params/make-url "https://api.test"
                   "/users"
                   {})
  => "https://api.test/users")
