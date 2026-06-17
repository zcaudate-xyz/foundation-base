(ns hara.lang.registry)

(def +registry+
  (atom {[:postgres :default]          'hara.model.spec-postgres
         [:postgres :jdbc]             'hara.runtime.postgres.base.client
         [:postgres :jdbc.client]      'hara.runtime.postgres.base.client
         
         [:solidity :default]          'hara.runtime.solidity
		 
         [:bash   :oneshot]            'hara.runtime.basic.impl.process-bash
         [:bash   :basic]              'hara.runtime.shell
         [:bash   :remote]             'hara.runtime.shell
         [:bash   :verify]             'hara.runtime.basic.impl.process-bash
         
         [:lua    :oneshot]            'hara.runtime.basic.impl.process-lua
         [:lua    :basic]              'hara.runtime.basic.impl.process-lua
         [:lua    :interactive]        'hara.runtime.basic.impl.process-lua
         [:lua    :websocket]          'hara.runtime.basic.impl.process-lua
         [:lua    :nginx]              'hara.runtime.nginx
         [:lua    :nginx.instance]     'hara.runtime.nginx
         [:lua    :redis]              'hara.runtime.redis
         [:lua.redis :default]         'hara.runtime.redis
         [:lua.redis :redis]           'hara.runtime.redis
         [:lua.nginx :oneshot]         'hara.runtime.basic.impl.process-lua
         [:lua.nginx :basic]           'hara.runtime.basic.impl.process-lua
         [:lua.nginx :interactive]     'hara.runtime.basic.impl.process-lua
         [:lua.nginx :websocket]       'hara.runtime.basic.impl.process-lua
         [:lua.nginx :nginx]           'hara.runtime.nginx
         [:lua.nginx :nginx.instance]  'hara.runtime.nginx
         [:lua.nginx :remote-port]     'hara.runtime.basic.impl.process-lua
         [:lua.nginx :remote-ws]       'hara.runtime.basic.impl.process-lua
         [:lua.nginx :verify]          'hara.runtime.basic.impl.process-lua
         [:lua    :remote-port]        'hara.runtime.basic.impl.process-lua
         [:lua    :remote-ws]          'hara.runtime.basic.impl.process-lua
         [:lua    :neovim]             'hara.runtime.neovim
         [:lua    :neovim.instance]    'hara.runtime.neovim
         [:lua    :verify]             'hara.runtime.basic.impl.process-lua
         
         [:js     :oneshot]            'hara.runtime.basic.impl.process-js
         [:js     :basic]              'hara.runtime.basic.impl.process-js
         [:js     :interactive]        'hara.runtime.basic.impl.process-js
         [:js     :websocket]          'hara.runtime.basic.impl.process-js
         [:js     :javafx]             'hara.runtime.javafx
         [:js     :graal]              'hara.runtime.graal
         [:js     :browser]            'hara.runtime.browser
         [:js     :remote-port]        'hara.runtime.basic.impl.process-js
         [:js     :remote-ws]          'hara.runtime.basic.impl.process-js
         [:js     :verify]             'hara.runtime.basic.impl.process-js
         [:js     :chromedriver]        'hara.runtime.chromedriver
         [:js     :chromedriver.instance] 'hara.runtime.chromedriver
         [:js     :vscode]             'hara.runtime.vscode
         [:js     :vscode.instance]    'hara.runtime.vscode
         
         [:python :oneshot]            'hara.runtime.basic.impl.process-python
         [:python :basic]              'hara.runtime.basic.impl.process-python
         [:python :interactive]        'hara.runtime.basic.impl.process-python
         [:python :websocket]          'hara.runtime.basic.impl.process-python
         [:python :graal]              'hara.runtime.graal
         [:python :jep]                'hara.runtime.jep
         [:python :libpython]          'hara.runtime.libpython
         [:python :blender]            'hara.runtime.blender
         [:python :blender.instance]   'hara.runtime.blender
         [:python :gimp]               'hara.runtime.gimp
         [:python :unreal]             'hara.runtime.unreal.impl
         [:python :unreal.instance]    'hara.runtime.unreal.impl
         [:python :remote-port]        'hara.runtime.basic.impl.process-python
         [:python :remote-ws]          'hara.runtime.basic.impl.process-python
         [:python :verify]             'hara.runtime.basic.impl.process-python

         [:scheme :oneshot]            'hara.runtime.basic.impl.process-scheme
         [:scheme :basic]              'hara.runtime.basic.impl.process-scheme
         [:scheme :verify]             'hara.runtime.basic.impl.process-scheme

         [:elisp  :oneshot]            'hara.runtime.basic.impl.process-elisp
         [:elisp  :basic]              'hara.runtime.basic.impl.process-elisp
         [:elisp  :verify]             'hara.runtime.basic.impl.process-elisp

         [:ruby   :oneshot]            'hara.runtime.basic.impl.process-ruby
         [:ruby   :basic]              'hara.runtime.basic.impl.process-ruby
         [:ruby   :verify]             'hara.runtime.basic.impl.process-ruby

         [:gdscript :twostep]          'hara.runtime.basic.impl.process-gdscript
         [:gdscript :godot]            'hara.runtime.godot
         [:gdscript :godot.instance]   'hara.runtime.godot
         [:gdscript :verify]           'hara.runtime.basic.impl.process-gdscript

         [:glsl   :oneshot]            'hara.runtime.basic.impl.process-glsl
         [:glsl   :verify]             'hara.runtime.basic.impl.process-glsl

         [:perl   :oneshot]            'hara.runtime.basic.impl-annex.process-perl
         [:perl   :basic]              'hara.runtime.basic.impl-annex.process-perl
         [:perl   :verify]             'hara.runtime.basic.impl-annex.process-perl

         [:php    :oneshot]            'hara.runtime.basic.impl-annex.process-php
         [:php    :basic]              'hara.runtime.basic.impl-annex.process-php
         [:php    :verify]             'hara.runtime.basic.impl-annex.process-php
         
         [:r      :oneshot]            'hara.runtime.basic.impl-annex.process-r
         [:r      :basic]              'hara.runtime.basic.impl-annex.process-r
         [:r      :verify]             'hara.runtime.basic.impl-annex.process-r

         [:octave :oneshot]            'hara.runtime.basic.impl-annex.process-octave
         [:octave :basic]              'hara.runtime.basic.impl-annex.process-octave
         [:octave :verify]             'hara.runtime.basic.impl-annex.process-octave

         [:julia  :oneshot]            'hara.runtime.basic.impl-annex.process-julia
         [:julia  :basic]              'hara.runtime.basic.impl-annex.process-julia
         [:julia  :verify]             'hara.runtime.basic.impl-annex.process-julia

         [:erlang :oneshot]            'hara.runtime.basic.impl-annex.process-erlang
         [:erlang :basic]              'hara.runtime.basic.impl-annex.process-erlang
         [:erlang :verify]             'hara.runtime.basic.impl-annex.process-erlang
         
         [:haskell :twostep]           'hara.runtime.basic.impl-annex.process-haskell
         [:lean    :twostep]           'hara.runtime.basic.impl-annex.process-lean
         [:ocaml   :twostep]           'hara.runtime.basic.impl-annex.process-ocaml
         [:haskell :verify]            'hara.runtime.basic.impl-annex.process-haskell
         [:lean    :verify]            'hara.runtime.basic.impl-annex.process-lean
         [:ocaml   :verify]            'hara.runtime.basic.impl-annex.process-ocaml
         
         [:rust   :twostep]            'hara.runtime.basic.impl-annex.process-rust
         [:rust   :verify]             'hara.runtime.basic.impl-annex.process-rust
         
         [:c      :jocl]               'hara.runtime.jocl
         [:c      :oneshot]            'hara.runtime.basic.impl.process-c
         [:c      :twostep]            'hara.runtime.basic.impl.process-c
         [:c      :verify]             'hara.runtime.basic.impl.process-c

         [:circom :twostep]            'hara.runtime.basic.impl-annex.process-circom
         [:circom :verify]             'hara.runtime.basic.impl-annex.process-circom

         [:verilog :twostep]           'hara.runtime.basic.impl.process-verilog
         [:verilog :verify]            'hara.runtime.basic.impl.process-verilog

         [:dart   :twostep]            'hara.runtime.basic.impl.process-dart
         [:dart   :verify]             'hara.runtime.basic.impl.process-dart
         [:go     :twostep]            'hara.runtime.basic.impl.process-go
         [:go     :verify]             'hara.runtime.basic.impl.process-go
		 
         [:haxe   :haxe]               'hara.runtime.haxe

         [:xtalk  :oneshot]            'hara.runtime.basic.impl.process-xtalk
         [:xtalk  :verify]             'hara.runtime.basic.impl.process-xtalk}))

(def +book-registry+
  (atom {[:xtalk    :default]          {:ns 'hara.model.spec-xtalk
                                        :book '+book+}

         [:bash     :default]          {:ns 'hara.model.spec-bash
                                        :book '+book+
                                        :parent :xtalk}
         [:c        :default]          {:ns 'hara.model.spec-c
                                        :book '+book+
                                        :parent :xtalk}
         [:dart     :default]          {:ns 'hara.model.spec-dart
                                        :book '+book+
                                        :parent :xtalk}
         [:glsl     :default]          {:ns 'hara.model.spec-glsl
                                        :book '+book+
                                        :parent :xtalk}
         [:go       :default]          {:ns 'hara.model.spec-go
                                        :book '+book+
                                        :parent :xtalk}
         [:js       :default]          {:ns 'hara.model.spec-js
                                        :book '+book+
                                        :parent :xtalk}
         [:llvm     :default]          {:ns 'hara.model.spec-llvm
                                        :book '+book+
                                        :parent :xtalk}
         [:lua      :default]          {:ns 'hara.model.spec-lua
                                        :book '+book+
                                        :parent :xtalk}
         [:lua.redis :default]         {:ns 'hara.model.spec-lua.variant-redis
                                        :book '+book+
                                        :parent :lua}
         [:lua.nginx :default]         {:ns 'hara.model.spec-lua.variant-nginx
                                        :book '+book+
                                        :parent :lua}
         [:python   :default]          {:ns 'hara.model.spec-python
                                        :book '+book+
                                        :parent :xtalk}
         [:elisp    :default]          {:ns 'hara.model.spec-elisp
                                        :book '+book+
                                        :parent :xtalk}
         [:gdscript :default]          {:ns 'hara.model.spec-gdscript
                                        :book '+book+
                                        :parent :xtalk}
          [:scheme   :default]          {:ns 'hara.model.spec-scheme
                                         :book '+book+
                                         :parent :xtalk}
          [:sql      :default]          {:ns 'hara.model.spec-sql
                                         :book '+book+}
          [:oracle   :default]          {:ns 'hara.model.sql.spec-oracle
                                         :book '+book+}
          
          [:postgres :default]          {:ns 'hara.model.spec-postgres
                                         :book '+book+}
         [:solidity :default]          {:ns 'hara.model.spec-solidity
                                        :book '+book+}

         [:circom   :default]          {:ns 'hara.model.annex.spec-circom
                                        :book '+book+
                                        :parent :xtalk}
         [:erlang   :default]          {:ns 'hara.model.annex.spec-erlang
                                        :book '+book+
                                        :parent :xtalk}
         [:fortran  :default]          {:ns 'hara.model.annex.spec-fortran
                                        :book '+book+
                                        :parent :xtalk}
         [:haskell  :default]          {:ns 'hara.model.annex.spec-haskell
                                        :book '+book+
                                        :parent :xtalk}
         [:lean     :default]          {:ns 'hara.model.annex.spec-lean
                                        :book '+book+
                                        :parent :xtalk}
         [:ocaml    :default]          {:ns 'hara.model.annex.spec-ocaml
                                        :book '+book+
                                        :parent :xtalk}
         [:jq       :default]          {:ns 'hara.model.annex.spec-jq
                                        :book '+book+
                                        :parent :xtalk}
         [:julia    :default]          {:ns 'hara.model.annex.spec-julia
                                        :book '+book+
                                        :parent :xtalk}
         [:perl     :default]          {:ns 'hara.model.annex.spec-perl
                                        :book '+book+
                                        :parent :xtalk}
         [:php      :default]          {:ns 'hara.model.annex.spec-php
                                        :book '+book+
                                        :parent :xtalk}
         [:r        :default]          {:ns 'hara.model.annex.spec-r
                                        :book '+book+
                                        :parent :xtalk}
         [:octave   :default]          {:ns 'hara.model.annex.spec-octave
                                        :book '+book+
                                        :parent :xtalk}
         [:ruby     :default]          {:ns 'hara.model.spec-ruby
                                        :book '+book+
                                        :parent :xtalk}
         [:rust     :default]          {:ns 'hara.model.annex.spec-rust
                                        :book '+book+
                                        :parent :xtalk}
         [:verilog  :default]          {:ns 'hara.model.annex.spec-verilog
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
