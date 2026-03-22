(ns lua.aws.common-test
  (:require [std.lang :as l]
            [std.string.common]
            [std.string.prose])
  (:use code.test))

(l/script- :lua
  {:runtime :basic
   :config {:program :resty}
   :require [[lua.core :as u]
             [lua.nginx :as n :include [:string]]
             [lua.aws.common :as common]
             [xt.lang.base-lib :as k]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer lua.aws.common/get-sha256 :added "4.0"}
(fact "gets the sha 256 cipher"
  ^:hidden
  
  (!.lua
   (n/to-hex (common/get-sha256 "")))
  => "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")

^{:refer lua.aws.common/get-md5 :added "4.0"}
(fact "gets the sha 256 cipher"
  ^:hidden
  
  (!.lua
   (n/to-hex (common/get-md5 "")))
  => "d41d8cd98f00b204e9800998ecf8427e")

^{:refer lua.aws.common/get-date-short :added "4.0"}
(fact "gets short amz date given ms"
  ^:hidden
  
  (common/get-date-short 0)
  => "19700101")

^{:refer lua.aws.common/get-date-long :added "4.0"}
(fact "gets long amz date given ms"
  ^:hidden
  
  (common/get-date-long 0)
  => "19700101T000000Z")

^{:refer lua.aws.common/get-aws-defaults :added "4.0"}
(fact "gets aws defaults"
  ^:hidden

  (common/get-aws-defaults {:t 0
                            :user "admin"
                            :secret "password"})
  => {"host" "127.0.0.1",
      "region" "us-east-1",
      "port" 9000,
      "t" 0,
      "user" "admin",
      "secret" "password",
      "service" "s3",
      "scheme" "http"})

^{:refer lua.aws.common/get-aws-scope :added "4.0"}
(fact "gets the scope for a call"
  ^:hidden
  
  (common/get-aws-scope 0 nil nil)
  => "19700101/us-east-1/s3/aws4_request")

^{:refer lua.aws.common/get-aws-credential :added "4.0"}
(fact "gets the aws credential"
  ^:hidden
  
  (common/get-aws-credential "admin" 0 nil nil)
  => "admin/19700101/us-east-1/s3/aws4_request")

^{:refer lua.aws.common/get-signing-key :added "4.0"}
(fact "gets the aws signing key"
  ^:hidden

  (!.lua
   (n/to-hex (common/get-signing-key "password"
                                     1656926392)))
  => "0b039b6f642bf59bb87a9a82124a7302aa2b70858866b30b27ce1f8e20ee6408")

^{:refer lua.aws.common/get-canonical-headers :added "4.0"}
(fact "gets the canonical headers"
  ^:hidden
  
  (!.lua
   (common/get-canonical-headers (tab ["B" "hello   1"]
                                      ["A" "hello   2"]
                                      ["Content-Type" "hello"])))
  => [["a" "hello 2"]
      ["b" "hello 1"]])

^{:refer lua.aws.common/get-canonical-route :added "4.0"}
(fact "gets the canonical route"
  ^:hidden
  
  (!.lua
   (common/get-canonical-route "/hello?b&a&c"))
  => {"params" [["a" ""] ["b" ""] ["c" ""]], "path" "/hello"})

^{:refer lua.aws.common/get-canonical :added "4.0"}
(fact "gets the canonical string"
  ^:hidden
  
  (!.lua
   (common/get-canonical "PUT"
                         (common/get-canonical-route
                          "/hello?b&a&c")
                         (common/get-canonical-headers
                          (tab ["B" "hello   1"]
                               ["A" "hello   2"]
                               ["Content-Type" "hello"]))
                         "SHA"))
  => (std.string.prose/|
      "PUT"
      "/hello"
      "a=&b=&c="
      "a:hello 2"
      "b:hello 1"
      ""
      "a;b"
      "SHA")
  
  (!.lua
   (common/get-canonical
    "PUT"
    (common/get-canonical-route
     "/europeteuri")
    (common/get-canonical-headers
     (tab ["host" "192.168.0.1:4390"]
          ["x-ams-content-sha256" "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"]
          ["x-amz-date" "20220704T091001Z"]))
    "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"))
  => (std.string.prose/|
      "PUT"
      "/europeteuri"
      ""
      "host:192.168.0.1:4390"
      "x-ams-content-sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
      "x-amz-date:20220704T091001Z"
      ""
      "host;x-ams-content-sha256;x-amz-date"
      "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"))

^{:refer lua.aws.common/get-signing-input :added "4.0"}
(fact "gets the signing input")

^{:refer lua.aws.common/header-authorization :added "4.0"}
(fact "creates the header authorization"
  ^:hidden
  
  (!.lua
   (var aws   (common/get-aws-defaults
               {:t (u/time {"year" 2022, "month" 7, "day" 4,
                            "hour" 19 "min" 18 "sec" 56})
                "user" "admin",
                "secret" "password"}))
   (var hash  (n/to-hex (common/get-sha256 "")))
   (common/header-authorization
    aws
    "PUT"
    "/europeteuri"
    {"host" "192.168.0.10:4390"
     "x-amz-content-sha256" hash
     "x-amz-date" (common/get-date-long (. aws t))}
    hash))
  => string?
  #_(std.string.common/join
      ", "
      ["AWS4-HMAC-SHA256 Credential=admin/20220704/us-east-1/s3/aws4_request"
       "SignedHeaders=host;x-amz-content-sha256;x-amz-date"
       "Signature=e9198ff1c11fed20d54f13e5d25f5852a5f518827c13b4ceac537a9a0c3ee471"]))

^{:refer lua.aws.common/make-request-map :added "4.0"}
(fact "make request map"
  ^:hidden
  
  (common/make-request-map
   (common/get-aws-defaults
    (common/get-aws-defaults
     {:t (u/time {"year" 2022, "month" 7, "day" 4,
                  "hour" 19 "min" 18 "sec" 56})
      "user" "admin",
      "secret" "password"}))
   "PUT"
   "BUCKET"
   {}
   "")
  => map?
  #_
  {"url" "http://127.0.0.1:9000/BUCKET",
   "body" "",
   "method" "PUT",
   "headers" {"host" "127.0.0.1:9000",
              "x-amz-date" "20220704T111856Z",
              "x-amz-content-sha256"
              "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
              "authorization"
              (k/arr-join
               ["AWS4-HMAC-SHA256 Credential=admin/20220704/us-east-1/s3/aws4_request"
                "SignedHeaders=host;x-amz-content-sha256;x-amz-date"
                "Signature=100a5e4eb3bebe283af79d0d85dbbf701a41efb48e0f670e01659c54b11ceb13"]
               ", ")}})

^{:refer lua.aws.common/with-parse :added "4.0"}
(fact "parses the response body")

^{:refer lua.aws.common/make-request-auth :added "4.0"}
(fact "makes an api auth request")

^{:refer lua.aws.common/make-request-public :added "4.0"}
(fact "makes an api public request")

^{:refer lua.aws.common/make-request :added "4.0"}
(fact "makes an api auth request")
