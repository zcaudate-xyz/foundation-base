tahto/runtime/godot/client_test.clj:1:(ns tahto.runtime.godot.client-test
  (:require [tahto.core :as h]
            [tahto.core.type-shared :as shared]
tahto/runtime/godot/client_test.clj:4:            [tahto.runtime.godot.client :as client]
            [std.lib.component :as component]
            [std.lib.env :as env])
  (:use code.test))

(fact:global {:skip (not (or (env/program-exists? "godot")
                              (env/program-exists? "godot-4")))})

tahto/runtime/godot/client_test.clj:12:^{:refer tahto.runtime.godot.client/client:create :added "4.1"}
(fact "creates a godot client record"
  (let [rt (client/client:create {:port 12345})]
    [(boolean rt)
     (= "127.0.0.1" (:host rt))
     (= 12345 (:port rt))])
  => [true true true])

tahto/runtime/godot/client_test.clj:20:^{:refer tahto.runtime.godot.client/raw-eval-godot :added "4.1"}
(fact "evaluates gdscript code through the godot runtime"
  (let [rt (client/godot {:bench :scratch})]
    (try
      (number? (client/raw-eval-godot rt (str "extends Node\n\n"
                                                "func eval():\n"
                                                "    return 1 + 2 + 3")))
      (finally
        (component/stop rt))))
  => true)

tahto/runtime/godot/client_test.clj:31:^{:refer tahto.runtime.godot.client/invoke-ptr-godot :added "4.1"}
(fact "invokes a pointer through the godot runtime"
  (let [rt (client/godot {:bench :scratch})]
    (try
      (number? (client/invoke-ptr-godot
                rt
                (h/ptr :gdscript {:module (ns-name *ns*)})
                ['(+ 1 2 3)]))
      (finally
        (component/stop rt))))
  => true)

tahto/runtime/godot/client_test.clj:43:^{:refer tahto.runtime.godot.client/godot:create :added "4.1"}
(fact "creates a godot runtime record"
  (let [rt (client/godot:create {})]
    [(boolean rt)
     (= :godot (:tag rt))
     (= "127.0.0.1" (:host rt))])
  => [true true true])

tahto/runtime/godot/client_test.clj:51:^{:refer tahto.runtime.godot.client/godot :added "4.1"}
(fact "creates and starts a godot runtime"
  (let [rt (client/godot {:bench :scratch})]
    (try
      (boolean rt)
      (finally
        (component/stop rt))))
  => true)

tahto/runtime/godot/client_test.clj:60:^{:refer tahto.runtime.godot.client/godot-shared:create :added "4.1"}
(fact "creates a shared godot runtime"
  (let [rt (client/godot-shared:create {:id :shared-godot-test})]
    [(shared/rt-is-shared? rt)
     (= :shared-godot-test (:id rt))
tahto/runtime/godot/client_test.clj:65:     (= :tahto/rt.godot (get-in rt [:client :type]))])
  => [true true true])
