(ns xt.lang.parser-xml-test
  (:require [std.lang :as l]
            [std.string.prose :as prose])
  (:use code.test))

^{:seedgen/root {:all true, :langs [:js :lua :python]}}
(l/script- :js
  {:runtime :basic
   :require [[xt.lang.parser-xml :as xml]
             [xt.lang.common-lib :as k]]})

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.parser-xml :as xml]
             [xt.lang.common-lib :as k]]})

(l/script- :python
  {:runtime :basic
   :require [[xt.lang.parser-xml :as xml]
             [xt.lang.common-lib :as k]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.parser-xml/parse-xml-params :added "4.0"}
(fact "parses the args"

  (!.js (xml/parse-xml-params "a='123', b=\"456\" "))
  => {"a" "123", "b" "456"}

  (!.lua (xml/parse-xml-params "a='123', b=\"456\" "))
  => {"a" "123", "b" "456"}

  (!.py (xml/parse-xml-params "a='123', b=\"456\" "))
  => {"a" "123", "b" "456"})

^{:refer xt.lang.parser-xml/parse-xml-stack :added "4.0"
  :setup [(def +out+
            [[{"tag" "a", "empty" true}]
             [{"tag" "a"} {"tag" "a", "close" true}]
             [{"tag" "a"} {"tag" "a", "text" "1", "close" true}]
             [{"tag" "a"}
              {"tag" "b"}
              {"tag" "b", "close" true}
              {"tag" "a", "close" true}]
             [{"tag" "a"}
              {"tag" "b", "text" "1"}
              {"tag" "b", "text" "2", "close" true}
              {"tag" "a", "text" "3", "close" true}]
             [{"tag" "a"}
              {"tag" "b", "empty" true}
              {"tag" "a", "close" true}]
             [{"tag" "a"}
              {"tag" "b", "empty" true}
              {"tag" "b"}
              {"tag" "b", "text" "2", "close" true}
              {"tag" "a", "close" true}]])]}
(fact "parses the xml into a ast stack"

  (!.js 
    [(xml/parse-xml-stack "<a/>")
     (xml/parse-xml-stack "<a></a>")
     (xml/parse-xml-stack "<a>1</a>")
     (xml/parse-xml-stack "<a><b></b></a>")
     (xml/parse-xml-stack "<a>1<b>2</b>3</a>")
     (xml/parse-xml-stack "<a><b/></a>")
     (xml/parse-xml-stack "<a><b/><b>2</b></a>")])
  => +out+

  (!.lua 
    [(xml/parse-xml-stack "<a/>")
     (xml/parse-xml-stack "<a></a>")
     (xml/parse-xml-stack "<a>1</a>")
     (xml/parse-xml-stack "<a><b></b></a>")
     (xml/parse-xml-stack "<a>1<b>2</b>3</a>")
     (xml/parse-xml-stack "<a><b/></a>")
     (xml/parse-xml-stack "<a><b/><b>2</b></a>")])
  => +out+

  (!.py 
    [(xml/parse-xml-stack "<a/>")
     (xml/parse-xml-stack "<a></a>")
     (xml/parse-xml-stack "<a>1</a>")
     (xml/parse-xml-stack "<a><b></b></a>")
     (xml/parse-xml-stack "<a>1<b>2</b>3</a>")
     (xml/parse-xml-stack "<a><b/></a>")
     (xml/parse-xml-stack "<a><b/><b>2</b></a>")])
  => +out+)

^{:refer xt.lang.parser-xml/to-node-normalise :added "4.0"}
(fact "normalises the node for viewing")

^{:refer xt.lang.parser-xml/to-node :added "4.0"
  :setup [(def +out+
            [{"tag" "a"}
             {"tag" "a"}
             {"children" ["1"], "tag" "a"}
             {"children" [{"tag" "b"}], "tag" "a"}
             {"children" ["1" {"children" ["2"], "tag" "b"} "3"], "tag" "a"}
             {"children" [{"tag" "b"}], "tag" "a"}
             {"children" [{"tag" "b"} {"children" ["2"], "tag" "b"}],
              "tag" "a"}])]}
(fact "transforms stack to node"

  (!.js
    [(xml/to-node
      [{"tag" "a", "empty" true}])
     (xml/to-node
      [{"tag" "a"}
       {"tag" "a", "close" true}])
     (xml/to-node
      [{"tag" "a"}
       {"tag" "a", "text" "1", "close" true}])
     (xml/to-node
      [{"tag" "a"}
       {"tag" "b"}
       {"tag" "b", "close" true}
       {"tag" "a", "close" true}])
     (xml/to-node
      [{"tag" "a"}
       {"tag" "b", "text" "1"}
       {"tag" "b", "text" "2", "close" true}
       {"tag" "a", "text" "3", "close" true}])
     (xml/to-node
      [{"tag" "a"}
       {"tag" "b", "empty" true}
       {"tag" "a", "close" true}])
     (xml/to-node
      [{"tag" "a"}
       {"tag" "b", "empty" true}
       {"tag" "b"}
       {"tag" "b", "text" "2", "close" true}
       {"tag" "a", "close" true}])])
  => +out+

  (!.lua
    [(xml/to-node
      [{"tag" "a", "empty" true}])
     (xml/to-node
      [{"tag" "a"}
       {"tag" "a", "close" true}])
     (xml/to-node
      [{"tag" "a"}
       {"tag" "a", "text" "1", "close" true}])
     (xml/to-node
      [{"tag" "a"}
       {"tag" "b"}
       {"tag" "b", "close" true}
       {"tag" "a", "close" true}])
     (xml/to-node
      [{"tag" "a"}
       {"tag" "b", "text" "1"}
       {"tag" "b", "text" "2", "close" true}
       {"tag" "a", "text" "3", "close" true}])
     (xml/to-node
      [{"tag" "a"}
       {"tag" "b", "empty" true}
       {"tag" "a", "close" true}])
     (xml/to-node
      [{"tag" "a"}
       {"tag" "b", "empty" true}
       {"tag" "b"}
       {"tag" "b", "text" "2", "close" true}
       {"tag" "a", "close" true}])])
  => +out+

  (!.py
    [(xml/to-node
      [{"tag" "a", "empty" true}])
     (xml/to-node
      [{"tag" "a"}
       {"tag" "a", "close" true}])
     (xml/to-node
      [{"tag" "a"}
       {"tag" "a", "text" "1", "close" true}])
     (xml/to-node
      [{"tag" "a"}
       {"tag" "b"}
       {"tag" "b", "close" true}
       {"tag" "a", "close" true}])
     (xml/to-node
      [{"tag" "a"}
       {"tag" "b", "text" "1"}
       {"tag" "b", "text" "2", "close" true}
       {"tag" "a", "text" "3", "close" true}])
     (xml/to-node
      [{"tag" "a"}
       {"tag" "b", "empty" true}
       {"tag" "a", "close" true}])
     (xml/to-node
      [{"tag" "a"}
       {"tag" "b", "empty" true}
       {"tag" "b"}
       {"tag" "b", "text" "2", "close" true}
       {"tag" "a", "close" true}])])
  => +out+)

^{:refer xt.lang.parser-xml/parse-xml :added "4.0"
  :setup [(def +out+
             [{"tag" "a"}
              {"tag" "a"}
              {"children" ["1"], "tag" "a"}
              {"children" [{"tag" "b"}], "tag" "a"}
              {"children" ["1" {"children" ["2"], "tag" "b"} "3"], "tag" "a"}
              {"children" [{"tag" "b"}], "tag" "a"}
              {"children" [{"tag" "b"} {"children" ["2"], "tag" "b"}],
               "tag" "a"}])]}
(fact "parses xml"

  (!.js
    [(xml/parse-xml "<a/>")
     (xml/parse-xml "<a></a>")
     (xml/parse-xml "<a>1</a>")
     (xml/parse-xml "<a><b></b></a>")
     (xml/parse-xml "<a>1<b>2</b>3</a>")
     (xml/parse-xml "<a><b/></a>")
     (xml/parse-xml "<a><b/><b>2</b></a>")])
  => +out+

  (!.lua
    [(xml/parse-xml "<a/>")
     (xml/parse-xml "<a></a>")
     (xml/parse-xml "<a>1</a>")
     (xml/parse-xml "<a><b></b></a>")
     (xml/parse-xml "<a>1<b>2</b>3</a>")
     (xml/parse-xml "<a><b/></a>")
     (xml/parse-xml "<a><b/><b>2</b></a>")])
  => +out+

  (!.py
    [(xml/parse-xml "<a/>")
     (xml/parse-xml "<a></a>")
     (xml/parse-xml "<a>1</a>")
     (xml/parse-xml "<a><b></b></a>")
     (xml/parse-xml "<a>1<b>2</b>3</a>")
     (xml/parse-xml "<a><b/></a>")
     (xml/parse-xml "<a><b/><b>2</b></a>")])
  => +out+)

^{:refer xt.lang.parser-xml/to-tree :added "4.0"
  :setup [(def +out+
            [["a"]
             ["a" "1"]
             ["a" ["b"]]
             ["a" "1" ["b" "2"] "3"]
             ["helo:test"
              ["ErrorCode" "ESB-00-000"]
              ["A" ["A1" "Hello-11-222"] ["A2" "Bandung"]]
              ["B"]
              ["C"
               ["C1" "Satu"]
               ["C2" "Dua"]
               ["C3" "Tiga"]
               ["C4" ["C41" "Empat-Satu"]]]]])]}
(fact "to node to tree"

  (!.js
   [(xml/to-tree
     (xml/parse-xml "<a/>"))
    (xml/to-tree
    (xml/parse-xml "<a>1</a>"))
    (xml/to-tree
     (xml/parse-xml "<a><b></b></a>"))
    (xml/to-tree
     (xml/parse-xml "<a>1<b>2</b>3</a>"))
    (xml/to-tree
    (xml/parse-xml
     (@! (prose/|
          "<helo:test>"
          "   <ErrorCode>ESB-00-000</ErrorCode>"
          "   <A>"
          "      <A1>Hello-11-222</A1>"
          "      <A2>Bandung</A2>"
          "   </A>"
          "   <B/>"
          "   <C>"
          "     <C1>Satu</C1>"
          "     <C2>Dua</C2>"
          "     <C3>Tiga</C3>"
          "     <C4><C41>Empat-Satu</C41></C4>"
          "   </C>"
          "</helo:test>"))))])
  => +out+

  (!.lua
   [(xml/to-tree
     (xml/parse-xml "<a/>"))
    (xml/to-tree
    (xml/parse-xml "<a>1</a>"))
    (xml/to-tree
     (xml/parse-xml "<a><b></b></a>"))
    (xml/to-tree
     (xml/parse-xml "<a>1<b>2</b>3</a>"))
    (xml/to-tree
    (xml/parse-xml
     (@! (prose/|
          "<helo:test>"
          "   <ErrorCode>ESB-00-000</ErrorCode>"
          "   <A>"
          "      <A1>Hello-11-222</A1>"
          "      <A2>Bandung</A2>"
          "   </A>"
          "   <B/>"
          "   <C>"
          "     <C1>Satu</C1>"
          "     <C2>Dua</C2>"
          "     <C3>Tiga</C3>"
          "     <C4><C41>Empat-Satu</C41></C4>"
          "   </C>"
          "</helo:test>"))))])
  => +out+

  (!.py
   [(xml/to-tree
     (xml/parse-xml "<a/>"))
    (xml/to-tree
    (xml/parse-xml "<a>1</a>"))
    (xml/to-tree
     (xml/parse-xml "<a><b></b></a>"))
    (xml/to-tree
     (xml/parse-xml "<a>1<b>2</b>3</a>"))
    (xml/to-tree
    (xml/parse-xml
     (@! (prose/|
          "<helo:test>"
          "   <ErrorCode>ESB-00-000</ErrorCode>"
          "   <A>"
          "      <A1>Hello-11-222</A1>"
          "      <A2>Bandung</A2>"
          "   </A>"
          "   <B/>"
          "   <C>"
          "     <C1>Satu</C1>"
          "     <C2>Dua</C2>"
          "     <C3>Tiga</C3>"
          "     <C4><C41>Empat-Satu</C41></C4>"
          "   </C>"
          "</helo:test>"))))])
  => +out+)

^{:refer xt.lang.parser-xml/from-tree :added "4.0"
  :setup [(def +out+
            {"params" {},
             "children"
             [{"params" {}, "children" ["ESB-00-000"], "tag" "ErrorCode"}
              {"params" {},
               "children"
               [{"params" {}, "children" ["Hello-11-222"], "tag" "A1"}
                {"params" {}, "children" ["Bandung"], "tag" "A2"}],
               "tag" "A"}
              {"tag" "B"}
              {"params" {},
               "children"
               [{"params" {}, "children" ["Satu"], "tag" "C1"}
                {"params" {}, "children" ["Dua"], "tag" "C2"}
                {"params" {}, "children" ["Tiga"], "tag" "C3"}
                {"params" {},
                 "children"
                 [{"params" {}, "children" ["Empat-Satu"], "tag" "C41"}],
                 "tag" "C4"}],
               "tag" "C"}],
             "tag" "helo:test"})]}
(fact "creates nodes from tree"

  (!.js
    (xml/from-tree
     ["helo:test"
      ["ErrorCode" "ESB-00-000"]
      ["A" ["A1" "Hello-11-222"] ["A2" "Bandung"]]
      ["B"]
      ["C"
       ["C1" "Satu"]
       ["C2" "Dua"]
       ["C3" "Tiga"]
       ["C4" ["C41" "Empat-Satu"]]]]))
  => +out+

  (!.lua
    (xml/from-tree
     ["helo:test"
      ["ErrorCode" "ESB-00-000"]
      ["A" ["A1" "Hello-11-222"] ["A2" "Bandung"]]
      ["B"]
      ["C"
       ["C1" "Satu"]
       ["C2" "Dua"]
       ["C3" "Tiga"]
       ["C4" ["C41" "Empat-Satu"]]]]))
  => +out+

  (!.py
    (xml/from-tree
     ["helo:test"
      ["ErrorCode" "ESB-00-000"]
      ["A" ["A1" "Hello-11-222"] ["A2" "Bandung"]]
      ["B"]
      ["C"
       ["C1" "Satu"]
       ["C2" "Dua"]
       ["C3" "Tiga"]
       ["C4" ["C41" "Empat-Satu"]]]]))
  => +out+)

^{:refer xt.lang.parser-xml/to-brief :added "4.0"
  :setup [(def +out-error+
            {"helo:test"
             {"C"
              {"C1" "Satu",
               "C2" "Dua",
               "C3" "Tiga",
               "C4" {"C41" "Empat-Satu"}},
              "ErrorCode" "ESB-00-000",
              "B" true,
              "A" {"A1" "Hello-11-222"}}})
                   (def +out-bucket+
                     {"error"
                      {"message"
                       "Your previous request to create the named bucket succeeded and you already own it.",
                       "resource" "/abc2",
                       "bucketname" "abc2",
                       "hostid" "5750e190-7dc8-4aab-abe9-13e39405c337",
                       "requestid" "16FEAEEC2B5EF151",
                       "code" "BucketAlreadyOwnedByYou"}})]}
(fact "xml to a more readable form"

  (!.js
   (xml/to-brief
    (xml/parse-xml
     (@! (prose/|
          "<helo:test>"
          "   <ErrorCode>ESB-00-000</ErrorCode>"
          "   <A>"
          "      <A1>Hello-11-222</A1>"
          "      <A2>Bandung</A2>"
          "   </A>"
          "   <B/>"
          "   <C>"
          "     <C1>Satu</C1>"
          "     <C2>Dua</C2>"
          "     <C3>Tiga</C3>"
          "     <C4><C41>Empat-Satu</C41></C4>"
          "   </C>"
          "</helo:test>")))))
  => +out-error+

  (!.js
   (xml/to-brief
    (xml/parse-xml
     (@! (std.html/html
          [:error
           [:code "BucketAlreadyOwnedByYou"]
           [:message
            "Your previous request to create the named bucket succeeded and you already own it."]
           [:bucketname "abc2"]
           [:resource "/abc2"]
           [:requestid "16FEAEEC2B5EF151"]
           [:hostid "5750e190-7dc8-4aab-abe9-13e39405c337"]])))))
  => +out-bucket+

  (!.lua
   (xml/to-brief
    (xml/parse-xml
     (@! (prose/|
          "<helo:test>"
          "   <ErrorCode>ESB-00-000</ErrorCode>"
          "   <A>"
          "      <A1>Hello-11-222</A1>"
          "      <A2>Bandung</A2>"
          "   </A>"
          "   <B/>"
          "   <C>"
          "     <C1>Satu</C1>"
          "     <C2>Dua</C2>"
          "     <C3>Tiga</C3>"
          "     <C4><C41>Empat-Satu</C41></C4>"
          "   </C>"
          "</helo:test>")))))
  => +out-error+

  (!.lua
   (xml/to-brief
    (xml/parse-xml
     (@! (std.html/html
          [:error
           [:code "BucketAlreadyOwnedByYou"]
           [:message
            "Your previous request to create the named bucket succeeded and you already own it."]
           [:bucketname "abc2"]
           [:resource "/abc2"]
           [:requestid "16FEAEEC2B5EF151"]
           [:hostid "5750e190-7dc8-4aab-abe9-13e39405c337"]])))))
  => +out-bucket+

  (!.py
   (xml/to-brief
    (xml/parse-xml
     (@! (prose/|
          "<helo:test>"
          "   <ErrorCode>ESB-00-000</ErrorCode>"
          "   <A>"
          "      <A1>Hello-11-222</A1>"
          "      <A2>Bandung</A2>"
          "   </A>"
          "   <B/>"
          "   <C>"
          "     <C1>Satu</C1>"
          "     <C2>Dua</C2>"
          "     <C3>Tiga</C3>"
          "     <C4><C41>Empat-Satu</C41></C4>"
          "   </C>"
          "</helo:test>")))))
  => +out-error+

  (!.py
   (xml/to-brief
    (xml/parse-xml
     (@! (std.html/html
          [:error
           [:code "BucketAlreadyOwnedByYou"]
           [:message
            "Your previous request to create the named bucket succeeded and you already own it."]
           [:bucketname "abc2"]
           [:resource "/abc2"]
           [:requestid "16FEAEEC2B5EF151"]
           [:hostid "5750e190-7dc8-4aab-abe9-13e39405c337"]])))))
  => +out-bucket+)

^{:refer xt.lang.parser-xml/to-string-value :added "4.1"}
(fact "TODO"

  (!.js
    [(xml/to-string-value true)
     (xml/to-string-value 1)
     (xml/to-string-value "a")])
  => ["true" "1" "a"]

  (!.lua
    [(xml/to-string-value true)
     (xml/to-string-value 1)
     (xml/to-string-value "a")])
  => ["true" "1" "a"]

  (!.py
    [(xml/to-string-value true)
     (xml/to-string-value 1)
     (xml/to-string-value "a")])
  => ["true" "1" "a"])

^{:refer xt.lang.parser-xml/to-string-params :added "4.0"}
(fact "to node params"

  (!.js
   (xml/to-string-params (tab [:a 1])))
  => " a=1"

  (!.lua
   (xml/to-string-params (tab [:a 1])))
  => " a=1"

  (!.py
   (xml/to-string-params (tab [:a 1])))
  => " a=1")

^{:refer xt.lang.parser-xml/to-string :added "4.0"
  :setup [(def +out+
            ["<helo:test></helo:test>"
             "<helo:test><ErrorCode>ESB-00-000</ErrorCode></helo:test>"
             "<helo:test><ErrorCode>ESB-00-000</ErrorCode><A><A1>Hello-11-222</A1><A2>Bandung</A2></A><B></B><C><C1>Satu</C1><C2>Dua</C2><C3>Tiga</C3><C4><C41>Empat-Satu</C41></C4></C></helo:test>"
             "<B></B>"
             "<Delete><Quiet>true</Quiet></Delete>"
             "<Delete><Quiet>true</Quiet></Delete>"
             "<Delete><Quiet>true</Quiet></Delete>"
             {"params" {},
              "children"
              [{"params" {}, "children" [true], "tag" "Quiet"}
               {"params" {},
                "children"
                [{"params" {}, "children" ["test.txt"], "tag" "Key"}],
                "tag" "Object"}
               {"params" {},
                "children"
                [{"params" {}, "children" ["test1.txt"], "tag" "Key"}],
                "tag" "Object"}],
              "tag" "Delete"}])]}
(fact "node to string"

  (!.js
    [(xml/to-string
      {"tag" "helo:test"
       "params" {},
       "children" []})
     (xml/to-string
      (xml/from-tree
       ["helo:test"
        ["ErrorCode" "ESB-00-000"]]))
     (xml/to-string
      (xml/from-tree
       ["helo:test"
        ["ErrorCode" "ESB-00-000"]
        ["A" ["A1" "Hello-11-222"] ["A2" "Bandung"]]
        ["B"]
        ["C"
         ["C1" "Satu"]
         ["C2" "Dua"]
         ["C3" "Tiga"]
         ["C4" ["C41" "Empat-Satu"]]]]))
     (xml/to-string
      (xml/from-tree
       ["B"]))
     (xml/to-string
      {"params" {},
       "children" [{"params" {}, "children" [true], "tag" "Quiet"}],
       "tag" "Delete"})
     (xml/to-string
      {"params" {},
       "children" [{"params" {}, "children" [true], "tag" "Quiet"}],
       "tag" "Delete"})
     (xml/to-string
      {"params" {},
       "children" [{"params" {}, "children" [true], "tag" "Quiet"}],
       "tag" "Delete"})
     (xml/from-tree
      ["Delete"
       ["Quiet" true]
       ["Object"
        ["Key" "test.txt"]]
       ["Object"
        ["Key" "test1.txt"]]])])
  => +out+

  (!.lua
    [(xml/to-string
      {"tag" "helo:test"
       "params" {},
       "children" []})
     (xml/to-string
      (xml/from-tree
       ["helo:test"
        ["ErrorCode" "ESB-00-000"]]))
     (xml/to-string
      (xml/from-tree
       ["helo:test"
        ["ErrorCode" "ESB-00-000"]
        ["A" ["A1" "Hello-11-222"] ["A2" "Bandung"]]
        ["B"]
        ["C"
         ["C1" "Satu"]
         ["C2" "Dua"]
         ["C3" "Tiga"]
         ["C4" ["C41" "Empat-Satu"]]]]))
     (xml/to-string
      (xml/from-tree
       ["B"]))
     (xml/to-string
      {"params" {},
       "children" [{"params" {}, "children" [true], "tag" "Quiet"}],
       "tag" "Delete"})
     (xml/to-string
      {"params" {},
       "children" [{"params" {}, "children" [true], "tag" "Quiet"}],
       "tag" "Delete"})
     (xml/to-string
      {"params" {},
       "children" [{"params" {}, "children" [true], "tag" "Quiet"}],
       "tag" "Delete"})
     (xml/from-tree
      ["Delete"
       ["Quiet" true]
       ["Object"
        ["Key" "test.txt"]]
       ["Object"
        ["Key" "test1.txt"]]])])
  => +out+

  (!.py
    [(xml/to-string
      {"tag" "helo:test"
       "params" {},
       "children" []})
     (xml/to-string
      (xml/from-tree
       ["helo:test"
        ["ErrorCode" "ESB-00-000"]]))
     (xml/to-string
      (xml/from-tree
       ["helo:test"
        ["ErrorCode" "ESB-00-000"]
        ["A" ["A1" "Hello-11-222"] ["A2" "Bandung"]]
        ["B"]
        ["C"
         ["C1" "Satu"]
         ["C2" "Dua"]
         ["C3" "Tiga"]
         ["C4" ["C41" "Empat-Satu"]]]]))
     (xml/to-string
      (xml/from-tree
       ["B"]))
     (xml/to-string
      {"params" {},
       "children" [{"params" {}, "children" [true], "tag" "Quiet"}],
       "tag" "Delete"})
     (xml/to-string
      {"params" {},
       "children" [{"params" {}, "children" [true], "tag" "Quiet"}],
       "tag" "Delete"})
     (xml/to-string
      {"params" {},
       "children" [{"params" {}, "children" [true], "tag" "Quiet"}],
       "tag" "Delete"})
     (xml/from-tree
      ["Delete"
       ["Quiet" true]
       ["Object"
        ["Key" "test.txt"]]
       ["Object"
        ["Key" "test1.txt"]]])])
  => +out+)

(comment
  
  (s/seedgen-benchadd '[xt.lang.parser-xml] {:lang [:dart :julia :ruby] :write true})
  (s/seedgen-langadd '[xt.lang.parser-xml] {:lang [:lua :python] :write true})
  (s/seedgen-langremove '[xt.lang.parser-xml] {:lang [:lua :python] :write true}))
