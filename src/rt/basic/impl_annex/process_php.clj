(ns rt.basic.impl-annex.process-php
  (:require [clojure.string :as str]
            [rt.basic.type-basic :as basic]
            [rt.basic.type-common :as common]
            [rt.basic.type-oneshot :as oneshot]
            [std.lang.base.impl :as impl]
            [std.lang.base.runtime :as rt]
            [std.lang.model-annex.spec-php :as spec]))

(def +php-init+
  (common/put-program-options
   :php  {:default  {:oneshot    :php
                      :basic      :php}
           :env      {:php    {:exec   "php"
                                :flags  {:oneshot   ["-r"]
                                        :basic     ["-r"]}}}}))

;;
;; ONESHOT
;;

(defn default-body-transform
  "transform oneshot forms for `return-eval`"
  {:added "4.1"}
  [input mopts]
  (rt/return-transform input mopts))

(defn php-body-source
  "creates a single-line php source string for runtime eval"
  {:added "4.1"}
  [input mopts]
  (-> (impl/emit-as
       :php [(default-body-transform input mopts)])
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
    "  catch(Exception $e){"
    "    return json_encode(['id' => $id, 'key' => $key, 'type' => 'raw', 'value' => '' . $out]);"
    "  }"
    "}"
    ""
    "function return_wrap($f){"
    "  try{"
    "    $out = call_user_func_array($f, []);"
    "  }"
    "  catch(Exception $e){"
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

(def +php-oneshot+
  [(rt/install-type!
    :php :oneshot
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
   :container {:image "foundation-base/rt-basic-php:latest"}
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
