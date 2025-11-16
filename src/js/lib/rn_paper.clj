(ns js.lib.rn-paper
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js
  {:import [["react-native-paper" :as [* RNPaper]]]})

;;
;; Paper
;;

(def +paper+
  '[ActivityIndicator
    Appbar

    Appbar.Action
    Appbar.BackAction
    Appbar.Content
    Appbar.Header
    Avatar

    Avatar.Icon
    Avatar.Image
    Avatar.Text
    Badge
    Banner
    BottomNavigation
    Button
    Card

    Card.Actions
    Card.Content
    Card.Cover
    Card.Title
    Checkbox

    Checkbox.Android
    Checkbox.IOS
    Checkbox.Item
    Chip
    DataTable

    DataTable.Cell
    DataTable.Header
    DataTable.Pagination
    DataTable.Title
    DataTable.Row
    Dialog

    Dialog.Actions
    Dialog.Content
    Dialog.ScrollArea
    Dialog.Title
    Divider
    Drawer

    Drawer.Item
    Drawer.Section
    FAB

    FAB.Group
    HelperText
    IconButton
    List

    List.Accordion
    List.AccordionGroup
    List.Icon
    List.Item
    List.Section
    List.Subheader
    Menu

    Menu.Item
    Modal
    Portal

    Portal.Host
    ProgressBar
    RadioButton

    RadioButton.Android
    RadioButton.Group
    RadioButton.IOS
    RadioButton.Item
    Searchbar
    Snackbar
    Surface
    Switch
    TextInput

    TextInput.Affix
    TextInput.Icon
    ToggleButton

    ToggleButton.Group
    ToggleButton.Row
    TouchableRipple

    Text
    Title
    Subheading
    Paragraph
    Headline
    Caption])

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "RNPaper"
                                   :tag "js"
                                   :shrink true}]
  +paper+)

(h/template-entries [l/tmpl-entry {:type :fragment
                                   :base "RNPaper"
                                   :tag "js"}]
  [useTheme
   withTheme
   Provider
   DefaultTheme
   DarkTheme])
