(ns std.lang.base.impl-deps-imports-test
  (:use code.test)
  (:require [std.lang.base.impl-deps :as deps]
            [std.lang.base.impl-deps-imports :as deps-imports]
            [std.lang.base.emit-prep-lua-test :as prep-lua]
            [std.lang.base.emit-prep-js-test :as prep-js]
            [std.lang.model.spec-lua :as lua]
            [std.lang.base.library :as lib]
            [std.lang.base.book :as b]
            [std.lang.base.library-snapshot :as snap]
            [std.lang.base.impl-entry :as entry]))

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


^{:refer std.lang.base.impl-deps-imports/collect-entry-imports :added "4.0"}
(fact "gets all fragment imports from code entries"
  ^:hidden
  
  (deps-imports/collect-entry-imports
   (map #(b/get-fragment-entry (lib/get-book +library-js+ :js) %) 
        (deps-imports/collect-script-fragment-deps
         (lib/get-book +library-js+ :js)
         (first (deps/collect-script-entries
                 (lib/get-book +library-js+ :js)
                 ['JS.app/App])))))
  
  
  )


^{:refer std.lang.base.impl-deps-imports/collect-script-fragment-deps :added "4.0"}
(fact "gets all the fragment dependencies"
  ^:hidden
  
  (deps-imports/collect-script-fragment-deps
   (lib/get-book +library-js+ :js)
   (first (deps/collect-script-entries
           (lib/get-book +library-js+ :js)
           ['JS.app/App])))
  => '#{JS.ui/Puck JS.app/Button JS.ui/Button})

^{:refer std.lang.base.impl-deps-imports/collect-script-import-deps :added "4.0"}
(fact "collect all native imports"
  ^:hidden
  
  (deps-imports/collect-script-import-deps
   (lib/get-book +library-js+ :js)
   (first (deps/collect-script-entries
           (lib/get-book +library-js+ :js)
           ['JS.app/App])))
  => '[{}
       {JS.ui/Puck {"@measured/puck" #{Puck}},
        JS.ui/Button {"@radix-ui/themes" #{Radix}}}
       #{JS.ui/Puck JS.app/Button JS.ui/Button}]

  (dissoc (lib/get-module +library-js+ :js 'JS.app)
          :fragment :code)
  => '{:require-impl nil,
       :static nil,
      :native-lu {},
      :internal {JS.ui ui, JS.app -},
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
       :display :default})

^{:refer std.lang.base.impl-deps-imports/build-script-import-ns :added "4.0"}
(fact "merges imports for both fragment and code entries"
  ^:hidden
  
  (deps-imports/build-script-import-ns
   '{}
   '{JS.ui/Puck {"@measured/puck" #{Puck}},
     JS.ui/Button {"@radix-ui/themes" #{Radix}}}
   )
  => '{JS.ui {"@measured/puck" #{Puck}, "@radix-ui/themes" #{Radix}}})

^{:refer std.lang.base.impl-deps-imports/build-script-imports :added "4.0"}
(fact "gets the ns imports for a script"
  ^:hidden
  
  (deps-imports/build-script-imports
   (lib/get-book +library-js+ :js)
   (first (deps/collect-script-entries
           (lib/get-book +library-js+ :js)
           ['JS.app/App])))
  => '{"@measured/puck"   {:as [* Puck]},
       "@radix-ui/themes" {:as [* Radix],
                           :bundle [["@radix-ui/themes/styles.css"]]}})
