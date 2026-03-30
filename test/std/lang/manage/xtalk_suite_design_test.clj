(ns std.lang.manage.xtalk-suite-design-test
  (:require [clojure.string :as str]
            [std.lang.manage :as manage]
            [std.lang.manage.xtalk-scaffold :as scaffold])
  (:use code.test))

(def runtime-template-forms
  (read-string
   "[(ns xt.lang.base-lib-js-test
       (:require [std.lang :as l]
                 [xt.lang.base-lib :as k])
       (:use code.test))
      (l/script- :js {:runtime :basic})
      (fact \"identity function\"
        ^:hidden
        (!.js (k/identity 1))
        => 1)]"))

^{:refer std.lang.manage.xtalk-scaffold/runtime-type :added "4.1"}
(fact "runtime config distinguishes fast and batched xt suite strategies"
  [(scaffold/runtime-type :js)
   (scaffold/runtime-check-mode :js)
   (scaffold/runtime-type :dart)
   (scaffold/runtime-check-mode :dart)
   (scaffold/runtime-suite-groups [:js :dart :go])]
  => [:basic
      :realtime
      :twostep
      :batched
      {:batched [:dart :go]
       :realtime [:js]}])

^{:refer std.lang.manage.xtalk-scaffold/template-runtime-test-forms :added "4.1"}
(fact "templating a common js runtime suite to dart keeps the twostep runtime"
  (let [out-forms (scaffold/template-runtime-test-forms runtime-template-forms :js :dart)
        out (scaffold/render-top-level-forms out-forms)]
    [(= 'xt.lang.base-lib-dt-test
        (second (first out-forms)))
     (str/includes? out "(l/script- :dart {:runtime :twostep})")
     (str/includes? out "!.dt")
     (not (str/includes? out "!.js"))])
  => [true true true true])

^{:refer std.lang.manage/xtalk-runtime-inventory :added "4.1"}
(fact "runtime inventory exposes suite strategy for slow xt runtimes"
  (select-keys (get (manage/xtalk-runtime-inventory {:langs [:dart :js]})
                    :dart)
               [:runtime?
                :runtime-executable?
                :runtime-type
                :runtime-check-mode])
  => {:runtime? true
      :runtime-executable? false
      :runtime-type :twostep
      :runtime-check-mode :batched})
