(ns js.gh-primer
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.html :as html]))

(l/script :js
  {:bundle {:default  [["@primer/react" :as [* Primer]]]}
   :import [["@primer/react" :as [* Primer]]]})

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "Primer"
                                   :tag "js"}]
  [ActionBar
   [ActionBar:IconButton ActionBar.IconButton]
   ActionList
   [ActionList:Item  ActionList.Item]
   [ActionList:Heading  ActionList.Heading]
   [ActionList:LinkItem  ActionList.LinkItem]
   [ActionList:LeadingVisual  ActionList.LeadingVisual]
   [ActionList:TrailingVisual  ActionList.TrailingVisual]
   [ActionList:TrailingAction  ActionList.TrailingAction]
   [ActionList:Description  ActionList.Description]
   [ActionList:GroupHeading  ActionList.GroupHeading]
   [ActionList:Group  ActionList.Group]
   [ActionList:Divider  ActionList.Divider]
   ActionMenu
   [ActionMenu:Button ActionMenu.Button]
   [ActionMenu:Anchor ActionMenu.Anchor]
   [ActionMenu:Overlay ActionMenu.Overlay]
   AnchoredOverlay
   Autocomplete
   [Autocomplete:Input Autocomplete.Input]
   [Autocomplete:Overlay Autocomplete.Overlay]
   [Autocomplete:Menu Autocomplete.Menu]
   Avatar
   AvatarPair
   AvatarStack
   Banner
   [Banner:Title Banner.Title]
   [Banner:Description Banner.Description]
   [Banner:PrimaryAction Banner.PrimaryAction]
   [Banner:SecondaryAction Banner.SecondaryAction]
   Blankslate
   [Blankslate:Visual Blankslate.Visual]
   [Blankslate:Heading Blankslate.Heading]
   [Blankslate:Description Blankslate.Description]
   [Blankslate:PrimaryAction Blankslate.PrimaryAction]
   [Blankslate:SecondaryAction Blankslate.SecondaryAction]
   Box
   BranchName
   Breadcrumbs
   [Breadcrumbs:Item Breadcrumbs.Item]
   Button
   ButtonGroup
   Checkbox
   CheckboxGroup
   [CheckboxGroup:Label CheckboxGroup.Label]
   [CheckboxGroup:Caption CheckboxGroup.Caption]
   [CheckboxGroup:Validation CheckboxGroup.Validation]
   CircleBadge
   [CircleBadge:Icon CircleBadge.Icon]
   CircleOcticon
   CounterLabel
   DataTable
   Details
   [Details:Summary Details.Summary]
   Dialog
   [Dialog:Body Dialog.Body]
   [Dialog:Buttons Dialog.Buttons]
   [Dialog:CloseButton Dialog.CloseButton]
   [Dialog:Footer Dialog.Footer]
   [Dialog:Header Dialog.Header]
   [Dialog:Title Dialog.Title]
   FormControl
   [FormControl:Label FormControl.Label]
   [FormControl:LeadingVisual FormControl.LeadingVisual]
   [FormControl:Caption FormControl.Caption]
   [FormControl:Validation FormControl.Validation]
   Heading
   IconButton
   InlineMessage
   Label
   LabelGroup
   Link
   NavList
   [NavList:Item NavList.Item]
   [NavList:LeadingVisual NavList.LeadingVisual]
   [NavList:TrailingVisual NavList.TrailingVisual]
   [NavList:SubNav NavList.SubNav]
   [NavList:Group NavList.Group]
   [NavList:GroupHeading NavList.GroupHeading]
   [NavList:Divider NavList.Divider]
   [NavList:TrailingAction NavList.TrailingAction]
   [NavList:GroupExpand NavList.GroupExpand]
   Overlay
   PageHeader
   [PageHeader:ContextArea PageHeader.ContextArea]
   [PageHeader:ParentLink PageHeader.ParentLink]
   [PageHeader:ContextBar PageHeader.ContextBar]
   [PageHeader:ContextAreaActions PageHeader.ContextAreaActions]
   [PageHeader:TitleArea PageHeader.TitleArea]
   [PageHeader:LeadingAction PageHeader.LeadingAction]
   [PageHeader:LeadingVisual PageHeader.LeadingVisual]
   [PageHeader:Title PageHeader.Title]
   [PageHeader:TrailingVisual PageHeader.TrailingVisual]
   [PageHeader:TrailingAction PageHeader.TrailingAction]
   [PageHeader:Actions PageHeader.Actions]
   [PageHeader:Breadcrumbs PageHeader.Breadcrumbs]
   [PageHeader:Description PageHeader.Description]
   [PageHeader:Navigation PageHeader.Navigation]
   PageLayout
   [PageLayout:Header PageLayout.Header]
   [PageLayout:Content PageLayout.Content]
   [PageLayout:Pane PageLayout.Pane]
   [PageLayout:Footer PageLayout.Footer]
   Pagination
   PointerBox
   Popover
   [Popover:Content Popover.Content]
   ProgressBar
   [ProgressBar:Item ProgressBar.Item]
   Radio
   RadioGroup
   [RadioGroup:Label RadioGroup.Label]
   [RadioGroup:Caption RadioGroup.Caption]
   [RadioGroup:Validation RadioGroup.Validation]
   RelativeTime
   SegmentedControl
   [SegmentedControl:Button SegmentedControl.Button]
   [SegmentedControl:IconButton SegmentedControl.IconButton]
   Select
   SelectPanel
   SkeletonAvatar
   SkeletonText
   SkeletonBox
   loaders
   Spinner
   Stack
   [Stack:Item Stack.Item]
   StateLabel
   Table
   [Table:Head Table.Head]
   [Table:Actions Table.Actions]
   [Table:Body Table.Body]
   [Table:Row Table.Row]
   [Table:Header Table.Header]
   [Table:Cell Table.Cell]
   [Table:CellPlaceholder Table.CellPlaceholder]
   [Table:Container Table.Container]
   [Table:Divider Table.Divider]
   [Table:Title Table.Title]
   [Table:Subtitle Table.Subtitle]
   [Table:Skeleton Table.Skeleton]
   [Table:Pagination Table.Pagination]
   [Table:ErrorDialog Table.ErrorDialog]
   [Table:SortHeader Table.SortHeader]
   Text
   TextInput
   [TextInput:Action TextInput.Action]
   TextInputWithTokens
   Textarea
   Timeline
   [Timeline:Item Timeline.Item]
   [Timeline:Badge  Timeline.Badge]
   [Timeline:Body  Timeline.Body]
   [Timeline:Break  Timeline.Break]
   ToggleSwitch
   Token
   Tooltip
   TreeView
   [TreeView:Item TreeView.Item]
   [TreeView:LeadingVisual TreeView.LeadingVisual]
   [TreeView:TrailingVisual TreeView.TrailingVisual]
   [TreeView:DirectoryIcon TreeView.DirectoryIcon]
   [TreeView:SubTree TreeView.SubTree]
   [TreeView:ErrorDialog TreeView.ErrorDialog]
   Truncate
   UnderlineNav
   [UnderlineNav:Item UnderlineNav.Item]
   UnderlinePanels
   [UnderlinePanels:Tab UnderlinePanels.Tab]])


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
  (defn make-array
    [t xs]
    (mapv (fn [x]
            [(symbol (str t ":" x))
             (symbol (str t "." x))])
          xs))
  
  (make-array
   "TreeView" '[Item LeadingVisual TrailingVisual DirectoryIcon SubTree ErrorDialog])
  []
  
  (make-array
   "PageHeader" '[ContextArea ParentLink ContextBar ContextAreaActions
                  TitleArea LeadingAction LeadingVisual Title  TrailingVisual TrailingAction
                  Actions Breadcrumbs Description Navigation])
  (make-array
   "NavList" '[Item LeadingVisual TrailingVisual SubNav Group GroupHeading Divider
               TrailingAction GroupExpand])
  (make-array
   "Dialog" '[Body Buttons CloseButton Footer Header Title]))
