(ns hara.runtime.basic.type-twostep-test
  (:require [hara.runtime.basic.impl.process-c]
            [hara.runtime.basic.impl-annex.process-rust]
            [hara.runtime.basic.type-common :as common]
            [hara.runtime.basic.type-twostep :as p]
            [std.fs :as fs]
            [std.lib.os :as os]
            [std.lib.env :as env])
  (:use code.test))

(fact:global
 {:skip (not (and (env/program-exists? "gcc")
                  (env/program-exists? "rustc")))})

(fact "returns compile stderr when compilation fails"

  (with-redefs [os/sh (fn [_] :compile)
                os/sh-wait (fn [_] nil)
                os/sh-output (fn [_] {:exit 1 :out "" :err "compile failed\n"})
                spit (fn [& _] nil)
                fs/parent (fn [_] "/tmp")
                fs/file-name (fn [_] "tmp")
                fs/delete (fn [& _] nil)]
    (p/sh-exec ["cmd"] "body" {:extension "ext" :stderr true}))
  => "compile failed")

(^{:refer hara.runtime.basic.type-twostep-test/CANARY-GCC :adopt true :added "4.0"}
 fact "runs a full compile and execution cycle for c twostep"

  (p/raw-eval-twostep
   (p/rt-twostep {:lang :c
                  :program :gcc})
   "#include <stdio.h>\nint main(){ printf(\"%d\", 2 + 3); return 0; }")
  => "5")

(^{:refer hara.runtime.basic.type-twostep-test/CANARY-RUSTC :adopt true :added "4.0"}
 fact "runs a full compile and execution cycle for rust twostep"

  (p/raw-eval-twostep
   (p/rt-twostep {:lang :rust
                  :program :rustc})
   "fn main() { println!(\"{}\", 2 + 3); }")
  => "5")

^{:refer hara.runtime.basic.type-twostep/sh-exec :added "4.0"}
(fact "basic function for executing the compile and run process"

  (with-redefs [os/sh (fn [opts]
                        (case (first (:args opts))
                          "cmd" :compile
                          "./tmp" :run))
                os/sh-wait (fn [_] nil)
                os/sh-output (fn [proc]
                               (case proc
                                 :compile {:exit 0 :out "" :err ""}
                                 :run {:exit 0 :out "ok\n" :err ""}))
                spit (fn [& _] nil)
                fs/parent (fn [_] "/tmp")
                fs/file-name (fn [_] "tmp")
                fs/delete (fn [& _] nil)]
    (p/sh-exec ["cmd"] "body" {:extension "ext"}))
  => "ok")

^{:refer hara.runtime.basic.type-twostep/local-exec-available? :added "4.1"}
(fact "TODO")

^{:refer hara.runtime.basic.type-twostep/sh-exec-docker :added "4.1"}
(fact "TODO")

^{:refer hara.runtime.basic.type-twostep/sh-exec-portable :added "4.1"}
(fact "TODO")

^{:refer hara.runtime.basic.type-twostep/raw-eval-twostep :added "4.0"}
(fact "evaluates the twostep evaluation"

  (with-redefs [p/sh-exec (fn [_ _ _] "result")]
    (p/raw-eval-twostep {:exec [] :process {}} "body"))
  => "result")

^{:refer hara.runtime.basic.type-twostep/invoke-ptr-twostep :added "4.0"}
(fact "invokes twostep pointer")

^{:refer hara.runtime.basic.type-twostep/rt-twostep-setup :added "4.0"}
(fact "setup params for the twostep runtime"

  (with-redefs [common/get-program-default (fn [& _] :program)
                common/get-options (fn [& _] {:a 1})
                common/get-program-exec (fn [& _] ["cmd"])]
    (p/rt-twostep-setup :lang nil nil nil))
  => [:program {:a 1} ["cmd"]])

^{:refer hara.runtime.basic.type-twostep/rt-twostep:create :added "4.0"}
(fact "creates a twostep runtime"

  (with-redefs [p/rt-twostep-setup (fn [& _] [:program {:process :opts} ["cmd"]])
                common/get-program-flags (fn [& _] {:twostep true})]
    (p/rt-twostep:create {:lang :lang :program :program}))
  => map?)

^{:refer hara.runtime.basic.type-twostep/rt-twostep :added "4.0"}
(fact "creates an active twostep runtime"

  (with-redefs [p/rt-twostep:create (fn [m] m)]
    (p/rt-twostep {:a 1}))
  => {:a 1})