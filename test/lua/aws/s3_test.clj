(ns lua.aws.s3-test
  (:require [lib.minio :as minio]
            [std.lang :as l])
  (:use code.test))

(l/script- :lua
  {:runtime :basic
   :config {:program :resty}
   :require [[lua.core :as u]
             [lua.nginx :as n :include [:string]]
             [lua.aws.s3 :as s3]
             [lua.aws.common :as common]
             [xt.lang.common-lib :as k]]})

(fact:global
 {:setup    [(l/rt:restart)
             (minio/start-minio-array [{:port 4489
                                        :console 4499}])
             (Thread/sleep 1000)]
  :teardown [(l/rt:stop)
             (minio/stop-minio-array [{:port 4489
                                        :console 4499}])]})

^{:refer lua.aws.s3/policy-public-read-only :added "4.0"}
(fact "creates bucket read only policy"

  (s3/policy-public-read-only "test")
  => {"Statement"
      [{"Effect" "Allow",
        "Resource" ["arn:aws:s3:::test/*"],
        "Principal" {"AWS" ["*"]},
        "Action" ["s3:GetObject"]}],
      "Version" "2012-10-17"})

^{:refer lua.aws.s3/s3-request :added "4.0"
  :setup [(s3/s3-request {:method "PUT"
                          :route "test"}
                         {:port 4489}
                         {})]}
(fact "performs an s3-request"

  (s3/s3-request {:method "HEAD"
                  :route "test"}
                 {:port 4489}
                 {})
  => (contains-in
      {"body" "",
       "has_body" false,
       "status" 200,
       "reason" "OK",
       "headers"
       {"Vary" ["Origin" "Accept-Encoding"],
        "Accept-Ranges" "bytes",
        "Content-Length" "0",
        "Server" "MinIO",
        "Content-Type" "application/xml"}}))

^{:refer lua.aws.s3/check-bucket :added "4.0"}
(fact "checks that bucket exists"

  (s3/check-bucket {:port 4489}
                   "test")
  => (contains-in
      {"body" "",
       "has_body" false,
       "status" 200,
       "reason" "OK"})

  (s3/check-bucket {:port 4489}
                   "test"
                   {:public true})
  => (contains-in
      {"body" "",
       "has_body" false,
       "status" 403,
       "reason" "Forbidden"}))

^{:refer lua.aws.s3/create-bucket :added "4.0"
  :setup [(s3/purge-bucket {:port 4489}
                           "test")]}
(fact "creates a bucket"

  (s3/create-bucket {:port 4489}
                    "test")
  => (contains-in
      {"body" "",
       "has_body" true,
       "status" 200,
       "reason" "OK"}))

^{:refer lua.aws.s3/delete-bucket :added "4.0"
  :setup [(s3/create-bucket {:port 4489}
                            "test")
          (s3/delete-all-objects
           {:port 4489}
           "test"
           (get (s3/list-objects {:port 4489}
                                 "test")
                "body"))]}
(fact "deletes a bucket"

  (s3/delete-bucket {:port 4489}
                    "test")
  => (contains-in
      {"body" "",
       "has_body" false,
       "status" 204,
       "reason" "No Content"}))

^{:refer lua.aws.s3/set-bucket-policy :added "4.0"
  :setup [(s3/create-bucket {:port 4489}
                            "test")]}
(fact "sets the bucket policy"

  (!.lua
   (s3/set-bucket-policy {:port 4489}
                         "test"
                         (s3/policy-public-read-only "test")))
  => (contains-in
      {"body" "",
       "has_body" false,
       "status" 204,
       "reason" "No Content"}))

^{:refer lua.aws.s3/list-objects-process :added "4.0"}
(fact "processes objects from request")

^{:refer lua.aws.s3/list-objects :added "4.0"
  :setup [(s3/create-bucket {:port 4489}
                            "test")
          (s3/put-object {:port 4489}
                         "test"
                         "abc000.txt"
                         "hello 000"
                         {:headers {"content-type" "text/plain"}})

          (s3/put-object {:port 4489}
                         "test"
                         "abc001.txt"
                         "hello 001"
                         {:headers {"content-type" "text/plain"}})]}
