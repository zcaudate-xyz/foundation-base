~namespace-block

;;;
;;; This file is a template.
;;; It will be used to generate a function.
;;;

(defn ~name
  "~docstring ~doc-extra"
  [~@args]
  (comment "This comment from the template is preserved!")

  ;; The function body will be spliced in below
  ~@body)