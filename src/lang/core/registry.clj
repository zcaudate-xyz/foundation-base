(ns lang.core.registry)

(def +registry+
  (atom {[:postgres :default]          'lang.model.annex.spec-postgres
         [:postgres :jdbc]             'lang.runtime.annex.postgres.base.client
         [:postgres :jdbc.client]      'lang.runtime.annex.postgres.base.client
         
         [:solidity :default]          'lang.runtime.annex.solidity
		 
         [:bash   :oneshot]            'lang.runtime.basic.impl.process-bash
         [:bash   :basic]              'lang.runtime.shell
         [:bash   :remote]             'lang.runtime.shell
         [:bash   :verify]             'lang.runtime.basic.impl.process-bash
         
         [:lua    :oneshot]            'lang.runtime.basic.impl.process-lua
         [:lua    :basic]              'lang.runtime.basic.impl.process-lua
         [:lua    :interactive]        'lang.runtime.basic.impl.process-lua
         [:lua    :websocket]          'lang.runtime.basic.impl.process-lua
         [:lua    :nginx]              'lang.runtime.nginx
         [:lua    :nginx.instance]     'lang.runtime.nginx
         [:lua    :redis]              'lang.runtime.annex.redis
         [:lua.redis :default]         'lang.runtime.annex.redis
         [:lua.redis :redis]           'lang.runtime.annex.redis
         [:lua.nginx :oneshot]         'lang.runtime.basic.impl.process-lua
         [:lua.nginx :basic]           'lang.runtime.basic.impl.process-lua
         [:lua.nginx :interactive]     'lang.runtime.basic.impl.process-lua
         [:lua.nginx :websocket]       'lang.runtime.basic.impl.process-lua
         [:lua.nginx :nginx]           'lang.runtime.nginx
         [:lua.nginx :nginx.instance]  'lang.runtime.nginx
         [:lua.nginx :remote-port]     'lang.runtime.basic.impl.process-lua
         [:lua.nginx :remote-ws]       'lang.runtime.basic.impl.process-lua
         [:lua.nginx :verify]          'lang.runtime.basic.impl.process-lua
         [:lua    :remote-port]        'lang.runtime.basic.impl.process-lua
         [:lua    :remote-ws]          'lang.runtime.basic.impl.process-lua
         [:lua    :neovim]             'lang.runtime.annex.neovim
         [:lua    :neovim.instance]    'lang.runtime.annex.neovim
         [:lua    :verify]             'lang.runtime.basic.impl.process-lua
         
         [:js     :oneshot]            'lang.runtime.basic.impl.process-js
         [:js     :basic]              'lang.runtime.basic.impl.process-js
         [:js     :interactive]        'lang.runtime.basic.impl.process-js
         [:js     :websocket]          'lang.runtime.basic.impl.process-js
         [:js     :playground]         'lang.runtime.js-playground
         [:js     :javafx]             'lang.runtime.javafx
         [:js     :graal]              'lang.runtime.graal
         [:js     :browser]            'lang.runtime.browser
         [:js     :remote-port]        'lang.runtime.basic.impl.process-js
         [:js     :remote-ws]          'lang.runtime.basic.impl.process-js
         [:js     :verify]             'lang.runtime.basic.impl.process-js
         [:js     :chromedriver]        'lang.runtime.chromedriver
         [:js     :chromedriver.instance] 'lang.runtime.chromedriver
         [:js     :vscode]             'lang.runtime.annex.vscode
         [:js     :vscode.instance]    'lang.runtime.annex.vscode
         
         [:python :oneshot]            'lang.runtime.basic.impl.process-python
         [:python :basic]              'lang.runtime.basic.impl.process-python
         [:python :interactive]        'lang.runtime.basic.impl.process-python
         [:python :websocket]          'lang.runtime.basic.impl.process-python
         [:python :graal]              'lang.runtime.graal
         [:python :jep]                'lang.runtime.annex.jep
         [:python :libpython]          'lang.runtime.annex.libpython
         [:python :blender]            'lang.runtime.annex.blender
         [:python :blender.instance]   'lang.runtime.annex.blender
         [:python :gimp]               'lang.runtime.annex.gimp
         [:python :unreal]             'lang.runtime.annex.unreal.impl
         [:python :unreal.instance]    'lang.runtime.annex.unreal.impl
         [:python :remote-port]        'lang.runtime.basic.impl.process-python
         [:python :remote-ws]          'lang.runtime.basic.impl.process-python
         [:python :verify]             'lang.runtime.basic.impl.process-python

         [:scheme :oneshot]            'lang.runtime.annex.basic.impl.process-scheme
         [:scheme :basic]              'lang.runtime.annex.basic.impl.process-scheme
         [:scheme :verify]             'lang.runtime.annex.basic.impl.process-scheme

         [:elisp  :oneshot]            'lang.runtime.annex.basic.impl.process-elisp
         [:elisp  :basic]              'lang.runtime.annex.basic.impl.process-elisp
         [:elisp  :verify]             'lang.runtime.annex.basic.impl.process-elisp

         [:ruby   :oneshot]            'lang.runtime.basic.impl.process-ruby
         [:ruby   :basic]              'lang.runtime.basic.impl.process-ruby
         [:ruby   :verify]             'lang.runtime.basic.impl.process-ruby

         [:gdscript :twostep]          'lang.runtime.annex.basic.impl.process-gdscript
         [:gdscript :godot]            'lang.runtime.annex.godot
         [:gdscript :godot.instance]   'lang.runtime.annex.godot
         [:gdscript :verify]           'lang.runtime.annex.basic.impl.process-gdscript

         [:glsl   :oneshot]            'lang.runtime.basic.impl.process-glsl
         [:glsl   :verify]             'lang.runtime.basic.impl.process-glsl

         [:perl   :oneshot]            'lang.runtime.annex.basic.impl.process-perl
         [:perl   :basic]              'lang.runtime.annex.basic.impl.process-perl
         [:perl   :verify]             'lang.runtime.annex.basic.impl.process-perl

         [:php    :oneshot]            'lang.runtime.annex.basic.impl.process-php
         [:php    :basic]              'lang.runtime.annex.basic.impl.process-php
         [:php    :verify]             'lang.runtime.annex.basic.impl.process-php
         
         [:r      :oneshot]            'lang.runtime.annex.basic.impl.process-r
         [:r      :basic]              'lang.runtime.annex.basic.impl.process-r
         [:r      :verify]             'lang.runtime.annex.basic.impl.process-r

         [:matlab :oneshot]            'lang.runtime.annex.basic.impl.process-matlab
         [:matlab :basic]              'lang.runtime.annex.basic.impl.process-matlab
         [:matlab :verify]             'lang.runtime.annex.basic.impl.process-matlab

         [:julia  :oneshot]            'lang.runtime.annex.basic.impl.process-julia
         [:julia  :basic]              'lang.runtime.annex.basic.impl.process-julia
         [:julia  :verify]             'lang.runtime.annex.basic.impl.process-julia

         [:erlang :oneshot]            'lang.runtime.annex.basic.impl.process-erlang
         [:erlang :basic]              'lang.runtime.annex.basic.impl.process-erlang
         [:erlang :verify]             'lang.runtime.annex.basic.impl.process-erlang
         
         [:haskell :twostep]           'lang.runtime.annex.basic.impl.process-haskell
         [:lean    :twostep]           'lang.runtime.annex.basic.impl.process-lean
         [:ocaml   :twostep]           'lang.runtime.annex.basic.impl.process-ocaml
         [:haskell :verify]            'lang.runtime.annex.basic.impl.process-haskell
         [:lean    :verify]            'lang.runtime.annex.basic.impl.process-lean
         [:ocaml   :verify]            'lang.runtime.annex.basic.impl.process-ocaml
         
         [:rust   :twostep]            'lang.runtime.annex.basic.impl.process-rust
         [:rust   :verify]             'lang.runtime.annex.basic.impl.process-rust
         
         [:c      :jocl]               'lang.runtime.annex.jocl
         [:c      :oneshot]            'lang.runtime.basic.impl.process-c
         [:c      :twostep]            'lang.runtime.basic.impl.process-c
         [:c      :verify]             'lang.runtime.basic.impl.process-c

         [:circom :twostep]            'lang.runtime.annex.basic.impl.process-circom
         [:circom :verify]             'lang.runtime.annex.basic.impl.process-circom

         [:verilog :twostep]           'lang.runtime.annex.basic.impl.process-verilog
         [:verilog :verify]            'lang.runtime.annex.basic.impl.process-verilog

         [:dart   :twostep]            'lang.runtime.basic.impl.process-dart
         [:dart   :verify]             'lang.runtime.basic.impl.process-dart
         [:go     :twostep]            'lang.runtime.annex.basic.impl.process-go
         [:go     :verify]             'lang.runtime.annex.basic.impl.process-go
		 
         [:haxe   :haxe]               'lang.runtime.annex.haxe

         [:xtalk  :oneshot]            'lang.runtime.basic.impl.process-xtalk
         [:xtalk  :verify]             'lang.runtime.basic.impl.process-xtalk}))

(def +book-registry+
  (atom {[:xtalk    :default]          {:ns 'lang.model.builtin.spec-xtalk
                                        :book '+book+}

         [:bash     :default]          {:ns 'lang.model.builtin.spec-bash
                                        :book '+book+
                                        :parent :xtalk}
         [:c        :default]          {:ns 'lang.model.builtin.spec-c
                                        :book '+book+
                                        :parent :xtalk}
         [:dart     :default]          {:ns 'lang.model.builtin.spec-dart
                                        :book '+book+
                                        :parent :xtalk}
         [:glsl     :default]          {:ns 'lang.model.builtin.spec-glsl
                                        :book '+book+
                                        :parent :xtalk}
         [:go       :default]          {:ns 'lang.model.builtin.spec-go
                                        :book '+book+
                                        :parent :xtalk}
         [:js       :default]          {:ns 'lang.model.builtin.spec-js
                                        :book '+book+
                                        :parent :xtalk}
         [:llvm     :default]          {:ns 'lang.model.annex.spec-llvm
                                        :book '+book+
                                        :parent :xtalk}
         [:lua      :default]          {:ns 'lang.model.builtin.spec-lua
                                        :book '+book+
                                        :parent :xtalk}
         [:lua.redis :default]         {:ns 'lang.model.builtin.spec-lua.variant-redis
                                        :book '+book+
                                        :parent :lua}
         [:lua.nginx :default]         {:ns 'lang.model.builtin.spec-lua.variant-nginx
                                        :book '+book+
                                        :parent :lua}
         [:python   :default]          {:ns 'lang.model.builtin.spec-python
                                        :book '+book+
                                        :parent :xtalk}
         [:elisp    :default]          {:ns 'lang.model.annex.spec-elisp
                                        :book '+book+
                                        :parent :xtalk}
         [:gdscript :default]          {:ns 'lang.model.annex.spec-gdscript
                                        :book '+book+
                                        :parent :xtalk}
          [:scheme   :default]          {:ns 'lang.model.annex.spec-scheme
                                         :book '+book+
                                         :parent :xtalk}
          [:sql      :default]          {:ns 'lang.model.annex.spec-sql
                                         :book '+book+}
          [:oracle   :default]          {:ns 'lang.model.annex.sql.spec-oracle
                                         :book '+book+}
          
          [:postgres :default]          {:ns 'lang.model.annex.spec-postgres
                                         :book '+book+}
         [:solidity :default]          {:ns 'lang.model.annex.spec-solidity
                                        :book '+book+}

         [:circom   :default]          {:ns 'lang.model.annex.spec-circom
                                        :book '+book+
                                        :parent :xtalk}
         [:erlang   :default]          {:ns 'lang.model.annex.spec-erlang
                                        :book '+book+
                                        :parent :xtalk}
         [:fortran  :default]          {:ns 'lang.model.annex.spec-fortran
                                        :book '+book+
                                        :parent :xtalk}
         [:haskell  :default]          {:ns 'lang.model.annex.spec-haskell
                                        :book '+book+
                                        :parent :xtalk}
         [:lean     :default]          {:ns 'lang.model.annex.spec-lean
                                        :book '+book+
                                        :parent :xtalk}
         [:ocaml    :default]          {:ns 'lang.model.annex.spec-ocaml
                                        :book '+book+
                                        :parent :xtalk}
         [:jq       :default]          {:ns 'lang.model.annex.spec-jq
                                        :book '+book+
                                        :parent :xtalk}
         [:julia    :default]          {:ns 'lang.model.annex.spec-julia
                                        :book '+book+
                                        :parent :xtalk}
         [:perl     :default]          {:ns 'lang.model.annex.spec-perl
                                        :book '+book+
                                        :parent :xtalk}
         [:php      :default]          {:ns 'lang.model.annex.spec-php
                                        :book '+book+
                                        :parent :xtalk}
         [:r        :default]          {:ns 'lang.model.annex.spec-r
                                        :book '+book+
                                        :parent :xtalk}
         [:matlab   :default]          {:ns 'lang.model.annex.spec-matlab
                                        :book '+book+
                                        :parent :xtalk}
         [:ruby     :default]          {:ns 'lang.model.annex.spec-ruby
                                        :book '+book+
                                        :parent :xtalk}
         [:rust     :default]          {:ns 'lang.model.annex.spec-rust
                                        :book '+book+
                                        :parent :xtalk}
         [:verilog  :default]          {:ns 'lang.model.annex.spec-verilog
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
