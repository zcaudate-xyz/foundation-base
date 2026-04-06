(ns code.mcp.tool.common)

(def default-print-options
  {:print {:function false
           :summary  false
           :result   false
           :item     false}})

(defn read-edn
  ([s]
   (read-edn s nil))
  ([s default]
   (if (nil? s)
     default
     (read-string s))))

(defn merge-print-options
  [options]
  (merge default-print-options (or options {})))

(defn render-result
  [raw-result]
  (if (string? raw-result)
    raw-result
    (with-out-str
      (prn raw-result))))

(defn response
  [raw-result]
  {:content [{:type "text"
              :text (render-result raw-result)}]
   :isError (boolean (and (map? raw-result)
                          (:error raw-result)))})

(defn error-response
  [message]
  {:content [{:type "text"
              :text message}]
   :isError true})
