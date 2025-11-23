(ns js.lib.figma
  (:require [std.lang :as l]
            [std.lib :as h]
            [std.string :as str]
            [std.block :as block]
            [js.react.compile :as compile]))

(l/script :js
  {:import [["@xtalk/figma-ui" :as [* FigmaUi]]
            ["sonner" :as [* Sonnar]]]})

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "Sonner"
                                   :tag "js"}]
  [toast])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "FigmaUi"
                                   :tag "js"}]
  [Accordion
   AccordionContent
   AccordionItem
   AccordionTrigger
   Alert
   AlertDescription
   AlertDialog
   AlertDialogAction
   AlertDialogCancel
   AlertDialogContent
   AlertDialogDescription
   AlertDialogFooter
   AlertDialogHeader
   AlertDialogOverlay
   AlertDialogPortal
   AlertDialogTitle
   AlertDialogTrigger
   AlertTitle
   AspectRatio
   Avatar
   AvatarFallback
   AvatarImage
   Badge
   Breadcrumb
   BreadcrumbEllipsis
   BreadcrumbItem
   BreadcrumbLink
   BreadcrumbList
   BreadcrumbPage
   BreadcrumbSeparator
   Button
   Calendar
   Card
   CardAction
   CardContent
   CardDescription
   CardFooter
   CardHeader
   CardTitle
   Carousel
   CarouselContent
   CarouselItem
   CarouselNext
   CarouselPrevious
   ChartContainer
   ChartLegend
   ChartLegendContent
   ChartStyle
   ChartTooltip
   ChartTooltipContent
   Checkbox
   Collapsible
   CollapsibleContent
   CollapsibleTrigger
   Command
   CommandDialog
   CommandEmpty
   CommandGroup
   CommandInput
   CommandItem
   CommandList
   CommandSeparator
   CommandShortcut
   ContextMenu
   ContextMenuCheckboxItem
   ContextMenuContent
   ContextMenuGroup
   ContextMenuItem
   ContextMenuLabel
   ContextMenuPortal
   ContextMenuRadioGroup
   ContextMenuRadioItem
   ContextMenuSeparator
   ContextMenuShortcut
   ContextMenuSub
   ContextMenuSubContent
   ContextMenuSubTrigger
   ContextMenuTrigger
   Dialog
   DialogClose
   DialogContent
   DialogDescription
   DialogFooter
   DialogHeader
   DialogOverlay
   DialogPortal
   DialogTitle
   DialogTrigger
   Drawer
   DrawerClose
   DrawerContent
   DrawerDescription
   DrawerFooter
   DrawerHeader
   DrawerOverlay
   DrawerPortal
   DrawerTitle
   DrawerTrigger
   DropdownMenu
   DropdownMenuCheckboxItem
   DropdownMenuContent
   DropdownMenuGroup
   DropdownMenuItem
   DropdownMenuLabel
   DropdownMenuPortal
   DropdownMenuRadioGroup
   DropdownMenuRadioItem
   DropdownMenuSeparator
   DropdownMenuShortcut
   DropdownMenuSub
   DropdownMenuSubContent
   DropdownMenuSubTrigger
   DropdownMenuTrigger
   Form
   FormControl
   FormDescription
   FormField
   FormItem
   FormLabel
   FormMessage
   HoverCard
   HoverCardContent
   HoverCardTrigger
   ImageWithFallback
   Input
   InputOTP
   InputOTPGroup
   InputOTPSeparator
   InputOTPSlot
   Label
   Menubar
   MenubarCheckboxItem
   MenubarContent
   MenubarGroup
   MenubarItem
   MenubarLabel
   MenubarMenu
   MenubarPortal
   MenubarRadioGroup
   MenubarRadioItem
   MenubarSeparator
   MenubarShortcut
   MenubarSub
   MenubarSubContent
   MenubarSubTrigger
   MenubarTrigger
   NavigationMenu
   NavigationMenuContent
   NavigationMenuIndicator
   NavigationMenuItem
   NavigationMenuLink
   NavigationMenuList
   NavigationMenuTrigger
   NavigationMenuViewport
   Pagination
   PaginationContent
   PaginationEllipsis
   PaginationItem
   PaginationLink
   PaginationNext
   PaginationPrevious
   Popover
   PopoverAnchor
   PopoverContent
   PopoverTrigger
   Progress
   RadioGroup
   RadioGroupItem
   ResizableHandle
   ResizablePanel
   ResizablePanelGroup
   ScrollArea
   ScrollBar
   Select
   SelectContent
   SelectGroup
   SelectItem
   SelectLabel
   SelectScrollDownButton
   SelectScrollUpButton
   SelectSeparator
   SelectTrigger
   SelectValue
   Separator
   Sheet
   SheetClose
   SheetContent
   SheetDescription
   SheetFooter
   SheetHeader
   SheetTitle
   SheetTrigger
   Sidebar
   SidebarContent
   SidebarFooter
   SidebarGroup
   SidebarGroupAction
   SidebarGroupContent
   SidebarGroupLabel
   SidebarHeader
   SidebarInput
   SidebarInset
   SidebarMenu
   SidebarMenuAction
   SidebarMenuBadge
   SidebarMenuButton
   SidebarMenuItem
   SidebarMenuSkeleton
   SidebarMenuSub
   SidebarMenuSubButton
   SidebarMenuSubItem
   SidebarProvider
   SidebarRail
   SidebarSeparator
   SidebarTrigger
   Skeleton
   Slider
   Switch
   Table
   TableBody
   TableCaption
   TableCell
   TableFooter
   TableHead
   TableHeader
   TableRow
   Tabs
   TabsContent
   TabsList
   TabsTrigger
   Textarea
   Toaster
   Toggle
   ToggleGroup
   ToggleGroupItem
   Tooltip
   TooltipContent
   TooltipProvider
   TooltipTrigger
   badgeVariants
   buttonVariants
   navigationMenuTriggerStyle
   toggleVariants
   useFormField
   useSidebar])

