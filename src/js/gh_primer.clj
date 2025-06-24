(ns js.gh-primer
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.html :as html]))

(l/script :js
  {:macro-only true
   :bundle {:default  [["@primer/react" :as [* Primer]]]}
   :import [["@primer/react" :as [* Primer]]]})

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "Primer"
                                   :tag "js"}]
  [ActionBar
   ActionList
   ActionMenu
   AnchoredOverlay
   Autocomplete
   Avatar
   AvatarPair
   AvatarStack
   Banner
   Blankslate
   Box
   BranchName
   Breadcrumbs
   Button
   ButtonGroup
   Checkbox
   CheckboxGroup
   CircleBadge
   CircleOcticon
   CounterLabel
   DataTable
   Details
   Dialog
   FormControl
   [FormControl:Label FormControl.Label]
   [FormControl:Caption FormControl.Caption]
   [FormControl:Validation FormControl.Validation]
   Heading
   IconButton
   InlineMessage
   Label
   LabelGroup
   Link
   NavList
   Overlay
   PageHeader
   PageLayout
   Pagination
   PointerBox
   Popover
   ProgressBar
   Radio
   RadioGroup
   RelativeTime
   SegmentedControl
   Select
   SelectPanel
   Skeleton
   loaders
   Spinner
   Stack
   StateLabel
   Text
   TextInput
   TextInputWithTokens
   Textarea
   Timeline
   ToggleSwitch
   Token
   Tooltip
   TreeView
   Truncate
   UnderlineNav
   UnderlinePanels])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "Primer"
                                   :tag "js"}]
  [useColorSchemeVar
   useConfirm
   useDetails
   useFocusTrap
   useFocusZone
   useFormControlForwardedProps
   useOnEscapePress
   useOnOutsideClick
   useOpenAndCloseFocus
   useOverlay
   useProvidedRefOrCreate
   useRefObjectAsForwardedRef
   useResizeObserver
   useResponsiveValue
   useSafeTimeout
   useTheme
   ThemeProvider
   BaseStyles])


(comment
  (mapv #(symbol (.text %))
        (.select
         (html/node
          (slurp (h/sys:resource "js/gh_primer.html")))
         "span[class=prc-ActionList-ItemLabel-TmBhn]"))






  (.? (first
       (.select
        (html/node
         (slurp (h/sys:resource "js/gh_primer.html")))
        "span[class=prc-ActionList-ItemLabel-TmBhn] span")))

  (html/tree
   (slurp (h/sys:resource "js/gh_primer.html"))))
