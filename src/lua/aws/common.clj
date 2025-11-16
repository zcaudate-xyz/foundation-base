(ns lua.aws.common
  (:require [std.lib :as h]
            [std.lang :as l]))

(l/script :lua
  {:require [[lua.nginx.openssl :as ssl]
             [lua.nginx :as n]
             [lua.nginx.http-client :as http]
             [lua.core :as u]
             [xt.lang.base-lib :as k]
             [xt.lang.util-xml :as xml]]})

(def.lua AWS_ALGO "AWS4-HMAC-SHA256")

(def.lua AWS_REQUEST "aws4_request")

(def.lua AWS_EXCLUDE
  {"authorization" true
   "content-length" true
   "content-type" true
   "user-agent" true})

(def.lua REQ_KEYS
  ["body"
   "has_body"
   "status"
   "reason"
   "headers"])

(defn.lua get-sha256
  "gets the sha 256 cipher"
  {:added "4.0"}
  [data]
  (var hash (n/sha256-new))
  (. hash (update data))
  (return (. hash (final))))

(defn.lua get-md5
  "gets the sha 256 cipher"
  {:added "4.0"}
  [data]
  (var hash (n/md5-new))
  (. hash (update data))
  (return (. hash (final))))

(defn.lua get-date-short
  "gets short amz date given ms"
  {:added "4.0"}
  [t]
  (return (u/date "!%Y%m%d" t)))

(defn.lua get-date-long
  "gets long amz date given ms"
  {:added "4.0"}
  [t]
  (return (u/date "!%Y%m%dT%H%M%SZ" t)))

