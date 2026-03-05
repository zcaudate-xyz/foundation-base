(ns bb.lang.readme)

'(. \*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*

    [TABLE OF CONTENTS]

    \*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*


    (\. [\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*

         - [:0 INTRODUCTION

            :1 WHY USE "H Lang" ?

            :2 WORKFLOW DESIGN

            :3 ARCHITECTURE DESIGN

            :4 FUTURE EXTENSIONS]

         \*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*]))


'(. \*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*

    [:0 INTRODUCTION]

    \*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*

    (\. ["H Lang" is a language, compiler and runtime
         allowing for \:

         - [a Meta-language for defining a
            language spec for creating a DSL
            that will transpile the code to ]

         - [dynamic generation and execution
            of arbitrary code for any language
            with an existing compatible spec]

         - [easier to evaluate performance
            on different platforms without
            the hurdle on setups for each]

         - [testing with part systems piecemeal
            for comms between systems]

         - [target and multiple runtimes with the
            availability of "X Lang" for creating
            notification pathways as will as
            arbitrary generated ui test-beds]]))

'(. \*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*

    [:1 WHY USE ^H LANG]

    \*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*

    (\. [Real world systems are a combination of many systems,
         requiring different languages and runtimes
         to operate seamlessly alongside each other to produce
         the needed results])

    (\. [With this approach, code is managed at the function level
         stored in a Format that the easily analysable and having
         the ability to transpile lisp code to the target language
         the per unit approach allows better management due to the
         fact that all testing and documentation uses the same
         framework which avoids context switching on big projects.])

    (\. [top level forms such as defn.<tag> will push code to the
         language library and then create a pointer to that piece
         of code.]))

'(. \*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*

    [:2 WORKFLOW DESIGN]

    \*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*)

'(. \*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*

    [:3 ARCHITECTURE DESIGN]

    \*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*)

'(. \*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*

    [:4 FUTURE EXTENSIONS]

    \*\*\*\*\*\*\*\*\*\*\*\*\*\*\*\*)



;;
;; There is really two important concepts at play here:
;;
;; LIBRARY/PROJECT
;;
;; - In general, most real world systems are a combination of
;;   many systems, requiring different languages and runtimes
;;   to operate seamlessly alongside each other to produce
;;   the needed results
;;
;; - with this approach, code is managed at the function level
;;   stored in a format that the easily analysable and having
;;   the ability to transpile lisp code to the target language
;;   the per unit approach allows better management due to the
;;   fact that all testing and documentation uses the same framework
;;   which avoids context switching on big projects.
;;
;; - top level forms such as defn.<tag> will push code to the
;;   language library and then create a pointer to that piece
;;   of code.
;;
;; - there is also a codegen component to the library that can
;;   be used to generate static files as well as provide the
;;   basic to send inputs to various runtimes.
;;
;; - codegen for projects allows both the sources and their
;;   the project configuration to be generated and emitted
;;   a `Makefile` is used to provide the most versatile setup
;;   that is language agnostic. Various config formats such
;;   as `yaml`, `toml`, `json`, `nginx.conf` are supported
;;   as well as being able to extend custom formats
;;
;; RUNTIME/TESTING
;;
;; - the main reason for this is to provide a unified way of
;;   executing code and quickly verifying that the code is
;;   correct on a step by step approach.
;;
;; - the pointer is static and points to code in a given module
;;
;; - different runtimes need different setups
;;   - some need to be run either remotely or within a specific context
;;   - all require the source to be formatted a certain way
;;   - some retain state of the functions per session (terminal based)
;;   - some retain state of the function indefinately (database, blockchain)
;;   - some execute only on input sent (redis, openresty)
;;
;; - the runtime which contains dynamic information determining
;;   how the pointer will behave when certain actions are required
;;
;;   - a default module name (for creating new code and pointers)
;;   - runtime type (specifies the general behaviour of pointers)
;;       - by default, code is stored into library and will emit
;;         a string of it's own form when evaluated and when
;;         invoked with args, will output the string of the
;;         form
;;
;;   - new runtimes can be created having different kinds of
;;     behaviour depending on where it is targetting
;;     - js has graal and webview runtimes
;;     - lua has redis and openresty runtimes
;;     - c has jocl runtime
;;     - python has
;;     - postgres is it's own runtime
;;
;; - basically, the more finer grain testing gets, the less errors
;;   can cause uncertainty and the quicker code can be produced,
;;   refactored and hardened.
;;
