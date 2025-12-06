(ns code.tool.refactor.tamagui-test
  (:use code.test)
  (:require [code.tool.refactor.tamagui :as sut]
            [std.block.navigate :as edit]
            [code.query :as query]
            [std.lib.zip :as zip]
            [std.block.base :as base]
            [std.string :as str]))

^{:refer code.tool.refactor.tamagui/convert-color :added "4.1"}
(fact "converts color tokens to tailwind colors"
  (sut/convert-color "$red10") => "red-800"
  (sut/convert-color "$blue5") => "blue-400"
  (sut/convert-color "red") => "red")

^{:refer code.tool.refactor.tamagui/convert-value :added "4.1"}
(fact "converts property values to tailwind classes"
  (sut/convert-value "$red10" "bg-") => "bg-red-800"
  (sut/convert-value "4" "p-") => "p-4"
  (sut/convert-value "full" "rounded-") => "rounded-full"
  (sut/convert-value "2" "rounded-") => "rounded-md"
  (sut/convert-value "4" "rounded-") => "rounded-xl")

^{:refer code.tool.refactor.tamagui/convert-flex :added "4.1"}
(fact "converts flex shorthand to tailwind"
  (sut/convert-flex {:flex 1}) => "flex-1"
  (sut/convert-flex {:flex "1"}) => "flex-1"
  (sut/convert-flex {:flex 2}) => "flex-[2]")

^{:refer code.tool.refactor.tamagui/convert-justify :added "4.1"}
(fact "converts justify-content values"
  (sut/convert-justify "center") => "justify-center"
  (sut/convert-justify "space-between") => "justify-between"
  (sut/convert-justify "flex-start") => "justify-start")

^{:refer code.tool.refactor.tamagui/convert-align :added "4.1"}
(fact "converts align-items values"
  (sut/convert-align "center") => "items-center"
  (sut/convert-align "flex-start") => "items-start")

^{:refer code.tool.refactor.tamagui/process-props :added "4.1"}
(fact "processes component properties into className"
  (let [res (sut/process-props {:p "$4" :bg "red"} 'tm/Stack)
        cls (:className res)]
    (str/includes? cls "bg-red") => true
    (str/includes? cls "p-4") => true)

  (let [res (sut/process-props {:flex 1 :jc "center"} 'tm/XStack)
        cls (:className res)]
    (str/includes? cls "flex") => true
    (str/includes? cls "flex-row") => true
    (str/includes? cls "justify-center") => true
    (str/includes? cls "flex-1") => true))

^{:refer code.tool.refactor.tamagui/refactor-element :added "4.1"}
(fact "refactors tamagui element to new component and props"
  (sut/refactor-element [:tm/YStack {:p "$4"} "Content"])
  => [:tm/YStack {:p "$4"} "Content"] ;; not keyword tag

  (let [[tag attrs & children] (sut/refactor-element [:% 'tm/YStack {:p "$4"} "Content"])]
    tag => :div
    (str/includes? (:className attrs) "flex") => true
    (str/includes? (:className attrs) "flex-col") => true
    (str/includes? (:className attrs) "p-4") => true
    children => '("Content")))

^{:refer code.tool.refactor.tamagui/transform-zipper :added "4.1"}
(fact "transforms a zipper location containing a tamagui component"
  (let [zloc (edit/parse-root "[:% tm/Stack {:p \"$4\"}]")
        ;; Navigate to the vector if it's wrapped
        zloc (if (vector? (edit/value zloc)) zloc (edit/down zloc))]
    (-> (sut/transform-zipper zloc)
        (edit/root-string)))
  => "[:div {:className \"p-4\"}]")

^{:refer code.tool.refactor.tamagui/replace-require :added "4.1"}
(fact "replaces tamagui require with figma require"
  (let [zloc (edit/parse-root "[js.tamagui :as tm]")
        zloc (if (vector? (edit/value zloc)) zloc (edit/down zloc))]
    (-> (sut/replace-require zloc)
        (edit/root-string)))
  => "[js.lib.figma :as fg]")

^{:refer code.tool.refactor.tamagui/refactor-string :added "4.1"}
(fact "refactors a code string"
  (sut/refactor-string "[:% tm/Stack {:p \"$4\"}]")
  => "[:div {:className \"p-4\"}]")

^{:refer code.tool.refactor.tamagui/refactor-file :added "4.1"}
(fact "refactors a file"
  (with-redefs [spit (fn [p c] c)
                slurp (fn [p] "[:% tm/Stack {:p \"$4\"}]")]
    (sut/refactor-file "path/to/file.clj"))
  => "[:div {:className \"p-4\"}]")
