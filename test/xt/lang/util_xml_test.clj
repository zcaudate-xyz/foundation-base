(ns xt.lang.util-xml-test
  (:require [std.lang :as l]
            [std.string.prose :as prose])
  (:use code.test))

(l/script- :lua
  {:runtime :basic
   :require [[xt.lang.util-xml :as xml]
             [xt.lang.base-lib :as k]]})

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer xt.lang.util-xml/parse-xml-params :added "4.0"}
(fact "parses the args"
  ^:hidden
  
  (xml/parse-xml-params "a='123', b=\"456\" ")
  => {"a" "123", "b" "456"})

^{:refer xt.lang.util-xml/parse-xml-stack :added "4.0"}
(fact "parses the xml into a ast stack"
  ^:hidden

  (xml/parse-xml-stack "<a/>")
  => [{"tag" "a", "empty" true}]

  (xml/parse-xml-stack "<a></a>")
  => [{"tag" "a"}
      {"tag" "a", "close" true}]
  
  (xml/parse-xml-stack "<a>1</a>")
  => [{"tag" "a"}
      {"tag" "a", "text" "1", "close" true}]
  
  (xml/parse-xml-stack "<a><b></b></a>")
  => [{"tag" "a"}
      {"tag" "b"}
      {"tag" "b", "close" true}
      {"tag" "a", "close" true}]

  (xml/parse-xml-stack "<a>1<b>2</b>3</a>")
  => [{"tag" "a"}
      {"tag" "b", "text" "1"}
      {"tag" "b", "text" "2", "close" true}
      {"tag" "a", "text" "3", "close" true}]

  (xml/parse-xml-stack "<a><b/></a>")
  => [{"tag" "a"}
      {"tag" "b", "empty" true}
      {"tag" "a", "close" true}]

  (xml/parse-xml-stack "<a><b/><b>2</b></a>")
  => [{"tag" "a"}
      {"tag" "b", "empty" true}
      {"tag" "b"}
      {"tag" "b", "text" "2", "close" true}
      {"tag" "a", "close" true}])

^{:refer xt.lang.util-xml/to-node-normalise :added "4.0"}
(fact "normalises the node for viewing")

^{:refer xt.lang.util-xml/to-node :added "4.0"}
(fact "transforms stack to node"
  ^:hidden
  
  (xml/to-node
   [{"tag" "a", "empty" true}])
  => {"tag" "a"}

  (xml/to-node
   [{"tag" "a"}
    {"tag" "a", "close" true}])
  => {"tag" "a"}

  (xml/to-node
   [{"tag" "a"}
    {"tag" "a", "text" "1", "close" true}])
  => {"children" ["1"], "tag" "a"}

  (xml/to-node
   [{"tag" "a"}
    {"tag" "b"}
    {"tag" "b", "close" true}
    {"tag" "a", "close" true}])
  => {"children" [{"tag" "b"}], "tag" "a"}

  (xml/to-node
   [{"tag" "a"}
    {"tag" "b", "text" "1"}
    {"tag" "b", "text" "2", "close" true}
    {"tag" "a", "text" "3", "close" true}])
  => {"children" ["1" {"children" ["2"], "tag" "b"} "3"],
      "tag" "a"}

  (xml/to-node
   [{"tag" "a"}
    {"tag" "b", "empty" true}
    {"tag" "a", "close" true}])
  => {"children" [{"tag" "b"}], "tag" "a"}

  (xml/to-node
   [{"tag" "a"}
    {"tag" "b", "empty" true}
    {"tag" "b"}
    {"tag" "b", "text" "2", "close" true}
    {"tag" "a", "close" true}])
  => {"children" [{"tag" "b"}
                  {"children" ["2"], "tag" "b"}],
      "tag" "a"})

^{:refer xt.lang.util-xml/parse-xml :added "4.0"}
(fact "parses xml"
  ^:hidden
  
  (xml/parse-xml "<a/>")
  => {"tag" "a"}
  
  (xml/parse-xml "<a></a>")
  => {"tag" "a"}
  
  (xml/parse-xml "<a>1</a>")
  => {"children" ["1"], "tag" "a"}
  
  (xml/parse-xml "<a><b></b></a>")
  => {"children" [{"tag" "b"}], "tag" "a"}

  (xml/parse-xml "<a>1<b>2</b>3</a>")
  => {"children" ["1" {"children" ["2"], "tag" "b"} "3"], "tag" "a"}

  (xml/parse-xml "<a><b/></a>")
  => {"children" [{"tag" "b"}], "tag" "a"}

  (xml/parse-xml "<a><b/><b>2</b></a>")
  => {"children" [{"tag" "b"} {"children" ["2"], "tag" "b"}], "tag" "a"})

