(ns std.lang.base.script-lint-test
  (:use code.test)
  (:require [std.lang.base.script-lint :refer :all]
            [std.lib :as h]
            [js.blessed]))

^{:refer std.lang.base.script-lint/get-reserved-raw :added "4.0"}
(fact "gets all reserved symbols in the grammar"
  (get-reserved-raw :lua) => set?)

^{:refer std.lang.base.script-lint/collect-vars :added "4.0"}
(fact "collects all vars"
  ^:hidden
  
  (collect-vars '#{[hello #{world}]})
  => '#{hello world})

^{:refer std.lang.base.script-lint/collect-module-globals :added "4.0"}
(fact "collects global symbols from module"
  ^:hidden
  
  (collect-module-globals (std.lang/get-module
                           (std.lang/default-library)
                           :js
                           'js.blessed))
  => '#{BlessedContrib ReactBlessedContrib Bresenham ReactBlessed Blessed Drawille})

^{:refer std.lang.base.script-lint/collect-sym-vars :added "4.0"}
(fact "collect symbols and vars"
  (collect-sym-vars {:form '(defn foo [x] x)
                     :lang :lua
                     :op-key :defn}
                    {:native {} :static {}})
  => {:vars #{'x} :syms #{'x}})

^{:refer std.lang.base.script-lint/sym-check-linter :added "4.0"}
(fact "checks the linter"
  ^:hidden
  
  (def +out+
    (doseq [[id module] (:modules (std.lang/get-book
                                   (std.lang/default-library)
                                   :js))]
      (doseq [[id entry] (:code module)]
        (sym-check-linter entry
                          module
                          (:globals (:js @+settings+)))))))

^{:refer std.lang.base.script-lint/lint-set :added "4.0"}
(fact "sets the linter for a namespace"
  (lint-set 'std.lang.base.script-lint-test) => map?)

^{:refer std.lang.base.script-lint/lint-clear :added "4.0"}
(fact "clears all linted namespaces"
  (lint-clear) => {})

^{:refer std.lang.base.script-lint/lint-needed? :added "4.0"}
(fact "checks if lint is needed"
  (lint-needed? 'std.lang.base.script-lint-test) => nil)

^{:refer std.lang.base.script-lint/lint-entry :added "4.0"}
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
  
  
  (collect-sym-vars @xt.lang.event-common/add-listener)
  (collect-sym-vars @xt.lang.base-repl/socket-connect)
  
  (get 
   (set (keys
         (:reserved (std.lang/grammar :js))))))
