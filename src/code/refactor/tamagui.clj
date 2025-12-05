(ns code.refactor.tamagui
  (:require [code.edit :as edit]
            [code.query :as query]
            [std.lib :as h]
            [std.string :as str]))

(def +component-map+
  {'tm/Button      'fg/Button
   'tm/Input       'fg/Input
   'tm/Text        :span
   'tm/Stack       :div
   'tm/XStack      :div
   'tm/YStack      :div
   'tm/ZStack      :div
   'tm/View        :div
   'tm/Label       'fg/Label
   'tm/Card        'fg/Card
   'tm/Switch      'fg/Switch
   'tm/Separator   'fg/Separator
   'tm/ScrollView  'fg/ScrollArea
   'tm/TextArea    'fg/Textarea
   'tm/Select      'fg/Select
   'tm/Dialog      'fg/Dialog
   'tm/Sheet       'fg/Sheet
   'tm/Tooltip     'fg/Tooltip
   'tm/Popover     'fg/Popover
   'tm/Tabs        'fg/Tabs
   'tm/Avatar      'fg/Avatar
   'tm/Image       :img
   'tm/Heading     :h3
   'tm/H1          :h1
   'tm/H2          :h2
   'tm/H3          :h3
   'tm/H4          :h4
   'tm/H5          :h5
   'tm/H6          :h6
   'tm/Paragraph   :p})

(def +prop-map+
  {"p" "p-"
   "px" "px-"
   "py" "py-"
   "pt" "pt-"
   "pb" "pb-"
   "pl" "pl-"
   "pr" "pr-"
   "m" "m-"
   "mx" "mx-"
   "my" "my-"
   "mt" "mt-"
   "mb" "mb-"
   "ml" "ml-"
   "mr" "mr-"
   "bg" "bg-"
   "backgroundColor" "bg-"
   "color" "text-"
   "w" "w-"
   "width" "w-"
   "h" "h-"
   "height" "h-"
   "minWidth" "min-w-"
   "minHeight" "min-h-"
   "maxWidth" "max-w-"
   "maxHeight" "max-h-"
   "gap" "gap-"
   "space" "gap-"
   "br" "rounded-"
   "borderRadius" "rounded-"
   "borderWidth" "border-"
   "borderColor" "border-"})

(defn convert-color
  [color]
  (cond (not (str/starts-with? color "$"))
        color

        :else
        (let [base (str/replace color #"^\$" "")
              ;; Match colorName + number (e.g. red10, blue5)
              match (re-find #"^([a-z]+)(\d+)$" base)]
          (if match
            (let [[_ name step] match
                  step-int (parse-long step)
                  ;; Approximate mapping from 1-12 scale to 50-950 scale
                  tw-step (condp <= step-int
                            11 900
                            9  800
                            8  600
                            6  500
                            4  400
                            3  300
                            2  100
                            50)]
              (str name "-" tw-step))
            base))))

(defn convert-value
  [val prefix]
  (cond (string? val)
        (let [v (str/replace val #"^\$" "")]
          (cond
            ;; Colors (bg-, text-, border-)
            (or (str/starts-with? prefix "bg-")
                (str/starts-with? prefix "text-")
                (str/starts-with? prefix "border-"))
            (str prefix (convert-color val))

            ;; Radius
            (str/starts-with? prefix "rounded-")
            (cond (= v "true") "rounded"
                  (= v "full") "rounded-full"
                  (and (re-matches #"\d+" v) (>= (parse-long v) 4)) "rounded-xl"
                  (and (re-matches #"\d+" v) (>= (parse-long v) 2)) "rounded-md"
                  :else (str prefix v))

            :else
            (str prefix v)))

        (number? val)
        (str prefix val)

        :else
        (str prefix val)))

(defn convert-flex
  [props]
  (let [flex (:flex props)]
    (cond (or (= flex 1) (= flex true) (= flex "1"))
          "flex-1"

          (number? flex)
          (str "flex-[" flex "]")

          :else
          nil)))

(defn convert-justify
  [val]
  (case val
    "center" "justify-center"
    "space-between" "justify-between"
    "space-around" "justify-around"
    "space-evenly" "justify-evenly"
    "flex-start" "justify-start"
    "flex-end" "justify-end"
    "start" "justify-start"
    "end" "justify-end"
    nil))

(defn convert-align
  [val]
  (case val
    "center" "items-center"
    "flex-start" "items-start"
    "flex-end" "items-end"
    "start" "items-start"
    "end" "items-end"
    "stretch" "items-stretch"
    "baseline" "items-baseline"
    nil))

(defn process-props
  [props tag]
  (let [classes (atom [])
        removals (atom #{})

        ;; Defaults for specific components
        _ (when (= tag 'tm/XStack)
            (swap! classes conj "flex" "flex-row"))
        _ (when (= tag 'tm/YStack)
            (swap! classes conj "flex" "flex-col"))]

    (doseq [[k v] props]
      (let [ks (name k)
            prefix (get +prop-map+ ks)]
        (cond prefix
              (do (swap! classes conj (convert-value v prefix))
                  (swap! removals conj k))

              (or (= k :flex) (= k :flexGrow))
              (when-let [cls (convert-flex props)]
                (swap! classes conj cls)
                (swap! removals conj k))

              (or (= k :jc) (= k :justifyContent))
              (when-let [cls (convert-justify v)]
                (swap! classes conj cls)
                (swap! removals conj k))

              (or (= k :ai) (= k :alignItems))
              (when-let [cls (convert-align v)]
                (swap! classes conj cls)
                (swap! removals conj k)))))

    (let [final-props (apply dissoc props @removals)
          existing-class (:className final-props)
          new-class (str/join " " @classes)]
      (if (empty? new-class)
        final-props
        (assoc final-props :className
               (cond (nil? existing-class)
                     new-class

                     (vector? existing-class)
                     (conj existing-class new-class)

                     :else
                     (str existing-class " " new-class)))))))

(defn refactor-element
  [[tag comp-name & args :as form]]
  (if (and (keyword? tag) (= tag :%) (symbol? comp-name))
    (let [props (first args)
          [actual-props rest-children] (if (map? props)
                                         [props (rest args)]
                                         [nil args])

          new-comp-sym (get +component-map+ comp-name)]

      (if new-comp-sym
        (let [new-props (process-props (or actual-props {}) comp-name)
              final-form (if (and (keyword? new-comp-sym) (not= new-comp-sym :%))
                           (into [new-comp-sym new-props] rest-children)
                           (into [:%% new-comp-sym new-props] rest-children))]
          ;; Fixup :%% back to :%
          (if (= (first final-form) :%%)
            (into [:% (second final-form) (nth final-form 2)] (drop 3 final-form))
            final-form))
        form))
    form))

(defn transform-zipper
  [zloc]
  (let [form (edit/value zloc)]
    (if (vector? form)
      (let [new-form (refactor-element form)]
        (if (not= form new-form)
          (edit/replace zloc new-form)
          zloc))
      zloc)))

(defn replace-require
  [zloc]
  (query/modify zloc
                '[_]
                (fn [zloc]
                  (let [node (edit/value zloc)]
                    (if (and (vector? node)
                             (= (count node) 3)
                             (= (first node) 'js.tamagui)
                             (= (second node) :as) ;; Keyword matches literally
                             (= (nth node 2) 'tm))
                      (edit/replace zloc '[js.lib.figma :as fg])
                      zloc)))))

(defn refactor-string
  [s]
  (-> (edit/parse-root s)
      (query/modify '[_]
                    (fn [zloc]
                      (transform-zipper zloc)))
      (replace-require)
      (edit/root-string)))

(defn refactor-file
  [path]
  (let [content (slurp path)
        new-content (refactor-string content)]
    (spit path new-content)))
