(ns xt.db.helpers.supabase-pull-live-xtalk-test
  (:require [hara.lang :as l]))

(l/script :js
  {:require [[js.lib.client-fetch :as js-fetch]
             [xt.lang.spec-base :as xt]
             [xt.protocol.impl.client-fetch :as fetch]]})

(defn.js patch-live-request
  [input]
  (var request (xt/x:obj-clone (or input {})))
  (var method (or (xt/x:get-key request "method") "GET"))
  (var headers (xt/x:obj-clone (or (xt/x:get-key request "headers") {})))
  (when (and (== "GET" method)
             (xt/x:not-nil? (xt/x:get-key headers "Content-Profile"))
             (xt/x:nil? (xt/x:get-key headers "Accept-Profile")))
    (xt/x:set-key headers
                  "Accept-Profile"
                  (xt/x:get-key headers "Content-Profile")))
  (xt/x:set-key request "method" method)
  (xt/x:set-key request "headers" headers)
  (return request))

(defn.js make-live-client
  [input]
  (var raw (xt/x:obj-clone (or input {})))
  (xt/x:set-key
   raw
   "request_sync"
   (fn [request _opts]
     (:= request (-/patch-live-request request))
     (var args ["-sS"
                "-X" (xt/x:get-key request "method")
                "-w" "\n%{http_code}"
                (xt/x:get-key request "url")])
     (xt/for:object [[k v] (xt/x:get-key request "headers")]
       (xt/x:arr-push args "-H")
       (xt/x:arr-push args (xt/x:cat k ": " v)))
     (when (xt/x:not-nil? (xt/x:get-key request "body"))
       (xt/x:arr-push args "-H")
       (xt/x:arr-push args "Content-Type: application/json")
       (xt/x:arr-push args "-d")
       (xt/x:arr-push args
                      (:? (xt/x:is-string? (xt/x:get-key request "body"))
                          (xt/x:get-key request "body")
                          (xt/x:json-encode (xt/x:get-key request "body")))))
     (var proc (. (require "child_process")
                  (spawnSync "curl" args {"encoding" "utf8"})))
     (when (not= 0 (. proc ["status"]))
       (xt/x:err (or (. proc ["stderr"]) "curl failed")))
     (var stdout (or (. proc ["stdout"]) ""))
     (var idx (. stdout (lastIndexOf "\n")))
     (var body (:? (>= idx 0)
                   (. stdout (substring 0 idx))
                   stdout))
     (var status-str (:? (>= idx 0)
                         (. stdout (substring (+ idx 1)))
                         "200"))
     (return {"status" (parseInt status-str 10)
              "body" (js-fetch/decode-body body)})))
  (return (fetch/client-create raw {})))

(l/script :python
  {:require [[python.lib.client-fetch :as py-fetch]
             [xt.lang.spec-base :as xt]
             [xt.protocol.impl.client-fetch :as fetch]]})

(defn.py patch-live-request
  [input]
  (var request (xt/x:obj-clone (or input {})))
  (var method (or (xt/x:get-key request "method") "GET"))
  (var headers (xt/x:obj-clone (or (xt/x:get-key request "headers") {})))
  (when (and (== "GET" method)
             (xt/x:not-nil? (xt/x:get-key headers "Content-Profile"))
             (xt/x:nil? (xt/x:get-key headers "Accept-Profile")))
    (xt/x:set-key headers
                  "Accept-Profile"
                  (xt/x:get-key headers "Content-Profile")))
  (xt/x:set-key request "method" method)
  (xt/x:set-key request "headers" headers)
  (return request))

(defn.py make-live-client
  [input]
  (var raw (xt/x:obj-clone (or input {})))
  (xt/x:set-key
   raw
   "request_sync"
   (fn [request _opts]
     (return (py-fetch/native-request (-/patch-live-request request)))))
  (return (fetch/client-create raw {})))

(l/script :lua
  {:require [[lua.core :as lua]
             [xt.lang.spec-base :as xt]
             [xt.protocol.impl.client-fetch :as fetch]]})

(defn.lua patch-live-request
  [input]
  (var request (xt/x:obj-clone (or input {})))
  (var method (or (xt/x:get-key request "method") "GET"))
  (var headers (xt/x:obj-clone (or (xt/x:get-key request "headers") {})))
  (when (and (== "GET" method)
             (xt/x:not-nil? (xt/x:get-key headers "Content-Profile"))
             (xt/x:nil? (xt/x:get-key headers "Accept-Profile")))
    (xt/x:set-key headers
                  "Accept-Profile"
                  (xt/x:get-key headers "Content-Profile")))
  (xt/x:set-key request "method" method)
  (xt/x:set-key request "headers" headers)
  (return request))

