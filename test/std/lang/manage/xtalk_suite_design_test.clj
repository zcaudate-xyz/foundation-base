(ns std.lang.manage.xtalk-suite-design-test
  (:require [std.lang.manage :as manage]
            [std.lang.runtime-meta :as runtime])
  (:use code.test))

^{:refer std.lang.runtime-meta/runtime-type :added "4.1"}
(fact "runtime config distinguishes fast and batched xt suite strategies"
  [(runtime/runtime-type :js)
   (runtime/runtime-check-mode :js)
   (runtime/runtime-type :dart)
   (runtime/runtime-check-mode :dart)
   (runtime/runtime-suite-groups [:js :dart :go])]
  => [:basic
      :realtime
      :twostep
      :batched
      {:batched [:dart :go]
       :realtime [:js]}])

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
