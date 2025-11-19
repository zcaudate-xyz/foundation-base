(ns code.dev.webapp.layout-library-browser-data
  (:require [std.lang :as l]
            [std.lib :as h]))

(l/script :js)

(def.js componentLibraryMore
  [{:id "ui.sections/hero-centered"
    :namespace "ui.sections"
    :name "HeroCentered"
    :description "Centered hero with background image"
    :stars 198
    :component {:id "hero-centered"
                :type "Container"
                :label "Hero Centered"
                :libraryRef "ui.sections/HeroCentered"
                :properties {:className "relative h-screen flex items-center justify-center bg-gray-900 text-white"}
                :children [{:id "hero-content-centered"
                            :type "Container"
                            :label "Content"
                            :properties {:className "text-center z-10"}
                            :children [{:id "hero-title-centered"
                                        :type "Heading"
                                        :label "Title"
                                        :properties {:children "Welcome to Our Platform"
                                                     :className "text-6xl font-bold mb-4"}
                                        :children []}
                                       {:id "hero-subtitle-centered"
                                        :type "Text"
                                        :label "Subtitle"
                                        :properties {:children "The best way to build modern applications"
                                                     :className "text-2xl mb-8"}
                                        :children []}]}]}}
   {:id "ui.sections/feature-grid"
    :namespace "ui.sections"
    :name "FeatureGrid"
    :description "3-column feature card grid"
    :stars 189
    :component {:id "feature-grid"
                :type "Container"
                :label "Feature Grid"
                :libraryRef "ui.sections/FeatureGrid"
                :properties {:className "grid grid-cols-1 md:grid-cols-3 gap-6 p-8"}
                :children [{:id "card-1"
                            :type "Card"
                            :label "Card 1"
                            :properties {:className "p-6 bg-white rounded-lg shadow-lg hover:shadow-xl transition-shadow"}
                            :children [{:id "card-1-title"
                                        :type "Heading"
                                        :label "Title"
                                        :properties {:children "Feature One"
                                                     :className "text-2xl font-bold mb-3"}
                                        :children []}
                                       {:id "card-1-desc"
                                        :type "Text"
                                        :label "Description"
                                        :properties {:children "Description of your amazing feature"
                                                     :className "text-gray-600"}
                                        :children []}]}
                           {:id "card-2"
                            :type "Card"
                            :label "Card 2"
                            :properties {:className "p-6 bg-white rounded-lg shadow-lg hover:shadow-xl transition-shadow"}
                            :children [{:id "card-2-title"
                                        :type "Heading"
                                        :label "Title"
                                        :properties {:children "Feature Two"
                                                     :className "text-2xl font-bold mb-3"}
                                        :children []}
                                       {:id "card-2-desc"
                                        :type "Text"
                                        :label "Description"
                                        :properties {:children "Another great feature to showcase"
                                                     :className "text-gray-600"}
                                        :children []}]}
                           {:id "card-3"
                            :type "Card"
                            :label "Card 3"
                            :properties {:className "p-6 bg-white rounded-lg shadow-lg hover:shadow-xl transition-shadow"}
                            :children [{:id "card-3-title"
                                        :type "Heading"
                                        :label "Title"
                                        :properties {:children "Feature Three"
                                                     :className "text-2xl font-bold mb-3"}
                                        :children []}
                                       {:id "card-3-desc"
                                        :type "Text"
                                        :label "Description"
                                        :properties {:children "Yet another awesome feature"
                                                     :className "text-gray-600"}
                                        :children []}]}]}}
   {:id "ui.navigation/navbar-simple"
    :namespace "ui.navigation"
    :name "NavbarSimple"
    :description "Simple navbar with logo and links"
    :stars 312
    :component {:id "navbar"
                :type "Container"
                :label "Navbar"
                :libraryRef "ui.navigation/NavbarSimple"
                :properties {:className "bg-white shadow-md px-6 py-4"}
                :children [{:id "navbar-content"
                            :type "FlexRow"
                            :label "Navbar Content"
                            :properties {:className "flex items-center justify-between max-w-7xl mx-auto"}
                            :children [{:id "logo"
                                        :type "Heading"
                                        :label "Logo"
                                        :properties {:children "Logo"
                                                     :className "text-2xl font-bold text-purple-600"}
                                        :children []}
                                       {:id "nav-links"
                                        :type "FlexRow"
                                        :label "Nav Links"
                                        :properties {:className "flex gap-6"}
                                        :children [{:id "link-1"
                                                    :type "Text"
                                                    :label "Link 1"
                                                    :properties {:children "Home"
                                                                 :className "text-gray-700 hover:text-purple-600 cursor-pointer"}
                                                    :children []}
                                                   {:id "link-2"
                                                    :type "Text"
                                                    :label "Link 2"
                                                    :properties {:children "About"
                                                                 :className "text-gray-700 hover:text-purple-600 cursor-pointer"}
                                                    :children []}
                                                   {:id "link-3"
                                                    :type "Text"
                                                    :label "Link 3"
                                                    :properties {:children "Contact"
                                                                 :className "text-gray-700 hover:text-purple-600 cursor-pointer"}
                                                    :children []}]}]}]}}
   {:id "ui.navigation/navbar-cta"
    :namespace "ui.navigation"
    :name "NavbarWithCTA"
    :description "Navbar with call-to-action button"
    :stars 278
    :component {:id "navbar-cta"
                :type "Container"
                :label "Navbar with CTA"
                :libraryRef "ui.navigation/NavbarWithCTA"
                :properties {:className "bg-gray-900 text-white px-6 py-4"}
                :children [{:id "navbar-cta-content"
                            :type "FlexRow"
                            :label "Content"
                            :properties {:className "flex items-center justify-between max-w-7xl mx-auto"}
                            :children [{:id "logo-cta"
                                        :type "Heading"
                                        :label "Logo"
                                        :properties {:children "Brand"
                                                     :className "text-2xl font-bold"}
                                        :children []}
                                       {:id "cta-button"
                                        :type "Button"
                                        :label "CTA"
                                        :properties {:children "Sign Up"
                                                     :className "bg-purple-600 px-6 py-2 rounded-lg hover:bg-purple-700"}
                                        :children []}]}]}}
   {:id "ui.forms/contact-form"
    :namespace "ui.forms"
    :name "ContactForm"
    :description "Basic contact form layout"
    :stars 156
    :component {:id "contact-form"
                :type "Container"
                :label "Contact Form"
                :properties {:className "max-w-md mx-auto p-6 bg-white rounded-lg shadow-md"}
                :children [{:id "form-title"
                            :type "Heading"
                            :label "Form Title"
                            :properties {:children "Contact Us"
                                         :className "text-2xl font-bold mb-4"}
                            :children []}
                           {:id "name-input"
                            :type "Input"
                            :label "Name Input"
                            :properties {:placeholder "Your name"
                                         :className "w-full px-4 py-2 border border-gray-300 rounded-lg mb-4"}
                            :children []}
                           {:id "email-input"
                            :type "Input"
                            :label "Email Input"
                            :properties {:placeholder "Your email"
                                         :className "w-full px-4 py-2 border border-gray-300 rounded-lg mb-4"}
                            :children []}
                           {:id "submit-btn"
                            :type "Button"
                            :label "Submit Button"
                            :properties {:children "Send Message"
                                         :className "w-full bg-purple-600 text-white px-4 py-2 rounded-lg hover:bg-purple-700"}
                            :children []}]}}
   {:id "ui.forms/login-form"
    :namespace "ui.forms"
    :name "LoginForm"
    :description "User login form with validation"
    :stars 203
    :component {:id "login-form"
                :type "Container"
                :label "Login Form"
                :libraryRef "ui.forms/LoginForm"
                :properties {:className "max-w-sm mx-auto p-8 bg-white rounded-xl shadow-lg"}
                :children [{:id "login-title"
                            :type "Heading"
                            :label "Title"
                            :properties {:children "Welcome Back"
                                         :className "text-3xl font-bold mb-6 text-center"}
                            :children []}
                           {:id "email-field"
                            :type "Input"
                            :label "Email"
                            :properties {:placeholder "Email address"
                                         :className "w-full px-4 py-3 border border-gray-300 rounded-lg mb-4"}
                            :children []}
                           {:id "password-field"
                            :type "Input"
                            :label "Password"
                            :properties {:placeholder "Password"
                                         :className "w-full px-4 py-3 border border-gray-300 rounded-lg mb-6"}
                            :children []}
                           {:id "login-btn"
                            :type "Button"
                            :label "Login"
                            :properties {:children "Sign In"
                                         :className "w-full bg-blue-600 text-white px-4 py-3 rounded-lg font-semibold hover:bg-blue-700"}
                            :children []}]}}
   {:id "ui.cards/pricing-card"
    :namespace "ui.cards"
    :name "PricingCard"
    :description "Pricing card with features list"
    :stars 167
    :component {:id "pricing-card"
                :type "Card"
                :label "Pricing Card"
                :libraryRef "ui.cards/PricingCard"
                :properties {:className "max-w-sm p-8 bg-white rounded-2xl shadow-xl border-2 border-purple-200"}
                :children [{:id "plan-name"
                            :type "Heading"
                            :label "Plan Name"
                            :properties {:children "Pro Plan"
                                         :className "text-2xl font-bold mb-2"}
                            :children []}
                           {:id "price"
                            :type "Text"
                            :label "Price"
                            :properties {:children "$29/month"
                                         :className "text-4xl font-bold text-purple-600 mb-6"}
                            :children []}
                           {:id "features"
                            :type "FlexCol"
                            :label "Features"
                            :properties {:className "space-y-3 mb-8"}
                            :children [{:id "feature-1"
                                        :type "Text"
                                        :label "Feature 1"
                                        :properties {:children "✓ Unlimited projects"
                                                     :className "text-gray-700"}
                                        :children []}
                                       {:id "feature-2"
                                        :type "Text"
                                        :label "Feature 2"
                                        :properties {:children "✓ Priority support"
                                                     :className "text-gray-700"}
                                        :children []}
                                       {:id "feature-3"
                                        :type "Text"
                                        :label "Feature 3"
                                        :properties {:children "✓ Advanced analytics"
                                                     :className "text-gray-700"}
                                        :children []}]}
                           {:id "subscribe-btn"
                            :type "Button"
                            :label "Subscribe"
                            :properties {:children "Get Started"
                                         :className "w-full bg-purple-600 text-white px-6 py-3 rounded-lg font-semibold hover:bg-purple-700"}
                            :children []}]}}
   {:id "ui.layout/two-column"
    :namespace "ui.layout"
    :name "TwoColumn"
    :description "Responsive two-column layout"
    :stars 145
    :component {:id "two-column"
                :type "Container"
                :label "Two Column"
                :libraryRef "ui.layout/TwoColumn"
                :properties {:className "grid grid-cols-1 md:grid-cols-2 gap-8 p-8"}
                :children [{:id "col-left"
                            :type "Container"
                            :label "Left Column"
                            :properties {:className "bg-gray-100 p-6 rounded-lg"}
                            :children [{:id "left-title"
                                        :type "Heading"
                                        :label "Title"
                                        :properties {:children "Left Side"
                                                     :className "text-2xl font-bold mb-4"}
                                        :children []}
                                       {:id "left-content"
                                        :type "Text"
                                        :label "Content"
                                        :properties {:children "Content for the left column goes here"
                                                     :className "text-gray-700"}
                                        :children []}]}
                           {:id "col-right"
                            :type "Container"
                            :label "Right Column"
                            :properties {:className "bg-gray-100 p-6 rounded-lg"}
                            :children [{:id "right-title"
                                        :type "Heading"
                                        :label "Title"
                                        :properties {:children "Right Side"
                                                     :className "text-2xl font-bold mb-4"}
                                        :children []}
                                       {:id "right-content"
                                        :type "Text"
                                        :label "Content"
                                        :properties {:children "Content for the right column goes here"
                                                     :className "text-gray-700"}
                                        :children []}]}]}}])
