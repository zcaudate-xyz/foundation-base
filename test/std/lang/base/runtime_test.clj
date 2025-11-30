(ns std.lang.base.runtime-test
  (:use code.test)
  (:require [std.lang.base.pointer :refer :all]
            [std.lang.base.util :as ut]
            [std.lang.base.impl-entry :as entry]
            [std.lang.base.impl-deps :as deps]
            [std.lang.base.book :as book]
            [std.lang.base.pointer :as ptr]
            [std.lang.base.library :as lib]
            [std.lang.base.library-snapshot :as snap]
            [std.lang.base.emit-prep-lua-test :as prep]
            [std.lang.base.runtime :as rt]
            [std.json :as json]
            [std.lib :as h]))

(def +library-ext+
  (doto (lib/library:create
         {:snapshot (snap/snapshot {:lua {:id :lua
                                          :book prep/+book-min+}})})
    (lib/install-module! :lua 'L.util
                         {:require '[[L.core :as u]]
                          :import '[["cjson" :as cjson]]})
    (lib/add-entry-single!
     (entry/create-code-base
      '(defn sub-fn
         [a b]
         (return ((u/identity-fn u/sub) a b)))
      {:lang :lua
       :namespace (h/ns-sym)
       :module 'L.util}
      {}))
    (lib/add-entry-single!
     (entry/create-code-base
      '(defn add-fn
         [a b]
         (return ((u/identity-fn u/add) a (-/sub-fn b 0))))
      {:lang :lua
       :namespace (h/ns-sym)
       :module 'L.util}
      {}))))

(def +ptr+
  (ut/lang-pointer :lua
                   {:module 'L.core
                    :id 'add
                    :section :fragment
                    :library +library-ext+}))

^{:refer std.lang.base.runtime/default-tags-ptr :added "4.0"}
(fact "runtime default args"
  ^:hidden
  
  (rt/default-tags-ptr {:runtime :hello}
                       +ptr+)
  => '[:hello L.core/add :fragment])

^{:refer std.lang.base.runtime/default-deref-ptr :added "4.0"}
(fact "runtime default deref"
  ^:hidden
  
  (rt/default-deref-ptr {:runtime :hello
                         :library +library-ext+}
                        +ptr+)
  => book/book-entry?

  (rt/default-deref-ptr {:runtime :hello}
                        +ptr+)
  => nil)

^{:refer std.lang.base.runtime/default-invoke-ptr :added "4.0"}
(fact "runtime default invoke"
  ^:hidden
  
  (rt/default-invoke-ptr {:runtime :hello
                          :library +library-ext+}
                         +ptr+
                         [1 2])
  => "1 + 2")

^{:refer std.lang.base.runtime/default-init-ptr :added "4.0"}
(fact "will init pointer if there is a :rt/init key"
  (rt/default-init-ptr {:lang :lua} {}) => nil)

^{:refer std.lang.base.runtime/default-display-ptr :added "4.0"}
(fact "runtime default display"
  ^:hidden
  
  (rt/default-display-ptr {:runtime :hello
                           :library +library-ext+}
                          +ptr+)
  => "(fn:> [x y] (+ x y))"

  (rt/default-display-ptr {:runtime :hello}
                          (dissoc +ptr+ :library))
  => "[:not-found L.core/add :fragment]")

^{:refer std.lang.base.runtime/rt-default :added "4.0"}
(fact "creates a lang runtime"
  ^:hidden
  
  (rt/rt-default {:lang :lua})
  => rt/rt-default?)

^{:refer std.lang.base.runtime/rt-default? :added "4.0"}
(fact "checks if object is default runtime"
  (rt/rt-default? (rt/rt-default {:lang :lua})) => true)

^{:refer std.lang.base.runtime/install-lang! :added "4.0"}
(fact "installs a language within `std.lib.context`"
  (rt/install-lang! :lua) => (any map? vector?))

^{:refer std.lang.base.runtime/install-type! :added "4.0"}
(fact "installs a specific runtime type given `:lang`"
  (rt/install-type! :lua :test-runtime {:type :hara/lang.rt
                                        :config {:bootstrap false}})
  => map?)

^{:refer std.lang.base.runtime/return-format-simple :added "4.0"}
(fact "format forms for return"
  ^:hidden
  
  (rt/return-format-simple [1 2 3])
  => '(1 2 (return 3)))

^{:refer std.lang.base.runtime/return-format :added "4.0"}
(fact "standard format for return"
  ^:hidden
  
  (rt/return-format '[1 2 (:= x 3)])
  => '(1 2 (:= x 3)))

^{:refer std.lang.base.runtime/return-wrap-invoke :added "4.0"}
(fact "wraps forms to be invoked"
  ^:hidden
  
  (rt/return-wrap-invoke '[1 2 (:= x 3)])
  => '((quote ((fn [] 1 2 (:= x 3))))))

^{:refer std.lang.base.runtime/return-transform :added "4.0"}
(fact "standard return transform"
  ^:hidden
  
  (rt/return-transform '[1 2 (:= x 3)] {})
  => '((quote ((fn [] (return [1 2 (:= x 3)])))))

  (rt/return-transform '[1 2 (:= x 3)] {:bulk true})
  => '((quote ((fn [] 1 2 (:= x 3))))))

^{:refer std.lang.base.runtime/default-invoke-script :added "4.0"}
(fact "default invoke script call used by most runtimes"
  ^:hidden

  (rt/default-invoke-script {:runtime :hello
                             :library +library-ext+}
                            +ptr+
                            [10 20]
                            rt/default-raw-eval
                            {:json false})
  => "10 + 20")

^{:refer std.lang.base.runtime/default-lifecycle-prep :added "4.0"}
(fact "prepares mopts for lifecycle"
  ^:hidden
  
  (rt/default-lifecycle-prep {:library +library-ext+
                              :lang :lua
                              :module 'L.core})
  => map?)

^{:refer std.lang.base.runtime/default-scaffold-setup-for :added "4.0"}
(fact "setup native modules, defglobals and defruns in the runtime"
  (rt/default-scaffold-setup-for
   (rt/map->RuntimeDefault
    {:library +library-ext+
     :lang :lua
     :module 'L.core
     :layout :full})
   'L.core)
  => string?)

^{:refer std.lang.base.runtime/default-scaffold-setup-to :added "4.0"}
(fact "setup scaffold up to but not including the current module in the runtime"
  (rt/default-scaffold-setup-to
   (rt/map->RuntimeDefault
    {:library +library-ext+
     :lang :lua
     :module 'L.core
     :layout :full})
   'L.util)
  => string?)

^{:refer std.lang.base.runtime/default-scaffold-imports :added "4.0"}
(fact "embed native imports to be globally accessible"
  (rt/default-scaffold-imports
   (rt/map->RuntimeDefault
    {:library +library-ext+
     :lang :lua
     :module 'L.core
     :layout :full})
   'L.util)
  => string?)

^{:refer std.lang.base.runtime/default-lifecycle-fn :added "4.0"}
(fact "constructs a lifecycle fn"
  ^:hidden
  
  ((rt/default-lifecycle-fn deps/has-module-form)
   {:library +library-ext+
    :lang :lua
    :module 'L.core}
   'L.core)
  => nil

  ((rt/default-lifecycle-fn deps/has-ptr-form)
   (rt/map->RuntimeDefault
    {:library +library-ext+
     :lang :lua
     :module 'L.core
     :layout :full})
   (ut/lang-pointer :lua
                    {:module 'L.util
                     :id 'add-fn
                     :section :code
                     :library +library-ext+}))
  => "L_util____add_fn != nil")

^{:refer std.lang.base.runtime/default-setup-module-emit :added "4.0"}
(fact "emits the string for the module"
  ^:hidden
  
  (rt/default-setup-module-emit
   (rt/map->RuntimeDefault
    {:library +library-ext+
     :lang :lua
     :namespace (h/ns-sym)
     :module 'L.core
     :layout :full})
   'L.util)
  => string?)

^{:refer std.lang.base.runtime/default-setup-module-basic :added "4.0"}
(fact "basic setup module action"
  ^:hidden
  
  (rt/default-setup-module-basic
   (rt/map->RuntimeDefault
    {:library +library-ext+
     :lang :lua
     :module 'L.core
     :layout :full})
   'L.util)
  => string?)

^{:refer std.lang.base.runtime/default-teardown-module-basic :added "4.0"}
(fact "basic teardown module action"
  ^:hidden
  
  (rt/default-teardown-module-basic
   (rt/map->RuntimeDefault
    {:library +library-ext+
     :lang :lua
     :module 'L.core
     :layout :full})
   'L.util)
  => "L_util____add_fn = nil\n\nL_util____sub_fn = nil")

^{:refer std.lang.base.runtime/default-setup-module :added "4.0"}
(fact "default setup module (with error isolation)"
  (rt/default-setup-module
   (rt/map->RuntimeDefault
    {:library +library-ext+
     :lang :lua
     :module 'L.core
     :layout :full})
   'L.util)
  => string?)

^{:refer std.lang.base.runtime/default-teardown-module :added "4.0"}
(fact "default teardown module (with error isolation)"
  (rt/default-teardown-module
   (rt/map->RuntimeDefault
    {:library +library-ext+
     :lang :lua
     :module 'L.core
     :layout :full})
   'L.util)
  => string?)

^{:refer std.lang.base.runtime/multistage-invoke :added "4.0"}
(fact "invokes a multistage pipeline given deps function"
  ^:hidden
  
  (rt/multistage-invoke
   (rt/map->RuntimeDefault
    {:library +library-ext+
     :lang :lua
     :module 'L.core
     :layout :full})
   'L.util
   (fn [_ _ _] nil)
   (fn [book module-id]
     (h/deps:ordered book [module-id])))
  => '[[L.core nil] [L.util nil]])

^{:refer std.lang.base.runtime/multistage-setup-for :added "4.0"}
(fact "setup for a given namespace"
  ^:hidden
  
  (rt/multistage-setup-for
   (rt/map->RuntimeDefault
    {:library +library-ext+
     :lang :lua
     :module 'L.core
     :layout :full})
   'L.util)
  => (contains-in [['L.core string?] ['L.util string?]]))

^{:refer std.lang.base.runtime/multistage-setup-to :added "4.0"}
(fact "setup to a given namespcase"
  ^:hidden
  
  (rt/multistage-setup-to
   (rt/map->RuntimeDefault
    {:library +library-ext+
     :lang :lua
     :module 'L.core
     :layout :full})
   'L.util)
  => (contains-in [['L.core string?]]))

^{:refer std.lang.base.runtime/multistage-teardown-for :added "4.0"}
(fact "teardown for a given namespace"
  ^:hidden
  
  (rt/multistage-teardown-for
   (rt/map->RuntimeDefault
    {:library +library-ext+
     :lang :lua
     :module 'L.core
     :layout :full})
   'L.util)
  => (contains-in [['L.util string?] ['L.core string?]]))

^{:refer std.lang.base.runtime/multistage-teardown-at :added "4.0"}
(fact "teardown all dependents including this"
  ^:hidden
  
  (rt/multistage-teardown-at
   (rt/map->RuntimeDefault
    {:library +library-ext+
     :lang :lua
     :module 'L.core
     :layout :full})
   'L.util)
  => '[[L.util "L_util____add_fn = nil\n\nL_util____sub_fn = nil"]])

^{:refer std.lang.base.runtime/multistage-teardown-to :added "4.0"}
(fact "teardown all dependents upto this"
  ^:hidden
  
  (rt/multistage-teardown-to
   (rt/map->RuntimeDefault
    {:library +library-ext+
     :lang :lua
     :module 'L.core
     :layout :full})
   'L.util)
  => []
  
  (rt/multistage-teardown-to
   (rt/map->RuntimeDefault
    {:library +library-ext+
     :lang :lua
     :module 'L.core
     :layout :full})
   'L.core)
  => '[[L.util "L_util____add_fn = nil\n\nL_util____sub_fn = nil"]])
