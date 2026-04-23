(ns std.lang.base.book-test
  (:require [std.lang.base.book :as b]
            [std.lang.base.book-entry :as entry]
            [std.lang.base.book-meta :as meta]
            [std.lang.base.book-module :as module]
            [std.lang.base.emit-common :as common]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.util :as ut]
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

^{:refer std.lang.base.book/get-base-entry :added "4.0"}
(fact "gets an entry in the book"

  (b/get-base-entry +sample+ 'L.core 'identity :fragment)
  => +macro-entry+

  (b/get-base-entry +sample+ 'L.core 'identity-fn :code)
  => +fn-entry+)

^{:refer std.lang.base.book/get-code-entry :added "4.0"}
(fact "gets a code entry in the book"

  (b/get-code-entry +sample+ 'L.core/identity-fn)
  => +fn-entry+)

^{:refer std.lang.base.book/get-fragment-entry :added "4.0"}
(fact "gets a fragment entry in the book"

  (b/get-fragment-entry +sample+ 'L.core/identity)
  => +macro-entry+)

^{:refer std.lang.base.book/get-entry :added "4.0"}
(fact "gets either the module or code entry"

  (b/get-entry +sample+ 'L.core)
  => module/book-module?

  (b/get-entry +sample+ 'L.core/identity-fn)
  => entry/book-entry?)

^{:refer std.lang.base.book/get-module :added "4.0"}
(fact "gets the module"

  (b/get-module +sample+ 'L.core)
  => module/book-module?)

^{:refer std.lang.base.book/get-code-deps :added "4.0"}
(fact "gets `:deps` or if a `:static/template` calculate dependencies"
  (-> (b/set-entry +sample+
                   (entry/book-entry {:lang :lua
                                      :id 'inc-fn
                                      :module 'L.core
                                      :section :code
                                      :form '(defn inc-fn [x] (return (+ 1 x)))
                                      :form-input '(defn inc-fn [x] (return (+ 1 x)))
                                      :deps '#{L.core/identity-fn}
                                      :namespace 'L.core
                                      :declared false}))
      second
      (b/get-code-deps 'L.core/inc-fn))
  => '#{L.core/identity-fn})

^{:refer std.lang.base.book/get-deps :added "4.0"}
(fact "get dependencies for a given id"

  (b/get-deps +sample+ 'L.core)
  => #{}

  (b/get-deps +sample+ 'L.core/identity-fn)
  => #{})

^{:refer std.lang.base.book/get-deps-native :added "4.0"}
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

^{:refer std.lang.base.book/list-entries :added "4.0"}
(fact "lists entries for a given symbol"

  (b/list-entries +sample+ :module)
  => '(L.core)

  (b/list-entries +sample+ :code)
  => '(L.core/identity-fn))

^{:refer std.lang.base.book/book-string :added "4.0"}
(fact "shows the book string"

  (b/book-string +sample+)
  => "#book [:lua] {L.core {:code 1, :fragment 1}}")

^{:refer std.lang.base.book/book? :added "4.0"}
(fact "checks that object is a book"

  (b/book? +sample+)
  => true)

^{:refer std.lang.base.book/book :added "4.0"}
(fact "creates a book"

  (b/book {:lang :lua.redis
           :meta    (:meta +book+)
           :grammar (:grammar +book+)
           :parent  :lua
           :merged  #{}})
  => b/book?)

^{:refer std.lang.base.book/book-merge :added "4.0"}
(fact "merges a book with it's parent"

  (b/book-merge (b/book {:lang    :lua.redis
                         :meta    (:meta +book+)
                         :grammar (:grammar +book+)
                         :parent  :lua
                         :merged  #{}})
                +sample+)
  => b/book?)

^{:refer std.lang.base.book/book-from :added "4.0"}
(fact "returns the merged book given snapshot"
  (let [snapshot {:lua  {:book +sample+}
                  :lua.redis {:book (b/book {:lang :lua.redis
                                        :meta    (:meta +book+)
                                        :grammar (:grammar +book+)
                                        :parent  :lua
                                        :merged  #{}})}}]
    (b/book-from snapshot :lua.redis))
  => b/book?)

^{:refer std.lang.base.book/check-compatible-lang :added "4.0"
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

^{:refer std.lang.base.book/assert-compatible-lang :added "4.0"}
(fact "asserts that the lang is compatible"

  (b/assert-compatible-lang +redis+ :lua)
  => true

  (b/assert-compatible-lang +redis+ :js)
  => (throws))

^{:refer std.lang.base.book/set-module :added "4.0"}
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

^{:refer std.lang.base.book/put-module :added "4.0"}
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

^{:refer std.lang.base.book/delete-module :added "4.0"}
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

^{:refer std.lang.base.book/delete-modules :added "4.0"}
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

^{:refer std.lang.base.book/has-module? :added "4.0"}
(fact "checks that a books has a given module"

  (b/has-module? +redis+ 'L.core)
  => true

  (b/has-module? +redis+ 'L.other)
  => false)

^{:refer std.lang.base.book/assert-module :added "4.0"}
(fact "asserts that module exists"

  (b/assert-module +redis+ 'L.core)
  => true

  (b/assert-module +redis+ 'L.other)
  => (throws))

^{:refer std.lang.base.book/set-entry :added "4.0"}
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

^{:refer std.lang.base.book/put-entry :added "4.0"}
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

^{:refer std.lang.base.book/has-entry? :added "4.0"}
(fact "checks that book has an entry"

  (b/has-entry? +redis+ 'L.core :fragment 'identity)
  => true)

^{:refer std.lang.base.book/assert-entry :added "4.0"}
(fact "asserts that module exists"

  (b/assert-entry +redis+ 'L.core :fragment 'identity)
  => true

  (b/assert-entry +redis+ 'ERROR :fragment 'identity)
  => (throws)

  (b/assert-entry +redis+ 'L.core :fragment 'ERROR)
  => (throws))

^{:refer std.lang.base.book/delete-entry :added "4.0"}
(fact "deletes an entry"

  (-> (b/delete-entry +redis+ 'L.core :fragment 'identity)
      second
      b/book-string)
  => "#book [:lua.redis] {L.core {:code 1, :fragment 0}}")

^{:refer std.lang.base.book/module-create-filename :added "4.0"}
(fact "creates a filename for module"

  (b/module-create-filename +redis+ 'redis.core)
  => "core.lua")

^{:refer std.lang.base.book/module-create-check :added "4.0"}
(fact "checks that bundles are available"

  (b/module-create-check +redis+ 'L.redis '{u L.core})
  => true

  (b/module-create-check +redis+ 'L.redis '{u NOT-FOUND})
  => (throws))

^{:refer std.lang.base.book/module-create-requires :added "4.0"}
(fact "creates a map for the requires"
  (b/module-create-requires '[[L.core :as u]])
  => '{L.core {:as u, :id L.core}})

^{:refer std.lang.base.book/module-create :added "4.0"}
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

  ^{:refer std.lang.base.book/module-create-bundled :added "4.0"}
  (fact "creates bundled packages given input modules"

    (b/module-create-bundled
     +redis+
     (b/module-create-requires '[[L.core :as u]]))
    => '{L.core {:suppress false, :native ()}}))
