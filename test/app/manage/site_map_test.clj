(ns app.manage.site-map-test
  (:require [clojure.java.io :as io]
            [app.manage.site-map :as site])
  (:use code.test))

(def User {:name "User" :columns []})
(def ping {:name "ping" :inputs [] :output :text})
(def save {:name "save" :inputs [] :output :jsonb})
(def REGISTRY
  {:seed-namespaces ['example.seed] :group-namespaces ['example.groups]
   :resolve {'example.seed/User User 'example.seed/ping ping
             'example.groups/PAGE {"id" "main" "models" {}
                                    "actions" {"save" {"rpc" {"id" "save"}}}}}
   :rpc? #(contains? % :inputs) :get-rpc {"ping" ping "save" save}
   :rpc-spec #(hash-map :id (:name %) :schema "example_rpc")
   :schema (constantly {"User" {}}) :table-schema (constantly "example_type")})

(defn with-source [source f]
  (let [file (java.io.File/createTempFile "app-page-" ".clj")]
    (try (spit file source) (f file) (finally (.delete file)))))

^{:refer app.manage.site-map/read-page :added "4.1"}
(fact "reads aliases and requirements while leaving executable page forms untouched"
  (with-source
    "(ns never.load (:require [example.seed :as seed]))
     (def SITE-MAP-REQUIREMENTS {:tables [seed/User] :rpcs [seed/ping]})
     (throw (Exception. \"Never run a page\"))"
    (fn [path]
      (let [{:keys [aliases requirements]} (site/read-page path)]
        (site/resolve-requirements REGISTRY requirements aliases))))
  => {:tables [User] :rpcs [ping]}
  (with-source "#=(System/setProperty \"manage.reader.executed\" \"yes\")"
    (fn [path] (try (site/read-page path) :accepted (catch Exception _ :rejected))))
  => :rejected
  (System/getProperty "manage.reader.executed") => nil)

^{:refer app.manage.site-map/resolve-requirements :added "4.1"}
(fact "group actions contribute RPCs; wrong-kind and unregistered references fail"
  (site/resolve-requirements REGISTRY {:groups ['groups/PAGE]} {'groups 'example.groups})
  => {:tables [] :rpcs [save]}
  (mapv (fn [[kind ref]]
          (try (site/resolve-reference REGISTRY kind {'seed 'example.seed} ref)
               :accepted (catch clojure.lang.ExceptionInfo _ :rejected)))
        [[:tables 'seed/ping] [:rpcs 'seed/User] [:tables 'other/User]])
  => [:rejected :rejected :rejected]
  (try (site/resolve-requirements REGISTRY {:rpcs '(seed/ping)} {})
       :accepted (catch clojure.lang.ExceptionInfo _ :rejected))
  => :rejected)

^{:refer app.manage.site-map/table-order :added "4.1"}
(fact "closes dependencies and terminates on cycles"
  (site/table-order
   {"A" {"b" {"type" "ref" "ref" {"type" "forward" "ns" "B"}}}
    "B" {"a" {"type" "ref" "ref" {"type" "forward" "ns" "A"}}}
    "Private" {}} ["A"])
  => ["B" "A"]
  (try (site/table-order {} ["Unknown"])
       :accepted (catch clojure.lang.ExceptionInfo e (ex-data e)))
  => {:table "Unknown"})

^{:refer app.manage.site-map/publication :added "4.1"}
(fact "publishes only each site's requirements with registered schemas"
  (let [data (site/publication REGISTRY {:site "public"}
                              [{:namespace 'public.index :requirements {:tables [] :rpcs [ping]}}])]
    [(get-in data [:manifest "pages"]) (:schema data) (:lookup data) (:rpc data)])
  => [['public.index] {} {} {"ping" {:id "ping" :schema "example_rpc"}}]
  (let [data (site/publication REGISTRY {:site "admin"}
                              [{:namespace 'admin.users :requirements {:tables [User] :rpcs []}}])]
    [(get-in data [:manifest "tables"]) (:lookup data) (:rpc data)])
  => [["User"] {"User" {"position" 0 "schema" "example_type"}} {}])

^{:refer app.manage.site-map/build-sections :added "4.1"}
(fact "std.make sections share a single deferred publication"
  (let [calls (atom 0)
        sections (site/build-sections #(do (swap! calls inc) {:manifest {"version" 1}}) {})
        before @calls
        outputs (mapv #((:main %)) sections)]
    [before @calls (mapv :file sections) outputs])
  => [0 1 ["manifest.json" "schema.json" "lookup.json" "rpc.json"] [{"version" 1} nil nil nil]])
