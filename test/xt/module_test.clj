(ns xt.module-test
  (:require [std.lang :as l]
            [std.lang.base.book :as book])
  (:use code.test))

(l/script- :js
  {:runtime :oneshot
   :config  {:program :nodejs}
   :require [[js.core :as j]
             [xt.module :as module]]})

(l/script- :lua
  {:runtime :oneshot
   :require [[lua.core :as u]
             [xt.module :as module]]})

(l/script- :python
  {:runtime :oneshot
   :require [[python.core :as y]
             [xt.module :as module]]})

^{:refer xt.module/current-module :added "4.0"}
(fact "gets the current module"
  ^:hidden

  (l/with:macro-opts [(l/rt:macro-opts :js)]
    (module/current-module 'js.core))
  => book/book-module?

  (l/with:macro-opts [(l/rt:macro-opts :lua)]
    (module/current-module 'lua.core))
  => book/book-module?

  (l/with:macro-opts [(l/rt:macro-opts :python)]
    (module/current-module 'python.core))
  => book/book-module?)

^{:refer xt.module/linked-natives :added "4.0"}
(fact "gets all linked natives")

^{:refer xt.module/current-natives :added "4.0"}
(fact "gets the current natives")

^{:refer xt.module/expose-module :added "4.0"}
(fact "helper function for additional libs")

^{:refer xt.module/module-native :added "4.0"}
(fact "returns the native map"
  ^:hidden
  
  (!.js  (module/module-native js.core))
  => {}
  
  (!.lua (module/module-native lua.core))
  => {}

  (!.py (module/module-native python.core))
  => {})

^{:refer xt.module/module-link :added "4.0"}
(fact "returns the module link map"
  ^:hidden
  
  (!.js  (module/module-link js.core))
  => {"-" "js.core"}
  
  (!.lua (module/module-link lua.core))
  => {"-" "lua.core"}

  (!.py (module/module-link python.core))
  => {"-" "python.core"})

^{:refer xt.module/module-internal :added "4.0"}
(fact "returns the module link map"
  ^:hidden
  
  (!.js  (module/module-internal js.core))
  => {"js.core" "-"}
  
  (!.lua (module/module-internal lua.core))
  => {"lua.core" "-"}

  (!.py (module/module-internal python.core))
  => {"python.core" "-"})

^{:refer xt.module/module-save :added "4.0"}
(fact "saves module to `module/*saved-module*` var")

(comment
  (./import)
  )
