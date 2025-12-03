(ns indigo.prompt.dsl-spec
  (:require [std.lib :as h]
            [std.lang :as l]
            [std.fs :as fs]
            [std.string :as str]
            [std.string.prose :as prose]
            [std.block.layout :as layout]))

(l/script :js
  {:import  [["lucide-react" :as #{Input Button}]
             ["react" :as React]
             ["react-native" :as [* ReactNative]]]
   :require [[js.react :as r]]})

(defn construct-item
  "constructs the item"
  {:added "4.0"}
  [form & [alts? desc?]]
  (let [[alts desc]  (cond (string? alts?)
                           [[] alts?]

                           :else
                           [alts? desc?])]
    {:op  :convert
     :desc desc
      :dsl [(str (layout/layout-main form))]
     :js  (vec (concat [(l/emit-script form {:lang :js})]
                       alts))}))

(defn construct-alts
  "constructs alternative forms that result in the same JS string"
  {:added "4.0"}
  [forms & [desc?]]
  {:op  :alternate
   :desc desc?
   :dsl (mapv (comp str layout/layout-main) forms)
   :js  (vec (set (mapv #(l/emit-script % {:lang :js})
                        forms)))})

(defn create-spec-description
  [{:keys [dsl js desc op]}]
  (str (if desc (str desc "\n"))
       "%JS\n"
       (str/join ""
                 (map (fn [val]
                        (str ": " (prose/indent-rest val 2) "\n"))
                      js))
       "%DSL\n"
       (str/join ""
                 (map (fn [val]
                        (str ": " (prose/indent-rest val 2) "\n"))
                      dsl))))

(defn create-spec-main
  [meta example-forms inputs example-files]
  
  (str "\n\n" meta "\n\n"
       
       (->> (partition 2 example-forms)
            (map (fn [[k arr]]
                   (str "### " (str/capital-case (name k))
                        "\n\n"
                        (str/join "\n\n"
                                  (map create-spec-description arr)))))
            (str/join "\n\n"))

       "\n\n"
       (str/join "\n\n" (map slurp inputs))
       "\n\n"
       (->> example-files
            (map (fn [[k [clj-file
                          js-file]]]
                   (str "### Example - " (str/capital-case (name k)) "\n"
                        "```clojure\n"
                        clj-file
                        "```\n"

                        "```javascript\n"
                        js-file
                        "```\n")))
            (str/join "\n\n"))))


(def +meta+
  ["# **std.lang (JS) DSL Specification**"
   ""
   "**Objective:** This document defines the syntax, conventions, and"
   " constraints for writing code using the Clojure-based Javascript DSL (JS DSL),"
   " based on the std.lang transpiler"
   ""
   "The dsl format is given as <DESC>\\n %JS ^: <E1> ^: <E2> %DSL ^: <T1> ^: <T2>"
   "where ^: is the start of a new line with a colon. This gives the spec for how to translate"
   "JS code to std.lang DSL. please follow the patterns, including precidence of "
   ""
   "For each of the code patterns in the JS section, translate to the DSL pattern, if"
   "there are multiple variations of DSL code, pick the DSL first over the rest."
   ""
   "In general, always pick the most succinct and closest translation to the original and"
   "make sure that there are the least possible parens in the final form."
   ""
   "When it is possible to follow the interop, follow the interop. Don't use library"
   "code."])

(defn spec-example-files
  []
  {:library-browser
   [(slurp "src/code/dev/webapp/layout_library_browser.clj")
    (slurp ".build/indigo/src/webapp/layout-library-browser.jsx")]})

(defn spec-example-forms
  []
  [:primitives
   [(construct-item nil)
    (construct-item 'undefined)
    (construct-item true)
    (construct-item false)
    (construct-item "hello")
    (construct-item 123)
    (construct-item 45.6)
    (construct-item 'NaN)
    (construct-item #"^he.*llo$")]

   :comparison
   [(construct-item '(<= a b))
    (construct-item '(== a b))
    (construct-item '(=== a b))
    (construct-item '(not= a b))
    (construct-item '(not== a b))
    (construct-item '(> a b))
    (construct-item '(>= a b))]

   :assignment
   [(construct-item '(:= a 10))
    (construct-item '(:++ a))
    (construct-item '(:-- a ))
    (construct-item '(:+= a 2))
    (construct-item '(:-= a 2))
    (construct-item '(:*= a 2))]
   
   :logical
   [(construct-item '(not condition))
    (construct-item '(or a b c))
    (construct-item '(and a b c))
    (construct-item '(:? condition true-val false-val))]

   :math
   [(construct-item '(+ a b))
    (construct-item '(+ a b c))
    (construct-item '(- a))
    (construct-item '(- a b))
    (construct-item '(- a b c))
    (construct-item '(* a b))
    (construct-item '(* a b c))
    (construct-item '(/ a b))
    (construct-item '(/ a b c))
    (construct-item '(pow base exp))
    (construct-item '(mod a b))]

   :bit-wise
   [(construct-item '(b:& a b))
    (construct-item '(b:<< a b))
    (construct-item '(b:>> a b))
    (construct-item '(b:xor a b))
    (construct-item '(b:| a b))]

   :arrays
   [(construct-item '[a b c])
    (construct-item '[a b c (:.. more)])]

   :objects
   [(construct-item '{:a a :b b :c coll})
    (construct-item '(del (. obj prop)))
    (construct-item 'this.abc)
    (construct-item '(super.method))]

   :assignment
   [(construct-item '(var x 1)
                    ["var x = 1"
                     "const x = 1"])
    (construct-item '(var {:# [a b c]} opts))
    (construct-item '(var [a b c] arr))]
   
   :functions
   [(construct-item '{:# [a b]
                      :c coll
                      :.. props}
                    (str "The map object syntax has two special keys `:#` and `:..`"
                         "`:#` collects all the non keyed symbol"
                         "`:..` collects the spread operator for the map" )
                    )
    (construct-item '{:# [a b c]
                      :.. props})
    (construct-item '(async (fn [{:# [a b]
                                  :c col :d dog
                                  :.. props}]
                              (return a))))
    (construct-item '(await (fn [{:# [a b]
                                  :c col :d dog
                                  :.. props}] (return a))))
    (construct-item '(fn []
                       (return 1))
                    ["() => 1"])
    (construct-item '(fn [err res]
                       (return (* res 1)))
                    ["(err, res) => res * 1"])
    
    
    (construct-item '(fn [(:= a 1)
                          (:= b 2)]
                       (return (* a b))))
    (construct-item '(fn [{:# [a b c d e]
                           :.. props}]
                       (return (* a b props.item.c))))]

   :access
   [(construct-alts '[this.prop
                      (. this prop)])
    
    (construct-alts '[(. this prop long [1] (call))
                      (. this.prop.long [1] (call))
                      #_(. (. (. (. this prop) long) [1]) (call))])
    (construct-item 'Array.from)
    (construct-item '(. Array from))
    (construct-alts ['(obj.item.doSomething 1 2)
                     '(. obj item (doSomething 1 2))])
    (construct-item '(. notation can be ["done"] (like 1) [0] thisway))]
   
   
   :control-flow
   [(construct-item '(break))
    (construct-item '(return value))
    (construct-item '(yield value)) ;; Used in generator functions
    (construct-item '(for [(var i 0) (< i 3) (:++ i)] (console.log i)))
    (construct-item '(if condition (console.log "true") (console.log "false")))
    (construct-item '(when condition
                       (console.log "true")
                       (console.log "more")))
    (construct-item '(cond (== x 1)
                           (do (:= a 1))

                           :else
                           (do (:= b 1))))
    
    (construct-item '(while condition (console.log "loop")))
    (construct-item '(case val
                       "a" (return a)
                       "b" (do (:= x 1)
                               (break))))
    
    (construct-item '(throw (new Error "message")))
    (construct-item '(try (do-something) (catch e (console.log e))))]
   
   :class
   [(construct-item '(instanceof obj Type))
    (construct-item '(typeof v))
    (construct-item '(new Constructor a b c))]
   
   :async
   [(construct-item '(async (fn [{:# [a b c d e f]
                                  :.. more}] (return a))))
    (construct-item '(await ('((async (fn [{:# [a b c d e f]
                                            :.. more}] (return a))))
                             {:a 1 :b 2})))]])

(defn create-spec
  "creates the actual spec"
  {:added "4.0"}
  []
  (create-spec-main (str/join-lines +meta+)
                    (spec-example-forms)
                    ["resources/assets/indigo/prompts/js_dsl_ns_spec.md"
                     "resources/assets/indigo/prompts/js_dsl_figma_amendments.md"
                     "resources/assets/indigo/prompts/js_dsl_amendments.md"
                     "resources/assets/indigo/prompts/js_dsl_amendments_2.md"
                     "resources/assets/indigo/prompts/js_dsl_amendments_3.md"
                     "resources/assets/indigo/prompts/js_dsl_amendments_4.md"]
                    
                    {} #_(spec-example-files)))

(comment
  
  (spit "dsl_spec_baseline.md"
        (create-spec))

  (spit "/Users/zcaudate/Development/greenways/Smalltalkinterfacedesign/translate_dsl.md"
        (create-spec))
  (spit "/Users/zcaudate/Development/greenways/Szncampaigncenter/translate_dsl.md"
        (create-spec))
  
  "Given the spec in @translate_dsl, translate src/App.jsx and all files in src/components/** to the std.lang dsl to src-translated. follow the original directory layout. please generate file in compilation order"

  "Given the spec in @translate_dsl, translate src/App.jsx and all files in src/components/** and src/lib/** to the std.lang dsl to src-translated. follow the original directory layout. look at the dependency map for the project and generate file in compilation order from least dependent to most"


  "Given the spec in @translate_dsl, translate src/App.jsx and all files in src/lib/** and src/components/* to the std.lang dsl to src-translated. follow the original directory layout and do not translate the subdirectories. look at the dependency map for the project and generate file in compilation order from least dependent to most"

  "Given the spec in @translate_dsl, translate src/App.jsx and all files in src/lib/** and src/components/* (DO NOT FOLLOW SUBDIRECTORIES) to the std.lang dsl to src-translated. DO NOT translate src/components/ui/* follow the original directory layout.  generate file in compilation order from least dependent to most"

  "Given the spec in @translate_dsl, translate src/App.jsx and all files in src/lib/** and src/components/* (DO NOT FOLLOW SUBDIRECTORIES) to the std.lang dsl to src-translated. DO NOT translate src/components/ui/* follow the original directory layout. "
  
  "I'd like to package exports in src/ui/components/* into a single .tsx file"
  )
