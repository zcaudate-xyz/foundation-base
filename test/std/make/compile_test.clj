(ns std.make.compile-test
  (:use code.test)
  (:require [std.make.compile :refer :all]
            [std.lang :as l])
  (:refer-clojure :exclude [compile]))

^{:refer std.make.compile/with:mock-compile :added "4.0"}
(fact "sets the mock output flag")

^{:refer std.make.compile/compile-fullbody :added "4.0"}
(fact "helper function for compile methods"
  ^:hidden
  
  (compile-fullbody "<BODY>" {:header "<HEADER>" :footer "<FOOTER>"})
  => "<HEADER>\n\n<BODY>\n\n<FOOTER>")

^{:refer std.make.compile/compile-out-path :added "4.0"}
(fact "creates the output path for file"
  ^:hidden
  
  (compile-out-path {:root ".build"
                     :target "src"
                     :file "pkg/file.lua"})
  => ".build/src/pkg/file.lua")

^{:refer std.make.compile/compile-write :added "4.0"}
(fact "writes body to the output path"
  ^:hidden
  
  (with:mock-compile
   (compile-write "hello.txt" "HELLO"))
  => ["hello.txt" "HELLO"])

^{:refer std.make.compile/compile-summarise :added "4.0"}
(fact "summaries the output"
  ^:hidden
  
  (compile-summarise [[:unchanged "hello.txt"]])
  => {:files 1, :status :unchanged})

^{:refer std.make.compile/compile-resource :added "4.0"}
(fact "copies resources to the build directory"
  ^:hidden
  
  (with:mock-compile
   (compile-resource 
    {:type :resource
     :target "assets"
     :main [["assets/std.make/tangle.sh" "tangle.make.sh"]]}))
  => coll?)

^{:refer std.make.compile/compile-resource :added "4.0"}
(fact "copies resources to the build directory"
  ^:hidden
  
  (with:mock-compile
   (compile-resource 
    {:type :resource
     :target "assets"
     :main [["assets/std.make/tangle.sh" "tangle.make.sh"]]}))
  => coll?)

^{:refer std.make.compile/compile-custom :added "4.0"}
(fact "creates a custom "
  ^:hidden
  
  (with:mock-compile
   (compile-custom {:root   ".build"
                    :target "src"
                    :file   "pkg/file.lua"
                    :header "HEADER"
                    :footer "FOOTER"
                    :body   "BODY"
                    :fn (fn [{:keys [body]}]
                          body)}))
  => [".build/src/pkg/file.lua"
      "HEADER\n\nBODY\n\nFOOTER"])

^{:refer std.make.compile/types-list :added "4.0"}
(fact "lists all compilation types"
  ^:hidden
  
  (set (types-list))
  => #{:contract.abi
       :contract.sol
       :custom
       :module.graph
       :module.schema
       :module.single
       :resource
       :script})

^{:refer std.make.compile/types-add :added "4.0"}
(fact "adds a compilation type")

^{:refer std.make.compile/types-remove :added "4.0"}
(fact "removes a compilation type")

