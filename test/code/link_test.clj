(ns code.link-test
  (:use code.test)
  (:require [code.link.common :as common]
            [code.link :refer :all]
            [std.fs :as fs]
            [code.project :as project]))

(def -lookups-   (create-file-lookups (project/project)))

(def -packages-  (read-packages))

^{:refer code.link/file-linkage :added "3.0"}
(fact "returns the exports and imports of a given file"

  (file-linkage "src/code/link/common.clj")
  => '{:exports #{[:class code.link.common.FileInfo]
                  [:clj code.link.common]},
       :imports #{[:clj std.fs]
                  [:clj std.lib]}})

^{:refer code.link/read-packages :added "3.0"}
(fact "reads a list of packages from a configuration file (e.g., 'config/packages.edn')"
  (-> (read-packages {:file "config/packages.edn"})
      (get 'xyz.zcaudate/std.lib))
  => (contains {:description string?
                :name 'xyz.zcaudate/std.lib}))

^{:refer code.link/create-file-lookups :added "3.0"}
(fact "creates file-lookups for clj, cljs and cljc files"

  (-> (create-file-lookups (project/project))
      (get-in [:clj 'std.lib.version]))
  => (str (fs/path "src/std/lib/version.clj")))

^{:refer code.link/collect-entries-single :added "3.0"}
(fact "collects all namespaces for given lookup and package"

  (collect-entries-single (get -packages- 'xyz.zcaudate/std.lib)
                          (:clj -lookups-))
  => coll?)

^{:refer code.link/collect-entries :added "3.0"}
(fact "collects all entries given packages and lookups"

  (-> (collect-entries -packages- -lookups-)
      (get-in '[xyz.zcaudate/std.lib :entries]))
  => coll?)

^{:refer code.link/overlapped-entries-single :added "3.0"}
(fact "finds any overlaps between entries"

  (overlapped-entries-single '{:name a
                               :entries #{[:clj hara.1]}}
                             '[{:name b
                                :entries #{[:clj hara.1] [:clj hara.2]}}])
  => '([#{a b} #{[:clj hara.1]}]))

^{:refer code.link/overlapped-entries :added "3.0"}
(fact "finds any overlapped entries for given map"

  (overlapped-entries '{a {:name a
                           :entries #{[:clj hara.1]}}
                        b {:name b
                           :entries #{[:clj hara.1] [:clj hara.2]}}})
  => '([#{a b} #{[:clj hara.1]}]))

^{:refer code.link/missing-entries :added "3.0"}
(fact "finds missing entries given packages and lookup"

  (missing-entries '{b {:name b
                        :entries #{[:clj hara.1] [:clj hara.2]}}}
                   '{:clj {hara.1 ""
                           hara.2 ""
                           hara.3 ""}})
  => '{:clj {hara.3 ""}})

^{:refer code.link/collect-external-deps :added "3.0"}
(fact "collects dependencies from the local system"

  (collect-external-deps '{:a {:dependencies [org.clojure/clojure]}})
  => (contains-in {:a {:dependencies [['org.clojure/clojure string?]]}}))

^{:refer code.link/collect-linkages :added "3.0"}
(fact "collects all imports and exports of a package"
  ^:hidden

  (-> -packages-
      (collect-entries -lookups-)
      (collect-linkages -lookups-)
      (get 'xyz.zcaudate/std.lib)
      (select-keys [:imports :exports]))
  => map?)

^{:refer code.link/collect-internal-deps :added "3.0"}
(fact "collects all internal dependencies"
  ^:hidden
  
  (-> -packages-
      (collect-entries -lookups-)
      (collect-linkages -lookups-)
      (collect-internal-deps)
      (get-in ['xyz.zcaudate/code.test :internal])
      sort)
  => '(xyz.zcaudate/code.project
       xyz.zcaudate/std.fs
       xyz.zcaudate/std.lib
       xyz.zcaudate/std.math
       xyz.zcaudate/std.pretty)

  (-> -packages-
      (collect-entries -lookups-)
      (collect-linkages -lookups-)
      (collect-internal-deps)
      (->> (std.lib/map-vals :internal))))

^{:refer code.link/collect-transfers :added "3.0"}
(fact "collects all files that are packaged"
  ^:hidden

  (-> -packages-
      (collect-entries -lookups-)
      (collect-linkages -lookups-)
      (collect-internal-deps)
      (collect-transfers -lookups- (project/project))
      (get-in '[xyz.zcaudate/std.lib :files])
      sort)
  => coll?)

^{:refer code.link/collect :added "3.0"}
(fact "collects comprehensive project information, including dependencies, exports, and imports, given package and file lookups"
  ^:hidden
  (-> (collect -packages- -lookups- (project/project))
      (get 'xyz.zcaudate/std.lib)
      ((comp sort keys)))
  => [:bundle :dependencies :description :entries :exports :files :imports :include :internal :name])

^{:refer code.link/make-project :added "3.0"}
(fact "makes a maven compatible project"

  (make-project)
  => map?)

^{:refer code.link/select-manifest :added "3.0"}
(fact "selects all related manifests"

  (select-manifest {:a {:internal #{:b}}
                    :b {:internal #{:c}}
                    :c {:internal #{}}
                    :d {:internal #{}}}
                   [:a])
  => {:a {:internal #{:b}}
      :b {:internal #{:c}}
      :c {:internal #{}}})

^{:refer code.link/all-linkages :added "4.0"}
(fact "retrieves all linkages for a given project"

  (all-linkages (make-project)))

^{:refer code.link/make-linkages :added "3.0"}
(fact "creates a map of linkages for a given project"

  (make-linkages (make-project))
  => map?)

(comment
  (./code:import))
