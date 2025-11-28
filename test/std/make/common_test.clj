(ns std.make.common-test
  (:use code.test)
  (:require [std.make.common :as common]
            [std.make.compile :as compile]))

(def +demo-config+
  (common/make-config
   {:tag "test.demo-make",
    :container
    "ghcr.io/zcaudate-xyz/test.demo-make",
    :build ".build/test.demo-make",
    :github
    {:repo "zcaudate-xyz/test.demo-make",
     :private true,
     :description "Demo Makefile"},
    :sections {}
    :default
    [{:type :raw,
      :file "hello.md",
      :main
      ["# hello world"]}]}))

(def +triggers+ (atom {:a 1}))

^{:refer std.make.common/with:triggers :added "4.0"}
(fact "purges triggers"
  ^:hidden

  (def +triggers+ (atom {:a 1}))
  (common/with:triggers [+triggers+]
    @common/*triggers*)
  => {:a 1})

^{:refer std.make.common/triggers-purge :added "4.0"}
(fact "purges triggers"
  ^:hidden

  (def +triggers+ (atom {:a 1}))
  (common/with:triggers [+triggers+]
    (common/triggers-purge)
    @+triggers+)
  => {})

^{:refer std.make.common/triggers-set :added "4.0"}
(fact "sets a trigger"
  ^:hidden

  (common/with:triggers
   [+triggers+]
   (common/triggers-set +demo-config+
                        ["hello"]))
  => map?)

^{:refer std.make.common/triggers-clear :added "4.0"}
(fact "clears a trigger"
  ^:hidden
  
  (common/with:triggers
   [+triggers+]
   (common/triggers-clear +demo-config+))
  => empty?)

^{:refer std.make.common/triggers-get :added "4.0"
  :setup [(common/with:triggers
           [+triggers+]
           (common/triggers-set +demo-config+
                                ["hello"]))]}
(fact "gets a trigger"
  ^:hidden
  
  (common/with:triggers
   [+triggers+]
   (common/triggers-get +demo-config+))
  => ["hello"])

^{:refer std.make.common/triggers-list :added "4.0"}
(fact "lists all trigers"
  ^:hidden

  (common/with:triggers
   [+triggers+]
   (common/triggers-list))
  => {"test.demo-make" ["hello"]})

^{:refer std.make.common/get-triggered :added "4.0"}
(fact "gets all configs given a trigger namespace"

  (mapv (fn [mcfg]
          (:tag @(:instance mcfg)))
        (common/with:triggers
         [+triggers+]
         (common/get-triggered 'hello)))
  => ["test.demo-make"])

^{:refer std.make.common/with:internal-shell :added "4.0"}
(fact "with internal shell")

^{:refer std.make.common/make-config? :added "4.0"}
(fact "checks that object is a `make` config"
  ^:hidden

  (common/make-config? +demo-config+)
  => true)

^{:refer std.make.common/get-config-tag :added "4.0"}
(fact "gets the tag for a config"
  ^:hidden
  
  (common/get-config-tag +demo-config+)
  => "test.demo-make")


^{:refer std.make.common/make-config-map :added "4.0"}
(fact "creates a make-config map"
  ^:hidden
  
  (common/make-config-map
   {:tag "test.demo-make",
    :container
    "ghcr.io/zcaudate-xyz/test.demo-make",
    :build ".build/test.demo-make",
    :github
    {:repo "zcaudate-xyz/test.demo-make",
     :private true,
     :description "Demo Makefile"},
    :sections {}
    :default
    [{:type :raw,
      :file "hello.md",
      :main
      ["# hello world"]}]})
  => (contains-in
      {:github
       {:repo "zcaudate-xyz/test.demo-make",
        :private true,
        :description "Demo Makefile"},
       :sections {},
       :default
       [{:type :raw, :file "hello.md", :main ["# hello world"]}],
       :orgfile "Main.org",
       :ns 'std.make.common-test,
       :build ".build/test.demo-make",
       :root string?
       :container "ghcr.io/zcaudate-xyz/test.demo-make",
       :tag "test.demo-make"}))

^{:refer std.make.common/make-config :added "4.0"}
(fact "function to create a `make` config"
  ^:hidden
  
  (common/make-config
   {:tag "test.demo-make",
    :container
    "ghcr.io/zcaudate-xyz/test.demo-make",
    :build ".build/test.demo-make",
    :github
    {:repo "zcaudate-xyz/test.demo-make",
     :private true,
     :description "Demo Makefile"},
    :sections {}
    :default
    [{:type :raw,
      :file "hello.md",
      :main
      ["# hello world"]}]})
  => common/make-config?)

^{:refer std.make.common/make-config-update :added "4.0"}
(fact "updates the make-config")

^{:refer std.make.common/make-dir :added "4.0"}
(fact "gets the dir specified by the config"

  (common/make-dir +demo-config+)
  => string?)

^{:refer std.make.common/make-run :added "4.0"}
(fact "runs the `make` executable"
  ^:hidden
  
  (compile/compile +demo-config+)
  (common/with:internal-shell
   (common/make-run +demo-config+ :dev))
  => (contains-in
      [number? true]))

^{:refer std.make.common/make-run-internal :added "4.0"}
(fact "runs the make executable internal"
  ^:hidden
  
  (compile/compile +demo-config+)
  (common/make-run-internal +demo-config+
                            :dev
                            :test)
  => (contains-in
      [[number? true]
       [number? true]]))

^{:refer std.make.common/make-shell :added "4.0"}
(fact "opens a terminal at the location of the `make` directory")

^{:refer std.make.common/make-dir-setup :added "4.0"}
(fact "sets up the `make` directory"
  ^:hidden
  
  (str (common/make-dir-setup
        +demo-config+))
  => (common/make-dir +demo-config+))

^{:refer std.make.common/make-dir-exists? :added "4.0"}
(fact "checks that the `make` directory exists"
  ^:hidden

  (str (common/make-dir-setup +demo-config+))
  => string?
  
  (common/make-dir-exists? +demo-config+)
  => true)

^{:refer std.make.common/make-dir-teardown :added "4.0"}
(fact "deletes the the `make` directory"
  ^:hidden

  (common/make-dir-teardown +demo-config+)
  => (contains-in [boolean?])
  
  (common/make-dir-exists? +demo-config+)
  => false)


^{:refer std.make.common/make-run-close :added "4.0"}
(fact "closes the make run window")
