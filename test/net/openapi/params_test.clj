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
  [(params/normalize-array-param (with-meta [1 2 3] {:collection-format :csv}))
   (params/normalize-array-param (with-meta [1 2 3] {:collection-format :pipes}))
   (vec (params/normalize-array-param (with-meta [1 2 3] {:collection-format :multi})))]
  => ["1,2,3" "1|2|3" ["1" "2" "3"]])

^{:refer net.openapi.params/normalize-param :added "4.0"}
(fact "Normalize parameter value, handling three cases:
  for sequential value, apply `normalize-array-param` which handles collection format;
  for File value, use current value;
  otherwise, apply `param->str`."
  (let [file (java.io.File. "/tmp/example")]
    [(params/normalize-param (with-meta [1 2] {:collection-format :csv}))
     (params/normalize-param file)
     (params/normalize-param :hello)])
  => ["1,2" (java.io.File. "/tmp/example") ":hello"])

^{:refer net.openapi.params/normalize-params :added "4.0"}
(fact "Normalize parameters values: remove nils, format to string with `param->str`."
  (params/normalize-params {:a 1
                            :b nil
                            :c (with-meta [1 2] {:collection-format :csv})})
  => {:a "1"
      :c "1,2"})

^{:refer net.openapi.params/make-url :added "4.0"}
(fact "Make full URL by adding base URL and filling path parameters."
  (params/make-url "https://api.test"
                   "/users/{id}"
                   {:id 42})
  => "https://api.test/users/42")
