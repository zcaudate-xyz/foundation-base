(ns indigo.infra-check
  (:require [code.test :as test]
            [code.project :as project]
            [std.fs :as fs]
            [std.string :as str]
            [std.lib :as h]))

(def +base+
  '[std math])

(def +framework+
  '[code jvm script])

(def +lib+
  '[lib])

(def +infra+
  '[platform kmi rt])

(def +app+
  '[fx js])

(defn- test-fn
  ([inputs]
   (fn [] (test/run inputs))))

(def test:base             (test-fn +base+))
(def test:base+framework  (test-fn (concat +base+ +framework+)))
(def test:standard   (test-fn (concat +base+ +framework+ +lib+)))
(def test:framework  (test-fn +framework+))
(def test:infra      (test-fn +infra+))
(def test:app        (test-fn +app+))
(def test:all        (test-fn (concat +base+ +framework+ +lib+ +infra+ +app+)))

(defn tests-in-wrong-file
  "checks for tests in the wrong file
 
   (tests-in-wrong-file)"
  {:added "3.0"}
  ([]
   (->> (fs/select "test" {:include ["*.clj$"]})
        (map (juxt identity
                   fs/file-namespace
                   (comp symbol
                         #(apply str (replace  {\_ "-" \/ "."} %))
                         #(str/replace % #"\.clj$" "")
                         str
                         (partial fs/relativize (fs/path "test")))))
        (filter (fn [[_ x y]] (not= x y)))
        (vec))))

(defn to-test-path
  "ns to test path
 
   (to-test-path 'indigo-test)
   => \"test/code/dev_test.clj\""
  {:added "3.0"}
  ([ns]
   (h/->> (str ns)
          (replace {\- \_ \. \/})
          (apply str)
          (str "test/" % ".clj"))))

(defn fix-tests
  "fix tests that are in wrong file"
  {:added "3.0"}
  ([]
   (let [wrong-files (tests-in-wrong-file)]
     (mapv (fn [[old ns]]
             (let [new (to-test-path ns)]
               (fs/move old new)))
           wrong-files))))

(defn rename-tests
  "rename tests given namespaces
 
   (rename-tests 'hara.util.transform 'std.lib.stream.xform)"
  {:added "3.0"}
  ([from to]
   (let [from-ns   (symbol (str from "-test"))
         to-ns     (symbol (str to "-test"))
         from-path (fs/path (to-test-path from-ns))
         to-path   (fs/path (to-test-path to-ns))]
     (spit from-path
           (.replaceAll (slurp from-path)
                        (str (h/re-create (str from)))
                        (str to)))
     (fs/move from-path to-path))))

(defn rename-test-var
  "rename test vars
 
   (rename-test-var 'std.lib.class 'class:array:primitive? 'primitive:array?)
   (rename-test-var 'std.lib.return 'ret:resolve 'return-resolve)"
  {:added "3.0"}
  ([ns from to]
   (let [ns-path (fs/path (to-test-path (str ns "-test")))]
     (spit ns-path
           (.replaceAll (slurp ns-path)
                        (str (h/re-create (str from)))
                        (str to))))))


(comment
  (rename-test-var 'std.lib.class 'class:array:primitive? 'primitive:array?)
  (rename-test-var 'std.lib.return 'ret:resolve 'return-resolve)
  (rename-tests 'hara.util.transform 'std.lib.stream.xform)

  (fix-tests)
  (str (h/re-create "hara.util"))
  
  (partial fs/relativize (fs/path "test/hara"))
  (map (comp symbol
             #(apply str (replace  {\_ "-" \/ "."} %))
             #(str/replace % #"\.clj$" "")
             str
             (partial fs/relativize (fs/path "test"))
             first)
       +move-tests+))
