(ns code.manage.unit.scaffold
  (:require [std.fs :as fs]
            [code.project :as project]
            [std.block.navigate :as nav]
            [std.block :as block]
            [std.string :as str]
            [std.lib :as h]))

(def ^:dynamic *ns-form* 'code.test)

(def ^:dynamic *ns-form-fn*
  (fn [source-ns test-ns test-form]
    (format "(ns %s\n  (:use %s)\n  (:require [%s :refer :all]))"
            (str test-ns)
            (str test-form)
            (str source-ns))))

(defn test-fact-form
  "creates a fact form for the namespace
 
   (test-fact-form 'lucid 'hello \"1.1\")
   => \"^{:refer lucid/hello :added \\\"1.1\\\"}\\n(fact \\\"TODO\\\")\""
  {:added "1.2"}
  ([ns var version]
   (-> [(format "^{:refer %s/%s :added \"%s\"}"
                ns var version)
        (format "(fact \"TODO\")")]
       (str/joinl "\n"))))

(defn new-filename
  "creates a new file based on test namespace
 
   (new-filename 'lucid.hello-test (project/project) false)
   => (str (fs/path \"test/lucid/hello_test.clj\"))"
  {:added "3.0"}
  ([test-ns project write]
   (let [extension (project/file-suffix)
         path (format "%s/%s/%s"
                      (:root project)
                      (first (:test-paths project))
                      (-> test-ns str ^String (munge) (.replaceAll "\\." "/") (str extension)))]
     (when write
       (fs/create-directory (fs/parent path)))
     path)))

(defn scaffold-new
  "creates a completely new scaffold"
  {:added "1.2"}
  ([source-ns test-ns vars version]
   (->> (map #(test-fact-form source-ns % version) vars)
        (cons (*ns-form-fn* source-ns test-ns *ns-form*))
        (str/join "\n\n"))))

(defn scaffold-append
  "creates a scaffold for an already existing file"
  {:added "1.2"}
  ([original source-ns new-vars  version]
   (if new-vars
     (->> new-vars
          (map #(test-fact-form source-ns % version))
          (str/join "\n\n")
          (str original "\n\n"))
     original)))

(defn scaffold-arrange
  "arranges tests to match the order of functions in source file"
  {:added "3.0"}
  ([original source-vars]
   (let [source-lu   (set source-vars)
         all-nodes   (->> (nav/parse-root original)
                          (nav/down)
                          (iterate nav/right)
                          (take-while identity)
                          (map nav/block))
         key-var     #(-> % (block/children) first block/value :refer name symbol source-lu)
         is-ns?      #(-> % block/value first (= 'ns))
         is-var?     #(and (-> % block/tag (= :meta))
                           (key-var %))
         is-comment? #(-> % block/value first (= 'comment))
         ns-nodes    (->> all-nodes (filter is-ns?))
         var-table   (->> all-nodes
                          (filter is-var?)
                          (h/map-juxt [key-var identity]))
         var-nodes   (keep var-table source-vars)
         other-nodes (->> all-nodes (remove is-ns?) (remove is-var?))
         start-nodes (remove is-comment? other-nodes)
         end-nodes   (filter is-comment? other-nodes)]
     (->> (concat ns-nodes start-nodes var-nodes end-nodes)
          (map block/string)
          (str/join "\n\n")))))
