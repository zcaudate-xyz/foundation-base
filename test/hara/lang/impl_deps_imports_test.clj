(ns hara.lang.impl-deps-imports-test
  (:require [xt.lang.common-promise]
             [xt.lang.common-data]
             [xt.lang.common-lib]
             [js.blessed]
             [js.react]
             [hara.lang.book :as b]
             [hara.lang.book-module :as module]
            [hara.common.emit-prep-js-test :as prep-js]
            [hara.common.emit-prep-lua-test :as prep-lua]
            [hara.lang.impl :as impl]
            [hara.lang.impl-deps :as deps]
             [hara.lang.impl-deps-imports :as deps-imports]
             [hara.lang.impl-entry :as entry]
             [hara.lang.library :as lib]
             [hara.lang.library-snapshot :as snap]
             [hara.model.spec-lua :as lua])
  (:use code.test))

(def +library-js-cloned+
  (let [lib (impl/clone-default-library)]
    (impl/with:library [lib]
      (require '[xt.lang.common-data] :reload)
      (require '[xt.lang.common-lib] :reload)
      (require '[js.react] :reload)
      (require '[js.blessed] :reload)
      (require '[js.blessed.ui-core] :reload)
      (require '[js.blessed.frame-status] :reload)
      (require '[js.blessed.frame-console] :reload)
       (require '[xt.lang.common-lib] :reload))
    lib))

(def +library-python-polyfill+
  (let [lib (impl/clone-default-library)]
    (impl/with:library [lib]
      (require '[xt.lang.common-promise] :reload)
      (lib/add-module! lib
                       (module/book-module
                        {:lang :python
                         :id 'demo.promise
                         :code '{demo-wrap {:deps #{}
                                            :xtalk-ops #{:x-promise}}}})))
    lib))

(def +library-lua+
  (doto (lib/library:create
         {:snapshot (snap/snapshot {:lua {:id :lua
                                          :book prep-lua/+book-min+}})})
    (lib/install-module! :lua 'L.util
                         {:import  '[["cjson" :as cjson]
                                     ["socket" :as socket]]})
    (lib/add-entry-single!
     (entry/create-fragment
      '(def$ cjson-read cjson.read)
      {:lang :lua
       :namespace 'L.util
       :module 'L.util}))
    (lib/add-entry-single!
     (entry/create-fragment
      '(def$ socket-read socket.read)
      {:lang :lua
       :namespace 'L.util
       :module 'L.util}))
    (lib/install-module! :lua 'L.app
                         {:require '[[L.util :as ut]]})
    (lib/add-entry-single!
     (entry/create-code-base
      '(defn read-json
         [a]
         (return (ut/cjson-read "{\"a\": 1}")))
      {:lang :lua
       :namespace 'L.app
       :module 'L.app}
      {}))
    (lib/add-entry-single!
     (entry/create-code-base
      '(defn read-socket
         [a]
         (return (ut/socket-read)))
      {:lang :lua
       :namespace 'L.app
       :module 'L.app}
      {}))))

(def +library-js+
  (doto (lib/library:create
         {:snapshot (snap/snapshot {:js {:id :js
                                          :book prep-js/+book-min+}})})
    (lib/install-module! :js 'JS.ui
                         {:import  ' [["@measured/puck" :as [* Puck]]
                                      ["@radix-ui/themes" :as [* Radix]
                                       :bundle [["@radix-ui/themes/styles.css"]]]]})
    (lib/add-entry-single!
     (entry/create-fragment
      '(def$ Button Radix.Button)
      {:lang :js
       :namespace 'JS.ui
       :module 'JS.ui}))
    (lib/add-entry-single!
     (entry/create-fragment
      '(def$ Puck Puck)
      {:lang :js
       :namespace 'JS.ui
       :module 'JS.ui}))
    (lib/install-module! :js 'JS.app
                         {:require '[[JS.ui :as ui]]})
    (lib/add-entry-single!
     (entry/create-fragment
      '(def$ Button ui/Button)
      {:lang :js
       :namespace 'JS.app
       :module 'JS.app}))
    (lib/add-entry-single!
     (entry/create-code-base
      '(defn App
         []
         (return
          [:% -/Button
           [:% ui/Puck]]))
      {:lang :js
       :namespace 'JS.app
       :module 'JS.app}
      {}))
    (lib/add-entry-single!
     (entry/create-code-base
      '(defn AppRadix
         []
         (return
          [:% ui/Button]))
      {:lang :js
       :namespace 'JS.app
       :module 'JS.app}
      {}))
    ))

^{:refer hara.lang.impl-deps-imports/get-entry-imports :added "4.0"}
(fact "gets all fragment imports from code entries"

  (deps-imports/get-entry-imports
   (map #(b/get-fragment-entry (lib/get-book +library-js+ :js) %)
        (deps-imports/get-fragment-deps
         (lib/get-book +library-js+ :js)
         (first (deps/collect-script-entries
                 (lib/get-book +library-js+ :js)
                 ['JS.app/App])))))
  => '{JS.ui/Puck {"@measured/puck" #{Puck}}, JS.ui/Button {"@radix-ui/themes" #{Radix}}})

^{:refer hara.lang.impl-deps-imports/get-namespace-imports :added "4.0"}
(fact "merges imports for both fragment and code entries"

  (deps-imports/get-namespace-imports
   (concat
    '{JS.ui/Puck {"@measured/puck" #{Puck}}}
    '{JS.ui/Button {"@radix-ui/themes" #{Radix}}}))
  => '{JS.ui {"@measured/puck" #{Puck}, "@radix-ui/themes" #{Radix}}})

^{:refer hara.lang.impl-deps-imports/get-fragment-deps :added "4.0"}
(fact "gets all the fragment dependencies"

  (deps-imports/get-fragment-deps
   (lib/get-book +library-js+ :js)
   (first (deps/collect-script-entries
           (lib/get-book +library-js+ :js)
           ['JS.app/App])))
  => '#{JS.ui/Puck JS.app/Button JS.ui/Button})

^{:refer hara.lang.impl-deps-imports/format-namespace-imports :added "4.0"}
(fact "formats a list of namespace imports into an import map"

  (deps-imports/format-namespace-imports
   (lib/get-book +library-js+ :js)
   (deps-imports/get-namespace-imports
    (concat
     '{JS.ui/Puck {"@measured/puck" #{Puck}}}
     '{JS.ui/Button {"@radix-ui/themes" #{Radix}}})))
  => '{"@measured/puck" {:as [* Puck]},
       "@radix-ui/themes" {:as [* Radix],
                           :bundle {"@radix-ui/themes/styles.css" {}}}})

^{:refer hara.lang.impl-deps-imports/script-import-deps :added "4.0"}
(fact "collect all native imports"

  (deps-imports/script-import-deps
   (lib/get-book +library-js+ :js)
   (first (deps/collect-script-entries
           (lib/get-book +library-js+ :js)
           ['JS.app/App])))
  => '{JS.ui {"@measured/puck" #{Puck}, "@radix-ui/themes" #{Radix}}}

  (dissoc (lib/get-module +library-js+ :js 'JS.app)
          :fragment :code)
  => '{:require-impl nil,
        :static nil,
        :specialize {},
        :includes #{},
        :native-lu {},
        :internal {JS.ui ui, JS.app -},
        :implements [],
        :lang :js,
        :alias {},
        :native {},
        :link {ui JS.ui, - JS.app},
       :id JS.app,
       :display :default}

  (dissoc (lib/get-module +library-js+ :js 'JS.ui)
          :fragment :code)
  => '{:require-impl nil,
        :static nil,
        :specialize {},
        :includes #{},
        :native-lu {Puck "@measured/puck",
                    Radix "@radix-ui/themes"},
        :internal {JS.ui -},
        :implements [],
        :lang :js,
        :alias {Puck Puck, Radix Radix},
        :native
       {"@measured/puck" {:as [* Puck]},
        "@radix-ui/themes"
        {:as [* Radix], :bundle {"@radix-ui/themes/styles.css" {}}}},
       :link {- JS.ui},
       :id JS.ui,
       :display :default})

^{:refer hara.lang.impl-deps-imports/script-imports :added "4.0"}
(fact "gets the ns imports for a script"

  (deps-imports/script-imports
   (lib/get-book +library-js+ :js)
   (first (deps/collect-script-entries
           (lib/get-book +library-js+ :js)
           ['JS.app/App])))
  => '{"@measured/puck"   {:as [* Puck]},
       "@radix-ui/themes" {:as [* Radix],
                           :bundle {"@radix-ui/themes/styles.css" {}}}})

^{:refer hara.lang.impl-deps-imports/module-imports :added "4.0"}
(fact "gets a modules imports as well as code links"

  (deps-imports/module-imports
   (lib/get-book +library-js+ :js)
   'JS.app)
  => '{:native {"@measured/puck" {:as [* Puck]},
                "@radix-ui/themes" {:as [* Radix], :bundle {"@radix-ui/themes/styles.css" {}}}},
       :direct #{}}

  (impl/with:library [+library-js-cloned+]
    (deps-imports/module-imports
     (lib/get-book
       +library-js-cloned+
       :js)
     'js.react))
  => '{:native {"react" {:as React}, "react-dom/client" {:as ReactDOM}},
        :direct #{xt.lang.common-data
                  xt.lang.common-lib
                  xt.lang.common-math
                  xt.lang.common-string
                  xt.lang.common-tree}}

  (impl/with:library [+library-js-cloned+]
    (deps-imports/module-imports
     (lib/get-book
      +library-js-cloned+
      :js)
     'js.blessed))
  => '{:native {"react-blessed" {:as ReactBlessed},
                "blessed" {:as Blessed}},
       :direct #{}}

  (impl/with:library [+library-js-cloned+]
    (deps-imports/module-imports
     (lib/get-book
       +library-js-cloned+
       :js)
     'js.blessed.ui-core))
  => '{:native {"react" {:as React},
                "blessed" {:as Blessed}},
       :direct #{js.blessed.ui-style
                 js.react
                 xt.lang.common-data
                 xt.lang.common-lib}}

  (impl/with:library [+library-js-cloned+]
    (deps-imports/module-imports
     (lib/get-book
      +library-js-cloned+
      :js)
     'js.blessed.frame-status))
  => '{:native {"react" {:as React}}, :direct #{}}

  (impl/with:library [+library-js-cloned+]
    (deps-imports/module-imports
     (lib/get-book
      +library-js-cloned+
      :js)
     'js.blessed.frame-status))
  => '{:native {"react" {:as React}}, :direct #{}}

  (impl/with:library [+library-js-cloned+]
    (deps-imports/module-imports
     (lib/get-book
       +library-js-cloned+
       :js)
     'js.blessed.frame-console))
  => '{:native {}
       :direct #{js.blessed.ui-core
                 js.blessed.ui-group
                 xt.lang.common-data
                 xt.lang.common-lib
                 xt.lang.common-sort-by}})


^{:refer hara.lang.impl-deps-imports/module-code-deps :added "4.0"}
(fact "gets the code dependencies for the module"

  (impl/with:library [+library-js-cloned+]
    (deps-imports/module-code-deps
     (lib/get-book
        +library-js-cloned+
        :js)
     '[js.blessed.frame-console]))
   => '{:all #{js.blessed.ui-style
                js.blessed.ui-group
                js.blessed.frame-console
                xt.lang.common-sort-by
                xt.lang.common-lib
                xt.lang.common-data
                xt.lang.common-math
                xt.lang.common-string
                xt.lang.common-tree
                js.react
                js.blessed.ui-core}
        :graph {js.blessed.frame-console  #{js.blessed.ui-core
                                            js.blessed.ui-group
                                           xt.lang.common-data
                                           xt.lang.common-lib
                                           xt.lang.common-sort-by}
               js.blessed.ui-group       #{js.blessed.ui-style
                                           js.react
                                           xt.lang.common-data
                                           xt.lang.common-lib
                                           }
                xt.lang.common-sort-by    #{xt.lang.common-data}
                xt.lang.common-lib        #{}
                 xt.lang.common-data       #{}
                 xt.lang.common-math       #{}
                 xt.lang.common-string     #{}
                 xt.lang.common-tree       #{xt.lang.common-data
                                             xt.lang.common-lib}
                js.blessed.ui-core        #{js.blessed.ui-style
                                            js.react
                                            xt.lang.common-data
                                            xt.lang.common-lib}
                js.blessed.ui-style       #{xt.lang.common-data}
                js.react                  #{xt.lang.common-data
                                            xt.lang.common-lib
                                            xt.lang.common-math
                                            xt.lang.common-string
                                            xt.lang.common-tree}}}

  (impl/with:library [+library-js-cloned+]
    (deps-imports/module-code-deps
     (lib/get-book
       +library-js-cloned+
       :js)
     '[js.react]))
  => '{:all #{js.react
               xt.lang.common-data
               xt.lang.common-lib
               xt.lang.common-math
               xt.lang.common-string
                xt.lang.common-tree},
         :graph {js.react #{xt.lang.common-data
                            xt.lang.common-lib
                            xt.lang.common-math
                            xt.lang.common-string
                            xt.lang.common-tree},
                   xt.lang.common-lib #{},
                   xt.lang.common-data #{}
                   xt.lang.common-math #{}
                   xt.lang.common-string #{}
                   xt.lang.common-tree #{xt.lang.common-data
                                        xt.lang.common-lib}}}

  (impl/with:library [+library-python-polyfill+]
    (deps-imports/module-code-deps
     (lib/get-book
      +library-python-polyfill+
      :python)
     '[demo.promise]))
  => '{:all #{demo.promise
              xt.lang.common-promise}
       :graph {demo.promise #{xt.lang.common-promise}
               xt.lang.common-promise #{}}})

(comment

  (dissoc
   (hara.lang/get-module
    +library-js+
    :js
    'JS.app)
   :code :fragment
   )

  '{:require-impl nil,
    :static nil,
    :native-lu {Puck "@measured/puck", Radix "@radix-ui/themes"},
    :internal {JS.ui -},
    :lang :js,
    :alias {Puck Puck, Radix Radix},
    :native
    {"@measured/puck" {:as [* Puck]},
     "@radix-ui/themes"
     {:as [* Radix], :bundle [["@radix-ui/themes/styles.css"]]}},
    :link {- JS.ui},
    :id JS.ui,
    :display :default}

  (get-namespace-imports
   '{}
   '{JS.ui/Puck {"@measured/puck" #{Puck}},
     JS.ui/Button {"@radix-ui/themes" #{Radix}}}
   )
  {JS.ui {"@measured/puck" #{Puck}, "@radix-ui/themes" #{Radix}}})

(comment


(lib/get-entry +library-js+
               '{:lang :js
                 :id Puck
                 :module JS.ui
                 :section :fragment})

(lib/get-entry +library-js+
               '{:lang :js
                 :id Button
                 :module JS.app
                 :section :fragment})
  )
