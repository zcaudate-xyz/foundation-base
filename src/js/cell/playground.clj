(ns js.cell.playground
  (:require [clojure.string]
            [js.cell.kernel.worker-local]
            [js.cell.kernel.worker-impl]
            [js.core :as j]
            [std.fs :as fs]
            [std.html :as html]
            [std.lang :as l]
            [std.lib.atom :as atom]
            [std.lib.network :as network]
            [std.lib.os :as os]
            [std.lib.security :as security]))

(defonce ^:dynamic *current* (atom nil))

(def +index+
  (html/html [:html
              [:head
               [:meta {:http-equiv "Content-Security-Policy"
                       :content (clojure.string/join ";"
                                          ["default-src * 'unsafe-eval'"
                                           "connect-src * 'unsafe-eval'"
                                           "script-src  'self' 'unsafe-eval' 'unsafe-inline"
                                           "worker-src  blob: data: 'self' 'unsafe-eval' 'unsafe-inline"])}]]]))

(defn- page-html
  "renders a page scaffold for browser e2e tests"
  [{:keys [title head body scripts]
    :or {title "js.cell playground"
         body [:div {:id "playground"}]}}]
  (let [csp (clojure.string/join ";"
                                 ["default-src * 'unsafe-eval'"
                                  "connect-src * 'unsafe-eval'"
                                  "script-src  'self' 'unsafe-eval' 'unsafe-inline'"
                                  "worker-src  blob: data: 'self' 'unsafe-eval' 'unsafe-inline'"])
        body (if (string? body)
               body
               (html/html body))
        head (cond (nil? head) ""
                   (string? head) head
                   :else (html/html head))
        scripts (apply str (map (fn [src]
                                  (str "<script src=\"" src "\"></script>"))
                                scripts))]
    (str "<!doctype html>"
         "<html><head>"
         "<meta http-equiv=\"Content-Security-Policy\" content=\"" csp "\">"
         "<title>" title "</title>"
         head
         "</head><body>"
         body
         scripts
         "</body></html>")))

(defn start-playground
  "starts the playground"
  {:added "4.0"}
  []
  (or @*current*
      (reset! *current*
              (let [root (str (fs/create-tmpdir))
                    _    (spit (str root "/index.html") +index+)
                    port (network/port:check-available 0)
                    process (os/sh "http-server" "-p" (str port) {:root root
                                                                 :wait false})
                    _ (network/wait-for-port "localhost" port)]
                {:root root
                 :port port
                 :process process}))))

(defn stop-playground
  "stops the playground"
  {:added "4.0"}
  []
  (when @*current*
    (atom/swap-return! *current*
      (fn [{:keys [process] :as m}]
        (os/sh-close process)
        [m nil]))))

(defn play-file
  "gets the file path in playground"
  {:added "4.0"}
  [& paths]
  (clojure.string/join "/" (cons (:root (start-playground)) paths)))

(defn play-url
  "gets the playground url"
  {:added "4.0"}
  [& [url]]
  (str "http://127.0.0.1:" (:port (start-playground)) "/" url))

(defn play-script
  "gets the script"
  {:added "4.0"}
  [forms & [as-script layout]]
  (cond as-script
        (l/emit-script (cons 'do forms) {:lang :js
                                         :layout (or layout :flat)})
        
        :else
        (let [script (l/emit-script (cons 'do forms) {:lang :js
                                                      :layout (or layout :flat)})
              sha (security/sha1 script)
              {:keys [root]} (start-playground)
              filename (str root "/" sha ".js")
              _ (when (not (fs/exists? filename))
                  (spit filename script))]
          (str sha ".js"))))

(defn play-page
  "gets a page asset in the playground"
  {:added "4.0"}
  [m & [as-script]]
  (let [page (page-html m)]
    (if as-script
      page
      (let [sha (security/sha1 page)
            {:keys [root]} (start-playground)
            name (or (:name m) "page")
            filename (str name "-" sha ".html")
            _ (spit (str root "/" filename) page)]
        filename))))

(defn play-worker
  "constructs the play worker"
  {:added "4.0"}
  [& [as-script]]
  (play-script '[(js.cell.kernel.worker-local/actions-init {})
                 (js.cell.kernel.worker-impl/worker-init self)
                 (js.cell.kernel.worker-impl/worker-init-signal self {:done true})]
               as-script))

(defn play-files
  "copies files to the playground"
  {:added "4.0"}
  [files]
  (let [{:keys [root]} (start-playground)]
    (doall (for [[dst src] files]
             (let [out (str root "/" dst)]
               (fs/copy src out {:options [:replace-existing]})
               [out src])))))