(fact "lists objects"

  (s3/list-objects {:port 4489}
                   "test")

  => (contains-in
      {"body"
       (contains ["abc000.txt"
                  "abc001.txt"]
                 :in-any-order),
       "has_body" true,
       "status" 200,
       "reason" "OK"}))

^{:refer lua.aws.s3/put-object :added "4.0"
  :setup [(s3/create-bucket {:port 4489}
                            "test")]}
(fact "puts object in bucket"

  (s3/put-object {:port 4489}
                 "test"
                 "abc000.txt"
                 "hello 000"
                 {:headers {"content-type" "text/plain"}})
  => (contains-in
      {"body" "",
       "has_body" true,
       "status" 200,
       "reason" "OK"}))

^{:refer lua.aws.s3/check-object :added "4.0"
  :setup [(s3/put-object {:port 4489}
                         "test"
                         "abc000.txt"
                         "hello 000"
                         {:headers {"content-type" "text/plain"}})]}
(fact "checks that object exists"

 (s3/check-object {:port 4489}
                   "test"
                   "abc000.txt")
  => (contains-in
      {"body" "",
       "has_body" false,
       "status" 200,
       "reason" "OK"})

  (s3/check-object {:port 4489}
                   "test"
                   "xyz000.txt")
  => (contains-in
      {"body" "",
       "has_body" false,
       "status" 404,
       "reason" "Not Found"}))

^{:refer lua.aws.s3/get-object :added "4.0"
  :setup [(s3/put-object {:port 4489}
                         "test"
                         "abc000.txt"
                         "hello 000"
                         {:headers {"content-type" "text/plain"}})]}
(fact "gets the object"

  (s3/get-object {:port 4489}
                 "test"
                 "abc000.txt")
  => (contains-in
      {"body" "hello 000",
       "has_body" true,
       "status" 200,
       "reason" "OK"}))

^{:refer lua.aws.s3/delete-object :added "4.0"
  :setup [(s3/put-object {:port 4489}
                         "test"
                         "abc000.txt"
                         "hello 000"
                         {:headers {"content-type" "text/plain"}})]}
(fact "deletes an object"

  (s3/delete-object {:port 4489}
                    "test"
                    "abc000.txt"))

^{:refer lua.aws.s3/delete-all-objects :added "4.0"
  :setup [(s3/put-object {:port 4489}
                         "test"
                         "abc000.txt"
                         "hello 000"
                         {:headers {"content-type" "text/plain"}})
          (s3/put-object {:port 4489}
                         "test"
                         "abc001.txt"
                         "hello 001"
                         {:headers {"content-type" "text/plain"}})
          (s3/put-object {:port 4489}
                         "test"
                         "abc002.txt"
                         "hello 002"
                         {:headers {"content-type" "text/plain"}})]}
(fact "deletes all objects"

  (s3/delete-all-objects {:port 4489}
                         "test"
                         ["abc000.txt"
                          "abc001.txt"])
  => (contains {"body" {"DeleteResult" true},
                "has_body" true,
                "status" 200,
                "reason" "OK"})


  (s3/list-objects {:port 4489} "test")
  => (contains {"body" ["abc002.txt"],
                "has_body" true,
                "status" 200,
                "reason" "OK"}))

^{:refer lua.aws.s3/purge-bucket :added "4.0"
  :setup [(s3/create-bucket {:port 4489}
                            "test")
          (s3/put-object {:port 4489}
                         "test"
                         "abc000.txt"
                         "hello 000"
                         {:headers {"content-type" "text/plain"}})
          (s3/put-object {:port 4489}
                         "test"
                         "abc001.txt"
                         "hello 001"
                         {:headers {"content-type" "text/plain"}})
          (s3/put-object {:port 4489}
                         "test"
                         "abc002.txt"
                         "hello 002"
                         {:headers {"content-type" "text/plain"}})]}
(fact "deletes the bucket and all objects"

  (s3/purge-bucket {:port 4489} "test")
  => (contains
      {"body" "",
       "has_body" false,
       "status" 204,
       "reason" "No Content"}))
