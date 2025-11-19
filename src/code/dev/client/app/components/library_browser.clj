(ns code.dev.client.app.components.library-browser
  (:require [std.lang :as l]))

(l/script :js
  {:require [[js.react :as r]
             [js.react.dnd :as dnd]
             [js.lib.figma :as fg]
             [js.lib.lucide :as lc]
             [code.dev.client.app.app :as app]]})

(var componentLibrary
  [(do {:id "ui.sections/hero-gradient"
        :namespace "ui.sections"
        :name "HeroGradient"
        :description "Hero section with gradient background"
        :stars 245
        :component
        (do {:id "hero-section"
             :type "Container"
             :label "Hero Section"
             :libraryRef "ui.sections/HeroGradient"
             :properties {:className "bg-gradient-to-r from-purple-600 to-blue-600 text-white py-20 px-6"}
             :children
             [(do {:id "hero-content"
                   :type "Container"
                   :label "Hero Content"
                   :properties {:className "max-w-4xl mx-auto text-center"}
                   :children
                   [(do {:id "hero-title"
                         :type "Heading"
                         :label "Title"
                         :properties {:children "Build Amazing UIs"
                                      :className "text-5xl font-bold mb-4"}
                         :children []})
                    (do {:id "hero-subtitle"
                         :type "Text"
                         :label "Subtitle"
                         :properties {:children "Create beautiful interfaces with our component builder"
                                      :className "text-xl mb-8 opacity-90"}
                         :children []})
                    (do {:id "hero-cta"
                         :type "Button"
                         :label "CTA Button"
                         :properties {:children "Get Started"
                                      :className "bg-white text-purple-600 px-8 py-3 rounded-lg font-semibold hover:bg-gray-100"}
                         :children []})]})]})
   (do {:id "ui.sections/hero-centered"
        :namespace "ui.sections"
        :name "HeroCentered"
        :description "Centered hero with background image"
        :stars 198
        :component
        (do {:id "hero-centered"
             :type "Container"
             :label "Hero Centered"
             :libraryRef "ui.sections/HeroCentered"
             :properties {:className "relative h-screen flex items-center justify-center bg-gray-900 text-white"}
             :children
             [(do {:id "hero-content-centered"
                   :type "Container"
                   :label "Content"
                   :properties {:className "text-center z-10"}
                   :children
                   [(do {:id "hero-title-centered"
                         :type "Heading"
                         :label "Title"
                         :properties {:children "Welcome to Our Platform"
                                      :className "text-6xl font-bold mb-4"}
                         :children []})
                    (do {:id "hero-subtitle-centered"
                         :type "Text"
                         :label "Subtitle"
                         :properties {:children "The best way to build modern applications"
                                      :className "text-2xl mb-8"}
                         :children []})]})]})})
   (do {:id "ui.sections/feature-grid"
        :namespace "ui.sections"
        :name "FeatureGrid"
        :description "3-column feature card grid"
        :stars 189
        :component
        (do {:id "feature-grid"
             :type "Container"
             :label "Feature Grid"
             :libraryRef "ui.sections/FeatureGrid"
             :properties {:className "grid grid-cols-1 md:grid-cols-3 gap-6 p-8"}
             :children
             [(do {:id "card-1"
                   :type "Card"
                   :label "Card 1"
                   :properties {:className "p-6 bg-white rounded-lg shadow-lg hover:shadow-xl transition-shadow"}
                   :children
                   [(do {:id "card-1-title"
                         :type "Heading"
                         :label "Title"
                         :properties {:children "Feature One"
                                      :className "text-2xl font-bold mb-3"}
                         :children []})
                    (do {:id "card-1-desc"
                         :type "Text"
                         :label "Description"
                         :properties {:children "Description of your amazing feature"
                                      :className "text-gray-600"}
                         :children []})]})
              (do {:id "card-2"
                   :type "Card"
                   :label "Card 2"
                   :properties {:className "p-6 bg-white rounded-lg shadow-lg hover:shadow-xl transition-shadow"}
                   :children
                   [(do {:id "card-2-title"
                         :type "Heading"
                         :label "Title"
                         :properties {:children "Feature Two"
                                      :className "text-2xl font-bold mb-3"}
                         :children []})
                    (do {:id "card-2-desc"
                         :type "Text"
                         :label "Description"
                         :properties {:children "Another great feature to showcase"
                                      :className "text-gray-600"}
                         :children []})]})
              (do {:id "card-3"
                   :type "Card"
                   :label "Card 3"
                   :properties {:className "p-6 bg-white rounded-lg shadow-lg hover:shadow-xl transition-shadow"}
                   :children
                   [(do {:id "card-3-title"
                         :type "Heading"
                         :label "Title"
                         :properties {:children "Feature Three"
                                      :className "text-2xl font-bold mb-3"}
                         :children []})
                    (do {:id "card-3-desc"
                         :type "Text"
                         :label "Description"
                         :properties {:children "Yet another awesome feature"
                                      :className "text-gray-600"}
                         :children []})]})]})})
   (do {:id "ui.navigation/navbar-simple"
        :namespace "ui.navigation"
        :name "NavbarSimple"
        :description "Simple navbar with logo and links"
        :stars 312
        :component
        (do {:id "navbar"
             :type "Container"
             :label "Navbar"
             :libraryRef "ui.navigation/NavbarSimple"
             :properties {:className "bg-white shadow-md px-6 py-4"}
             :children
             [(do {:id "navbar-content"
                   :type "FlexRow"
                   :label "Navbar Content"
                   :properties {:className "flex items-center justify-between max-w-7xl mx-auto"}
                   :children
                   [(do {:id "logo"
                         :type "Heading"
                         :label "Logo"
                         :properties {:children "Logo"
                                      :className "text-2xl font-bold text-purple-600"}
                         :children []})
                    (do {:id "nav-links"
                         :type "FlexRow"
                         :label "Nav Links"
                         :properties {:className "flex gap-6"}
                         :children
                         [(do {:id "link-1"
                               :type "Text"
                               :label "Link 1"
                               :properties {:children "Home"
                                            :className "text-gray-700 hover:text-purple-600 cursor-pointer"}
                               :children []})
                          (do {:id "link-2"
                               :type "Text"
                               :label "Link 2"
                               :properties {:children "About"
                                            :className "text-gray-700 hover:text-purple-600 cursor-pointer"}
                               :children []})
                          (do {:id "link-3"
                               :type "Text"
                               :label "Link 3"
                               :properties {:children "Contact"
                                            :className "text-gray-700 hover:text-purple-600 cursor-pointer"}
                               :children []})]})]})]})})
   (do {:id "ui.navigation/navbar-cta"
        :namespace "ui.navigation"
        :name "NavbarWithCTA"
        :description "Navbar with call-to-action button"
        :stars 278
        :component
        (do {:id "navbar-cta"
             :type "Container"
             :label "Navbar with CTA"
             :libraryRef "ui.navigation/NavbarWithCTA"
             :properties {:className "bg-gray-900 text-white px-6 py-4"}
             :children
             [(do {:id "navbar-cta-content"
                   :type "FlexRow"
                   :label "Content"
                   :properties {:className "flex items-center justify-between max-w-7xl mx-auto"}
                   :children
                   [(do {:id "logo-cta"
                         :type "Heading"
                         :label "Logo"
                         :properties {:children "Brand"
                                      :className "text-2xl font-bold"}
                         :children []})
                    (do {:id "cta-button"
                         :type "Button"
                         :label "CTA"
                         :properties {:children "Sign Up"
                                      :className "bg-purple-600 px-6 py-2 rounded-lg hover:bg-purple-700"}
                         :children []})]})]})})
   (do {:id "ui.forms/contact-form"
        :namespace "ui.forms"
        :name "ContactForm"
        :description "Basic contact form layout"
        :stars 156
        :component
        (do {:id "contact-form"
             :type "Container"
             :label "Contact Form"
             :properties {:className "max-w-md mx-auto p-6 bg-white rounded-lg shadow-md"}
             :children
             [(do {:id "form-title"
                   :type "Heading"
                   :label "Form Title"
                   :properties {:children "Contact Us"
                                :className "text-2xl font-bold mb-4"}
                   :children []})
              (do {:id "name-input"
                   :type "Input"
                   :label "Name Input"
                   :properties {:placeholder "Your name"
                                :className "w-full px-4 py-2 border border-gray-300 rounded-lg mb-4"}
                   :children []})
              (do {:id "email-input"
                   :type "Input"
                   :label "Email Input"
                   :properties {:placeholder "Your email"
                                :className "w-full px-4 py-2 border border-gray-300 rounded-lg mb-4"}
                   :children []})
              (do {:id "submit-btn"
                   :type "Button"
                   :label "Submit Button"
                   :properties {:children "Send Message"
                                :className "w-full bg-purple-600 text-white px-4 py-2 rounded-lg hover:bg-purple-700"}
                   :children []})]})})
   (do {:id "ui.forms/login-form"
        :namespace "ui.forms"
        :name "LoginForm"
        :description "User login form with validation"
        :stars 203
        :component
        (do {:id "login-form"
             :type "Container"
             :label "Login Form"
             :libraryRef "ui.forms/LoginForm"
             :properties {:className "max-w-sm mx-auto p-8 bg-white rounded-xl shadow-lg"}
             :children
             [(do {:id "login-title"
                   :type "Heading"
                   :label "Title"
                   :properties {:children "Welcome Back"
                                :className "text-3xl font-bold mb-6 text-center"}
                   :children []})
              (do {:id "email-field"
                   :type "Input"
                   :label "Email"
                   :properties {:placeholder "Email address"
                                :className "w-full px-4 py-3 border border-gray-300 rounded-lg mb-4"}
                   :children []})
              (do {:id "password-field"
                   :type "Input"
                   :label "Password"
                   :properties {:placeholder "Password"
                                :className "w-full px-4 py-3 border border-gray-300 rounded-lg mb-6"}
                   :children []})
              (do {:id "login-btn"
                   :type "Button"
                   :label "Login"
                   :properties {:children "Sign In"
                                :className "w-full bg-blue-600 text-white px-4 py-3 rounded-lg font-semibold hover:bg-blue-700"}
                   :children []})]})})
   (do {:id "ui.cards/pricing-card"
        :namespace "ui.cards"
        :name "PricingCard"
        :description "Pricing card with features list"
        :stars 167
        :component
        (do {:id "pricing-card"
             :type "Card"
             :label "Pricing Card"
             :libraryRef "ui.cards/PricingCard"
             :properties {:className "max-w-sm p-8 bg-white rounded-2xl shadow-xl border-2 border-purple-200"}
             :children
             [(do {:id "plan-name"
                   :type "Heading"
                   :label "Plan Name"
                   :properties {:children "Pro Plan"
                                :className "text-2xl font-bold mb-2"}
                   :children []})
              (do {:id "price"
                   :type "Text"
                   :label "Price"
                   :properties {:children "$29/month"
                                :className "text-4xl font-bold text-purple-600 mb-6"}
                   :children []})
              (do {:id "features"
                   :type "FlexCol"
                   :label "Features"
                   :properties {:className "space-y-3 mb-8"}
                   :children
                   [(do {:id "feature-1"
                         :type "Text"
                         :label "Feature 1"
                         :properties {:children "✓ Unlimited projects"
                                      :className "text-gray-700"}
                         :children []})
                    (do {:id "feature-2"
                         :type "Text"
                         :label "Feature 2"
                         :properties {:children "✓ Priority support"
                                      :className "text-gray-700"}
                         :children []})
                    (do {:id "feature-3"
                         :type "Text"
                         :label "Feature 3"
                         :properties {:children "✓ Advanced analytics"
                                      :className "text-gray-700"}
                         :children []})]})
              (do {:id "subscribe-btn"
                   :type "Button"
                   :label "Subscribe"
                   :properties {:children "Get Started"
                                :className "w-full bg-purple-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-purple-700"}
                   :children []})]})})
   (do {:id "ui.layout/two-column"
        :namespace "ui.layout"
        :name "TwoColumn"
        :description "Responsive two-column layout"
        :stars 145
        :component
        (do {:id "two-column"
             :type "Container"
             :label "Two Column"
             :libraryRef "ui.layout/TwoColumn"
             :properties {:className "grid grid-cols-1 md:grid-cols-2 gap-8 p-8"}
             :children
             [(do {:id "col-left"
                   :type "Container"
                   :label "Left Column"
                   :properties {:className "bg-gray-100 p-6 rounded-lg"}
                   :children
                   [(do {:id "left-title"
                         :type "Heading"
                         :label "Title"
                         :properties {:children "Left Side"
                                      :className "text-2xl font-bold mb-4"}
                         :children []})
                    (do {:id "left-content"
                         :type "Text"
                         :label "Content"
                         :properties {:children "Content for the left column goes here"
                                      :className "text-gray-700"}
                         :children []})]})
              (do {:id "col-right"
                   :type "Container"
                   :label "Right Column"
                   :properties {:className "bg-gray-100 p-6 rounded-lg"}
                   :children
                   [(do {:id "right-title"
                         :type "Heading"
                         :label "Title"
                         :properties {:children "Right Side"
                                      :className "text-2xl font-bold mb-4"}
                         :children []})
                    (do {:id "right-content"
                         :type "Text"
                         :label "Content"
                         :properties {:children "Content for the right column goes here"
                                      :className "text-gray-700"}
                         :children []})]})]})})})])

