(ns hara.runtime.basic.impl-annex.process-octave
  (:require [clojure.string :as str]
            [hara.runtime.basic.type-basic :as basic]
            [hara.runtime.basic.type-common :as common]
            [hara.runtime.basic.type-oneshot :as oneshot]
            [hara.runtime.basic.type-verify :as type-verify]
            [std.json :as json]
            [hara.lang.runtime :as rt]
            [hara.model.annex.spec-octave :as spec]))

(def +octave-init+
  (common/put-program-options
   :octave {:default  {:oneshot     :octave-cli
                       :verify      :octave-cli
                       :basic       :octave-cli}
            :env      {:octave-cli  {:exec    "octave-cli"
                                     :output  {}
                                     :extension "m"
                                     :flags   {:oneshot ["--no-gui" "--eval"]
                                               :verify  ["--no-gui" "--eval"
                                                         "try; eval(fileread(\"__FILE__\")); catch; quit(1); end_try_catch;"]
                                               :basic   ["--no-gui" "--eval"]}}
                       :octave      {:exec    "octave"
                                     :output  {}
                                     :flags   {:oneshot ["--no-gui" "--eval"]
                                               :basic   ["--no-gui" "--eval"]}}}
   }))


(def +octave-bootstrap+
  (str "function out = xt_return_encode(out0, id, key)\n"
       "  if nargin < 2, id = []; endif\n"
       "  if nargin < 3, key = []; endif\n"
       "  if isna(out0)\n"
       "    outtype = \"nil\";\n"
       "  elseif isstruct(out0)\n"
       "    outtype = \"object\";\n"
       "  elseif iscell(out0)\n"
       "    outtype = \"array\";\n"
       "  elseif isnumeric(out0) && numel(out0) > 1\n"
       "    outtype = \"array\";\n"
       "  elseif islogical(out0)\n"
       "    outtype = \"boolean\";\n"
       "  elseif isnumeric(out0)\n"
       "    outtype = \"number\";\n"
       "  elseif ischar(out0)\n"
       "    outtype = \"string\";\n"
       "  elseif isa(out0, \"function_handle\")\n"
       "    outtype = \"function\";\n"
       "  else\n"
       "    outtype = \"unknown\";\n"
       "  endif\n"
       "  payload = struct(\"type\", \"data\", \"return\", outtype, \"value\", out0);\n"
       "  if ~isempty(id), payload.id = id; endif\n"
       "  if ~isempty(key), payload.key = key; endif\n"
       "  if strcmp(outtype, \"function\")\n"
       "    payload.type = \"raw\";\n"
       "    payload.value = \"function\";\n"
       "  endif\n"
       "  try\n"
       "    out = jsonencode(payload);\n"
       "  catch\n"
       "    payload.type = \"raw\";\n"
       "    payload.value = \"unencodable\";\n"
       "    out = jsonencode(payload);\n"
       "  end_try_catch\n"
       "endfunction\n\n"
       "function out = xt_return_wrap(f)\n"
       "  try\n"
       "    out0 = f();\n"
       "    out = xt_return_encode(out0);\n"
       "  catch err\n"
       "    out = jsonencode(struct(\"type\", \"error\", \"return\", \"error\", \"value\", err.message));\n"
       "  end_try_catch\n"
       "endfunction\n\n"
       "function out = xt_return_eval(s)\n"
       "  function inner = eval_fn()\n"
       "    eval(s);\n"
       "    inner = ans;\n"
       "  endfunction\n"
       "  out = xt_return_wrap(@eval_fn);\n"
       "endfunction"))

(defn default-oneshot-wrap
  "wraps an octave expression body for one-shot eval"
  {:added "4.0"}
  [body]
  (let [parts (->> (str/split (str +octave-bootstrap+ "\n\n" body)
                              #"\n\s*\n")
                   (map str/trim)
                   (remove empty?))
        expr  (last parts)
        defs  (butlast parts)]
    (str "1;\n\n"
         (when (seq defs)
           (str (str/join "\n\n" defs) "\n\n"))
         "disp(xt_return_encode(" expr "))\n")))

(defn default-oneshot-trim
  "passes the raw output returned by octave to the json parser"
  {:added "4.0"}
  [s]
  (str/trim s))

(def +octave-oneshot-config+
  (common/set-context-options
   [:octave :oneshot :default]
   {:main  {:in  #'default-oneshot-wrap
            :out #'default-oneshot-trim}
    :emit  {:body  {:transform (fn [form _] form)}}
    :json :full}))

(def +octave-verify-config+
  (common/set-context-options
   [:octave :verify :default]
   {:main    {}
    :emit    {}
    :json    false
    :exec-fn #'type-verify/verify-exec-file}))

(def +octave-oneshot+
  [(rt/install-type!
    :octave :oneshot
    {:type :hara/rt.oneshot
     :instance {:create oneshot/rt-oneshot:create}})])

(def +octave-verify+
  [(rt/install-type!
    :octave :verify
    {:type :hara/rt.oneshot
     :instance {:create oneshot/rt-oneshot:create}})])

;;
;; BASIC
;;

(def +client-basic+
  (str "function client_basic(host, port, opts)\n"
       "  pkg load sockets;\n"
       "  while true\n"
       "    conn = socket(AF_INET, SOCK_STREAM, 0);\n"
       "    connect(conn, host, port);\n"
       "    while true\n"
       "      line = fgetl(conn);\n"
       "      if ~ischar(line), continue; endif\n"
       "      if strcmp(line, \"<PING>\"), continue; endif\n"
       "      result = xt_return_eval(line);\n"
       "      fprintf(conn, \"%s\\n\", result);\n"
       "      fflush(conn);\n"
       "    endwhile\n"
       "  endwhile\n"
       "endfunction"))

(defn default-basic-client
  "returns the octave source for the basic tcp client"
  {:added "4.0"}
  [port & [{:keys [host]}]]
  (str "1;\n\n"
       +octave-bootstrap+
       "\n\n"
       +client-basic+
       "\n\n"
       "client_basic(\"" (or host "127.0.0.1") "\", " port ", struct());"))

(def +octave-basic-config+
  (common/set-context-options
   [:octave :basic :default]
   {:bootstrap #'default-basic-client
    :main  {}
    :emit  {:body  {:transform (fn [form _] form)}}
    :json :full
    :encode :json
    :timeout 2000}))

(def +octave-basic+
  [(rt/install-type!
    :octave :basic
    {:type :hara/rt.basic
     :instance {:create #'basic/rt-basic:create}
     :config {:layout :full}})])
