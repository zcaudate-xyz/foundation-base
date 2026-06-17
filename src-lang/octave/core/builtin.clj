(ns octave.core.builtin
  "Generated outline of GNU Octave 8.4 builtins from /usr/share/octave/8.4.0/etc/doc-cache."
  (:require [hara.lang :as l]
            [std.lib.foundation :as f]))

(l/script :octave
  octave.core
  {:macro-only true})

(def +containers+
  "Octave builtins from the `+containers` category."
  [
    "containers.Map"
  ])

(def +matlab+
  "Octave builtins from the `+matlab` category."
  [
    "matlab.lang.makeUniqueStrings" "matlab.lang.makeValidName" "matlab.net.base64decode"
    "matlab.net.base64encode"
  ])

(def +ftp+
  "Octave builtins from the `@ftp` category."
  [
    "@ftp/ascii" "@ftp/binary" "@ftp/cd" "@ftp/close" "@ftp/delete" "@ftp/dir" "@ftp/ftp"
    "@ftp/loadobj" "@ftp/mget" "@ftp/mkdir" "@ftp/mput" "@ftp/rename" "@ftp/rmdir"
    "@ftp/saveobj"
  ])

(def +audio+
  "Octave builtins from the `audio` category."
  [
    "@audioplayer/audioplayer" "@audioplayer/disp" "@audioplayer/get"
    "@audioplayer/isplaying" "@audioplayer/pause" "@audioplayer/play"
    "@audioplayer/playblocking" "@audioplayer/resume" "@audioplayer/set"
    "@audioplayer/stop" "@audioplayer/subsasgn" "@audioplayer/subsref"
    "@audiorecorder/audiorecorder" "@audiorecorder/disp" "@audiorecorder/get"
    "@audiorecorder/getaudiodata" "@audiorecorder/getplayer" "@audiorecorder/isrecording"
    "@audiorecorder/pause" "@audiorecorder/play" "@audiorecorder/record"
    "@audiorecorder/recordblocking" "@audiorecorder/resume" "@audiorecorder/set"
    "@audiorecorder/stop" "@audiorecorder/subsasgn" "@audiorecorder/subsref" "lin2mu"
    "mu2lin" "record" "sound" "soundsc"
  ])

