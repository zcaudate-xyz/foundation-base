(ns std.lang.base.library-load-test
  (:require [std.lang.base.library-load :as loader]
            [std.lang.base.library :as lib]
            [std.lang.base.impl :as impl]
            [code.test :as t]
            [js.core])) ;; Require js.core to ensure it's loaded in default lib

(t/fact "load-string-into-library loads module and code into isolated library"
  (let [lib (impl/clone-default-library)
        code "(ns my.test.module (:require [std.lang :as l]))
              (l/script :js {:require [[js.core :as j]]})
              (defn.js hello [] (return \"world\"))"]

    (loader/load-string-into-library code lib 'my.test.module)

    (let [book (lib/get-book lib :js)
          module (get-in book [:modules 'my.test.module])
          entry (get-in module [:code 'hello])]

      (:op entry) => 'defn
      (:lang entry) => :js
      (:form entry) => '(defn hello [] (return "world")))))
