^{:no-test true}
(ns js.webpack
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.core :as j]]
   :static  {:lang/no-lint true}})

(def.js webpack (require "webpack"))

(def.js path (require "path"))

(def.js jsx-rule
  {:test #"\.jsx?$"
   :use [{:loader "cache-loader"}
         {:loader "@sucrase/webpack-loader"
          :options {:transforms ["jsx"]}}]})

(def.js node-rule
  {:test #"\.node$"
   :use "node-loader"})

(def.js native-rule
  {:test #"\.(m?js|node)$"
   :parser {:amd false}
   :use
   {:loader "@marshallofsound/webpack-asset-relocator-loader"
    :options {:options {:outputAssetBase "native_modules"}}}})

(def.js css-rule
  {:test #"\.css$"
   :use [{:loader "style-loader"}
         {:loader "css-loader"}]})

(def.js raw-rule
  (fn [test]
   (return
    {:test test
     :use [{:loader "cache-loader"}
           {:loader "raw-loader"}]})))

(def.js file-rule
  (fn [test]
    (return
     {:test test
      :use "file-loader"})))

(def.js image-rule
  (-/file-rule #"\.(png|j?g|svg|gif)?$"))

;;
;; Node Config
;;

(def.js RunNodeWebpackPlugin (require "run-node-webpack-plugin"))

(def.js WebpackNodeExternals (require "webpack-node-externals"))

(def.js node-common-config
  {:entry  "./src/main.js"
   :target "node"
   :module {:rules [-/jsx-rule]}
   :externals [(-/WebpackNodeExternals)]})

(defn.js
  node-dev-config
  [#{provide}]
  (return (j/assign {} -/node-common-config
                    {:mode "development"
                     :output {:path (. -/path (join __dirname "dist"))
                              :filename "main.js"}
                     :plugins
                     (-> [#_(new (. -/webpack IgnorePlugin) #"\.(css|less)$")
                          (new (. -/webpack BannerPlugin)
                               "require(\"source-map-support\").install();"
                               {:raw true :entryOnly false})
                          (:? provide (new (. -/webpack ProvidePlugin) provide))
                          (new -/RunNodeWebpackPlugin)]
                         (j/filter (fn:> [x] x)))})))

(def.js node-prod-config
  (j/assign {} -/node-common-config
            {:mode "production"
             :output {:path (. -/path (join __dirname "dist"))
                      :filename "main.min.js"}
             :plugins
             [#_(new (. -/webpack IgnorePlugin) #"\.(css|less)$")]}))

(def.js node-config
  (fn [env]
    (if env.prod
      (return -/node-prod-config)
      (return (-/node-dev-config {})))))

(def +node-basic+
  {:type     :script
   :main     #'node-config
   :file     "webpack.config.js"
   :footer   (l/emit-as :js '[(:= module.exports node-config)])})

(def +node-makefile+
  {:type  :makefile
   :main  '[[:init [pnpm install]]
            [:dev  [pnpm dev]]
            [:run  [node "dist/main.js"]]
            [:package [pnpm package]]
            [:release {:- [package]}
             [pnpm release]]]})

(def +node-gitignore+
  {:type :gitignore
   :main '[dist
           node_modules
           yarn.lock
           pnpm-lock.yaml
           bundle.js]})

;;
;; Web Config
;;

(def.js HtmlWebpackPlugin (require "html-webpack-plugin"))

(def.js web-dev-server
  {:contentBase (. -/path (resolve __dirname "dist"))
   :open true
   :clientLogLevel "silent"
   :port 9000})


(def.js web-dev-config
  {:entry  (. -/path (resolve __dirname "src" "index.js"))
   :output {:path (. -/path (resolve __dirname "dist"))
            :filename "bundle.js"}

   :devServer -/web-dev-server

   :module
   {:rules [#{(:.. -/jsx-rule)
              {:exclude #"node_modules"}}
            -/css-rule
            -/image-rule]}

   :externals [(-/WebpackNodeExternals)]

   :plugins
   [(new (. -/webpack HotModuleReplacementPlugin))
    (new -/HtmlWebpackPlugin
         {:template (. -/path (resolve __dirname "public/index.html"))
          :filename "index.html"})]})

(def +web-basic+
  {:type     :script
   :main     web-dev-config
   :file     "webpack.config.js"
   :footer   (l/emit-as :js '[(:= module.exports web-dev-config)])})

;;
;; Electron Config Frame
;;

(def.js electron-config-frame
  {:entry "./main-frame.js"
   :module {:rules [-/node-rule
                    -/jsx-rule
                    -/native-rule]}
   :devServer {:hot true}})

(def +electron-config-frame+
  {:type     :script
   :main     electron-config-frame
   :file     "config/webpack.config.frame.js"
   :footer   (l/emit-as :js '[(:= module.exports electron-config-frame)])})

;;
;; Electron Config Iner
;;

(def.js electron-config-inner
  {:module {:rules [-/native-rule
                    -/node-rule
                    -/jsx-rule
                    -/css-rule
                    (-/raw-rule #"\.lua$")]}
   :plugins [#_(new (. -/webpack HotModuleReplacementPlugin))]
   :resolve {:extensions [".js" ".jsx" ".css" ".json" ".lua"]}})

(def +electron-config-inner+
  {:type     :script
   :main     electron-config-inner
   :file     "config/webpack.config.inner.js"
   :footer   (l/emit-as :js '[(:= module.exports electron-config-inner)])})

;;
;; Electron Main Frame
;;

(def.js electron (require "electron"))
(def.js contextMenu (require "electron-context-menu"))
(def.js BrowserWindow (. electron BrowserWindow))

(def.js electron-create-window
  (fn []
    (let [frame (new -/BrowserWindow
                     {:width 800
                      :height 600
                      :webPreferences
                      {:nodeIntegration true}})]
      (frame.loadURL MAIN_INNER_WEBPACK_ENTRY)
      (frame.webContents.openDevTools))))

(def.js electron-main-frame
  (fn []
    (let [app (. -/electron app)]
      (-/contextMenu {})
      (app.on "ready" -/electron-create-window)
      (app.on "window-all-closed"
              (fn [] (if (!== process.platform "darwin")
                       (app.quit))))
      (app.on "activate"
              (fn []
                (if (-> (. BrowserWindow getAllWindows)
                        (. length)
                        (=== 0))
                  (-/electron-create-window)))))))

(def +electron-main-frame+
  {:type     :script
   :main     electron-main-frame
   :file     "main-frame.js"
   :footer   (electron-main-frame)})

;;
;; Electron Main Inner
;;

(def.js React (require "react"))
(def.js ReactDOM (require "react-dom"))
(def.js App (. (require "./src/App")
               default))

(def.js MainInner
  (fn []
    (:# :declare -/App -/React -/ReactDOM)
    (. ReactDOM
       (render
        [:% -/App]
        (document.getElementById "app")))))

(def +electron-main-inner+
  {:type     :script
   :main     MainInner
   :file     "main-inner.jsx"
   :footer   (l/emit-as
              :js
              '[(def #{hot} (require "react-hot-loader"))
                (MainInner)
                (:= module.exports ((hot module) App))])})

(def +electron-main-inner-html+
  {:type    :html
   :main    [:html [:head [:meta {:charset "UTF-8"}]]
             [:body [:div {:id "app"}]]]
   :file    "main-inner.html"})

(def +electron-gitignore+
  {:type    :gitignore
   :main    '[.webpack
              dist
              node_modules
              yarn.lock
              yarn-error*
              pnpm-lock.yaml]})

(def +electron-makefile+
  {:type  :makefile
   :main  '[[:init [pnpm install --shamefully-hoist]]
            [:dev  [yarn dev]]
            [:package [yarn package]]
            [:release {:- [package]} [yarn release]]]})

(def +electron-setup+
  [+electron-config-frame+
   +electron-config-inner+
   +electron-main-frame+
   +electron-main-inner+
   +electron-main-inner-html+
   +electron-gitignore+
   +electron-makefile+])