(def +core+
  "Octave builtins from the `core` category."
  [
    "!" "!=" "\"" "#" "#{" "#}" "%" "%{" "%}" "&" "&&" "'" "(" ")" "*" "**" "+" "++" ","
    "-" "-" ".'" ".*" ".**" "..." "./" ".\\" ".^" "/" ":" ";" "<" "<=" "=" "==" ">" ">="
    "@" "EDITOR" "EXEC_PATH" "F_DUPFD" "F_GETFD" "F_GETFL" "F_SETFD" "F_SETFL" "I"
    "IMAGE_PATH" "Inf" "NA" "NaN" "OCTAVE_EXEC_HOME" "OCTAVE_HOME" "OCTAVE_VERSION"
    "O_APPEND" "O_ASYNC" "O_CREAT" "O_EXCL" "O_NONBLOCK" "O_RDONLY" "O_RDWR" "O_SYNC"
    "O_TRUNC" "O_WRONLY" "PAGER" "PAGER_FLAGS" "PS1" "PS2" "PS4" "P_tmpdir" "SEEK_CUR"
    "SEEK_END" "SEEK_SET" "SIG" "S_ISBLK" "S_ISCHR" "S_ISDIR" "S_ISFIFO" "S_ISLNK"
    "S_ISREG" "S_ISSOCK" "WCONTINUE" "WCOREDUMP" "WEXITSTATUS" "WIFCONTINUED" "WIFEXITED"
    "WIFSIGNALED" "WIFSTOPPED" "WNOHANG" "WSTOPSIG" "WTERMSIG" "WUNTRACED" "[" "\\" "]"
    "^" "abs" "acos" "acosh" "add_input_event_hook" "addlistener" "addpath" "addproperty"
    "airy" "all" "amd" "and" "angle" "ans" "any" "arg" "argv" "arrayfun" "asin" "asinh"
    "assignin" "atan" "atan2" "atanh" "atexit" "auto_repeat_debug_command" "autoload"
    "available_graphics_toolkits" "balance" "base64_decode" "base64_encode"
    "beep_on_error" "besselh" "besseli" "besselj" "besselk" "bessely" "bitand" "bitor"
    "bitpack" "bitshift" "bitunpack" "bitxor" "blkmm" "break" "bsxfun"
    "built_in_docstrings_file" "builtin" "canonicalize_file_name" "case" "cat" "catch"
    "cbrt" "ccolamd" "cd" "ceil" "cell" "cell2struct" "cellfun" "cellindexmat"
    "cellslices" "cellstr" "char" "chol" "chol2inv" "choldelete" "cholinsert" "cholinv"
    "cholshift" "cholupdate" "class" "classdef" "clc" "clear" "cmdline_options" "colamd"
    "colloc" "colon" "columns" "command_line_path" "commandhistory" "commandwindow"
    "completion_append_char" "completion_matches" "complex" "confirm_recursive_rmdir"
    "conj" "continue" "conv2" "convn" "cos" "cosh" "cputime" "crash_dumps_octave_core"
    "csymamd" "ctranspose" "cummax" "cummin" "cumprod" "cumsum" "daspk" "daspk_options"
    "dasrt" "dasrt_options" "dassl" "dassl_options" "dawson" "dbclear" "dbcont" "dbdown"
    "dblist" "dbquit" "dbstack" "dbstatus" "dbstep" "dbstop" "dbtype" "dbup" "dbwhere"
    "debug_java" "debug_on_error" "debug_on_interrupt" "debug_on_warning" "dellistener"
    "desktop" "det" "diag" "diary" "diff" "dir_encoding" "dir_in_loadpath" "disp"
    "display" "dlmread" "dmperm" "do" "do_string_escapes" "doc_cache_file" "dot" "double"
    "drawnow" "dup2" "e" "echo" "edit_history" "eig" "ellipj" "else" "elseif" "end"
    "end_try_catch" "end_unwind_protect" "endclassdef" "endenumeration" "endevents"
    "endfor" "endfunction" "endgrent" "endif" "endmethods" "endparfor" "endproperties"
    "endpwent" "endspmd" "endswitch" "endwhile" "enumeration" "eps" "eq" "erf" "erfc"
    "erfcinv" "erfcx" "erfi" "erfinv" "errno" "errno_list" "error" "etree" "eval" "evalc"
    "evalin" "events" "exec" "exist" "exp" "expm1" "eye" "false" "fclear" "fclose" "fcntl"
    "fdisp" "feof" "ferror" "feval" "fflush" "fft" "fft2" "fftn" "fgetl" "fgets"
    "file_in_loadpath" "file_in_path" "filebrowser" "filesep" "filter" "find" "fix"
    "fixed_point_format" "flintmax" "floor" "fopen" "for" "fork" "format" "fprintf"
    "fputs" "fread" "freport" "frewind" "fscanf" "fseek" "fskipl" "ftell" "full"
    "func2str" "function" "functions" "fwrite" "gamma" "gcd" "ge" "genpath" "get"
    "get_help_text" "get_help_text_from_file" "get_home_directory" "getegid" "getenv"
    "geteuid" "getgid" "getgrent" "getgrgid" "getgrnam" "gethostname" "getpgrp" "getpid"
    "getppid" "getpwent" "getpwnam" "getpwuid" "getrusage" "getuid" "givens" "glob"
    "global" "gmtime" "gsvd" "gt" "hash" "have_window_system" "hess" "hex2num" "history"
    "history_control" "history_file" "history_save" "history_size"
    "history_timestamp_format_string" "horzcat" "hypot" "if" "ifft" "ifft2" "ifftn"
    "ignore_function_time_stamp" "imag" "ind2sub" "inferiorto" "info_file" "info_program"
    "input" "int16" "int32" "int64" "int8" "intmax" "intmin" "inv" "ipermute"
    "is_absolute_filename" "is_dq_string" "is_function_handle"
    "is_rooted_relative_filename" "is_same_file" "is_sq_string" "isa" "isalnum" "isalpha"
    "isargout" "isascii" "iscell" "iscellstr" "ischar" "iscntrl" "iscolumn" "iscomplex"
    "isdebugmode" "isdigit" "isempty" "isfield" "isfinite" "isfloat" "isglobal" "isgraph"
    "isguirunning" "ishghandle" "isieee" "isindex" "isinf" "isinteger" "isjava"
    "iskeyword" "islogical" "islower" "ismatrix" "isna" "isnan" "isnull" "isnumeric"
    "isobject" "isprint" "ispunct" "isreal" "isrow" "isscalar" "issorted" "isspace"
    "issparse" "issquare" "isstruct" "isstudent" "isupper" "isvarname" "isvector"
    "isxdigit" "javaMethod" "javaObject" "java_matrix_autoconversion"
    "java_unsigned_autoconversion" "jsondecode" "jsonencode" "kbhit" "keyboard" "kill"
    "kron" "lasterr" "lasterror" "lastwarn" "ldivide" "le" "length" "lgamma" "link"
    "linspace" "list_in_columns" "load" "loaded_graphics_toolkits" "localfunctions"
    "localtime" "log" "log10" "log1p" "log2" "logical" "lookup" "lsode" "lsode_options"
    "lstat" "lt" "lu" "luupdate" "make_absolute_filename" "makeinfo_program" "mat2cell"
    "matrix_type" "max" "max_recursion_depth" "max_stack_depth" "merge" "metaclass"
    "mfilename" "mgorth" "min" "minus" "mislocked" "missing_component_hook"
    "missing_function_hook" "mkfifo" "mkstemp" "mktime" "mldivide" "mlock" "mod" "more"
    "mpower" "mrdivide" "mtimes" "munlock" "nargin" "nargout" "native_float_format"
    "ndims" "ne" "newline" "nnz" "norm" "not" "nproc" "nth_element" "num2cell" "num2hex"
    "numel" "numfields" "nzmax" "octave_core_file_limit" "octave_core_file_name"
    "octave_core_file_options" "onCleanup" "ones" "openvar" "optimize_diagonal_matrix"
    "optimize_permutation_matrix" "optimize_range" "optimize_subsasgn_calls" "or" "ordqz"
    "ordschur" "otherwise" "output_precision" "page_output_immediately"
    "page_screen_output" "parfor" "path" "pathsep" "pause" "pclose" "permute" "persistent"
    "pi" "pinv" "pipe" "plus" "popen" "popen2" "pow2" "power" "print_empty_dimensions"
    "print_struct_array_contents" "printf" "prod" "program_invocation_name" "program_name"
    "properties" "psi" "puts" "pwd" "qr" "qrdelete" "qrinsert" "qrshift" "qrupdate" "quad"
    "quad_options" "quadcc" "quit" "qz" "rand" "rande" "randg" "randn" "randp" "randperm"
    "rats" "rcond" "rdivide" "readdir" "readline_re_read_init_file"
    "readline_read_init_file" "readlink" "real" "realmax" "realmin" "regexp" "regexpi"
    "regexprep" "register_graphics_toolkit" "rehash" "rem" "remove_input_event_hook"
    "rename" "repelems" "reset" "reshape" "resize" "restoredefaultpath" "rethrow" "return"
    "rmdir" "rmfield" "rmpath" "round" "roundb" "rows" "rsf2csf" "run_history" "save"
    "save_default_options" "save_header_format_string" "save_precision" "scanf" "schur"
    "set" "setenv" "setgrent" "setpwent" "sighup_dumps_octave_core" "sign" "signbit"
    "sigquit_dumps_octave_core" "sigterm_dumps_octave_core" "silent_functions" "sin"
    "single" "sinh" "size" "size_equal" "sizemax" "sizeof" "sort" "source" "spalloc"
    "sparse" "split_long_rows" "spmd" "spparms" "sprank" "sprintf" "sqrt" "sqrtm"
    "squeeze" "sscanf" "stat" "stderr" "stdin" "stdout" "str2double" "str2func" "strcmp"
    "strcmpi" "strfind" "strftime" "string_fill_char" "strncmp" "strncmpi" "strptime"
    "strrep" "struct" "struct2cell" "struct_levels_to_print" "strvcat" "sub2ind"
    "subsasgn" "subsref" "sum" "sumsq" "superiorto" "suppress_verbose_help_message" "svd"
    "svd_driver" "switch" "sylvester" "symamd" "symbfact" "symlink" "symrcm" "system"
    "tan" "tanh" "tempdir" "tempname" "terminal_size" "texi_macros_file" "textscan" "tic"
    "tilde_expand" "time" "times" "tmpfile" "toc" "tolower" "toupper" "transpose" "tril"
    "triu" "true" "try" "tsearch" "typecast" "typeinfo" "uint16" "uint32" "uint64" "uint8"
    "umask" "uminus" "uname" "undo_string_escapes" "unicode_idx" "unlink" "unsetenv"
    "until" "unwind_protect" "unwind_protect_cleanup" "uplus" "urlread" "urlwrite"
    "user_config_dir" "user_data_dir" "varargin" "varargout" "vec" "vertcat" "waitfor"
    "waitpid" "warning" "warranty" "while" "who" "whos" "whos_line_format" "winqueryreg"
    "workspace" "yes_or_no" "zeros" "|" "||" "~" "~="
  ])

(def +deprecated+
  "Octave builtins from the `deprecated` category."
  [
    "disable_diagonal_matrix" "disable_permutation_matrix" "disable_range" "shift"
    "sparse_auto_mutate"
  ])

