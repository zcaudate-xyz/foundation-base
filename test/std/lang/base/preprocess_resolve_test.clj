(ns std.lang.base.preprocess-resolve-test
  (:use code.test)
  (:require [std.lang.base.book-entry :as entry]
            [std.lang.base.emit-prep-js-test :as prep-js]
            [std.lang.base.emit-prep-lua-test :as prep]
            [std.lang.base.impl-entry :as impl-entry]
            [std.lang.base.library :as lib]
            [std.lang.base.library-snapshot :as snap]
            [std.lang.base.preprocess-resolve :refer :all]))

^{:refer std.lang.base.preprocess-resolve/get-fragment :added "4.1"}
(fact "gets the fragment given a symbol and modules"
  (get-fragment 'L.core/add
                (:modules prep/+book-min+)
                {:module {:id 'L.util
                          :link '{u L.core}}})
  => entry/book-entry?)

^{:refer std.lang.base.preprocess-resolve/process-namespaced-resolve :added "4.1"}
(fact "resolves symbol in current namespace"
  (process-namespaced-resolve 'u/add
                              (:modules prep/+book-min+)
                              {:module {:id 'L.util
                                        :link '{u L.core}}})
  => '[L.core add L.core/add]

  (process-namespaced-resolve 'u/UNKNOWN
                              (:modules prep/+book-min+)
                              {:module {:id 'L.util
                                        :link '{u L.core}}})
  => '[L.core UNKNOWN L.core/UNKNOWN]

  (process-namespaced-resolve 'other/function
                              (:modules prep/+book-min+)
                              {:module {:id 'L.util
                                        :link '{u L.core}}})
  => (throws))

^{:refer std.lang.base.preprocess-resolve/process-namespaced-symbol :added "4.1"}
(fact "process namespaced symbols"
  (process-namespaced-symbol 'u/add
                             (:modules prep/+book-min+)
                             {:module {:id 'L.util
                                       :link '{u L.core}}}
                             (volatile! #{})
                             (volatile! #{})
                             identity)
  => '(fn [x y] (return (+ x y)))

  (process-namespaced-symbol 'u/sub
                             (:modules prep/+book-min+)
                             {:module {:id 'L.util
                                       :link '{u L.core}}}
                             (volatile! #{})
                             (volatile! #{})
                             identity)
  => '(fn [x y] (return (- x y)))

  (process-namespaced-symbol 'u/identity-fn
                             (:modules prep/+book-min+)
                             {:module {:id 'L.util
                                       :link '{u L.core}}}
                             (volatile! #{})
                             (volatile! #{})
                             identity)
  => 'L.core/identity-fn

  (process-namespaced-symbol '-/hello
                             (:modules prep/+book-min+)
                             {:entry {:id 'hello}
                              :module {:id 'L.util
                                       :link '{u L.core
                                               - L.util}}}
                             (volatile! #{})
                             (volatile! #{})
                             identity)
  => 'L.util/hello

  (process-namespaced-symbol 'u/UNKNOWN
                             (:modules prep/+book-min+)
                             {:module {:id 'L.util
                                       :link '{u L.core}}}
                             (volatile! #{})
                             (volatile! #{})
                             identity)
  => (throws))

^{:refer std.lang.base.preprocess-resolve/process-standard-symbol :added "4.1"}
(fact "processes a standard symbol"
  (let [library-js (doto (lib/library:create
                          {:snapshot (snap/snapshot {:js {:id :js
                                                          :book prep-js/+book-min+}})})
                     (lib/install-module! :js 'JS.ui
                                          {:import '[["@measured/puck" :as [* Puck]]
                                                     ["@radix-ui/themes" :as [* Radix]
                                                      :bundle [["@radix-ui/themes/styles.css"]]]]}))
        deps-native (volatile! {})
        sym (process-standard-symbol 'Puck.Puck
                                     {:module (lib/get-module library-js
                                                              :js
                                                              'JS.ui)}
                                     deps-native)]
    [sym @deps-native])
  => '[Puck.Puck {"@measured/puck" #{Puck}}])

^{:refer std.lang.base.preprocess-resolve/find-natives :added "4.1"}
(fact "find natives for a macro entry"
  (let [library-js (doto (lib/library:create
                          {:snapshot (snap/snapshot {:js {:id :js
                                                          :book prep-js/+book-min+}})})
                     (lib/install-module! :js 'JS.ui
                                          {:import '[["react" :as React]]})
                     (lib/add-entry-single!
                      (impl-entry/create-macro
                       '(defmacro hello
                          [s]
                          (list 'React.useEffect s))
                       {:lang :js
                        :namespace 'JS.ui
                        :module 'JS.ui})))]
    (find-natives
     (lib/get-entry library-js
                    {:lang :js
                     :module 'JS.ui
                     :id 'hello
                     :section :fragment})
     {:module (lib/get-module library-js
                              :js
                              'JS.ui)}))
  => '{"react" #{React}})
