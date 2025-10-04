(ns js.tamagui
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:macro-only true
   :bundle {:default  [["tamagui" :as [* T]]]
            :toast    [["@tamagui/toast" :as [* TToast]]]
            :token    [["@tamagui/get-token" :as [* TToken]]]}})

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "TToken"
                                   :tag "js"}]

  [getSize])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "TToast"
                                   :tag "js"}]

  [Toast
   ToastProvider
   ToastViewport
   useToastController
   useToastState
   [ToastTitle Toast.Title]
   [ToastDescription Toast.Description]
   [ToastClose Toast.Close]
   [ToastAction Toast.Action]])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "T"
                                   :tag "js"}]
  [Accordion
   Adapt
   AdaptContents
   AdaptContext
   AdaptParent
   AdaptPortalContents
   AlertDialog
   AlertDialogAction
   AlertDialogCancel
   AlertDialogContent
   AlertDialogDescription
   AlertDialogOverlay
   AlertDialogPortal
   AlertDialogTitle
   AlertDialogTrigger
   Anchor
   AnimatePresence
   Article
   Aside
   Avatar
   AvatarFallback
   AvatarFallbackFrame
   AvatarFrame
   AvatarImage
   Button
   ButtonContext
   ButtonFrame
   ButtonIcon
   ButtonNestingContext
   ButtonText
   Card
   CardBackground
   CardFooter
   CardFrame
   CardHeader
   Checkbox
   [CheckboxIndicator Checkbox.Indicator]
   CheckboxContext
   CheckboxFrame
   CheckboxIndicatorFrame
   CheckboxStyledContext
   Circle
   ComponentContext
   Configuration
   Dialog
   DialogClose
   DialogContent
   DialogContext
   DialogDescription
   DialogOverlay
   DialogOverlayFrame
   DialogPortal
   DialogPortalFrame
   DialogProvider
   DialogTitle
   DialogTrigger
   DialogWarningProvider
   EnsureFlexed
   Fieldset
   FontLanguage
   Footer
   Form
   FormFrame
   FormProvider
   FormTrigger
   ForwardSelectContext
   Frame
   Group
   Group.Item
   GroupContext
   GroupFrame
   H1
   H2
   H3
   H4
   H5
   H6
   Handle
   Header
   Heading
   Image
   Input
   InputFrame
   Label
   LabelFrame
   ListItem
   ListItemFrame
   ListItemSubtitle
   ListItemText
   ListItemTitle
   Main
   Nav
   Overlay])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "T"
                                   :tag "js"}]
  [Paragraph
   ParentSheetContext
   Popover
   PopoverAnchor
   PopoverArrow
   PopoverClose
   PopoverContent
   PopoverContext
   PopoverTrigger
   Popper
   PopperAnchor
   PopperArrow
   PopperArrowFrame
   PopperContent
   PopperContentFrame
   PopperContextFast
   PopperContextSlow
   PopperPositionContext
   PopperProvider
   PopperProviderFast
   PopperProviderSlow
   Portal
   PortalHost
   PortalItem
   PortalProvider
   PresenceChild
   PresenceContext
   Progress
   ProgressFrame
   ProgressIndicator
   ProgressIndicatorFrame
   ProvideAdaptContext
   RadioGroup
   RadioGroupFrame
   RadioGroupIndicatorFrame
   RadioGroupItemFrame
   RadioGroupStyledContext
   Range
   ResetPresence
   ScrollView
   Section
   Select
   SelectGroupFrame
   SelectIcon
   SelectItemParentProvider
   SelectProvider
   SelectSeparator
   [SelectContent Select.Content]
   [SelectFocusScope Select.FocusScope]
   [SelectGroup Select.Group]
   [SelectItem Select.Item]
   [SelectItemIndicator Select.ItemIndicator]
   [SelectItemText Select.ItemText]
   [SelectLabel Select.Label]
   [SelectScrollDownButton Select.ScrollDownButton]
   [SelectScrollUpButton Select.ScrollUpButton]
   [SelectTrigger Select.Trigger]
   [SelectValue Select.Value]
   [SelectViewport Select.Viewport]
   Separator
   Sheet
   SheetController
   SheetControllerContext
   SheetHandleFrame
   SheetInsideSheetContext
   SheetOverlayFrame
   SheetScrollView
   SizableStack
   SizableText
   Slider
   SliderContext
   SliderFrame
   SliderThumb
   SliderThumbFrame
   SliderTrack
   SliderTrackActive
   SliderTrackActiveFrame
   SliderTrackFrame
   Spacer
   Spinner
   Square
   Stack
   StyleObjectIdentifier
   StyleObjectProperty
   StyleObjectPseudo
   StyleObjectRules
   StyleObjectValue
   Switch
   SwitchContext
   SwitchFrame
   SwitchStyledContext
   SwitchThumb
   Tabs
   TabsProvider
   
   Text
   TextArea
   TextAreaFrame
   Theme
   ThemeableStack
   Thumb
   ToggleGroup
   [ToggleGroupItem ToggleGroup.Item]
   Tooltip
   TooltipGroup
   TooltipSimple
   Track
   
   Unspaced
   View
   VisuallyHidden
   XGroup
   XStack
   YGroup
   YStack
   ZStack])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "T"
                                   :tag "js"}]

  [USE_NATIVE_PORTAL
   INITIAL_STATE
   IS_FABRIC
   TamaguiProvider
   addTheme
   allPortalHosts
   clamp
   closeOpenTooltips
   composeEventHandlers
   composeRefs
   concatClassName
   configureInitialWindowDimensions
   createAvatarScope
   createCheckbox
   createComponent
   createContext
   createContextScope
   createFont
   createMedia
   createProgressScope
   createRadioGroup
   createSheet
   createSheetScope
   createShorthands
   createStyledContext
   createSwitch
   createTabs
   createTamagui
   createTheme
   createTokens
   createVariable
   debounce
   defaultStyles
   fullscreenStyle
   getCSSStylesAtomic
   getConfig
   getFontSize
   getFontSizeToken
   getFontSizeVariable
   getMedia
   getNativeSheet
   getShapeSize
   getThemes
   getToken
   getTokenValue
   getTokens
   getVariable
   getVariableName
   getVariableValue
   insertFont
   isChrome
   isClient
   isPresent
   isServer
   isServerSide
   isTamaguiComponent
   isTamaguiElement
   isTouchable
   isVariable
   isWeb
   isWebTouchable
   matchMedia
   mediaObjectToString
   mediaQueryConfig
   mediaState
   mutateThemes
   portalListeners
   prevent
   replaceTheme
   resolveViewZIndex
   setConfig
   setOnLayoutStrategy
   setRef
   setupDev
   setupNativeSheet
   setupPopper
   setupReactNative
   shouldRenderNativePlatform
   simpleHash
   spacedChildren
   stylePropsAll
   stylePropsText
   stylePropsTextOnly
   stylePropsTransform
   stylePropsUnitless
   stylePropsView
   styled
   themeable
   themeableVariants
   tokenCategories
   updateTheme
   useAdaptContext
   useAdaptIsActive
   useButton
   useComposedRefs
   useConfiguration
   useControllableState
   useCurrentColor
   useDebounce
   useDebounceValue
   useDialogContext
   useDidFinishSSR
   useEvent
   useFloatingContext
   useForceUpdate
   useFormContext
   useGet
   useGetThemedIcon
   useGroupItem
   useInputProps
   useIsPresent
   useIsTouchDevice
   useIsomorphicLayoutEffect
   useLabelContext
   useListItem
   useMedia
   usePopoverContext
   usePopperContext
   usePopperContextSlow
   usePortal
   usePresence
   useProps
   usePropsAndStyle
   useSelectContext
   useSelectItemParentContext
   useSheet
   useSheetController
   useSheetOffscreenSize
   useSheetOpenState
   useStyle
   useTabsContext
   useTheme
   useThemeName
   useWindowDimensions
   validPseudoKeys
   validStyles
   variableToString
   withStaticProperties
   wrapChildrenInText])


