(ns std.lang.base.emit-preprocess-test
  (:use code.test)
  (:require [std.lang.base.emit-preprocess :refer :all]
            [std.lang.base.emit-common :as common]
            [std.lang.base.emit-helper :as helper]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.emit-prep-lua-test :as prep]
            [std.lang.base.emit-prep-js-test :as prep-js]
            [std.lang.base.book-entry :as entry]
            [std.lang.base.library :as lib]
            [std.lang.base.book :as b]
            [std.lang.base.library-snapshot :as snap]
            [std.lang.base.impl-entry :as impl-entry]
            [std.lib :as h]))

(def +reserved+
  (-> (grammar/build)
      (grammar/to-reserved)))

(def +grammar+
  (grammar/grammar :test +reserved+ helper/+default+))

^{:refer std.lang.base.emit-preprocess/macro-form :added "4.0"}
(fact "gets the current macro form")

^{:refer std.lang.base.emit-preprocess/macro-opts :added "4.0"}
(fact "gets current macro-opts")

^{:refer std.lang.base.emit-preprocess/macro-grammar :added "4.0"}
(fact "gets the current grammar")

^{:refer std.lang.base.emit-preprocess/with:macro-opts :added "4.0"}
(fact "bind macro opts")

^{:refer std.lang.base.emit-preprocess/to-input-form :added "4.0"}
(fact "processes a form"
  
  (def hello 1)
  
  (to-input-form '(@! (+ 1 2 3)))
  => '(!:template (+ 1 2 3))
  
  (to-input-form '(-/Class$$new))
  => (any '(static-invoke -/Class "new")
          nil)
  
  (to-input-form '(Class$$new 1 2 3))
  => (any '(static-invoke Class "new" 1 2 3)
          nil)
  
  (to-input-form '@#'hello)
  => '(!:deref (var std.lang.base.emit-preprocess-test/hello))
  
  (to-input-form '@(+ 1 2 3))
  => '(!:eval (+ 1 2 3))

  (to-input-form '(@.lua (do 1 2 3)))
  => '(!:lang {:lang :lua} (do 1 2 3)))

^{:refer std.lang.base.emit-preprocess/to-input :added "4.0"}
(fact "converts a form to input (extracting deref forms)"
  ^:hidden

  (to-input '(do (~! [1 2 3 4])))
  => (throws)
  
  (binding [*macro-splice* true]
    (to-input '(do (~! [1 2 3 4]))))
  => '(do 1 2 3 4))

^{:refer std.lang.base.emit-preprocess/get-fragment :added "4.0"}
(fact "gets the fragment given a symbol and modules"
  ^:hidden
  
  (get-fragment 'L.core/add
                (:modules prep/+book-min+)
                {:module {:id 'L.util
                          :link '{u L.core}}})
  => entry/book-entry?)

^{:refer std.lang.base.emit-preprocess/process-namespaced-resolve :added "4.0"}
(fact "resolves symbol in current namespace"
  ^:hidden
  
  (process-namespaced-resolve 'u/add
                              (:modules prep/+book-min+)
                              {:module   {:id 'L.util
                                          :link '{u L.core}}})
  => '[L.core add L.core/add]

  (process-namespaced-resolve 'u/UNKNOWN
                              (:modules prep/+book-min+)
                              {:module   {:id  'L.util
                                          :link '{u L.core}}})
  => '[L.core UNKNOWN L.core/UNKNOWN]
  
  (process-namespaced-resolve 'other/function
                              (:modules prep/+book-min+)
                              {:module   {:id 'L.util
                                          :link '{u L.core}}})
  => (throws))

^{:refer std.lang.base.emit-preprocess/process-namespaced-symbol :added "4.0"}
(fact "process namespaced symbols"
  ^:hidden
  
  (process-namespaced-symbol 'u/add
                             (:modules prep/+book-min+)
                             {:module   {:id 'L.util
                                         :link '{u L.core}}}
                             (volatile! #{})
                             (volatile! #{})
                             identity)
  => '(fn [x y] (return (+ x y)))
  
  (process-namespaced-symbol 'u/sub
                             (:modules prep/+book-min+)
                             {:module   {:id 'L.util
                                         :link '{u L.core}}}
                             (volatile! #{})
                             (volatile! #{})
                             identity)
  => '(fn [x y] (return (- x y)))

  (process-namespaced-symbol 'u/identity-fn
                             (:modules prep/+book-min+)
                             {:module   {:id 'L.util
                                         :link '{u L.core}}}
                             (volatile! #{})
                             (volatile! #{})
                             identity)
  => 'L.core/identity-fn

  (process-namespaced-symbol '-/hello
                             (:modules prep/+book-min+)
                             
                             {:entry {:id 'hello}
                              :module   {:id 'L.util
                                         :link '{u L.core
                                                 - L.util}}}
                             (volatile! #{})
                             (volatile! #{})
                             identity)
  => 'L.util/hello

  (process-namespaced-symbol 'u/UNKNOWN
                              (:modules prep/+book-min+)
                              {:module   {:id  'L.util
                                          :link '{u L.core}}}
                              (volatile! #{})
                              (volatile! #{})
                             identity)
  => (throws))

^{:refer std.lang.base.emit-preprocess/process-inline-assignment :added "4.0"}
(fact "prepares the form for inline assignment"
  ^:hidden
  
  (def +form+
    (process-inline-assignment '(var a := (u/identity-fn 1) :inline)
                               (:modules prep/+book-min+)
                               '{:module {:link {u L.core}}}
                               true))
  +form+
  => '(var a := (L.core/identity-fn 1))


  (meta (last +form+))
  => {:assign/inline true})

^{:refer std.lang.base.emit-preprocess/to-staging-form :added "4.0"}
(fact "different staging forms"
  ^:hidden
  
  (to-staging-form '(!:template (+ 1 2 3))
                   nil
                   (:modules prep/+book-min+)
                   '{:module {:link {u L.core}}}
                   nil
                   identity)
  => 6

  @(to-staging-form '(!:eval (+ 1 2 3))
                    nil
                    (:modules prep/+book-min+)
                   '{:module {:link {u L.core}}}
                   nil
                   identity)
  => '(!:eval (+ 1 2 3))

  (to-staging-form '(hello 1 2 3)
                   {:reserved {'hello {:type :template
                                       :macro (fn [[_ & args]]
                                                (cons '+ (concat args args)))}}}
                   (:modules prep/+book-min+)
                   '{:module {:link {u L.core}}}
                   nil
                   identity)
  => '(+ 1 2 3 1 2 3)

  (to-staging-form '(hello 1 2 3)
                   {:reserved {'hello {:type :hard-link
                                       :raw 'world}}}
                   (:modules prep/+book-min+)
                   '{:module {:link {u L.core}}}
                   nil
                   identity)
  => '(world 1 2 3))

^{:refer std.lang.base.emit-preprocess/process-standard-symbol :added "4.0"}
(fact "processes a standard symbol"
  ^:hidden
  
  (def +library-js+
    (doto (lib/library:create
           {:snapshot (snap/snapshot {:js {:id :js
                                           :book prep-js/+book-min+}})})
      (lib/install-module! :js 'JS.ui
                           {:import  ' [["@measured/puck" :as [* Puck]]
                                        ["@radix-ui/themes" :as [* Radix]
                                         :bundle [["@radix-ui/themes/styles.css"]]]]})))

  (let [deps-native (volatile! {})
        sym (process-standard-symbol 'Puck.Puck
                                     {:module (lib/get-module +library-js+
                                                              :js
                                                              'JS.ui)}
                                     deps-native)]
    [sym @deps-native])
  => '[Puck.Puck {"@measured/puck" #{Puck}}])

^{:refer std.lang.base.emit-preprocess/to-staging :added "4.0"}
(fact "converts the stage"
  ^:hidden
  
  (to-staging '(u/add (u/identity-fn 1) 2)
              nil
              (:modules prep/+book-min+)
              '{:module {:link {u L.core}}})
  => '[(+ (L.core/identity-fn 1) 2) #{L.core/identity-fn} #{L.core/add} {}]
  
  (to-staging '(u/sub (u/add (u/identity-fn 1) 2)
                      (-/hello))
              nil
              (:modules prep/+book-min+)
              '{:entry {:id hello}
                :module {:id L.util
                         :link {u L.core}}})
  => '[(- (+ (L.core/identity-fn 1) 2) (L.util/hello))
       #{L.core/identity-fn}
       #{L.core/add L.core/sub}
       {}]

  ((juxt identity
         (comp meta last first))
   (to-staging '(var a := (u/identity-fn 1) :inline)
               {:reserved {'var {:emit :def-assign}}}
               (:modules prep/+book-min+)
               '{:module {:link {u L.core}}}))
  => '[[(var a := (L.core/identity-fn 1)) #{} #{} {}] #:assign{:inline true}])

^{:refer std.lang.base.emit-preprocess/to-resolve :added "4.0"}
(fact "resolves only the code symbols (no macroexpansion)"
  ^:hidden

  (to-resolve '(u/add (u/identity-fn 1) 2)
               nil
               (:modules prep/+book-min+)
               '{:module {:link {u L.core}}})
  => '(L.core/add (L.core/identity-fn 1) 2))

^{:refer std.lang.base.emit-preprocess/find-natives :added "4.0"}
(fact "find natives for a macro entry"
  ^:hidden
  
  (def +library-js+
    (doto (lib/library:create
           {:snapshot (snap/snapshot {:js {:id :js
                                           :book prep-js/+book-min+}})})
      (lib/install-module! :js 'JS.ui
                           {:import  ' [["react" :as React]]})
      (lib/add-entry-single!
       (impl-entry/create-macro
        '(defmacro hello
           [s]
           (list 'React.useEffect s))
        {:lang :js
         :namespace 'JS.ui
         :module 'JS.ui}))))

  (find-natives 
   (lib/get-entry +library-js+
                  {:lang :js
                   :module 'JS.ui
                   :id 'hello
                   :section :fragment})
   {:module (lib/get-module +library-js+
                            :js
                            'JS.ui)})
  => '{"react" #{React}})
