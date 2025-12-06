(ns code.tool.refactor.tamagui
  (:require [code.edit :as edit]
            [code.query :as query]
            [std.lib :as h]
            [std.string :as str]))

(def +component-map+
  {;; Basic
   'tm/Button      'fg/Button
   'tm/Input       'fg/Input
   'tm/TextArea    'fg/Textarea
   'tm/Switch      'fg/Switch
   'tm/Checkbox    'fg/Checkbox
   'tm/Label       'fg/Label
   'tm/Image       :img
   'tm/Separator   'fg/Separator
   'tm/Spacer      :div ;; Handle as empty div?

   ;; Layout
   'tm/Stack       :div
   'tm/XStack      :div
   'tm/YStack      :div
   'tm/ZStack      :div
   'tm/View        :div
   'tm/Frame       :div
   'tm/Group       :div
   'tm/XGroup      :div
   'tm/YGroup      :div
   'tm/SizableStack :div
   'tm/ThemeableStack :div
   'tm/ScrollView  'fg/ScrollArea

   ;; Text / Typography
   'tm/Text        :span
   'tm/Paragraph   :p
   'tm/Heading     :h3
   'tm/H1          :h1
   'tm/H2          :h2
   'tm/H3          :h3
   'tm/H4          :h4
   'tm/H5          :h5
   'tm/H6          :h6
   'tm/SizableText :span
   'tm/Anchor      :a
   'tm/VisuallyHidden :span

   ;; Semantic HTML
   'tm/Article     :article
   'tm/Aside       :aside
   'tm/Footer      :footer
   'tm/Header      :header
   'tm/Main        :main
   'tm/Nav         :nav
   'tm/Section     :section
   'tm/Form        :form
   'tm/Fieldset    :fieldset
   'tm/ListItem    :li

   ;; Complex Components & Subcomponents

   ;; Accordion
   'tm/Accordion         'fg/Accordion
   'tm/AccordionItem     'fg/AccordionItem
   'tm/AccordionTrigger  'fg/AccordionTrigger
   'tm/AccordionContent  'fg/AccordionContent

   ;; AlertDialog
   'tm/AlertDialog            'fg/AlertDialog
   'tm/AlertDialogTrigger     'fg/AlertDialogTrigger
   'tm/AlertDialogContent     'fg/AlertDialogContent
   'tm/AlertDialogTitle       'fg/AlertDialogTitle
   'tm/AlertDialogDescription 'fg/AlertDialogDescription
   'tm/AlertDialogAction      'fg/AlertDialogAction
   'tm/AlertDialogCancel      'fg/AlertDialogCancel
   'tm/AlertDialogOverlay     'fg/AlertDialogOverlay
   'tm/AlertDialogPortal      'fg/AlertDialogPortal

   ;; Avatar
   'tm/Avatar          'fg/Avatar
   'tm/AvatarImage     'fg/AvatarImage
   'tm/AvatarFallback  'fg/AvatarFallback

   ;; Card
   'tm/Card            'fg/Card
   'tm/CardHeader      'fg/CardHeader
   'tm/CardFooter      'fg/CardFooter
   'tm/CardBackground  :div ;; No direct mapping

   ;; Dialog
   'tm/Dialog            'fg/Dialog
   'tm/DialogTrigger     'fg/DialogTrigger
   'tm/DialogContent     'fg/DialogContent
   'tm/DialogTitle       'fg/DialogTitle
   'tm/DialogDescription 'fg/DialogDescription
   'tm/DialogClose       'fg/DialogClose
   'tm/DialogOverlay     'fg/DialogOverlay
   'tm/DialogPortal      'fg/DialogPortal

   ;; Popover
   'tm/Popover           'fg/Popover
   'tm/PopoverTrigger    'fg/PopoverTrigger
   'tm/PopoverContent    'fg/PopoverContent
   'tm/PopoverClose      'fg/PopoverClose ;; Not standard in fg?
   'tm/PopoverAnchor     'fg/PopoverAnchor
   'tm/PopoverArrow      :div ;; fg handles arrow internally often

   ;; Progress
   'tm/Progress          'fg/Progress
   'tm/ProgressIndicator :div ;; fg/Progress handles this internally

   ;; RadioGroup
   'tm/RadioGroup         'fg/RadioGroup
   'tm/RadioGroupItem     'fg/RadioGroupItem
   'tm/RadioGroupIndicator :div

   ;; Select
   'tm/Select                'fg/Select
   'tm/SelectTrigger         'fg/SelectTrigger
   'tm/SelectValue           'fg/SelectValue
   'tm/SelectContent         'fg/SelectContent
   'tm/SelectItem            'fg/SelectItem
   'tm/SelectItemText        'fg/SelectItemText
   'tm/SelectLabel           'fg/SelectLabel
   'tm/SelectGroup           'fg/SelectGroup
   'tm/SelectSeparator       'fg/SelectSeparator
   'tm/SelectViewport        :div ;; fg/SelectContent usually wraps
   'tm/SelectScrollUpButton  'fg/SelectScrollUpButton
   'tm/SelectScrollDownButton 'fg/SelectScrollDownButton

   ;; Sheet
   'tm/Sheet             'fg/Sheet
   'tm/SheetTrigger      'fg/SheetTrigger
   'tm/SheetContent      'fg/SheetContent
   'tm/SheetOverlay      'fg/SheetOverlay
   'tm/SheetHandle       :div
   'tm/SheetFooter       'fg/SheetFooter ;; If exists

   ;; Slider
   'tm/Slider            'fg/Slider
   'tm/SliderTrack       :div
   'tm/SliderRange       :div
   'tm/SliderThumb       :div

   ;; Switch
   'tm/SwitchThumb       :div

   ;; Tabs
   'tm/Tabs              'fg/Tabs
   'tm/TabsList          'fg/TabsList
   'tm/TabsTrigger       'fg/TabsTrigger
   'tm/TabsContent       'fg/TabsContent

   ;; Toast
   'tm/Toast             :div ;; fg uses sonner toast() function usually
   'tm/ToastProvider     :div
   'tm/ToastViewport     :div

   ;; ToggleGroup
   'tm/ToggleGroup       'fg/ToggleGroup
   'tm/ToggleGroupItem   'fg/ToggleGroupItem

   ;; Tooltip
   'tm/Tooltip           'fg/Tooltip
   'tm/TooltipTrigger    'fg/TooltipTrigger
   'tm/TooltipContent    'fg/TooltipContent
   'tm/TooltipProvider   'fg/TooltipProvider
   'tm/TooltipArrow      :div

   ;; Visuals
   'tm/Circle            :div
   'tm/Square            :div
   'tm/Spinner           'fg/Skeleton
})

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
   "borderColor" "border-"
   "o" "opacity-"
   "opacity" "opacity-"
   "z" "z-"
   "zIndex" "z-"})

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
            (swap! classes conj "flex" "flex-col"))
        _ (when (= tag 'tm/Spacer)
            (swap! classes conj "flex-1")) ;; Spacer default?
        _ (when (= tag 'tm/Circle)
            (swap! classes conj "rounded-full flex items-center justify-center"))
        _ (when (= tag 'tm/Square)
            (swap! classes conj "flex items-center justify-center"))
        _ (when (= tag 'tm/VisuallyHidden)
            (swap! classes conj "sr-only"))]

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
