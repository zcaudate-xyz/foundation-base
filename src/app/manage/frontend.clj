(ns app.manage.frontend
  "Reproducible static Next.js frontend build; all application behaviour is DSL-authored."
  (:require [app.manage.make :as make] [clojure.java.io :as io] [clojure.data.json :as json]
            [app.manage.process :as process]))
(defn generate! [directory]
  (let [root (io/file directory)
        files (make/render-sections {:root root}
                {:load ['app.manage.workspace]
                 :sections [{:type :module.single :lang :js :main 'app.manage.workspace
                             :file "app/workspace.jsx" :header "\"use client\";\nimport {loader} from '@monaco-editor/react';loader.config({paths:{vs:'/monaco/vs'}});"
                             :emit {:code {:label false}}}]})]
    (doseq [[path content] (merge files
                           {"package.json" (json/write-str {"private" true "scripts" {"build" "next build --webpack"}
                             "dependencies" {"next" "16.4.6" "react" "19.3.1" "react-dom" "19.3.1"
                                             "@monaco-editor/react" "4.7.0" "monaco-editor" "0.52.2"}})
                            "next.config.mjs" "export default {output:'export',images:{unoptimized:true}};"
                            "package-lock.json" (slurp (io/resource "assets/app.manage/frontend-lock.json"))
                            "app/layout.jsx" "import './style.css';export default function Layout({children}){return <html lang='en'><body>{children}</body></html>}"
                            "app/page.jsx" "'use client';import dynamic from 'next/dynamic';const Workspace=dynamic(()=>import('./workspace').then(m=>m.Workspace),{ssr:false});export default function Page(){return <Workspace/>}"
                            "app/style.css" (slurp (io/resource "assets/app.manage/workspace.css"))})]
      (let [file (io/file root path)] (io/make-parents file) (spit file content)))
    (str root)))
(defn publish! [directory]
  (let [destination (io/file "resources/assets/app.manage/next")
        modules (io/file directory "node_modules")
        sources (concat [[(io/file directory "out") ""]
                         [(io/file modules "monaco-editor/min/vs") "monaco/vs/"]]
                        (for [file (file-seq modules)
                              :when (and (.isFile file)
                                         (re-matches #"(?i)(licen[cs]e|copying|notice)(\.(txt|md))?" (.getName file)))]
                          [file (str "licenses/" (.relativize (.toPath modules) (.toPath file)))]))
        paths (atom [])]
    (when-not (and (.isFile (io/file directory "out/index.html"))
                   (.isFile (io/file directory "node_modules/monaco-editor/min/vs/editor/editor.main.js")))
      (throw (ex-info "Publish requires a successful static build and local Monaco assets" {})))
    (when (.exists destination)
      (doseq [file (reverse (file-seq destination))]
        (when-not (.delete file) (throw (ex-info "Could not replace generated workbench asset" {:path (str file)})))))
    (doseq [[root prefix] sources file (file-seq root) :when (.isFile file)]
      (let [path (str prefix (.relativize (.toPath root) (.toPath file)))
            target (io/file destination path)]
        (io/make-parents target)
        (java.nio.file.Files/copy (.toPath file) (.toPath target)
          (into-array java.nio.file.CopyOption [java.nio.file.StandardCopyOption/REPLACE_EXISTING]))
        (swap! paths conj path)))
    (spit (io/file destination "manifest.edn") (pr-str (vec (sort @paths))))
    (count @paths)))
(defn -main [directory & [action]]
  (println (case action
    "publish" (publish! directory)
    "build" (do (generate! directory)
                (doseq [command [["npm" "ci" "--ignore-scripts" "--no-audit" "--no-fund"] ["npm" "run" "build"]]]
                  (let [result (process/run {:root directory :command command :timeout-ms 300000})]
                    (when-not (= :ok (:status result)) (throw (ex-info "Workbench frontend build failed" result)))))
                (publish! directory))
    (generate! directory)))
  (System/exit 0))