(def +elfun+
  "Octave builtins from the `elfun` category."
  [
    "acosd" "acot" "acotd" "acoth" "acsc" "acscd" "acsch" "asec" "asecd" "asech" "asind"
    "atan2d" "atand" "cosd" "cospi" "cot" "cotd" "coth" "csc" "cscd" "csch" "sec" "secd"
    "sech" "sind" "sinpi" "tand"
  ])

(def +external+
  "Octave builtins from the `external` category."
  [
    "audiodevinfo" "audioformats" "audioinfo" "audioread" "audiowrite" "bzip2" "convhulln"
    "fftw" "gzip"
  ])

(def +general+
  "Octave builtins from the `general` category."
  [
    "accumarray" "accumdim" "bincoeff" "bitcmp" "bitget" "bitset" "blkdiag" "cart2pol"
    "cart2sph" "cell2mat" "celldisp" "circshift" "common_size" "cplxpair" "cumtrapz"
    "curl" "dblquad" "deal" "deg2rad" "del2" "divergence" "flip" "fliplr" "flipud"
    "gradient" "idivide" "int2str" "integral" "integral2" "integral3" "interp1" "interp2"
    "interp3" "interpft" "interpn" "isequal" "isequaln" "logspace" "nextpow2" "num2str"
    "pagectranspose" "pagetranspose" "pol2cart" "polyarea" "postpad" "prepad" "quad2d"
    "quadgk" "quadl" "quadv" "rad2deg" "randi" "rat" "repelem" "repmat" "rescale" "rng"
    "rot90" "rotdim" "shiftdim" "sortrows" "sph2cart" "structfun" "subsindex" "trapz"
    "triplequad" "xor"
  ])

(def +geometry+
  "Octave builtins from the `geometry` category."
  [
    "convhull" "delaunay" "delaunayn" "dsearch" "dsearchn" "griddata" "griddata3"
    "griddatan" "inpolygon" "rectint" "rotx" "roty" "rotz" "tsearchn" "voronoi" "voronoin"
  ])

(def +gui+
  "Octave builtins from the `gui` category."
  [
    "dialog" "errordlg" "getappdata" "getpixelposition" "guidata" "guihandles" "helpdlg"
    "inputdlg" "isappdata" "listdlg" "listfonts" "movegui" "msgbox" "questdlg" "rmappdata"
    "setappdata" "uibuttongroup" "uicontextmenu" "uicontrol" "uifigure" "uigetdir"
    "uigetfile" "uimenu" "uipanel" "uipushtool" "uiputfile" "uiresume" "uisetfont"
    "uitable" "uitoggletool" "uitoolbar" "uiwait" "waitbar" "waitforbuttonpress" "warndlg"
  ])

(def +help+
  "Octave builtins from the `help` category."
  [
    "bessel" "debug" "doc" "doc_cache_create" "error_ids" "get_first_help_sentence" "help"
    "lookfor" "print_usage" "slash" "type" "warning_ids" "which"
  ])

(def +image+
  "Octave builtins from the `image` category."
  [
    "autumn" "bone" "brighten" "cmpermute" "cmunique" "colorcube" "colormap" "contrast"
    "cool" "copper" "cubehelix" "flag" "frame2im" "getframe" "gray" "gray2ind" "hot" "hsv"
    "hsv2rgb" "im2double" "im2frame" "image" "imagesc" "imfinfo" "imformats" "imread"
    "imshow" "imwrite" "ind2gray" "ind2rgb" "iscolormap" "jet" "lines" "movie" "ocean"
    "pink" "prism" "rainbow" "rgb2gray" "rgb2hsv" "rgb2ind" "rgbplot" "spinmap" "spring"
    "summer" "turbo" "viridis" "white" "winter"
  ])

(def +io+
  "Octave builtins from the `io` category."
  [
    "beep" "csvread" "csvwrite" "dlmwrite" "fileread" "importdata" "is_valid_file_id"
  ])

(def +java+
  "Octave builtins from the `java` category."
  [
    "javaArray" "java_get" "java_set" "javaaddpath" "javachk" "javaclasspath" "javamem"
    "javarmpath" "usejava"
  ])

(def +legacy+
  "Octave builtins from the `legacy` category."
  [
    "@inline/argnames" "@inline/cat" "@inline/char" "@inline/disp" "@inline/exist"
    "@inline/feval" "@inline/formula" "@inline/horzcat" "@inline/inline" "@inline/nargin"
    "@inline/nargout" "@inline/subsref" "@inline/symvar" "@inline/vectorize"
    "@inline/vertcat" "findstr" "flipdim" "genvarname" "isdir" "isequalwithequalnans"
    "isstr" "maxNumCompThreads" "setstr" "strmatch" "strread" "textread" "vectorize"
  ])

(def +linear-algebra+
  "Octave builtins from the `linear-algebra` category."
  [
    "bandwidth" "commutation_matrix" "cond" "condeig" "condest" "cross"
    "duplication_matrix" "expm" "gls" "housh" "isbanded" "isdefinite" "isdiag"
    "ishermitian" "issymmetric" "istril" "istriu" "krylov" "linsolve" "logm" "lscov"
    "normest" "normest1" "null" "ols" "ordeig" "orth" "planerot" "qzhess" "rank" "rref"
    "subspace" "trace" "vech" "vecnorm"
  ])

(def +miscellaneous+
  "Octave builtins from the `miscellaneous` category."
  [
    "bug_report" "bunzip2" "cast" "citation" "clearAllMemoizedCaches" "clearvars"
    "compare_versions" "computer" "copyfile" "delete" "dir" "dos" "edit" "fieldnames"
    "fileattrib" "fileparts" "fullfile" "getfield" "grabcode" "gunzip" "info"
    "inputParser" "inputname" "isdeployed" "isfile" "isfolder" "ismac" "ismethod" "ispc"
    "isunix" "jupyter_notebook" "license" "list_primes" "loadobj" "ls" "ls_command"
    "memoize" "memory" "menu" "methods" "mex" "mexext" "mkdir" "mkoctfile" "movefile"
    "mustBeFinite" "mustBeGreaterThan" "mustBeGreaterThanOrEqual" "mustBeInteger"
    "mustBeLessThan" "mustBeLessThanOrEqual" "mustBeMember" "mustBeNegative"
    "mustBeNonNan" "mustBeNonempty" "mustBeNonnegative" "mustBeNonpositive"
    "mustBeNonsparse" "mustBeNonzero" "mustBeNumeric" "mustBeNumericOrLogical"
    "mustBePositive" "mustBeReal" "namedargs2cell" "namelengthmax" "nargchk" "narginchk"
    "nargoutchk" "news" "nthargout" "open" "orderfields" "pack" "parseparams" "perl"
    "publish" "python" "recycle" "run" "saveobj" "setfield" "substruct" "swapbytes"
    "symvar" "tar" "unix" "unpack" "untar" "unzip" "validateattributes" "ver"
    "verLessThan" "version" "what" "zip"
  ])

