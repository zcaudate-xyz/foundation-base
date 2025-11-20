(ns code.heal.tokens
  (:require [std.string :as str]))

(defn heal-tokens
  [content]
  (let [ ;; 1. The primary regex target: Keywords ending in ':' followed by a common delimiter.
        ;;    This captures patterns like :key:] or :key: , etc.
        ;;    The regex matches: (:) (name) (:) (delimiter)
        cleaned-content (str/replace content
                                     #":([a-zA-Z0-9_\-]+):([\s\n\}\]\{\[\)])"
                                     (fn [[match name-part delimiter]]
                                       ;; Replacement: :name<delimiter> (e.g., :key])
                                       (str ":" name-part delimiter)))
        
        ;; 2. The secondary regex target: Keywords ending in ':' at the end of the file/string.
        ;;    This captures the edge case where the file ends with a corrupt token.
        cleaned-content-final (str/replace cleaned-content
                                           #":([a-zA-Z0-9_\-]+):$"
                                           (str ":$1"))]
    cleaned-content-final))
