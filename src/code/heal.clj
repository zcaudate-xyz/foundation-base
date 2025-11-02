(ns code.heal
  (:require [code.heal.core :as core]
            [code.heal.print :as print]
            [code.heal.parse :as parse]
            [std.lib :as h]))

(h/intern-in [heal core/heal])

(defn pprint
  [content]
  (print/print-rainbow
   content
   (parse/pair-delimiters
    (parse/parse-delimiters content))))


(comment

  (pprint
   (slurp
    "src/sznui/_gen/quickstart_page_demo/guide_selection.clj"))


  (core/heal-indented
   "(\"hello)\" ))")
  
  (parse/pair-delimiters
   (parse/parse-delimiters
    (core/heal
     (slurp
      "src/sznui/_gen/quickstart_page_demo/guide_selection.clj"))))
  
  
  (doseq [path (keys (std.fs/list
                      "src/sznui/_gen"
                      {:include [#"\d.+\.clj$"]
                       :recursive true}))]
    (h/prn path)
    (spit path
          (core/heal
           (slurp path)
           {:print true})))

  )