(def +ode+
  "Octave builtins from the `ode` category."
  [
    "decic" "ode15i" "ode15s" "ode23" "ode23s" "ode45" "odeget" "odeplot" "odeset"
  ])

(def +optimization+
  "Octave builtins from the `optimization` category."
  [
    "fminbnd" "fminsearch" "fminunc" "fsolve" "fzero" "glpk" "humps" "lsqnonneg"
    "optimget" "optimset" "pqpnonneg" "qp" "sqp"
  ])

(def +path+
  "Octave builtins from the `path` category."
  [
    "import" "matlabroot" "pathdef" "savepath"
  ])

(def +pkg+
  "Octave builtins from the `pkg` category."
  [
    "pkg"
  ])

(def +plot+
  "Octave builtins from the `plot` category."
  [
    "allchild" "ancestor" "annotation" "area" "axes" "axis" "bar" "barh" "box" "camlight"
    "camlookat" "camorbit" "campos" "camroll" "camtarget" "camup" "camva" "camzoom"
    "caxis" "cla" "clabel" "clf" "close" "closereq" "colorbar" "colstyle" "comet" "comet3"
    "compass" "contour" "contour3" "contourc" "contourf" "copyobj" "cylinder" "daspect"
    "datetick" "diffuse" "ellipsoid" "errorbar" "ezcontour" "ezcontourf" "ezmesh"
    "ezmeshc" "ezplot" "ezplot3" "ezpolar" "ezsurf" "ezsurfc" "feather" "figure" "fill"
    "fill3" "findall" "findfigs" "findobj" "fplot" "gca" "gcbf" "gcbo" "gcf" "gco"
    "ginput" "gnuplot_binary" "graphics_toolkit" "grid" "groot" "gtext" "gui_mainfcn"
    "hdl2struct" "hggroup" "hgload" "hgsave" "hgtransform" "hidden" "hist" "hold" "isaxes"
    "isfigure" "isgraphics" "ishandle" "ishold" "isocaps" "isocolors" "isonormals"
    "isosurface" "isprop" "legend" "light" "lightangle" "lighting" "line" "linkaxes"
    "linkprop" "loglog" "loglogerr" "material" "mesh" "meshc" "meshgrid" "meshz" "ndgrid"
    "newplot" "openfig" "orient" "ostreamtube" "pan" "pareto" "patch" "pbaspect" "pcolor"
    "peaks" "pie" "pie3" "plot" "plot3" "plotmatrix" "plotyy" "polar" "print" "printd"
    "quiver" "quiver3" "rectangle" "reducepatch" "reducevolume" "refresh" "refreshdata"
    "ribbon" "rose" "rotate" "rotate3d" "rticks" "saveas" "savefig" "scatter" "scatter3"
    "semilogx" "semilogxerr" "semilogy" "semilogyerr" "shading" "shg" "shrinkfaces"
    "slice" "smooth3" "sombrero" "specular" "sphere" "stairs" "stem" "stem3" "stemleaf"
    "stream2" "stream3" "streamline" "streamribbon" "streamtube" "struct2hdl" "subplot"
    "surf" "surface" "surfc" "surfl" "surfnorm" "tetramesh" "text" "thetaticks" "title"
    "trimesh" "triplot" "trisurf" "view" "waterfall" "whitebg" "xlabel" "xlim"
    "xtickangle" "xticklabels" "xticks" "ylabel" "ylim" "ytickangle" "yticklabels"
    "yticks" "zlabel" "zlim" "zoom" "ztickangle" "zticklabels" "zticks"
  ])

(def +polynomial+
  "Octave builtins from the `polynomial` category."
  [
    "compan" "conv" "deconv" "mkpp" "mpoles" "padecoef" "pchip" "poly" "polyaffine"
    "polyder" "polyeig" "polyfit" "polygcd" "polyint" "polyout" "polyreduce" "polyval"
    "polyvalm" "ppder" "ppint" "ppjumps" "ppval" "residue" "roots" "spline" "splinefit"
    "unmkpp"
  ])

(def +prefs+
  "Octave builtins from the `prefs` category."
  [
    "addpref" "getpref" "ispref" "prefdir" "preferences" "rmpref" "setpref"
  ])

(def +profiler+
  "Octave builtins from the `profiler` category."
  [
    "profexplore" "profexport" "profile" "profshow"
  ])

(def +set+
  "Octave builtins from the `set` category."
  [
    "intersect" "ismember" "powerset" "setdiff" "setxor" "union" "unique" "uniquetol"
  ])

(def +signal+
  "Octave builtins from the `signal` category."
  [
    "arch_fit" "arch_rnd" "arch_test" "arma_rnd" "autoreg_matrix" "bartlett" "blackman"
    "detrend" "diffpara" "durbinlevinson" "fftconv" "fftfilt" "fftshift" "filter2"
    "fractdiff" "freqz" "freqz_plot" "hamming" "hanning" "hurst" "ifftshift" "movfun"
    "movslice" "periodogram" "sinc" "sinetone" "sinewave" "spectral_adf" "spectral_xdf"
    "spencer" "stft" "synthesis" "unwrap" "yulewalker"
  ])

(def +sparse+
  "Octave builtins from the `sparse` category."
  [
    "bicg" "bicgstab" "cgs" "colperm" "eigs" "etreeplot" "gmres" "gplot" "ichol" "ilu"
    "nonzeros" "pcg" "pcr" "qmr" "spaugment" "spconvert" "spdiags" "speye" "spfun"
    "spones" "sprand" "sprandn" "sprandsym" "spstats" "spy" "svds" "tfqmr" "treelayout"
    "treeplot"
  ])

(def +specfun+
  "Octave builtins from the `specfun` category."
  [
    "beta" "betainc" "betaincinv" "betaln" "cosint" "ellipke" "expint" "factor"
    "factorial" "gammainc" "gammaincinv" "isprime" "lcm" "legendre" "nchoosek" "nthroot"
    "perms" "primes" "reallog" "realpow" "realsqrt" "sinint"
  ])

(def +special-matrix+
  "Octave builtins from the `special-matrix` category."
  [
    "gallery" "hadamard" "hankel" "hilb" "invhilb" "magic" "pascal" "rosser" "toeplitz"
    "vander" "wilkinson"
  ])

