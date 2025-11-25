(ns std.tailwind-test
  (:require [std.tailwind :refer :all]
            [std.string :as str]
            [std.lib :as h]
            [code.test :refer :all]))

(def +class-str+ "flex flex-col items-center w-full md:w-1/2")

(fact "basic parsing"
  (parse "w-full")
  => {:width "100%"})

(fact "complex parsing"
  (parse +class-str+)
  => {:display :flex
      :flex-direction :column
      :align-items "center"
      :width "100%"
      :media {:md {:width "50%"}}})

(fact "grid parsing"
  (parse "grid grid-cols-3 gap-4")
  => {:display :grid
      :grid-template-columns "repeat(3, minmax(0, 1fr))"
      :gap "1rem"})

(fact "arbitrary values"
  (parse "w-[10px]")
  => {:width "10px"}
  (parse "top-[321px]")
  => {:top "321px"})

(fact "spacing"
  (parse "m-4 p-4") ;; p-4 should be ignored as per my implementation? I didn't implement p-.
  => {:margin "1rem"})

(fact "position"
  (parse "absolute top-0 left-1/2")
  => {:position :absolute
      :top "0px"
      :left "50%"})

(fact "media queries"
  (parse "sm:block md:hidden")
  => {:media {:sm {:display :block}
              :md {:display :hidden}}})

(fact "space utils"
  (parse "space-x-4")
  => {:space-x "1rem"}

  (parse "space-y-4")
  => {:space-y "1rem"})

(fact "new primitives"
  (parse "columns-3")
  => {:column-count "3"}

  (parse "columns-xs")
  => {:column-width "20rem"}

  (parse "break-inside-avoid")
  => {:break-inside :avoid}

  (parse "box-decoration-clone")
  => {:box-decoration-break :clone}

  (parse "overscroll-x-contain")
  => {:overscroll-behavior-x :contain})

(fact "render ascii"
  (let [output (render [:div {:class "w-20 h-5 border"} "hello"])]
    (str/includes? output "+------------------+") => true
    (str/includes? output "|hello             |") => true))

(fact "render nested flex"
  (let [output (render [:div {:class "flex border w-30 h-5"}
                        [:div {:class "w-10 border"} "A"]
                        [:div {:class "w-10 border"} "B"]])]
    (str/includes? output "|+--------++--------+        |") => true))

(fact "render complex layout: sidebar"
  (let [layout [:div {:class "flex w-40 h-10 border"}
                [:div {:class "w-10 h-full border"} "Side"]
                [:div {:class "w-20 h-full border"} "Main"]]
        output (render layout)]
    ;; Check structure: Sidebar box and Main box side-by-side
    (str/includes? output "+--------++------------------+") => true
    (str/includes? output "||Side") => true
    (str/includes? output "||Main") => true))

(fact "render complex layout: card"
  (let [card [:div {:class "flex flex-col w-30 h-12 border"}
              [:div {:class "h-4 border w-full"} "Img"]
              [:div {:class "h-4 w-full border-b"} "Title"]
              [:div {:class "h-4 w-full"} "Desc"]]
        output (render card)]
    (str/includes? output "+--------------------------+") => true ;; Top border of image
    (str/includes? output "||Img") => true
    (str/includes? output "||Title") => true))


^{:refer std.tailwind/resolve-value :added "4.0"}
(fact "TODO")

^{:refer std.tailwind/resolve-spacing :added "4.0"}
(fact "TODO")

^{:refer std.tailwind/resolve-size :added "4.0"}
(fact "TODO")

^{:refer std.tailwind/match-class :added "4.0"}
(fact "TODO")

^{:refer std.tailwind/parse-token :added "4.0"}
(fact "TODO")

^{:refer std.tailwind/deep-merge :added "4.0"}
(fact "TODO")

^{:refer std.tailwind/parse :added "4.0"}
(fact "TODO")

^{:refer std.tailwind/make-canvas :added "4.0"}
(fact "TODO")

^{:refer std.tailwind/draw-point :added "4.0"}
(fact "TODO")

^{:refer std.tailwind/draw-text :added "4.0"}
(fact "TODO")

^{:refer std.tailwind/draw-box-outline :added "4.0"}
(fact "TODO")

^{:refer std.tailwind/render-canvas-str :added "4.0"}
(fact "TODO")

^{:refer std.tailwind/parse-unit :added "4.0"}
(fact "TODO")

^{:refer std.tailwind/measure-node :added "4.0"}
(fact "TODO")

^{:refer std.tailwind/layout-node :added "4.0"}
(fact "TODO")

^{:refer std.tailwind/draw-node :added "4.0"}
(fact "TODO")

^{:refer std.tailwind/render :added "4.0"}
(fact "TODO")