(defn.lua get-aws-defaults
  "gets aws defaults"
  {:added "4.0"}
  [aws]
  (:= aws (or aws {}))
  (return {:scheme  (or (. aws scheme) "http")
           :host    (or (. aws host) "127.0.0.1")
           :port    (or (. aws port) 9000)
           :user    (or (. aws user) "minioadmin" #_"admin")
           :secret  (or (. aws secret) "minioadmin" #_"password")
           :t       (or (. aws t) (u/time))
           :region  (or (. aws region) "us-east-1")
           :service (or (. aws service) "s3")}))

(defn.lua get-aws-scope
  "gets the scope for a call"
  {:added "4.0"}
  [t region service]
  (return (k/cat (-/get-date-short t)
                 "/"
                 (or region "us-east-1")
                 "/"
                 (or service "s3")
                 "/"
                 -/AWS_REQUEST)))

(defn.lua get-aws-credential
  "gets the aws credential"
  {:added "4.0"}
  [user t region service]
  (return (k/cat user "/" (-/get-aws-scope t region service))))

(defn.lua get-signing-key
  "gets the aws signing key"
  {:added "4.0"}
  [secret t region service]
  (var h0    (ssl/hmac (k/cat "AWS4" secret)
                       (-/get-date-short t)
                       "sha256"))
  (var h1    (ssl/hmac h0 (or region  "us-east-1") "sha256"))
  (var h2    (ssl/hmac h1 (or service "s3") "sha256"))
  (var h3    (ssl/hmac h2 -/AWS_REQUEST "sha256"))
  (return h3))

;;
;; canonical
;;

(defn.lua get-canonical-headers
  "gets the canonical headers"
  {:added "4.0"}
  [headers]
  (var out [])
  (k/for:object [[k v] headers]
    (var nk (k/to-lowercase k))
    (when (not (k/get-key -/AWS_EXCLUDE nk))
      (var nv (k/replace v "[ ]+" " "))
      (x:arr-push out [nk nv])))
  (return (k/arr-sort out k/first k/lt-string)))

(defn.lua get-canonical-route
  "gets the canonical route"
  {:added "4.0"}
  [route]
  (var arr     (k/split route "?"))
  (var path    (k/first arr))
  (var search  (k/second arr))
  (var params [])
  (when search
    (k/for:array [pair (k/split search "&")]
      (var [k v] (k/split pair "="))
      (x:arr-push params [k (or v "")])))
  (return {:path path
           :params (k/arr-sort params k/first k/lt-string)}))

(defn.lua get-canonical
  "gets the canonical string"
  {:added "4.0"}
  [method route headers hash]
  (var #{path params} route)
  (return
   (k/arr-join
    [(k/to-uppercase method)
     path
     (k/arr-join (k/arr-map params (fn:> [arr] (k/cat (k/first arr) "=" (k/second arr))))
                 "&")
     (k/arr-join (k/arr-map headers (fn:> [arr] (k/cat (k/first arr) ":" (k/second arr))))
                 "\n")
     ""
     (k/arr-join (k/arr-map headers k/first) ";")
     (or hash "UNSIGNED-PAYLOAD")]
    "\n")
   headers))

(defn.lua get-signing-input
  "gets the signing input"
  {:added "4.0"}
  [canonical t region service]
  (return
   (k/arr-join [-/AWS_ALGO
                (-/get-date-long t)
                (-/get-aws-scope t region service)
                (n/to-hex (-/get-sha256 canonical))]
               "\n")))

(defn.lua header-authorization
  "creates the header authorization"
  {:added "4.0"}
  [aws method route headers hash]
  (var #{user secret t region service} aws)
  (var signing-key (-/get-signing-key secret t region service))
  (var canonical-route   (-/get-canonical-route route))
  (var canonical-headers (-/get-canonical-headers headers))
  (var canonical   (-/get-canonical method
                                    canonical-route
                                    canonical-headers
                                    hash))
  (var signing-input (-/get-signing-input canonical t region service))
  (var credential  (-/get-aws-credential user t region service))
  
  (var signature   (n/to-hex (ssl/hmac signing-key signing-input "sha256")))
  (var out (k/cat -/AWS_ALGO
                  " "
                  "Credential="
                  credential
                  ", "
                  "SignedHeaders="
                  (k/arr-join (k/arr-map canonical-headers k/first) ";")
                  ", "
                  "Signature="
                  signature))
  (return out))

;;
;; REQUEST
;;

(defn.lua make-request-map
  "make request map"
  {:added "4.0"}
  [aws method route headers body]
  (:= aws    (-/get-aws-defaults aws))
  (var #{scheme host port} aws)
  (var hash  (n/to-hex (-/get-sha256 body)))
  (var headers (k/obj-assign
                headers
                {"host" (k/cat host ":" port)
                 "x-amz-content-sha256" hash
                 "x-amz-date" (-/get-date-long (. aws t))}))
  (var authorization (-/header-authorization
                      aws
                      method
                      (k/cat "/" route)
                      headers
                      hash))
  (k/set-key headers "authorization" authorization)
  (var url (k/cat scheme "://" host ":" port "/" route))
  (return
   {:method method
    :headers headers
    :body body
    :url  url}))

(defn.lua with-parse
  "parses the response body"
  {:added "4.0"}
  [response opts]
  (var out (k/obj-pick response -/REQ_KEYS))
  (var body (. out body))
  (if (and (k/not-empty? body)
           (k/starts-with? body "<?xml"))
    (k/set-key out "body"
               (xml/to-brief (xml/parse-xml (k/second (k/split body "\n"))))))
  (return out))

(defn.lua make-request-auth
  "makes an api auth request"
  {:added "4.0"}
  [aws method route headers body opts]
  (var m (-/make-request-map aws method route headers body))
  (var #{url} m)
  (return (-> (http/request-uri (http/new) url m)
              (-/with-parse opts))))

(defn.lua make-request-public
  "makes an api public request"
  {:added "4.0"}
  [aws method route headers body opts]
  (:= aws    (-/get-aws-defaults aws))
  (var #{scheme host port} aws)
  (var headers {"host" (k/cat host ":" port)})
  (var url (k/cat scheme "://" host ":" port "/" route))
  (return (-> (http/request-uri (http/new) url {:method method
                                                :headers headers
                                                :body body})
              (-/with-parse opts))))

(defn.lua make-request
  "makes an api auth request"
  {:added "4.0"}
  [aws opts]
  (var #{method route headers body public} opts)
  (cond public
        (return
         (-/make-request-public aws method route (or headers {}) (or body "") opts))
        
        :else
        (return
         (-/make-request-auth aws method route (or headers {}) (or body "") opts))))

