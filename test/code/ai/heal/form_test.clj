(ns code.ai.heal.form-test
  (:use code.test)
  (:require [code.ai.heal.form :refer :all]))

(defn mock-task [handler]
  (reify
    clojure.lang.Associative
    (containsKey [this k] false)
    (entryAt [this k] nil)
    (assoc [this k v] this)

    clojure.lang.ILookup
    (valAt [this k] nil)
    (valAt [this k d] d)

    clojure.lang.IPersistentCollection
    (count [this] 0)
    (cons [this o] this)
    (empty [this] this)
    (equiv [this o] false)
    (seq [this] nil)

    clojure.lang.IFn
    (invoke [this a b c] (handler a b c))
    (applyTo [this args] (apply handler args))))


^{:refer code.ai.heal.form/get-dsl-deps-fn :added "4.1"}
(fact "gets the dsl dependencies"
  ^:hidden
  
  (get-dsl-deps-fn "(ns my.ns (:require [std.lang :as l] [std.lib :as h])) (l/script :lua {:require [[xt.lang.base-lib :as k]]})")
  => {:ns 'my.ns :deps #{'xt.lang.base-lib}})

^{:refer code.ai.heal.form/load-file-fn :added "4.1"}
(fact "loads a file"
  ^:hidden
  
  (load-file-fn "(ns my.ns) (+ 1 2)")
  => ['my.ns])

^{:refer code.ai.heal.form/get-load-order :added "4.1"}
(fact "gets the load order"
  ^:hidden
  
  (get-load-order {'a {:ns 'a :deps #{}}
                   'b {:ns 'b :deps #{'a}}})
  => ['a 'b])

^{:refer code.ai.heal.form/heal-directory :added "4.1"}
(fact "heals a directory"
  ^:hidden
  
  (with-redefs [code.manage/transform-code (fn [_ _ _] :healed)]
    (heal-directory {:root "." :source-paths ["src"]}))
  => :healed)

^{:refer code.ai.heal.form/refactor-directory :added "4.1"}
(fact "refactors a directory"
  ^:hidden
  
  (with-redefs [code.manage/refactor-code (fn [_ _ _] :refactored)]
    (refactor-directory {:root "." :source-paths ["src"]} []))
  => :refactored)

^{:refer code.ai.heal.form/get-dsl-deps :added "4.1"}
(fact "gets dsl deps for directory"
  ^:hidden
  
  (with-redefs [code.manage/extract (mock-task (fn [_ _ _] :deps))]
    (get-dsl-deps {:root "." :source-paths ["src"]}))
  => :deps)

^{:refer code.ai.heal.form/load-directory :added "4.1"}
(fact "loads a directory"
  ^:hidden
  
  (with-redefs [code.manage/extract (mock-task (fn [_ _ _] :loaded))]
    (load-directory {:root "." :source-paths ["src"]} {}))
  => :loaded)