(def +statistics+
  "Octave builtins from the `statistics` category."
  [
    "bounds" "center" "corr" "corrcoef" "cov" "discrete_cdf" "discrete_inv" "discrete_pdf"
    "discrete_rnd" "empirical_cdf" "empirical_inv" "empirical_pdf" "empirical_rnd" "histc"
    "iqr" "kendall" "kurtosis" "mad" "mean" "meansq" "median" "mode" "moment" "movmad"
    "movmax" "movmean" "movmedian" "movmin" "movprod" "movstd" "movsum" "movvar"
    "normalize" "prctile" "quantile" "range" "ranks" "run_count" "runlength" "skewness"
    "spearman" "statistics" "std" "var" "zscore"
  ])

(def +strings+
  "Octave builtins from the `strings` category."
  [
    "base2dec" "bin2dec" "blanks" "cstrcat" "deblank" "dec2base" "dec2bin" "dec2hex"
    "endsWith" "erase" "hex2dec" "index" "isletter" "isstring" "isstrprop" "mat2str"
    "native2unicode" "ostrsplit" "regexptranslate" "rindex" "startsWith" "str2num"
    "strcat" "strchr" "strjoin" "strjust" "strsplit" "strtok" "strtrim" "strtrunc"
    "substr" "unicode2native" "untabify" "validatestring"
  ])

(def +testfun+
  "Octave builtins from the `testfun` category."
  [
    "assert" "demo" "example" "fail" "oruntests" "rundemos" "speed" "test"
  ])

(def +time+
  "Octave builtins from the `time` category."
  [
    "addtodate" "asctime" "calendar" "clock" "ctime" "date" "datenum" "datestr" "datevec"
    "eomday" "etime" "is_leap_year" "now" "weekday"
  ])

(def +web+
  "Octave builtins from the `web` category."
  [
    "web" "weboptions" "webread" "webwrite"
  ])

