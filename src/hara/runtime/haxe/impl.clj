(ns hara.runtime.haxe.impl
  (:require [clojure.string :as str]
            [std.json :as json]
            [std.lib.component :as component]
            [std.lib.foundation :as f]
            [std.lib.impl :as std-impl]
            [hara.lang.runtime :as rt]
            [hara.runtime.basic.type-common :as common]
            [std.fs :as fs])
  (:import [java.io BufferedReader InputStreamReader]
           [java.util.concurrent.atomic AtomicInteger]))

(defn haxe-exec
  "Resolves the Haxe executable."
  {:added "4.1"}
  []
  (or (System/getenv "HAXE_EXEC")
      (some (fn [cmd]
              (when (common/program-exists? cmd)
                cmd))
            ["haxe"])
      "haxe"))

(defn- haxe-wrapper
  "Wraps user code in a Haxe Main class that prints JSON output."
  {:added "4.1"}
  [code]
  (str "import haxe.Json;\n\n"
       "class Main {\n"
       "  static function main() {\n"
       "    " code "\n"
       "    var result = { type: \"data\", return: \"dynamic\", value: OUT };\n"
       "    Sys.println(Json.stringify(result));\n"
       "  }\n"
       "}\n"))

(defn- slurp-stream
  "Reads all lines from an input stream into a string."
  {:added "4.1"}
  [stream]
  (let [reader (BufferedReader. (InputStreamReader. stream))
        sb (StringBuilder.)]
    (loop []
      (if-let [line (.readLine reader)]
        (do (.append sb line)
            (.append sb "\n")
            (recur))
        (str sb)))))

(defn- run-haxe
  "Compiles and runs the given Haxe code with `haxe --interp`."
  {:added "4.1"}
  [exec code]
  (let [dir (fs/create-tmpdir)
        file (str dir "/Main.hx")
        _ (spit file (haxe-wrapper code))
        proc (.start (ProcessBuilder. ^"[Ljava.lang.String;"
                                      (into-array String [exec
                                                          "--interp"
                                                          "-main" "Main"
                                                          "-cp" (str dir)])))
        exit (.waitFor proc)
        output (slurp-stream (.getInputStream proc))
        errors (slurp-stream (.getErrorStream proc))]
    (if (zero? exit)
      (str/trim output)
      (throw (ex-info "Haxe compilation/execution failed"
                      {:code code
                       :exit exit
                       :output output
                       :errors errors})))))

(defn raw-eval-haxe
  "Evaluates Haxe code and returns the decoded result."
  {:added "4.1"}
  [rt code]
  (let [exec (or (:exec rt) (haxe-exec))
        output (run-haxe exec code)
        parsed (json/read output json/+keyword-mapper+)]
    (if (= "data" (:type parsed))
      (:value parsed)
      (throw (ex-info "Haxe eval error" {:code code :response parsed})))))

(defn start-haxe
  "Creates a Haxe runtime (stateless one-shot compiler)."
  {:added "4.1"}
  [{:keys [id exec] :as rt}]
  (assoc rt
         :id (or id (f/sid))
         :exec exec
         :msgid (AtomicInteger. 0)))

(defn stop-haxe
  "Stops the Haxe runtime."
  {:added "4.1"}
  [rt]
  rt)

(defn- rt-haxe-string
  "String representation of the haxe runtime."
  {:added "4.1"}
  [{:keys [id]}]
  (str "#rt.haxe" [id]))

(std-impl/defimpl RuntimeHaxe [id]
  :string rt-haxe-string
  :protocols [std.protocol.component/IComponent
              :suffix "-haxe"
              :method {-start start-haxe
                       -stop stop-haxe
                       -kill stop-haxe}
              std.protocol.context/IContext
              :prefix "rt/default-"
              :method {-raw-eval raw-eval-haxe}])

(defn haxe:create
  "Creates a Haxe runtime."
  {:added "4.1"}
  [{:keys [id exec]
    :as m}]
  (map->RuntimeHaxe (merge
                     {:id (or id (f/sid))
                      :tag :haxe
                      :exec exec
                      :lifecycle {:main {}
                                  :emit {}
                                  :json :full}}
                     m)))

(defn haxe
  "Creates a Haxe runtime."
  {:added "4.1"}
  ([]
   (haxe {}))
  ([m]
   (-> (haxe:create m)
       (component/start))))

(def +init+
  [(rt/install-type!
    :haxe :haxe
    {:type :hara/rt.haxe
     :config {:layout :full}
     :instance {:create haxe:create}})])
