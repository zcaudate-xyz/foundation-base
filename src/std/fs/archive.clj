(ns std.fs.archive
  (:require [std.fs :as fs]
            [std.protocol.archive :as protocol.archive])
  (:import (java.net URI) (java.nio.file FileSystem FileSystems Files Paths) (jdk.nio.zipfs ZipFileSystem))
  (:refer-clojure :exclude [list remove]))

(def supported #{:zip :jar})

(extend-protocol protocol.archive/IArchive
  ZipFileSystem
  (-url    [archive]
    (str archive))

  (-path    [^ZipFileSystem archive ^String entry]
    (.getPath archive (str entry) ^"[Ljava.lang.String;" (make-array String 0)))

  (-list    [archive]
    (-> (protocol.archive/-path archive "/")
        (fs/select)))

  (-has?    [archive entry]
    (fs/exists? (protocol.archive/-path archive entry)))

  (-archive [archive root inputs]
    (->> inputs
         (map (juxt #(str (fs/relativize root %))
                    identity))
         (mapv (fn [[entry input]]
                 (protocol.archive/-insert archive (str entry) input)))))

  (-extract [archive output entries]
    (keep (fn [entry]
            (let [zip-path (protocol.archive/-path archive entry)
                  out-path (fs/path (str output) entry)]
              (when-not (fs/directory? zip-path)
                (fs/create-directory (fs/parent out-path))
                (fs/copy-single (protocol.archive/-path archive entry)
                                out-path
                                {:options [:replace-existing]}))))
          entries))

  (-insert  [archive entry input]
    (fs/copy-single (fs/path input)
                    (protocol.archive/-path archive entry)
                    {:options [:replace-existing]}))

  (-remove  [archive entry]
    (fs/delete (protocol.archive/-path archive entry)))

  (-write   [archive entry stream]
    (fs/write-into (protocol.archive/-path archive entry) stream))

  (-stream  [archive entry]
    (fs/input-stream (protocol.archive/-path archive entry))))

(extend-protocol protocol.archive/IArchive
  String
  (-url [archive]
    archive))

(defn zip-system?
  "checks if object is a `ZipSystem`
 
   (zip-system? (open \"test-scratch/hello.jar\"))
   => true"
  {:added "3.0"}
  ([obj]
   (= ZipFileSystem (type obj))))

(defn create
  "creats a zip file
 
   (fs/delete \"test-scratch/hello.jar\")
 
   (create \"test-scratch/hello.jar\")
   => zip-system?"
  {:added "3.0"}
  ([archive]
   (if (fs/exists? archive)
     (throw (ex-info "Archive already exists" {:path archive}))
     (let [path (fs/path archive)]
       (do (fs/create-directory (fs/parent path))
           (FileSystems/newFileSystem
            (URI. (str "jar:file:" path))
            {"create" "true"}))))))

(defn ^ZipFileSystem open
  "either opens an existing archive or creates one if it doesn't exist
 
   (open \"test-scratch/hello.jar\" {:create true})
   => zip-system?"
  {:added "3.0"}
  ([archive]
   (open archive {:create true}))
  ([archive opts]
   (cond (instance? FileSystem archive)
         archive

         :else
         (let [path (fs/path archive)]
           (cond (fs/exists? path)
                 (FileSystems/newFileSystem path)

                 (:create opts)
                 (create archive)

                 :else
                 (throw (ex-info "archive does not exist"
                                 {:path archive})))))))

(defn open-and
  "helper function for opening an archive and performing a single operation
 
   (->> (open-and \"test-scratch/hello.jar\" {:create false} #(protocol.archive/-list %))
        (map str))
   => [\"/\"]"
  {:added "3.0"}
  ([archive opts callback]
   (let [farchive (open archive opts)
         result   (callback farchive)]
     (if (string? archive) (.close farchive))
     result)))

(defn url
  "returns the url of the archive
 
   (url (open \"test-scratch/hello.jar\"))
   => (str (fs/path \"test-scratch/hello.jar\"))"
  {:added "3.0"}
  ([archive]
   (protocol.archive/-url archive)))

(defn path
  "returns the url of the archive
 
   (-> (open \"test-scratch/hello.jar\")
       (path \"world.java\")
       (str))
   => \"world.java\""
  {:added "3.0"}
  ([archive entry]
   (open-and archive {:create false} #(protocol.archive/-path % entry))))

(defn list
  "lists all the entries in the archive
 
   (map str (list \"test-scratch/hello.jar\"))
   => [\"/\"]"
  {:added "3.0"}
  ([archive]
   (open-and archive {:create false} protocol.archive/-list)))

(defn has?
  "checks if the archive has a particular entry
 
   (has? \"test-scratch/hello.jar\" \"world.java\")
   => false"
  {:added "3.0"}
  ([archive entry]
   (open-and archive {:create false} #(protocol.archive/-has? % entry))))

(defn archive
  "puts files into an archive
 
   (archive \"test-scratch/hello.jar\" \"src\")
   => coll?"
  {:added "3.0"}
  ([archive root]
   (let [ach (open archive)
         res (->> (fs/select root {:exclude [fs/directory?]})
                  (protocol.archive/-archive ach root))]
     (.close ach)
     res))
  ([archive root inputs]
   (protocol.archive/-archive (open archive) root inputs)))

(defn extract
  "extracts all file from an archive
 
   (extract \"test-scratch/hello.jar\")
   => coll?"
  {:added "3.0"}
  ([archive]
   (extract archive (fs/parent (url archive))))
  ([archive output]
   (extract archive output (list archive)))
  ([archive output entries]
   (protocol.archive/-extract (open archive {:create false}) output entries)))

(defn insert
  "inserts a file to an entry within the archive
 
   (open   \"test-scratch/hello.jar\" {:create true})
   (insert \"test-scratch/hello.jar\" \"project.clj\" \"project.clj\")
   => fs/path?"
  {:added "3.0"}
  ([archive entry input]
   (open-and archive {} #(protocol.archive/-insert % entry input))))

(defn remove
  "removes an entry from the archive
 
   (remove \"test-scratch/hello.jar\" \"project.clj\")
   => #{\"project.clj\"}"
  {:added "3.0"}
  ([archive entry]
   (open-and archive {:create false} #(protocol.archive/-remove % entry))))

(defn write
  "writes files to an archive
 
   (doto \"test-scratch/hello.jar\"
     (fs/delete)
     (open)
     (write \"test.stuff\"
            (binary/input-stream (.getBytes \"Hello World\"))))
 
   (slurp (stream (open \"test-scratch/hello.jar\") \"test.stuff\"))
   => \"Hello World\""
  {:added "3.0"}
  ([archive entry stream]
   (open-and archive {:create false} #(protocol.archive/-write % entry stream))))

(defn stream
  "creates a stream for an entry wthin the archive
 
   (do (insert \"test-scratch/hello.jar\" \"project.clj\" \"project.clj\")
       (slurp (stream \"test-scratch/hello.jar\" \"project.clj\")))
   => (slurp \"project.clj\")"
  {:added "3.0"}
  ([archive entry]
   (protocol.archive/-stream (open archive {:create false}) entry)))
