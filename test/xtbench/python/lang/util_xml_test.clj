(ns
 xtbench.python.lang.util-xml-test
 (:require [std.lang :as l] [std.string.prose :as prose])
 (:use code.test))

(l/script-
 :python
 {:runtime :basic,
  :require [[xt.lang.util-xml :as xml] [xt.lang.common-lib :as k]]})

(fact:global {:setup [(l/rt:restart)], :teardown [(l/rt:stop)]})

^{:refer xt.lang.util-xml/to-tree, :added "4.0"}
(fact
 "to node to tree"
 ^{:hidden true}
 (!.py (xml/to-tree (xml/parse-xml "<a/>")))
 =>
 ["a"]
 (!.py (xml/to-tree (xml/parse-xml "<a>1</a>")))
 =>
 ["a" "1"]
 (!.py (xml/to-tree (xml/parse-xml "<a><b></b></a>")))
 =>
 ["a" ["b"]]
 (!.py (xml/to-tree (xml/parse-xml "<a>1<b>2</b>3</a>")))
 =>
 ["a" "1" ["b" "2"] "3"]
 (!.py
  (xml/to-tree
   (xml/parse-xml
    (@!
     (prose/|
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
 =>
 ["helo:test"
  ["ErrorCode" "ESB-00-000"]
  ["A" ["A1" "Hello-11-222"] ["A2" "Bandung"]]
  ["B"]
  ["C"
   ["C1" "Satu"]
   ["C2" "Dua"]
   ["C3" "Tiga"]
   ["C4" ["C41" "Empat-Satu"]]]])

^{:refer xt.lang.util-xml/to-brief, :added "4.0"}
(fact
 "xml to a more readable form"
 ^{:hidden true}
 (!.py
  (xml/to-brief
   (xml/parse-xml
    (@!
     (prose/|
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
 =>
 {"helo:test"
  {"C"
   {"C1" "Satu", "C2" "Dua", "C3" "Tiga", "C4" {"C41" "Empat-Satu"}},
   "ErrorCode" "ESB-00-000",
   "B" true,
   "A" {"A1" "Hello-11-222"}}}
 (!.py
  (xml/to-brief
   (xml/parse-xml
    (@!
     (std.html/html
      [:error
       [:code "BucketAlreadyOwnedByYou"]
       [:message
        "Your previous request to create the named bucket succeeded and you already own it."]
       [:bucketname "abc2"]
       [:resource "/abc2"]
       [:requestid "16FEAEEC2B5EF151"]
       [:hostid "5750e190-7dc8-4aab-abe9-13e39405c337"]])))))
 =>
 {"error"
  {"message"
   "Your previous request to create the named bucket succeeded and you already own it.",
   "resource" "/abc2",
   "bucketname" "abc2",
   "hostid" "5750e190-7dc8-4aab-abe9-13e39405c337",
   "requestid" "16FEAEEC2B5EF151",
   "code" "BucketAlreadyOwnedByYou"}})

^{:refer xt.lang.util-xml/to-string-params, :added "4.0"}
(fact
 "to node params"
 ^{:hidden true}
 (!.py (xml/to-string-params (tab [:a 1])))
 =>
 " a=1")

^{:refer xt.lang.util-xml/to-string, :added "4.0"}
(fact
 "node to string"
 ^{:hidden true}
 (!.py (xml/to-string {"tag" "helo:test", "params" {}, "children" []}))
 =>
 "<helo:test></helo:test>"
 (!.py
  (xml/to-string
   (xml/from-tree ["helo:test" ["ErrorCode" "ESB-00-000"]])))
 =>
 "<helo:test><ErrorCode>ESB-00-000</ErrorCode></helo:test>"
 (!.py
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
 =>
 "<helo:test><ErrorCode>ESB-00-000</ErrorCode><A><A1>Hello-11-222</A1><A2>Bandung</A2></A><B></B><C><C1>Satu</C1><C2>Dua</C2><C3>Tiga</C3><C4><C41>Empat-Satu</C41></C4></C></helo:test>"
 (!.py (xml/to-string (xml/from-tree ["B"])))
 =>
 "<B></B>"
 (!.py
  (xml/to-string
   {"params" {},
    "children" [{"params" {}, "children" [true], "tag" "Quiet"}],
    "tag" "Delete"}))
 =>
 "<Delete><Quiet>true</Quiet></Delete>"
 (!.py
  (xml/to-string
   {"params" {},
    "children" [{"params" {}, "children" [true], "tag" "Quiet"}],
    "tag" "Delete"}))
 =>
 "<Delete><Quiet>true</Quiet></Delete>"
 (!.py
  (xml/to-string
   {"params" {},
    "children" [{"params" {}, "children" [true], "tag" "Quiet"}],
    "tag" "Delete"}))
 =>
 "<Delete><Quiet>true</Quiet></Delete>"
 (!.py
  (xml/to-string
   (xml/from-tree
    ["Delete"
     ["Quiet" true]
     ["Object" ["Key" "test.txt"]]
     ["Object" ["Key" "test1.txt"]]])))
 =>
 "<Delete><Quiet>true</Quiet><Object><Key>test.txt</Key></Object><Object><Key>test1.txt</Key></Object></Delete>")

(comment)
