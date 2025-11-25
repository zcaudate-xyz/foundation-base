(ns rt.basic.type-twostep-test
  (:use code.test)
  (:require [rt.basic.type-twostep :as p]
            [std.lib :as h]
            [std.fs :as fs]
            [rt.basic.type-common :as common]))

^{:refer rt.basic.type-twostep/sh-exec :added "4.0"}
(fact "basic function for executing the compile and run process"
  (with-redefs [h/sh (fn [& _] {:out "ok"})
                spit (fn [& _] nil)
                fs/parent (fn [_] "/tmp")
                fs/file-name (fn [_] "tmp")]
    (p/sh-exec ["cmd"] "body" {:extension "ext"}))
  => "{:out \"ok\"}")

^{:refer rt.basic.type-twostep/raw-eval-twostep :added "4.0"}
(fact "evaluates the twostep evaluation"
  (with-redefs [p/sh-exec (fn [_ _ _] "result")]
    (p/raw-eval-twostep {:exec [] :process {}} "body"))
  => "result")

^{:refer rt.basic.type-twostep/invoke-ptr-twostep :added "4.0"}
(fact "invokes twostep pointer"
  ;; delegates to default
  )

^{:refer rt.basic.type-twostep/rt-twostep-setup :added "4.0"}
(fact "setup params for the twostep runtime"
  (with-redefs [common/get-program-default (fn [& _] :program)
                common/get-options (fn [& _] {:a 1})
                common/get-program-exec (fn [& _] ["cmd"])]
    (p/rt-twostep-setup :lang nil nil nil))
  => [:program {:a 1} ["cmd"]])

^{:refer rt.basic.type-twostep/rt-twostep:create :added "4.0"}
(fact "creates a twostep runtime"
  (with-redefs [p/rt-twostep-setup (fn [& _] [:program {:process :opts} ["cmd"]])
                common/get-program-flags (fn [& _] {:twostep true})]
    (p/rt-twostep:create {:lang :lang :program :program}))
  => map?)

^{:refer rt.basic.type-twostep/rt-twostep :added "4.0"}
(fact "creates an active twostep runtime"
  (with-redefs [p/rt-twostep:create (fn [m] m)]
    (p/rt-twostep {:a 1}))
  => {:a 1})
