(ns js.lib.tree-sitter
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :basic
   :bundle {:default  [["tree-sitter" :as parser]]}})


(!.js
 (:= (!:G Parser) (require "tree-sitter")))
(!.js
 (:= (!:G CGrammer) (require "tree-sitter-c")))


(!.js
 (:= (!:G parser) (new Parser))
 (. parser (setLanguage CGrammer)))

(!.js
 (:= (!:G TREE)
      (parser.parse
       (@!
        (slurp "/usr/local/Cellar/openresty/1.19.9.1_2/luajit/include/luajit-2.1/lua.h")))))

(!.js
 (-> 
  (TREE.rootNode)
  (. (child 1))))

