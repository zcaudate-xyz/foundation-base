(ns code.framework.link-test
  (:require [code.framework.link :refer :all]
            [code.framework.link.common :as common]
            [code.project :as project]
            [std.fs :as fs]
            [std.lib.collection :as collection])
  (:use code.test))

(def -lookups-   (create-file-lookups (project/project)))

(def -packages-  (read-packages))

^{:refer code.framework.link/file-linkage :added "3.0"}
(fact "returns the exports and imports of a given file"

  (file-linkage "src/code/framework/link/common.clj")
  => '{:exports #{[:class code.framework.link.common.FileInfo]
                  [:clj code.framework.link.common]},
        :imports #{[:clj std.fs]
                   [:clj std.lib.invoke]}})

^{:refer code.framework.link/read-packages :added "3.0"}
(fact "reads a list of packages from a configuration file (e.g., 'config/packages.edn')"
  (-> (read-packages {:file "config/packages.edn"})
      (get 'xyz.zcaudate/std.lib))
  => (contains {:description string?
                :name 'xyz.zcaudate/std.lib}))

^{:refer code.framework.link/create-file-lookups :added "3.0"}
(fact "creates file-lookups for clj, cljs and cljc files"

  (-> (create-file-lookups (project/project))
      (get-in [:clj 'std.lib.version]))
  => (str (fs/path "src/std/lib/version.clj")))

^{:refer code.framework.link/collect-entries-single :added "3.0"}
(fact "collects all namespaces for given lookup and package"
 
  (collect-entries-single (get -packages- 'xyz.zcaudate/std.lib)
                          (:clj -lookups-))
  => coll?)

^{:refer code.framework.link/collect-entries-single :added "4.0"}
(fact "supports subtree exclusions when collecting package entries"
  (->> (collect-entries-single {:include [[foo :exclude [foo.bar]]]}
                               '{foo ""
                                 foo.alpha ""
                                 foo.bar ""
                                 foo.bar.baz ""
                                 foo.baz ""})
       (set))
  => '#{foo foo.alpha foo.baz})

^{:refer code.framework.link/collect-entries :added "3.0"}
(fact "collects all entries given packages and lookups"
 
  (-> (collect-entries -packages- -lookups-)
      (get-in '[xyz.zcaudate/std.lib :entries]))
  => coll?)

^{:refer code.framework.link/collect-entries :added "4.0"}
(fact "splits runtime namespaces into dedicated packages"
  (let [entries          (collect-entries -packages- -lookups-)
        hara-entries     (set (get-in entries '[xyz.zcaudate/hara :entries]))
        postgres-entries (set (get-in entries '[xyz.zcaudate/postgres :entries]))
        solidity-entries (set (get-in entries '[xyz.zcaudate/solidity :entries]))
        nginx-entries    (set (get-in entries '[xyz.zcaudate/hara.runtime.nginx :entries]))
        graal-entries    (set (get-in entries '[xyz.zcaudate/rt.graal :entries]))
        jep-entries      (set (get-in entries '[xyz.zcaudate/rt.jep :entries]))
        redis-entries    (set (get-in entries '[xyz.zcaudate/rt.redis :entries]))]
    [(boolean (hara-entries [:clj 'hara.runtime.postgres]))
     (boolean (hara-entries [:clj 'hara.runtime.solidity]))
     (boolean (hara-entries [:clj 'hara.runtime.solidity.client]))
     (boolean (hara-entries [:clj 'hara.runtime.graal]))
     (boolean (hara-entries [:clj 'hara.runtime.jep]))
     (boolean (hara-entries [:clj 'hara.runtime.redis]))
     (boolean (hara-entries [:clj 'hara.runtime.nginx]))
     (boolean (hara-entries [:clj 'hara.runtime.nginx.config]))
     (boolean (hara-entries [:clj 'xt.lang.common-lib]))
     (boolean (postgres-entries [:clj 'postgres.core]))
     (boolean (postgres-entries [:clj 'hara.runtime.postgres]))
     (boolean (solidity-entries [:clj 'solidity.core]))
     (boolean (solidity-entries [:clj 'hara.runtime.solidity]))
     (boolean (solidity-entries [:clj 'hara.runtime.solidity.client]))
     (boolean (nginx-entries [:clj 'hara.runtime.nginx]))
     (boolean (nginx-entries [:clj 'hara.runtime.nginx.config]))
     (boolean (graal-entries [:clj 'hara.runtime.graal]))
     (boolean (jep-entries [:clj 'hara.runtime.jep]))
     (boolean (redis-entries [:clj 'hara.runtime.redis]))
     (contains? entries 'xyz.zcaudate/xtalk.lang)
     (contains? entries 'xyz.zcaudate/hara.runtime.solidity)])
  => [false false false false false false false false true true true true true true true true true true true false false])

^{:refer code.framework.link/overlapped-entries-single :added "3.0"}
(fact "finds any overlaps between entries"

  (overlapped-entries-single '{:name a
                               :entries #{[:clj hara.1]}}
                             '[{:name b
                                :entries #{[:clj hara.1] [:clj hara.2]}}])
  => '([#{a b} #{[:clj hara.1]}]))

^{:refer code.framework.link/overlapped-entries :added "3.0"}
(fact "finds any overlapped entries for given map"

  (overlapped-entries '{a {:name a
                           :entries #{[:clj hara.1]}}
                        b {:name b
                           :entries #{[:clj hara.1] [:clj hara.2]}}})
  => '([#{a b} #{[:clj hara.1]}]))

^{:refer code.framework.link/missing-entries :added "3.0"}
(fact "finds missing entries given packages and lookup"

  (missing-entries '{b {:name b
                        :entries #{[:clj hara.1] [:clj hara.2]}}}
                   '{:clj {hara.1 ""
                           hara.2 ""
                           hara.3 ""}})
  => '{:clj {hara.3 ""}})

^{:refer code.framework.link/collect-external-deps :added "3.0"}
(fact "collects dependencies from the local system"

  (collect-external-deps '{:a {:dependencies [org.clojure/clojure]}})
  => (contains-in {:a {:dependencies [['org.clojure/clojure string?]]}}))

^{:refer code.framework.link/collect-linkages :added "3.0"}
(fact "collects all imports and exports of a package"

  (-> -packages-
      (collect-entries -lookups-)
      (collect-linkages -lookups-)
      (get 'xyz.zcaudate/std.lib)
      (select-keys [:imports :exports]))
  => map?)

^{:refer code.framework.link/collect-internal-deps :added "3.0"}
(fact "collects all internal dependencies"

  (-> -packages-
      (collect-entries -lookups-)
      (collect-linkages -lookups-)
      (collect-internal-deps)
      (get-in ['xyz.zcaudate/code.test :internal])
      sort)
  => (contains '[xyz.zcaudate/code.project
                 xyz.zcaudate/std.fs
                 xyz.zcaudate/std.lib] :in-any-order :gaps-ok)

  (-> -packages-
      (collect-entries -lookups-)
      (collect-linkages -lookups-)
      (collect-internal-deps)
      (->> (collection/map-vals :internal))))

^{:refer code.framework.link/collect-transfers :added "3.0"}
(fact "collects all files that are packaged"

  (-> -packages-
      (collect-entries -lookups-)
      (collect-linkages -lookups-)
      (collect-internal-deps)
      (collect-transfers -lookups- (project/project))
      (get-in '[xyz.zcaudate/std.lib :files])
      sort)
  => coll?)

^{:refer code.framework.link/collect :added "3.0"}
(fact "collects comprehensive project information, including dependencies, exports, and imports, given package and file lookups"
  (-> (collect -packages- -lookups- (project/project))
      (get 'xyz.zcaudate/std.lib)
      ((comp sort keys)))
  => [:bundle :dependencies :description :entries :exports :files :imports :include :internal :name])

^{:refer code.framework.link/make-project :added "3.0"}
(fact "makes a maven compatible project"

  (make-project)
  => map?)

^{:refer code.framework.link/select-manifest :added "3.0"}
(fact "selects all related manifests"

  (select-manifest {:a {:internal #{:b}}
                    :b {:internal #{:c}}
                    :c {:internal #{}}
                    :d {:internal #{}}}
                   [:a])
  => {:a {:internal #{:b}}
      :b {:internal #{:c}}
      :c {:internal #{}}})

^{:refer code.framework.link/all-linkages :added "4.0"}
(fact "retrieves all linkages for a given project"

  (all-linkages (make-project))
  => map?)

^{:refer code.framework.link/make-linkages :added "3.0"}
(fact "creates a map of linkages for a given project"

  (make-linkages (make-project))
  => map?)

(comment
  (./code:import))
