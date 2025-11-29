(ns std.tailwind
  (:require [std.string :as str]
            [std.lib :as h]))

(def +media+
  {:sm "640px"
   :md "768px"
   :lg "1024px"
   :xl "1280px"
   :2xl "1536px"})

(def +spacing-scale+
  {"0" "0px"
   "px" "1px"
   "0.5" "0.125rem"
   "1" "0.25rem"
   "1.5" "0.375rem"
   "2" "0.5rem"
   "2.5" "0.625rem"
   "3" "0.75rem"
   "3.5" "0.875rem"
   "4" "1rem"
   "5" "1.25rem"
   "6" "1.5rem"
   "7" "1.75rem"
   "8" "2rem"
   "9" "2.25rem"
   "10" "2.5rem"
   "11" "2.75rem"
   "12" "3rem"
   "14" "3.5rem"
   "16" "4rem"
   "20" "5rem"
   "24" "6rem"
   "28" "7rem"
   "32" "8rem"
   "36" "9rem"
   "40" "10rem"
   "44" "11rem"
   "48" "12rem"
   "52" "13rem"
   "56" "14rem"
   "60" "15rem"
   "64" "16rem"
   "72" "18rem"
   "80" "20rem"
   "96" "24rem"})

(def +fraction-scale+
  {"1/2" "50%"
   "1/3" "33.333333%"
   "2/3" "66.666667%"
   "1/4" "25%"
   "2/4" "50%"
   "3/4" "75%"
   "1/5" "20%"
   "2/5" "40%"
   "3/5" "60%"
   "4/5" "80%"
   "1/6" "16.666667%"
   "2/6" "33.333333%"
   "3/6" "50%"
   "4/6" "66.666667%"
   "5/6" "83.333333%"
   "1/12" "8.333333%"
   "2/12" "16.666667%"
   "3/12" "25%"
   "4/12" "33.333333%"
   "5/12" "41.666667%"
   "6/12" "50%"
   "7/12" "58.333333%"
   "8/12" "66.666667%"
   "9/12" "75%"
   "10/12" "83.333333%"
   "11/12" "91.666667%"
   "full" "100%"
   "screen" "100vh"
   "min" "min-content"
   "max" "max-content"
   "fit" "fit-content"
   "auto" "auto"})

(def +columns-scale+
  {"auto" "auto"
   "1" "1" "2" "2" "3" "3" "4" "4" "5" "5" "6" "6"
   "7" "7" "8" "8" "9" "9" "10" "10" "11" "11" "12" "12"
   "3xs" "16rem"
   "2xs" "18rem"
   "xs" "20rem"
   "sm" "24rem"
   "md" "28rem"
   "lg" "32rem"
   "xl" "36rem"
   "2xl" "42rem"
   "3xl" "48rem"
   "4xl" "56rem"
   "5xl" "64rem"
   "6xl" "72rem"
   "7xl" "80rem"})

