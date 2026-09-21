(ns component.web-melbourne
  (:require [lang.core :as l]
            [std.lib :as h]))

(l/script :js
  {:runtime :websocket
   :config {:id :test/web-main
            :bench false
            :emit {:native {:suppress true}
                   :lang/jsx false}
            :notify {:type :webpage :path "dev/notify"}}
   :require [[melbourne.ui-base-palette-test :as ui-base-palette-test]
             #_[melbourne.ui-common-test :as ui-common-test]
             [melbourne.ui-autocomplete-test :as ui-autocomplete-test]
             [melbourne.ui-static-test :as ui-static-test]
             [melbourne.ui-helper-test :as ui-helper-test]
             [melbourne.ui-button-test :as ui-button-test]
             [melbourne.ui-checkbox-test :as ui-checkbox-test]
             [melbourne.ui-chip-test :as ui-chip-test]
             [melbourne.ui-chip-input-test :as ui-chip-input-test]
             [melbourne.ui-color-input-test :as ui-color-input-test]
             [melbourne.ui-dropdown-test :as ui-dropdown-test]
             
             [melbourne.ui-group-test :as ui-group-test]
             [melbourne.ui-image-test :as ui-image-test]
             [melbourne.ui-input-test :as ui-input-test]
             [melbourne.ui-input-xl-test :as ui-input-xl-test]
             
             [melbourne.ui-radio-test :as ui-radio-test]
             [melbourne.ui-picker-test :as ui-picker-test]
             [melbourne.ui-picker-basic-test :as ui-picker-basic-test]
             [melbourne.ui-section-test :as ui-section-test]
             [melbourne.ui-spinner-test :as ui-spinner-test]
             [melbourne.ui-spinner-basic-test :as ui-spinner-basic-test]
             [melbourne.ui-stepper-test :as ui-stepper-test]
             [melbourne.ui-slider-test :as ui-slider-test]
             [melbourne.ui-swiper-test :as ui-swiper-test]
             
             [melbourne.ui-text-test :as ui-text-test]
             [melbourne.ui-text-dialog-test :as ui-text-dialog-test]
             [melbourne.ui-toggle-button-test :as ui-toggle-button-test]
             [melbourne.ui-toggle-switch-test :as ui-toggle-switch-test]
             [melbourne.ui-toolbar-test :as ui-toolbar-test]
             [melbourne.slim-common-test :as slim-common-test]
             #_[melbourne.slim-content-test :as slim-content-test]
             [melbourne.slim-dialog-test :as slim-dialog-test]
             [melbourne.slim-entry-test :as slim-entry-test]
             [melbourne.slim-error-test :as slim-error-test]
             [melbourne.slim-image-test :as slim-image-test]
             [melbourne.slim-link-test :as slim-link-test]
             [melbourne.slim-number-test :as slim-number-test]
             
             [melbourne.slim-select-test :as slim-select-test]
             [melbourne.slim-sheet-test :as slim-sheet-test]
             [melbourne.slim-submit-test :as slim-submit-test]
             [melbourne.slim-test :as slim-test]
             [melbourne.slim-table-common-test :as slim-table-common-test]
             [melbourne.slim-table-list-test :as slim-table-list-test]
             [melbourne.slim-table-page-test :as slim-table-page-test]
             [melbourne.slim-table-search-test :as slim-table-search-test]
             [melbourne.slim-table-test :as slim-table-test]
             #_[melbourne.slim-table-test :as slim-table-test]]
   :export [MODULE]})

(defn.js PaletteExamples
  []
  (return
   [:<>
    [:% ui-base-palette-test/PaletteBaseDemo]
    [:% ui-base-palette-test/PaletteToneDemo]]))

(defn.js TextExamples
  []
  (return
   [:<>
    [:% ui-text-test/HeaderBaseDemo]
    [:% ui-text-test/TextBaseDemo]
    [:% ui-text-test/MinorBaseDemo]
    [:% ui-text-test/AccentBaseDemo]
    [:% ui-text-test/AvatarDemo]
    [:% ui-text-test/ButtonTooltipDemo]
    [:% ui-text-test/ConfirmTooltipDemo]
    [:% ui-text-dialog-test/ConfirmDialogDemo]
    [:% ui-text-test/EnumMinorDemo]
    [:% ui-text-test/TabsMinorDemo]
    [:% ui-text-test/EnumAccentDemo]
    [:% ui-text-test/TabsAccentDemo]
    [:% ui-text-test/TextAltDemo]]))

(defn.js AutocompleteExamples
  []
  (return
   [:<>
    [:% ui-autocomplete-test/SelectSingleDemo]]))

(defn.js SectionExamples
  []
  (return
   [:<>
    [:% ui-section-test/SectionBaseDemo]
    [:% ui-section-test/SectionDemo]
    [:% ui-section-test/SectionFoldDemo]
    [:% ui-section-test/CardBoundaryDemo]
    [:% ui-section-test/EmptyButtonDemo]]))

(defn.js HelperExamples
  []
  (return
   [:<>
    [:% ui-helper-test/HelperControlDemo]]))

(defn.js StaticExamples
  []
  (return
   [:<>
    [:% ui-static-test/DivDemo]
    [:% ui-static-test/TextDemo]
    [:% ui-static-test/ScrollViewDemo]
    [:% ui-static-test/SeparatorDemo]
    [:% ui-static-test/TextDisplayDemo]]))

(defn.js ButtonExamples
  []
  (return
   [:<>
    [:% ui-button-test/ButtonDemo]]))

(defn.js ChipExamples
  []
  (return
   [:<>
    [:% ui-chip-test/ChipDemo]]))

(defn.js ChipInputExamples
  []
  (return
   [:<>
    [:% ui-chip-input-test/ChipInputDemo]]))

(defn.js ColorInputExamples
  []
  (return
   [:<>
    [:% ui-color-input-test/ColorInputDemo]]))

(defn.js ToggleButtonExamples
  []
  (return
   [:<>
    [:% ui-toggle-button-test/ToggleButtonDemo]]))

(defn.js InputExamples
  []
  (return
   [:<>
    [:% ui-input-test/InputLightExamples]
    [:% ui-input-test/InputDarkExamples]]))

(defn.js InputXLExamples
  []
  (return
   [:<>
    #_[:% ui-input-xl-test/InputPlaceHolderDemo]
    [:% ui-input-xl-test/InputXLDemo]]))

(defn.js EnumMultiExamples
  []
  (return
   [:<>
    [:% ui-group-test/EnumMultiIndexedDemo]
    [:% ui-group-test/EnumMultiDemo]]))

(defn.js GroupTabsExamples
  []
  (return
   [:<>
    [:% ui-group-test/TabsIndexedDemo]
    [:% ui-group-test/TabsDemo]]))

(defn.js GroupListExamples
  []
  (return
   [:<>
    [:% ui-group-test/ListIndexedDemo]
    [:% ui-group-test/ListDemo]]))

(defn.js MenuButtonExamples
  []
  (return
   [:<>
    #_[:% ui-menu-button-test/MenuButtonDemo]]))

(defn.js StepperExamples
  []
  (return
   [:<>
    [:% ui-stepper-test/StepperTabsDemo]
    [:% ui-stepper-test/StepperDemo]]))

(defn.js SpinnerExamples
  []
  (return
   [:<>
    #_#_
    [:% ui-spinner-test/SpinnerPaddingDemo]
    [:% ui-spinner-test/SpinnerDigitDemo]
    [:% ui-spinner-test/SpinnerControlsDemo]
    [:% ui-spinner-test/SpinnerValuesDemo]
    [:% ui-spinner-test/SpinnerDemo]]))

(defn.js SpinnerBasicExamples
  []
  (return
   [:<>
    #_#_
    [:% ui-spinner-test/SpinnerPaddingDemo]
    [:% ui-spinner-test/SpinnerDigitDemo]
    [:% ui-spinner-basic-test/SpinnerBasicControlsDemo]
    [:% ui-spinner-basic-test/SpinnerBasicDemo]
    [:% ui-spinner-basic-test/SpinnerBasicEditDemo]]))

(defn.js DropdownExamples
  []
  (return
   [:<>
    [:% ui-dropdown-test/DropdownDemo]
    [:% ui-dropdown-test/DropdownIndexedDemo]]))

(defn.js CheckBoxExamples
  []
  (return
   [:<>
    [:% ui-checkbox-test/CheckBoxDemo]
    [:% ui-checkbox-test/CheckGroupIndexedDemo]
    [:% ui-checkbox-test/CheckGroupDemo]]))

(defn.js RadioExamples
  []
  (return
   [:<>
    [:% ui-radio-test/RadioBoxDemo]
    [:% ui-radio-test/RadioGroupIndexedDemo]
    [:% ui-radio-test/RadioGroupDemo]]))

(defn.js SliderExamples
  []
  (return
   [:<>
    [:% ui-slider-test/SliderHDemo]
    [:% ui-slider-test/SliderVDemo]]))

(defn.js PickerExamples
  []
  (return
   [:<>
    [:% ui-picker-test/PickerValuesDemo]
    [:% ui-picker-test/PickerIndexedDemo]
    [:% ui-picker-test/PickerDemo]]))

(defn.js PickerBasicExamples
  []
  (return
   [:<>
    [:% ui-picker-basic-test/PickerBasicIndexedDemo]
    [:% ui-picker-basic-test/PickerBasicDemo]]))


(defn.js SwiperExamples
  []
  (return
   [:<>
    [:% ui-swiper-test/SwiperDemo]]))

(defn.js ImageExamples
  []
  (return
   [:<>
    [:% ui-image-test/SelectImageDemo]
    [:% ui-image-test/ImagePickerDemo]
    [:% ui-image-test/ImagePickerSecondaryDemo]]))

(defn.js ToggleSwitchExamples
  []
  (return
   [:<>
    [:% ui-toggle-switch-test/ToggleSwitchDemo]]))

(defn.js ToolbarExamples
  []
  (return
   [:<>
    #_#_
    [:% ui-toolbar-test/ToolbarToggleButtonDemo]
    [:% ui-toolbar-test/ToolbarTabsDemo]
    [:% ui-toolbar-test/ToolbarOverlayTooltipDemo]
    [:% ui-toolbar-test/ToolbarOverlayDemo]
    [:% ui-toolbar-test/ToolbarDemo]
    [:% ui-toolbar-test/ToolbarAnnexDemo]]))

(defn.js AddonValidationDemo
  []
  (return
   [:<>]))

(defn.js SlimCommonExamples
  []
  (return
   [:<>
    [:% slim-common-test/FormEnclosedDemo]
    [:% slim-common-test/FormInputDemo]
    [:% slim-common-test/FormInputXLDemo]
    [:% slim-common-test/FormTextAreaDemo]
    [:% slim-common-test/FormCheckBoxDemo]
    [:% slim-common-test/FormToggleButtonDemo]
    [:% slim-common-test/FormToggleSwitchDemo]
    [:% slim-common-test/FormEnumSingleDemo]
    [:% slim-common-test/FormEnumMultiDemo]
    [:% slim-common-test/FormColorInputDemo]
    [:% slim-common-test/FormChipInputDemo]
    [:% slim-common-test/FormLayoutDemo]]))

(defn.js SlimEntryExamples
  []
  (return
   [:<>
    
    [:% slim-entry-test/EntryFreeDemo]
    [:% slim-entry-test/EntryContentRawDemo]
    [:% slim-entry-test/EntryLayoutHorizontalDemo]
    [:% slim-entry-test/EntryLayoutVerticalDemo]
    [:% slim-entry-test/EntryLayoutEnclosedDemo]
    [:% slim-entry-test/EntryLayoutPortalDemo]
    [:% slim-entry-test/EntryLayoutPortalSinkDemo]
    [:% slim-entry-test/EntryLayoutDebugDemo]
    
    [:% slim-entry-test/EntryLayoutFormFadeDemo]
    [:% slim-entry-test/EntryLayoutFormFoldDemo]
    [:% slim-entry-test/EntryContentTitleH1Demo]
    [:% slim-entry-test/EntryContentTitleH2Demo]
    [:% slim-entry-test/EntryContentTitleH3Demo]
    [:% slim-entry-test/EntryContentTitleH4Demo]
    [:% slim-entry-test/EntryContentTitleH5Demo]
    [:% slim-entry-test/EntryContentBoldDemo]
    [:% slim-entry-test/EntryContentRawDemo]
    [:% slim-entry-test/EntryContentRawFormDemo]
    [:% slim-entry-test/EntryContentFillDemo]
    [:% slim-entry-test/EntryContentTitleDemo]
    [:% slim-entry-test/EntryContentParagraphDemo]
    [:% slim-entry-test/EntryContentSeparatorDemo]
    [:% slim-entry-test/EntryContentIconDemo]
    [:% slim-entry-test/EntryContentImageDemo]
    [:% slim-entry-test/EntryContentPairDemo]
    [:% slim-entry-test/EntryContentFieldDemo]
    [:% slim-entry-test/EntryContentControlDemo]
    [:% slim-entry-test/EntryLayoutControlDemo]
    [:% slim-entry-test/EntryContentActionDemo]
    [:% slim-entry-test/EntryContentLinkDemo]
    [:% slim-entry-test/EntryLayoutLinkDemo]
    [:% slim-entry-test/EntryContentRouteDemo]
    [:% slim-entry-test/EntryContentRouteToggleDemo]
    [:% slim-entry-test/EntryContentSubmitDemo]
    [:% slim-entry-test/EntryLayoutCardDemo]
    [:% slim-entry-test/EntryLayoutFormDemo]
    
    
    [:% slim-entry-test/EntryDemo]]))

(defn.js SlimEntryPopupExamples
  []
  (return
   [:<>
    [:% slim-entry-test/EntryLayoutPopupDemo]]))

(defn.js SlimSheetExamples
  []
  (return
   [:<>
    [:% slim-sheet-test/SheetHeaderDemo]
    [:% slim-sheet-test/SheetRowDemo]
    [:% slim-sheet-test/SheetBasicRowsDemo]
    [:% slim-sheet-test/SheetBasicDemo]
    [:% slim-sheet-test/SheetPaginationDemo]
    [:% slim-sheet-test/SheetGroupHeaderDemo]
    [:% slim-sheet-test/SheetGroupRowsDemo]
    [:% slim-sheet-test/SheetDemo]]))

(defn.js SlimTableMiscExamples
  []
  (return
   [:<>
    [:% slim-table-common-test/TableDefaultNotFoundDemo]
    [:% slim-table-common-test/TableDefaultIsLoadingDemo]
    [:% slim-table-common-test/TableBackButtonDemo]
    [:% slim-table-list-test/TableListCardBriefDemo]
    [:% slim-table-list-test/TableListCardNavDemo]
    [:% slim-table-list-test/TableListCardFoldDemo]
    [:% slim-table-list-test/TableListCardSwipeDemo]
    [:% slim-table-list-test/TableListDemo]]))

(defn.js SlimTablePageExamples
  []
  (return
   [:<>
    [:% slim-table-list-test/TableListViewPagedDemo]
    [:% slim-table-page-test/TableListPagedDemo]
    [:% slim-table-list-test/TableListViewRemotePagedDemo]
    [:% slim-table-page-test/TableListRemotePagedDemo]]))

(defn.js SlimTableSearchExamples
  []
  (return
   [:% slim-table-search-test/TableListSearchDemo]))

(defn.js SlimTableGroupExamples
  []
  (return
   [:<>
    [:% slim-table-list-test/TableListViewEntriesDemo]
    [:% slim-table-list-test/TableListViewGroupDemo]
    [:% slim-table-list-test/TableListViewDemo]]))



(defn.js SlimTableExamples
  []
  (return
   [:<>
    [:% slim-table-test/TableDetailViewDemo]
    [:% slim-table-test/TableCreateViewDemo]
    [:% slim-table-test/TableModifyViewDemo]
    [:% slim-table-test/TableRouterViewDemo]
    [:% slim-table-test/TableRouterDemo]
    [:% slim-table-test/TableDemo]]))

(defn.js SlimTableExtraExamples
  []
  (return
   [:<>
    [:% slim-table-test/TableStandardDemo]
    [:% slim-table-test/TableEmbeddedDemo]]))

(defn.js SlimExamples
  []
  (return
   [:<>
    [:% slim-test/TableDemo]
    [:% slim-test/CreateEntryDemo]
    [:% slim-test/EntryDemo]
    [:% slim-test/UseRouteControlDemo]]))

(defn.js SlimDialogExamples
  []
  (return
   [:<>
    [:% slim-dialog-test/DialogDemo]]))



(defn.js SlimImageExamples
  []
  (return
   [:<>
    [:% slim-image-test/FormImageDemo]]))


(defn.js SlimLinkExamples
  []
  (return
   [:<>
    [:% slim-link-test/FormLinkDropdownDemo]
    [:% slim-link-test/FormLinkReadOnlyDemo]
    [:% slim-link-test/FormLinkEntryReadOnlyDemo]]))


(defn.js SlimNumberExamples
  []
  (return
   [:<>
    [:% slim-number-test/FormSpinnerDemo]
    [:% slim-number-test/FormSliderDemo]]))

(defn.js SlimSelectExamples
  []
  (return
   [:<>
    [:% slim-select-test/FormPickerDemo]
    [:% slim-select-test/FormDropdownDemo]]))

(defn.js SlimErrorExamples
  []
  (return
   [:<>
    [:% slim-error-test/ErrorInfoDemo]]))

(defn.js SlimSubmitExamples
  []
  (return
   [:<>
    [:% slim-submit-test/UseSubmitDemo]
    [:% slim-submit-test/SubmitButtonDemo]
    [:% slim-submit-test/SubmitLineDemo]
    [:% slim-submit-test/SubmitLineActionsDemo]
    [:% slim-submit-test/UseSubmitFieldDemo]
    [:% slim-submit-test/UseSubmitFormDemo]]))

;;
;; MELBOURNE
;;

(defn.js melbourne-controls
  []
  (return
   (tab ["000-palette"       -/PaletteExamples]
        #_["100-background"    -/BackgroundExamples]
        ["101-helper"       -/HelperExamples]
        ["102-static"        -/StaticExamples]
        ["201a-button"       -/ButtonExamples]
        ["201b-toggle"       -/ToggleButtonExamples]
        ["201c-switch"       -/ToggleSwitchExamples]
        ["202a-input"        -/InputExamples]
        ["202b-input-xl"     -/InputXLExamples]
        ["203a-enum-multi"   -/EnumMultiExamples]
        ["203b-group-tabs"   -/GroupTabsExamples]
        ["203c-group-list"   -/GroupListExamples]
        ["204a-dropdown"     -/DropdownExamples]
        ["204b-menu-button"  -/MenuButtonExamples]
        ["205-slider"        -/SliderExamples]
        ["206a-checkbox"     -/CheckBoxExamples]
        ["206b-radio"        -/RadioExamples]
        ["206c-chip"         -/ChipExamples]
        ["206d-chip-input"   -/ChipInputExamples]
        ["206e-color-input"  -/ColorInputExamples]

        ["207-spinner"        -/SpinnerExamples]
        ["207a-spinner-basic" -/SpinnerBasicExamples]
        ["208-stepper"       -/StepperExamples]
        ["209-picker"        -/PickerExamples]
        ["209a-picker-basic"  -/PickerBasicExamples]
        ["210-swiper"        -/SwiperExamples]
        ["211-image"         -/ImageExamples]
        ["212-text"          -/TextExamples]
        ["213-section"       -/SectionExamples]
        ["214-autocomplete"  -/AutocompleteExamples]
        ["215-toolbar"       -/ToolbarExamples])))

(defn.js slim-controls
  []
  (return
   (tab ["201a-slim-common"   -/SlimCommonExamples]
        ["201b-slim-number"   -/SlimNumberExamples]
        ["201c-slim-select"   -/SlimSelectExamples]
        ["201e-slim-image"    -/SlimImageExamples]
        ["201f-slim-link"     -/SlimLinkExamples]
        ["201g-slim-error"    -/SlimErrorExamples]
        ["201h-slim-submit"   -/SlimSubmitExamples]
        ["201i-slim-dialog"   -/SlimDialogExamples]
        ["201j-slim-entry"    -/SlimEntryExamples]
        ["201k-slim-popup"    -/SlimEntryPopupExamples]
        ["201l-slim-sheet"    -/SlimSheetExamples]
        
        ["201m-slim-tablem"   -/SlimTableMiscExamples]
        ["201n-slim-tableg"   -/SlimTableGroupExamples]
        ["201o-slim-table"    -/SlimTableExamples]
        ["201p-slim-tablep"   -/SlimTablePageExamples]
        ["201q-slim-tables"   -/SlimTableSearchExamples]
        ["201r-slim-tablex"   -/SlimTableExtraExamples]
        ["201s-slim"          -/SlimExamples])))

(def.js MODULE (!:module))
