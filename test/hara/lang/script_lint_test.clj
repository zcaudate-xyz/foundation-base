(ns hara.lang.script-lint-test
  (:require [xt.lang.common-data]
             [xt.lang.common-lib]
             [js.blessed]
             [hara.lang :as l]
             [hara.lang.impl :as impl]
             [hara.lang.library :as lib]
             [hara.lang.runtime :as rt]
             [hara.lang.script-lint :refer :all])
  (:use code.test))

(def +library+
  (let [lib (impl/clone-default-library)]
    (impl/with:library [lib]
      (require '[xt.lang.common-data] :reload)
      (require '[xt.lang.common-lib] :reload)
      (require '[js.blessed] :reload))
    lib))

(rt/install-lang! :lua)

^{:refer hara.lang.script-lint/get-reserved-raw :added "4.0"}
(fact "gets all reserved symbols in the grammar"
  (get-reserved-raw :lua) => set?)

^{:refer hara.lang.script-lint/collect-vars :added "4.0"}
(fact "collects all vars"

  (collect-vars '#{[hello #{world}]})
  => '#{hello world})

^{:refer hara.lang.script-lint/collect-module-globals :added "4.0"}
(fact "collects global symbols from module"

  (impl/with:library [+library+]
    (collect-module-globals (l/get-module
                             +library+
                             :js
                             'js.blessed)))
  => '#{BlessedContrib ReactBlessedContrib Bresenham ReactBlessed Blessed Drawille})

^{:refer hara.lang.script-lint/collect-sym-vars :added "4.0"}
(fact "collect symbols and vars"
  (collect-sym-vars {:form '(defn foo [x] x)
                     :lang :lua
                     :op-key :defn}
                    {:native {} :static {}})
  => {:vars #{'x} :syms #{'x}})

^{:refer hara.lang.script-lint/sym-check-linter :added "4.0"}
(fact "checks the linter"

  (impl/with:library [+library+]
    (def +out+
      (doseq [[id module] (:modules (l/get-book
                                     +library+
                                     :js))]
        (doseq [[id entry] (:code module)]
          (sym-check-linter entry
                            module
                            (:globals (:js @+settings+))))))))

^{:refer hara.lang.script-lint/lint-set :added "4.0"}
(fact "sets the linter for a namespace"
  (lint-set 'hara.lang.script-lint-test) => map?)

^{:refer hara.lang.script-lint/lint-clear :added "4.0"}
(fact "clears all linted namespaces"
  (lint-clear) => {})

^{:refer hara.lang.script-lint/lint-needed? :added "4.0"}
(fact "checks if lint is needed"
  (lint-needed? 'hara.lang.script-lint-test) => nil)

^{:refer hara.lang.script-lint/lint-entry :added "4.0"}
(fact "lints a single entry"
  (lint-entry {:form '(defn foo [x] x)
               :lang :lua
               :op-key :defn}
              {:native {} :static {}})
  => nil)

(comment

  (collect-sym-vars @js.blessed.ui-label/ActionLabel
                    #{})

  (collect-sym-vars @js.react-native-test/ListPaneDemo
                    #{})


  (collect-sym-vars @xt.event.base-listener/add-listener)
  (collect-sym-vars @xt.lang.common-repl/socket-connect)

  (get
   (set (keys
         (:reserved (hara.lang/grammar :js))))))
