tahto/runtime/basic/type_verify_test.clj:1:(ns tahto.runtime.basic.type-verify-test
  (:use code.test)
tahto/runtime/basic/type_verify_test.clj:3:  (:require [tahto.runtime.basic.type-verify :refer :all]
            [std.lib.os :as os]
            [std.fs :as fs]))

tahto/runtime/basic/type_verify_test.clj:7:^{:refer tahto.runtime.basic.type-verify/verify-exec-oneshot :added "4.1"}
(fact "returns the source body when the checker exits 0"
  (with-redefs [os/sh (fn [_] :proc)
                os/sh-wait (fn [_] nil)
                os/sh-output (fn [_] {:exit 0 :out "" :err ""})]
    (verify-exec-oneshot ["checker"] "body" {}))
  => "body"

  (with-redefs [os/sh (fn [_] :proc)
                os/sh-wait (fn [_] nil)
                os/sh-output (fn [_] {:exit 1 :out "" :err "bad syntax"})]
    (verify-exec-oneshot ["checker"] "body" {:stderr true}))
  => "bad syntax")

tahto/runtime/basic/type_verify_test.clj:21:^{:refer tahto.runtime.basic.type-verify/verify-exec-file :added "4.1"}
(fact "writes source to a temp file and runs a file-based checker"
  (with-redefs [os/sh (fn [_] :proc)
                os/sh-wait (fn [_] nil)
                os/sh-output (fn [_] {:exit 0 :out "" :err ""})
                fs/delete (fn [_] nil)]
    (verify-exec-file ["checker" "__FILE__"] "body" {:extension "lua"}))
  => "body"

  (with-redefs [os/sh (fn [_] :proc)
                os/sh-wait (fn [_] nil)
                os/sh-output (fn [_] {:exit 1 :out "" :err "compile error"})
                fs/delete (fn [_] nil)]
    (verify-exec-file ["checker" "__FILE__"] "body" {:extension "lua" :stderr true}))
  => "compile error")

tahto/runtime/basic/type_verify_test.clj:37:^{:refer tahto.runtime.basic.type-verify/verify-exec-twostep :added "4.1"}
(fact "delegates to verify-exec-file"
  (with-redefs [verify-exec-file (fn [_args body _opts]
                                   (str "verified:" body))]
    (verify-exec-twostep ["checker"] "body" {:extension "c"}))
  => "verified:body")