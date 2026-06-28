(ns lua.aws.s3
  (:require [hara.lang :as l]))

(l/script :lua.nginx
  {:require [[lua.aws.common :as common] [lua.nginx :as n]
             [xt.lang.spec-base :as xt] [xt.lang.common-lib :as k]
             [xt.lang.common-data :as xtd]
             [xt.lang.parser-xml :as xml] [xt.lang.spec-base :as xt]]})

(defn.lua policy-public-read-only
  "creates bucket read only policy"
  {:added "4.0"}
  [bucket]
  (return
   {"Statement"
    [{"Effect" "Allow",
      "Resource" [(xt/x:cat "arn:aws:s3:::" bucket "/*")],
      "Principal" {"AWS" ["*"]},
      "Action" ["s3:GetObject"]}],
    "Version" "2012-10-17"}))

(defn.lua s3-request
  "performs an s3-request"
  {:added "4.0"}
  [enforced aws opts]
  (return
   (common/make-request
    (xtd/obj-assign (xtd/obj-clone aws) {:service "s3"})
    (:? opts
        (xtd/obj-assign opts enforced)
        enforced))))

(defn.lua check-bucket
  "checks that bucket exists"
  {:added "4.0"}
  [aws bucket opts]
  (return
   (-/s3-request {:method "HEAD"
                  :route bucket}
                 aws opts)))

(defn.lua create-bucket
  "creates a bucket"
  {:added "4.0"}
  [aws bucket opts]
  (return
   (-/s3-request {:method "PUT"
                  :route bucket}
                 aws opts)))

(defn.lua delete-bucket
  "deletes a bucket"
  {:added "4.0"}
  [aws bucket opts]
  (return
   (-/s3-request {:method "DELETE"
                  :route bucket}
                 aws opts)))

(defn.lua set-bucket-policy
  "sets the bucket policy"
  {:added "4.0"}
  [aws bucket policy opts]
  (return
   (-/s3-request {:method "PUT"
                  :route (xt/x:cat bucket "?policy")
                  :body  (xt/x:json-encode policy)}
                 aws opts)))

(defn.lua list-objects-process
  "processes objects from request"
  {:added "4.0"}
  [res]
  (var #{status body} res)
  (when (== status 200)
    (var result (xtd/get-in body ["ListBucketResult"]))
    (var arr (:? (k/is-array? result)
                 (xtd/arr-keep result (fn:> [e] (xt/x:get-key e "Contents")))
                 (xtd/arrayify (xtd/get-in result ["Contents"]))))
    (:= (. res body) (xtd/arr-map arr (fn:> [e] (xt/x:get-key e "Key")))))
  (return res))

(defn.lua list-objects
  "lists objects"
  {:added "4.0"}
  [aws bucket opts]
  (var res (-/s3-request {:method "GET"
                          :route bucket}
                         aws opts))
  (var process (or (xtd/get-in opts ["process"])
                   -/list-objects-process))
  (return (process res)))

(defn.lua put-object
  "puts object in bucket"
  {:added "4.0"}
  [aws bucket key data opts]
  (return
   (-/s3-request {:method "PUT"
                  :route (xt/x:cat bucket "/" key)
                  :body  data}
                 aws opts)))

(defn.lua check-object
  "checks that object exists"
  {:added "4.0"}
  [aws bucket key opts]
  (return
   (-/s3-request {:method "HEAD"
                  :route (xt/x:cat bucket "/" key)}
                 aws opts)))

(defn.lua get-object
  "gets the object"
  {:added "4.0"}
  [aws bucket key opts]
  (return
   (-/s3-request {:method "GET"
                  :route (xt/x:cat bucket "/" key)}
                 aws opts)))

(defn.lua delete-object
  "deletes an object"
  {:added "4.0"}
  [aws bucket key opts]
  (return
   (-/s3-request {:method "DELETE"
                  :route (xt/x:cat bucket "/" key)}
                 aws opts)))

(defn.lua delete-all-objects
  "deletes all objects"
  {:added "4.0"}
  [aws bucket all-keys opts]
  (var node
       {:tag "Delete"
        :children
        (xtd/arr-assign
         [{:tag "Quiet"
           :children [true]}]
         (xtd/arr-map all-keys
                    (fn:> [key]
                      {:tag "Object"
                       :children [{:tag "Key"
                                   :children [key]}]})))})
  (var data (xml/to-string node))
  (return
   (-/s3-request {:method "post"
                  :route (xt/x:cat bucket "?delete")
                  :body  data
                  :headers {"content-md5" (n/encode-base64 (common/get-md5 data))}}
                 aws
                 opts)))

(defn.lua purge-bucket
  "deletes the bucket and all objects"
  {:added "4.0"}
  [aws bucket opts]
  (var m (-/list-objects aws bucket opts))
  (when (== 200 (. m status))
    (when (xtd/not-empty? (. m body))
      (-/delete-all-objects aws bucket (. m body) opts))
    (return (-/delete-bucket aws bucket opts)))
  (return m))