(def +all+
  "All documented Octave builtins (1654 total)."
  [
    "!" "!=" "\"" "#" "#{" "#}" "%" "%{" "%}" "&" "&&" "'" "(" ")" "*" "**" "+" "++" ","
    "-" "-" ".'" ".*" ".**" "..." "./" ".\\" ".^" "/" ":" ";" "<" "<=" "=" "==" ">" ">="
    "@" "@audioplayer/audioplayer" "@audioplayer/disp" "@audioplayer/get"
    "@audioplayer/isplaying" "@audioplayer/pause" "@audioplayer/play"
    "@audioplayer/playblocking" "@audioplayer/resume" "@audioplayer/set"
    "@audioplayer/stop" "@audioplayer/subsasgn" "@audioplayer/subsref"
    "@audiorecorder/audiorecorder" "@audiorecorder/disp" "@audiorecorder/get"
    "@audiorecorder/getaudiodata" "@audiorecorder/getplayer" "@audiorecorder/isrecording"
    "@audiorecorder/pause" "@audiorecorder/play" "@audiorecorder/record"
    "@audiorecorder/recordblocking" "@audiorecorder/resume" "@audiorecorder/set"
    "@audiorecorder/stop" "@audiorecorder/subsasgn" "@audiorecorder/subsref" "@ftp/ascii"
    "@ftp/binary" "@ftp/cd" "@ftp/close" "@ftp/delete" "@ftp/dir" "@ftp/ftp"
    "@ftp/loadobj" "@ftp/mget" "@ftp/mkdir" "@ftp/mput" "@ftp/rename" "@ftp/rmdir"
    "@ftp/saveobj" "@inline/argnames" "@inline/cat" "@inline/char" "@inline/disp"
    "@inline/exist" "@inline/feval" "@inline/formula" "@inline/horzcat" "@inline/inline"
    "@inline/nargin" "@inline/nargout" "@inline/subsref" "@inline/symvar"
    "@inline/vectorize" "@inline/vertcat" "EDITOR" "EXEC_PATH" "F_DUPFD" "F_GETFD"
    "F_GETFL" "F_SETFD" "F_SETFL" "I" "IMAGE_PATH" "Inf" "NA" "NaN" "OCTAVE_EXEC_HOME"
    "OCTAVE_HOME" "OCTAVE_VERSION" "O_APPEND" "O_ASYNC" "O_CREAT" "O_EXCL" "O_NONBLOCK"
    "O_RDONLY" "O_RDWR" "O_SYNC" "O_TRUNC" "O_WRONLY" "PAGER" "PAGER_FLAGS" "PS1" "PS2"
    "PS4" "P_tmpdir" "SEEK_CUR" "SEEK_END" "SEEK_SET" "SIG" "S_ISBLK" "S_ISCHR" "S_ISDIR"
    "S_ISFIFO" "S_ISLNK" "S_ISREG" "S_ISSOCK" "WCONTINUE" "WCOREDUMP" "WEXITSTATUS"
    "WIFCONTINUED" "WIFEXITED" "WIFSIGNALED" "WIFSTOPPED" "WNOHANG" "WSTOPSIG" "WTERMSIG"
    "WUNTRACED" "[" "\\" "]" "^" "abs" "accumarray" "accumdim" "acos" "acosd" "acosh"
    "acot" "acotd" "acoth" "acsc" "acscd" "acsch" "add_input_event_hook" "addlistener"
    "addpath" "addpref" "addproperty" "addtodate" "airy" "all" "allchild" "amd" "ancestor"
    "and" "angle" "annotation" "ans" "any" "arch_fit" "arch_rnd" "arch_test" "area" "arg"
    "argv" "arma_rnd" "arrayfun" "asctime" "asec" "asecd" "asech" "asin" "asind" "asinh"
    "assert" "assignin" "atan" "atan2" "atan2d" "atand" "atanh" "atexit" "audiodevinfo"
    "audioformats" "audioinfo" "audioread" "audiowrite" "auto_repeat_debug_command"
    "autoload" "autoreg_matrix" "autumn" "available_graphics_toolkits" "axes" "axis"
    "balance" "bandwidth" "bar" "barh" "bartlett" "base2dec" "base64_decode"
    "base64_encode" "beep" "beep_on_error" "bessel" "besselh" "besseli" "besselj"
    "besselk" "bessely" "beta" "betainc" "betaincinv" "betaln" "bicg" "bicgstab" "bin2dec"
    "bincoeff" "bitand" "bitcmp" "bitget" "bitor" "bitpack" "bitset" "bitshift"
    "bitunpack" "bitxor" "blackman" "blanks" "blkdiag" "blkmm" "bone" "bounds" "box"
    "break" "brighten" "bsxfun" "bug_report" "built_in_docstrings_file" "builtin"
    "bunzip2" "bzip2" "calendar" "camlight" "camlookat" "camorbit" "campos" "camroll"
    "camtarget" "camup" "camva" "camzoom" "canonicalize_file_name" "cart2pol" "cart2sph"
    "case" "cast" "cat" "catch" "caxis" "cbrt" "ccolamd" "cd" "ceil" "cell" "cell2mat"
    "cell2struct" "celldisp" "cellfun" "cellindexmat" "cellslices" "cellstr" "center"
    "cgs" "char" "chol" "chol2inv" "choldelete" "cholinsert" "cholinv" "cholshift"
    "cholupdate" "circshift" "citation" "cla" "clabel" "class" "classdef" "clc" "clear"
    "clearAllMemoizedCaches" "clearvars" "clf" "clock" "close" "closereq"
    "cmdline_options" "cmpermute" "cmunique" "colamd" "colloc" "colon" "colorbar"
    "colorcube" "colormap" "colperm" "colstyle" "columns" "comet" "comet3"
    "command_line_path" "commandhistory" "commandwindow" "common_size"
    "commutation_matrix" "compan" "compare_versions" "compass" "completion_append_char"
    "completion_matches" "complex" "computer" "cond" "condeig" "condest"
    "confirm_recursive_rmdir" "conj" "containers.Map" "continue" "contour" "contour3"
    "contourc" "contourf" "contrast" "conv" "conv2" "convhull" "convhulln" "convn" "cool"
    "copper" "copyfile" "copyobj" "corr" "corrcoef" "cos" "cosd" "cosh" "cosint" "cospi"
    "cot" "cotd" "coth" "cov" "cplxpair" "cputime" "crash_dumps_octave_core" "cross" "csc"
    "cscd" "csch" "cstrcat" "csvread" "csvwrite" "csymamd" "ctime" "ctranspose"
    "cubehelix" "cummax" "cummin" "cumprod" "cumsum" "cumtrapz" "curl" "cylinder"
    "daspect" "daspk" "daspk_options" "dasrt" "dasrt_options" "dassl" "dassl_options"
    "date" "datenum" "datestr" "datetick" "datevec" "dawson" "dbclear" "dbcont" "dbdown"
    "dblist" "dblquad" "dbquit" "dbstack" "dbstatus" "dbstep" "dbstop" "dbtype" "dbup"
    "dbwhere" "deal" "deblank" "debug" "debug_java" "debug_on_error" "debug_on_interrupt"
    "debug_on_warning" "dec2base" "dec2bin" "dec2hex" "decic" "deconv" "deg2rad" "del2"
    "delaunay" "delaunayn" "delete" "dellistener" "demo" "desktop" "det" "detrend" "diag"
    "dialog" "diary" "diff" "diffpara" "diffuse" "dir" "dir_encoding" "dir_in_loadpath"
    "disable_diagonal_matrix" "disable_permutation_matrix" "disable_range" "discrete_cdf"
    "discrete_inv" "discrete_pdf" "discrete_rnd" "disp" "display" "divergence" "dlmread"
    "dlmwrite" "dmperm" "do" "do_string_escapes" "doc" "doc_cache_create" "doc_cache_file"
    "dos" "dot" "double" "drawnow" "dsearch" "dsearchn" "dup2" "duplication_matrix"
    "durbinlevinson" "e" "echo" "edit" "edit_history" "eig" "eigs" "ellipj" "ellipke"
    "ellipsoid" "else" "elseif" "empirical_cdf" "empirical_inv" "empirical_pdf"
    "empirical_rnd" "end" "end_try_catch" "end_unwind_protect" "endclassdef"
    "endenumeration" "endevents" "endfor" "endfunction" "endgrent" "endif" "endmethods"
    "endparfor" "endproperties" "endpwent" "endsWith" "endspmd" "endswitch" "endwhile"
    "enumeration" "eomday" "eps" "eq" "erase" "erf" "erfc" "erfcinv" "erfcx" "erfi"
    "erfinv" "errno" "errno_list" "error" "error_ids" "errorbar" "errordlg" "etime"
    "etree" "etreeplot" "eval" "evalc" "evalin" "events" "example" "exec" "exist" "exp"
    "expint" "expm" "expm1" "eye" "ezcontour" "ezcontourf" "ezmesh" "ezmeshc" "ezplot"
    "ezplot3" "ezpolar" "ezsurf" "ezsurfc" "factor" "factorial" "fail" "false" "fclear"
    "fclose" "fcntl" "fdisp" "feather" "feof" "ferror" "feval" "fflush" "fft" "fft2"
    "fftconv" "fftfilt" "fftn" "fftshift" "fftw" "fgetl" "fgets" "fieldnames" "figure"
    "file_in_loadpath" "file_in_path" "fileattrib" "filebrowser" "fileparts" "fileread"
    "filesep" "fill" "fill3" "filter" "filter2" "find" "findall" "findfigs" "findobj"
    "findstr" "fix" "fixed_point_format" "flag" "flintmax" "flip" "flipdim" "fliplr"
    "flipud" "floor" "fminbnd" "fminsearch" "fminunc" "fopen" "for" "fork" "format"
    "fplot" "fprintf" "fputs" "fractdiff" "frame2im" "fread" "freport" "freqz"
    "freqz_plot" "frewind" "fscanf" "fseek" "fskipl" "fsolve" "ftell" "full" "fullfile"
    "func2str" "function" "functions" "fwrite" "fzero" "gallery" "gamma" "gammainc"
    "gammaincinv" "gca" "gcbf" "gcbo" "gcd" "gcf" "gco" "ge" "genpath" "genvarname" "get"
    "get_first_help_sentence" "get_help_text" "get_help_text_from_file"
    "get_home_directory" "getappdata" "getegid" "getenv" "geteuid" "getfield" "getframe"
    "getgid" "getgrent" "getgrgid" "getgrnam" "gethostname" "getpgrp" "getpid"
    "getpixelposition" "getppid" "getpref" "getpwent" "getpwnam" "getpwuid" "getrusage"
    "getuid" "ginput" "givens" "glob" "global" "glpk" "gls" "gmres" "gmtime"
    "gnuplot_binary" "gplot" "grabcode" "gradient" "graphics_toolkit" "gray" "gray2ind"
    "grid" "griddata" "griddata3" "griddatan" "groot" "gsvd" "gt" "gtext" "gui_mainfcn"
    "guidata" "guihandles" "gunzip" "gzip" "hadamard" "hamming" "hankel" "hanning" "hash"
    "have_window_system" "hdl2struct" "help" "helpdlg" "hess" "hex2dec" "hex2num"
    "hggroup" "hgload" "hgsave" "hgtransform" "hidden" "hilb" "hist" "histc" "history"
    "history_control" "history_file" "history_save" "history_size"
    "history_timestamp_format_string" "hold" "horzcat" "hot" "housh" "hsv" "hsv2rgb"
    "humps" "hurst" "hypot" "ichol" "idivide" "if" "ifft" "ifft2" "ifftn" "ifftshift"
    "ignore_function_time_stamp" "ilu" "im2double" "im2frame" "imag" "image" "imagesc"
    "imfinfo" "imformats" "import" "importdata" "imread" "imshow" "imwrite" "ind2gray"
    "ind2rgb" "ind2sub" "index" "inferiorto" "info" "info_file" "info_program" "inpolygon"
    "input" "inputParser" "inputdlg" "inputname" "int16" "int2str" "int32" "int64" "int8"
    "integral" "integral2" "integral3" "interp1" "interp2" "interp3" "interpft" "interpn"
    "intersect" "intmax" "intmin" "inv" "invhilb" "ipermute" "iqr" "is_absolute_filename"
    "is_dq_string" "is_function_handle" "is_leap_year" "is_rooted_relative_filename"
    "is_same_file" "is_sq_string" "is_valid_file_id" "isa" "isalnum" "isalpha" "isappdata"
    "isargout" "isascii" "isaxes" "isbanded" "iscell" "iscellstr" "ischar" "iscntrl"
    "iscolormap" "iscolumn" "iscomplex" "isdebugmode" "isdefinite" "isdeployed" "isdiag"
    "isdigit" "isdir" "isempty" "isequal" "isequaln" "isequalwithequalnans" "isfield"
    "isfigure" "isfile" "isfinite" "isfloat" "isfolder" "isglobal" "isgraph" "isgraphics"
    "isguirunning" "ishandle" "ishermitian" "ishghandle" "ishold" "isieee" "isindex"
    "isinf" "isinteger" "isjava" "iskeyword" "isletter" "islogical" "islower" "ismac"
    "ismatrix" "ismember" "ismethod" "isna" "isnan" "isnull" "isnumeric" "isobject"
    "isocaps" "isocolors" "isonormals" "isosurface" "ispc" "ispref" "isprime" "isprint"
    "isprop" "ispunct" "isreal" "isrow" "isscalar" "issorted" "isspace" "issparse"
    "issquare" "isstr" "isstring" "isstrprop" "isstruct" "isstudent" "issymmetric"
    "istril" "istriu" "isunix" "isupper" "isvarname" "isvector" "isxdigit" "javaArray"
    "javaMethod" "javaObject" "java_get" "java_matrix_autoconversion" "java_set"
    "java_unsigned_autoconversion" "javaaddpath" "javachk" "javaclasspath" "javamem"
    "javarmpath" "jet" "jsondecode" "jsonencode" "jupyter_notebook" "kbhit" "kendall"
    "keyboard" "kill" "kron" "krylov" "kurtosis" "lasterr" "lasterror" "lastwarn" "lcm"
    "ldivide" "le" "legend" "legendre" "length" "lgamma" "license" "light" "lightangle"
    "lighting" "lin2mu" "line" "lines" "link" "linkaxes" "linkprop" "linsolve" "linspace"
    "list_in_columns" "list_primes" "listdlg" "listfonts" "load"
    "loaded_graphics_toolkits" "loadobj" "localfunctions" "localtime" "log" "log10"
    "log1p" "log2" "logical" "loglog" "loglogerr" "logm" "logspace" "lookfor" "lookup"
    "ls" "ls_command" "lscov" "lsode" "lsode_options" "lsqnonneg" "lstat" "lt" "lu"
    "luupdate" "mad" "magic" "make_absolute_filename" "makeinfo_program" "mat2cell"
    "mat2str" "material" "matlab.lang.makeUniqueStrings" "matlab.lang.makeValidName"
    "matlab.net.base64decode" "matlab.net.base64encode" "matlabroot" "matrix_type" "max"
    "maxNumCompThreads" "max_recursion_depth" "max_stack_depth" "mean" "meansq" "median"
    "memoize" "memory" "menu" "merge" "mesh" "meshc" "meshgrid" "meshz" "metaclass"
    "methods" "mex" "mexext" "mfilename" "mgorth" "min" "minus" "mislocked"
    "missing_component_hook" "missing_function_hook" "mkdir" "mkfifo" "mkoctfile" "mkpp"
    "mkstemp" "mktime" "mldivide" "mlock" "mod" "mode" "moment" "more" "movefile"
    "movegui" "movfun" "movie" "movmad" "movmax" "movmean" "movmedian" "movmin" "movprod"
    "movslice" "movstd" "movsum" "movvar" "mpoles" "mpower" "mrdivide" "msgbox" "mtimes"
    "mu2lin" "munlock" "mustBeFinite" "mustBeGreaterThan" "mustBeGreaterThanOrEqual"
    "mustBeInteger" "mustBeLessThan" "mustBeLessThanOrEqual" "mustBeMember"
    "mustBeNegative" "mustBeNonNan" "mustBeNonempty" "mustBeNonnegative"
    "mustBeNonpositive" "mustBeNonsparse" "mustBeNonzero" "mustBeNumeric"
    "mustBeNumericOrLogical" "mustBePositive" "mustBeReal" "namedargs2cell"
    "namelengthmax" "nargchk" "nargin" "narginchk" "nargout" "nargoutchk" "native2unicode"
    "native_float_format" "nchoosek" "ndgrid" "ndims" "ne" "newline" "newplot" "news"
    "nextpow2" "nnz" "nonzeros" "norm" "normalize" "normest" "normest1" "not" "now"
    "nproc" "nth_element" "nthargout" "nthroot" "null" "num2cell" "num2hex" "num2str"
    "numel" "numfields" "nzmax" "ocean" "octave_core_file_limit" "octave_core_file_name"
    "octave_core_file_options" "ode15i" "ode15s" "ode23" "ode23s" "ode45" "odeget"
    "odeplot" "odeset" "ols" "onCleanup" "ones" "open" "openfig" "openvar" "optimget"
    "optimize_diagonal_matrix" "optimize_permutation_matrix" "optimize_range"
    "optimize_subsasgn_calls" "optimset" "or" "ordeig" "orderfields" "ordqz" "ordschur"
    "orient" "orth" "oruntests" "ostreamtube" "ostrsplit" "otherwise" "output_precision"
    "pack" "padecoef" "page_output_immediately" "page_screen_output" "pagectranspose"
    "pagetranspose" "pan" "pareto" "parfor" "parseparams" "pascal" "patch" "path"
    "pathdef" "pathsep" "pause" "pbaspect" "pcg" "pchip" "pclose" "pcolor" "pcr" "peaks"
    "periodogram" "perl" "perms" "permute" "persistent" "pi" "pie" "pie3" "pink" "pinv"
    "pipe" "pkg" "planerot" "plot" "plot3" "plotmatrix" "plotyy" "plus" "pol2cart" "polar"
    "poly" "polyaffine" "polyarea" "polyder" "polyeig" "polyfit" "polygcd" "polyint"
    "polyout" "polyreduce" "polyval" "polyvalm" "popen" "popen2" "postpad" "pow2" "power"
    "powerset" "ppder" "ppint" "ppjumps" "ppval" "pqpnonneg" "prctile" "prefdir"
    "preferences" "prepad" "primes" "print" "print_empty_dimensions"
    "print_struct_array_contents" "print_usage" "printd" "printf" "prism" "prod"
    "profexplore" "profexport" "profile" "profshow" "program_invocation_name"
    "program_name" "properties" "psi" "publish" "puts" "pwd" "python" "qmr" "qp" "qr"
    "qrdelete" "qrinsert" "qrshift" "qrupdate" "quad" "quad2d" "quad_options" "quadcc"
    "quadgk" "quadl" "quadv" "quantile" "questdlg" "quit" "quiver" "quiver3" "qz" "qzhess"
    "rad2deg" "rainbow" "rand" "rande" "randg" "randi" "randn" "randp" "randperm" "range"
    "rank" "ranks" "rat" "rats" "rcond" "rdivide" "readdir" "readline_re_read_init_file"
    "readline_read_init_file" "readlink" "real" "reallog" "realmax" "realmin" "realpow"
    "realsqrt" "record" "rectangle" "rectint" "recycle" "reducepatch" "reducevolume"
    "refresh" "refreshdata" "regexp" "regexpi" "regexprep" "regexptranslate"
    "register_graphics_toolkit" "rehash" "rem" "remove_input_event_hook" "rename"
    "repelem" "repelems" "repmat" "rescale" "reset" "reshape" "residue" "resize"
    "restoredefaultpath" "rethrow" "return" "rgb2gray" "rgb2hsv" "rgb2ind" "rgbplot"
    "ribbon" "rindex" "rmappdata" "rmdir" "rmfield" "rmpath" "rmpref" "rng" "roots" "rose"
    "rosser" "rot90" "rotate" "rotate3d" "rotdim" "rotx" "roty" "rotz" "round" "roundb"
    "rows" "rref" "rsf2csf" "rticks" "run" "run_count" "run_history" "rundemos"
    "runlength" "save" "save_default_options" "save_header_format_string" "save_precision"
    "saveas" "savefig" "saveobj" "savepath" "scanf" "scatter" "scatter3" "schur" "sec"
    "secd" "sech" "semilogx" "semilogxerr" "semilogy" "semilogyerr" "set" "setappdata"
    "setdiff" "setenv" "setfield" "setgrent" "setpref" "setpwent" "setstr" "setxor"
    "shading" "shg" "shift" "shiftdim" "shrinkfaces" "sighup_dumps_octave_core" "sign"
    "signbit" "sigquit_dumps_octave_core" "sigterm_dumps_octave_core" "silent_functions"
    "sin" "sinc" "sind" "sinetone" "sinewave" "single" "sinh" "sinint" "sinpi" "size"
    "size_equal" "sizemax" "sizeof" "skewness" "slash" "slice" "smooth3" "sombrero" "sort"
    "sortrows" "sound" "soundsc" "source" "spalloc" "sparse" "sparse_auto_mutate"
    "spaugment" "spconvert" "spdiags" "spearman" "spectral_adf" "spectral_xdf" "specular"
    "speed" "spencer" "speye" "spfun" "sph2cart" "sphere" "spinmap" "spline" "splinefit"
    "split_long_rows" "spmd" "spones" "spparms" "sprand" "sprandn" "sprandsym" "sprank"
    "spring" "sprintf" "spstats" "spy" "sqp" "sqrt" "sqrtm" "squeeze" "sscanf" "stairs"
    "startsWith" "stat" "statistics" "std" "stderr" "stdin" "stdout" "stem" "stem3"
    "stemleaf" "stft" "str2double" "str2func" "str2num" "strcat" "strchr" "strcmp"
    "strcmpi" "stream2" "stream3" "streamline" "streamribbon" "streamtube" "strfind"
    "strftime" "string_fill_char" "strjoin" "strjust" "strmatch" "strncmp" "strncmpi"
    "strptime" "strread" "strrep" "strsplit" "strtok" "strtrim" "strtrunc" "struct"
    "struct2cell" "struct2hdl" "struct_levels_to_print" "structfun" "strvcat" "sub2ind"
    "subplot" "subsasgn" "subsindex" "subspace" "subsref" "substr" "substruct" "sum"
    "summer" "sumsq" "superiorto" "suppress_verbose_help_message" "surf" "surface" "surfc"
    "surfl" "surfnorm" "svd" "svd_driver" "svds" "swapbytes" "switch" "sylvester" "symamd"
    "symbfact" "symlink" "symrcm" "symvar" "synthesis" "system" "tan" "tand" "tanh" "tar"
    "tempdir" "tempname" "terminal_size" "test" "tetramesh" "texi_macros_file" "text"
    "textread" "textscan" "tfqmr" "thetaticks" "tic" "tilde_expand" "time" "times" "title"
    "tmpfile" "toc" "toeplitz" "tolower" "toupper" "trace" "transpose" "trapz"
    "treelayout" "treeplot" "tril" "trimesh" "triplequad" "triplot" "trisurf" "triu"
    "true" "try" "tsearch" "tsearchn" "turbo" "type" "typecast" "typeinfo" "uibuttongroup"
    "uicontextmenu" "uicontrol" "uifigure" "uigetdir" "uigetfile" "uimenu" "uint16"
    "uint32" "uint64" "uint8" "uipanel" "uipushtool" "uiputfile" "uiresume" "uisetfont"
    "uitable" "uitoggletool" "uitoolbar" "uiwait" "umask" "uminus" "uname"
    "undo_string_escapes" "unicode2native" "unicode_idx" "union" "unique" "uniquetol"
    "unix" "unlink" "unmkpp" "unpack" "unsetenv" "untabify" "untar" "until"
    "unwind_protect" "unwind_protect_cleanup" "unwrap" "unzip" "uplus" "urlread"
    "urlwrite" "usejava" "user_config_dir" "user_data_dir" "validateattributes"
    "validatestring" "vander" "var" "varargin" "varargout" "vec" "vech" "vecnorm"
    "vectorize" "ver" "verLessThan" "version" "vertcat" "view" "viridis" "voronoi"
    "voronoin" "waitbar" "waitfor" "waitforbuttonpress" "waitpid" "warndlg" "warning"
    "warning_ids" "warranty" "waterfall" "web" "weboptions" "webread" "webwrite" "weekday"
    "what" "which" "while" "white" "whitebg" "who" "whos" "whos_line_format" "wilkinson"
    "winqueryreg" "winter" "workspace" "xlabel" "xlim" "xor" "xtickangle" "xticklabels"
    "xticks" "yes_or_no" "ylabel" "ylim" "ytickangle" "yticklabels" "yticks" "yulewalker"
    "zeros" "zip" "zlabel" "zlim" "zoom" "zscore" "ztickangle" "zticklabels" "zticks" "|"
    "||" "~" "~="
  ])

(def +count+
  "Total number of documented Octave builtins."
  1654)
