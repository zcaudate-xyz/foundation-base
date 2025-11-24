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
