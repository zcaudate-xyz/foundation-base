(ns std.lib.system.partial-test
  (:use code.test)
  (:require [std.lib.system.partial :refer :all]
            [std.lib.system.topology :as topology]
            [std.lib.system.type :as type]))

(def +sys+
  (type/system
   {:model   [identity]
    :ids     [[identity]]
    :traps   [[identity] [:model {:as :raw}] [:ids {:type :element :as :id}]]
    :entry   [identity :model :ids]
    :nums    [[{:expose :id}] :traps]
    :model-tag  [{:expose :tag
                  :setup identity}  :model]}
   {:model {:tag :barbie}
    :ids   [1 2 3 4 5]
    :traps [{} {} {} {} {}]
    :entry {}}))

^{:refer std.lib.system.partial/valid-subcomponents :added "3.0"}
(fact "returns only the components that will work (for partial systems)" ^:hidden

  (valid-subcomponents
   (topology/long-form {:model  [identity]
                        :tag    [{:expose :tag} :model]
                        :kramer [identity :tag]})
   [:model])
  => [:model :tag])

^{:refer std.lib.system.partial/system-subkeys :added "3.0"}
(fact "returns the subcomponents connect with the system" ^:hidden

  (system-subkeys +sys+ #{:entry})
  => #{:ids :entry :model})

^{:refer std.lib.system.partial/subsystem :added "3.0"}
(fact "returns the subsystem given certain keys"

  (subsystem +sys+ #{:entry})
  => any?)

^{:refer std.lib.system.partial/wait :added "3.0"}
(fact "wait for a system entry to come online"
  (resolve 'wait) => var?)

^{:refer std.lib.system.partial/wait-for :added "3.0"}
(fact "wait for all system entries to come online"
  (resolve 'wait-for) => var?)

(comment
  (./import))
