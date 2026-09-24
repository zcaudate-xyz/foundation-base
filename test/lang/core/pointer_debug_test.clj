(ns lang.core.pointer-debug-test
  (:use code.test)
  (:require [clojure.string :as string]
            [std.json :as json]
            [lang.base.book :as book]
            [lang.base.book-module :as module]
            [lang.base.emit-prep-lua-test :as prep]
            [lang.core.impl :as impl]
            [lang.core.impl-entry :as entry]
            [lang.core.library :as lib]
            [lang.core.library-snapshot :as snap]
            [lang.core.runtime :as runtime]
            [lang.core.script-macro :as macro]
            [lang.model.builtin.spec-js :as js]
            [lang.model.builtin.spec-xtalk :as xtalk]
            [lang.base.util :as ut]
            [std.lib.foundation :as f]
            [std.string.prose :as prose]
            [lang.core.pointer :as ptr]
            [lang-demos.js-003-expo-rn.main :as rn-main]))

(defn invoke-js-003-expo-rn-main
  []
  (ptr/ptr-invoke-script (ut/lang-pointer :js
                                          {:module 'lang-demos.js-003-expo-rn.main
                                           })
                         [rn-main/App
                          rn-main/App]
                         {:layout :full})
  nil)

^{:refer lang.core.pointer/ptr-invoke-script :added "4.0"}
(fact "emits a script with dependencies"

  (ptr/ptr-invoke-script +ptr+ [1 2] {:layout :full})
  => "1 + 2"

  (invoke-js-003-expo-rn-main)
  
  
  
  )



