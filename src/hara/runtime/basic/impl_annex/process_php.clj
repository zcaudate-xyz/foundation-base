(ns hara.runtime.basic.impl-annex.process-php
  (:require [clojure.string :as str]
            [hara.runtime.basic.type-basic :as basic]
            [hara.runtime.basic.type-common :as common]
            [hara.runtime.basic.type-oneshot :as oneshot]
            [hara.runtime.basic.type-verify :as type-verify]
            [hara.lang.impl :as impl]
            [hara.lang.runtime :as rt]
            [hara.model.annex.spec-php :as spec]))

(def +php-init+
  (common/put-program-options
   :php  {:default  {:oneshot    :php
                     :verify     :php
                     :basic      :php}
          :env      {:php    {:exec   "php"
                              :extension "php"
                              :flags  {:oneshot   ["-r"]
                                       :verify    ["-l"]
                                       :basic     ["-r"]}}}}))

;;
;; ONESHOT
;;

(defn default-body-transform
  "transform oneshot forms for `return-eval`"
  {:added "4.1"}
  [input mopts]
  (rt/return-transform input mopts))

(defn php-prefix-params
  "Prefixes bare function parameter symbols with `$` for valid PHP emission.
   Handles `fn` and `defn` forms with simple lexical scoping."
  {:added "4.1"}
  [form]
  (letfn [(param-sym? [x]
            (and (symbol? x)
                 (not (str/starts-with? (name x) "$"))))
          (prefix-param [x]
            (if (param-sym? x)
              (symbol (str "$" (name x)))
              x))
          (params-of [params-vec]
            (set (filter param-sym? params-vec)))
          (walk [form params]
            (cond
              (and (seq? form)
                   ('#{fn} (first form)))
              (let [[op params-vec & body] form
                    new-params (into params (params-of params-vec))]
                (list* op
                       (vec (map prefix-param params-vec))
                       (map #(walk % new-params) body)))

              (and (seq? form)
                   ('#{defn defn-} (first form)))
              (let [[op name params-vec & body] form
                    name (with-meta name {:inner true})
                    new-params (into params (params-of params-vec))]
                (list* op
                       name
                       (vec (map prefix-param params-vec))
                       (map #(walk % new-params) body)))

              (symbol? form)
              (if (params form)
                (prefix-param form)
                form)

              (seq? form)
              (map #(walk % params) form)

              (vector? form)
              (vec (map #(walk % params) form))

              (map? form)
              (into {} (map (fn [[k v]]
                              [(walk k params) (walk v params)])
                            form))

              :else form))]
    (walk form #{})))

(defn php-body-source
  "creates a single-line php source string for runtime eval"
  {:added "4.1"}
  [input mopts]
  (-> (impl/emit-as
       :php [(php-prefix-params (default-body-transform input mopts))])
      (str/replace #"(?s)<\?php\s*" "")
      (str/replace #"(?s)\s*\?>" "")
       (str/replace #"\n+" " ")
      (str/trim)))

(def +return-bootstrap+
  (str/join
   "\n"
    ["function return_encode($out, $id, $key){"
     "  try{"
     "    return json_encode(['id' => $id, 'key' => $key, 'type' => 'data', 'value' => $out]);"
     "  }"
     "  catch(Throwable $e){"
     "    return json_encode(['id' => $id, 'key' => $key, 'type' => 'raw', 'value' => '' . $out]);"
     "  }"
     "}"
    ""
    "function return_wrap($f){"
    "  try{"
    "    $out = call_user_func_array($f, []);"
    "  }"
    "  catch(Throwable $e){"
    "    return json_encode(['type' => 'error', 'value' => '' . $e]);"
    "  }"
    "  return return_encode($out, null, null);"
    "}"
    ""
    "function return_eval($s){"
    "  return return_wrap(function () use ($s) {"
    "    return eval($s);"
    "  });"
    "}"]))

(def ^{:arglists '([body])}
  default-oneshot-wrap
  (let [bootstrap +return-bootstrap+]
    (fn [body]
      (let [body-source (str "return "
                             (php-body-source body {})
                             ";")]
        (str bootstrap
             "\n\n"
             (impl/emit-as
              :php [(list 'do
                           (list 'echo (list 'return-eval body-source)))]))))))

(defn default-basic-body-transform
  "transform basic runtime forms for `return-eval`"
  {:added "4.1"}
  [input mopts]
  (list ':- (str "return "
                 (php-body-source input mopts)
                 ";")))

(def +php-oneshot-config+
  (common/set-context-options
   [:php :oneshot :default]
   {:main  {:in    #'default-oneshot-wrap}
    :emit  {:body  {:transform #'default-body-transform}}
    :json :full}))

(def +php-verify-config+
  (common/set-context-options
   [:php :verify :default]
   {:main    {}
    :emit    {}
    :json    false
    :exec-fn #'type-verify/verify-exec-file}))

(def +php-oneshot+
  [(rt/install-type!
    :php :oneshot
    {:type :hara/rt.oneshot
     :instance {:create oneshot/rt-oneshot:create}})])

(def +php-verify+
  [(rt/install-type!
    :php :verify
    {:type :hara/rt.oneshot
     :instance {:create oneshot/rt-oneshot:create}})])

;;
;; BASIC
;;

(def +client-basic+
  (str/join
   "\n"
   ["function client_basic($host, $port, $opts){"
    "  $conn = fsockopen($host, $port);"
    "  while (!feof($conn)){"
    "    $line = fgets($conn);"
    "    if ($line === \"<PING>\\n\"){"
    "      continue;"
    "    }"
    "    $input = json_decode($line);"
    "    if ($input !== null){"
    "      fwrite($conn, return_eval($input) . \"\\n\");"
    "    }"
    "  }"
    "}"]))

(def ^{:arglists '([port & [{:keys [host]}]])}
  default-basic-client
  (let [bootstrap (str +return-bootstrap+
                       "\n\n"
                       +client-basic+)]
    (fn [port & [{:keys [host]}]]
      (str bootstrap
           "\n\n"
           (impl/emit-as
            :php [(list 'do
                         (list 'client-basic
                               (or host "127.0.0.1")
                               port
                               {}))])))))

(def +default-basic-config+
  {:bootstrap #'default-basic-client
   :main   {}
   :container {:image "ghcr.io/zcaudate-xyz/foundation-base/rt-basic-php:latest"}
   :container-backup true
   :emit   {:body  {:transform #'default-basic-body-transform}
            :lang/format :global}
   :json   :full
   :encode :json ;; default
   :timeout 2000})

(def +php-basic-config+
  (common/set-context-options
   [:php :basic :default]
   +default-basic-config+))

(def +php-basic+
  [(rt/install-type!
    :php :basic
    {:type :hara/rt.basic
     :instance {:create #'basic/rt-basic:create}
     :config {:layout :full}})])
