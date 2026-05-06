(ns hara.lang.impl-deps-test
  (:require [hara.common.emit-prep-lua-test :as prep]
            [hara.lang.impl :as impl]
            [hara.lang.impl-deps :as deps]
            [hara.lang.impl-entry :as entry]
            [hara.lang.library :as lib]
            [hara.lang.library-snapshot :as snap]
            [hara.model.spec-lua :as lua]
            [xt.lang.common-data])
  (:use code.test))

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

^{:refer hara.lang.impl-deps/module-import-form :added "4.0"}
(fact "import form"

  (deps/module-import-form prep/+book-min+
                           'cjson
                           '{:as cjson}
                           {})
  => '(var* :local cjson := (require "cjson")))

^{:refer hara.lang.impl-deps/module-export-form :added "4.0"}
(fact "export form"

  (deps/module-export-form prep/+book-min+
                           {}
                           {})
  => '(return (tab)))

^{:refer hara.lang.impl-deps/module-link-form :added "4.0"}
(fact "link form for projects"

  (deps/module-link-form prep/+book-min+
                         'kmi.common
                         {:root-ns 'kmi.hello})
  => "./common")

^{:refer hara.lang.impl-deps/has-module-form :added "4.0"}
(fact "checks if module is available"

  (deps/has-module-form prep/+book-min+
                        'kmi.common)
  => nil)

^{:refer hara.lang.impl-deps/setup-module-form :added "4.0"}
(fact "setup the module"

  (deps/setup-module-form prep/+book-min+
                          'kmi.common)
  => nil)

^{:refer hara.lang.impl-deps/teardown-module-form :added "4.0"}
(fact "teardown the module"

  (deps/teardown-module-form prep/+book-min+
                             'kmi.common)
  => nil)

^{:refer hara.lang.impl-deps/has-ptr-form :added "4.0"}
(fact "form to check if pointer exists"

  (deps/has-ptr-form prep/+book-min+
                     '{:id hello
                       :module kmi.common})
  => '(not= kmi.common/hello nil))

^{:refer hara.lang.impl-deps/setup-ptr-form :added "4.0"}
(fact "form to setup pointer"

  (deps/setup-ptr-form prep/+book-min+
                       '{:id hello
                         :module kmi.common})
  => nil)

^{:refer hara.lang.impl-deps/teardown-ptr-form :added "4.0"}
(fact "form to teardown pointer"

  (deps/teardown-ptr-form prep/+book-min+
                          '{:id hello
                            :module kmi.common})
  => '(:= kmi.common/hello nil))

^{:refer hara.lang.impl-deps/collect-script-natives :added "4.0"}
(fact "gets native imported modules"

  (deps/collect-script-natives [{:native {'cjson "cjson"}}
                                {:native {'cjson "cjson"
                                          'lustache "lustache"}}]
                               {})
  => '{cjson "cjson", lustache "lustache"})

^{:refer hara.lang.impl-deps/collect-script-entries :added "4.0"}
(fact "collects all entries"

  (deps/collect-script-entries (lib/get-book +library-ext+ :lua)
                               '[L.util/add-fn])
  => vector?)

^{:refer hara.lang.impl-deps/collect-script :added "4.0"}
(fact "collect dependencies given a form and book"

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
       {"cjson" {:as cjson}}]

  (let [library (impl/clone-default-library)]
    (-> (deps/collect-script (lib/get-book library :lua)
                             '(x:obj-keys {:a 1})
                             {:module {:id 'L.user
                                       :link '{- L.user}}})
        (deps/collect-script-summary)))
  => '[(xt.lang.common-data/obj-keys {:a 1})
       (xt.lang.common-data/obj-keys)
       {}])

^{:refer hara.lang.impl-deps/collect-script-summary :added "4.0"}
(fact "summaries the output of `collect-script`"

  (-> '[(+ (L.util/sub-fn 1 2) (L.util/add-fn 3 4))
        ()
        {"cjson" {:as cjson}}]
      (deps/collect-script-summary))
  => '[(+ (L.util/sub-fn 1 2)
          (L.util/add-fn 3 4))
       ()
       {"cjson" {:as cjson}}])


^{:refer hara.lang.impl-deps/collect-module :added "4.0"}
(fact "collects information for the entire module"

  (-> (deps/collect-module (lib/get-book +library-ext+ :lua)
                           (lib/get-module +library-ext+ :lua 'L.util))
      (update :code (fn [arr]
                      (set (map :id arr)))))
  => '{:setup nil, :teardown nil, :code #{cjson-read add-fn sub-fn}, :native {"cjson" {:as cjson}}, :direct #{L.core}}


  (-> (deps/collect-module (lib/get-book +library-ext+ :lua)
                           (lib/get-module +library-ext+ :lua 'L.util))
      (update :code (fn [arr]
                        (set (map :id arr)))))
  => '{:setup nil, :teardown nil, :code #{cjson-read add-fn sub-fn}, :native {"cjson" {:as cjson}}, :direct #{L.core}}

  (-> (deps/collect-module (lib/get-book +library-ext+ :lua)
                           (lib/get-module +library-ext+ :lua 'L.app))
      (update :code (fn [arr]
                      (set (map :id arr)))))
  => '{:setup nil, :teardown nil, :code #{app-read}, :native {}, :direct #{L.util}})




(comment
  (./import))
