(ns std.config.resolve-test
  (:use code.test)
  (:require [std.config.resolve :refer :all]
            [std.config.secure :as secure]
            [std.lib.encode :as encode]
            [std.lib.security :as security]
            [std.config.ext.gpg])
  (:refer-clojure :exclude [resolve load]))

(def -key- (secure/resolve-key "3YO19A4QAlRLDc8qmhLD7A=="))

^{:refer std.config.resolve/ex-config :added "3.0"}
(fact "helper function for creating config exceptions"

  (ex-config "error" {}) => clojure.lang.ExceptionInfo)

^{:refer std.config.resolve/directive? :added "3.0"}
(fact "checks if a form is a directive"

  (directive? "hello") => false

  (directive? [:eval '(+ 1 2 3)]) => true)

^{:refer std.config.resolve/resolve-directive :added "3.0"}
(fact "resolves directives with error handling"

  (resolve-directive [:merge {:a 1} {:b 2}])
  => {:a 1, :b 2})

^{:refer std.config.resolve/resolve :added "3.0"}
(fact "helper function for resolve"

  (resolve 1) => 1

  (resolve [:str ["hello" " " "there"]])
  => "hello there")

^{:refer std.config.resolve/resolve-type :added "3.0"}
(fact "converts a string to a type"

  (resolve-type :text "3")
  => "3"

  (resolve-type :edn "3")
  => 3)

^{:refer std.config.resolve/resolve-select :added "3.0"}
(fact "resolves selected sections of map data"

  (resolve-select {:a {:b 1 :c 2}}
                  {:in   [:a]
                   :keys [:b]})
  => {:b 1} ^:hidden

  (resolve-select {:a {:b 1 :c 2}}
                  {:in      [:a]
                   :exclude [:c]})
  => {:b 1})

^{:refer std.config.resolve/resolve-content :added "3.0"}
(fact "resolves directive into content"

  (resolve-content "hello" {}) => "hello"

  (resolve-content "hello" {:type :edn}) => 'hello

  (resolve-content "yzRQQ+R5tWYeg7DNp9wvvA=="
                   {:type :edn
                    :secured true
                    :key "Bz6i3dhp912F8+P82v1tvA=="})
  => {:a 1, :b 2})

^{:refer std.config.resolve/resolve-directive-properties :added "3.0"}
(fact "reads from system properties"

  (resolve-directive-properties [:properties "user.name"])
  ;; "chris"
  => string?)

^{:refer std.config.resolve/resolve-directive-env :added "3.0"}
(fact "reads from system env"

  (resolve-directive-env [:env "HOME"])
  ;; "/Users/chris"
  => string?)

^{:refer std.config.resolve/resolve-directive-project :added "3.0"}
(fact "reads form the project"

  (resolve-directive-project [:project :name])
  => symbol?)

^{:refer std.config.resolve/resolve-directive-format :added "3.0"}
(fact "formats values to form a string"

  (resolve-directive-format [:format ["http://%s:%s" "localhost" "8080"]])
  => "http://localhost:8080")

^{:refer std.config.resolve/resolve-directive-str :added "3.0"}
(fact "joins values to form a string"

  (resolve-directive-str [:str ["hello"
                                [:env "SPACE" {:default " "}]
                                "world"]])
  => "hello world")

^{:refer std.config.resolve/resolve-directive-or :added "3.0"}
(fact "reads from the first non-nil value"

  (resolve-directive-or [:or [:env "SERVER"] "<server>"])
  => "<server>")

^{:refer std.config.resolve/resolve-directive-case :added "3.0"}
(fact "switches from the selected element"

  (System/setProperty "std.config.profile" "dev")

  (resolve-directive-case
   [:case [:properties "std.config.profile"]
    "public" "public-profile"
    "dev"    "dev-profile"])
  => "dev-profile")

^{:refer std.config.resolve/resolve-directive-error :added "3.0"}
(fact "throws an error with message"

  (resolve-directive-error [:error "errored" {}])
  => (throws clojure.lang.ExceptionInfo))

^{:refer std.config.resolve/resolve-directive-merge :added "3.0"}
(fact "merges two entries together"

  (resolve-directive-merge [:merge
                            {:a {:b 1}}
                            {:a {:c 2}}])
  => {:a {:c 2}} ^:hidden

  (resolve-directive-merge [:merge :nil
                            {:a {:b 1}}
                            {:a {:c 2}}])
  => {:a {:b 1}}

  (resolve-directive-merge [:merge :nested
                            {:a {:b 1}}
                            {:a {:c 2}}])
  => {:a {:b 1, :c 2}}

  (resolve-directive-merge [:merge :nested-nil
                            {:a {:b 1}}
                            {:a {:b 2 :c 2}}])
  => {:a {:b 1, :c 2}})

^{:refer std.config.resolve/resolve-directive-eval :added "3.0"}
(fact "evaluates a config form"

  (System/setProperty "std.config.items" "3")

  (resolve-directive-eval [:eval '(+ 1 2
                                     [:properties "std.config.items"
                                      {:type :edn}])])
  => 6)

^{:refer std.config.resolve/resolve-map :added "3.0"}
(fact "resolves a map"

  (resolve-map {:a {:b [:eval "hello"]}}
               [:a :b])
  => "hello")

^{:refer std.config.resolve/resolve-directive-root :added "3.0"}
(fact "resolves a directive from the config root"

  (binding [*current* [{:a {:b [:eval "hello"]}}]]
    (resolve-directive-root [:root [:a :b]]))
  => "hello")

^{:refer std.config.resolve/resolve-directive-parent :added "3.0"}
(fact "resolves a directive from the map parent"

  (binding [*current* [{:a {:b [:eval "hello"]}}
                       {:b [:eval "hello"]}]]
    (resolve-directive-parent [:parent :b]))
  => "hello")

^{:refer std.config.resolve/resolve-directive-global :added "3.0"}
(fact "resolves a directive form the global map"

  (resolve-directive-global [:global :user.name])
  => string?)

^{:refer std.config.resolve/resolve-path :added "3.0"}
(fact "helper function for file resolvers"

  (resolve-path ["config/" [:eval "hello"]])
  => "config/hello")

^{:refer std.config.resolve/resolve-directive-file :added "3.0"}
(fact "resolves to file content"

  (resolve-directive-file
   [:file "test-data/std.config/hello.clear"])
  => "hello" ^:hidden

  (resolve-directive-file
   [:file "test-data/std.config/hello.encrypted" {:secured true :key -key-}])
  => "hello")

^{:refer std.config.resolve/resolve-directive-resource :added "3.0"}
(fact "resolves to resource content"

  (resolve-directive-resource
   [:resource "std.config/hello.clear"])
  => "hello" ^:hidden

  (resolve-directive-resource
   [:resource "std.config/hello.encrypted" {:secured true :key -key-}])
  => "hello")

^{:refer std.config.resolve/resolve-directive-include :added "3.0"}
(fact "resolves to either current project or config directory"

  (resolve-directive-include
   [:include "test-data/std.config/hello.clear" {:type :edn}])
  => 'hello)

^{:refer std.config.resolve/load :added "3.0"}
(fact "helper function for file resolvers"

  (load "test-data/std.config/config.edn")
  => map?)
