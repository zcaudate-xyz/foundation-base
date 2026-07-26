(ns tahto.core.registry)

(def +registry+
  (atom {[:postgres :default]          'tahto.model.spec-postgres
         [:postgres :jdbc]             'tahto.runtime.postgres.base.client
         [:postgres :jdbc.client]      'tahto.runtime.postgres.base.client
         
         [:solidity :default]          'tahto.runtime.solidity
		 
         [:bash   :oneshot]            'tahto.runtime.basic.impl.process-bash
         [:bash   :basic]              'tahto.runtime.shell
         [:bash   :remote]             'tahto.runtime.shell
         [:bash   :verify]             'tahto.runtime.basic.impl.process-bash
         
         [:lua    :oneshot]            'tahto.runtime.basic.impl.process-lua
         [:lua    :basic]              'tahto.runtime.basic.impl.process-lua
         [:lua    :interactive]        'tahto.runtime.basic.impl.process-lua
         [:lua    :websocket]          'tahto.runtime.basic.impl.process-lua
         [:lua    :nginx]              'tahto.runtime.nginx
         [:lua    :nginx.instance]     'tahto.runtime.nginx
         [:lua    :redis]              'tahto.runtime.redis
         [:lua.redis :default]         'tahto.runtime.redis
         [:lua.redis :redis]           'tahto.runtime.redis
         [:lua.nginx :oneshot]         'tahto.runtime.basic.impl.process-lua
         [:lua.nginx :basic]           'tahto.runtime.basic.impl.process-lua
         [:lua.nginx :interactive]     'tahto.runtime.basic.impl.process-lua
         [:lua.nginx :websocket]       'tahto.runtime.basic.impl.process-lua
         [:lua.nginx :nginx]           'tahto.runtime.nginx
         [:lua.nginx :nginx.instance]  'tahto.runtime.nginx
         [:lua.nginx :remote-port]     'tahto.runtime.basic.impl.process-lua
         [:lua.nginx :remote-ws]       'tahto.runtime.basic.impl.process-lua
         [:lua.nginx :verify]          'tahto.runtime.basic.impl.process-lua
         [:lua    :remote-port]        'tahto.runtime.basic.impl.process-lua
         [:lua    :remote-ws]          'tahto.runtime.basic.impl.process-lua
         [:lua    :neovim]             'tahto.runtime.neovim
         [:lua    :neovim.instance]    'tahto.runtime.neovim
         [:lua    :verify]             'tahto.runtime.basic.impl.process-lua
         
         [:js     :oneshot]            'tahto.runtime.basic.impl.process-js
         [:js     :basic]              'tahto.runtime.basic.impl.process-js
         [:js     :interactive]        'tahto.runtime.basic.impl.process-js
         [:js     :websocket]          'tahto.runtime.basic.impl.process-js
         [:js     :playground]         'tahto.runtime.js-playground
         [:js     :javafx]             'tahto.runtime.javafx
         [:js     :graal]              'tahto.runtime.graal
         [:js     :browser]            'tahto.runtime.browser
         [:js     :remote-port]        'tahto.runtime.basic.impl.process-js
         [:js     :remote-ws]          'tahto.runtime.basic.impl.process-js
         [:js     :verify]             'tahto.runtime.basic.impl.process-js
         [:js     :chromedriver]        'tahto.runtime.chromedriver
         [:js     :chromedriver.instance] 'tahto.runtime.chromedriver
         [:js     :vscode]             'tahto.runtime.vscode
         [:js     :vscode.instance]    'tahto.runtime.vscode
         
         [:python :oneshot]            'tahto.runtime.basic.impl.process-python
         [:python :basic]              'tahto.runtime.basic.impl.process-python
         [:python :interactive]        'tahto.runtime.basic.impl.process-python
         [:python :websocket]          'tahto.runtime.basic.impl.process-python
         [:python :graal]              'tahto.runtime.graal
         [:python :jep]                'tahto.runtime.jep
         [:python :libpython]          'tahto.runtime.libpython
         [:python :blender]            'tahto.runtime.blender
         [:python :blender.instance]   'tahto.runtime.blender
         [:python :gimp]               'tahto.runtime.gimp
         [:python :unreal]             'tahto.runtime.unreal.impl
         [:python :unreal.instance]    'tahto.runtime.unreal.impl
         [:python :remote-port]        'tahto.runtime.basic.impl.process-python
         [:python :remote-ws]          'tahto.runtime.basic.impl.process-python
         [:python :verify]             'tahto.runtime.basic.impl.process-python

         [:scheme :oneshot]            'tahto.runtime.basic.impl.process-scheme
         [:scheme :basic]              'tahto.runtime.basic.impl.process-scheme
         [:scheme :verify]             'tahto.runtime.basic.impl.process-scheme

         [:elisp  :oneshot]            'tahto.runtime.basic.impl.process-elisp
         [:elisp  :basic]              'tahto.runtime.basic.impl.process-elisp
         [:elisp  :verify]             'tahto.runtime.basic.impl.process-elisp

         [:ruby   :oneshot]            'tahto.runtime.basic.impl.process-ruby
         [:ruby   :basic]              'tahto.runtime.basic.impl.process-ruby
         [:ruby   :verify]             'tahto.runtime.basic.impl.process-ruby

         [:gdscript :twostep]          'tahto.runtime.basic.impl.process-gdscript
         [:gdscript :godot]            'tahto.runtime.godot
         [:gdscript :godot.instance]   'tahto.runtime.godot
         [:gdscript :verify]           'tahto.runtime.basic.impl.process-gdscript

         [:glsl   :oneshot]            'tahto.runtime.basic.impl.process-glsl
         [:glsl   :verify]             'tahto.runtime.basic.impl.process-glsl

         [:perl   :oneshot]            'tahto.runtime.basic.impl-annex.process-perl
         [:perl   :basic]              'tahto.runtime.basic.impl-annex.process-perl
         [:perl   :verify]             'tahto.runtime.basic.impl-annex.process-perl

         [:php    :oneshot]            'tahto.runtime.basic.impl-annex.process-php
         [:php    :basic]              'tahto.runtime.basic.impl-annex.process-php
         [:php    :verify]             'tahto.runtime.basic.impl-annex.process-php
         
         [:r      :oneshot]            'tahto.runtime.basic.impl-annex.process-r
         [:r      :basic]              'tahto.runtime.basic.impl-annex.process-r
         [:r      :verify]             'tahto.runtime.basic.impl-annex.process-r

         [:matlab :oneshot]            'tahto.runtime.basic.impl-annex.process-matlab
         [:matlab :basic]              'tahto.runtime.basic.impl-annex.process-matlab
         [:matlab :verify]             'tahto.runtime.basic.impl-annex.process-matlab

         [:julia  :oneshot]            'tahto.runtime.basic.impl-annex.process-julia
         [:julia  :basic]              'tahto.runtime.basic.impl-annex.process-julia
         [:julia  :verify]             'tahto.runtime.basic.impl-annex.process-julia

         [:erlang :oneshot]            'tahto.runtime.basic.impl-annex.process-erlang
         [:erlang :basic]              'tahto.runtime.basic.impl-annex.process-erlang
         [:erlang :verify]             'tahto.runtime.basic.impl-annex.process-erlang
         
         [:haskell :twostep]           'tahto.runtime.basic.impl-annex.process-haskell
         [:lean    :twostep]           'tahto.runtime.basic.impl-annex.process-lean
         [:ocaml   :twostep]           'tahto.runtime.basic.impl-annex.process-ocaml
         [:haskell :verify]            'tahto.runtime.basic.impl-annex.process-haskell
         [:lean    :verify]            'tahto.runtime.basic.impl-annex.process-lean
         [:ocaml   :verify]            'tahto.runtime.basic.impl-annex.process-ocaml
         
         [:rust   :twostep]            'tahto.runtime.basic.impl-annex.process-rust
         [:rust   :verify]             'tahto.runtime.basic.impl-annex.process-rust
         
         [:c      :jocl]               'tahto.runtime.jocl
         [:c      :oneshot]            'tahto.runtime.basic.impl.process-c
         [:c      :twostep]            'tahto.runtime.basic.impl.process-c
         [:c      :verify]             'tahto.runtime.basic.impl.process-c

         [:circom :twostep]            'tahto.runtime.basic.impl-annex.process-circom
         [:circom :verify]             'tahto.runtime.basic.impl-annex.process-circom

         [:verilog :twostep]           'tahto.runtime.basic.impl.process-verilog
         [:verilog :verify]            'tahto.runtime.basic.impl.process-verilog

         [:dart   :twostep]            'tahto.runtime.basic.impl.process-dart
         [:dart   :verify]             'tahto.runtime.basic.impl.process-dart
         [:go     :twostep]            'tahto.runtime.basic.impl.process-go
         [:go     :verify]             'tahto.runtime.basic.impl.process-go
		 
         [:haxe   :haxe]               'tahto.runtime.haxe

         [:xtalk  :oneshot]            'tahto.runtime.basic.impl.process-xtalk
         [:xtalk  :verify]             'tahto.runtime.basic.impl.process-xtalk}))

(def +book-registry+
  (atom {[:xtalk    :default]          {:ns 'tahto.model.spec-xtalk
                                        :book '+book+}

         [:bash     :default]          {:ns 'tahto.model.spec-bash
                                        :book '+book+
                                        :parent :xtalk}
         [:c        :default]          {:ns 'tahto.model.spec-c
                                        :book '+book+
                                        :parent :xtalk}
         [:dart     :default]          {:ns 'tahto.model.spec-dart
                                        :book '+book+
                                        :parent :xtalk}
         [:glsl     :default]          {:ns 'tahto.model.spec-glsl
                                        :book '+book+
                                        :parent :xtalk}
         [:go       :default]          {:ns 'tahto.model.spec-go
                                        :book '+book+
                                        :parent :xtalk}
         [:js       :default]          {:ns 'tahto.model.spec-js
                                        :book '+book+
                                        :parent :xtalk}
         [:llvm     :default]          {:ns 'tahto.model.spec-llvm
                                        :book '+book+
                                        :parent :xtalk}
         [:lua      :default]          {:ns 'tahto.model.spec-lua
                                        :book '+book+
                                        :parent :xtalk}
         [:lua.redis :default]         {:ns 'tahto.model.spec-lua.variant-redis
                                        :book '+book+
                                        :parent :lua}
         [:lua.nginx :default]         {:ns 'tahto.model.spec-lua.variant-nginx
                                        :book '+book+
                                        :parent :lua}
         [:python   :default]          {:ns 'tahto.model.spec-python
                                        :book '+book+
                                        :parent :xtalk}
         [:elisp    :default]          {:ns 'tahto.model.spec-elisp
                                        :book '+book+
                                        :parent :xtalk}
         [:gdscript :default]          {:ns 'tahto.model.spec-gdscript
                                        :book '+book+
                                        :parent :xtalk}
          [:scheme   :default]          {:ns 'tahto.model.spec-scheme
                                         :book '+book+
                                         :parent :xtalk}
          [:sql      :default]          {:ns 'tahto.model.spec-sql
                                         :book '+book+}
          [:oracle   :default]          {:ns 'tahto.model.sql.spec-oracle
                                         :book '+book+}
          
          [:postgres :default]          {:ns 'tahto.model.spec-postgres
                                         :book '+book+}
         [:solidity :default]          {:ns 'tahto.model.spec-solidity
                                        :book '+book+}

         [:circom   :default]          {:ns 'tahto.model.annex.spec-circom
                                        :book '+book+
                                        :parent :xtalk}
         [:erlang   :default]          {:ns 'tahto.model.annex.spec-erlang
                                        :book '+book+
                                        :parent :xtalk}
         [:fortran  :default]          {:ns 'tahto.model.annex.spec-fortran
                                        :book '+book+
                                        :parent :xtalk}
         [:haskell  :default]          {:ns 'tahto.model.annex.spec-haskell
                                        :book '+book+
                                        :parent :xtalk}
         [:lean     :default]          {:ns 'tahto.model.annex.spec-lean
                                        :book '+book+
                                        :parent :xtalk}
         [:ocaml    :default]          {:ns 'tahto.model.annex.spec-ocaml
                                        :book '+book+
                                        :parent :xtalk}
         [:jq       :default]          {:ns 'tahto.model.annex.spec-jq
                                        :book '+book+
                                        :parent :xtalk}
         [:julia    :default]          {:ns 'tahto.model.annex.spec-julia
                                        :book '+book+
                                        :parent :xtalk}
         [:perl     :default]          {:ns 'tahto.model.annex.spec-perl
                                        :book '+book+
                                        :parent :xtalk}
         [:php      :default]          {:ns 'tahto.model.annex.spec-php
                                        :book '+book+
                                        :parent :xtalk}
         [:r        :default]          {:ns 'tahto.model.annex.spec-r
                                        :book '+book+
                                        :parent :xtalk}
         [:matlab   :default]          {:ns 'tahto.model.annex.spec-matlab
                                        :book '+book+
                                        :parent :xtalk}
         [:ruby     :default]          {:ns 'tahto.model.spec-ruby
                                        :book '+book+
                                        :parent :xtalk}
         [:rust     :default]          {:ns 'tahto.model.annex.spec-rust
                                        :book '+book+
                                        :parent :xtalk}
         [:verilog  :default]          {:ns 'tahto.model.annex.spec-verilog
                                        :book '+book+
                                        :parent :xtalk}}))

(defn registry-book-list
  "lists all registered books"
  {:added "4.1"}
  ([] (keys @+book-registry+)))

(defn registry-book-ns
  "gets the namespace for a book registry entry"
  {:added "4.1"}
  ([lang] (registry-book-ns lang :default))
  ([lang key]
   (:ns (get @+book-registry+ [lang key]))))

(defn registry-book-info
  "gets the full book registry entry"
  {:added "4.1"}
  ([lang] (registry-book-info lang :default))
  ([lang key]
   (get @+book-registry+ [lang key])))

(defn registry-book
  "loads the book namespace and returns the book"
  {:added "4.1"}
  ([lang] (registry-book lang :default))
  ([lang key]
   (when-let [{:keys [ns book]} (registry-book-info lang key)]
     (clojure.core/require ns)
     (some-> (ns-resolve (the-ns ns) book)
             var-get))))
