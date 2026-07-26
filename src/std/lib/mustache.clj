(ns std.lib.mustache
  (:require [std.lib.collection :as coll])
  (:import (newlisp.lib.mustache Context Mustache)))

(defn render
  "converts a template with mustache data
 
   (render \"{{user.name}}\" {:user {:name \"tahto\"}})
   => \"tahto\"
 
   (render \"{{# user.account}}{{name}} {{/user.account}}\"
           {:user {:account [{:name \"admin\"}
                             {:name \"user\"}]}})
   => \"admin user \"
 
   (render \"{{? user}}hello{{/user}}\" {:user true})
   => \"hello\"
 
   (render \"{{^ user.name}}hello{{/user.name}}\" {:user nil})
   => \"hello\""
  {:added "3.0"}
  ([template data]
   (if (empty? template)
     template
     (let [template (Mustache/preprocess template)
           flattened (coll/tree-flatten data ".")]
       (.render template (Context. flattened nil))))))