(defn.lua decode-body
  [body]
  (cond (not (xt/x:is-string? body))
        (return body)

        (== "" body)
        (return nil)

        :else
        (try
          (return (xt/x:json-decode body))
          (catch err
            (return body)))))

(defn.lua shell-quote
  [s]
  (return (xt/x:cat "'" (xt/x:to-string s) "'")))

(defn.lua make-live-client
  [input]
  (var raw (xt/x:obj-clone (or input {})))
  (xt/x:set-key
   raw
   "request_sync"
   (fn [request _opts]
     (:= request (-/patch-live-request request))
     (var command
          (xt/x:cat
           "curl -sS"
           " -X " (-/shell-quote (xt/x:get-key request "method"))
           " -w " (-/shell-quote "\n%{http_code}")
           " " (-/shell-quote (xt/x:get-key request "url"))))
     (xt/for:object [[k v] (xt/x:get-key request "headers")]
       (:= command
           (xt/x:cat command
                     " -H "
                     (-/shell-quote (xt/x:cat k ": " v)))))
     (when (xt/x:not-nil? (xt/x:get-key request "body"))
       (:= command
           (xt/x:cat command
                     " -H "
                     (-/shell-quote "Content-Type: application/json")
                     " -d "
                     (-/shell-quote
                      (:? (xt/x:is-string? (xt/x:get-key request "body"))
                          (xt/x:get-key request "body")
                          (xt/x:json-encode (xt/x:get-key request "body")))))))
     (var proc (lua/io-popen command))
     (var stdout (. proc (read "*a")))
     (. proc (close))
     (var parts (xt/x:str-split (or stdout "") "\n"))
     (var status-str (or (xt/x:arr-pop parts) "200"))
     (var body (xt/x:str-join "\n" parts))
     (return {"status" (xt/x:to-number status-str)
              "body" (-/decode-body body)})))
  (return (fetch/client-create raw {})))

(l/script :dart
  {:require [[dart.lib.client-fetch :as dart-fetch]
             [xt.lang.spec-base :as xt]
             [xt.protocol.impl.client-fetch :as fetch]]
   :import [["dart:io" :as io]]})

(defn.dt patch-live-request
  [input]
  (var request (xt/x:obj-clone (or input {})))
  (var method (or (xt/x:get-key request "method") "GET"))
  (var headers (xt/x:obj-clone (or (xt/x:get-key request "headers") {})))
  (when (and (== "GET" method)
             (xt/x:not-nil? (xt/x:get-key headers "Content-Profile"))
             (xt/x:nil? (xt/x:get-key headers "Accept-Profile")))
    (xt/x:set-key headers
                  "Accept-Profile"
                  (xt/x:get-key headers "Content-Profile")))
  (xt/x:set-key request "method" method)
  (xt/x:set-key request "headers" headers)
  (return request))

(defn.dt shell-quote
  [s]
  (return (xt/x:cat "'" (xt/x:to-string s) "'")))

(defn.dt make-live-client
  [input]
  (var raw (xt/x:obj-clone (or input {})))
  (xt/x:set-key
   raw
   "request_sync"
   (fn [request _opts]
     (:= request (-/patch-live-request request))
     (var command
          (xt/x:cat
           "curl -sS"
           " -X " (-/shell-quote (xt/x:get-key request "method"))
           " -w " (-/shell-quote "\n%{http_code}")
           " " (-/shell-quote (xt/x:get-key request "url"))))
     (xt/for:object [[k v] (xt/x:get-key request "headers")]
       (:= command
           (xt/x:cat command
                     " -H "
                     (-/shell-quote (xt/x:cat k ": " v)))))
     (when (xt/x:not-nil? (xt/x:get-key request "body"))
       (:= command
           (xt/x:cat command
                     " -H "
                     (-/shell-quote "Content-Type: application/json")
                     " -d "
                     (-/shell-quote
                      (:? (xt/x:is-string? (xt/x:get-key request "body"))
                          (xt/x:get-key request "body")
                          (xt/x:json-encode (xt/x:get-key request "body")))))))
     (var result (io.Process.runSync "/bin/bash" ["-lc" command]))
     (when (not= 0 (. result exitCode))
       (xt/x:err (or (. result stderr) "curl failed")))
     (var stdout (xt/x:to-string (. result stdout)))
     (var parts (xt/x:str-split stdout "\n"))
     (var status-str (or (xt/x:arr-pop parts) "200"))
     (var body (xt/x:str-join "\n" parts))
     (return {"status" (xt/x:to-number status-str)
              "body" (dart-fetch/decode-body body)})))
  (return (fetch/client-create raw {})))