(def +components+
  (apply hash-map
         '[:fg/accordion                     {:tag -/Accordion}
           :fg/accordion-content             {:tag -/AccordionContent}
           :fg/accordion-item                {:tag -/AccordionItem}
           :fg/accordion-trigger             {:tag -/AccordionTrigger}
           :fg/alert                         {:tag -/Alert}
           :fg/alert-description             {:tag -/AlertDescription}
           :fg/alert-dialog                  {:tag -/AlertDialog}
           :fg/alert-dialog-action           {:tag -/AlertDialogAction}
           :fg/alert-dialog-cancel           {:tag -/AlertDialogCancel}
           :fg/alert-dialog-content          {:tag -/AlertDialogContent}
           :fg/alert-dialog-description      {:tag -/AlertDialogDescription}
           :fg/alert-dialog-footer           {:tag -/AlertDialogFooter}
           :fg/alert-dialog-header           {:tag -/AlertDialogHeader}
           :fg/alert-dialog-overlay          {:tag -/AlertDialogOverlay}
           :fg/alert-dialog-portal           {:tag -/AlertDialogPortal}
           :fg/alert-dialog-title            {:tag -/AlertDialogTitle}
           :fg/alert-dialog-trigger          {:tag -/AlertDialogTrigger}
           :fg/alert-title                   {:tag -/AlertTitle}
           :fg/aspect-ratio                  {:tag -/AspectRatio}
           :fg/avatar                        {:tag -/Avatar}
           :fg/avatar-fallback               {:tag -/AvatarFallback}
           :fg/avatar-image                  {:tag -/AvatarImage}
           :fg/badge                         {:tag -/Badge}
           :fg/breadcrumb                    {:tag -/Breadcrumb}
           :fg/breadcrumb-ellipsis           {:tag -/BreadcrumbEllipsis}
           :fg/breadcrumb-item               {:tag -/BreadcrumbItem}
           :fg/breadcrumb-link               {:tag -/BreadcrumbLink}
           :fg/breadcrumb-list               {:tag -/BreadcrumbList}
           :fg/breadcrumb-page               {:tag -/BreadcrumbPage}
           :fg/breadcrumb-separator          {:tag -/BreadcrumbSeparator}
           :fg/button                        {:tag -/Button}
           :fg/calendar                      {:tag -/Calendar}
           :fg/card                          {:tag -/Card}
           :fg/card-action                   {:tag -/CardAction}
           :fg/card-content                  {:tag -/CardContent}
           :fg/card-description              {:tag -/CardDescription}
           :fg/card-footer                   {:tag -/CardFooter}
           :fg/card-header                   {:tag -/CardHeader}
           :fg/card-title                    {:tag -/CardTitle}
           :fg/carousel                      {:tag -/Carousel}
           :fg/carousel-content              {:tag -/CarouselContent}
           :fg/carousel-item                 {:tag -/CarouselItem}
           :fg/carousel-next                 {:tag -/CarouselNext}
           :fg/carousel-previous             {:tag -/CarouselPrevious}
           :fg/chart-container               {:tag -/ChartContainer}
           :fg/chart-legend                  {:tag -/ChartLegend}
           :fg/chart-legend-content          {:tag -/ChartLegendContent}
           :fg/chart-style                   {:tag -/ChartStyle}
           :fg/chart-tooltip                 {:tag -/ChartTooltip}
           :fg/chart-tooltip-content         {:tag -/ChartTooltipContent}
           :fg/checkbox                      {:tag -/Checkbox}
           :fg/collapsible                   {:tag -/Collapsible}
           :fg/collapsible-content           {:tag -/CollapsibleContent}
           :fg/collapsible-trigger           {:tag -/CollapsibleTrigger}
           :fg/command                       {:tag -/Command}
           :fg/command-dialog                {:tag -/CommandDialog}
           :fg/command-empty                 {:tag -/CommandEmpty}
           :fg/command-group                 {:tag -/CommandGroup}
           :fg/command-input                 {:tag -/CommandInput}
           :fg/command-item                  {:tag -/CommandItem}
           :fg/command-list                  {:tag -/CommandList}
           :fg/command-separator             {:tag -/CommandSeparator}
           :fg/command-shortcut              {:tag -/CommandShortcut}
           :fg/context-menu                  {:tag -/ContextMenu}
           :fg/context-menu-checkbox-item    {:tag -/ContextMenuCheckboxItem}
           :fg/context-menu-content          {:tag -/ContextMenuContent}
           :fg/context-menu-group            {:tag -/ContextMenuGroup}
           :fg/context-menu-item             {:tag -/ContextMenuItem}
           :fg/context-menu-label            {:tag -/ContextMenuLabel}
           :fg/context-menu-portal           {:tag -/ContextMenuPortal}
           :fg/context-menu-radio-group      {:tag -/ContextMenuRadioGroup}
           :fg/context-menu-radio-item       {:tag -/ContextMenuRadioItem}
           :fg/context-menu-separator        {:tag -/ContextMenuSeparator}
           :fg/context-menu-shortcut         {:tag -/ContextMenuShortcut}
           :fg/context-menu-sub              {:tag -/ContextMenuSub}
           :fg/context-menu-sub-content      {:tag -/ContextMenuSubContent}
           :fg/context-menu-sub-trigger      {:tag -/ContextMenuSubTrigger}
           :fg/context-menu-trigger          {:tag -/ContextMenuTrigger}
           :fg/dialog                        {:tag -/Dialog}
           :fg/dialog-close                  {:tag -/DialogClose}
           :fg/dialog-content                {:tag -/DialogContent}
           :fg/dialog-description            {:tag -/DialogDescription}
           :fg/dialog-footer                 {:tag -/DialogFooter}
           :fg/dialog-header                 {:tag -/DialogHeader}
           :fg/dialog-overlay                {:tag -/DialogOverlay}
           :fg/dialog-portal                 {:tag -/DialogPortal}
           :fg/dialog-title                  {:tag -/DialogTitle}
           :fg/dialog-trigger                {:tag -/DialogTrigger}
           :fg/drawer                        {:tag -/Drawer}
           :fg/drawer-close                  {:tag -/DrawerClose}
           :fg/drawer-content                {:tag -/DrawerContent}
           :fg/drawer-description            {:tag -/DrawerDescription}
           :fg/drawer-footer                 {:tag -/DrawerFooter}
           :fg/drawer-header                 {:tag -/DrawerHeader}
           :fg/drawer-overlay                {:tag -/DrawerOverlay}
           :fg/drawer-portal                 {:tag -/DrawerPortal}
           :fg/drawer-title                  {:tag -/DrawerTitle}
           :fg/drawer-trigger                {:tag -/DrawerTrigger}
           :fg/dropdown-menu                 {:tag -/DropdownMenu}
           :fg/dropdown-menu-checkbox-item   {:tag -/DropdownMenuCheckboxItem}
           :fg/dropdown-menu-content         {:tag -/DropdownMenuContent}
           :fg/dropdown-menu-group           {:tag -/DropdownMenuGroup}
           :fg/dropdown-menu-item            {:tag -/DropdownMenuItem}
           :fg/dropdown-menu-label           {:tag -/DropdownMenuLabel}
           :fg/dropdown-menu-portal          {:tag -/DropdownMenuPortal}
           :fg/dropdown-menu-radio-group     {:tag -/DropdownMenuRadioGroup}
           :fg/dropdown-menu-radio-item      {:tag -/DropdownMenuRadioItem}
           :fg/dropdown-menu-separator       {:tag -/DropdownMenuSeparator}
           :fg/dropdown-menu-shortcut        {:tag -/DropdownMenuShortcut}
           :fg/dropdown-menu-sub             {:tag -/DropdownMenuSub}
           :fg/dropdown-menu-sub-content     {:tag -/DropdownMenuSubContent}
           :fg/dropdown-menu-sub-trigger     {:tag -/DropdownMenuSubTrigger}
           :fg/dropdown-menu-trigger         {:tag -/DropdownMenuTrigger}
           :fg/form                          {:tag -/Form}
           :fg/form-control                  {:tag -/FormControl}
           :fg/form-description              {:tag -/FormDescription}
           :fg/form-field                    {:tag -/FormField}
           :fg/form-item                     {:tag -/FormItem}
           :fg/form-label                    {:tag -/FormLabel}
           :fg/form-message                  {:tag -/FormMessage}
           :fg/hover-card                    {:tag -/HoverCard}
           :fg/hover-card-content            {:tag -/HoverCardContent}
           :fg/hover-card-trigger            {:tag -/HoverCardTrigger}
           :fg/image-with-fallback           {:tag -/ImageWithFallback}
           :fg/input                         {:tag -/Input}
           :fg/input-otp                     {:tag -/InputOTP}
           :fg/input-otpgroup                {:tag -/InputOTPGroup}
           :fg/input-otpseparator            {:tag -/InputOTPSeparator}
           :fg/input-otpslot                 {:tag -/InputOTPSlot}
           :fg/label                         {:tag -/Label}
           :fg/menubar                       {:tag -/Menubar}
           :fg/menubar-checkbox-item         {:tag -/MenubarCheckboxItem}
           :fg/menubar-content               {:tag -/MenubarContent}
           :fg/menubar-group                 {:tag -/MenubarGroup}
           :fg/menubar-item                  {:tag -/MenubarItem}
           :fg/menubar-label                 {:tag -/MenubarLabel}
           :fg/menubar-menu                  {:tag -/MenubarMenu}
           :fg/menubar-portal                {:tag -/MenubarPortal}
           :fg/menubar-radio-group           {:tag -/MenubarRadioGroup}
           :fg/menubar-radio-item            {:tag -/MenubarRadioItem}
           :fg/menubar-separator             {:tag -/MenubarSeparator}
           :fg/menubar-shortcut              {:tag -/MenubarShortcut}
           :fg/menubar-sub                   {:tag -/MenubarSub}
           :fg/menubar-sub-content           {:tag -/MenubarSubContent}
           :fg/menubar-sub-trigger           {:tag -/MenubarSubTrigger}
           :fg/menubar-trigger               {:tag -/MenubarTrigger}
           :fg/navigation-menu               {:tag -/NavigationMenu}
           :fg/navigation-menu-content       {:tag -/NavigationMenuContent}
           :fg/navigation-menu-indicator     {:tag -/NavigationMenuIndicator}
           :fg/navigation-menu-item          {:tag -/NavigationMenuItem}
           :fg/navigation-menu-link          {:tag -/NavigationMenuLink}
           :fg/navigation-menu-list          {:tag -/NavigationMenuList}
           :fg/navigation-menu-trigger       {:tag -/NavigationMenuTrigger}
           :fg/navigation-menu-viewport      {:tag -/NavigationMenuViewport}
           :fg/pagination                    {:tag -/Pagination}
           :fg/pagination-content            {:tag -/PaginationContent}
           :fg/pagination-ellipsis           {:tag -/PaginationEllipsis}
           :fg/pagination-item               {:tag -/PaginationItem}
           :fg/pagination-link               {:tag -/PaginationLink}
           :fg/pagination-next               {:tag -/PaginationNext}
           :fg/pagination-previous           {:tag -/PaginationPrevious}
           :fg/popover                       {:tag -/Popover}
           :fg/popover-anchor                {:tag -/PopoverAnchor}
           :fg/popover-content               {:tag -/PopoverContent}
           :fg/popover-trigger               {:tag -/PopoverTrigger}
           :fg/progress                      {:tag -/Progress}
           :fg/radio-group                   {:tag -/RadioGroup}
           :fg/radio-group-item              {:tag -/RadioGroupItem}
           :fg/resizable-handle              {:tag -/ResizableHandle}
           :fg/resizable-panel               {:tag -/ResizablePanel}
           :fg/resizable-panel-group         {:tag -/ResizablePanelGroup}
           :fg/scroll-area                   {:tag -/ScrollArea}
           :fg/scroll-bar                    {:tag -/ScrollBar}
           :fg/select                        {:tag -/Select}
           :fg/select-content                {:tag -/SelectContent}
           :fg/select-group                  {:tag -/SelectGroup}
           :fg/select-item                   {:tag -/SelectItem}
           :fg/select-label                  {:tag -/SelectLabel}
           :fg/select-scroll-down-button     {:tag -/SelectScrollDownButton}
           :fg/select-scroll-up-button       {:tag -/SelectScrollUpButton}
           :fg/select-separator              {:tag -/SelectSeparator}
           :fg/select-trigger                {:tag -/SelectTrigger}
           :fg/select-value                  {:tag -/SelectValue}
           :fg/separator                     {:tag -/Separator}
           :fg/sheet                         {:tag -/Sheet}
           :fg/sheet-close                   {:tag -/SheetClose}
           :fg/sheet-content                 {:tag -/SheetContent}
           :fg/sheet-description             {:tag -/SheetDescription}
           :fg/sheet-footer                  {:tag -/SheetFooter}
           :fg/sheet-header                  {:tag -/SheetHeader}
           :fg/sheet-title                   {:tag -/SheetTitle}
           :fg/sheet-trigger                 {:tag -/SheetTrigger}
           :fg/sidebar                       {:tag -/Sidebar}
           :fg/sidebar-content               {:tag -/SidebarContent}
           :fg/sidebar-footer                {:tag -/SidebarFooter}
           :fg/sidebar-group                 {:tag -/SidebarGroup}
           :fg/sidebar-group-action          {:tag -/SidebarGroupAction}
           :fg/sidebar-group-content         {:tag -/SidebarGroupContent}
           :fg/sidebar-group-label           {:tag -/SidebarGroupLabel}
           :fg/sidebar-header                {:tag -/SidebarHeader}
           :fg/sidebar-input                 {:tag -/SidebarInput}
           :fg/sidebar-inset                 {:tag -/SidebarInset}
           :fg/sidebar-menu                  {:tag -/SidebarMenu}
           :fg/sidebar-menu-action           {:tag -/SidebarMenuAction}
           :fg/sidebar-menu-badge            {:tag -/SidebarMenuBadge}
           :fg/sidebar-menu-button           {:tag -/SidebarMenuButton}
           :fg/sidebar-menu-item             {:tag -/SidebarMenuItem}
           :fg/sidebar-menu-skeleton         {:tag -/SidebarMenuSkeleton}
           :fg/sidebar-menu-sub              {:tag -/SidebarMenuSub}
           :fg/sidebar-menu-sub-button       {:tag -/SidebarMenuSubButton}
           :fg/sidebar-menu-sub-item         {:tag -/SidebarMenuSubItem}
           :fg/sidebar-provider              {:tag -/SidebarProvider}
           :fg/sidebar-rail                  {:tag -/SidebarRail}
           :fg/sidebar-separator             {:tag -/SidebarSeparator}
           :fg/sidebar-trigger               {:tag -/SidebarTrigger}
           :fg/skeleton                      {:tag -/Skeleton}
           :fg/slider                        {:tag -/Slider}
           :fg/switch                        {:tag -/Switch}
           :fg/table                         {:tag -/Table}
           :fg/table-body                    {:tag -/TableBody}
           :fg/table-caption                 {:tag -/TableCaption}
           :fg/table-cell                    {:tag -/TableCell}
           :fg/table-footer                  {:tag -/TableFooter}
           :fg/table-head                    {:tag -/TableHead}
           :fg/table-header                  {:tag -/TableHeader}
           :fg/table-row                     {:tag -/TableRow}
           :fg/tabs                          {:tag -/Tabs}
           :fg/tabs-content                  {:tag -/TabsContent}
           :fg/tabs-list                     {:tag -/TabsList}
           :fg/tabs-trigger                  {:tag -/TabsTrigger}
           :fg/textarea                      {:tag -/Textarea}
           :fg/toaster                       {:tag -/Toaster}
           :fg/toggle                        {:tag -/Toggle}
           :fg/toggle-group                  {:tag -/ToggleGroup}
           :fg/toggle-group-item             {:tag -/ToggleGroupItem}
           :fg/tooltip                       {:tag -/Tooltip}
           :fg/tooltip-content               {:tag -/TooltipContent}
           :fg/tooltip-provider              {:tag -/TooltipProvider}
           :fg/tooltip-trigger               {:tag -/TooltipTrigger}]))

(defn generate-blocks
  []
  (block/layout
   (vec (mapcat (fn [[k]]
                  [(keyword "fg" (str/spear-case (str k)))
                   {:tag (symbol "-" (str k))}])
                (sort (ns-publics *ns*))))))

(defn init-components
  []
  (compile/put-registry :figma +components+)
  true)

(def +init+
  (init-components))
