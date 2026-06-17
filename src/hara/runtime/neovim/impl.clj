(ns hara.runtime.neovim.impl
  (:require [std.json :as json]
            [std.lib.component :as component]
            [std.lib.foundation :as f]
            [std.lib.impl :as std-impl]
            [hara.lang.runtime :as rt]
            [hara.runtime.basic.impl.process-lua :as process-lua]
            [hara.runtime.basic.type-common :as common])
  (:import [java.io BufferedInputStream ByteArrayOutputStream]
           [java.util ArrayList Map$Entry]
           [org.msgpack MessagePack]
           [org.msgpack.type Value]))

;;
;; MSGPACK UTILS
;;

(def ^:private ^MessagePack msgpack
  (MessagePack.))

(defn- value->clj
  "Converts an org.msgpack.type.Value into Clojure data."
  {:added "4.1"}
  [^Value v]
  (cond (nil? v) nil
        (.isNilValue v) nil
        (.isBooleanValue v) (.getBoolean (.asBooleanValue v))
        (.isIntegerValue v) (.getLong (.asIntegerValue v))
        (.isFloatValue v) (.getDouble (.asFloatValue v))
        (.isRawValue v) (.getString (.asRawValue v))
        (.isArrayValue v) (mapv value->clj (.getElementArray (.asArrayValue v)))
        (.isMapValue v) (into {} (map (fn [^Map$Entry e]
                                        [(value->clj (.getKey e))
                                         (value->clj (.getValue e))])
                                      (.entrySet (.asMapValue v))))
        :else v))

(defn- pack-request
  "Packs a MessagePack-RPC request into bytes."
  {:added "4.1"}
  [msgid method params]
  (let [out (ByteArrayOutputStream.)
        packer (.createPacker msgpack out)
        req (doto (ArrayList.)
              (.add 0)
              (.add (long msgid))
              (.add method)
              (.add params))]
    (.write packer req)
    (.flush packer)
    (.toByteArray out)))

;;
;; PROCESS
;;

(defn neovim-exec
  "Resolves the neovim executable."
  {:added "4.1"}
  []
  (or (System/getenv "NEOVIM_EXEC")
      (some (fn [cmd]
              (when (common/program-exists? cmd)
                cmd))
            ["nvim" "neovim"])
      "nvim"))

(defn start-neovim
  "Starts a headless nvim --embed process."
  {:added "4.1"}
  [{:keys [id exec] :as rt}]
  (let [exec (or exec (neovim-exec))
        proc (.start (ProcessBuilder. ^"[Ljava.lang.String;"
                                      (into-array String [exec "--embed" "--headless"])))
        in   (BufferedInputStream. (.getInputStream proc))
        out  (.getOutputStream proc)
        unpacker (.createUnpacker msgpack in)]
    (assoc rt
           :process proc
           :output out
           :unpacker unpacker
           :msgid (atom 0))))

(defn stop-neovim
  "Stops the nvim process."
  {:added "4.1"}
  [{:keys [^Process process] :as rt}]
  (when process
    (try (.destroyForcibly process)
         (catch Throwable _)))
  rt)

;;
;; RPC
;;

(defn- next-msgid
  "Returns the next request id and increments the counter."
  {:added "4.1"}
  [rt]
  (swap! (:msgid rt) inc))

(defn- send-request
  "Sends a msgpack-rpc request and waits for the matching response."
  {:added "4.1"}
  [{:keys [^java.io.OutputStream output
           ^org.msgpack.unpacker.Unpacker unpacker]
    :as rt}
   method params]
  (let [msgid (next-msgid rt)
        ^bytes req-bytes (pack-request msgid method params)]
    (.write output req-bytes 0 (alength req-bytes))
    (.flush output)
    (loop []
      (let [response (value->clj (.read unpacker Value))]
        (if (and (vector? response)
                 (= 4 (count response))
                 (= 1 (first response))
                 (= msgid (second response)))
          (let [[_ _ err result] response]
            (if err
              (throw (ex-info "Neovim RPC error" {:method method :params params :error err}))
              result))
          (recur))))))

;;
;; EVAL
;;

(defn lua-eval-wrap
  "Wraps Lua code so that the return value is JSON-serialized by Neovim."
  {:added "4.1"}
  [code]
  (str "local _hara_eval = function(...)\n"
       code
       "\nend\n"
       "local _hara_ok, _hara_result = pcall(_hara_eval)\n"
       "if _hara_ok then\n"
       "  return vim.json.encode({ok=true, value=_hara_result})\n"
       "else\n"
       "  return vim.json.encode({ok=false, error=tostring(_hara_result)})\n"
       "end"))

(defn raw-eval-neovim
  "Evaluates Lua code inside Neovim and returns the JSON-decoded result."
  {:added "4.1"}
  [rt code]
  (let [wrapped (lua-eval-wrap code)
        params (doto (ArrayList.) (.add wrapped) (.add (ArrayList.)))
        result (send-request rt "nvim_exec_lua" params)]
    (if (string? result)
      (let [parsed (json/read result json/+keyword-mapper+)]
        (if (:ok parsed)
          (:value parsed)
          (throw (ex-info "Neovim Lua error" {:code code :error (:error parsed)}))))
      (throw (ex-info "Neovim returned non-string result" {:result result})))))

(defn invoke-ptr-neovim
  "Invokes a pointer in the Neovim runtime."
  {:added "4.1"}
  ([rt ptr args]
   (rt/default-invoke-script rt ptr args raw-eval-neovim
                             {:main  {}
                              :emit  {:body  {:transform #'process-lua/default-body-transform}
                                      :code  (:code process-lua/+lua-basic-script-emit+)}
                              :json  :full
                              :encode :json})))

;;
;; RUNTIME RECORD
;;

(defn- rt-neovim-string
  "String representation of the neovim runtime."
  {:added "4.1"}
  [{:keys [id]}]
  (str "#rt.neovim" [id]))

(std-impl/defimpl RuntimeNeovim [id]
  :string rt-neovim-string
  :protocols [std.protocol.component/IComponent
              :suffix "-neovim"
              :method {-start start-neovim
                       -stop stop-neovim
                       -kill stop-neovim}
              std.protocol.context/IContext
              :prefix "rt/default-"
              :method {-raw-eval raw-eval-neovim
                       -invoke-ptr invoke-ptr-neovim}])

(defn neovim:create
  "Creates a Neovim runtime."
  {:added "4.1"}
  [{:keys [id exec]
    :as m}]
  (map->RuntimeNeovim (merge
                       {:id (or id (f/sid))
                        :tag :neovim
                        :exec exec
                        :lifecycle {:main {}
                                    :emit {}
                                    :json :full}}
                       m)))

(defn neovim
  "Creates and starts a Neovim runtime."
  {:added "4.1"}
  ([]
   (neovim {}))
  ([m]
   (-> (neovim:create m)
       (component/start))))

(def +init+
  [(rt/install-type!
    :lua :neovim
    {:type :hara/rt.neovim
     :config {:layout :full}
     :instance {:create neovim:create}})])
