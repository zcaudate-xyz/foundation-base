(ns js.lib.radix
  (:require [std.string :as str]
            [std.block :as block]
            [std.lib :as h]
            [std.lang :as l]))

(l/script :js
  {:import [["@radix-ui/themes" :as [* RadixMain]
             :bundle [["@radix-ui/themes/styles.css"]]]]})

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "RadixMain"
                                   :tag "js"}]
  [AccqessibleIcon
   AlertDialog
   [AlertDialogAction AlertDialog.Action]
   [AlertDialogCancel AlertDialog.Cancel]
   [AlertDialogContent AlertDialog.Content]
   [AlertDialogDescription AlertDialog.Description]
   [AlertDialogRoot AlertDialog.Root]
   [AlertDialogTitle AlertDialog.Title]
   [AlertDialogTrigger AlertDialog.Trigger]
   AspectRatio
   Avatar
   Badge
   Blockquote
   Box
   Button
   Callout
   [CalloutIcon Callout.Icon]
   [CalloutRoot Callout.Root]
   [CalloutText Callout.Text]
   Card
   Checkbox
   CheckboxCards
   [CheckboxCardsItem CheckboxCards.Item]
   [CheckboxCardsRoot CheckboxCards.Root]
   CheckboxGroup
   [CheckboxGroupItem CheckboxGroup.Item]
   [CheckboxGroupRoot CheckboxGroup.Root]
   ChevronDownIcon
   Code
   Container
   ContextMenu
   [ContextMenuCheckboxItem ContextMenu.CheckboxItem]
   [ContextMenuContent ContextMenu.Content]
   [ContextMenuGroup ContextMenu.Group]
   [ContextMenuItem ContextMenu.Item]
   [ContextMenuLabel ContextMenu.Label]
   [ContextMenuRadioGroup ContextMenu.RadioGroup]
   [ContextMenuRadioItem ContextMenu.RadioItem]
   [ContextMenuRoot ContextMenu.Root]
   [ContextMenuSeparator ContextMenu.Separator]
   [ContextMenuSub ContextMenu.Sub]
   [ContextMenuSubContent ContextMenu.SubContent]
   [ContextMenuSubTrigger ContextMenu.SubTrigger]
   [ContextMenuTrigger ContextMenu.Trigger]
   DataList
   [DataListItem DataList.Item]
   [DataListLabel DataList.Label]
   [DataListRoot DataList.Root]
   [DataListValue DataList.Value]
   Dialog
   [DialogClose Dialog.Close]
   [DialogContent Dialog.Content]
   [DialogDescription Dialog.Description]
   [DialogRoot Dialog.Root]
   [DialogTitle Dialog.Title]
   [DialogTrigger Dialog.Trigger]
   DropdownMenu
   [DropdownMenuCheckboxItem DropdownMenu.CheckboxItem]
   [DropdownMenuContent DropdownMenu.Content]
   [DropdownMenuGroup DropdownMenu.Group]
   [DropdownMenuItem DropdownMenu.Item]
   [DropdownMenuLabel DropdownMenu.Label]
   [DropdownMenuRadioGroup DropdownMenu.RadioGroup]
   [DropdownMenuRadioItem DropdownMenu.RadioItem]
   [DropdownMenuRoot DropdownMenu.Root]
   [DropdownMenuSeparator DropdownMenu.Separator]
   [DropdownMenuSub DropdownMenu.Sub]
   [DropdownMenuSubContent DropdownMenu.SubContent]
   [DropdownMenuSubTrigger DropdownMenu.SubTrigger]
   [DropdownMenuTrigger DropdownMenu.Trigger]
   [DropdownMenuTriggerIcon DropdownMenu.TriggerIcon]
   Em
   Flex
   Grid
   Heading
   HoverCard
   [HoverCardContent HoverCard.Content]
   [HoverCardRoot HoverCard.Root]
   [HoverCardTrigger HoverCard.Trigger]
   IconButton
   Inset
   Kbd
   Link
   Popover
   [PopoverAnchor Popover.Anchor]
   [PopoverClose Popover.Close]
   [PopoverContent Popover.Content]
   [PopoverRoot Popover.Root]
   [PopoverTrigger Popover.Trigger]
   Portal
   Progress
   Quote
   Radio
   RadioCards
   [RadioCardsItem RadioCards.Item]
   [RadioCardsRoot RadioCards.Root]
   RadioGroup
   [RadioGroupItem RadioGroup.Item]
   [RadioGroupRoot RadioGroup.Root]
   Reset
   ScrollArea
   Section
   SegmentedControl
   [SegmentedControlItem SegmentedControl.Item]
   [SegmentedControlRoot SegmentedControl.Root]
   Select
   [SelectContent Select.Content]
   [SelectGroup Select.Group]
   [SelectItem Select.Item]
   [SelectLabel Select.Label]
   [SelectRoot Select.Root]
   [SelectSeparator Select.Separator]
   [SelectTrigger Select.Trigger]
   Separator
   Skeleton
   Slider
   Slot
   Slottable
   Spinner
   Strong
   Switch
   TabNav
   [TabNavLink TabNav.Link]
   [TabNavRoot TabNav.Root]
   Table
   [TableBody Table.Body]
   [TableCell Table.Cell]
   [TableColumnHeaderCell Table.ColumnHeaderCell]
   [TableHeader Table.Header]
   [TableRoot Table.Root]
   [TableRow Table.Row]
   [TableRowHeaderCell Table.RowHeaderCell]
   Tabs
   [TabsContent Tabs.Content]
   [TabsList Tabs.List]
   [TabsRoot Tabs.Root]
   [TabsTrigger Tabs.Trigger]
   Text
   TextArea
   TextField
   [TextFieldRoot TextField.Root]
   [TextFieldSlot TextField.Slot]
   Theme
   ThemeContext
   [ThemeContextProvider ThemeContext.Provider]
   [ThemeContextConsumer ThemeContext.Consumer]
   ThemePanel
   ThickCheckIcon
   ThickChevronRightIcon
   ThickDividerHorizontalIcon
   Tooltip
   VisuallyHidden
   useThemeContext])

