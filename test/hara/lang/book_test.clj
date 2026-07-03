(ns hara.lang.book-test
  (:require [hara.lang.book :as b]
  	        [hara.lang.book-entry :as entry]
            [hara.lang.impl-entry :as impl-entry]
            [hara.lang.book-meta :as meta]
            [hara.lang.book-module :as module]
            [hara.common.emit-common :as common]
            [hara.common.emit-helper :as helper]
            [hara.common.grammar :as grammar]
            [hara.common.util :as ut]
            [std.lib.template :as template])
  (:use code.test))

(def +book+
  (b/book {:lang :lua
           :meta (meta/book-meta {:module-export  (fn [{:keys [as]} opts]
                                                    (template/$ (return ~as)))
                                  :module-import  (fn [name {:keys [as]} opts]
                                                    (template/$ (var ~as := (require ~(str name)))))
                                  :has-ptr        (fn [ptr]
                                                    (list 'not= (ut/sym-full ptr) nil))
                                  :teardown-ptr   (fn [ptr]
                                                    (list := (ut/sym-full ptr) nil))})
           :grammar (grammar/grammar :lua
                      (grammar/build)
                      helper/+default+)}))

(def +module+
  (module/book-module
   {:id      'L.core
    :lang    :lua
    :link    '{- L.core}
    :alias '{cr coroutine
             t  table}}))

(def +fn-entry+
  (entry/book-entry {:lang :lua
                     :id 'identity-fn
                     :module 'L.core
                     :section :code
                     :form '(defn identity-fn [x] x)
                     :form-input '(defn identity-fn [x] x)
                     :deps #{}
                     :namespace 'L.core
                     :declared false}))

(def +macro-entry+
  (entry/book-entry {:lang :lua
                     :id 'identity
                     :module 'L.core
                     :section :fragment
                     :form '(defn identity [x] x)
                     :form-input '(defn identity [x] x)
                     :template (fn [x] x)
                     :standalone '(fn [x] (return x))
                     :deps #{}
                     :namespace 'L.core
                     :declared false}))

(def +sample+
  (-> +book+
      (b/set-module +module+)
      second
      (b/set-entry +fn-entry+)
      second
      (b/set-entry +macro-entry+)
      second))

^{:refer hara.lang.book/get-base-entry :added "4.0"}
(fact "gets an entry in the book"

  (b/get-base-entry +sample+ 'L.core 'identity :fragment)
  => +macro-entry+

  (b/get-base-entry +sample+ 'L.core 'identity-fn :code)
  => +fn-entry+)

^{:refer hara.lang.book/get-code-entry :added "4.0"}
(fact "gets a code entry in the book"

  (b/get-code-entry +sample+ 'L.core/identity-fn)
  => +fn-entry+)

^{:refer hara.lang.book/get-fragment-entry :added "4.0"}
(fact "gets a fragment entry in the book"

  (b/get-fragment-entry +sample+ 'L.core/identity)
  => +macro-entry+)

^{:refer hara.lang.book/get-entry :added "4.0"}
(fact "gets either the module or code entry"

  (b/get-entry +sample+ 'L.core)
  => module/book-module?

  (b/get-entry +sample+ 'L.core/identity-fn)
  => entry/book-entry?)

^{:refer hara.lang.book/get-module :added "4.0"}
(fact "gets the module"

  (b/get-module +sample+ 'L.core)
  => module/book-module?)

^{:refer hara.lang.book/get-code-deps :added "4.0"}
(fact "gets dependencies for a materialized code entry"
  (-> (b/set-entry +sample+
                   (impl-entry/create-code
                    '(defn inc-fn [x]
                       (return (L.core/identity-fn x)))
                    {:lang :lua
                     :namespace 'L.core
                     :module 'L.core}
                    +sample+))
      second
      (b/get-code-deps 'L.core/inc-fn))
  => '#{L.core/identity-fn})

^{:refer hara.lang.book/get-code-entry-view :added "4.1"}
(fact "gets a code entry materialized for the current book language"
  (b/get-code-entry-view +sample+ 'L.core/identity-fn)
  => (contains '{:id identity-fn
                 :module L.core
                 :lang :lua
                 :section :code
                 :deps #{}})

  (b/get-code-entry-view +sample+ 'L.core/missing)
  => (throws))

^{:refer hara.lang.book/get-deps :added "4.0"}
(fact "get dependencies for a given id"

  (b/get-deps +sample+ 'L.core)
  => #{}

  (b/get-deps +sample+ 'L.core/identity-fn)
  => #{})

^{:refer hara.lang.book/get-deps-native :added "4.0"}
(fact "gets the imports for a book"

  (b/get-deps-native
   (-> +book+
       (b/set-module
        (module/book-module
         {:id      'L.json
          :lang    :lua
          :link    '{- L.json}
          :native {"cjson" 'cjson}}))
       second
       (b/set-entry
        (entry/book-entry
         {:lang :lua
          :id 'read
          :module 'L.json
          :section :code
          :form '(defn read [s] (cjson.read s))
          :form-input '(defn read [s] (cjson.read s))
          :deps #{}
          :deps-native #{"cjson"}
          :namespace 'L.json
          :declared false}))
       second)
   'L.json/read)
  => #{"cjson"})

^{:refer hara.lang.book/list-entries :added "4.0"}
(fact "lists entries for a given symbol"

  (b/list-entries +sample+ :module)
  => '(L.core)

  (b/list-entries +sample+ :code)
  => '(L.core/identity-fn))

^{:refer hara.lang.book/book-string :added "4.0"}
(fact "shows the book string"

  (b/book-string +sample+)
  => "#book [:lua] {L.core {:code 1, :fragment 1}}")

^{:refer hara.lang.book/book? :added "4.0"}
(fact "checks that object is a book"

  (b/book? +sample+)
  => true)

^{:refer hara.lang.book/book :added "4.0"}
(fact "creates a book"

  (b/book {:lang :lua.redis
           :meta    (:meta +book+)
           :grammar (:grammar +book+)
           :parent  :lua
           :merged  #{}})
  => b/book?)

^{:refer hara.lang.book/book-merge :added "4.0"}
(fact "merges a book with it's parent"

  (b/book-merge (b/book {:lang    :lua.redis
                         :meta    (:meta +book+)
                         :grammar (:grammar +book+)
                         :parent  :lua
                         :merged  #{}})
                +sample+)
  => b/book?)

^{:refer hara.lang.book/book-from :added "4.0"}
(fact "returns the merged book given snapshot"
  (let [snapshot {:lua  {:book +sample+}
                  :lua.redis {:book (b/book {:lang :lua.redis
                                        :meta    (:meta +book+)
                                        :grammar (:grammar +book+)
                                        :parent  :lua
                                        :merged  #{}})}}]
    (b/book-from snapshot :lua.redis))
  => b/book?)

^{:refer hara.lang.book/check-compatible-lang :added "4.0"
  :setup [(def +redis+
            (b/book-merge (b/book {:lang    :lua.redis
                                   :meta    (:meta +book+)
                                   :grammar (:grammar +book+)
                                   :parent  :lua
                                   :merged  #{}})
                          +sample+))]}
(fact "checks if the lang is compatible with the book"

  (b/check-compatible-lang +redis+ :lua)
  => true

  (b/check-compatible-lang +redis+ :lua.redis)
  => true

  (b/check-compatible-lang +redis+ :js)
  => false)

^{:refer hara.lang.book/assert-compatible-lang :added "4.0"}
(fact "asserts that the lang is compatible"

  (b/assert-compatible-lang +redis+ :lua)
  => true

  (b/assert-compatible-lang +redis+ :js)
  => (throws))

^{:refer hara.lang.book/set-module :added "4.0"}
(fact "adds an addional module to the book"

  (-> (b/set-module +redis+
                    (module/book-module
                     {:id      'L.util
                      :lang    :lua
                      :link    '{u L.core}}))
      second)
  => b/book?

  (b/set-module +redis+
                (module/book-module
                 {:id      'js.core
                  :lang    :js}))
  => (throws))

^{:refer hara.lang.book/put-module :added "4.0"}
(fact "adds or updates a module"

  (-> (b/put-module +redis+
                    (module/book-module
                     {:id      'L.core
                      :lang    :lua
                      :alias   '{s string}}))
      second
      (get-in [:modules
               'L.core
               :alias]))
  => '{cr coroutine, t table, s string})

^{:refer hara.lang.book/delete-module :added "4.0"}
(fact "deletes a module given a book"

  (-> +redis+
      (b/set-module  (module/book-module
                     {:id      'L.util
                      :lang    :lua
                      :link    '{u L.core}}))
      second
      (b/delete-module 'L.core)
      second
      (b/list-entries :module))
  => '(L.util))

^{:refer hara.lang.book/delete-modules :added "4.0"}
(fact "deletes all modules"

  (-> +redis+
      (b/set-module  (module/book-module
                     {:id      'L.util
                      :lang    :lua
                      :link    '{u L.core}}))
      second
      (b/delete-modules  '[L.core L.util])
      second
      (b/list-entries :module))
  => nil)

^{:refer hara.lang.book/has-module? :added "4.0"}
(fact "checks that a books has a given module"

  (b/has-module? +redis+ 'L.core)
  => true

  (b/has-module? +redis+ 'L.other)
  => false)

^{:refer hara.lang.book/assert-module :added "4.0"}
(fact "asserts that module exists"

  (b/assert-module +redis+ 'L.core)
  => true

  (b/assert-module +redis+ 'L.other)
  => (throws))

^{:refer hara.lang.book/set-entry :added "4.0"}
(fact "sets entry in the book"

  (-> (b/set-entry +redis+
                   (entry/book-entry {:lang :lua
                                      :id 'inc-fn
                                      :module 'L.core
                                      :section :code
                                      :form '(defn inc-fn [x] (return (+ 1 x)))
                                      :form-input '(defn inc-fn [x] (return (+ 1 x)))
                                      :deps #{}
                                      :namespace 'L.core
                                      :declared false}))
      second
      (b/list-entries))
  => '(L.core/identity-fn L.core/inc-fn))

^{:refer hara.lang.book/put-entry :added "4.0"}
(fact "updates entry value in the book"

  (-> (b/put-entry +redis+
                   (entry/book-entry {:lang :lua
                                      :id 'identity-fn
                                      :module 'L.core
                                      :section :code
                                      :form '()}))
      second
      (b/get-entry 'L.core/identity-fn)
      (->> (into {})))
  => (contains '{:form-input (defn identity-fn [x] x),
                 :section :code,
                 :module L.core,
                 :lang :lua,
                 :id identity-fn,
                 :declared false,
                 :display :default,
                 :form (),
                 :namespace L.core,
                 :deps #{}}))

^{:refer hara.lang.book/has-entry? :added "4.0"}
(fact "checks that book has an entry"

  (b/has-entry? +redis+ 'L.core :fragment 'identity)
  => true)

^{:refer hara.lang.book/assert-entry :added "4.0"}
(fact "asserts that module exists"

  (b/assert-entry +redis+ 'L.core :fragment 'identity)
  => true

  (b/assert-entry +redis+ 'ERROR :fragment 'identity)
  => (throws)

  (b/assert-entry +redis+ 'L.core :fragment 'ERROR)
  => (throws))

^{:refer hara.lang.book/delete-entry :added "4.0"}
(fact "deletes an entry"

  (-> (b/delete-entry +redis+ 'L.core :fragment 'identity)
      second
      b/book-string)
  => "#book [:lua.redis] {L.core {:code 1, :fragment 0}}")

^{:refer hara.lang.book/module-create-filename :added "4.0"}
(fact "creates a filename for module"

  (b/module-create-filename +redis+ 'redis.core)
  => "core.lua")

^{:refer hara.lang.book/module-create-check :added "4.0"}
(fact "checks that bundles are available"

  (b/module-create-check +redis+ 'L.redis '{u L.core})
  => true

  (b/module-create-check +redis+ 'L.redis '{u NOT-FOUND})
  => (throws))

^{:refer hara.lang.book/module-create-requires :added "4.0"}
(fact "creates a map for the requires"
  (b/module-create-requires '[[L.core :as u]])
  => '{L.core {:as u, :id L.core}})

^{:refer hara.lang.book/module-normalize-implements :added "4.1"}
(fact "normalizes module contracts into a vector of symbols"
  (b/module-normalize-implements nil)
  => []

  (b/module-normalize-implements 'IProto)
  => '[IProto]

  (b/module-normalize-implements '[IProto IOther])
  => '[IProto IOther]

  (b/module-normalize-implements #{'IProto 'IOther})
  => '[IOther IProto]

  (b/module-normalize-implements "contract")
  => ["contract"])

^{:refer hara.lang.book/module-export-requires :added "4.1"}
(fact "reconstructs module requires from stored link metadata"
  (b/module-export-requires
   (module/book-module
    '{:id L.util
      :lang :lua
      :link {- L.util
             u L.core}
      :includes #{L.core}}))
  => '[[L.core :as u :include true]])

^{:refer hara.lang.book/module-export-imports :added "4.1"}
(fact "reconstructs native imports from stored module metadata"
  (b/module-export-imports
   (module/book-module
    '{:id L.util
      :lang :lua
      :link {- L.util}
      :native {cjson {:as cjson}}}))
  => '[[cjson :as cjson]]

  (b/module-export-imports
   (module/book-module
    '{:id L.util
      :lang :lua
      :link {- L.util}
      :native {cjson {:as cjson}
               json {:as json
                     :bundle {decoder {:version "1"}}}}}))
  => '[[cjson :as cjson]
       [json :as json :bundle [[decoder :version "1"]]]])

^{:refer hara.lang.book/module-specialize-symbol :added "4.1"}
(fact "rewrites self references from one module to another"
  (b/module-specialize-symbol 'L.core 'L.alt 'L.core/identity-fn)
  => 'L.alt/identity-fn

  (b/module-specialize-symbol 'L.core 'L.alt 'L.other/identity-fn)
  => 'L.other/identity-fn

  (b/module-specialize-symbol 'L.core 'L.alt 'plain-sym)
  => 'plain-sym)

^{:refer hara.lang.book/module-specialize-form :added "4.1"}
(fact "rewrites self references inside an entry form"
  (b/module-specialize-form 'L.core 'L.alt
                            '(defn f [x] (L.core/identity-fn x)))
  => '(defn f [x] (L.alt/identity-fn x))

  (b/module-specialize-form 'L.core 'L.alt
                            '(defn f [x] (L.other/identity-fn x)))
  => '(defn f [x] (L.other/identity-fn x)))

^{:refer hara.lang.book/module-specialize-entry :added "4.1"}
(fact "clones an entry into a new module"
  (let [entry (entry/book-entry {:lang :lua
                                 :id 'f
                                 :module 'L.core
                                 :section :code
                                 :form '(defn f [x] (L.core/identity-fn x))
                                 :form-input '(defn f [x] (L.core/identity-fn x))
                                 :standalone '(fn [x] (L.core/identity-fn x))
                                 :deps #{'L.core/identity-fn}
                                 :deps-fragment #{'L.core/identity}
                                 :namespace 'L.core
                                 :declared false
                                 :static/code.cache (atom {})
                                 :static/template.cache (atom {})})
        specialized (b/module-specialize-entry 'L.core 'L.alt entry)]
    [(:module specialized)
     (:form specialized)
     (:deps specialized)
     (:deps-fragment specialized)
     (:static/code.cache specialized)
     (:static/template.cache specialized)]
    => ['L.alt
        '(defn f [x] (L.alt/identity-fn x))
        '#{L.alt/identity-fn}
        '#{L.alt/identity}
        nil
        nil]))

^{:refer hara.lang.book/module-specialize-bindings :added "4.1"}
(fact "normalizes specialization bindings keyed by alias or module id"
  (let [module (module/book-module
                '{:id L.core
                  :lang :lua
                  :link {- L.core
                         cache L.cache}
                  :internal {L.core -
                             L.cache cache}})]
    [(b/module-specialize-bindings module '{L.cache L.custom-cache})
     (b/module-specialize-bindings module '{cache L.custom-cache})
     (b/module-specialize-bindings module '{L.core L.alt other L.x})])
  => [{'L.cache 'L.custom-cache}
      {'L.cache 'L.custom-cache}
      {'L.core 'L.alt
       'other 'L.x}])

^{:refer hara.lang.book/module-specialize :added "4.1"}
(fact "clones a module under a new id with rewritten links"
  (let [source (-> +sample+
                   (b/set-module
                    (module/book-module
                     '{:id L.custom-cache
                       :lang :lua
                       :link {- L.custom-cache}}))
                   second
                   (b/put-module
                    (merge (b/get-module +sample+ 'L.core)
                           {:link '{- L.core
                                    cache L.cache}
                            :internal '{L.core -
                                        L.cache cache}}))
                   second)
        cloned (b/module-specialize source
                                    'L.core
                                    'L.alt
                                    {:bindings {'L.cache
                                                'L.custom-cache}})]
    [(:id cloned)
     (:module (get-in cloned [:code 'identity-fn]))
     (:link cloned)])
  => ['L.alt 'L.alt '{- L.alt
                      cache L.custom-cache}])

^{:refer hara.lang.book/module-create :added "4.0"}
(fact "creates a module given book and options"

  (b/module-create +redis+
                   'L.redis
                   '{:require [[L.core :as u]]})
  => module/book-module?)

(comment
  (./import)
  (./create-tests)
  ;;
  ;; defn.<> specification
  ;;
  (book-entry {:lang :lua
               :id 'identity-fn
               :module 'L.core
               :section :code
               :form '(defn identity-fn [x] x)
               :form-input '(defn identity-fn [x] x)
               :deps #{}
               :namespace 'L.core
               :declared false})
  => book-entry?

  ;;
  ;; defmacro.<> specification
  ;;
  (book-entry {:lang :lua
               :id 'identity
               :module 'L.core
               :section :fragment
               :form '(defn identity [x] x)
               :form-input '(defn identity [x] x)
               :template (fn [x] x)
               :standalone '(fn [x] (return x))
               :deps #{}
               :namespace 'L.core
               :declared false})
  => book-entry?)

(comment

  ^{:refer hara.lang.book/module-create-bundled :added "4.0"}
  (fact "creates bundled packages given input modules"

    (b/module-create-bundled
     +redis+
     (b/module-create-requires '[[L.core :as u]]))
    => '{L.core {:suppress false, :native ()}}))