(ns concat-files
  (:require [std.fs :as fs]
            [clojure.java.io :as io]))

(def +all-files+
  (sort (keys (fs/list "src" {:recursive true
                              :include [".clj$"]}))))

(defn concat-files
  [& [file-filter out-file dir root]]
  (let [out-file  (or out-file "output.clj")
        root      (or root ".")
        root-path (fs/path root)
        dir       (or dir "src")
        proj-file "project.clj"]
    (with-open [writer (io/writer out-file)]
      (let [all-paths  (cons (str (fs/path root proj-file))
                             (sort (keys (fs/list (fs/path root dir)
                                                  {:recursive true
                                                   :include [(or file-filter ".clj$")]}))))]
        
        (doseq [p all-paths]
          (let [relative-path (fs/relativize root-path p)]
            (.write writer (str "\n\n\n;;; ------- " relative-path "------- \n\n\n"))
            (try (with-open [r (io/reader p)]
                   (doseq [line (line-seq r)]
                     (.write writer ^String line)
                     (.write writer "\n")))
                 (catch Throwable t))))))))

(comment
  (concat-files)
  
  (fs/list (fs/path "test")
           {:recursive true
            :include [".clj$"]})
  (concat-files nil
                "output-xtalk.clj"
                "src/xt")
  (concat-files nil
                "output-tests.clj"
                "test")


  (concat-files ".clj$"
                "statsdb-src.clj"
                "../statstrade-core/src/statsdb"
                "../statstrade-core")
  
  (concat-files ".clj$"
                "statsdb-test.clj"
                "../statstrade-core/test/statsdb"
                "../statstrade-core")

  (concat-files ".clj$"
                "statsapi-src.clj"
                "../statstrade-core/src/statsapi"
                "../statstrade-core")
  
  (concat-files ".clj$"
                "statsapi-test.clj"
                "../statstrade-core/test/statsapi"
                "../statstrade-core")

  
  (concat-files ".clj$"
                "statsnet-src.clj"
                "../statstrade-core/src/statsnet"
                "../statstrade-core")
  
  (concat-files ".clj$"
                "statsnet-test.clj"
                "../statstrade-core/test/statsnet"
                "../statstrade-core")

    
  (concat-files ".clj$"
                "statslink-src.clj"
                "../statstrade-core/src/statslink"
                "../statstrade-core")
  
  (concat-files ".clj$"
                "statslink-test.clj"
                "../statstrade-core/test/statslink"
                "../statstrade-core")

  
  )
