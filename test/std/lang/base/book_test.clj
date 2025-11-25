(ns std.lang.base.book-test
  (:use code.test)
  (:require [std.lang.base.book :as b]
            [std.lang.base.book-meta :as meta]
            [std.lang.base.book-module :as module]
            [std.lang.base.book-entry :as entry]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.emit-common :as common]
	    [std.lang.base.emit-helper :as helper]
            [std.lang.base.util :as ut]
            [std.lib :as h]))

(def +book+
  (b/book {:lang :lua
           :meta (meta/book-meta {:module-export  (fn [{:keys [as]} opts]
                                                    (h/$ (return ~as)))
                                  :module-import  (fn [name {:keys [as]} opts]  
                                                    (h/$ (var ~as := (require ~(str name)))))
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
  ^:hidden

  (b/get-base-entry +sample+ 'L.core 'identity :fragment)
  => +macro-entry+

  (b/get-base-entry +sample+ 'L.core 'identity-fn :code)
  => +fn-entry+)

^{:refer std.lang.base.book/get-code-entry :added "4.0"}
(fact "gets a code entry in the book"
  ^:hidden

  (b/get-code-entry +sample+ 'L.core/identity-fn)
  => +fn-entry+)

^{:refer std.lang.base.book/get-fragment-entry :added "4.0"}
(fact "gets a fragment entry in the book"
  ^:hidden
  
  (b/get-fragment-entry +sample+ 'L.core/identity)
  => +macro-entry+)

^{:refer std.lang.base.book/get-entry :added "4.0"}
(fact "gets either the module or code entry"
  ^:hidden
  
  (b/get-entry +sample+ 'L.core)
  => module/book-module?
  
  (b/get-entry +sample+ 'L.core/identity-fn)
  => entry/book-entry?)

^{:refer std.lang.base.book/get-module :added "4.0"}
(fact "gets the module"
  ^:hidden
  
  (b/get-module +sample+ 'L.core)
  => module/book-module?)

^{:refer std.lang.base.book/get-code-deps :added "4.0"}
(fact "gets `:deps` or if a `:static/template` calculate dependencies"
  ^:hidden
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
  ^:hidden
  
  (b/get-deps +sample+ 'L.core)
  => #{}

  (b/get-deps +sample+ 'L.core/identity-fn)
  => #{})

^{:refer std.lang.base.book/get-deps-native :added "4.0"}
(fact "gets the imports for a book"
  ^:hidden
  
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
  ^:hidden
  
  (b/list-entries +sample+ :module)
  => '(L.core)

  (b/list-entries +sample+ :code)
  => '(L.core/identity-fn))

^{:refer std.lang.base.book/book-string :added "4.0"}
(fact "shows the book string"
  ^:hidden
  
  (b/book-string +sample+)
  => "#book [:lua] {L.core {:code 1, :fragment 1}}")

^{:refer std.lang.base.book/book? :added "4.0"}
(fact "checks that object is a book"
  ^:hidden
  
  (b/book? +sample+)
  => true)

^{:refer std.lang.base.book/book :added "4.0"}
(fact "creates a book"
  ^:hidden
  
  (b/book {:lang :redis
           :meta    (:meta +book+)
           :grammar (:grammar +book+)
           :parent  :lua
           :merged  #{}})
  => b/book?)

^{:refer std.lang.base.book/book-merge :added "4.0"}
(fact "merges a book with it's parent"
  ^:hidden
  
  (b/book-merge (b/book {:lang    :redis
                         :meta    (:meta +book+)
                         :grammar (:grammar +book+)
                         :parent  :lua
                         :merged  #{}})
                +sample+)
  => b/book?)

^{:refer std.lang.base.book/book-from :added "4.0"}
(fact "returns the merged book given snapshot"
  ^:hidden
  (let [snapshot {:lua  {:book +sample+}
                  :redis {:book (b/book {:lang :redis
                                        :meta    (:meta +book+)
                                        :grammar (:grammar +book+)
                                        :parent  :lua
                                        :merged  #{}})}}]
    (b/book-from snapshot :redis))
  => b/book?)

^{:refer std.lang.base.book/check-compatible-lang :added "4.0"
  :setup [(def +redis+
            (b/book-merge (b/book {:lang    :redis
                                   :meta    (:meta +book+)
                                   :grammar (:grammar +book+)
                                   :parent  :lua
                                   :merged  #{}})
                          +sample+))]}
(fact "checks if the lang is compatible with the book"
  ^:hidden
  
  (b/check-compatible-lang +redis+ :lua)
  => true

  (b/check-compatible-lang +redis+ :redis)
  => true

  (b/check-compatible-lang +redis+ :js)
  => false)

^{:refer std.lang.base.book/assert-compatible-lang :added "4.0"}
(fact "asserts that the lang is compatible"
  ^:hidden
  
  (b/assert-compatible-lang +redis+ :lua)
  => true

  (b/assert-compatible-lang +redis+ :js)
  => (throws))

^{:refer std.lang.base.book/set-module :added "4.0"}
(fact "adds an addional module to the book"
  ^:hidden
  
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
  ^:hidden

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
  ^:hidden

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
  ^:hidden
  
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
  ^:hidden

  (b/has-module? +redis+ 'L.core)
  => true

  (b/has-module? +redis+ 'L.other)
  => false)

^{:refer std.lang.base.book/assert-module :added "4.0"}
(fact "asserts that module exists"
  ^:hidden
  
  (b/assert-module +redis+ 'L.core)
  => true
  
  (b/assert-module +redis+ 'L.other)
  => (throws))

^{:refer std.lang.base.book/set-entry :added "4.0"}
(fact "sets entry in the book"
  ^:hidden
  
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
  ^:hidden
  
  (-> (b/put-entry +redis+
                   (entry/book-entry {:lang :lua
                                      :id 'identity-fn
                                      :module 'L.core
                                      :section :code
                                      :form '()}))
      second
      (b/get-entry 'L.core/identity-fn)
      (->> (into {})))
  => '{:form-input (defn identity-fn [x] x),
       :section :code,
       :time nil,
       :standalone nil,
       :deps-native nil,
       :template nil,
       :module L.core,
       :lang :lua,
       :line nil,
       :deps-fragment nil,
       :priority nil,
       :id identity-fn,
       :declared false,
       :display :default,
       :form (),
       :namespace L.core,
       :deps #{}})

^{:refer std.lang.base.book/has-entry? :added "4.0"}
(fact "checks that book has an entry"
  ^:hidden
  
  (b/has-entry? +redis+ 'L.core :fragment 'identity)
  => true)

^{:refer std.lang.base.book/assert-entry :added "4.0"}
(fact "asserts that module exists"
  ^:hidden
  
  (b/assert-entry +redis+ 'L.core :fragment 'identity)
  => true

  (b/assert-entry +redis+ 'ERROR :fragment 'identity)
  => (throws)
  
  (b/assert-entry +redis+ 'L.core :fragment 'ERROR)
  => (throws))

^{:refer std.lang.base.book/delete-entry :added "4.0"}
(fact "deletes an entry"
  ^:hidden
  
  (-> (b/delete-entry +redis+ 'L.core :fragment 'identity)
      second
      b/book-string)
  => "#book [:redis] {L.core {:code 1, :fragment 0}}")

^{:refer std.lang.base.book/module-create-filename :added "4.0"}
(fact "creates a filename for module"
  ^:hidden
  
  (b/module-create-filename +redis+ 'redis.core)
  => "core.lua")

^{:refer std.lang.base.book/module-create-check :added "4.0"}
(fact "checks that bundles are available"
  ^:hidden
  
  (b/module-create-check +redis+ 'L.redis '{u L.core})
  => true

  (b/module-create-check +redis+ 'L.redis '{u NOT-FOUND})
  => (throws))

^{:refer std.lang.base.book/module-create-requires :added "4.0"}
(fact "creates a map for the requires"
  ^:hidden
  (b/module-create-requires '[[L.core :as u]])
  => '{L.core {:as u, :id L.core}})

^{:refer std.lang.base.book/module-create :added "4.0"}
(fact "creates a module given book and options"
  ^:hidden
  
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
    ^:hidden
    
    (b/module-create-bundled
     +redis+
     (b/module-create-requires '[[L.core :as u]]))
    => '{L.core {:suppress false, :native ()}}))