(defn resolve-value [val scale & [default]]
  (cond (get scale val) (get scale val)
        (re-matches #"\[.*\]" val) (subs val 1 (dec (count val)))
        default default
        :else val))

(defn resolve-spacing [val]
  (resolve-value val +spacing-scale+ val))

(defn resolve-size [val]
  (resolve-value val (merge +spacing-scale+ +fraction-scale+) val))

(def +layout-matchers+
  [;; Display
   {:regex #"^(block|inline-block|inline|flex|inline-flex|table|inline-table|table-caption|table-cell|table-column|table-column-group|table-footer-group|table-header-group|table-row-group|table-row|flow-root|grid|inline-grid|contents|list-item|hidden)$"
    :fn (fn [[_ v]] {:display (keyword v)})}

   ;; Columns
   {:regex #"^columns-(.+)$"
    :fn (fn [[_ v]] (let [val (resolve-value v +columns-scale+ v)]
                      (if (re-matches #"\d+" val)
                        {:column-count val}
                        {:column-width val})))}

   ;; Break
   {:regex #"^break-after-(.+)$"
    :fn (fn [[_ v]] {:break-after (keyword v)})}
   {:regex #"^break-before-(.+)$"
    :fn (fn [[_ v]] {:break-before (keyword v)})}
   {:regex #"^break-inside-(.+)$"
    :fn (fn [[_ v]] {:break-inside (keyword v)})}

   ;; Box Decoration Break
   {:regex #"^box-decoration-(slice|clone)$"
    :fn (fn [[_ v]] {:box-decoration-break (keyword v)})}

   ;; Box Sizing
   {:regex #"^box-(border|content)$"
    :fn (fn [[_ v]] {:box-sizing (keyword (str v "-box"))})}

   ;; Floats / Clear
   {:regex #"^float-(right|left|none)$"
    :fn (fn [[_ v]] {:float (keyword v)})}
   {:regex #"^clear-(left|right|both|none)$"
    :fn (fn [[_ v]] {:clear (keyword v)})}

   ;; Isolation
   {:regex #"^(isolate|isolation-auto)$"
    :fn (fn [[_ v]] {:isolation (if (= v "isolate") "isolate" "auto")})}

   ;; Object Fit / Position
   {:regex #"^object-(contain|cover|fill|none|scale-down)$"
    :fn (fn [[_ v]] {:object-fit (keyword v)})}
   {:regex #"^object-(.+)$"
    :fn (fn [[_ v]] {:object-position (str/replace v "-" " ")})}

   ;; Overflow
   {:regex #"^overflow-(auto|hidden|clip|visible|scroll)$"
    :fn (fn [[_ v]] {:overflow (keyword v)})}
   {:regex #"^overflow-([xy])-(auto|hidden|clip|visible|scroll)$"
    :fn (fn [[_ axis v]] {(keyword (str "overflow-" axis)) (keyword v)})}

   ;; Overscroll
   {:regex #"^overscroll-(auto|contain|none)$"
    :fn (fn [[_ v]] {:overscroll-behavior (keyword v)})}
   {:regex #"^overscroll-([xy])-(auto|contain|none)$"
    :fn (fn [[_ axis v]] {(keyword (str "overscroll-behavior-" axis)) (keyword v)})}

   ;; Position
   {:regex #"^(static|fixed|absolute|relative|sticky)$"
    :fn (fn [[_ v]] {:position (keyword v)})}

   ;; Top / Right / Bottom / Left / Inset
   {:regex #"^inset-(.+)$"
    :fn (fn [[_ v]] (let [s (resolve-value v (merge +spacing-scale+ +fraction-scale+) v)] {:top s :right s :bottom s :left s}))}
   {:regex #"^inset-x-(.+)$"
    :fn (fn [[_ v]] (let [s (resolve-value v (merge +spacing-scale+ +fraction-scale+) v)] {:left s :right s}))}
   {:regex #"^inset-y-(.+)$"
    :fn (fn [[_ v]] (let [s (resolve-value v (merge +spacing-scale+ +fraction-scale+) v)] {:top s :bottom s}))}
   {:regex #"^(top|right|bottom|left)-(.+)$"
    :fn (fn [[_ prop v]] {(keyword prop) (resolve-value v (merge +spacing-scale+ +fraction-scale+) v)})}

   ;; Visibility
   {:regex #"^(visible|invisible|collapse)$"
    :fn (fn [[_ v]] {:visibility (keyword v)})}

   ;; Z-Index
   {:regex #"^z-(.+)$"
    :fn (fn [[_ v]] {:z-index (resolve-value v nil v)})}

   ;; Flexbox & Grid
   {:regex #"^flex-(row|col)(-reverse)?$"
    :fn (fn [[_ dir rev]] {:flex-direction (keyword (str (if (= dir "col") "column" dir) (if rev "-reverse" "")))})}
   {:regex #"^flex-(wrap|wrap-reverse|nowrap)$"
    :fn (fn [[_ v]] {:flex-wrap (keyword v)})}
   {:regex #"^flex-(1|auto|initial|none)$"
    :fn (fn [[_ v]] {:flex (condp = v "1" "1 1 0%" "auto" "1 1 auto" "initial" "0 1 auto" "none" "none")})}
   {:regex #"^grow(?:-(0))?$"
    :fn (fn [[_ v]] {:flex-grow (if v 0 1)})}
   {:regex #"^shrink(?:-(0))?$"
    :fn (fn [[_ v]] {:flex-shrink (if v 0 1)})}
   {:regex #"^order-(.+)$"
    :fn (fn [[_ v]] {:order (resolve-value v {"first" "-9999" "last" "9999" "none" "0"} v)})}
   {:regex #"^grid-cols-(.+)$"
    :fn (fn [[_ v]] {:grid-template-columns (if (= v "none") "none" (if (re-matches #"\d+" v) (str "repeat(" v ", minmax(0, 1fr))") (resolve-value v nil)))})}
   {:regex #"^grid-rows-(.+)$"
    :fn (fn [[_ v]] {:grid-template-rows (if (= v "none") "none" (if (re-matches #"\d+" v) (str "repeat(" v ", minmax(0, 1fr))") (resolve-value v nil)))})}
   {:regex #"^col-(auto|span-.+|start-.+|end-.+)$"
    :fn (fn [[_ v]] {:grid-column (cond (= v "auto") "auto"
                                       (str/starts-with? v "span-") (let [n (subs v 5)] (if (= n "full") "1 / -1" (str "span " n " / span " n)))
                                       (str/starts-with? v "start-") (subs v 6)
                                       (str/starts-with? v "end-") (subs v 4))})}
   {:regex #"^row-(auto|span-.+|start-.+|end-.+)$"
    :fn (fn [[_ v]] {:grid-row (cond (= v "auto") "auto"
                                    (str/starts-with? v "span-") (let [n (subs v 5)] (if (= n "full") "1 / -1" (str "span " n " / span " n)))
                                    (str/starts-with? v "start-") (subs v 6)
                                    (str/starts-with? v "end-") (subs v 4))})}
   {:regex #"^grid-flow-(.+)$"
    :fn (fn [[_ v]] {:grid-auto-flow (str/replace v "-" " ")})}
   {:regex #"^auto-cols-(.+)$"
    :fn (fn [[_ v]] {:grid-auto-columns (resolve-value v {"auto" "auto" "min" "min-content" "max" "max-content" "fr" "minmax(0, 1fr)"})})}
   {:regex #"^auto-rows-(.+)$"
    :fn (fn [[_ v]] {:grid-auto-rows (resolve-value v {"auto" "auto" "min" "min-content" "max" "max-content" "fr" "minmax(0, 1fr)"})})}
   {:regex #"^gap-(.+)$"
    :fn (fn [[_ v]] {:gap (resolve-spacing v)})}
   {:regex #"^gap-x-(.+)$"
    :fn (fn [[_ v]] {:column-gap (resolve-spacing v)})}
   {:regex #"^gap-y-(.+)$"
    :fn (fn [[_ v]] {:row-gap (resolve-spacing v)})}

   ;; Alignment
   {:regex #"^justify-(start|end|center|between|around|evenly|stretch)$"
    :fn (fn [[_ v]] {:justify-content (if (contains? #{"start" "end"} v) (str "flex-" v) v)})}
   {:regex #"^justify-items-(start|end|center|stretch)$"
    :fn (fn [[_ v]] {:justify-items v})}
   {:regex #"^justify-self-(auto|start|end|center|stretch)$"
    :fn (fn [[_ v]] {:justify-self v})}
   {:regex #"^content-(start|end|center|between|around|evenly|baseline)$" ;; Tailwind content- alignment
    :fn (fn [[_ v]] {:align-content (if (contains? #{"start" "end"} v) (str "flex-" v) v)})}
   {:regex #"^items-(start|end|center|baseline|stretch)$"
    :fn (fn [[_ v]] {:align-items (if (contains? #{"start" "end"} v) (str "flex-" v) v)})}
   {:regex #"^self-(auto|start|end|center|stretch|baseline)$"
    :fn (fn [[_ v]] {:align-self (if (contains? #{"start" "end"} v) (str "flex-" v) v)})}
   {:regex #"^place-content-(.+)$"
    :fn (fn [[_ v]] {:place-content (str/replace v "-" " ")})}
   {:regex #"^place-items-(.+)$"
    :fn (fn [[_ v]] {:place-items (str/replace v "-" " ")})}
   {:regex #"^place-self-(.+)$"
    :fn (fn [[_ v]] {:place-self (str/replace v "-" " ")})}

   ;; Sizing
   {:regex #"^w-(.+)$"
    :fn (fn [[_ v]] {:width (resolve-size v)})}
   {:regex #"^min-w-(.+)$"
    :fn (fn [[_ v]] {:min-width (resolve-size v)})}
   {:regex #"^max-w-(.+)$"
    :fn (fn [[_ v]] {:max-width (resolve-size v)})}
   {:regex #"^h-(.+)$"
    :fn (fn [[_ v]] {:height (resolve-size v)})}
   {:regex #"^min-h-(.+)$"
    :fn (fn [[_ v]] {:min-height (resolve-size v)})}
   {:regex #"^max-h-(.+)$"
    :fn (fn [[_ v]] {:max-height (resolve-size v)})}
   {:regex #"^aspect-(.+)$"
    :fn (fn [[_ v]] {:aspect-ratio (resolve-value v {"auto" "auto" "square" "1 / 1" "video" "16 / 9"})})}

   ;; Spacing (Margin)
   {:regex #"^m-(.+)$"
    :fn (fn [[_ v]] {:margin (resolve-spacing v)})}
   {:regex #"^mx-(.+)$"
    :fn (fn [[_ v]] (let [s (resolve-spacing v)] {:margin-left s :margin-right s}))}
   {:regex #"^my-(.+)$"
    :fn (fn [[_ v]] (let [s (resolve-spacing v)] {:margin-top s :margin-bottom s}))}
   {:regex #"^mt-(.+)$"
    :fn (fn [[_ v]] {:margin-top (resolve-spacing v)})}
   {:regex #"^mr-(.+)$"
    :fn (fn [[_ v]] {:margin-right (resolve-spacing v)})}
   {:regex #"^mb-(.+)$"
    :fn (fn [[_ v]] {:margin-bottom (resolve-spacing v)})}
   {:regex #"^ml-(.+)$"
    :fn (fn [[_ v]] {:margin-left (resolve-spacing v)})}
   {:regex #"^space-x-(.+)$"
    :fn (fn [[_ v]] {:space-x (resolve-spacing v)})}
   {:regex #"^space-([xy])-(.+)$"
    :fn (fn [[_ axis v]] {(keyword (str "space-" axis)) (resolve-spacing v)})}
   ])

(defn match-class [cls]
  (reduce (fn [_ {:keys [regex fn]}]
            (if-let [match (re-find regex cls)]
              (reduced (fn match))
              nil))
          nil
          +layout-matchers+))

(defn parse-token [token]
  (let [[_ mod base] (re-find #"(?:(sm|md|lg|xl|2xl):)?(.+)" token)]
    (if (and base (not (str/blank? base)))
      (if-let [props (match-class base)]
        (if mod
          {:media {(keyword mod) props}}
          props)
        nil)
      nil)))

(defn deep-merge [a b]
  (if (and (map? a) (map? b))
    (merge-with deep-merge a b)
    b))

(defn parse
  "Parses a string of Tailwind classes and returns a layout map."
  [class-str]
  (let [tokens (-> class-str str/trim (str/split #"\s+"))]
    (reduce (fn [acc token]
              (if-let [res (parse-token token)]
                (deep-merge acc res)
                acc))
            {}
            tokens)))

;; Rendering

(def +border-chars+
  {:tl \+ :tr \+ :bl \+ :br \+ :h \- :v \|})

(defn make-canvas [w h]
  (vec (repeat h (vec (repeat w \space)))))

(defn draw-point [canvas x y c]
  (if (and (>= y 0) (< y (count canvas))
           (>= x 0) (< x (count (first canvas))))
    (assoc-in canvas [y x] c)
    canvas))

(defn draw-text [canvas x y text]
  (reduce (fn [c [i char]]
            (draw-point c (+ x i) y char))
          canvas
          (map-indexed vector text)))

(defn draw-box-outline [canvas x y w h]
  (let [x2 (+ x w -1)
        y2 (+ y h -1)
        c (reduce (fn [c i]
                    (-> c
                        (draw-point (+ x i) y (:h +border-chars+))
                        (draw-point (+ x i) y2 (:h +border-chars+))))
                  canvas
                  (range 1 (dec w)))
        c (reduce (fn [c i]
                    (-> c
                        (draw-point x (+ y i) (:v +border-chars+))
                        (draw-point x2 (+ y i) (:v +border-chars+))))
                  c
                  (range 1 (dec h)))]
    (-> c
        (draw-point x y (:tl +border-chars+))
        (draw-point x2 y (:tr +border-chars+))
        (draw-point x y2 (:bl +border-chars+))
        (draw-point x2 y2 (:br +border-chars+)))))

(defn render-canvas-str [canvas]
  (str/join "\n" (map #(apply str %) canvas)))

(defn parse-unit [v parent-dim]
  (cond (nil? v) nil
        (number? v) v
        (string? v)
        (cond (str/ends-with? v "%")
              (int (* (/ (Double/parseDouble (subs v 0 (dec (count v)))) 100.0) parent-dim))
              (str/ends-with? v "rem")
              (int (* (Double/parseDouble (subs v 0 (- (count v) 3))) 4)) ;; 1rem = 4 chars
              (str/ends-with? v "px")
              1 ;; 1px = 1 char
              (= v "full") parent-dim
              (re-matches #"\d+" v) (Integer/parseInt v)
              :else (or (h/parse-long v) 0))
        :else 0))

(defn measure-node [node parent-w]
  (cond (string? node) {:w (count node) :h 1 :type :text :content node}
        (vector? node)
        (let [[tag & rest] node
              [attrs children] (if (map? (first rest)) [(first rest) (next rest)] [{} rest])
              props (parse (:class attrs ""))

              fixed-w (parse-unit (:width props) parent-w)
              fixed-h (parse-unit (:height props) 10)

              is-flex (= (:display props) :flex)
              is-col (or (= (:flex-direction props) :column)
                         (= (:flex-direction props) :column-reverse))

              has-border (or (str/includes? (:class attrs "") "border") (boolean (:border props)))

              avail-w (let [base (or fixed-w parent-w 80)]
                        (if has-border (- base 2) base))

              measured-children (map #(measure-node % avail-w) children)

              content-w (if (empty? measured-children) 0
                            (if (and is-flex (not is-col))
                              (reduce + (map :w measured-children))
                              (if (empty? measured-children) 0 (apply max (map :w measured-children)))))

              content-h (if (empty? measured-children) 0
                            (if (and is-flex (not is-col))
                              (if (empty? measured-children) 0 (apply max (map :h measured-children)))
                              (reduce + (map :h measured-children))))

              final-w (or fixed-w (+ content-w (if has-border 2 0)))
              final-h (or fixed-h (+ content-h (if has-border 2 0)))]

          {:type :element
           :tag tag
           :props props
           :w final-w
           :h final-h
           :children measured-children
           :has-border has-border})
        :else {:w 0 :h 0 :type :empty}))

(defn layout-node [node x y]
  (let [node (assoc node :x x :y y)
        is-flex (= (get-in node [:props :display]) :flex)
        is-col (let [dir (get-in node [:props :flex-direction])]
                 (or (= dir :column) (= dir :column-reverse)))

        start-x (if (:has-border node) (inc x) x)
        start-y (if (:has-border node) (inc y) y)

        [_ children] (reduce (fn [[current-pos acc] child]
                               (let [child-node (layout-node child
                                                            (if (and is-flex (not is-col)) current-pos start-x)
                                                            (if (and is-flex (not is-col)) start-y current-pos))
                                     next-pos (if (and is-flex (not is-col))
                                                (+ current-pos (:w child))
                                                (+ current-pos (:h child)))]
                                 [next-pos (conj acc child-node)]))
                             [(if (and is-flex (not is-col)) start-x start-y) []]
                             (:children node))]
    (assoc node :children children)))

(defn draw-node [canvas node]
  (let [canvas (if (:has-border node)
                 (draw-box-outline canvas (:x node) (:y node) (:w node) (:h node))
                 canvas)
        canvas (if (= (:type node) :text)
                 (draw-text canvas (:x node) (:y node) (:content node))
                 canvas)]
    (reduce draw-node canvas (:children node))))

(defn render
  "Renders a Hiccup form to an ASCII string."
  [form & [opts]]
  (let [measured (measure-node form (or (:width opts) 80))
        layout (layout-node measured 0 0)
        w (:w layout)
        h (:h layout)
        canvas (make-canvas w h)
        final-canvas (draw-node canvas layout)]
    (render-canvas-str final-canvas)))