^{:refer std.make.compile/compile-ext-fn :added "4.0"}
(fact "creates various formats "
  ^:hidden
  
  (with:mock-compile
   (compile-ext {:root   ".build"
                 :format :makefile 
                 :main  '[[:init [pnpm install --shamefully-hoist]]
                          [:dev  [yarn dev]]
                          [:package [yarn package]]
                          [:release {:- [package]} [yarn release]]]}))
  => [".build/Makefile"
      (std.string/|
       "init:"
       "\tpnpm install --shamefully-hoist"
       ""
       "dev:"
       "\tyarn dev"
       ""
       "package:"
       "\tyarn package"
       ""
       "release: package"
       "\tyarn release")]
  
  (with:mock-compile
   (compile-ext {:root   ".build"
                 :file "src/file.yaml"
                 :format :yaml
                 :main  '{:hello ["world"]}}))
  => [".build/src/file.yaml"
      "hello: [world]\n"]

  (with:mock-compile
   (compile-ext {:root   ".build"
                 :file "src/index.html"
                 :format :html
                 :main  '[:html [:div "hello world"]]}))
  => [".build/src/index.html"
      "<html>\n  <div>hello world</div>\n</html>"])

^{:refer std.make.compile/compile-resolve :added "4.0"}
(fact "resolves a symbol or pointer")

^{:refer std.make.compile/compile-ext :added "4.0"}
(fact "compiles project files of different extensions"
  ^:hidden
  
  (with:mock-compile
   (compile-ext {:format :makefile
                 :main [[:hello "world"]]}))
  => [nil "hello:\n\tworld"]

  (with:mock-compile
   (compile-ext {:format :gitignore
                 :main [[:hello "world"]]}))
  => [nil "hello world"]

  (with:mock-compile
   (compile-ext {:format :dockerfile
                 :main [[:hello "world"]]})))

^{:refer std.make.compile/compile-single :added "4.0"}
(fact "compiles a single file"
  ^:hidden
  
  (with:mock-compile
   (compile-single
    "<ROOT>/.build"
    nil
    {:main {:name "project"}, :type :package.json}))
  => ["<ROOT>/.build/package.json" "{\n  \"name\" : \"project\"\n}"])

^{:refer std.make.compile/compile-section :added "4.0"}
(fact "compiles section"
  ^:hidden
  
  (with:mock-compile
   (compile-section
    {:keys ["<ROOT>" ".build" nil nil],
     :as {:root "<ROOT>",
          :build ".build",
          :default
          [{:type :blank, :file "blank"}
           {:type :makefile, :main [[:hello [1 2]] [:world [1 2]]]}
           {:type :package.json, :main {:name "project"}}]}}
    :default
    [{:type :blank, :file "blank"}
     {:type :makefile, :main [[:hello [1 2]] [:world [1 2]]]}
     {:type :package.json, :main {:name "project"}}]))
  => '({:files 3, :status :changed,
        :written ([nil ""]
                  [nil "hello:\n\t1 2\n\nworld:\n\t1 2"]
                  [nil "{\n  \"name\" : \"project\"\n}"])}))

^{:refer std.make.compile/compile-directive :added "4.0"}
(fact "compiles directive"
  ^:hidden
  
  (with:mock-compile
   (compile-directive
    {:root "<ROOT>",
     :build ".build",
     :default
     [{:type :blank, :file "blank"}
      {:type :makefile, :main [[:hello [1 2]] [:world [1 2]]]}
      {:type :package.json, :main {:name "project"}}]}
    {:default
     [{:type :blank, :file "blank"}
      {:type :makefile, :main [[:hello [1 2]] [:world [1 2]]]}
      {:type :package.json, :main {:name "project"}}]}
    :default))
  => '({:files 3,
        :status :changed,
        :written
        (["<ROOT>/.build/blank" ""]
         ["<ROOT>/.build/Makefile" "hello:\n\t1 2\n\nworld:\n\t1 2"]
         ["<ROOT>/.build/package.json"
          "{\n  \"name\" : \"project\"\n}"])}))

^{:refer std.make.compile/compile :added "4.0"}
  (fact "creates files based on entries"
    ^:hidden
    
    (with:mock-compile
     (compile {:instance (atom {:root   "<ROOT>"
                                :build  ".build"
                                :default  [{:type :blank
                                            :file "blank"}
                                          {:type :makefile
                                           :main [[:hello [1 2]]
                                                  [:world [1 2]]]}
                                           {:type :package.json
                                            :main {:name "project"}}]})}))
    = '({:files 3,
         :status :changed,
         :written (["<ROOT>/.build/blank" ""]
                   ["<ROOT>/.build/Makefile" "hello:\n\t1 2\n\nworld:\n\t1 2"]
                   ["<ROOT>/.build/package.json" "{\n  \"name\" : \"project\"\n}"])}))


^{:refer std.make.compile/compile-directory :added "4.0"}
(fact "copies a directory to the build directory")