(ns std.fs.api
  (:require [std.fs.common :as common]
            [std.fs.attribute :as attr]
            [std.fs.walk :as walk]
            [std.fs.path :as path])
  (:refer-clojure :exclude [list])
  (:import (java.nio.file Files Path CopyOption
                          DirectoryNotEmptyException)
           (java.nio.file.attribute FileAttribute)))


(defn create-directory
  "creates a directory on the filesystem
 
   (do (create-directory \"test-scratch/.hello/.world/.foo\")
       (path/directory? \"test-scratch/.hello/.world/.foo\"))
   => true
 
   (delete \"test-scratch/.hello\")"
  {:added "3.0"}
  ([path]
   (create-directory path {}))
  ([path attrs]
   (Files/createDirectories (path/path path)
                            (attr/map->attr-array attrs))))

(defn create-symlink
  "creates a symlink to another file
 
   (do (create-symlink \"test-scratch/project.lnk\" \"project.clj\")
       (path/link? \"test-scratch/project.lnk\"))
   => true"
  {:added "3.0"}
  ([path link-to]
   (create-symlink path link-to {}))
  ([path link-to attrs]
   (Files/createSymbolicLink (path/path path)
                             (path/path link-to)
                             (attr/map->attr-array attrs))))

(defn create-tmpfile
  "creates a tempory file
 
   (create-tmpfile)
   ;;#file:\"/var/folders/rc/4nxjl26j50gffnkgm65ll8gr0000gp/T/tmp2270822955686495575\"
   => java.io.File"
  {:added "3.0"}
  ([]
   (java.io.File/createTempFile "tmp" ""))
  ([contents]
   (let [f (create-tmpfile)]
     (spit f contents)
     f)))

(defn create-tmpdir
  "creates a temp directory on the filesystem
 
   (create-tmpdir)
   ;;=> #path:\"/var/folders/d6/yrjldmsd4jd1h0nm970wmzl40000gn/T/4870108199331749225\""
  {:added "3.0"}
  ([]
   (create-tmpdir ""))
  ([prefix]
   (Files/createTempDirectory prefix (make-array FileAttribute 0))))

(defn select
  "selects all the files in a directory
 
   (->> (select \"src/std/fs\")
        (map #(path/relativize \"std/fs\" %))
       (map str)
        (sort))"
  {:added "3.0"}
  ([root]
   (select root nil))
  ([root opts]
   (walk/walk root opts)))

(defn list
  "lists the files and attributes for a given directory
 
   (list \"src\")
 
   (list \"../hara/src/std/fs\" {:recursive true})"
  {:added "3.0"}
  ([root] (list root {}))
  ([root opts]
   (let [gather-fn (fn [{:keys [path attrs accumulator]}]
                     (swap! accumulator
                            assoc
                            (str path)
                            (str (path/permissions path) "/" (path/typestring path))))]
     (walk/walk root
                (merge {:depth 1
                        :directory gather-fn
                        :file gather-fn
                        :accumulator (atom {})
                        :accumulate #{}
                        :with #{}}
                       opts)))))

(defn copy
  "copies all specified files from one to another
 
   (copy \"src\" \".src\" {:include [\".clj\"]})
   => map?
 
   (delete \".src\")"
  {:added "3.0"}
  ([source target]
   (copy source target {}))
  ([source target opts]
   (let [copy-fn (fn [{:keys [root path attrs target accumulator simulate]}]
                   (let [rel   (.relativize ^Path root path)
                         dest  (.resolve ^Path target rel)
                         ^"[Ljava.nio.file.CopyOption;" copts (->> [:copy-attributes :nofollow-links]
                                                                   (or (:options opts))
                                                                   (mapv common/option)
                                                                   (into-array CopyOption))]
                     (when-not simulate
                       (Files/createDirectories (.getParent dest) attr/*empty*)
                       (when (not (path/directory? dest))
                         (Files/copy ^Path path ^Path dest copts)))
                     (swap! accumulator
                            assoc
                            (str path)
                            (str dest))))]
     (walk/walk source
                (merge {:target (path/path target)
                        :directory copy-fn
                        :file copy-fn
                        :with #{:root}
                        :accumulator (atom {})
                        :accumulate #{}}
                       opts)))))

(defn copy-single
  "copies a single file to a destination
 
   (copy-single \"project.clj\"
                \"test-scratch/project.clj.bak\"
                {:options #{:replace-existing}})
   => (path/path \".\" \"test-scratch/project.clj.bak\")
 
   (delete \"test-scratch/project.clj.bak\")"
  {:added "3.0"}
  ([source target]
   (copy-single source target {}))
  ([source target opts]
   (if-let [dir (path/parent target)]
     (if-not (path/exists? dir)
       (create-directory dir)))
   (let [^"[Ljava.nio.file.CopyOption;" opts (->> (:options opts)
                                                  (mapv common/option)
                                                  (into-array CopyOption))]
     (Files/copy (path/path source)
                 (path/path target)
                 opts))))

(defn copy-into
  "copies a single file to a destination
 
   (copy-into \"src/std/fs.clj\"
              \"test-scratch/std.fs\")
   => vector?
 
   (delete \"test-scratch/std.fs\")"
  {:added "3.0"}
  ([source target]
   (copy-into source target {:include [path/file?]}))
  ([source target opts]
   (let [source-root  (path/path source)
         target-root  (path/path target)
         files (select source-root (select-keys opts [:include]))]
     (mapv (fn [source]
             (let [path    (path/relativize source-root source)
                   target  (path/path target-root path)]
               (copy-single source target (select-keys opts [:options]))))
           files))))

(defn delete
  "copies all specified files from one to another
 
   (do (copy \"src/std/fs.clj\" \".src/std/fs.clj\")
       (delete \".src\" {:include [\"fs.clj\"]}))
   => #{(str (path/path \".src/std/fs.clj\"))}
 
   (delete \".src\")
   => set?"
  {:added "3.0"}
  ([root] (delete root {}))
  ([root opts]
   (let [delete-fn (fn [{:keys [path attrs accumulator simulate]}]
                     (try (if-not simulate
                            (Files/delete path))
                          (swap! accumulator conj (str path))
                          (catch DirectoryNotEmptyException e)))]
     (walk/walk root
                (merge {:directory {:post delete-fn}
                        :file delete-fn
                        :with #{:root}
                        :accumulator (atom #{})
                        :accumulate #{}}
                       opts)))))

(defn move
  "moves a file or directory
 
   (do (move \"shortlist\" \".shortlist\")
       (move \".shortlist\" \"shortlist\"))
 
   (move \".non-existent\" \".moved\")
   => {}"
  {:added "3.0"}
  ([source target]
   (move source target {}))
  ([source target opts]
   (let [move-fn (fn [{:keys [root path attrs target accumulator simulate]}]
                   (let [rel   (.relativize ^Path root path)
                         dest  (.resolve ^Path target rel)
                         ^"[Ljava.nio.file.CopyOption;" copts (->> [:atomic-move]
                                                                   (or (:options opts))
                                                                   (mapv common/option)
                                                                   (into-array CopyOption))]
                     (when-not simulate
                       (Files/createDirectories (.getParent dest) attr/*empty*)
                       (Files/move ^Path path ^Path dest copts))
                     (swap! accumulator
                            assoc
                            (str path)
                            (str dest))))
         results (walk/walk source
                            (merge {:target (path/path target)
                                    :recursive true
                                    :directory {:post (fn [{:keys [path]}]
                                                        (if (path/empty-directory? path)
                                                          (delete path opts)))}
                                    :file move-fn
                                    :with #{:root}
                                    :accumulator (atom {})
                                    :accumulate #{}}
                                   opts))]
     results)))
