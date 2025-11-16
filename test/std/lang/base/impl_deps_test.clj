(ns std.lang.base.impl-deps-test
  (:use code.test)
  (:require [std.lang.base.impl-deps :as deps]
            [std.lang.base.emit-prep-lua-test :as prep]
            [std.lang.model.spec-lua :as lua]
            [std.lang.base.library :as lib]
            [std.lang.base.library-snapshot :as snap]
            [std.lang.base.impl-entry :as entry]))

(def +library-ext+
  (doto (lib/library:create
         {:snapshot (snap/snapshot {:lua {:id :lua
                                          :book prep/+book-min+}})})
    (lib/install-module! :lua 'L.util
                         {:require '[[L.core :as u]]
                          :import  '[["cjson" :as cjson]]})
    (lib/add-entry-single!
     (entry/create-code-base
      '(defn sub-fn
         [a b]
         (return (fn:> ((u/identity-fn u/sub) a b))))
      {:lang :lua
       :namespace 'L.util
       :module 'L.util}
      {}))
    (lib/add-entry-single!
     (entry/create-code-base
      '(defn add-fn
         [a b]
         (return (fn:> ((u/identity-fn u/add) a (-/sub-fn b 0)))))
      {:lang :lua
       :namespace 'L.util
       :module 'L.util}
      {}))

    (lib/add-entry-single!
     (entry/create-code-base
      '(defn cjson-read
         [s]
         (return (cjson.read s)))
      {:lang :lua
       :namespace 'L.util
       :module 'L.util}
      {}))
    (lib/install-module! :lua 'L.app
                         {:require '[[L.core :as u]
                                     [L.util :as ut]]})
    (lib/add-entry-single!
     (entry/create-code-base
      '(defn app-read
         [s]
         (return (ut/cjson-read s)))
      {:lang :lua
       :namespace 'L.app
       :module 'L.app}
      {}))))

^{:refer std.lang.base.impl-deps/module-import-form :added "4.0"}
(fact "import form"
  ^:hidden
  
  (deps/module-import-form prep/+book-min+
                           'cjson
                           '{:as cjson}
                           {})
  => '(var* :local cjson := (require "cjson")))

^{:refer std.lang.base.impl-deps/module-export-form :added "4.0"}
(fact "export form"
  ^:hidden
  
  (deps/module-export-form prep/+book-min+
                           '{:as EXPORTED}
                           {})
  => '(return EXPORTED))

^{:refer std.lang.base.impl-deps/module-link-form :added "4.0"}
(fact "link form for projects"
  ^:hidden
  
  (deps/module-link-form prep/+book-min+
                         'kmi.common
                         {:root-ns 'kmi.hello})
  => "./common")

^{:refer std.lang.base.impl-deps/has-module-form :added "4.0"}
(fact "checks if module is available"
  ^:hidden
  
  (deps/has-module-form prep/+book-min+
                        'kmi.common)
  => nil)

^{:refer std.lang.base.impl-deps/setup-module-form :added "4.0"}
(fact "setup the module"
  ^:hidden
  
  (deps/setup-module-form prep/+book-min+
                          'kmi.common)
  => nil)

^{:refer std.lang.base.impl-deps/teardown-module-form :added "4.0"}
(fact "teardown the module"
  ^:hidden
  
  (deps/teardown-module-form prep/+book-min+
                             'kmi.common)
  => nil)

^{:refer std.lang.base.impl-deps/has-ptr-form :added "4.0"}
(fact "form to check if pointer exists"
  ^:hidden
  
  (deps/has-ptr-form prep/+book-min+
                     '{:id hello
                       :module kmi.common})
  => '(not= kmi.common/hello nil))

^{:refer std.lang.base.impl-deps/setup-ptr-form :added "4.0"}
(fact "form to setup pointer"
  ^:hidden
  
  (deps/setup-ptr-form prep/+book-min+
                       '{:id hello
                         :module kmi.common})
  => nil)

^{:refer std.lang.base.impl-deps/teardown-ptr-form :added "4.0"}
(fact "form to teardown pointer"
  ^:hidden
  
  (deps/teardown-ptr-form prep/+book-min+
                          '{:id hello
                            :module kmi.common})
  => '(:= kmi.common/hello nil))

^{:refer std.lang.base.impl-deps/collect-script-natives :added "4.0"}
(fact "gets native imported modules"
  ^:hidden
  
  (deps/collect-script-natives [{:native {'cjson "cjson"}}
                                {:native {'cjson "cjson"
                                          'lustache "lustache"}}]
                               {})
  => '{cjson "cjson", lustache "lustache"})

^{:refer std.lang.base.impl-deps/collect-script-entries :added "4.0"}
(fact "collects all entries"
  ^:hidden
  
  (deps/collect-script-entries (lib/get-book +library-ext+ :lua)
                               '[L.util/add-fn])
  => vector?)

^{:refer std.lang.base.impl-deps/collect-script :added "4.0"}
(fact "collect dependencies given a form and book"
  ^:hidden
  
  (-> (deps/collect-script (lib/get-book +library-ext+ :lua)
                           '(u/add (ut/sub-fn 1 2)
                                   (ut/add-fn 3 4))
                           {:module {:link '{ut L.util
                                             u  L.core}}})
      (deps/collect-script-summary))
  => '[(+ (L.util/sub-fn 1 2)
          (L.util/add-fn 3 4))
       (L.core/identity-fn L.util/add-fn L.util/sub-fn)
       {}]

  (-> (deps/collect-script (lib/get-book +library-ext+ :lua)
                           '(ut/cjson-read "hello")
                           {:module {:link '{ut L.util
                                             u  L.core}}})
      (deps/collect-script-summary))
  => '[(L.util/cjson-read "hello")
       (L.util/cjson-read)
       {"cjson" {:as cjson}}])

^{:refer std.lang.base.impl-deps/collect-script-summary :added "4.0"}
(fact "summaries the output of `collect-script`"
  ^:hidden
  
  (-> '[(+ (L.util/sub-fn 1 2) (L.util/add-fn 3 4))
        ()
        {"cjson" {:as cjson}}]
      (deps/collect-script-summary))
  => '[(+ (L.util/sub-fn 1 2)
          (L.util/add-fn 3 4))
       ()
       {"cjson" {:as cjson}}])

^{:refer std.lang.base.impl-deps/collect-module-check-options :added "4.0"}
(fact "does the checks for the inputs "
  ^:hidden
  
  (deps/collect-module-check-options
   {:type :graph
    :root-ns 'L.script
    :path-suffix ""})
  => nil

  (deps/collect-module-check-options
   {:type :graph
    :path-suffix ""})
  => (throws)
  
  (deps/collect-module-check-options
   {:type :directory
    :root-ns 'L
    :root-libs "libs"
    :root-prefix "@"
    :path-suffix ".lua"})
  => nil

  (deps/collect-module-check-options
   {:type :directory
    :root-prefix "@"
    :path-suffix ".lua"})
  => (throws)

  (deps/collect-module-check-options
   {:type :custom
    :fn-link-form (fn [_ _ _])
    :root-ns 'L
    :params {}})
  => nil

  (deps/collect-module-check-options
   {:type :custom
    :fn-link-form (fn [_ _ _])
    :params {}})
  => (throws))

^{:refer std.lang.base.impl-deps/collect-module-ns-select :added "4.0"}
(fact "TODO")

^{:refer std.lang.base.impl-deps/collect-module-directory-form :added "4.0"}
(fact "collects forms for "
  ^:hidden

  (deps/collect-module-directory-form
   nil
   'kmi.common
   {:root-ns 'L
    :root-libs "libs"
    :root-prefix "@"
    :path-suffix ".lua"})
  => "@/libs/kmi/common.lua"

  (deps/collect-module-directory-form
   nil
   'L.script
   {:root-ns 'L
    :root-libs "libs"
    :root-prefix "@"
    :path-suffix ".lua"})
  => "@/script.lua"

  (deps/collect-module-directory-form
   nil
   'L.script
   {:root-ns 'L
    :root-libs "libs"
    :root-prefix "."
    :path-suffix ""})
  => "./script")

^{:refer std.lang.base.impl-deps/collect-module :added "4.0"}
(fact "collects information for the entire module"
  ^:hidden
  
  (-> (deps/collect-module (lib/get-book +library-ext+ :lua)
                           (lib/get-module +library-ext+ :lua 'L.util)
                           {:type :graph
                            :root-ns 'L.script
                            :path-suffix ""})
      (update :code (fn [arr]
                      (set (map :id arr)))))
  => '{:setup nil, :teardown nil,
       :code #{cjson-read add-fn sub-fn},
       :native {"cjson" {:as cjson}},
       :link (["./core" {:as u, :ns L.core}])}
  

  (-> (deps/collect-module (lib/get-book +library-ext+ :lua)
                           (lib/get-module +library-ext+ :lua 'L.util)
                           {:type :directory
                            :root-ns 'L
                            :root-libs   "libs"
                            :root-prefix "@"
                            :path-suffix ".lua"})
        (update :code (fn [arr]
                        (set (map :id arr)))))
  => '{:setup nil, :teardown nil,
       :code #{cjson-read add-fn sub-fn},
       :native {"cjson" {:as cjson}},
       :link (["@/core.lua" {:as u, :ns L.core}])}

  (-> (deps/collect-module (lib/get-book +library-ext+ :lua)
                           (lib/get-module +library-ext+ :lua 'L.app)
                           {:type :graph
                            :root-ns 'L.app
                            :path-suffix ""})
      (update :code (fn [arr]
                      (set (map :id arr)))))
  => '{:setup nil, :teardown nil,
       :code #{app-read},
       :native {},
       :link (["./util" {:as ut, :ns L.util}])})




(comment
  (./import))