(def +components+
  (apply hash-map
         `[:rx/accessible-icon               {:tag -/AccessibleIcon}
           :rx/alert-dialog                  {:tag -/AlertDialog}
           :rx/alert-dialog-action           {:tag -/AlertDialogAction}
           :rx/alert-dialog-cancel           {:tag -/AlertDialogCancel}
           :rx/alert-dialog-content          {:tag -/AlertDialogContent}
           :rx/alert-dialog-description      {:tag -/AlertDialogDescription}
           :rx/alert-dialog-root             {:tag -/AlertDialogRoot}
           :rx/alert-dialog-title            {:tag -/AlertDialogTitle}
           :rx/alert-dialog-trigger          {:tag -/AlertDialogTrigger}
           :rx/aspect-ratio                  {:tag -/AspectRatio}
           :rx/avatar                        {:tag -/Avatar}
           :rx/badge                         {:tag -/Badge}
           :rx/blockquote                    {:tag -/Blockquote}
           :rx/box                           {:tag -/Box}
           :rx/button                        {:tag -/Button}
           :rx/callout                       {:tag -/Callout}
           :rx/callout-icon                  {:tag -/CalloutIcon}
           :rx/callout-root                  {:tag -/CalloutRoot}
           :rx/callout-text                  {:tag -/CalloutText}
           :rx/card                          {:tag -/Card}
           :rx/checkbox                      {:tag -/Checkbox}
           :rx/checkbox-cards                {:tag -/CheckboxCards}
           :rx/checkbox-cards-item           {:tag -/CheckboxCardsItem}
           :rx/checkbox-cards-root           {:tag -/CheckboxCardsRoot}
           :rx/checkbox-group                {:tag -/CheckboxGroup}
           :rx/checkbox-group-item           {:tag -/CheckboxGroupItem}
           :rx/checkbox-group-root           {:tag -/CheckboxGroupRoot}
           :rx/chevron-down-icon             {:tag -/ChevronDownIcon}
           :rx/code                          {:tag -/Code}
           :rx/container                     {:tag -/Container}
           :rx/context-menu                  {:tag -/ContextMenu}
           :rx/context-menu-checkbox-item    {:tag -/ContextMenuCheckboxItem}
           :rx/context-menu-content          {:tag -/ContextMenuContent}
           :rx/context-menu-group            {:tag -/ContextMenuGroup}
           :rx/context-menu-item             {:tag -/ContextMenuItem}
           :rx/context-menu-label            {:tag -/ContextMenuLabel}
           :rx/context-menu-radio-group      {:tag -/ContextMenuRadioGroup}
           :rx/context-menu-radio-item       {:tag -/ContextMenuRadioItem}
           :rx/context-menu-root             {:tag -/ContextMenuRoot}
           :rx/context-menu-separator        {:tag -/ContextMenuSeparator}
           :rx/context-menu-sub              {:tag -/ContextMenuSub}
           :rx/context-menu-sub-content      {:tag -/ContextMenuSubContent}
           :rx/context-menu-sub-trigger      {:tag -/ContextMenuSubTrigger}
           :rx/context-menu-trigger          {:tag -/ContextMenuTrigger}
           :rx/data-list                     {:tag -/DataList}
           :rx/data-list-item                {:tag -/DataListItem}
           :rx/data-list-label               {:tag -/DataListLabel}
           :rx/data-list-root                {:tag -/DataListRoot}
           :rx/data-list-value               {:tag -/DataListValue}
           :rx/dialog                        {:tag -/Dialog}
           :rx/dialog-close                  {:tag -/DialogClose}
           :rx/dialog-content                {:tag -/DialogContent}
           :rx/dialog-description            {:tag -/DialogDescription}
           :rx/dialog-root                   {:tag -/DialogRoot}
           :rx/dialog-title                  {:tag -/DialogTitle}
           :rx/dialog-trigger                {:tag -/DialogTrigger}
           :rx/dropdown-menu                 {:tag -/DropdownMenu}
           :rx/dropdown-menu-checkbox-item   {:tag -/DropdownMenuCheckboxItem}
           :rx/dropdown-menu-content         {:tag -/DropdownMenuContent}
           :rx/dropdown-menu-group           {:tag -/DropdownMenuGroup}
           :rx/dropdown-menu-item            {:tag -/DropdownMenuItem}
           :rx/dropdown-menu-label           {:tag -/DropdownMenuLabel}
           :rx/dropdown-menu-radio-group     {:tag -/DropdownMenuRadioGroup}
           :rx/dropdown-menu-radio-item      {:tag -/DropdownMenuRadioItem}
           :rx/dropdown-menu-root            {:tag -/DropdownMenuRoot}
           :rx/dropdown-menu-separator       {:tag -/DropdownMenuSeparator}
           :rx/dropdown-menu-sub             {:tag -/DropdownMenuSub}
           :rx/dropdown-menu-sub-content     {:tag -/DropdownMenuSubContent}
           :rx/dropdown-menu-sub-trigger     {:tag -/DropdownMenuSubTrigger}
           :rx/dropdown-menu-trigger         {:tag -/DropdownMenuTrigger}
           :rx/dropdown-menu-trigger-icon    {:tag -/DropdownMenuTriggerIcon}
           :rx/em                            {:tag -/Em}
           :rx/flex                          {:tag -/Flex}
           :rx/grid                          {:tag -/Grid}
           :rx/heading                       {:tag -/Heading}
           :rx/hover-card                    {:tag -/HoverCard}
           :rx/hover-card-content            {:tag -/HoverCardContent}
           :rx/hover-card-root               {:tag -/HoverCardRoot}
           :rx/hover-card-trigger            {:tag -/HoverCardTrigger}
           :rx/icon-button                   {:tag -/IconButton}
           :rx/inset                         {:tag -/Inset}
           :rx/kbd                           {:tag -/Kbd}
           :rx/link                          {:tag -/Link}
           :rx/popover                       {:tag -/Popover}
           :rx/popover-anchor                {:tag -/PopoverAnchor}
           :rx/popover-close                 {:tag -/PopoverClose}
           :rx/popover-content               {:tag -/PopoverContent}
           :rx/popover-root                  {:tag -/PopoverRoot}
           :rx/popover-trigger               {:tag -/PopoverTrigger}
           :rx/portal                        {:tag -/Portal}
           :rx/progress                      {:tag -/Progress}
           :rx/quote                         {:tag -/Quote}
           :rx/radio                         {:tag -/Radio}
           :rx/radio-cards                   {:tag -/RadioCards}
           :rx/radio-cards-item              {:tag -/RadioCardsItem}
           :rx/radio-cards-root              {:tag -/RadioCardsRoot}
           :rx/radio-group                   {:tag -/RadioGroup}
           :rx/radio-group-item              {:tag -/RadioGroupItem}
           :rx/radio-group-root              {:tag -/RadioGroupRoot}
           :rx/reset                         {:tag -/Reset}
           :rx/scroll-area                   {:tag -/ScrollArea}
           :rx/section                       {:tag -/Section}
           :rx/segmented-control             {:tag -/SegmentedControl}
           :rx/segmented-control-item        {:tag -/SegmentedControlItem}
           :rx/segmented-control-root        {:tag -/SegmentedControlRoot}
           :rx/select                        {:tag -/Select}
           :rx/select-content                {:tag -/SelectContent}
           :rx/select-group                  {:tag -/SelectGroup}
           :rx/select-item                   {:tag -/SelectItem}
           :rx/select-label                  {:tag -/SelectLabel}
           :rx/select-root                   {:tag -/SelectRoot}
           :rx/select-separator              {:tag -/SelectSeparator}
           :rx/select-trigger                {:tag -/SelectTrigger}
           :rx/separator                     {:tag -/Separator}
           :rx/skeleton                      {:tag -/Skeleton}
           :rx/slider                        {:tag -/Slider}
           :rx/slot                          {:tag -/Slot}
           :rx/slottable                     {:tag -/Slottable}
           :rx/spinner                       {:tag -/Spinner}
           :rx/strong                        {:tag -/Strong}
           :rx/switch                        {:tag -/Switch}
           :rx/tab-nav                       {:tag -/TabNav}
           :rx/tab-nav-link                  {:tag -/TabNavLink}
           :rx/tab-nav-root                  {:tag -/TabNavRoot}
           :rx/table                         {:tag -/Table}
           :rx/table-body                    {:tag -/TableBody}
           :rx/table-cell                    {:tag -/TableCell}
           :rx/table-column-header-cell      {:tag -/TableColumnHeaderCell}
           :rx/table-header                  {:tag -/TableHeader}
           :rx/table-root                    {:tag -/TableRoot}
           :rx/table-row                     {:tag -/TableRow}
           :rx/table-row-header-cell         {:tag -/TableRowHeaderCell}
           :rx/tabs                          {:tag -/Tabs}
           :rx/tabs-content                  {:tag -/TabsContent}
           :rx/tabs-list                     {:tag -/TabsList}
           :rx/tabs-root                     {:tag -/TabsRoot}
           :rx/tabs-trigger                  {:tag -/TabsTrigger}
           :rx/text                          {:tag -/Text}
           :rx/text-area                     {:tag -/TextArea}
           :rx/text-field                    {:tag -/TextField}
           :rx/text-field-root               {:tag -/TextFieldRoot}
           :rx/text-field-slot               {:tag -/TextFieldSlot}
           :rx/theme                         {:tag -/Theme}
           :rx/theme-context                 {:tag -/ThemeContext}
           :rx/theme-context-consumer        {:tag -/ThemeContextConsumer}
           :rx/theme-context-provider        {:tag -/ThemeContextProvider}
           :rx/theme-panel                   {:tag -/ThemePanel}
           :rx/thick-check-icon              {:tag -/ThickCheckIcon}
           :rx/thick-chevron-right-icon      {:tag -/ThickChevronRightIcon}
           :rx/thick-divider-horizontal-icon {:tag -/ThickDividerHorizontalIcon}
           :rx/tooltip                       {:tag -/Tooltip}
           :rx/visually-hidden               {:tag -/VisuallyHidden}]))

(defn generate-blocks
  []
  (block/layout
   (vec (mapcat (fn [[k]]
                  [(keyword "rx" (str/spear-case (str k)))
                   {:tag (symbol "-" (str k))}])
                (sort (ns-publics *ns*))))))


(comment
  (l/rt:module-meta :js)
  )
