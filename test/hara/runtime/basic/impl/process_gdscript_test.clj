(ns hara.runtime.basic.impl.process-gdscript-test
  (:require [hara.lang :as l]
            [hara.lang.impl :as impl]
            [hara.model.spec-gdscript]
            [hara.runtime.basic.impl.process-gdscript :as gd])
  (:use code.test))

^{:refer hara.runtime.basic.impl.process-gdscript/default-body-transform :added "4.1"}
(fact "wraps body forms for return"
  (impl/emit-as :gdscript [(gd/default-body-transform '[(:= a 1) (+ a 2)] {:bulk true})])
  => "a = 1\nreturn a + 2")

^{:refer hara.runtime.basic.impl.process-gdscript/default-oneshot-wrap :added "4.1"}
(fact "wraps emitted GDScript in a SceneTree script"
  (let [body (impl/emit-as :gdscript [(gd/default-body-transform '[(:= a 1) (+ a 2)] {:bulk true})])]
    (gd/default-oneshot-wrap body))
  => (str "extends SceneTree\n\n"
          "func OUT_FN():\n"
          "  a = 1\n"
          "  return a + 2\n\n"
          "func _init():\n"
          "  var __result__ = OUT_FN()\n"
          "  print(JSON.stringify(__result__))\n"
          "  quit()\n"))