^{:refer xt.lang.util-xml/to-tree :added "4.0"}
(fact "to node to tree"
  ^:hidden
  
  (!.lua
   (xml/to-tree
    (xml/parse-xml "<a/>")))
  => ["a"]

  (!.lua
   (xml/to-tree
    (xml/parse-xml "<a>1</a>")))
  => ["a" "1"]

  (!.lua
   (xml/to-tree
    (xml/parse-xml "<a><b></b></a>")))
  => ["a" ["b"]]

  (!.lua
   (xml/to-tree
    (xml/parse-xml "<a>1<b>2</b>3</a>")))
  => ["a" "1" ["b" "2"] "3"]
  
  (!.lua
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
          "</helo:test>")))))
  => ["helo:test"
      ["ErrorCode" "ESB-00-000"]
      ["A" ["A1" "Hello-11-222"] ["A2" "Bandung"]]
      ["B"]
      ["C"
       ["C1" "Satu"]
       ["C2" "Dua"]
       ["C3" "Tiga"]
       ["C4" ["C41" "Empat-Satu"]]]])

^{:refer xt.lang.util-xml/from-tree :added "4.0"}
(fact "creates nodes from tree"
  ^:hidden
  
  (xml/from-tree
   ["helo:test"
    ["ErrorCode" "ESB-00-000"]
    ["A" ["A1" "Hello-11-222"] ["A2" "Bandung"]]
    ["B"]
    ["C"
     ["C1" "Satu"]
     ["C2" "Dua"]
     ["C3" "Tiga"]
     ["C4" ["C41" "Empat-Satu"]]]])
  => {"params" {},
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
      "tag" "helo:test"})

^{:refer xt.lang.util-xml/to-brief :added "4.0"}
(fact "xml to a more readable form"
  ^:hidden

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
  => {"helo:test"
      {"C"
       {"C1" "Satu",
        "C2" "Dua",
        "C3" "Tiga",
        "C4" {"C41" "Empat-Satu"}},
       "ErrorCode" "ESB-00-000",
       "B" true,
       "A" {"A1" "Hello-11-222"}}}
  
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
  => {"error"
      {"message"
       "Your previous request to create the named bucket succeeded and you already own it.",
       "resource" "/abc2",
       "bucketname" "abc2",
       "hostid" "5750e190-7dc8-4aab-abe9-13e39405c337",
       "requestid" "16FEAEEC2B5EF151",
       "code" "BucketAlreadyOwnedByYou"}})

^{:refer xt.lang.util-xml/to-string-params :added "4.0"}
(fact "to node params"
  ^:hidden

  (!.lua
   (xml/to-string-params (tab [:a 1])))
  => " a=1")

^{:refer xt.lang.util-xml/to-string :added "4.0"}
(fact "node to string"
  ^:hidden
  
  (!.lua
   (xml/to-string
    {"tag" "helo:test"
     "params" {},
     "children" []}))
  => "<helo:test></helo:test>"

  (!.lua
   (xml/to-string
    (xml/from-tree
     ["helo:test"
      ["ErrorCode" "ESB-00-000"]])))
  => "<helo:test><ErrorCode>ESB-00-000</ErrorCode></helo:test>"

  (!.lua
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
       ["C4" ["C41" "Empat-Satu"]]]])))
  => "<helo:test><ErrorCode>ESB-00-000</ErrorCode><A><A1>Hello-11-222</A1><A2>Bandung</A2></A><B></B><C><C1>Satu</C1><C2>Dua</C2><C3>Tiga</C3><C4><C41>Empat-Satu</C41></C4></C></helo:test>"
  
  (!.lua
   (xml/to-string
    (xml/from-tree
     ["B"])))
  => "<B></B>"

  (!.lua
   (xml/to-string
    {"params" {},
     "children" [{"params" {}, "children" [true], "tag" "Quiet"}],
     "tag" "Delete"}))
  => "<Delete><Quiet>true</Quiet></Delete>"

  (!.lua
   (xml/to-string
    {"params" {},
     "children" [{"params" {}, "children" [true], "tag" "Quiet"}],
     "tag" "Delete"}))
  => "<Delete><Quiet>true</Quiet></Delete>"

  (!.lua
   (xml/to-string
    {"params" {},
     "children" [{"params" {}, "children" [true], "tag" "Quiet"}],
     "tag" "Delete"}))
  => "<Delete><Quiet>true</Quiet></Delete>"

  (!.lua
   (xml/to-string
    (xml/from-tree
     ["Delete"
      ["Quiet" true]
      ["Object"
       ["Key" "test.txt"]]
      ["Object"
       ["Key" "test1.txt"]]])))
  => "<Delete><Quiet>true</Quiet><Object><Key>test.txt</Key></Object><Object><Key>test1.txt</Key></Object></Delete>")


(comment
  )
