(ns xt.db.base-util-test
  (:use code.test)
  (:require [std.lang :as l]))

(l/script- :js
  {:runtime :oneshot
   :config {:program :nodejs}
   :require [[xt.db.base-util :as ut]
             [xt.lang.base-lib :as k]]})

(l/script- :lua
  {:runtime :oneshot
   :require [[xt.db.base-util :as ut]
             [xt.lang.base-lib :as k]]})

(l/script- :python
  {:runtime :oneshot
   :require [[xt.db.base-util :as ut]
             [xt.lang.base-lib :as k]]})

^{:refer xt.db.base-util/collect-routes :added "4.0"
  :setup [(def +routes+ [{:input [],
                          :return "text",
                          :schema "core/util",
                          :id "ping",
                          :flags {}
                          :url "api/ping"}
                         {:input [{:symbol "input", :type "text"}],
                          :return "text",
                          :schema "core/util",
                          :id "echo",
                          :flags {}
                          :url "api/echo"}])
          (def +result+
            (contains-in {"api/echo"
                          {"url" "api/echo",
                           "return" "text",
                           "schema" "core/util",
                           "id" "echo",
                           "flags" {},
                           "input" [{"symbol" "input", "type" "text"}],
                           "type" "db"},
                          "api/ping"
                          {"url" "api/ping",
                           "return" "text",
                           "schema" "core/util",
                           "id" "ping",
                           "flags" {},
                           "type" "db"}}))]}
(fact "collect routes"
  ^:hidden
  
  (!.js
   (ut/collect-routes (@! +routes+) "db"))
  => +result+

  (!.lua
   (ut/collect-routes (@! +routes+) "db"))
  => +result+

  (!.py
   (ut/collect-routes (@! +routes+) "db"))
  => +result+)

^{:refer xt.db.base-util/collect-views :added "4.0"
  :setup [(def +views+
            [{:input [{:symbol "i_currency_id", :type "citext"}],
              :return "jsonb",
              :schema "core/application-support",
              :id "currency_default",
              :flags {:public true},
              :view {:table "Currency",
                     :type "return",
                     :tag "default",
                     :access {:query nil,
                              :roles {},
                              :relation nil,
                              :symbol nil},
                     :query ["*/data"],
                     :guards []}}
             {:input [],
              :return "jsonb",
              :schema "core/application-support",
              :id "currency_all",
              :flags {:public true},
              :view {:table "Currency",
                     :type "select",
                     :tag "all",
                     :access {:query nil,
                              :roles {},
                              :relation nil,
                              :symbol nil},
                     :query nil,
                     :guards []}}
             {:input [{:symbol "i_type", :type "text"}],
              :return "jsonb",
              :schema "core/application-support",
              :id "currency_by_type",
              :flags {:public true},
              :view {:table "Currency",
                     :type "select",
                     :tag "by_type",
                     :access {:query nil,
                              :roles {},
                              :relation nil,
                              :symbol nil},
                     :query {"type" "i_type"},
                     :guards []}}])]}
(fact "collect views into views structure"
  ^:hidden

  (!.js
   (ut/collect-views (@! +views+)))
  => (contains-in {"Currency" {"select" {"all" map?
                                         "by_type" map?}
                               "return" {"default" map?}}})

  (!.lua
   (ut/collect-views (@! +views+)))
  => (contains-in {"Currency" {"select" {"all" map?
                                         "by_type" map?}
                               "return" {"default" map?}}})

  (!.py
   (ut/collect-views (@! +views+)))
  => (contains-in {"Currency" {"select" {"all" map?
                                         "by_type" map?}
                               "return" {"default" map?}}}))

^{:refer xt.db.base-util/merge-views :added "4.0"}
(fact "merges multiple views together")

^{:refer xt.db.base-util/keepf-limit :added "4.0"}
(fact "keeps given limit"
  ^:hidden
  
  (!.js
   (ut/keepf-limit [1 2 3 4 5]
	           k/odd?
                   k/identity
                   3))
  => [1 3 5]
  
  (!.lua
   (ut/keepf-limit [1 2 3 4 5]
	           k/odd?
                   k/identity
                   3))
  => [1 3 5]
  
  (!.py
   (ut/keepf-limit [1 2 3 4 5]
	           k/odd?
                   k/identity
                   3))
  => [1 3 5])

^{:refer xt.db.base-util/lu-nested :added "4.0"}
(fact "helper for lu-map"
  ^:hidden
  
  (!.js
   (ut/lu-nested [{:id "1"}]
                 (fn:> [e] e.id)))
  => {"1" {"id" "1"}}

  (!.lua
   (ut/lu-nested [{:id "1"}]
                 (fn [e] (return e.id))))
  => {"1" {"id" "1"}}

  (!.py
   (ut/lu-nested [{:id "1"}]
                 (fn [e] (return (. e ["id"])))))
  => {"1" {"id" "1"}})

^{:refer xt.db.base-util/lu-map :added "4.0"}
(fact "constructs a nested lu map of ids"
  ^:hidden
  
  (!.js [(ut/lu-map
          {:a [{:id "b"}]})
         (ut/lu-map
          {:a [{:id "b"
                :sub [{:id "c"}
                      {:id "d"}]}]})])
  => [{"a" {"b" {"id" "b"}}}
      {"a" {"b" {"sub" {"d" {"id" "d"}, "c" {"id" "c"}}, "id" "b"}}}]
  
  (!.lua [(ut/lu-map
           {:a [{:id "b"}]})
          (ut/lu-map
           {:a [{:id "b"
                 :sub [{:id "c"}
                       {:id "d"}]}]})])
  => [{"a" {"b" {"id" "b"}}}
      {"a" {"b" {"sub" {"d" {"id" "d"}, "c" {"id" "c"}}, "id" "b"}}}]

  (!.py [(ut/lu-map
          {:a [{:id "b"}]})
         (ut/lu-map
          {:a [{:id "b"
                :sub [{:id "c"}
                      {:id "d"}]}]})])
  => [{"a" {"b" {"id" "b"}}}
      {"a" {"b" {"sub" {"d" {"id" "d"}, "c" {"id" "c"}}, "id" "b"}}}])


^{:refer xt.db.base-util/collect-routes-object :added "4.0"}
(fact "collects routes from an object input")