(var buildNamespaceTree (fn [components]
                          (var root {:name "root"
                                     :fullPath ""
                                     :components []
                                     :children (new Map())})

                          (. components (forEach (fn [comp]
                                                   (var parts (. comp.namespace (split ".")))
                                                   (var current root)

                                                   (. parts (forEach (fn [part index]
                                                                       (when (not (. current.children (has part)))
                                                                         (. current.children (set part
                                                                                                (do {:name part
                                                                                                     :fullPath (. (. parts (slice 0 (+ index 1))) (join "."))
                                                                                                     :components []
                                                                                                     :children (new Map())}))))
                                                                       (:= current (. current.children (get part))))))
                                                   (. current.components (push comp))))))
                          (return root))

(defn.js LibraryBrowser [{:# [onImportComponent onImportAndEdit]}]
  (var [search setSearch] (r/useState ""))
  (var [expandedNamespaces setExpandedNamespaces] (r/useState (new Set ["ui"])))

  (var toggleNamespace (fn [path]
                         (setExpandedNamespaces (fn [prev]
                                                  (var next (new Set prev))
                                                  (if (. next (has path))
                                                    (. next (delete path))
                                                    (. next (add path)))
                                                  (return next)))))

  (var filteredComponents (. componentLibrary (filter (fn [comp]
                                                        (when (not search)
                                                          (return true))
                                                        (var searchLower (. search (toLowerCase)))
                                                        (return (or (. comp.name (toLowerCase) (includes searchLower))
                                                                    (. comp.namespace (toLowerCase) (includes searchLower))
                                                                    (. comp.description (toLowerCase) (includes searchLower))))))))

  (var namespaceTree (buildNamespaceTree filteredComponents))

  (var renderNamespaceNode (fn [node (:= depth 0)]
                             (var results [])
                             (var isExpanded (. expandedNamespaces (has node.fullPath)))

                             (when (!= node.name "root")
                               (. results (push
                                            [:div {:key node.fullPath}
                                              [:button
                                                {:onClick (fn [e]
                                                            (. e (stopPropagation))
                                                            (toggleNamespace node.fullPath))
                                                 :className (+ "w-full flex items-center gap-2 py-1 px-2 hover:bg-[#323232] text-left group ")
                                                 :style {:paddingLeft (+ (* depth 12) 8 "px")}}
                                                (:? isExpanded
                                                    [:% lc/ChevronDown {:className "w-3 h-3 text-gray-500"}]
                                                    [:% lc/ChevronRight {:className "w-3 h-3 text-gray-500"}])
                                                [:% lc/Folder {:className "w-3 h-3 text-blue-400"}]
                                                [:span {:className "text-xs text-gray-400"} node.name]
                                                [:span {:className "text-[10px] text-gray-600 ml-auto opacity-0 group-hover:opacity-100"}
                                                  (+ (. node.components length)
                                                     (. (Array.from (. node.children (values))) (reduce (fn [sum child] (return (+ sum (. child.components length)))) 0)))]]

                                              (:? isExpanded
                                                  [:<>
                                                    (. node.components (map (fn [comp]
                                                                              (return
                                                                                [:% -/LibraryComponentItem
                                                                                  {:key comp.id
                                                                                   :comp comp
                                                                                   :depth (+ depth 1)
                                                                                   :onImportComponent onImportComponent
                                                                                   :onImportAndEdit onImportAndEdit}]))))

                                                    (. (Array.from (. node.children (values))) (map (fn [childNode]
                                                                                                      (return (renderNamespaceNode childNode (+ depth 1))))))]
                                                  nil)])))
                             (when (== node.name "root")
                               (. (Array.from (. node.children (values))) (forEach (fn [childNode]
                                                                                     (. results (push (renderNamespaceNode childNode depth)))))))
                             (return results)))

  (return
    [:div {:className "flex flex-col h-full bg-[#252525]"}
      [:div {:className "p-3 border-b border-[#323232]"}
        [:h2 {:className "text-xs text-gray-400 uppercase tracking-wide mb-2"}
          "Component Library"]

        [:div {:className "relative"}
          [:% lc/Search {:className "absolute left-2 top-1/2 -translate-y-1/2 w-3 h-3 text-gray-500"}]
          [:% fg/Input
            {:value search
             :onChange (fn [e] (return (setSearch e.target.value)))
             :placeholder "Search namespaces..."
             :className "h-8 pl-7 bg-[#1e1e1e] border-[#323232] text-gray-300 text-xs placeholder:text-gray-600"}]]]

      [:% fg/ScrollArea {:className "flex-1"}
        [:div {:className "py-2"}
          (renderNamespaceNode namespaceTree)]]

      [:div {:className "p-2 border-t border-[#323232]"}
        [:div {:className "text-[10px] text-gray-600 text-center"}
          (+ (filteredComponents.length) " components in library")]]]))

(defn.js LibraryComponentItem [{:# [comp depth onImportComponent onImportAndEdit]}]
  (var [isDragging drag]
    (dnd/useDrag (fn []
                   (return {:type "LIBRARY_COMPONENT"
                            :item {:libraryComponent comp.component}
                            :collect (fn [monitor]
                                       (return {:isDragging (. monitor (isDragging))}))}))))

  (var handleDoubleClick (fn []
                           (onImportAndEdit comp.component)))

  (return
    [:div
      {:ref drag
       :onDoubleClick handleDoubleClick
       :className (+ "flex items-start gap-2 py-2 px-2 hover:bg-[#323232] group cursor-grab "
                     (:? isDragging "opacity-50 cursor-grabbing" ""))
       :style {:paddingLeft (+ (* depth 12) 8 "px")}}
      [:% lc/FileCode {:className "w-3 h-3 text-purple-400 mt-0.5 flex-shrink-0"}]
      [:div {:className "flex-1 min-w-0"}
        [:div {:className "flex items-center gap-2 mb-1"}
          [:span {:className "text-xs text-gray-300"} comp.name]
          [:div {:className "flex items-center gap-1 text-[10px] text-gray-500"}
            [:% lc/Star {:className "w-2.5 h-2.5 fill-current"}]
            comp.stars]]
        [:p {:className "text-[10px] text-gray-600 mb-1"} comp.description]
        [:% fg/Button
          {:size "sm"
           :onClick (fn [e]
                      (. e (stopPropagation))
                      (onImportComponent comp.component))
           :className "h-5 text-[10px] px-2 bg-[#404040] hover:bg-[#4a4a4a] text-gray-300 opacity-0 group-hover:opacity-100 transition-opacity"}
          [:% lc/Download {:className "w-2.5 h-2.5 mr-1"}]
          "Import"]
        [:% fg/Button
          {:size "sm"
           :onClick (fn [e]
                      (. e (stopPropagation))
                      (onImportAndEdit comp.component))
           :className "h-5 text-[10px] px-2 bg-[#404040] hover:bg-[#4a4a4a] text-gray-300 opacity-0 group-hover:opacity-100 transition-opacity"}
          [:% lc/Download {:className "w-2.5 h-2.5 mr-1"}]
          "Import & Edit"]]]))