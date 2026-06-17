(ns octave.core.builtin
  "Generated outline of GNU Octave 8.4 builtins with signatures, from /usr/share/octave/8.4.0/etc/doc-cache."
  (:require [hara.lang :as l]
            [std.lib.foundation :as f]))

(l/script :octave
  octave.core
  {:macro-only true})

(def +containers+
  "Octave builtins from the `+containers` category, with signatures."
  [
    {:name "containers.Map"
      :signatures [
        "M = containers.Map ()"
        "M = containers.Map (KEYS, VALS)"
        "M = containers.Map (KEYS, VALS, \"UniformValues\", IS_UNIFORM)"
        "M = containers.Map (\"KeyType\", KT, \"ValueType\", VT)"
      ]}
  ])

(def +matlab+
  "Octave builtins from the `+matlab` category, with signatures."
  [
    {:name "matlab.lang.makeUniqueStrings"
      :signatures [
        "UNIQSTR = matlab.lang.makeUniqueStrings (STR)"
        "UNIQSTR = matlab.lang.makeUniqueStrings (STR, EX)"
        "UNIQSTR = matlab.lang.makeUniqueStrings (STR, EX, MAXLENGTH)"
        "[UNIQSTR, ISMODIFIED] = matlab.lang.makeUniqueStrings (...)"
      ]}
    {:name "matlab.lang.makeValidName"
      :signatures [
        "VARNAME = matlab.lang.makeValidName (STR)"
        "VARNAME = matlab.lang.makeValidName (..., \"ReplacementStyle\", RS)"
        "VARNAME = matlab.lang.makeValidName (..., \"Prefix\", PFX)"
        "[VARNAME, ISMODIFIED] = matlab.lang.makeValidName (...)"
      ]}
    {:name "matlab.net.base64decode"
      :signatures [
        "OUT_VEC = matlab.net.base64decode (B64_STR)"
      ]}
    {:name "matlab.net.base64encode"
      :signatures [
        "B64_STR = matlab.net.base64encode (IN)"
      ]}
  ])

(def +ftp+
  "Octave builtins from the `@ftp` category, with signatures."
  [
    {:name "@ftp/ascii"
      :signatures [
        "ascii (F)"
      ]}
    {:name "@ftp/binary"
      :signatures [
        "binary (F)"
      ]}
    {:name "@ftp/cd"
      :signatures [
        "CWD = cd (F)"
        "cd (F, PATH)"
        "NEW_CWD = cd (F, PATH)"
      ]}
    {:name "@ftp/close"
      :signatures [
        "close (F)"
      ]}
    {:name "@ftp/delete"
      :signatures [
        "delete (F, FILE)"
      ]}
    {:name "@ftp/dir"
      :signatures [
        "dir (F)"
        "LST = dir (F)"
      ]}
    {:name "@ftp/ftp"
      :signatures [
        "F = ftp (HOST)"
        "F = ftp (HOST, USERNAME, PASSWORD)"
      ]}
    {:name "@ftp/loadobj"
      :signatures [
        "B = loadobj (A)"
      ]}
    {:name "@ftp/mget"
      :signatures [
        "mget (F, FILE)"
        "mget (F, DIR)"
        "mget (F, REMOTE_NAME, TARGET)"
      ]}
    {:name "@ftp/mkdir"
      :signatures [
        "mkdir (F, PATH)"
      ]}
    {:name "@ftp/mput"
      :signatures [
        "mput (F, FILE)"
        "FILE_LIST = mput (F, FILE)"
      ]}
    {:name "@ftp/rename"
      :signatures [
        "rename (F, OLDNAME, NEWNAME)"
      ]}
    {:name "@ftp/rmdir"
      :signatures [
        "rmdir (F, PATH)"
      ]}
    {:name "@ftp/saveobj"
      :signatures [
        "B = saveobj (A)"
      ]}
  ])

(def +audio+
  "Octave builtins from the `audio` category, with signatures."
  [
    {:name "@audioplayer/audioplayer"
      :signatures [
        "PLAYER = audioplayer (Y, FS)"
        "PLAYER = audioplayer (Y, FS, NBITS)"
        "PLAYER = audioplayer (Y, FS, NBITS, ID)"
        "PLAYER = audioplayer (RECORDER)"
        "PLAYER = audioplayer (RECORDER, ID)"
      ]}
    {:name "@audioplayer/disp"
      :signatures [
        "disp (PLAYER)"
      ]}
    {:name "@audioplayer/get"
      :signatures [
        "VALUE = get (PLAYER, NAME)"
        "VALUES = get (PLAYER, {NAME1, NAME2, ...})"
        "VALUES = get (PLAYER)"
      ]}
    {:name "@audioplayer/isplaying"
      :signatures [
        "TF = isplaying (PLAYER)"
      ]}
    {:name "@audioplayer/pause"
      :signatures [
        "pause (PLAYER)"
      ]}
    {:name "@audioplayer/play"
      :signatures [
        "play (PLAYER)"
        "play (PLAYER, START)"
        "play (PLAYER, [START, END])"
      ]}
    {:name "@audioplayer/playblocking"
      :signatures [
        "playblocking (PLAYER)"
        "playblocking (PLAYER, START)"
        "playblocking (PLAYER, [START, END])"
      ]}
    {:name "@audioplayer/resume"
      :signatures [
        "resume (PLAYER)"
      ]}
    {:name "@audioplayer/set"
      :signatures [
        "set (PLAYER, NAME, VALUE)"
        "set (PLAYER, NAME_CELL, VALUE_CELL)"
        "set (PLAYER, PROPERTIES_STRUCT)"
        "PROPERTIES = set (PLAYER)"
      ]}
    {:name "@audioplayer/stop"
      :signatures [
        "stop (PLAYER)"
      ]}
    {:name "@audioplayer/subsasgn"
      :signatures [
        "VALUE = subsasgn (PLAYER, IDX, RHS)"
      ]}
    {:name "@audioplayer/subsref"
      :signatures [
        "VALUE = subsref (PLAYER, IDX)"
      ]}
    {:name "@audiorecorder/audiorecorder"
      :signatures [
        "RECORDER = audiorecorder ()"
        "RECORDER = audiorecorder (FS, NBITS, NCHANNELS)"
        "RECORDER = audiorecorder (FS, NBITS, NCHANNELS, ID)"
      ]}
    {:name "@audiorecorder/disp"
      :signatures [
        "disp (RECORDER)"
      ]}
    {:name "@audiorecorder/get"
      :signatures [
        "VALUE = get (RECORDER, NAME)"
        "VALUES = get (RECORDER, {NAME1, NAME2, ...})"
        "VALUES = get (RECORDER)"
      ]}
    {:name "@audiorecorder/getaudiodata"
      :signatures [
        "DATA = getaudiodata (RECORDER)"
        "DATA = getaudiodata (RECORDER, DATATYPE)"
      ]}
    {:name "@audiorecorder/getplayer"
      :signatures [
        "PLAYER = getplayer (RECORDER)"
      ]}
    {:name "@audiorecorder/isrecording"
      :signatures [
        "TF = isrecording (RECORDER)"
      ]}
    {:name "@audiorecorder/pause"
      :signatures [
        "pause (RECORDER)"
      ]}
    {:name "@audiorecorder/play"
      :signatures [
        "PLAYER = play (RECORDER)"
        "PLAYER = play (RECORDER, START)"
        "PLAYER = play (RECORDER, [START, END])"
      ]}
    {:name "@audiorecorder/record"
      :signatures [
        "record (RECORDER)"
        "record (RECORDER, LENGTH)"
      ]}
    {:name "@audiorecorder/recordblocking"
      :signatures [
        "recordblocking (RECORDER, LENGTH)"
      ]}
    {:name "@audiorecorder/resume"
      :signatures [
        "resume (RECORDER)"
      ]}
    {:name "@audiorecorder/set"
      :signatures [
        "set (RECORDER, NAME, VALUE)"
        "set (RECORDER, NAME_CELL, VALUE_CELL)"
        "set (RECORDER, PROPERTIES_STRUCT)"
        "PROPERTIES = set (RECORDER)"
      ]}
    {:name "@audiorecorder/stop"
      :signatures [
        "stop (RECORDER)"
      ]}
    {:name "@audiorecorder/subsasgn"
      :signatures [
        "VALUE = subsasgn (RECORDER, IDX, RHS)"
      ]}
    {:name "@audiorecorder/subsref"
      :signatures [
        "VALUE = subsref (RECORDER, IDX)"
      ]}
    {:name "lin2mu"
      :signatures [
        "Y = lin2mu (X)"
        "Y = lin2mu (X, N)"
      ]}
    {:name "mu2lin"
      :signatures [
        "Y = mu2lin (X)"
        "Y = mu2lin (X, N)"
      ]}
    {:name "record"
      :signatures [
        "DATA = record (SEC)"
        "DATA = record (SEC, FS)"
      ]}
    {:name "sound"
      :signatures [
        "sound (Y)"
        "sound (Y, FS)"
        "sound (Y, FS, NBITS)"
      ]}
    {:name "soundsc"
      :signatures [
        "soundsc (Y)"
        "soundsc (Y, FS)"
        "soundsc (Y, FS, NBITS)"
        "soundsc (..., [YMIN, YMAX])"
      ]}
  ])

(def ^:private +core-1+
  "Chunk 1 of Octave builtins from the `core` category, with signatures."
  [
    {:name "!"
      :signatures [
        "!"
      ]}
    {:name "!="
      :signatures [
        "!="
      ]}
    {:name "\""
      :signatures [
        "\""
      ]}
    {:name "#"
      :signatures [
        "#"
      ]}
    {:name "#{"
      :signatures [
        "#{"
      ]}
    {:name "#}"
      :signatures [
        "#}"
      ]}
    {:name "%"
      :signatures [
        "%"
      ]}
    {:name "%{"
      :signatures [
        "%{"
      ]}
    {:name "%}"
      :signatures [
        "%}"
      ]}
    {:name "&"
      :signatures [
        "&"
      ]}
    {:name "&&"
      :signatures [
        "&&"
      ]}
    {:name "'"
      :signatures [
        "'"
      ]}
    {:name "("
      :signatures [
        "("
      ]}
    {:name ")"
      :signatures [
        ")"
      ]}
    {:name "*"
      :signatures [
        "*"
      ]}
    {:name "**"
      :signatures [
        "**"
      ]}
    {:name "+"
      :signatures [
        "+"
      ]}
    {:name "++"
      :signatures [
        "++"
      ]}
    {:name ","
      :signatures [
        ","
      ]}
    {:name "-"
      :signatures [
        "-"
      ]}
    {:name "-"
      :signatures [
        "--"
      ]}
    {:name ".'"
      :signatures [
        ".'"
      ]}
    {:name ".*"
      :signatures [
        ".*"
      ]}
    {:name ".**"
      :signatures [
        ".**"
      ]}
    {:name "..."
      :signatures [
        "..."
      ]}
    {:name "./"
      :signatures [
        "./"
      ]}
    {:name ".\\"
      :signatures [
        ".\\"
      ]}
    {:name ".^"
      :signatures [
        ".^"
      ]}
    {:name "/"
      :signatures [
        "/"
      ]}
    {:name ":"
      :signatures [
        ":"
      ]}
    {:name ";"
      :signatures [
        ";"
      ]}
    {:name "<"
      :signatures [
        "<"
      ]}
    {:name "<="
      :signatures [
        "<="
      ]}
    {:name "="
      :signatures [
        "="
      ]}
    {:name "=="
      :signatures [
        "=="
      ]}
    {:name ">"
      :signatures [
        ">"
      ]}
    {:name ">="
      :signatures [
        ">="
      ]}
    {:name "@"
      :signatures [
        "@"
      ]}
    {:name "EDITOR"
      :signatures [
        "VAL = EDITOR ()"
        "OLD_VAL = EDITOR (NEW_VAL)"
        "OLD_VAL = EDITOR (NEW_VAL, \"local\")"
      ]}
    {:name "EXEC_PATH"
      :signatures [
        "VAL = EXEC_PATH ()"
        "OLD_VAL = EXEC_PATH (NEW_VAL)"
        "OLD_VAL = EXEC_PATH (NEW_VAL, \"local\")"
      ]}
    {:name "F_DUPFD"
      :signatures [
        "V = F_DUPFD ()"
      ]}
    {:name "F_GETFD"
      :signatures [
        "V = F_GETFD ()"
      ]}
    {:name "F_GETFL"
      :signatures [
        "V = F_GETFL ()"
      ]}
    {:name "F_SETFD"
      :signatures [
        "V = F_SETFD ()"
      ]}
    {:name "F_SETFL"
      :signatures [
        "V = F_SETFL ()"
      ]}
    {:name "I"
      :signatures [
        "A = I"
        "A = I (N)"
        "A = I (N, M)"
        "A = I (N, M, K, ...)"
        "A = I (..., CLASS)"
      ]}
    {:name "IMAGE_PATH"
      :signatures [
        "VAL = IMAGE_PATH ()"
        "OLD_VAL = IMAGE_PATH (NEW_VAL)"
        "OLD_VAL = IMAGE_PATH (NEW_VAL, \"local\")"
      ]}
    {:name "Inf"
      :signatures [
        "A = Inf"
        "A = Inf (N)"
        "A = Inf (N, M)"
        "A = Inf (N, M, K, ...)"
        "A = Inf (..., CLASS)"
      ]}
    {:name "NA"
      :signatures [
        "VAL = NA"
        "VAL = NA (N)"
        "VAL = NA (N, M)"
        "VAL = NA (N, M, K, ...)"
        "VAL = NA (..., \"like\", VAR)"
        "VAL = NA (..., CLASS)"
      ]}
    {:name "NaN"
      :signatures [
        "VAL = NaN"
        "VAL = NaN (N)"
        "VAL = NaN (N, M)"
        "VAL = NaN (N, M, K, ...)"
        "VAL = NaN (..., \"like\", VAR)"
        "VAL = NaN (..., CLASS)"
      ]}
    {:name "OCTAVE_EXEC_HOME"
      :signatures [
        "DIR = OCTAVE_EXEC_HOME ()"
      ]}
    {:name "OCTAVE_HOME"
      :signatures [
        "DIR = OCTAVE_HOME ()"
      ]}
    {:name "OCTAVE_VERSION"
      :signatures [
        "VERSTR = OCTAVE_VERSION ()"
      ]}
    {:name "O_APPEND"
      :signatures [
        "V = O_APPEND ()"
      ]}
    {:name "O_ASYNC"
      :signatures [
        "V = O_ASYNC ()"
      ]}
    {:name "O_CREAT"
      :signatures [
        "V = O_CREAT ()"
      ]}
    {:name "O_EXCL"
      :signatures [
        "V = O_EXCL ()"
      ]}
    {:name "O_NONBLOCK"
      :signatures [
        "V = O_NONBLOCK ()"
      ]}
    {:name "O_RDONLY"
      :signatures [
        "V = O_RDONLY ()"
      ]}
    {:name "O_RDWR"
      :signatures [
        "V = O_RDWR ()"
      ]}
    {:name "O_SYNC"
      :signatures [
        "V = O_SYNC ()"
      ]}
    {:name "O_TRUNC"
      :signatures [
        "V = O_TRUNC ()"
      ]}
    {:name "O_WRONLY"
      :signatures [
        "V = O_WRONLY ()"
      ]}
    {:name "PAGER"
      :signatures [
        "VAL = PAGER ()"
        "OLD_VAL = PAGER (NEW_VAL)"
        "OLD_VAL = PAGER (NEW_VAL, \"local\")"
      ]}
    {:name "PAGER_FLAGS"
      :signatures [
        "VAL = PAGER_FLAGS ()"
        "OLD_VAL = PAGER_FLAGS (NEW_VAL)"
        "OLD_VAL = PAGER_FLAGS (NEW_VAL, \"local\")"
      ]}
    {:name "PS1"
      :signatures [
        "VAL = PS1 ()"
        "OLD_VAL = PS1 (NEW_VAL)"
        "OLD_VAL = PS1 (NEW_VAL, \"local\")"
      ]}
    {:name "PS2"
      :signatures [
        "VAL = PS2 ()"
        "OLD_VAL = PS2 (NEW_VAL)"
        "OLD_VAL = PS2 (NEW_VAL, \"local\")"
      ]}
    {:name "PS4"
      :signatures [
        "VAL = PS4 ()"
        "OLD_VAL = PS4 (NEW_VAL)"
        "OLD_VAL = PS4 (NEW_VAL, \"local\")"
      ]}
    {:name "P_tmpdir"
      :signatures [
        "SYS_TMPDIR = P_tmpdir ()"
      ]}
    {:name "SEEK_CUR"
      :signatures [
        "FSEEK_ORIGIN = SEEK_CUR ()"
      ]}
    {:name "SEEK_END"
      :signatures [
        "FSEEK_ORIGIN = SEEK_END ()"
      ]}
    {:name "SEEK_SET"
      :signatures [
        "FSEEK_ORIGIN = SEEK_SET ()"
      ]}
    {:name "SIG"
      :signatures [
        "S = SIG ()"
      ]}
    {:name "S_ISBLK"
      :signatures [
        "TF = S_ISBLK (MODE)"
      ]}
    {:name "S_ISCHR"
      :signatures [
        "TF = S_ISCHR (MODE)"
      ]}
    {:name "S_ISDIR"
      :signatures [
        "TF = S_ISDIR (MODE)"
      ]}
    {:name "S_ISFIFO"
      :signatures [
        "TF = S_ISFIFO (MODE)"
      ]}
    {:name "S_ISLNK"
      :signatures [
        "TF = S_ISLNK (MODE)"
      ]}
    {:name "S_ISREG"
      :signatures [
        "TF = S_ISREG (MODE)"
      ]}
    {:name "S_ISSOCK"
      :signatures [
        "TF = S_ISSOCK (MODE)"
      ]}
    {:name "WCONTINUE"
      :signatures [
        "V = WCONTINUE ()"
      ]}
    {:name "WCOREDUMP"
      :signatures [
        "TF = WCOREDUMP (STATUS)"
      ]}
    {:name "WEXITSTATUS"
      :signatures [
        "TF = WEXITSTATUS (STATUS)"
      ]}
    {:name "WIFCONTINUED"
      :signatures [
        "TF = WIFCONTINUED (STATUS)"
      ]}
    {:name "WIFEXITED"
      :signatures [
        "TF = WIFEXITED (STATUS)"
      ]}
    {:name "WIFSIGNALED"
      :signatures [
        "TF = WIFSIGNALED (STATUS)"
      ]}
    {:name "WIFSTOPPED"
      :signatures [
        "TF = WIFSTOPPED (STATUS)"
      ]}
    {:name "WNOHANG"
      :signatures [
        "V = WNOHANG ()"
      ]}
    {:name "WSTOPSIG"
      :signatures [
        "TF = WSTOPSIG (STATUS)"
      ]}
    {:name "WTERMSIG"
      :signatures [
        "TF = WTERMSIG (STATUS)"
      ]}
    {:name "WUNTRACED"
      :signatures [
        "V = WUNTRACED ()"
      ]}
    {:name "["
      :signatures [
        "["
      ]}
    {:name "\\"
      :signatures [
        "\\"
      ]}
    {:name "]"
      :signatures [
        "]"
      ]}
    {:name "^"
      :signatures [
        "^"
      ]}
    {:name "abs"
      :signatures [
        "Z = abs (X)"
      ]}
    {:name "acos"
      :signatures [
        "Y = acos (X)"
      ]}
    {:name "acosh"
      :signatures [
        "Y = acosh (X)"
      ]}
    {:name "add_input_event_hook"
      :signatures [
        "ID = add_input_event_hook (FCN)"
        "ID = add_input_event_hook (FCN, DATA)"
      ]}
    {:name "addlistener"
      :signatures [
        "addlistener (H, PROP, FCN)"
      ]}
    {:name "addpath"
      :signatures [
        "addpath (DIR1, ...)"
        "addpath (DIR1, ..., OPTION)"
        "OLDPATH = addpath (...)"
      ]}
    {:name "addproperty"
      :signatures [
        "addproperty (NAME, H, TYPE)"
        "addproperty (NAME, H, TYPE, ARG, ...)"
      ]}
    {:name "airy"
      :signatures [
        "A = airy (Z)"
        "A = airy (K, Z)"
        "A = airy (K, Z, SCALE)"
        "[A, IERR] = airy (...)"
      ]}
    {:name "all"
      :signatures [
        "TF = all (X)"
        "TF = all (X, DIM)"
      ]}
    {:name "amd"
      :signatures [
        "P = amd (S)"
        "P = amd (S, OPTS)"
      ]}
    {:name "and"
      :signatures [
        "TF = and (X, Y)"
        "TF = and (X1, X2, ...)"
      ]}
    {:name "angle"
      :signatures [
        "THETA = angle (Z)"
      ]}
    {:name "ans"
      :signatures [
      ]}
    {:name "any"
      :signatures [
        "TF = any (X)"
        "TF = any (X, DIM)"
      ]}
    {:name "arg"
      :signatures [
        "THETA = arg (Z)"
        "THETA = angle (Z)"
      ]}
    {:name "argv"
      :signatures [
        "ARGS = argv ()"
      ]}
    {:name "arrayfun"
      :signatures [
        "B = arrayfun (FCN, A)"
        "B = arrayfun (FCN, A1, A2, ...)"
        "[B1, B2, ...] = arrayfun (FCN, A, ...)"
        "B = arrayfun (..., \"UniformOutput\", VAL)"
        "B = arrayfun (..., \"ErrorHandler\", ERRFCN)"
      ]}
    {:name "asin"
      :signatures [
        "Y = asin (X)"
      ]}
    {:name "asinh"
      :signatures [
        "Y = asinh (X)"
      ]}
    {:name "assignin"
      :signatures [
        "assignin (CONTEXT, VARNAME, VALUE)"
      ]}
    {:name "atan"
      :signatures [
        "Y = atan (X)"
      ]}
    {:name "atan2"
      :signatures [
        "ANGLE = atan2 (Y, X)"
      ]}
    {:name "atanh"
      :signatures [
        "Y = atanh (X)"
      ]}
    {:name "atexit"
      :signatures [
        "atexit (FCN)"
        "atexit (FCN, true)"
        "atexit (FCN, false)"
        "STATUS = atexit (FCN, false)"
      ]}
    {:name "auto_repeat_debug_command"
      :signatures [
        "VAL = auto_repeat_debug_command ()"
        "OLD_VAL = auto_repeat_debug_command (NEW_VAL)"
        "OLD_VAL = auto_repeat_debug_command (NEW_VAL, \"local\")"
      ]}
    {:name "autoload"
      :signatures [
        "AUTOLOAD_MAP = autoload ()"
        "autoload (FUNCTION, FILE)"
        "autoload (..., \"remove\")"
      ]}
    {:name "available_graphics_toolkits"
      :signatures [
        "TOOLKITS = available_graphics_toolkits ()"
      ]}
    {:name "balance"
      :signatures [
        "AA = balance (A)"
        "AA = balance (A, OPT)"
        "[DD, AA] = balance (A, OPT)"
        "[D, P, AA] = balance (A, OPT)"
        "[CC, DD, AA, BB] = balance (A, B, OPT)"
      ]}
    {:name "base64_decode"
      :signatures [
        "X = base64_decode (S)"
        "X = base64_decode (S, DIMS)"
      ]}
    {:name "base64_encode"
      :signatures [
        "S = base64_encode (X)"
      ]}
    {:name "beep_on_error"
      :signatures [
        "VAL = beep_on_error ()"
        "OLD_VAL = beep_on_error (NEW_VAL)"
        "OLD_VAL = beep_on_error (NEW_VAL, \"local\")"
      ]}
    {:name "besselh"
      :signatures [
        "H = besselh (ALPHA, X)"
        "H = besselh (ALPHA, K, X)"
        "H = besselh (ALPHA, K, X, OPT)"
        "[H, IERR] = besselh (...)"
      ]}
    {:name "besseli"
      :signatures [
        "I = besseli (ALPHA, X)"
        "I = besseli (ALPHA, X, OPT)"
        "[I, IERR] = besseli (...)"
      ]}
    {:name "besselj"
      :signatures [
        "J = besselj (ALPHA, X)"
        "J = besselj (ALPHA, X, OPT)"
        "[J, IERR] = besselj (...)"
      ]}
    {:name "besselk"
      :signatures [
        "K = besselk (ALPHA, X)"
        "K = besselk (ALPHA, X, OPT)"
        "[K, IERR] = besselk (...)"
      ]}
    {:name "bessely"
      :signatures [
        "Y = bessely (ALPHA, X)"
        "Y = bessely (ALPHA, X, OPT)"
        "[Y, IERR] = bessely (...)"
      ]}
    {:name "bitand"
      :signatures [
        "Z = bitand (X, Y)"
      ]}
    {:name "bitor"
      :signatures [
        "Z = bitor (X, Y)"
      ]}
    {:name "bitpack"
      :signatures [
        "Y = bitpack (X, CLASS)"
      ]}
    {:name "bitshift"
      :signatures [
        "B = bitshift (A, K)"
        "B = bitshift (A, K, N)"
      ]}
    {:name "bitunpack"
      :signatures [
        "Y = bitunpack (X)"
      ]}
    {:name "bitxor"
      :signatures [
        "Z = bitxor (X, Y)"
      ]}
    {:name "blkmm"
      :signatures [
        "C = blkmm (A, B)"
      ]}
    {:name "break"
      :signatures [
        "break"
      ]}
    {:name "bsxfun"
      :signatures [
        "C = bsxfun (F, A, B)"
      ]}
    {:name "built_in_docstrings_file"
      :signatures [
        "VAL = built_in_docstrings_file ()"
        "OLD_VAL = built_in_docstrings_file (NEW_VAL)"
        "OLD_VAL = built_in_docstrings_file (NEW_VAL, \"local\")"
      ]}
    {:name "builtin"
      :signatures [
        "[...] = builtin (F, ...)"
      ]}
    {:name "canonicalize_file_name"
      :signatures [
        "[CNAME, STATUS, MSG] = canonicalize_file_name (FNAME)"
      ]}
    {:name "case"
      :signatures [
        "case VALUE"
        "case {VALUE, ...}"
      ]}
    {:name "cat"
      :signatures [
        "A = cat (DIM, ARRAY1, ARRAY2, ..., ARRAYN)"
      ]}
    {:name "catch"
      :signatures [
        "catch"
        "catch VALUE"
      ]}
    {:name "cbrt"
      :signatures [
        "Y = cbrt (X)"
      ]}
    {:name "ccolamd"
      :signatures [
        "P = ccolamd (S)"
        "P = ccolamd (S, KNOBS)"
        "P = ccolamd (S, KNOBS, CMEMBER)"
        "[P, STATS] = ccolamd (...)"
      ]}
    {:name "cd"
      :signatures [
        "cd DIR"
        "cd"
        "OLD_DIR = cd"
        "OLD_DIR = cd (DIR)"
        "chdir ..."
      ]}
    {:name "ceil"
      :signatures [
        "Y = ceil (X)"
      ]}
  ])

(def ^:private +core-2+
  "Chunk 2 of Octave builtins from the `core` category, with signatures."
  [
    {:name "cell"
      :signatures [
        "C = cell (N)"
        "C = cell (M, N)"
        "C = cell (M, N, K, ...)"
        "C = cell ([M N ...])"
      ]}
    {:name "cell2struct"
      :signatures [
        "S = cell2struct (CELL, FIELDS)"
        "S = cell2struct (CELL, FIELDS, DIM)"
      ]}
    {:name "cellfun"
      :signatures [
        "A = cellfun (\"FCN\", C)"
        "A = cellfun (\"size\", C, K)"
        "A = cellfun (\"isclass\", C, CLASS)"
        "A = cellfun (@FCN, C)"
        "A = cellfun (FCN, C)"
        "A = cellfun (FCN, C1, C2, ...)"
        "[A1, A2, ...] = cellfun (...)"
        "A = cellfun (..., \"ErrorHandler\", ERRFCN)"
        "A = cellfun (..., \"UniformOutput\", VAL)"
      ]}
    {:name "cellindexmat"
      :signatures [
        "Y = cellindexmat (X, VARARGIN)"
      ]}
    {:name "cellslices"
      :signatures [
        "SL = cellslices (X, LB, UB, DIM)"
      ]}
    {:name "cellstr"
      :signatures [
        "CSTR = cellstr (STRMAT)"
      ]}
    {:name "char"
      :signatures [
        "C = char (A)"
        "C = char (A, ...)"
        "C = char (STR1, STR2, ...)"
        "C = char (CELL_ARRAY)"
      ]}
    {:name "chol"
      :signatures [
        "R = chol (A)"
        "[R, P] = chol (A)"
        "[R, P, Q] = chol (A)"
        "[R, P, Q] = chol (A, \"vector\")"
        "[L, ...] = chol (..., \"lower\")"
        "[R, ...] = chol (..., \"upper\")"
      ]}
    {:name "chol2inv"
      :signatures [
        "AINV = chol2inv (R)"
      ]}
    {:name "choldelete"
      :signatures [
        "R1 = choldelete (R, J)"
      ]}
    {:name "cholinsert"
      :signatures [
        "R1 = cholinsert (R, J, U)"
        "[R1, INFO] = cholinsert (R, J, U)"
      ]}
    {:name "cholinv"
      :signatures [
        "AINV = cholinv (A)"
      ]}
    {:name "cholshift"
      :signatures [
        "R1 = cholshift (R, I, J)"
      ]}
    {:name "cholupdate"
      :signatures [
        "[R1, INFO] = cholupdate (R, U, OP)"
      ]}
    {:name "class"
      :signatures [
        "CLASSNAME = class (OBJ)"
        "CLS = class (S, CLASSNAME)"
        "CLS = class (S, CLASSNAME, PARENT1, ...)"
      ]}
    {:name "classdef"
      :signatures [
        "classdef"
      ]}
    {:name "clc"
      :signatures [
        "clc ()"
        "home ()"
      ]}
    {:name "clear"
      :signatures [
        "clear"
        "clear PATTERN ..."
        "clear OPTIONS PATTERN ..."
      ]}
    {:name "cmdline_options"
      :signatures [
        "OPT_STRUCT = cmdline_options ()"
      ]}
    {:name "colamd"
      :signatures [
        "P = colamd (S)"
        "P = colamd (S, KNOBS)"
        "[P, STATS] = colamd (S)"
        "[P, STATS] = colamd (S, KNOBS)"
      ]}
    {:name "colloc"
      :signatures [
        "[R, AMAT, BMAT, Q] = colloc (N, \"left\", \"right\")"
      ]}
    {:name "colon"
      :signatures [
        "R = colon (BASE, LIMIT)"
        "R = colon (BASE, INCREMENT, LIMIT)"
      ]}
    {:name "columns"
      :signatures [
        "NC = columns (A)"
      ]}
    {:name "command_line_path"
      :signatures [
        "PATHSTR = command_line_path ()"
      ]}
    {:name "commandhistory"
      :signatures [
        "commandhistory ()"
      ]}
    {:name "commandwindow"
      :signatures [
        "commandwindow ()"
      ]}
    {:name "completion_append_char"
      :signatures [
        "VAL = completion_append_char ()"
        "OLD_VAL = completion_append_char (NEW_VAL)"
        "OLD_VAL = completion_append_char (NEW_VAL, \"local\")"
      ]}
    {:name "completion_matches"
      :signatures [
        "COMPLETION_LIST = completion_matches (\"HINT\")"
      ]}
    {:name "complex"
      :signatures [
        "Z = complex (X)"
        "Z = complex (RE, IM)"
      ]}
    {:name "confirm_recursive_rmdir"
      :signatures [
        "VAL = confirm_recursive_rmdir ()"
        "OLD_VAL = confirm_recursive_rmdir (NEW_VAL)"
        "OLD_VAL = confirm_recursive_rmdir (NEW_VAL, \"local\")"
      ]}
    {:name "conj"
      :signatures [
        "ZC = conj (Z)"
      ]}
    {:name "continue"
      :signatures [
        "continue"
      ]}
    {:name "conv2"
      :signatures [
        "C = conv2 (A, B)"
        "C = conv2 (V1, V2, M)"
        "C = conv2 (..., SHAPE)"
      ]}
    {:name "convn"
      :signatures [
        "C = convn (A, B)"
        "C = convn (A, B, SHAPE)"
      ]}
    {:name "cos"
      :signatures [
        "Y = cos (X)"
      ]}
    {:name "cosh"
      :signatures [
        "Y = cosh (X)"
      ]}
    {:name "cputime"
      :signatures [
        "[TOTAL, USER, SYSTEM] = cputime ();"
      ]}
    {:name "crash_dumps_octave_core"
      :signatures [
        "VAL = crash_dumps_octave_core ()"
        "OLD_VAL = crash_dumps_octave_core (NEW_VAL)"
        "OLD_VAL = crash_dumps_octave_core (NEW_VAL, \"local\")"
      ]}
    {:name "csymamd"
      :signatures [
        "P = csymamd (S)"
        "P = csymamd (S, KNOBS)"
        "P = csymamd (S, KNOBS, CMEMBER)"
        "[P, STATS] = csymamd (...)"
      ]}
    {:name "ctranspose"
      :signatures [
        "B = ctranspose (A)"
      ]}
    {:name "cummax"
      :signatures [
        "M = cummax (X)"
        "M = cummax (X, DIM)"
        "[M, IM] = cummax (...)"
      ]}
    {:name "cummin"
      :signatures [
        "M = cummin (X)"
        "M = cummin (X, DIM)"
        "[M, IM] = cummin (X)"
      ]}
    {:name "cumprod"
      :signatures [
        "Y = cumprod (X)"
        "Y = cumprod (X, DIM)"
      ]}
    {:name "cumsum"
      :signatures [
        "Y = cumsum (X)"
        "Y = cumsum (X, DIM)"
        "Y = cumsum (..., \"native\")"
        "Y = cumsum (..., \"double\")"
      ]}
    {:name "daspk"
      :signatures [
        "[X, XDOT, ISTATE, MSG] = daspk (FCN, X_0, XDOT_0, T, T_CRIT)"
      ]}
    {:name "daspk_options"
      :signatures [
        "daspk_options ()"
        "val = daspk_options (OPT)"
        "daspk_options (OPT, VAL)"
      ]}
    {:name "dasrt"
      :signatures [
        "[X, XDOT, T_OUT, ISTAT, MSG] = dasrt (FCN, G, X_0, XDOT_0, T)"
        "... = dasrt (FCN, G, X_0, XDOT_0, T, T_CRIT)"
        "... = dasrt (FCN, X_0, XDOT_0, T)"
        "... = dasrt (FCN, X_0, XDOT_0, T, T_CRIT)"
      ]}
    {:name "dasrt_options"
      :signatures [
        "dasrt_options ()"
        "val = dasrt_options (OPT)"
        "dasrt_options (OPT, VAL)"
      ]}
    {:name "dassl"
      :signatures [
        "[X, XDOT, ISTATE, MSG] = dassl (FCN, X_0, XDOT_0, T, T_CRIT)"
      ]}
    {:name "dassl_options"
      :signatures [
        "dassl_options ()"
        "val = dassl_options (OPT)"
        "dassl_options (OPT, VAL)"
      ]}
    {:name "dawson"
      :signatures [
        "V = dawson (Z)"
      ]}
    {:name "dbclear"
      :signatures [
        "dbclear FCN"
        "dbclear FCN LINE"
        "dbclear FCN LINE1 LINE2 ..."
        "dbclear LINE ..."
        "dbclear all"
        "dbclear in FCN"
        "dbclear in FCN at LINE"
        "dbclear if EVENT"
        "dbclear (\"FCN\")"
        "dbclear (\"FCN\", LINE)"
        "dbclear (\"FCN\", LINE1, LINE2, ...)"
        "dbclear (\"FCN\", LINE1, ...)"
        "dbclear (LINE, ...)"
        "dbclear (\"all\")"
      ]}
    {:name "dbcont"
      :signatures [
        "dbcont"
      ]}
    {:name "dbdown"
      :signatures [
        "dbdown"
        "dbdown N"
      ]}
    {:name "dblist"
      :signatures [
        "dblist"
        "dblist N"
      ]}
    {:name "dbquit"
      :signatures [
        "dbquit"
        "dbquit all"
      ]}
    {:name "dbstack"
      :signatures [
        "dbstack"
        "dbstack N"
        "dbstack -COMPLETENAMES"
        "[STACK, IDX] = dbstack (...)"
      ]}
    {:name "dbstatus"
      :signatures [
        "dbstatus"
        "dbstatus FCN"
        "BP_LIST = dbstatus ..."
      ]}
    {:name "dbstep"
      :signatures [
        "dbstep"
        "dbstep N"
        "dbstep in"
        "dbstep out"
        "dbnext ..."
      ]}
    {:name "dbstop"
      :signatures [
        "dbstop FCN"
        "dbstop FCN LINE"
        "dbstop FCN LINE1 LINE2 ..."
        "dbstop LINE1 ..."
        "dbstop in FCN"
        "dbstop in FCN at LINE"
        "dbstop in FCN at LINE if \"CONDITION\""
        "dbstop in CLASS at METHOD"
        "dbstop if EVENT"
        "dbstop if EVENT ID"
        "dbstop (BP_STRUCT)"
        "RLINE = dbstop ..."
      ]}
    {:name "dbtype"
      :signatures [
        "dbtype"
        "dbtype LINENO"
        "dbtype STARTL:ENDL"
        "dbtype STARTL:END"
        "dbtype FCN"
        "dbtype FCN LINENO"
        "dbtype FCN STARTL:ENDL"
        "dbtype FCN STARTL:END"
      ]}
    {:name "dbup"
      :signatures [
        "dbup"
        "dbup N"
      ]}
    {:name "dbwhere"
      :signatures [
        "dbwhere"
      ]}
    {:name "debug_java"
      :signatures [
        "VAL = debug_java ()"
        "OLD_VAL = debug_java (NEW_VAL)"
        "OLD_VAL = debug_java (NEW_VAL, \"local\")"
      ]}
    {:name "debug_on_error"
      :signatures [
        "VAL = debug_on_error ()"
        "OLD_VAL = debug_on_error (NEW_VAL)"
        "OLD_VAL = debug_on_error (NEW_VAL, \"local\")"
      ]}
    {:name "debug_on_interrupt"
      :signatures [
        "VAL = debug_on_interrupt ()"
        "OLD_VAL = debug_on_interrupt (NEW_VAL)"
        "OLD_VAL = debug_on_interrupt (NEW_VAL, \"local\")"
      ]}
    {:name "debug_on_warning"
      :signatures [
        "VAL = debug_on_warning ()"
        "OLD_VAL = debug_on_warning (NEW_VAL)"
        "OLD_VAL = debug_on_warning (NEW_VAL, \"local\")"
      ]}
    {:name "dellistener"
      :signatures [
        "dellistener (H, PROP, FCN)"
      ]}
    {:name "desktop"
      :signatures [
        "desktop ()"
      ]}
    {:name "det"
      :signatures [
        "D = det (A)"
        "[D, RCOND] = det (A)"
      ]}
    {:name "diag"
      :signatures [
        "M = diag (V)"
        "M = diag (V, K)"
        "M = diag (V, M, N)"
        "V = diag (M)"
        "V = diag (M, K)"
      ]}
    {:name "diary"
      :signatures [
        "diary"
        "diary on"
        "diary off"
        "diary FILENAME"
        "[STATUS, DIARYFILE] = diary"
      ]}
    {:name "diff"
      :signatures [
        "Y = diff (X)"
        "Y = diff (X, K)"
        "Y = diff (X, K, DIM)"
      ]}
    {:name "dir_encoding"
      :signatures [
        "CURRENT_ENCODING = dir_encoding (DIR)"
        "dir_encoding (DIR, NEW_ENCODING)"
        "dir_encoding (DIR, \"delete\")"
        "OLD_ENCODING = dir_encoding (DIR, NEW_ENCODING)"
      ]}
    {:name "dir_in_loadpath"
      :signatures [
        "DIRNAME = dir_in_loadpath (DIR)"
        "DIRNAME = dir_in_loadpath (DIR, \"all\")"
      ]}
    {:name "disp"
      :signatures [
        "disp (X)"
        "STR = disp (X)"
      ]}
    {:name "display"
      :signatures [
        "display (OBJ)"
      ]}
    {:name "dlmread"
      :signatures [
        "DATA = dlmread (FILE)"
        "DATA = dlmread (FILE, SEP)"
        "DATA = dlmread (FILE, SEP, R0, C0)"
        "DATA = dlmread (FILE, SEP, RANGE)"
        "DATA = dlmread (..., \"emptyvalue\", EMPTYVAL)"
      ]}
    {:name "dmperm"
      :signatures [
        "P = dmperm (A)"
        "[P, Q, R, S, CC, RR] = dmperm (A)"
      ]}
    {:name "do"
      :signatures [
        "do"
      ]}
    {:name "do_string_escapes"
      :signatures [
        "NEWSTR = do_string_escapes (STRING)"
      ]}
    {:name "doc"
      :signatures [
        "doc FUNCTION_NAME"
        "doc"
      ]}
    {:name "doc_cache_file"
      :signatures [
        "VAL = doc_cache_file ()"
        "OLD_VAL = doc_cache_file (NEW_VAL)"
        "OLD_VAL = doc_cache_file (NEW_VAL, \"local\")"
      ]}
    {:name "dot"
      :signatures [
        "Z = dot (X, Y)"
        "Z = dot (X, Y, DIM)"
      ]}
    {:name "double"
      :signatures [
        "Y = double (X)"
      ]}
    {:name "drawnow"
      :signatures [
        "drawnow ()"
        "drawnow (\"expose\")"
        "drawnow (TERM, FILE, DEBUG_FILE)"
      ]}
    {:name "dup2"
      :signatures [
        "[FID, MSG] = dup2 (OLD, NEW)"
      ]}
    {:name "e"
      :signatures [
        "A = e"
        "A = e (N)"
        "A = e (N, M)"
        "A = e (N, M, K, ...)"
        "A = e (..., CLASS)"
      ]}
    {:name "echo"
      :signatures [
        "echo"
        "echo on"
        "echo off"
        "echo on all"
        "echo off all"
        "echo FUNCTION on"
        "echo FUNCTION off"
      ]}
    {:name "edit_history"
      :signatures [
        "edit_history"
        "edit_history CMD_NUMBER"
        "edit_history FIRST LAST"
      ]}
    {:name "eig"
      :signatures [
        "LAMBDA = eig (A)"
        "LAMBDA = eig (A, B)"
        "[V, LAMBDA] = eig (A)"
        "[V, LAMBDA] = eig (A, B)"
        "[V, LAMBDA, W] = eig (A)"
        "[V, LAMBDA, W] = eig (A, B)"
        "[...] = eig (A, BALANCEOPTION)"
        "[...] = eig (A, B, ALGORITHM)"
        "[...] = eig (..., EIGVALOPTION)"
      ]}
    {:name "ellipj"
      :signatures [
        "[SN, CN, DN, ERR] = ellipj (U, M)"
        "[SN, CN, DN, ERR] = ellipj (U, M, TOL)"
      ]}
    {:name "else"
      :signatures [
        "else"
      ]}
    {:name "elseif"
      :signatures [
        "elseif (COND)"
      ]}
    {:name "end"
      :signatures [
        "end"
      ]}
    {:name "end_try_catch"
      :signatures [
        "end_try_catch"
      ]}
    {:name "end_unwind_protect"
      :signatures [
        "end_unwind_protect"
      ]}
    {:name "endclassdef"
      :signatures [
        "endclassdef"
      ]}
    {:name "endenumeration"
      :signatures [
        "endenumeration"
      ]}
    {:name "endevents"
      :signatures [
        "endevents"
      ]}
    {:name "endfor"
      :signatures [
        "endfor"
      ]}
    {:name "endfunction"
      :signatures [
        "endfunction"
      ]}
    {:name "endgrent"
      :signatures [
        "[STATUS, MSG] = endgrent ()"
      ]}
    {:name "endif"
      :signatures [
        "endif"
      ]}
    {:name "endmethods"
      :signatures [
        "endmethods"
      ]}
    {:name "endparfor"
      :signatures [
        "endparfor"
      ]}
    {:name "endproperties"
      :signatures [
        "endproperties"
      ]}
    {:name "endpwent"
      :signatures [
        "[STATUS, MSG] = endpwent ()"
      ]}
    {:name "endspmd"
      :signatures [
        "endparfor"
      ]}
    {:name "endswitch"
      :signatures [
        "endswitch"
      ]}
    {:name "endwhile"
      :signatures [
        "endwhile"
      ]}
    {:name "enumeration"
      :signatures [
        "enumeration"
      ]}
    {:name "eps"
      :signatures [
        "D = eps"
        "D = eps (X)"
        "D = eps (N, M)"
        "D = eps (N, M, K, ...)"
        "D = eps (..., CLASS)"
      ]}
    {:name "eq"
      :signatures [
        "TF = eq (A, B)"
      ]}
    {:name "erf"
      :signatures [
        "V = erf (Z)"
      ]}
    {:name "erfc"
      :signatures [
        "V = erfc (Z)"
      ]}
    {:name "erfcinv"
      :signatures [
        "Y = erfcinv (X)"
      ]}
    {:name "erfcx"
      :signatures [
        "V = erfcx (Z)"
      ]}
    {:name "erfi"
      :signatures [
        "V = erfi (Z)"
      ]}
    {:name "erfinv"
      :signatures [
        "Y = erfinv (X)"
      ]}
    {:name "errno"
      :signatures [
        "ERR = errno ()"
        "ERR = errno (VAL)"
        "ERR = errno (NAME)"
      ]}
    {:name "errno_list"
      :signatures [
        "S = errno_list ()"
      ]}
    {:name "error"
      :signatures [
        "error (TEMPLATE, ...)"
        "error (ID, TEMPLATE, ...)"
      ]}
    {:name "etree"
      :signatures [
        "P = etree (S)"
        "P = etree (S, TYP)"
        "[P, Q] = etree (S, TYP)"
      ]}
    {:name "eval"
      :signatures [
        "eval (TRY)"
        "eval (TRY, CATCH)"
      ]}
    {:name "evalc"
      :signatures [
        "S = evalc (TRY)"
        "S = evalc (TRY, CATCH)"
      ]}
    {:name "evalin"
      :signatures [
        "evalin (CONTEXT, TRY)"
        "evalin (CONTEXT, TRY, CATCH)"
      ]}
    {:name "events"
      :signatures [
        "events"
      ]}
    {:name "exec"
      :signatures [
        "[ERR, MSG] = exec (FILE, ARGS)"
      ]}
    {:name "exist"
      :signatures [
        "C = exist (NAME)"
        "C = exist (NAME, TYPE)"
      ]}
    {:name "exp"
      :signatures [
        "Y = exp (X)"
      ]}
    {:name "expm1"
      :signatures [
        "Y = expm1 (X)"
      ]}
    {:name "eye"
      :signatures [
        "I = eye (N)"
        "I = eye (M, N)"
        "I = eye ([M N])"
        "I = eye (..., CLASS)"
      ]}
    {:name "false"
      :signatures [
        "VAL = false (X)"
        "VAL = false (N, M)"
        "VAL = false (N, M, K, ...)"
        "VAL = false (..., \"like\", VAR)"
      ]}
    {:name "fclear"
      :signatures [
        "fclear (FID)"
      ]}
    {:name "fclose"
      :signatures [
        "STATUS = fclose (FID)"
        "STATUS = fclose (\"all\")"
      ]}
    {:name "fcntl"
      :signatures [
        "fcntl (FID, REQUEST, ARG)"
        "[STATUS, MSG] = fcntl (FID, REQUEST, ARG)"
      ]}
    {:name "fdisp"
      :signatures [
        "fdisp (FID, X)"
      ]}
    {:name "feof"
      :signatures [
        "STATUS = feof (FID)"
      ]}
    {:name "ferror"
      :signatures [
        "MSG = ferror (FID)"
        "[MSG, ERR] = ferror (FID)"
        "[...] = ferror (FID, \"clear\")"
      ]}
    {:name "feval"
      :signatures [
        "feval (NAME, ...)"
      ]}
    {:name "fflush"
      :signatures [
        "STATUS = fflush (FID)"
      ]}
    {:name "fft"
      :signatures [
        "Y = fft (X)"
        "Y = fft (X, N)"
        "Y = fft (X, N, DIM)"
      ]}
    {:name "fft2"
      :signatures [
        "B = fft2 (A)"
        "B = fft2 (A, M, N)"
      ]}
    {:name "fftn"
      :signatures [
        "B = fftn (A)"
        "B = fftn (A, SIZE)"
      ]}
    {:name "fgetl"
      :signatures [
        "STR = fgetl (FID)"
        "STR = fgetl (FID, LEN)"
      ]}
    {:name "fgets"
      :signatures [
        "STR = fgets (FID)"
        "STR = fgets (FID, LEN)"
      ]}
    {:name "file_in_loadpath"
      :signatures [
        "FNAME = file_in_loadpath (FILE)"
        "FNAME = file_in_loadpath (FILE, \"all\")"
      ]}
    {:name "file_in_path"
      :signatures [
        "FNAME = file_in_path (PATH, FILE)"
        "FNAME = file_in_path (PATH, FILE, \"all\")"
      ]}
    {:name "filebrowser"
      :signatures [
        "filebrowser ()"
      ]}
  ])

(def ^:private +core-3+
  "Chunk 3 of Octave builtins from the `core` category, with signatures."
  [
    {:name "filesep"
      :signatures [
        "SEP = filesep ()"
        "filesep (\"all\")"
      ]}
    {:name "filter"
      :signatures [
        "Y = filter (B, A, X)"
        "[Y, SF] = filter (B, A, X, SI)"
        "[Y, SF] = filter (B, A, X, [], DIM)"
        "[Y, SF] = filter (B, A, X, SI, DIM)"
      ]}
    {:name "find"
      :signatures [
        "IDX = find (X)"
        "IDX = find (X, N)"
        "IDX = find (X, N, DIRECTION)"
        "[i, j] = find (...)"
        "[i, j, v] = find (...)"
      ]}
    {:name "fix"
      :signatures [
        "Y = fix (X)"
      ]}
    {:name "fixed_point_format"
      :signatures [
        "VAL = fixed_point_format ()"
        "OLD_VAL = fixed_point_format (NEW_VAL)"
        "OLD_VAL = fixed_point_format (NEW_VAL, \"local\")"
      ]}
    {:name "flintmax"
      :signatures [
        "IMAX = flintmax ()"
        "IMAX = flintmax (\"double\")"
        "IMAX = flintmax (\"single\")"
        "IMAX = flintmax (VAR)"
      ]}
    {:name "floor"
      :signatures [
        "Y = floor (X)"
      ]}
    {:name "fopen"
      :signatures [
        "FID = fopen (NAME)"
        "FID = fopen (NAME, MODE)"
        "FID = fopen (NAME, MODE, ARCH)"
        "FID = fopen (NAME, MODE, ARCH, ENCODING)"
        "[FID, MSG] = fopen (...)"
        "FID_LIST = fopen (\"all\")"
        "[FILE, MODE, ARCH, ENCODING] = fopen (FID)"
      ]}
    {:name "for"
      :signatures [
        "for I = RANGE"
      ]}
    {:name "fork"
      :signatures [
        "[PID, MSG] = fork ()"
      ]}
    {:name "format"
      :signatures [
        "format"
        "format options"
        "format (OPTIONS)"
        "[FORMAT, FORMATSPACING, UPPERCASE] = format"
      ]}
    {:name "fprintf"
      :signatures [
        "fprintf (FID, TEMPLATE, ...)"
        "fprintf (TEMPLATE, ...)"
        "NUMBYTES = fprintf (...)"
      ]}
    {:name "fputs"
      :signatures [
        "STATUS = fputs (FID, STRING)"
      ]}
    {:name "fread"
      :signatures [
        "VAL = fread (FID)"
        "VAL = fread (FID, SIZE)"
        "VAL = fread (FID, SIZE, PRECISION)"
        "VAL = fread (FID, SIZE, PRECISION, SKIP)"
        "VAL = fread (FID, SIZE, PRECISION, SKIP, ARCH)"
        "[VAL, COUNT] = fread (...)"
      ]}
    {:name "freport"
      :signatures [
        "freport ()"
      ]}
    {:name "frewind"
      :signatures [
        "frewind (FID)"
        "STATUS = frewind (FID)"
      ]}
    {:name "fscanf"
      :signatures [
        "[VAL, COUNT, ERRMSG] = fscanf (FID, TEMPLATE, SIZE)"
        "[V1, V2, ..., COUNT, ERRMSG] = fscanf (FID, TEMPLATE, \"C\")"
      ]}
    {:name "fseek"
      :signatures [
        "STATUS = fseek (FID, OFFSET)"
        "STATUS = fseek (FID, OFFSET, ORIGIN)"
      ]}
    {:name "fskipl"
      :signatures [
        "NLINES = fskipl (FID)"
        "NLINES = fskipl (FID, COUNT)"
        "NLINES = fskipl (FID, Inf)"
      ]}
    {:name "ftell"
      :signatures [
        "POS = ftell (FID)"
      ]}
    {:name "full"
      :signatures [
        "FM = full (SM)"
      ]}
    {:name "func2str"
      :signatures [
        "STR = func2str (FCN_HANDLE)"
      ]}
    {:name "function"
      :signatures [
        "function OUTPUTS = function_name (INPUT, ...)"
        "function function_name (INPUT, ...)"
        "function OUTPUTS = function_name"
      ]}
    {:name "functions"
      :signatures [
        "S = functions (FCN_HANDLE)"
      ]}
    {:name "fwrite"
      :signatures [
        "COUNT = fwrite (FID, DATA)"
        "COUNT = fwrite (FID, DATA, PRECISION)"
        "COUNT = fwrite (FID, DATA, PRECISION, SKIP)"
        "COUNT = fwrite (FID, DATA, PRECISION, SKIP, ARCH)"
      ]}
    {:name "gamma"
      :signatures [
        "V = gamma (Z)"
      ]}
    {:name "gcd"
      :signatures [
        "G = gcd (A1, A2, ...)"
        "[G, V1, ...] = gcd (A1, A2, ...)"
      ]}
    {:name "ge"
      :signatures [
        "TF = ge (A, B)"
      ]}
    {:name "genpath"
      :signatures [
        "PATHSTR = genpath (DIR)"
        "PATHSTR = genpath (DIR, SKIPDIR1, ...)"
      ]}
    {:name "get"
      :signatures [
        "VAL = get (H)"
        "VAL = get (H, P)"
      ]}
    {:name "get_help_text"
      :signatures [
        "[TEXT, FORMAT] = get_help_text (NAME)"
      ]}
    {:name "get_help_text_from_file"
      :signatures [
        "[TEXT, FORMAT] = get_help_text_from_file (FNAME)"
      ]}
    {:name "get_home_directory"
      :signatures [
        "HOMEDIR = get_home_directory ()"
      ]}
    {:name "getegid"
      :signatures [
        "egid = getegid ()"
      ]}
    {:name "getenv"
      :signatures [
        "VAL = getenv (\"VAR\")"
      ]}
    {:name "geteuid"
      :signatures [
        "euid = geteuid ()"
      ]}
    {:name "getgid"
      :signatures [
        "gid = getgid ()"
      ]}
    {:name "getgrent"
      :signatures [
        "GRP_STRUCT = getgrent ()"
      ]}
    {:name "getgrgid"
      :signatures [
        "GRP_STRUCT = getgrgid (GID)."
      ]}
    {:name "getgrnam"
      :signatures [
        "GRP_STRUCT = getgrnam (NAME)"
      ]}
    {:name "gethostname"
      :signatures [
        "NAME = gethostname ()"
      ]}
    {:name "getpgrp"
      :signatures [
        "pgid = getpgrp ()"
      ]}
    {:name "getpid"
      :signatures [
        "pid = getpid ()"
      ]}
    {:name "getppid"
      :signatures [
        "pid = getppid ()"
      ]}
    {:name "getpwent"
      :signatures [
        "PW_STRUCT = getpwent ()"
      ]}
    {:name "getpwnam"
      :signatures [
        "PW_STRUCT = getpwnam (NAME)"
      ]}
    {:name "getpwuid"
      :signatures [
        "PW_STRUCT = getpwuid (UID)."
      ]}
    {:name "getrusage"
      :signatures [
        "PROCSTATS = getrusage ()"
      ]}
    {:name "getuid"
      :signatures [
        "uid = getuid ()"
      ]}
    {:name "givens"
      :signatures [
        "G = givens (X, Y)"
        "[C, S] = givens (X, Y)"
      ]}
    {:name "glob"
      :signatures [
        "CSTR = glob (PATTERN)"
      ]}
    {:name "global"
      :signatures [
        "global VAR"
      ]}
    {:name "gmtime"
      :signatures [
        "TM_STRUCT = gmtime (T)"
      ]}
    {:name "gsvd"
      :signatures [
        "S = gsvd (A, B)"
        "[U, V, X, C, S] = gsvd (A, B)"
        "[U, V, X, C, S] = gsvd (A, B, 0)"
      ]}
    {:name "gt"
      :signatures [
        "TF = gt (A, B)"
      ]}
    {:name "hash"
      :signatures [
        "HASHVAL = hash (\"HASHFCN\", STR)"
      ]}
    {:name "have_window_system"
      :signatures [
        "TF = have_window_system ()"
      ]}
    {:name "hess"
      :signatures [
        "H = hess (A)"
        "[P, H] = hess (A)"
      ]}
    {:name "hex2num"
      :signatures [
        "N = hex2num (S)"
        "N = hex2num (S, CLASS)"
      ]}
    {:name "history"
      :signatures [
        "history"
        "history OPT1 ..."
        "H = history ()"
        "H = history (OPT1, ...)"
      ]}
    {:name "history_control"
      :signatures [
        "VAL = history_control ()"
        "OLD_VAL = history_control (NEW_VAL)"
      ]}
    {:name "history_file"
      :signatures [
        "VAL = history_file ()"
        "OLD_VAL = history_file (NEW_VAL)"
      ]}
    {:name "history_save"
      :signatures [
        "VAL = history_save ()"
        "OLD_VAL = history_save (NEW_VAL)"
        "OLD_VAL = history_save (NEW_VAL, \"local\")"
      ]}
    {:name "history_size"
      :signatures [
        "VAL = history_size ()"
        "OLD_VAL = history_size (NEW_VAL)"
      ]}
    {:name "history_timestamp_format_string"
      :signatures [
        "VAL = history_timestamp_format_string ()"
        "OLD_VAL = history_timestamp_format_string (NEW_VAL)"
        "OLD_VAL = history_timestamp_format_string (NEW_VAL, \"local\")"
      ]}
    {:name "horzcat"
      :signatures [
        "A = horzcat (ARRAY1, ARRAY2, ..., ARRAYN)"
      ]}
    {:name "hypot"
      :signatures [
        "H = hypot (X, Y)"
        "H = hypot (X, Y, Z, ...)"
      ]}
    {:name "if"
      :signatures [
        "if (COND) ... endif"
        "if (COND) ... else ... endif"
        "if (COND) ... elseif (COND) ... endif"
        "if (COND) ... elseif (COND) ... else ... endif"
      ]}
    {:name "ifft"
      :signatures [
        "X = ifft (Y)"
        "X = ifft (Y, N)"
        "X = ifft (Y, N, DIM)"
      ]}
    {:name "ifft2"
      :signatures [
        "A = ifft2 (B)"
        "A = ifft2 (B, M, N)"
      ]}
    {:name "ifftn"
      :signatures [
        "A = ifftn (B)"
        "A = ifftn (B, SIZE)"
      ]}
    {:name "ignore_function_time_stamp"
      :signatures [
        "VAL = ignore_function_time_stamp ()"
        "OLD_VAL = ignore_function_time_stamp (NEW_VAL)"
      ]}
    {:name "imag"
      :signatures [
        "Y = imag (Z)"
      ]}
    {:name "ind2sub"
      :signatures [
        "[S1, S2, ..., SN] = ind2sub (DIMS, IND)"
      ]}
    {:name "inferiorto"
      :signatures [
        "inferiorto (CLASS_NAME, ...)"
      ]}
    {:name "info_file"
      :signatures [
        "VAL = info_file ()"
        "OLD_VAL = info_file (NEW_VAL)"
        "OLD_VAL = info_file (NEW_VAL, \"local\")"
      ]}
    {:name "info_program"
      :signatures [
        "VAL = info_program ()"
        "OLD_VAL = info_program (NEW_VAL)"
        "OLD_VAL = info_program (NEW_VAL, \"local\")"
      ]}
    {:name "input"
      :signatures [
        "ANS = input (PROMPT)"
        "ANS = input (PROMPT, \"s\")"
      ]}
    {:name "int16"
      :signatures [
        "Y = int16 (X)"
      ]}
    {:name "int32"
      :signatures [
        "Y = int32 (X)"
      ]}
    {:name "int64"
      :signatures [
        "Y = int64 (X)"
      ]}
    {:name "int8"
      :signatures [
        "Y = int8 (X)"
      ]}
    {:name "intmax"
      :signatures [
        "IMAX = intmax ()"
        "IMAX = intmax (\"TYPE\")"
        "IMAX = intmax (VAR)"
      ]}
    {:name "intmin"
      :signatures [
        "IMIN = intmin ()"
        "IMIN = intmin (\"TYPE\")"
        "IMIN = intmin (VAR)"
      ]}
    {:name "inv"
      :signatures [
        "X = inv (A)"
        "[X, RCOND] = inv (A)"
        "[...] = inverse (...)"
      ]}
    {:name "ipermute"
      :signatures [
        "A = ipermute (B, IPERM)"
      ]}
    {:name "is_absolute_filename"
      :signatures [
        "TF = is_absolute_filename (FILE)"
      ]}
    {:name "is_dq_string"
      :signatures [
        "TF = is_dq_string (X)"
      ]}
    {:name "is_function_handle"
      :signatures [
        "TF = is_function_handle (X)"
      ]}
    {:name "is_rooted_relative_filename"
      :signatures [
        "TF = is_rooted_relative_filename (FILE)"
      ]}
    {:name "is_same_file"
      :signatures [
        "SAME = is_same_file (FILEPATH1, FILEPATH2)"
      ]}
    {:name "is_sq_string"
      :signatures [
        "TF = is_sq_string (X)"
      ]}
    {:name "isa"
      :signatures [
        "TF = isa (OBJ, CLASSNAME)"
      ]}
    {:name "isalnum"
      :signatures [
        "TF = isalnum (S)"
      ]}
    {:name "isalpha"
      :signatures [
        "TF = isalpha (S)"
      ]}
    {:name "isargout"
      :signatures [
        "TF = isargout (K)"
      ]}
    {:name "isascii"
      :signatures [
        "TF = isascii (S)"
      ]}
    {:name "iscell"
      :signatures [
        "TF = iscell (X)"
      ]}
    {:name "iscellstr"
      :signatures [
        "TF = iscellstr (CELL)"
      ]}
    {:name "ischar"
      :signatures [
        "TF = ischar (X)"
      ]}
    {:name "iscntrl"
      :signatures [
        "TF = iscntrl (S)"
      ]}
    {:name "iscolumn"
      :signatures [
        "TF = iscolumn (X)"
      ]}
    {:name "iscomplex"
      :signatures [
        "TF = iscomplex (X)"
      ]}
    {:name "isdebugmode"
      :signatures [
        "TF = isdebugmode ()"
      ]}
    {:name "isdigit"
      :signatures [
        "TF = isdigit (S)"
      ]}
    {:name "isempty"
      :signatures [
        "TF = isempty (A)"
      ]}
    {:name "isfield"
      :signatures [
        "TF = isfield (X, \"NAME\")"
        "TF = isfield (X, NAME)"
      ]}
    {:name "isfinite"
      :signatures [
        "TF = isfinite (X)"
      ]}
    {:name "isfloat"
      :signatures [
        "TF = isfloat (X)"
      ]}
    {:name "isglobal"
      :signatures [
        "TF = isglobal (NAME)"
      ]}
    {:name "isgraph"
      :signatures [
        "TF = isgraph (S)"
      ]}
    {:name "isguirunning"
      :signatures [
        "TF = isguirunning ()"
      ]}
    {:name "ishghandle"
      :signatures [
        "TF = ishghandle (H)"
      ]}
    {:name "isieee"
      :signatures [
        "TF = isieee ()"
      ]}
    {:name "isindex"
      :signatures [
        "TF = isindex (IND)"
        "TF = isindex (IND, N)"
      ]}
    {:name "isinf"
      :signatures [
        "TF = isinf (X)"
      ]}
    {:name "isinteger"
      :signatures [
        "TF = isinteger (X)"
      ]}
    {:name "isjava"
      :signatures [
        "TF = isjava (X)"
      ]}
    {:name "iskeyword"
      :signatures [
        "iskeyword ()"
        "iskeyword (NAME)"
      ]}
    {:name "islogical"
      :signatures [
        "TF = islogical (X)"
        "TF = isbool (X)"
      ]}
    {:name "islower"
      :signatures [
        "TF = islower (S)"
      ]}
    {:name "ismatrix"
      :signatures [
        "TF = ismatrix (X)"
      ]}
    {:name "isna"
      :signatures [
        "TF = isna (X)"
      ]}
    {:name "isnan"
      :signatures [
        "TF = isnan (X)"
      ]}
    {:name "isnull"
      :signatures [
        "TF = isnull (X)"
      ]}
    {:name "isnumeric"
      :signatures [
        "TF = isnumeric (X)"
      ]}
    {:name "isobject"
      :signatures [
        "TF = isobject (X)"
      ]}
    {:name "isprint"
      :signatures [
        "TF = isprint (S)"
      ]}
    {:name "ispunct"
      :signatures [
        "TF = ispunct (S)"
      ]}
    {:name "isreal"
      :signatures [
        "TF = isreal (X)"
      ]}
    {:name "isrow"
      :signatures [
        "TF = isrow (X)"
      ]}
    {:name "isscalar"
      :signatures [
        "TF = isscalar (X)"
      ]}
    {:name "issorted"
      :signatures [
        "TF = issorted (A)"
        "TF = issorted (A, MODE)"
        "TF = issorted (A, \"rows\", MODE)"
      ]}
    {:name "isspace"
      :signatures [
        "TF = isspace (S)"
      ]}
    {:name "issparse"
      :signatures [
        "TF = issparse (X)"
      ]}
    {:name "issquare"
      :signatures [
        "TF = issquare (X)"
      ]}
    {:name "isstruct"
      :signatures [
        "TF = isstruct (X)"
      ]}
    {:name "isstudent"
      :signatures [
        "TF = isstudent ()"
      ]}
    {:name "isupper"
      :signatures [
        "TF = isupper (S)"
      ]}
    {:name "isvarname"
      :signatures [
        "TF = isvarname (NAME)"
      ]}
    {:name "isvector"
      :signatures [
        "TF = isvector (X)"
      ]}
    {:name "isxdigit"
      :signatures [
        "TF = isxdigit (S)"
      ]}
    {:name "javaMethod"
      :signatures [
        "RET = javaMethod (METHODNAME, OBJ)"
        "RET = javaMethod (METHODNAME, OBJ, ARG1, ...)"
      ]}
    {:name "javaObject"
      :signatures [
        "JOBJ = javaObject (CLASSNAME)"
        "JOBJ = javaObject (CLASSNAME, ARG1, ...)"
      ]}
    {:name "java_matrix_autoconversion"
      :signatures [
        "VAL = java_matrix_autoconversion ()"
        "OLD_VAL = java_matrix_autoconversion (NEW_VAL)"
        "OLD_VAL = java_matrix_autoconversion (NEW_VAL, \"local\")"
      ]}
    {:name "java_unsigned_autoconversion"
      :signatures [
        "VAL = java_unsigned_autoconversion ()"
        "OLD_VAL = java_unsigned_autoconversion (NEW_VAL)"
        "OLD_VAL = java_unsigned_autoconversion (NEW_VAL, \"local\")"
      ]}
    {:name "jsondecode"
      :signatures [
        "OBJECT = jsondecode (JSON_TXT)"
        "OBJECT = jsondecode (..., \"ReplacementStyle\", RS)"
        "OBJECT = jsondecode (..., \"Prefix\", PFX)"
        "OBJECT = jsondecode (..., \"makeValidName\", TF)"
      ]}
    {:name "jsonencode"
      :signatures [
        "JSON_TXT = jsonencode (OBJECT)"
        "JSON_TXT = jsonencode (..., \"ConvertInfAndNaN\", TF)"
        "JSON_TXT = jsonencode (..., \"PrettyPrint\", TF)"
      ]}
    {:name "kbhit"
      :signatures [
        "C = kbhit ()"
        "C = kbhit (1)"
      ]}
    {:name "keyboard"
      :signatures [
        "keyboard ()"
        "keyboard (\"PROMPT\")"
      ]}
  ])

(def ^:private +core-4+
  "Chunk 4 of Octave builtins from the `core` category, with signatures."
  [
    {:name "kill"
      :signatures [
        "kill (PID, SIG)"
        "[STATUS, MSG] = kill (PID, SIG)"
      ]}
    {:name "kron"
      :signatures [
        "C = kron (A, B)"
        "C = kron (A1, A2, ...)"
      ]}
    {:name "lasterr"
      :signatures [
        "[MSG, MSGID] = lasterr ()"
        "lasterr (MSG)"
        "lasterr (MSG, MSGID)"
      ]}
    {:name "lasterror"
      :signatures [
        "LASTERR = lasterror ()"
        "lasterror (ERR)"
        "lasterror (\"reset\")"
      ]}
    {:name "lastwarn"
      :signatures [
        "[MSG, MSGID] = lastwarn ()"
        "lastwarn (MSG)"
        "lastwarn (MSG, MSGID)"
      ]}
    {:name "ldivide"
      :signatures [
        "C = ldivide (A, B)"
      ]}
    {:name "le"
      :signatures [
        "TF = le (A, B)"
      ]}
    {:name "length"
      :signatures [
        "N = length (A)"
      ]}
    {:name "lgamma"
      :signatures [
        "Y = gammaln (X)"
        "Y = lgamma (X)"
      ]}
    {:name "line"
      :signatures [
        "line ()"
        "line (X, Y)"
        "line (X, Y, Z)"
        "line (\"xdata\", X, \"ydata\", Y)"
        "line (\"xdata\", X, \"ydata\", Y, \"zdata\", Z)"
        "line (..., PROPERTY, VALUE)"
        "line (HAX, ...)"
        "H = line (...)"
      ]}
    {:name "lines"
      :signatures [
        "MAP = lines ()"
        "MAP = lines (N)"
      ]}
    {:name "link"
      :signatures [
        "link OLD NEW"
        "[STATUS, MSG] = link (OLD, NEW)"
      ]}
    {:name "linspace"
      :signatures [
        "Y = linspace (START, END)"
        "Y = linspace (START, END, N)"
      ]}
    {:name "list_in_columns"
      :signatures [
        "STR = list_in_columns (ARG, WIDTH, PREFIX)"
      ]}
    {:name "load"
      :signatures [
        "load file"
        "load options file"
        "load options file v1 v2 ..."
        "S = load (\"options\", \"file\", \"v1\", \"v2\", ...)"
        "load file options"
        "load file options v1 v2 ..."
        "S = load (\"file\", \"options\", \"v1\", \"v2\", ...)"
      ]}
    {:name "loaded_graphics_toolkits"
      :signatures [
        "TOOLKITS = loaded_graphics_toolkits ()"
      ]}
    {:name "localfunctions"
      :signatures [
        "SUBFCN_LIST = localfunctions ()"
      ]}
    {:name "localtime"
      :signatures [
        "TM_STRUCT = localtime (T)"
      ]}
    {:name "log"
      :signatures [
        "Y = log (X)"
      ]}
    {:name "log10"
      :signatures [
        "Y = log10 (X)"
      ]}
    {:name "log1p"
      :signatures [
        "Y = log1p (X)"
      ]}
    {:name "log2"
      :signatures [
        "Y = log2 (X)"
        "[F, E] = log2 (X)"
      ]}
    {:name "logical"
      :signatures [
        "TF = logical (X)"
      ]}
    {:name "lookup"
      :signatures [
        "IDX = lookup (TABLE, Y)"
        "IDX = lookup (TABLE, Y, OPT)"
      ]}
    {:name "lsode"
      :signatures [
        "[X, ISTATE, MSG] = lsode (FCN, X_0, T)"
        "[X, ISTATE, MSG] = lsode (FCN, X_0, T, T_CRIT)"
      ]}
    {:name "lsode_options"
      :signatures [
        "lsode_options ()"
        "val = lsode_options (OPT)"
        "lsode_options (OPT, VAL)"
      ]}
    {:name "lstat"
      :signatures [
        "INFO = lstat (SYMLINK)"
        "[INFO, ERR, MSG] = lstat (SYMLINK)"
      ]}
    {:name "lt"
      :signatures [
        "TF = lt (A, B)"
      ]}
    {:name "lu"
      :signatures [
        "[L, U] = lu (A)"
        "[L, U, P] = lu (A)"
        "[L, U, P, Q] = lu (S)"
        "[L, U, P, Q, R] = lu (S)"
        "[...] = lu (S, THRESH)"
        "Y = lu (...)"
        "[...] = lu (..., \"vector\")"
      ]}
    {:name "luupdate"
      :signatures [
        "[L, U] = luupdate (L, U, X, Y)"
        "[L, U, P] = luupdate (L, U, P, X, Y)"
      ]}
    {:name "make_absolute_filename"
      :signatures [
        "ABS_FNAME = make_absolute_filename (FILE)"
      ]}
    {:name "makeinfo_program"
      :signatures [
        "VAL = makeinfo_program ()"
        "OLD_VAL = makeinfo_program (NEW_VAL)"
        "OLD_VAL = makeinfo_program (NEW_VAL, \"local\")"
      ]}
    {:name "mat2cell"
      :signatures [
        "C = mat2cell (A, DIM1, DIM2, ..., DIMI, ..., DIMN)"
        "C = mat2cell (A, ROWDIM)"
      ]}
    {:name "matrix_type"
      :signatures [
        "TYPE = matrix_type (A)"
        "TYPE = matrix_type (A, \"nocompute\")"
        "A = matrix_type (A, TYPE)"
        "A = matrix_type (A, \"upper\", PERM)"
        "A = matrix_type (A, \"lower\", PERM)"
        "A = matrix_type (A, \"banded\", NL, NU)"
      ]}
    {:name "max"
      :signatures [
        "M = max (X)"
        "M = max (X, [], DIM)"
        "[M, IM] = max (X)"
        "M = max (X, Y)"
      ]}
    {:name "max_recursion_depth"
      :signatures [
        "VAL = max_recursion_depth ()"
        "OLD_VAL = max_recursion_depth (NEW_VAL)"
        "OLD_VAL = max_recursion_depth (NEW_VAL, \"local\")"
      ]}
    {:name "max_stack_depth"
      :signatures [
        "VAL = max_stack_depth ()"
        "OLD_VAL = max_stack_depth (NEW_VAL)"
        "OLD_VAL = max_stack_depth (NEW_VAL, \"local\")"
      ]}
    {:name "merge"
      :signatures [
        "M = merge (MASK, TVAL, FVAL)"
        "M = ifelse (MASK, TVAL, FVAL)"
      ]}
    {:name "metaclass"
      :signatures [
        "METACLASS_OBJ = metaclass (obj)"
      ]}
    {:name "mfilename"
      :signatures [
        "mfilename ()"
        "mfilename (\"fullpath\")"
        "mfilename (\"fullpathext\")"
      ]}
    {:name "mgorth"
      :signatures [
        "[Y, H] = mgorth (X, V)"
      ]}
    {:name "min"
      :signatures [
        "M = min (X)"
        "M = min (X, [], DIM)"
        "[M, IM] = min (X)"
        "M = min (X, Y)"
      ]}
    {:name "minus"
      :signatures [
        "C = minus (A, B)"
      ]}
    {:name "mislocked"
      :signatures [
        "TF = mislocked ()"
        "TF = mislocked (FCN)"
      ]}
    {:name "missing_component_hook"
      :signatures [
        "VAL = missing_component_hook ()"
        "OLD_VAL = missing_component_hook (NEW_VAL)"
        "OLD_VAL = missing_component_hook (NEW_VAL, \"local\")"
      ]}
    {:name "missing_function_hook"
      :signatures [
        "VAL = missing_function_hook ()"
        "OLD_VAL = missing_function_hook (NEW_VAL)"
        "OLD_VAL = missing_function_hook (NEW_VAL, \"local\")"
      ]}
    {:name "mkfifo"
      :signatures [
        "mkfifo (NAME, MODE)"
        "[STATUS, MSG] = mkfifo (NAME, MODE)"
      ]}
    {:name "mkstemp"
      :signatures [
        "[FID, NAME, MSG] = mkstemp (\"TEMPLATE\")"
        "[FID, NAME, MSG] = mkstemp (\"TEMPLATE\", DELETE)"
      ]}
    {:name "mktime"
      :signatures [
        "SECONDS = mktime (TM_STRUCT)"
      ]}
    {:name "mldivide"
      :signatures [
        "C = mldivide (A, B)"
      ]}
    {:name "mlock"
      :signatures [
        "mlock ()"
      ]}
    {:name "mod"
      :signatures [
        "M = mod (X, Y)"
      ]}
    {:name "more"
      :signatures [
        "more"
        "more on"
        "more off"
      ]}
    {:name "mpower"
      :signatures [
        "C = mpower (A, B)"
      ]}
    {:name "mrdivide"
      :signatures [
        "C = mrdivide (A, B)"
      ]}
    {:name "mtimes"
      :signatures [
        "C = mtimes (A, B)"
        "C = mtimes (A1, A2, ...)"
      ]}
    {:name "munlock"
      :signatures [
        "munlock ()"
        "munlock (FCN)"
      ]}
    {:name "nargin"
      :signatures [
        "N = nargin ()"
        "N = nargin (FCN)"
      ]}
    {:name "nargout"
      :signatures [
        "N = nargout ()"
        "N = nargout (FCN)"
      ]}
    {:name "native_float_format"
      :signatures [
        "FMTSTR = native_float_format ()"
      ]}
    {:name "ndims"
      :signatures [
        "N = ndims (A)"
      ]}
    {:name "ne"
      :signatures [
        "TF = ne (A, B)"
      ]}
    {:name "newline"
      :signatures [
        "C = newline"
      ]}
    {:name "nnz"
      :signatures [
        "N = nnz (A)"
      ]}
    {:name "norm"
      :signatures [
        "N = norm (A)"
        "N = norm (A, P)"
        "N = norm (A, P, OPT)"
      ]}
    {:name "not"
      :signatures [
        "Z = not (X)"
      ]}
    {:name "nproc"
      :signatures [
        "N = nproc ()"
        "N = nproc (QUERY)"
      ]}
    {:name "nth_element"
      :signatures [
        "NEL = nth_element (X, N)"
        "NEL = nth_element (X, N, DIM)"
      ]}
    {:name "num2cell"
      :signatures [
        "C = num2cell (A)"
        "C = num2cell (A, DIM)"
      ]}
    {:name "num2hex"
      :signatures [
        "S = num2hex (N)"
        "S = num2hex (N, \"cell\")"
      ]}
    {:name "numel"
      :signatures [
        "N = numel (A)"
        "N = numel (A, IDX1, IDX2, ...)"
      ]}
    {:name "numfields"
      :signatures [
        "N = numfields (S)"
      ]}
    {:name "nzmax"
      :signatures [
        "N = nzmax (SM)"
      ]}
    {:name "octave_core_file_limit"
      :signatures [
        "VAL = octave_core_file_limit ()"
        "OLD_VAL = octave_core_file_limit (NEW_VAL)"
        "OLD_VAL = octave_core_file_limit (NEW_VAL, \"local\")"
      ]}
    {:name "octave_core_file_name"
      :signatures [
        "VAL = octave_core_file_name ()"
        "OLD_VAL = octave_core_file_name (NEW_VAL)"
        "OLD_VAL = octave_core_file_name (NEW_VAL, \"local\")"
      ]}
    {:name "octave_core_file_options"
      :signatures [
        "VAL = octave_core_file_options ()"
        "OLD_VAL = octave_core_file_options (NEW_VAL)"
        "OLD_VAL = octave_core_file_options (NEW_VAL, \"local\")"
      ]}
    {:name "onCleanup"
      :signatures [
        "OBJ = onCleanup (FUNCTION)"
      ]}
    {:name "ones"
      :signatures [
        "VAL = ones (N)"
        "VAL = ones (M, N)"
        "VAL = ones (M, N, K, ...)"
        "VAL = ones ([M N ...])"
        "VAL = ones (..., \"like\", VAR)"
        "VAL = ones (..., CLASS)"
      ]}
    {:name "openvar"
      :signatures [
        "openvar (NAME)"
      ]}
    {:name "optimize_diagonal_matrix"
      :signatures [
        "VAL = optimize_diagonal_matrix ()"
        "OLD_VAL = optimize_diagonal_matrix (NEW_VAL)"
        "OLD_VAL = optimize_diagonal_matrix (NEW_VAL, \"local\")"
      ]}
    {:name "optimize_permutation_matrix"
      :signatures [
        "VAL = optimize_permutation_matrix ()"
        "OLD_VAL = optimize_permutation_matrix (NEW_VAL)"
        "OLD_VAL = optimize_permutation_matrix (NEW_VAL, \"local\")"
      ]}
    {:name "optimize_range"
      :signatures [
        "VAL = optimize_range ()"
        "OLD_VAL = optimize_range (NEW_VAL)"
        "OLD_VAL = optimize_range (NEW_VAL, \"local\")"
      ]}
    {:name "optimize_subsasgn_calls"
      :signatures [
        "VAL = optimize_subsasgn_calls ()"
        "OLD_VAL = optimize_subsasgn_calls (NEW_VAL)"
        "OLD_VAL = optimize_subsasgn_calls (NEW_VAL, \"local\")"
      ]}
    {:name "or"
      :signatures [
        "TF = or (X, Y)"
        "TF = or (X1, X2, ...)"
      ]}
    {:name "ordqz"
      :signatures [
        "[AR, BR, QR, ZR] = ordqz (AA, BB, Q, Z, KEYWORD)"
        "[AR, BR, QR, ZR] = ordqz (AA, BB, Q, Z, SELECT)"
      ]}
    {:name "ordschur"
      :signatures [
        "[UR, SR] = ordschur (U, S, SELECT)"
      ]}
    {:name "otherwise"
      :signatures [
        "otherwise"
      ]}
    {:name "output_precision"
      :signatures [
        "VAL = output_precision ()"
        "OLD_VAL = output_precision (NEW_VAL)"
        "OLD_VAL = output_precision (NEW_VAL, \"local\")"
      ]}
    {:name "page_output_immediately"
      :signatures [
        "VAL = page_output_immediately ()"
        "OLD_VAL = page_output_immediately (NEW_VAL)"
        "OLD_VAL = page_output_immediately (NEW_VAL, \"local\")"
      ]}
    {:name "page_screen_output"
      :signatures [
        "VAL = page_screen_output ()"
        "OLD_VAL = page_screen_output (NEW_VAL)"
        "OLD_VAL = page_screen_output (NEW_VAL, \"local\")"
      ]}
    {:name "parfor"
      :signatures [
        "parfor I = RANGE"
        "parfor (I = RANGE, MAXPROC)"
      ]}
    {:name "path"
      :signatures [
        "path ()"
        "STR = path ()"
        "STR = path (PATH1, ...)"
      ]}
    {:name "pathsep"
      :signatures [
        "VAL = pathsep ()"
      ]}
    {:name "pause"
      :signatures [
        "pause ()"
        "pause (N)"
        "OLD_STATE = pause (\"on\")"
        "OLD_STATE = pause (\"off\")"
        "OLD_STATE = pause (\"query\")"
      ]}
    {:name "pclose"
      :signatures [
        "STATUS = pclose (FID)"
      ]}
    {:name "permute"
      :signatures [
        "B = permute (A, PERM)"
      ]}
    {:name "persistent"
      :signatures [
        "persistent VAR"
      ]}
    {:name "pi"
      :signatures [
        "P = pi"
        "P = pi (N)"
        "P = pi (N, M)"
        "P = pi (N, M, K, ...)"
        "P = pi (..., CLASS)"
      ]}
    {:name "pinv"
      :signatures [
        "B = pinv (A)"
        "B = pinv (A, TOL)"
      ]}
    {:name "pipe"
      :signatures [
        "[READ_FD, WRITE_FD, ERR, MSG] = pipe ()"
      ]}
    {:name "plus"
      :signatures [
        "C = plus (A, B)"
        "C = plus (A1, A2, ...)"
      ]}
    {:name "popen"
      :signatures [
        "FID = popen (COMMAND, MODE)"
      ]}
    {:name "popen2"
      :signatures [
        "[IN, OUT, PID] = popen2 (COMMAND, ARGS)"
      ]}
    {:name "pow2"
      :signatures [
        "Y = pow2 (X)"
        "Y = pow2 (F, E)"
      ]}
    {:name "power"
      :signatures [
        "C = power (A, B)"
      ]}
    {:name "print_empty_dimensions"
      :signatures [
        "VAL = print_empty_dimensions ()"
        "OLD_VAL = print_empty_dimensions (NEW_VAL)"
        "OLD_VAL = print_empty_dimensions (NEW_VAL, \"local\")"
      ]}
    {:name "print_struct_array_contents"
      :signatures [
        "VAL = print_struct_array_contents ()"
        "OLD_VAL = print_struct_array_contents (NEW_VAL)"
        "OLD_VAL = print_struct_array_contents (NEW_VAL, \"local\")"
      ]}
    {:name "printf"
      :signatures [
        "printf (TEMPLATE, ...)"
        "NUMBYTES = printf (...)"
      ]}
    {:name "prod"
      :signatures [
        "Y = prod (X)"
        "Y = prod (X, DIM)"
        "Y = prod (..., \"native\")"
        "Y = prod (..., \"double\")"
      ]}
    {:name "program_invocation_name"
      :signatures [
        "NAME = program_invocation_name ()"
      ]}
    {:name "program_name"
      :signatures [
        "NAME = program_name ()"
      ]}
    {:name "properties"
      :signatures [
        "properties (OBJ)"
        "properties (CLASS_NAME)"
        "PROPLIST = properties (...)"
      ]}
    {:name "psi"
      :signatures [
        "Y = psi (Z)"
        "Y = psi (K, Z)"
      ]}
    {:name "puts"
      :signatures [
        "STATUS = puts (STRING)"
      ]}
    {:name "pwd"
      :signatures [
        "DIR = pwd ()"
      ]}
    {:name "qr"
      :signatures [
        "[Q, R] = qr (A)"
        "[Q, R, P] = qr (A)"
        "X = qr (A) # non-sparse A"
        "R = qr (A) # sparse A"
        "X = qr (A, B) # sparse A"
        "[C, R] = qr (A, B)"
        "[...] = qr (..., 0)"
        "[...] = qr (..., \"econ\")"
        "[...] = qr (..., \"vector\")"
        "[...] = qr (..., \"matrix\")"
      ]}
    {:name "qrdelete"
      :signatures [
        "[Q1, R1] = qrdelete (Q, R, J, ORIENT)"
      ]}
    {:name "qrinsert"
      :signatures [
        "[Q1, R1] = qrinsert (Q, R, J, X, ORIENT)"
      ]}
    {:name "qrshift"
      :signatures [
        "[Q1, R1] = qrshift (Q, R, I, J)"
      ]}
    {:name "qrupdate"
      :signatures [
        "[Q1, R1] = qrupdate (Q, R, U, V)"
      ]}
    {:name "quad"
      :signatures [
        "Q = quad (F, A, B)"
        "Q = quad (F, A, B, TOL)"
        "Q = quad (F, A, B, TOL, SING)"
        "[Q, IER, NFEV, ERR] = quad (...)"
      ]}
    {:name "quad_options"
      :signatures [
        "quad_options ()"
        "val = quad_options (OPT)"
        "quad_options (OPT, VAL)"
      ]}
    {:name "quadcc"
      :signatures [
        "Q = quadcc (F, A, B)"
        "Q = quadcc (F, A, B, TOL)"
        "Q = quadcc (F, A, B, TOL, SING)"
        "[Q, ERR, NR_POINTS] = quadcc (...)"
      ]}
    {:name "quit"
      :signatures [
        "quit"
        "quit cancel"
        "quit force"
        "quit (\"cancel\")"
        "quit (\"force\")"
        "quit (STATUS)"
        "quit (STATUS, \"force\")"
      ]}
    {:name "qz"
      :signatures [
        "LAMBDA = qz (A, B)"
        "[AA, BB, Q, Z, V, W, LAMBDA] = qz (A, B)"
        "[AA, BB, Z] = qz (A, B, OPT)"
        "[AA, BB, Z, LAMBDA] = qz (A, B, OPT)"
      ]}
    {:name "rand"
      :signatures [
        "X = rand (N)"
        "X = rand (M, N, ...)"
        "X = rand ([M N ...])"
        "X = rand (..., \"single\")"
        "X = rand (..., \"double\")"
        "V = rand (\"state\")"
        "rand (\"state\", V)"
        "rand (\"state\", \"reset\")"
        "V = rand (\"seed\")"
        "rand (\"seed\", V)"
        "rand (\"seed\", \"reset\")"
      ]}
    {:name "rande"
      :signatures [
        "X = rande (N)"
        "X = rande (M, N, ...)"
        "X = rande ([M N ...])"
        "X = rande (..., \"single\")"
        "X = rande (..., \"double\")"
        "V = rande (\"state\")"
        "rande (\"state\", V)"
        "rande (\"state\", \"reset\")"
        "V = rande (\"seed\")"
        "rande (\"seed\", V)"
        "rande (\"seed\", \"reset\")"
      ]}
    {:name "randg"
      :signatures [
        "X = randg (A, N)"
        "X = randg (A, M, N, ...)"
        "X = randg (A, [M N ...])"
        "X = randg (..., \"single\")"
        "X = randg (..., \"double\")"
        "V = randg (\"state\")"
        "randg (\"state\", V)"
        "randg (\"state\", \"reset\")"
        "V = randg (\"seed\")"
        "randg (\"seed\", V)"
        "randg (\"seed\", \"reset\")"
      ]}
    {:name "randn"
      :signatures [
        "X = randn (N)"
        "X = randn (M, N, ...)"
        "X = randn ([M N ...])"
        "X = randn (..., \"single\")"
        "X = randn (..., \"double\")"
        "V = randn (\"state\")"
        "randn (\"state\", V)"
        "randn (\"state\", \"reset\")"
        "V = randn (\"seed\")"
        "randn (\"seed\", V)"
        "randn (\"seed\", \"reset\")"
      ]}
    {:name "randp"
      :signatures [
        "X = randp (L, N)"
        "X = randp (L, M, N, ...)"
        "X = randp (L, [M N ...])"
        "X = randp (..., \"single\")"
        "X = randp (..., \"double\")"
        "V = randp (\"state\")"
        "randp (\"state\", V)"
        "randp (\"state\", \"reset\")"
        "V = randp (\"seed\")"
        "randp (\"seed\", V)"
        "randp (\"seed\", \"reset\")"
      ]}
    {:name "randperm"
      :signatures [
        "V = randperm (N)"
        "V = randperm (N, M)"
      ]}
    {:name "rats"
      :signatures [
        "S = rats (X)"
        "S = rats (X, LEN)"
      ]}
    {:name "rcond"
      :signatures [
        "C = rcond (A)"
      ]}
    {:name "rdivide"
      :signatures [
        "C = rdivide (A, B)"
      ]}
    {:name "readdir"
      :signatures [
        "FILES = readdir (DIR)"
        "[FILES, ERR, MSG] = readdir (DIR)"
      ]}
    {:name "readline_re_read_init_file"
      :signatures [
        "readline_re_read_init_file ()"
      ]}
    {:name "readline_read_init_file"
      :signatures [
        "readline_read_init_file ()"
        "readline_read_init_file (FILE)"
      ]}
    {:name "readlink"
      :signatures [
        "RESULT = readlink SYMLINK"
        "[RESULT, ERR, MSG] = readlink (SYMLINK)"
      ]}
    {:name "real"
      :signatures [
        "X = real (Z)"
      ]}
    {:name "realmax"
      :signatures [
        "RMAX = realmax"
        "RMAX = realmax (N)"
        "RMAX = realmax (N, M)"
        "RMAX = realmax (N, M, K, ...)"
        "RMAX = realmax (..., CLASS)"
      ]}
    {:name "realmin"
      :signatures [
        "RMIN = realmin"
        "RMIN = realmin (N)"
        "RMIN = realmin (N, M)"
        "RMIN = realmin (N, M, K, ...)"
        "RMIN = realmin (..., CLASS)"
      ]}
    {:name "regexp"
      :signatures [
        "[S, E, TE, M, T, NM, SP] = regexp (STR, PAT)"
        "[...] = regexp (STR, PAT, \"OPT1\", ...)"
      ]}
    {:name "regexpi"
      :signatures [
        "[S, E, TE, M, T, NM, SP] = regexpi (STR, PAT)"
        "[...] = regexpi (STR, PAT, \"OPT1\", ...)"
      ]}
    {:name "regexprep"
      :signatures [
        "OUTSTR = regexprep (STRING, PAT, REPSTR)"
        "OUTSTR = regexprep (STRING, PAT, REPSTR, \"OPT1\", ...)"
      ]}
    {:name "register_graphics_toolkit"
      :signatures [
        "register_graphics_toolkit (\"TOOLKIT\")"
      ]}
    {:name "rehash"
      :signatures [
        "rehash ()"
      ]}
    {:name "rem"
      :signatures [
        "R = rem (X, Y)"
      ]}
    {:name "remove_input_event_hook"
      :signatures [
        "remove_input_event_hook (NAME)"
        "remove_input_event_hook (FCN_ID)"
      ]}
    {:name "rename"
      :signatures [
        "rename OLD NEW"
        "[STATUS, MSG] = rename (OLD, NEW)"
      ]}
    {:name "repelems"
      :signatures [
        "Y = repelems (X, R)"
      ]}
  ])

(def ^:private +core-5+
  "Chunk 5 of Octave builtins from the `core` category, with signatures."
  [
    {:name "reset"
      :signatures [
        "reset (H)"
      ]}
    {:name "reshape"
      :signatures [
        "B = reshape (A, M, N, ...)"
        "B = reshape (A, [M N ...])"
        "B = reshape (A, ..., [], ...)"
        "B = reshape (A, SIZE)"
      ]}
    {:name "resize"
      :signatures [
        "B = resize (A, M)"
        "B = resize (A, M, N, ...)"
        "B = resize (A, [M N ...])"
      ]}
    {:name "restoredefaultpath"
      :signatures [
        "PATHSTR = restoredefaultpath ()"
      ]}
    {:name "rethrow"
      :signatures [
        "rethrow (ERR)"
      ]}
    {:name "return"
      :signatures [
        "return"
      ]}
    {:name "rmdir"
      :signatures [
        "rmdir DIR"
        "rmdir (DIR, \"s\")"
        "[STATUS, MSG, MSGID] = rmdir (...)"
      ]}
    {:name "rmfield"
      :signatures [
        "SOUT = rmfield (S, \"F\")"
        "SOUT = rmfield (S, F)"
      ]}
    {:name "rmpath"
      :signatures [
        "rmpath (DIR1, ...)"
        "OLDPATH = rmpath (DIR1, ...)"
      ]}
    {:name "round"
      :signatures [
        "Y = round (X)"
      ]}
    {:name "roundb"
      :signatures [
        "Y = roundb (X)"
      ]}
    {:name "rows"
      :signatures [
        "NR = rows (A)"
      ]}
    {:name "rsf2csf"
      :signatures [
        "[U, T] = rsf2csf (UR, TR)"
      ]}
    {:name "run_history"
      :signatures [
        "run_history"
        "run_history CMD_NUMBER"
        "run_history FIRST LAST"
      ]}
    {:name "save"
      :signatures [
        "save file"
        "save options file"
        "save options file V1 V2 ..."
        "save options file -struct STRUCT"
        "save options file -struct STRUCT F1 F2 ..."
        "save - V1 V2 ..."
        "STR = save (\"-\", \"V1\", \"V2\", ...)"
      ]}
    {:name "save_default_options"
      :signatures [
        "VAL = save_default_options ()"
        "OLD_VAL = save_default_options (NEW_VAL)"
        "OLD_VAL = save_default_options (NEW_VAL, \"local\")"
      ]}
    {:name "save_header_format_string"
      :signatures [
        "VAL = save_header_format_string ()"
        "OLD_VAL = save_header_format_string (NEW_VAL)"
        "OLD_VAL = save_header_format_string (NEW_VAL, \"local\")"
      ]}
    {:name "save_precision"
      :signatures [
        "VAL = save_precision ()"
        "OLD_VAL = save_precision (NEW_VAL)"
        "OLD_VAL = save_precision (NEW_VAL, \"local\")"
      ]}
    {:name "scanf"
      :signatures [
        "[VAL, COUNT, ERRMSG] = scanf (TEMPLATE, SIZE)"
        "[V1, V2, ..., COUNT, ERRMSG] = scanf (TEMPLATE, \"C\")"
      ]}
    {:name "schur"
      :signatures [
        "S = schur (A)"
        "S = schur (A, \"real\")"
        "S = schur (A, \"complex\")"
        "S = schur (A, OPT)"
        "[U, S] = schur (...)"
      ]}
    {:name "set"
      :signatures [
        "set (H, PROPERTY, VALUE, ...)"
        "set (H, {PROPERTIES}, {VALUES})"
        "set (H, PV)"
        "VALUE_LIST = set (H, PROPERTY)"
        "ALL_VALUE_LIST = set (H)"
      ]}
    {:name "setenv"
      :signatures [
        "setenv (\"VAR\", VALUE)"
        "setenv (VAR)"
        "putenv (...)"
      ]}
    {:name "setgrent"
      :signatures [
        "[STATUS, MSG] = setgrent ()"
      ]}
    {:name "setpwent"
      :signatures [
        "[STATUS, MSG] = setpwent ()"
      ]}
    {:name "sighup_dumps_octave_core"
      :signatures [
        "VAL = sighup_dumps_octave_core ()"
        "OLD_VAL = sighup_dumps_octave_core (NEW_VAL)"
        "OLD_VAL = sighup_dumps_octave_core (NEW_VAL, \"local\")"
      ]}
    {:name "sign"
      :signatures [
        "Y = sign (X)"
      ]}
    {:name "signbit"
      :signatures [
        "Y = signbit (X)"
      ]}
    {:name "sigquit_dumps_octave_core"
      :signatures [
        "VAL = sigquit_dumps_octave_core ()"
        "OLD_VAL = sigquit_dumps_octave_core (NEW_VAL)"
        "OLD_VAL = sigquit_dumps_octave_core (NEW_VAL, \"local\")"
      ]}
    {:name "sigterm_dumps_octave_core"
      :signatures [
        "VAL = sigterm_dumps_octave_core ()"
        "OLD_VAL = sigterm_dumps_octave_core (NEW_VAL)"
        "OLD_VAL = sigterm_dumps_octave_core (NEW_VAL, \"local\")"
      ]}
    {:name "silent_functions"
      :signatures [
        "VAL = silent_functions ()"
        "OLD_VAL = silent_functions (NEW_VAL)"
        "OLD_VAL = silent_functions (NEW_VAL, \"local\")"
      ]}
    {:name "sin"
      :signatures [
        "Y = sin (X)"
      ]}
    {:name "single"
      :signatures [
        "Y = single (X)"
      ]}
    {:name "sinh"
      :signatures [
        "Y = sinh (X)"
      ]}
    {:name "size"
      :signatures [
        "SZ = size (A)"
        "DIM_SZ = size (A, DIM)"
        "DIM_SZ = size (A, D1, D2, ...)"
        "[ROWS, COLS, ..., DIM_N_SZ] = size (...)"
      ]}
    {:name "size_equal"
      :signatures [
        "TF = size_equal (A, B)"
        "TF = size_equal (A, B, ...)"
      ]}
    {:name "sizemax"
      :signatures [
        "MAX_NUMEL = sizemax ()"
      ]}
    {:name "sizeof"
      :signatures [
        "SZ = sizeof (VAL)"
      ]}
    {:name "sort"
      :signatures [
        "[S, I] = sort (X)"
        "[S, I] = sort (X, DIM)"
        "[S, I] = sort (X, MODE)"
        "[S, I] = sort (X, DIM, MODE)"
      ]}
    {:name "source"
      :signatures [
        "source (FILE)"
        "source (FILE, CONTEXT)"
      ]}
    {:name "spalloc"
      :signatures [
        "S = spalloc (M, N, NZ)"
      ]}
    {:name "sparse"
      :signatures [
        "S = sparse (A)"
        "S = sparse (I, J, SV, M, N)"
        "S = sparse (I, J, SV)"
        "S = sparse (M, N)"
        "S = sparse (I, J, S, M, N, \"unique\")"
        "S = sparse (I, J, SV, M, N, NZMAX)"
      ]}
    {:name "split_long_rows"
      :signatures [
        "VAL = split_long_rows ()"
        "OLD_VAL = split_long_rows (NEW_VAL)"
        "OLD_VAL = split_long_rows (NEW_VAL, \"local\")"
      ]}
    {:name "spmd"
      :signatures [
        "spmd"
        "spmd (N)"
        "spmd (M, N)"
      ]}
    {:name "spparms"
      :signatures [
        "spparms ()"
        "VALS = spparms ()"
        "[KEYS, VALS] = spparms ()"
        "VAL = spparms (KEY)"
        "spparms (VALS)"
        "spparms (\"default\")"
        "spparms (\"tight\")"
        "spparms (KEY, VAL)"
      ]}
    {:name "sprank"
      :signatures [
        "P = sprank (S)"
      ]}
    {:name "sprintf"
      :signatures [
        "STR = sprintf (TEMPLATE, ...)"
      ]}
    {:name "sqrt"
      :signatures [
        "Y = sqrt (X)"
      ]}
    {:name "sqrtm"
      :signatures [
        "S = sqrtm (A)"
        "[S, ERROR_ESTIMATE] = sqrtm (A)"
      ]}
    {:name "squeeze"
      :signatures [
        "B = squeeze (A)"
      ]}
    {:name "sscanf"
      :signatures [
        "[VAL, COUNT, ERRMSG, POS] = sscanf (STRING, TEMPLATE, SIZE)"
        "[V1, V2, ..., COUNT, ERRMSG] = sscanf (STRING, TEMPLATE, \"C\")"
      ]}
    {:name "stat"
      :signatures [
        "[INFO, ERR, MSG] = stat (FILE)"
        "[INFO, ERR, MSG] = stat (FID)"
        "[INFO, ERR, MSG] = lstat (FILE)"
        "[INFO, ERR, MSG] = lstat (FID)"
      ]}
    {:name "stderr"
      :signatures [
        "FID = stderr ()"
      ]}
    {:name "stdin"
      :signatures [
        "FID = stdin ()"
      ]}
    {:name "stdout"
      :signatures [
        "FID = stdout ()"
      ]}
    {:name "str2double"
      :signatures [
        "D = str2double (STR)"
      ]}
    {:name "str2func"
      :signatures [
        "HFCN = str2func (STR)"
      ]}
    {:name "strcmp"
      :signatures [
        "TF = strcmp (STR1, STR2)"
      ]}
    {:name "strcmpi"
      :signatures [
        "TF = strcmpi (STR1, STR2)"
      ]}
    {:name "strfind"
      :signatures [
        "IDX = strfind (STR, PATTERN)"
        "IDX = strfind (CELLSTR, PATTERN)"
        "IDX = strfind (..., \"overlaps\", VAL)"
        "IDX = strfind (..., \"forcecelloutput\", VAL)"
      ]}
    {:name "strftime"
      :signatures [
        "STR = strftime (FMT, TM_STRUCT)"
      ]}
    {:name "string_fill_char"
      :signatures [
        "VAL = string_fill_char ()"
        "OLD_VAL = string_fill_char (NEW_VAL)"
        "OLD_VAL = string_fill_char (NEW_VAL, \"local\")"
      ]}
    {:name "strncmp"
      :signatures [
        "TF = strncmp (STR1, STR2, N)"
      ]}
    {:name "strncmpi"
      :signatures [
        "TF = strncmpi (STR1, STR2, N)"
      ]}
    {:name "strptime"
      :signatures [
        "[TM_STRUCT, NCHARS] = strptime (STR, FMT)"
      ]}
    {:name "strrep"
      :signatures [
        "NEWSTR = strrep (STR, PTN, REP)"
        "NEWSTR = strrep (CELLSTR, PTN, REP)"
        "NEWSTR = strrep (..., \"overlaps\", VAL)"
      ]}
    {:name "struct"
      :signatures [
        "S = struct ()"
        "S = struct (FIELD1, VALUE1, FIELD2, VALUE2, ...)"
        "S = struct (OBJ)"
      ]}
    {:name "struct2cell"
      :signatures [
        "C = struct2cell (S)"
      ]}
    {:name "struct_levels_to_print"
      :signatures [
        "VAL = struct_levels_to_print ()"
        "OLD_VAL = struct_levels_to_print (NEW_VAL)"
        "OLD_VAL = struct_levels_to_print (NEW_VAL, \"local\")"
      ]}
    {:name "strvcat"
      :signatures [
        "C = strvcat (A)"
        "C = strvcat (A, ...)"
        "C = strvcat (STR1, STR2, ...)"
        "C = strvcat (CELL_ARRAY)"
      ]}
    {:name "sub2ind"
      :signatures [
        "IND = sub2ind (DIMS, I, J)"
        "IND = sub2ind (DIMS, S1, S2, ..., SN)"
      ]}
    {:name "subsasgn"
      :signatures [
        "NEWVAL = subsasgn (VAL, IDX, RHS)"
      ]}
    {:name "subsref"
      :signatures [
        "NEWVAL = subsref (VAL, IDX)"
      ]}
    {:name "sum"
      :signatures [
        "Y = sum (X)"
        "Y = sum (X, DIM)"
        "Y = sum (..., \"native\")"
        "Y = sum (..., \"double\")"
        "Y = sum (..., \"extra\")"
      ]}
    {:name "sumsq"
      :signatures [
        "Y = sumsq (X)"
        "Y = sumsq (X, DIM)"
      ]}
    {:name "superiorto"
      :signatures [
        "superiorto (CLASS_NAME, ...)"
      ]}
    {:name "suppress_verbose_help_message"
      :signatures [
        "VAL = suppress_verbose_help_message ()"
        "OLD_VAL = suppress_verbose_help_message (NEW_VAL)"
        "OLD_VAL = suppress_verbose_help_message (NEW_VAL, \"local\")"
      ]}
    {:name "svd"
      :signatures [
        "S = svd (A)"
        "[U, S, V] = svd (A)"
        "[U, S, V] = svd (A, \"econ\")"
        "[U, S, V] = svd (A, 0)"
      ]}
    {:name "svd_driver"
      :signatures [
        "VAL = svd_driver ()"
        "OLD_VAL = svd_driver (NEW_VAL)"
        "OLD_VAL = svd_driver (NEW_VAL, \"local\")"
      ]}
    {:name "switch"
      :signatures [
        "switch STATEMENT"
      ]}
    {:name "sylvester"
      :signatures [
        "X = sylvester (A, B, C)"
      ]}
    {:name "symamd"
      :signatures [
        "P = symamd (S)"
        "P = symamd (S, KNOBS)"
        "[P, STATS] = symamd (S)"
        "[P, STATS] = symamd (S, KNOBS)"
      ]}
    {:name "symbfact"
      :signatures [
        "[COUNT, H, PARENT, POST, R] = symbfact (S)"
        "[...] = symbfact (S, TYP)"
        "[...] = symbfact (S, TYP, MODE)"
      ]}
    {:name "symlink"
      :signatures [
        "symlink OLD NEW"
        "[STATUS, MSG] = symlink (OLD, NEW)"
      ]}
    {:name "symrcm"
      :signatures [
        "P = symrcm (S)"
      ]}
    {:name "system"
      :signatures [
        "system (\"STRING\")"
        "system (\"STRING\", RETURN_OUTPUT)"
        "system (\"STRING\", RETURN_OUTPUT, TYPE)"
        "[STATUS, OUTPUT] = system (...)"
      ]}
    {:name "tan"
      :signatures [
        "Y = tan (Z)"
      ]}
    {:name "tanh"
      :signatures [
        "Y = tanh (X)"
      ]}
    {:name "tempdir"
      :signatures [
        "DIR = tempdir ()"
      ]}
    {:name "tempname"
      :signatures [
        "FNAME = tempname ()"
        "FNAME = tempname (DIR)"
        "FNAME = tempname (DIR, PREFIX)"
      ]}
    {:name "terminal_size"
      :signatures [
        "[ROWS, COLS] = terminal_size ()"
        "terminal_size ([ROWS, COLS])"
      ]}
    {:name "texi_macros_file"
      :signatures [
        "VAL = texi_macros_file ()"
        "OLD_VAL = texi_macros_file (NEW_VAL)"
        "OLD_VAL = texi_macros_file (NEW_VAL, \"local\")"
      ]}
    {:name "textscan"
      :signatures [
        "C = textscan (FID, FORMAT)"
        "C = textscan (FID, FORMAT, REPEAT)"
        "C = textscan (FID, FORMAT, PARAM, VALUE, ...)"
        "C = textscan (FID, FORMAT, REPEAT, PARAM, VALUE, ...)"
        "C = textscan (STR, ...)"
        "[C, POSITION, ERRMSG] = textscan (...)"
      ]}
    {:name "tic"
      :signatures [
        "tic ()"
        "ID = tic ()"
      ]}
    {:name "tilde_expand"
      :signatures [
        "NEWSTR = tilde_expand (STRING)"
        "NEWCSTR = tilde_expand (CELLSTR)"
      ]}
    {:name "time"
      :signatures [
        "SECONDS = time ()"
      ]}
    {:name "times"
      :signatures [
        "C = times (A, B)"
        "C = times (A1, A2, ...)"
      ]}
    {:name "tmpfile"
      :signatures [
        "[FID, MSG] = tmpfile ()"
      ]}
    {:name "toc"
      :signatures [
        "toc ()"
        "toc (ID)"
        "ELAPSED_TIME = toc (...)"
      ]}
    {:name "tolower"
      :signatures [
        "Y = tolower (S)"
        "Y = lower (S)"
      ]}
    {:name "toupper"
      :signatures [
        "Y = toupper (S)"
        "Y = upper (S)"
      ]}
    {:name "transpose"
      :signatures [
        "B = transpose (A)"
      ]}
    {:name "tril"
      :signatures [
        "A_LO = tril (A)"
        "A_LO = tril (A, K)"
        "A_LO = tril (A, K, PACK)"
      ]}
    {:name "triu"
      :signatures [
        "A_UP = triu (A)"
        "A_UP = triu (A, K)"
        "A_UP = triu (A, K, PACK)"
      ]}
    {:name "true"
      :signatures [
        "VAL = true (X)"
        "VAL = true (N, M)"
        "VAL = true (N, M, K, ...)"
        "VAL = true (..., \"like\", VAR)"
      ]}
    {:name "try"
      :signatures [
        "try"
      ]}
    {:name "tsearch"
      :signatures [
        "IDX = tsearch (X, Y, T, XI, YI)"
      ]}
    {:name "typecast"
      :signatures [
        "Y = typecast (X, \"CLASS\")"
      ]}
    {:name "typeinfo"
      :signatures [
        "TYPESTR = typeinfo (EXPR)"
        "CSTR = typeinfo ()"
      ]}
    {:name "uint16"
      :signatures [
        "Y = uint16 (X)"
      ]}
    {:name "uint32"
      :signatures [
        "Y = uint32 (X)"
      ]}
    {:name "uint64"
      :signatures [
        "Y = uint64 (X)"
      ]}
    {:name "uint8"
      :signatures [
        "Y = uint8 (X)"
      ]}
    {:name "umask"
      :signatures [
        "OLDMASK = umask (MASK)"
      ]}
    {:name "uminus"
      :signatures [
        "B = uminus (A)"
      ]}
    {:name "uname"
      :signatures [
        "[UTS, ERR, MSG] = uname ()"
      ]}
    {:name "undo_string_escapes"
      :signatures [
        "NEWSTR = undo_string_escapes (STRING)"
      ]}
    {:name "unicode_idx"
      :signatures [
        "IDX = unicode_idx (STR)"
      ]}
    {:name "unlink"
      :signatures [
        "unlink (FILE)"
        "[STATUS, MSG] = unlink (FILE)"
      ]}
    {:name "unsetenv"
      :signatures [
        "STATUS = unsetenv (VAR)"
      ]}
    {:name "until"
      :signatures [
        "until (COND)"
      ]}
    {:name "unwind_protect"
      :signatures [
        "unwind_protect"
      ]}
    {:name "unwind_protect_cleanup"
      :signatures [
        "unwind_protect_cleanup"
      ]}
    {:name "uplus"
      :signatures [
        "B = uplus (A)"
      ]}
    {:name "urlread"
      :signatures [
        "S = urlread (URL)"
        "[S, SUCCESS] = urlread (URL)"
        "[S, SUCCESS, MESSAGE] = urlread (URL)"
        "[...] = urlread (URL, METHOD, PARAM)"
      ]}
    {:name "urlwrite"
      :signatures [
        "urlwrite (URL, LOCALFILE)"
        "F = urlwrite (URL, LOCALFILE)"
        "[F, SUCCESS] = urlwrite (URL, LOCALFILE)"
        "[F, SUCCESS, MESSAGE] = urlwrite (URL, LOCALFILE)"
      ]}
    {:name "user_config_dir"
      :signatures [
        "cfg_dir = user_config_dir ()"
      ]}
    {:name "user_data_dir"
      :signatures [
        "data_dir = user_data_dir ()"
      ]}
    {:name "varargin"
      :signatures [
        "varargin"
      ]}
    {:name "varargout"
      :signatures [
        "varargout"
      ]}
    {:name "vec"
      :signatures [
        "V = vec (X)"
        "V = vec (X, DIM)"
      ]}
    {:name "vertcat"
      :signatures [
        "A = vertcat (ARRAY1, ARRAY2, ..., ARRAYN)"
      ]}
    {:name "waitfor"
      :signatures [
        "waitfor (H)"
        "waitfor (H, PROP)"
        "waitfor (H, PROP, VALUE)"
        "waitfor (..., \"timeout\", TIMEOUT)"
      ]}
    {:name "waitpid"
      :signatures [
        "[PID, STATUS, MSG] = waitpid (PID, OPTIONS)"
      ]}
    {:name "warning"
      :signatures [
        "warning (TEMPLATE, ...)"
        "warning (ID, TEMPLATE, ...)"
        "warning (\"on\", ID)"
        "warning (\"off\", ID)"
        "warning (\"error\", ID)"
        "warning (\"query\", ID)"
        "warning (STATE, ID, \"local\")"
        "warning (WARNING_STRUCT)"
        "WARNING_STRUCT = warning (...)"
        "warning (STATE, MODE)"
      ]}
    {:name "warranty"
      :signatures [
        "warranty ()"
      ]}
    {:name "while"
      :signatures [
        "while (COND)"
      ]}
    {:name "who"
      :signatures [
        "who"
        "who pattern ..."
        "who option pattern ..."
        "C = who (...)"
      ]}
    {:name "whos"
      :signatures [
        "whos"
        "whos pattern ..."
        "whos option pattern ..."
        "S = whos (\"pattern\", ...)"
      ]}
    {:name "whos_line_format"
      :signatures [
        "VAL = whos_line_format ()"
        "OLD_VAL = whos_line_format (NEW_VAL)"
        "OLD_VAL = whos_line_format (NEW_VAL, \"local\")"
      ]}
    {:name "winqueryreg"
      :signatures [
        "VALUE = winqueryreg (ROOTKEY, SUBKEY, VALUENAME)"
        "VALUE = winqueryreg (ROOTKEY, SUBKEY)"
        "NAMES = winqueryreg (\"name\", ROOTKEY, SUBKEY)"
      ]}
    {:name "workspace"
      :signatures [
        "workspace ()"
      ]}
    {:name "yes_or_no"
      :signatures [
        "ANS = yes_or_no (\"PROMPT\")"
      ]}
    {:name "zeros"
      :signatures [
        "VAL = zeros (N)"
        "VAL = zeros (M, N)"
        "VAL = zeros (M, N, K, ...)"
        "VAL = zeros ([M N ...])"
        "VAL = zeros (..., \"like\", VAR)"
        "VAL = zeros (..., CLASS)"
      ]}
    {:name "|"
      :signatures [
        "|"
      ]}
    {:name "||"
      :signatures [
        "||"
      ]}
    {:name "~"
      :signatures [
        "~"
      ]}
    {:name "~="
      :signatures [
        "~="
      ]}
  ])

(def +core+
  "Octave builtins from the `core` category, with signatures."
  (vec (concat +core-1+ +core-2+ +core-3+ +core-4+ +core-5+)))

(def +deprecated+
  "Octave builtins from the `deprecated` category, with signatures."
  [
    {:name "disable_diagonal_matrix"
      :signatures [
        "VAL = disable_diagonal_matrix ()"
        "OLD_VAL = disable_diagonal_matrix (NEW_VAL)"
        "OLD_VAL = disable_diagonal_matrix (NEW_VAL, \"local\")"
      ]}
    {:name "disable_permutation_matrix"
      :signatures [
        "VAL = disable_permutation_matrix ()"
        "OLD_VAL = disable_permutation_matrix (NEW_VAL)"
        "OLD_VAL = disable_permutation_matrix (NEW_VAL, \"local\")"
      ]}
    {:name "disable_range"
      :signatures [
        "VAL = disable_range ()"
        "OLD_VAL = disable_range (NEW_VAL)"
        "OLD_VAL = disable_range (NEW_VAL, \"local\")"
      ]}
    {:name "shift"
      :signatures [
        "Y = shift (X, B)"
        "Y = shift (X, B, DIM)"
      ]}
    {:name "sparse_auto_mutate"
      :signatures [
        "VAL = sparse_auto_mutate ()"
        "OLD_VAL = sparse_auto_mutate (NEW_VAL)"
        "OLD_VAL = sparse_auto_mutate (NEW_VAL, \"local\")"
      ]}
  ])

(def +elfun+
  "Octave builtins from the `elfun` category, with signatures."
  [
    {:name "acosd"
      :signatures [
        "Y = acosd (X)"
      ]}
    {:name "acot"
      :signatures [
        "Y = acot (X)"
      ]}
    {:name "acotd"
      :signatures [
        "Y = acotd (X)"
      ]}
    {:name "acoth"
      :signatures [
        "Y = acoth (X)"
      ]}
    {:name "acsc"
      :signatures [
        "Y = acsc (X)"
      ]}
    {:name "acscd"
      :signatures [
        "Y = acscd (X)"
      ]}
    {:name "acsch"
      :signatures [
        "Y = acsch (X)"
      ]}
    {:name "asec"
      :signatures [
        "Y = asec (X)"
      ]}
    {:name "asecd"
      :signatures [
        "Y = asecd (X)"
      ]}
    {:name "asech"
      :signatures [
        "Y = asech (X)"
      ]}
    {:name "asind"
      :signatures [
        "Y = asind (X)"
      ]}
    {:name "atan2d"
      :signatures [
        "D = atan2d (Y, X)"
      ]}
    {:name "atand"
      :signatures [
        "Y = atand (X)"
      ]}
    {:name "cosd"
      :signatures [
        "Y = cosd (X)"
      ]}
    {:name "cospi"
      :signatures [
        "Y = cospi (X)"
      ]}
    {:name "cot"
      :signatures [
        "Y = cot (X)"
      ]}
    {:name "cotd"
      :signatures [
        "Y = cotd (X)"
      ]}
    {:name "coth"
      :signatures [
        "Y = coth (X)"
      ]}
    {:name "csc"
      :signatures [
        "Y = csc (X)"
      ]}
    {:name "cscd"
      :signatures [
        "Y = cscd (X)"
      ]}
    {:name "csch"
      :signatures [
        "Y = csch (X)"
      ]}
    {:name "sec"
      :signatures [
        "Y = sec (X)"
      ]}
    {:name "secd"
      :signatures [
        "Y = secd (X)"
      ]}
    {:name "sech"
      :signatures [
        "Y = sech (X)"
      ]}
    {:name "sind"
      :signatures [
        "Y = sind (X)"
      ]}
    {:name "sinpi"
      :signatures [
        "Y = sinpi (X)"
      ]}
    {:name "tand"
      :signatures [
        "Y = tand (X)"
      ]}
  ])

(def +external+
  "Octave builtins from the `external` category, with signatures."
  [
    {:name "audiodevinfo"
      :signatures [
        "DEVINFO = audiodevinfo ()"
        "DEVS = audiodevinfo (IO)"
        "NAME = audiodevinfo (IO, ID)"
        "ID = audiodevinfo (IO, NAME)"
        "DRIVERVERSION = audiodevinfo (IO, ID, \"DriverVersion\")"
        "ID = audiodevinfo (IO, RATE, BITS, CHANS)"
        "SUPPORTS = audiodevinfo (IO, ID, RATE, BITS, CHANS)"
      ]}
    {:name "audioformats"
      :signatures [
        "audioformats ()"
        "audioformats (FORMAT)"
      ]}
    {:name "audioinfo"
      :signatures [
        "INFO = audioinfo (FILENAME)"
      ]}
    {:name "audioread"
      :signatures [
        "[Y, FS] = audioread (FILENAME)"
        "[Y, FS] = audioread (FILENAME, SAMPLES)"
        "[Y, FS] = audioread (FILENAME, DATATYPE)"
        "[Y, FS] = audioread (FILENAME, SAMPLES, DATATYPE)"
      ]}
    {:name "audiowrite"
      :signatures [
        "audiowrite (FILENAME, Y, FS)"
        "audiowrite (FILENAME, Y, FS, NAME, VALUE, ...)"
      ]}
    {:name "bzip2"
      :signatures [
        "FILELIST = bzip2 (FILES)"
        "FILELIST = bzip2 (FILES, DIR)"
      ]}
    {:name "convhulln"
      :signatures [
        "H = convhulln (PTS)"
        "H = convhulln (PTS, OPTIONS)"
        "[H, V] = convhulln (...)"
      ]}
    {:name "fftw"
      :signatures [
        "METHOD = fftw (\"planner\")"
        "fftw (\"planner\", METHOD)"
        "WISDOM = fftw (\"dwisdom\")"
        "fftw (\"dwisdom\", WISDOM)"
        "NTHREADS = fftw (\"threads\")"
        "fftw (\"threads\", NTHREADS)"
      ]}
    {:name "gzip"
      :signatures [
        "FILELIST = gzip (FILES)"
        "FILELIST = gzip (FILES, DIR)"
      ]}
  ])

(def +general+
  "Octave builtins from the `general` category, with signatures."
  [
    {:name "accumarray"
      :signatures [
        "A = accumarray (SUBS, VALS)"
        "A = accumarray (SUBS, VALS, SZ)"
        "A = accumarray (SUBS, VALS, SZ, FCN)"
        "A = accumarray (SUBS, VALS, SZ, FCN, FILLVAL)"
        "A = accumarray (SUBS, VALS, SZ, FCN, FILLVAL, ISSPARSE)"
      ]}
    {:name "accumdim"
      :signatures [
        "A = accumdim (SUBS, VALS)"
        "A = accumdim (SUBS, VALS, DIM)"
        "A = accumdim (SUBS, VALS, DIM, N)"
        "A = accumdim (SUBS, VALS, DIM, N, FCN)"
        "A = accumdim (SUBS, VALS, DIM, N, FCN, FILLVAL)"
      ]}
    {:name "bincoeff"
      :signatures [
        "B = bincoeff (N, K)"
      ]}
    {:name "bitcmp"
      :signatures [
        "C = bitcmp (A, K)"
      ]}
    {:name "bitget"
      :signatures [
        "B = bitget (A, N)"
      ]}
    {:name "bitset"
      :signatures [
        "B = bitset (A, N)"
        "B = bitset (A, N, VAL)"
      ]}
    {:name "blkdiag"
      :signatures [
        "M = blkdiag (A, B, C, ...)"
      ]}
    {:name "cart2pol"
      :signatures [
        "[THETA, R] = cart2pol (X, Y)"
        "[THETA, R, Z] = cart2pol (X, Y, Z)"
        "[THETA, R] = cart2pol (C)"
        "[THETA, R, Z] = cart2pol (C)"
      ]}
    {:name "cart2sph"
      :signatures [
        "[THETA, PHI, R] = cart2sph (X, Y, Z)"
        "[THETA, PHI, R] = cart2sph (C)"
      ]}
    {:name "cell2mat"
      :signatures [
        "M = cell2mat (C)"
      ]}
    {:name "celldisp"
      :signatures [
        "celldisp (C)"
        "celldisp (C, NAME)"
      ]}
    {:name "circshift"
      :signatures [
        "Y = circshift (X, N)"
        "Y = circshift (X, N, DIM)"
      ]}
    {:name "common_size"
      :signatures [
        "[ERR, YI, ...] = common_size (XI, ...)"
      ]}
    {:name "cplxpair"
      :signatures [
        "ZSORT = cplxpair (Z)"
        "ZSORT = cplxpair (Z, TOL)"
        "ZSORT = cplxpair (Z, TOL, DIM)"
      ]}
    {:name "cumtrapz"
      :signatures [
        "Q = cumtrapz (Y)"
        "Q = cumtrapz (X, Y)"
        "Q = cumtrapz (..., DIM)"
      ]}
    {:name "curl"
      :signatures [
        "[CX, CY, CZ, V] = curl (X, Y, Z, FX, FY, FZ)"
        "[CZ, V] = curl (X, Y, FX, FY)"
        "[...] = curl (FX, FY, FZ)"
        "[...] = curl (FX, FY)"
        "V = curl (...)"
      ]}
    {:name "dblquad"
      :signatures [
        "Q = dblquad (F, XA, XB, YA, YB)"
        "Q = dblquad (F, XA, XB, YA, YB, TOL)"
        "Q = dblquad (F, XA, XB, YA, YB, TOL, QUADF)"
        "Q = dblquad (F, XA, XB, YA, YB, TOL, QUADF, ...)"
      ]}
    {:name "deal"
      :signatures [
        "[R1, R2, ..., RN] = deal (A)"
        "[R1, R2, ..., RN] = deal (A1, A2, ..., AN)"
      ]}
    {:name "deg2rad"
      :signatures [
        "RAD = deg2rad (DEG)"
      ]}
    {:name "del2"
      :signatures [
        "L = del2 (M)"
        "L = del2 (M, H)"
        "L = del2 (M, DX, DY, ...)"
      ]}
    {:name "divergence"
      :signatures [
        "DIV = divergence (X, Y, Z, FX, FY, FZ)"
        "DIV = divergence (FX, FY, FZ)"
        "DIV = divergence (X, Y, FX, FY)"
        "DIV = divergence (FX, FY)"
      ]}
    {:name "flip"
      :signatures [
        "B = flip (A)"
        "B = flip (A, DIM)"
      ]}
    {:name "fliplr"
      :signatures [
        "B = fliplr (A)"
      ]}
    {:name "flipud"
      :signatures [
        "B = flipud (A)"
      ]}
    {:name "gradient"
      :signatures [
        "DX = gradient (M)"
        "[DX, DY, DZ, ...] = gradient (M)"
        "[...] = gradient (M, S)"
        "[...] = gradient (M, X, Y, Z, ...)"
        "[...] = gradient (F, X0)"
        "[...] = gradient (F, X0, S)"
        "[...] = gradient (F, X0, X, Y, ...)"
      ]}
    {:name "idivide"
      :signatures [
        "C = idivide (A, B, OP)"
      ]}
    {:name "int2str"
      :signatures [
        "STR = int2str (N)"
      ]}
    {:name "integral"
      :signatures [
        "Q = integral (F, A, B)"
        "Q = integral (F, A, B, PROP, VAL, ...)"
        "[Q, ERR] = integral (...)"
      ]}
    {:name "integral2"
      :signatures [
        "Q = integral2 (F, XA, XB, YA, YB)"
        "Q = integral2 (F, XA, XB, YA, YB, PROP, VAL, ...)"
        "[Q, ERR] = integral2 (...)"
      ]}
    {:name "integral3"
      :signatures [
        "Q = integral3 (F, XA, XB, YA, YB, ZA, ZB)"
        "Q = integral3 (F, XA, XB, YA, YB, ZA, ZB, PROP, VAL, ...)"
      ]}
    {:name "interp1"
      :signatures [
        "YI = interp1 (X, Y, XI)"
        "YI = interp1 (Y, XI)"
        "YI = interp1 (..., METHOD)"
        "YI = interp1 (..., EXTRAP)"
        "YI = interp1 (..., \"left\")"
        "YI = interp1 (..., \"right\")"
        "PP = interp1 (..., \"pp\")"
      ]}
    {:name "interp2"
      :signatures [
        "ZI = interp2 (X, Y, Z, XI, YI)"
        "ZI = interp2 (Z, XI, YI)"
        "ZI = interp2 (Z, N)"
        "ZI = interp2 (Z)"
        "ZI = interp2 (..., METHOD)"
        "ZI = interp2 (..., METHOD, EXTRAP)"
      ]}
    {:name "interp3"
      :signatures [
        "VI = interp3 (X, Y, Z, V, XI, YI, ZI)"
        "VI = interp3 (V, XI, YI, ZI)"
        "VI = interp3 (V, N)"
        "VI = interp3 (V)"
        "VI = interp3 (..., METHOD)"
        "VI = interp3 (..., METHOD, EXTRAPVAL)"
      ]}
    {:name "interpft"
      :signatures [
        "Y = interpft (X, N)"
        "Y = interpft (X, N, DIM)"
      ]}
    {:name "interpn"
      :signatures [
        "VI = interpn (X1, X2, ..., V, Y1, Y2, ...)"
        "VI = interpn (V, Y1, Y2, ...)"
        "VI = interpn (V, M)"
        "VI = interpn (V)"
        "VI = interpn (..., METHOD)"
        "VI = interpn (..., METHOD, EXTRAPVAL)"
      ]}
    {:name "isequal"
      :signatures [
        "TF = isequal (X1, X2, ...)"
      ]}
    {:name "isequaln"
      :signatures [
        "TF = isequaln (X1, X2, ...)"
      ]}
    {:name "logspace"
      :signatures [
        "Y = logspace (A, B)"
        "Y = logspace (A, B, N)"
        "Y = logspace (A, pi)"
        "Y = logspace (A, pi, N)"
      ]}
    {:name "nextpow2"
      :signatures [
        "N = nextpow2 (X)"
      ]}
    {:name "num2str"
      :signatures [
        "STR = num2str (X)"
        "STR = num2str (X, PRECISION)"
        "STR = num2str (X, FORMAT)"
      ]}
    {:name "pagectranspose"
      :signatures [
        "Y = pagectranspose (A)"
      ]}
    {:name "pagetranspose"
      :signatures [
        "B = pagetranspose (A)"
      ]}
    {:name "pol2cart"
      :signatures [
        "[X, Y] = pol2cart (THETA, R)"
        "[X, Y, Z] = pol2cart (THETA, R, Z)"
        "[X, Y] = pol2cart (P)"
        "[X, Y, Z] = pol2cart (P)"
      ]}
    {:name "polyarea"
      :signatures [
        "A = polyarea (X, Y)"
        "A = polyarea (X, Y, DIM)"
      ]}
    {:name "postpad"
      :signatures [
        "B = postpad (A, L)"
        "B = postpad (A, L, C)"
        "B = postpad (A, L, C, DIM)"
      ]}
    {:name "prepad"
      :signatures [
        "B = prepad (A, L)"
        "B = prepad (A, L, C)"
        "B = prepad (A, L, C, DIM)"
      ]}
    {:name "quad2d"
      :signatures [
        "Q = quad2d (F, XA, XB, YA, YB)"
        "Q = quad2d (F, XA, XB, YA, YB, PROP, VAL, ...)"
        "[Q, ERR, ITER] = quad2d (...)"
      ]}
    {:name "quadgk"
      :signatures [
        "Q = quadgk (F, A, B)"
        "Q = quadgk (F, A, B, ABSTOL)"
        "Q = quadgk (F, A, B, ABSTOL, TRACE)"
        "Q = quadgk (F, A, B, \"PROP\", VAL, ...)"
        "[Q, ERR] = quadgk (...)"
      ]}
    {:name "quadl"
      :signatures [
        "Q = quadl (F, A, B)"
        "Q = quadl (F, A, B, TOL)"
        "Q = quadl (F, A, B, TOL, TRACE)"
        "Q = quadl (F, A, B, TOL, TRACE, P1, P2, ...)"
        "[Q, NFEV] = quadl (...)"
      ]}
    {:name "quadv"
      :signatures [
        "Q = quadv (F, A, B)"
        "Q = quadv (F, A, B, TOL)"
        "Q = quadv (F, A, B, TOL, TRACE)"
        "Q = quadv (F, A, B, TOL, TRACE, P1, P2, ...)"
        "[Q, NFEV] = quadv (...)"
      ]}
    {:name "rad2deg"
      :signatures [
        "DEG = rad2deg (RAD)"
      ]}
    {:name "randi"
      :signatures [
        "R = randi (IMAX)"
        "R = randi (IMAX, N)"
        "R = randi (IMAX, M, N, ...)"
        "R = randi ([IMIN IMAX], ...)"
        "R = randi (..., \"CLASS\")"
      ]}
    {:name "rat"
      :signatures [
        "S = rat (X)"
        "S = rat (X, TOL)"
        "[N, D] = rat (...)"
      ]}
    {:name "repelem"
      :signatures [
        "XXX = repelem (X, R)"
        "XXX = repelem (X, R_1, ..., R_N)"
      ]}
    {:name "repmat"
      :signatures [
        "B = repmat (A, M)"
        "B = repmat (A, M, N)"
        "B = repmat (A, M, N, P ...)"
        "B = repmat (A, [M N])"
        "B = repmat (A, [M N P ...])"
      ]}
    {:name "rescale"
      :signatures [
        "B = rescale (A)"
        "B = rescale (A, L, U)"
        "B = rescale (..., \"inputmin\", INMIN)"
        "B = rescale (..., \"inputmax\", INMAX)"
      ]}
    {:name "rng"
      :signatures [
        "rng (SEED)"
        "rng (SEED, \"GENERATOR\")"
        "rng (\"shuffle\")"
        "rng (\"shuffle\", \"GENERATOR\")"
        "rng (\"default\")"
        "S = rng ()"
        "rng (S)"
        "S = rng (...)"
      ]}
    {:name "rot90"
      :signatures [
        "B = rot90 (A)"
        "B = rot90 (A, K)"
      ]}
    {:name "rotdim"
      :signatures [
        "B = rotdim (A)"
        "B = rotdim (A, N)"
        "B = rotdim (A, N, PLANE)"
      ]}
    {:name "shiftdim"
      :signatures [
        "Y = shiftdim (X, N)"
        "[Y, NS] = shiftdim (X)"
      ]}
    {:name "sortrows"
      :signatures [
        "[S, I] = sortrows (A)"
        "[S, I] = sortrows (A, C)"
      ]}
    {:name "sph2cart"
      :signatures [
        "[X, Y, Z] = sph2cart (THETA, PHI, R)"
        "[X, Y, Z] = sph2cart (S)"
      ]}
    {:name "structfun"
      :signatures [
        "A = structfun (FCN, S)"
        "A = structfun (..., \"ErrorHandler\", ERRFCN)"
        "A = structfun (..., \"UniformOutput\", VAL)"
        "[A, B, ...] = structfun (...)"
      ]}
    {:name "subsindex"
      :signatures [
        "IDX = subsindex (OBJ)"
      ]}
    {:name "trapz"
      :signatures [
        "Q = trapz (Y)"
        "Q = trapz (X, Y)"
        "Q = trapz (..., DIM)"
      ]}
    {:name "triplequad"
      :signatures [
        "Q = triplequad (F, XA, XB, YA, YB, ZA, ZB)"
        "Q = triplequad (F, XA, XB, YA, YB, ZA, ZB, TOL)"
        "Q = triplequad (F, XA, XB, YA, YB, ZA, ZB, TOL, QUADF)"
        "Q = triplequad (F, XA, XB, YA, YB, ZA, ZB, TOL, QUADF, ...)"
      ]}
    {:name "xor"
      :signatures [
        "Z = xor (X, Y)"
        "Z = xor (X1, X2, ...)"
      ]}
  ])

(def +geometry+
  "Octave builtins from the `geometry` category, with signatures."
  [
    {:name "convhull"
      :signatures [
        "H = convhull (X, Y)"
        "H = convhull (X, Y, Z)"
        "H = convhull (X)"
        "H = convhull (..., OPTIONS)"
        "[H, V] = convhull (...)"
      ]}
    {:name "delaunay"
      :signatures [
        "TRI = delaunay (X, Y)"
        "TETR = delaunay (X, Y, Z)"
        "TRI = delaunay (X)"
        "TRI = delaunay (..., OPTIONS)"
      ]}
    {:name "delaunayn"
      :signatures [
        "T = delaunayn (PTS)"
        "T = delaunayn (PTS, OPTIONS)"
      ]}
    {:name "dsearch"
      :signatures [
        "IDX = dsearch (X, Y, TRI, XI, YI)"
        "IDX = dsearch (X, Y, TRI, XI, YI, S)"
      ]}
    {:name "dsearchn"
      :signatures [
        "IDX = dsearchn (X, TRI, XI)"
        "IDX = dsearchn (X, TRI, XI, OUTVAL)"
        "IDX = dsearchn (X, XI)"
        "[IDX, D] = dsearchn (...)"
      ]}
    {:name "griddata"
      :signatures [
        "ZI = griddata (X, Y, Z, XI, YI)"
        "ZI = griddata (X, Y, Z, XI, YI, METHOD)"
        "[XI, YI, ZI] = griddata (...)"
        "VI = griddata (X, Y, Z, V, XI, YI, ZI)"
        "VI = griddata (X, Y, Z, V, XI, YI, ZI, METHOD)"
        "VI = griddata (X, Y, Z, V, XI, YI, ZI, METHOD, OPTIONS)"
      ]}
    {:name "griddata3"
      :signatures [
        "VI = griddata3 (X, Y, Z, V, XI, YI, ZI)"
        "VI = griddata3 (X, Y, Z, V, XI, YI, ZI, METHOD)"
        "VI = griddata3 (X, Y, Z, V, XI, YI, ZI, METHOD, OPTIONS)"
      ]}
    {:name "griddatan"
      :signatures [
        "YI = griddatan (X, Y, XI)"
        "YI = griddatan (X, Y, XI, METHOD)"
        "YI = griddatan (X, Y, XI, METHOD, OPTIONS)"
      ]}
    {:name "inpolygon"
      :signatures [
        "IN = inpolygon (X, Y, XV, YV)"
        "[IN, ON] = inpolygon (X, Y, XV, YV)"
      ]}
    {:name "rectint"
      :signatures [
        "AREA = rectint (A, B)"
      ]}
    {:name "rotx"
      :signatures [
        "T = rotx (ANGLE)"
      ]}
    {:name "roty"
      :signatures [
        "T = roty (ANGLE)"
      ]}
    {:name "rotz"
      :signatures [
        "T = rotz (ANGLE)"
      ]}
    {:name "tsearchn"
      :signatures [
        "IDX = tsearchn (X, T, XI)"
        "[IDX, P] = tsearchn (X, T, XI)"
      ]}
    {:name "voronoi"
      :signatures [
        "voronoi (X, Y)"
        "voronoi (X, Y, OPTIONS)"
        "voronoi (..., \"linespec\")"
        "voronoi (HAX, ...)"
        "H = voronoi (...)"
        "[VX, VY] = voronoi (...)"
      ]}
    {:name "voronoin"
      :signatures [
        "[C, F] = voronoin (PTS)"
        "[C, F] = voronoin (PTS, OPTIONS)"
      ]}
  ])

(def +gui+
  "Octave builtins from the `gui` category, with signatures."
  [
    {:name "dialog"
      :signatures [
        "H = dialog ()"
        "H = dialog (\"PROPERTY\", VALUE, ...)"
      ]}
    {:name "errordlg"
      :signatures [
        "errordlg ()"
        "errordlg (MSG)"
        "errordlg (MSG, TITLE)"
        "errordlg (MSG, TITLE, OPT)"
        "H = errordlg (...)"
      ]}
    {:name "getappdata"
      :signatures [
        "VALUE = getappdata (H, NAME)"
        "APPDATA = getappdata (H)"
      ]}
    {:name "getpixelposition"
      :signatures [
        "POS = getpixelposition (H)"
        "POS = getpixelposition (H, REL_TO_FIG)"
      ]}
    {:name "guidata"
      :signatures [
        "DATA = guidata (H)"
        "guidata (H, DATA)"
      ]}
    {:name "guihandles"
      :signatures [
        "HDATA = guihandles (H)"
        "HDATA = guihandles"
      ]}
    {:name "helpdlg"
      :signatures [
        "helpdlg ()"
        "helpdlg (MSG)"
        "helpdlg (MSG, TITLE)"
        "H = helpdlg (...)"
      ]}
    {:name "inputdlg"
      :signatures [
        "CSTR = inputdlg (PROMPT)"
        "CSTR = inputdlg (PROMPT, TITLE)"
        "CSTR = inputdlg (PROMPT, TITLE, ROWSCOLS)"
        "CSTR = inputdlg (PROMPT, TITLE, ROWSCOLS, DEFAULTS)"
        "CSTR = inputdlg (PROMPT, TITLE, ROWSCOLS, DEFAULTS, OPTIONS)"
      ]}
    {:name "isappdata"
      :signatures [
        "VALID = isappdata (H, NAME)"
      ]}
    {:name "listdlg"
      :signatures [
        "[SEL, OK] = listdlg (KEY, VALUE, ...)"
      ]}
    {:name "listfonts"
      :signatures [
        "fonts = listfonts ()"
        "fonts = listfonts (H)"
      ]}
    {:name "movegui"
      :signatures [
        "movegui"
        "movegui (H)"
        "movegui (POS)"
        "movegui (H, POS)"
        "movegui (H, EVENT)"
        "movegui (H, EVENT, POS)"
      ]}
    {:name "msgbox"
      :signatures [
        "H = msgbox (MSG)"
        "H = msgbox (MSG, TITLE)"
        "H = msgbox (MSG, TITLE, ICON)"
        "H = msgbox (MSG, TITLE, \"custom\", CDATA)"
        "H = msgbox (MSG, TITLE, \"custom\", CDATA, COLORMAP)"
        "H = msgbox (..., OPT)"
      ]}
    {:name "questdlg"
      :signatures [
        "BTN = questdlg (MSG)"
        "BTN = questdlg (MSG, TITLE)"
        "BTN = questdlg (MSG, TITLE, DEFAULT)"
        "BTN = questdlg (MSG, TITLE, BTN1, BTN2, DEFAULT)"
        "BTN = questdlg (MSG, TITLE, BTN1, BTN2, BTN3, DEFAULT)"
      ]}
    {:name "rmappdata"
      :signatures [
        "rmappdata (H, NAME)"
        "rmappdata (H, NAME1, NAME2, ...)"
      ]}
    {:name "setappdata"
      :signatures [
        "setappdata (H, NAME, VALUE)"
        "setappdata (H, NAME1, VALUE1, NAME2, VALUE3, ...)"
        "setappdata (H, {NAME1, NAME2, ...}, {VALUE1, VALUE2, ...})"
      ]}
    {:name "uibuttongroup"
      :signatures [
        "HUI = uibuttongroup ()"
        "HUI = uibuttongroup (PROPERTY, VALUE, ...)"
        "HUI = uibuttongroup (PARENT)"
        "HUI = uibuttongroup (PARENT, PROPERTY, VALUE, ...)"
        "uibuttongroup (H)"
      ]}
    {:name "uicontextmenu"
      :signatures [
        "HUI = uicontextmenu (PROPERTY, VALUE, ...)"
        "HUI = uicontextmenu (H, PROPERTY, VALUE, ...)"
      ]}
    {:name "uicontrol"
      :signatures [
        "HUI = uicontrol ()"
        "HUI = uicontrol (PROPERTY, VALUE, ...)"
        "HUI = uicontrol (PARENT)"
        "HUI = uicontrol (PARENT, PROPERTY, VALUE, ...)"
        "uicontrol (H)"
      ]}
    {:name "uifigure"
      :signatures [
        "H = uifigure ()"
        "H = uifigure (\"PROPERTY\", VALUE, ...)"
      ]}
    {:name "uigetdir"
      :signatures [
        "DIRNAME = uigetdir ()"
        "DIRNAME = uigetdir (INIT_PATH)"
        "DIRNAME = uigetdir (INIT_PATH, DIALOG_NAME)"
      ]}
    {:name "uigetfile"
      :signatures [
        "[FNAME, FPATH, FLTIDX] = uigetfile ()"
        "[...] = uigetfile (FLT)"
        "[...] = uigetfile (FLT, DIALOG_NAME)"
        "[...] = uigetfile (FLT, DIALOG_NAME, DEFAULT_FILE)"
        "[...] = uigetfile (..., \"MultiSelect\", MODE)"
      ]}
    {:name "uimenu"
      :signatures [
        "HUI = uimenu (PROPERTY, VALUE, ...)"
        "HUI = uimenu (H, PROPERTY, VALUE, ...)"
      ]}
    {:name "uipanel"
      :signatures [
        "HUI = uipanel ()"
        "HUI = uipanel (PROPERTY, VALUE, ...)"
        "HUI = uipanel (PARENT)"
        "HUI = uipanel (PARENT, PROPERTY, VALUE, ...)"
      ]}
    {:name "uipushtool"
      :signatures [
        "HUI = uipushtool ()"
        "HUI = uipushtool (PROPERTY, VALUE, ...)"
        "HUI = uipushtool (PARENT)"
        "HUI = uipushtool (PARENT, PROPERTY, VALUE, ...)"
      ]}
    {:name "uiputfile"
      :signatures [
        "[FNAME, FPATH, FLTIDX] = uiputfile ()"
        "[FNAME, FPATH, FLTIDX] = uiputfile (FLT)"
        "[FNAME, FPATH, FLTIDX] = uiputfile (FLT, DIALOG_NAME)"
        "[FNAME, FPATH, FLTIDX] = uiputfile (FLT, DIALOG_NAME, DEFAULT_FILE)"
      ]}
    {:name "uiresume"
      :signatures [
        "uiresume (H)"
      ]}
    {:name "uisetfont"
      :signatures [
        "uisetfont ()"
        "uisetfont (H)"
        "uisetfont (FONTSTRUCT)"
        "uisetfont (..., TITLE)"
        "FONTSTRUCT = uisetfont (...)"
      ]}
    {:name "uitable"
      :signatures [
        "HUI = uitable (PROPERTY, VALUE, ...)"
        "HUI = uitable (PARENT, PROPERTY, VALUE, ...)"
      ]}
    {:name "uitoggletool"
      :signatures [
        "HUI = uitoggletool ()"
        "HUI = uitoggletool (PROPERTY, VALUE, ...)"
        "HUI = uitoggletool (PARENT)"
        "HUI = uitoggletool (PARENT, PROPERTY, VALUE, ...)"
      ]}
    {:name "uitoolbar"
      :signatures [
        "HUI = uitoolbar ()"
        "HUI = uitoolbar (PROPERTY, VALUE, ...)"
        "HUI = uitoolbar (PARENT)"
        "HUI = uitoolbar (PARENT, PROPERTY, VALUE, ...)"
      ]}
    {:name "uiwait"
      :signatures [
        "uiwait"
        "uiwait (H)"
        "uiwait (H, TIMEOUT)"
      ]}
    {:name "waitbar"
      :signatures [
        "H = waitbar (FRAC)"
        "H = waitbar (FRAC, MSG)"
        "H = waitbar (..., \"createcancelbtn\", FCN, ...)"
        "H = waitbar (..., PROP, VAL, ...)"
        "waitbar (FRAC)"
        "waitbar (FRAC, H)"
        "waitbar (FRAC, H, MSG)"
      ]}
    {:name "waitforbuttonpress"
      :signatures [
        "B = waitforbuttonpress ()"
      ]}
    {:name "warndlg"
      :signatures [
        "warndlg ()"
        "warndlg (MSG)"
        "warndlg (MSG, TITLE)"
        "warndlg (MSG, TITLE, OPT)"
        "H = warndlg (...)"
      ]}
  ])

(def +help+
  "Octave builtins from the `help` category, with signatures."
  [
    {:name "bessel"
      :signatures [
        "[J, IERR] = besselj (ALPHA, X, OPT)"
        "[Y, IERR] = bessely (ALPHA, X, OPT)"
        "[I, IERR] = besseli (ALPHA, X, OPT)"
        "[K, IERR] = besselk (ALPHA, X, OPT)"
        "[H, IERR] = besselh (ALPHA, K, X, OPT)"
      ]}
    {:name "debug"
      :signatures [
        "debug ()"
      ]}
    {:name "doc_cache_create"
      :signatures [
        "doc_cache_create (OUT_FILE, DIRECTORY)"
        "doc_cache_create (OUT_FILE)"
        "doc_cache_create ()"
      ]}
    {:name "error_ids"
      :signatures [
      ]}
    {:name "get_first_help_sentence"
      :signatures [
        "TEXT = get_first_help_sentence (NAME)"
        "TEXT = get_first_help_sentence (NAME, MAX_LEN)"
        "[TEXT, STATUS] = get_first_help_sentence (...)"
      ]}
    {:name "help"
      :signatures [
        "help NAME"
        "help --list"
        "help ."
        "help"
        "HELP_TEXT = help (...)"
      ]}
    {:name "lookfor"
      :signatures [
        "lookfor STR"
        "lookfor -all STR"
        "[FCN, HELP1STR] = lookfor (STR)"
        "[FCN, HELP1STR] = lookfor (\"-all\", STR)"
      ]}
    {:name "print_usage"
      :signatures [
        "print_usage ()"
        "print_usage (NAME)"
      ]}
    {:name "slash"
      :signatures [
      ]}
    {:name "type"
      :signatures [
        "type NAME ..."
        "type -q NAME ..."
        "text = type (\"NAME\", ...)"
      ]}
    {:name "warning_ids"
      :signatures [
      ]}
    {:name "which"
      :signatures [
        "which name ..."
      ]}
  ])

(def +image+
  "Octave builtins from the `image` category, with signatures."
  [
    {:name "autumn"
      :signatures [
        "MAP = autumn ()"
        "MAP = autumn (N)"
      ]}
    {:name "bone"
      :signatures [
        "MAP = bone ()"
        "MAP = bone (N)"
      ]}
    {:name "brighten"
      :signatures [
        "MAP_OUT = brighten (BETA)"
        "MAP_OUT = brighten (MAP, BETA)"
        "MAP_OUT = brighten (H, BETA)"
        "brighten (...)"
      ]}
    {:name "cmpermute"
      :signatures [
        "[Y, NEWMAP] = cmpermute (X, MAP)"
        "[Y, NEWMAP] = cmpermute (X, MAP, INDEX)"
      ]}
    {:name "cmunique"
      :signatures [
        "[Y, NEWMAP] = cmunique (X, MAP)"
        "[Y, NEWMAP] = cmunique (RGB)"
        "[Y, NEWMAP] = cmunique (I)"
      ]}
    {:name "colorcube"
      :signatures [
        "MAP = colorcube ()"
        "MAP = colorcube (N)"
      ]}
    {:name "colormap"
      :signatures [
        "CMAP = colormap ()"
        "CMAP = colormap (MAP)"
        "CMAP = colormap (\"default\")"
        "CMAP = colormap (MAP_NAME)"
        "CMAP = colormap (HAX, ...)"
        "colormap MAP_NAME"
      ]}
    {:name "contrast"
      :signatures [
        "CMAP = contrast (X)"
        "CMAP = contrast (X, N)"
      ]}
    {:name "cool"
      :signatures [
        "MAP = cool ()"
        "MAP = cool (N)"
      ]}
    {:name "copper"
      :signatures [
        "MAP = copper ()"
        "MAP = copper (N)"
      ]}
    {:name "cubehelix"
      :signatures [
        "MAP = cubehelix ()"
        "MAP = cubehelix (N)"
        "MAP = cubehelix (N, START, ROTS, HUE, GAMMA)"
      ]}
    {:name "flag"
      :signatures [
        "MAP = flag ()"
        "MAP = flag (N)"
      ]}
    {:name "frame2im"
      :signatures [
        "[X, MAP] = frame2im (FRAME)"
      ]}
    {:name "getframe"
      :signatures [
        "FRAME = getframe ()"
        "FRAME = getframe (HAX)"
        "FRAME = getframe (HFIG)"
        "FRAME = getframe (..., RECT)"
      ]}
    {:name "gray"
      :signatures [
        "MAP = gray ()"
        "MAP = gray (N)"
      ]}
    {:name "gray2ind"
      :signatures [
        "IMG = gray2ind (I)"
        "IMG = gray2ind (I, N)"
        "IMG = gray2ind (BW)"
        "IMG = gray2ind (BW, N)"
        "[IMG, MAP] = gray2ind (...)"
      ]}
    {:name "hot"
      :signatures [
        "MAP = hot ()"
        "MAP = hot (N)"
      ]}
    {:name "hsv"
      :signatures [
        "MAP = hsv ()"
        "MAP = hsv (N)"
      ]}
    {:name "hsv2rgb"
      :signatures [
        "RGB_MAP = hsv2rgb (HSV_MAP)"
        "RGB_IMG = hsv2rgb (HSV_IMG)"
      ]}
    {:name "im2double"
      :signatures [
        "DIMG = im2double (IMG)"
        "DIMG = im2double (IMG, \"indexed\")"
      ]}
    {:name "im2frame"
      :signatures [
        "FRAME = im2frame (RGB)"
        "FRAME = im2frame (X, MAP)"
      ]}
    {:name "image"
      :signatures [
        "image (IMG)"
        "image (X, Y, IMG)"
        "image (..., \"PROP\", VAL, ...)"
        "image (\"PROP1\", VAL1, ...)"
        "H = image (...)"
      ]}
    {:name "imagesc"
      :signatures [
        "imagesc (IMG)"
        "imagesc (X, Y, IMG)"
        "imagesc (..., CLIMITS)"
        "imagesc (..., \"PROP\", VAL, ...)"
        "imagesc (\"PROP1\", VAL1, ...)"
        "imagesc (HAX, ...)"
        "H = imagesc (...)"
      ]}
    {:name "imfinfo"
      :signatures [
        "INFO = imfinfo (FILENAME)"
        "INFO = imfinfo (URL)"
        "INFO = imfinfo (..., EXT)"
      ]}
    {:name "imformats"
      :signatures [
        "imformats ()"
        "FORMATS = imformats (EXT)"
        "FORMATS = imformats (FORMAT)"
        "FORMATS = imformats (\"add\", FORMAT)"
        "FORMATS = imformats (\"remove\", EXT)"
        "FORMATS = imformats (\"update\", EXT, FORMAT)"
        "FORMATS = imformats (\"factory\")"
      ]}
    {:name "imread"
      :signatures [
        "[IMG, MAP, ALPHA] = imread (FILENAME)"
        "[...] = imread (URL)"
        "[...] = imread (..., EXT)"
        "[...] = imread (..., IDX)"
        "[...] = imread (..., PARAM1, VALUE1, ...)"
      ]}
    {:name "imshow"
      :signatures [
        "imshow (IM)"
        "imshow (IM, LIMITS)"
        "imshow (IM, MAP)"
        "imshow (RGB, ...)"
        "imshow (FILENAME)"
        "imshow (..., STRING_PARAM1, VALUE1, ...)"
        "H = imshow (...)"
      ]}
    {:name "imwrite"
      :signatures [
        "imwrite (IMG, FILENAME)"
        "imwrite (IMG, FILENAME, EXT)"
        "imwrite (IMG, MAP, FILENAME)"
        "imwrite (..., PARAM1, VAL1, ...)"
      ]}
    {:name "ind2gray"
      :signatures [
        "I = ind2gray (X, MAP)"
      ]}
    {:name "ind2rgb"
      :signatures [
        "RGB = ind2rgb (X, MAP)"
        "[R, G, B] = ind2rgb (X, MAP)"
      ]}
    {:name "iscolormap"
      :signatures [
        "TF = iscolormap (CMAP)"
      ]}
    {:name "jet"
      :signatures [
        "MAP = jet ()"
        "MAP = jet (N)"
      ]}
    {:name "movie"
      :signatures [
        "movie (MOV)"
        "movie (MOV, N)"
        "movie (MOV, N, FPS)"
        "movie (H, ...)"
      ]}
    {:name "ocean"
      :signatures [
        "MAP = ocean ()"
        "MAP = ocean (N)"
      ]}
    {:name "pink"
      :signatures [
        "MAP = pink ()"
        "MAP = pink (N)"
      ]}
    {:name "prism"
      :signatures [
        "MAP = prism ()"
        "MAP = prism (N)"
      ]}
    {:name "rainbow"
      :signatures [
        "MAP = rainbow ()"
        "MAP = rainbow (N)"
      ]}
    {:name "rgb2gray"
      :signatures [
        "I = rgb2gray (RGB_IMG)"
        "GRAY_MAP = rgb2gray (RGB_MAP)"
      ]}
    {:name "rgb2hsv"
      :signatures [
        "HSV_MAP = rgb2hsv (RGB_MAP)"
        "HSV_IMG = rgb2hsv (RGB_IMG)"
      ]}
    {:name "rgb2ind"
      :signatures [
        "[X, MAP] = rgb2ind (RGB)"
        "[X, MAP] = rgb2ind (R, G, B)"
      ]}
    {:name "rgbplot"
      :signatures [
        "rgbplot (CMAP)"
        "rgbplot (CMAP, STYLE)"
        "H = rgbplot (...)"
      ]}
    {:name "spinmap"
      :signatures [
        "spinmap ()"
        "spinmap (T)"
        "spinmap (T, INC)"
        "spinmap (\"inf\")"
      ]}
    {:name "spring"
      :signatures [
        "MAP = spring ()"
        "MAP = spring (N)"
      ]}
    {:name "summer"
      :signatures [
        "MAP = summer ()"
        "MAP = summer (N)"
      ]}
    {:name "turbo"
      :signatures [
        "MAP = turbo ()"
        "MAP = turbo (N)"
      ]}
    {:name "viridis"
      :signatures [
        "MAP = viridis ()"
        "MAP = viridis (N)"
      ]}
    {:name "white"
      :signatures [
        "MAP = white ()"
        "MAP = white (N)"
      ]}
    {:name "winter"
      :signatures [
        "MAP = winter ()"
        "MAP = winter (N)"
      ]}
  ])

(def +io+
  "Octave builtins from the `io` category, with signatures."
  [
    {:name "beep"
      :signatures [
        "beep ()"
      ]}
    {:name "csvread"
      :signatures [
        "X = csvread (FILENAME)"
        "X = csvread (FILENAME, DLM_OPT1, ...)"
      ]}
    {:name "csvwrite"
      :signatures [
        "csvwrite (FILENAME, X)"
        "csvwrite (FILENAME, X, DLM_OPT1, ...)"
      ]}
    {:name "dlmwrite"
      :signatures [
        "dlmwrite (FILE, M)"
        "dlmwrite (FILE, M, DELIM, R, C)"
        "dlmwrite (FILE, M, KEY, VAL ...)"
        "dlmwrite (FILE, M, \"-append\", ...)"
        "dlmwrite (FID, ...)"
      ]}
    {:name "fileread"
      :signatures [
        "STR = fileread (FILENAME)"
      ]}
    {:name "importdata"
      :signatures [
        "A = importdata (FNAME)"
        "A = importdata (FNAME, DELIMITER)"
        "A = importdata (FNAME, DELIMITER, HEADER_ROWS)"
        "[A, DELIMITER] = importdata (...)"
        "[A, DELIMITER, HEADER_ROWS] = importdata (...)"
      ]}
    {:name "is_valid_file_id"
      :signatures [
        "TF = is_valid_file_id (FID)"
      ]}
  ])

(def +java+
  "Octave builtins from the `java` category, with signatures."
  [
    {:name "javaArray"
      :signatures [
        "JARY = javaArray (CLASSNAME, SZ)"
        "JARY = javaArray (CLASSNAME, M, N, ...)"
      ]}
    {:name "java_get"
      :signatures [
        "VAL = java_get (OBJ, NAME)"
      ]}
    {:name "java_set"
      :signatures [
        "OBJ = java_set (OBJ, NAME, VAL)"
      ]}
    {:name "javaaddpath"
      :signatures [
        "javaaddpath (CLSPATH)"
        "javaaddpath (CLSPATH1, ...)"
        "javaaddpath ({CLSPATH1, ...})"
        "javaaddpath (..., \"-end\")"
      ]}
    {:name "javachk"
      :signatures [
        "MSG = javachk (FEATURE)"
        "MSG = javachk (FEATURE, CALLER)"
      ]}
    {:name "javaclasspath"
      :signatures [
        "javaclasspath ()"
        "DPATH = javaclasspath ()"
        "[DPATH, SPATH] = javaclasspath ()"
        "CLSPATH = javaclasspath (WHAT)"
      ]}
    {:name "javamem"
      :signatures [
        "javamem ()"
        "JMEM = javamem ()"
      ]}
    {:name "javarmpath"
      :signatures [
        "javarmpath (CLSPATH)"
        "javarmpath (CLSPATH1, ...)"
        "javarmpath ({CLSPATH1, ...})"
      ]}
    {:name "usejava"
      :signatures [
        "TF = usejava (FEATURE)"
      ]}
  ])

(def +legacy+
  "Octave builtins from the `legacy` category, with signatures."
  [
    {:name "@inline/argnames"
      :signatures [
        "ARGS = argnames (FOBJ)"
      ]}
    {:name "@inline/cat"
      :signatures [
        "cat (FOBJ1, DOTS)"
      ]}
    {:name "@inline/char"
      :signatures [
        "FCNSTR = char (FOBJ)"
      ]}
    {:name "@inline/disp"
      :signatures [
        "disp (FOBJ)"
      ]}
    {:name "@inline/exist"
      :signatures [
        "C = exist (FOBJ)"
      ]}
    {:name "@inline/feval"
      :signatures [
        "feval (FOBJ, ...)"
      ]}
    {:name "@inline/formula"
      :signatures [
        "FCNSTR = formula (FOBJ)"
      ]}
    {:name "@inline/horzcat"
      :signatures [
        "horzcat (FOBJ1, DOTS)"
      ]}
    {:name "@inline/inline"
      :signatures [
        "FOBJ = inline (STR)"
        "FOBJ = inline (STR, ARG1, ...)"
        "FOBJ = inline (STR, N)"
      ]}
    {:name "@inline/nargin"
      :signatures [
        "N = nargin (FOBJ)"
      ]}
    {:name "@inline/nargout"
      :signatures [
        "N = nargout (FOBJ)"
      ]}
    {:name "@inline/subsref"
      :signatures [
        "VALUE = subsref (FOBJ, IDX)"
      ]}
    {:name "@inline/symvar"
      :signatures [
        "ARGS = symvar (FOBJ)"
      ]}
    {:name "@inline/vectorize"
      :signatures [
        "VFCN = vectorize (FOBJ)"
      ]}
    {:name "@inline/vertcat"
      :signatures [
        "vertcat (FOBJ1, DOTS)"
      ]}
    {:name "findstr"
      :signatures [
        "V = findstr (S, T)"
        "V = findstr (S, T, OVERLAP)"
      ]}
    {:name "flipdim"
      :signatures [
        "B = flipdim (A)"
        "B = flipdim (A, DIM)"
      ]}
    {:name "genvarname"
      :signatures [
        "VARNAME = genvarname (STR)"
        "VARNAME = genvarname (STR, EXCLUSIONS)"
      ]}
    {:name "isdir"
      :signatures [
        "TF = isdir (F)"
      ]}
    {:name "isequalwithequalnans"
      :signatures [
        "TF = isequalwithequalnans (X1, X2, ...)"
      ]}
    {:name "isstr"
      :signatures [
        "TF = isstr (X)"
      ]}
    {:name "maxNumCompThreads"
      :signatures [
        "N = maxNumCompThreads ()"
        "N_OLD = maxNumCompThreads (N)"
        "N_OLD = maxNumCompThreads (\"automatic\")"
      ]}
    {:name "setstr"
      :signatures [
        "S = setstr (X)"
      ]}
    {:name "strmatch"
      :signatures [
        "IDX = strmatch (S, A)"
        "IDX = strmatch (S, A, \"exact\")"
      ]}
    {:name "strread"
      :signatures [
        "[A, ...] = strread (STR)"
        "[A, ...] = strread (STR, FORMAT)"
        "[A, ...] = strread (STR, FORMAT, FORMAT_REPEAT)"
        "[A, ...] = strread (STR, FORMAT, PROP1, VALUE1, ...)"
        "[A, ...] = strread (STR, FORMAT, FORMAT_REPEAT, PROP1, VALUE1, ...)"
      ]}
    {:name "textread"
      :signatures [
        "[A, ...] = textread (FILENAME)"
        "[A, ...] = textread (FILENAME, FORMAT)"
        "[A, ...] = textread (FILENAME, FORMAT, N)"
        "[A, ...] = textread (FILENAME, FORMAT, PROP1, VALUE1, ...)"
        "[A, ...] = textread (FILENAME, FORMAT, N, PROP1, VALUE1, ...)"
      ]}
    {:name "vectorize"
      :signatures [
        "VFCN = vectorize (FCN)"
      ]}
  ])

(def +linear-algebra+
  "Octave builtins from the `linear-algebra` category, with signatures."
  [
    {:name "bandwidth"
      :signatures [
        "BW = bandwidth (A, TYPE)"
        "[LOWER, UPPER] = bandwidth (A)"
      ]}
    {:name "commutation_matrix"
      :signatures [
        "K = commutation_matrix (M, N)"
      ]}
    {:name "cond"
      :signatures [
        "C = cond (A)"
        "C = cond (A, P)"
      ]}
    {:name "condeig"
      :signatures [
        "C = condeig (A)"
        "[V, LAMBDA, C] = condeig (A)"
      ]}
    {:name "condest"
      :signatures [
        "CEST = condest (A)"
        "CEST = condest (A, T)"
        "CEST = condest (A, AINVFCN)"
        "CEST = condest (A, AINVFCN, T)"
        "CEST = condest (A, AINVFCN, T, P1, P2, ...)"
        "CEST = condest (AFCN, AINVFCN)"
        "CEST = condest (AFCN, AINVFCN, T)"
        "CEST = condest (AFCN, AINVFCN, T, P1, P2, ...)"
        "[CEST, V] = condest (...)"
      ]}
    {:name "cross"
      :signatures [
        "Z = cross (X, Y)"
        "Z = cross (X, Y, DIM)"
      ]}
    {:name "duplication_matrix"
      :signatures [
        "D = duplication_matrix (N)"
      ]}
    {:name "expm"
      :signatures [
        "R = expm (A)"
      ]}
    {:name "gls"
      :signatures [
        "[BETA, V, R] = gls (Y, X, O)"
      ]}
    {:name "housh"
      :signatures [
        "[HOUSV, BETA, ZER] = housh (X, J, Z)"
      ]}
    {:name "isbanded"
      :signatures [
        "TF = isbanded (A, LOWER, UPPER)"
      ]}
    {:name "isdefinite"
      :signatures [
        "TF = isdefinite (A)"
        "TF = isdefinite (A, TOL)"
      ]}
    {:name "isdiag"
      :signatures [
        "TF = isdiag (A)"
      ]}
    {:name "ishermitian"
      :signatures [
        "TF = ishermitian (A)"
        "TF = ishermitian (A, TOL)"
        "TF = ishermitian (A, \"skew\")"
        "TF = ishermitian (A, \"skew\", TOL)"
      ]}
    {:name "issymmetric"
      :signatures [
        "TF = issymmetric (A)"
        "TF = issymmetric (A, TOL)"
        "TF = issymmetric (A, \"skew\")"
        "TF = issymmetric (A, \"skew\", TOL)"
      ]}
    {:name "istril"
      :signatures [
        "TF = istril (A)"
      ]}
    {:name "istriu"
      :signatures [
        "TF = istriu (A)"
      ]}
    {:name "krylov"
      :signatures [
        "[U, H, NU] = krylov (A, V, K, EPS1, PFLG)"
      ]}
    {:name "linsolve"
      :signatures [
        "X = linsolve (A, B)"
        "X = linsolve (A, B, OPTS)"
        "[X, R] = linsolve (...)"
      ]}
    {:name "logm"
      :signatures [
        "S = logm (A)"
        "S = logm (A, OPT_ITERS)"
        "[S, ITERS] = logm (...)"
      ]}
    {:name "lscov"
      :signatures [
        "X = lscov (A, B)"
        "X = lscov (A, B, V)"
        "X = lscov (A, B, V, ALG)"
        "[X, STDX, MSE, S] = lscov (...)"
      ]}
    {:name "normest"
      :signatures [
        "NEST = normest (A)"
        "NEST = normest (A, TOL)"
        "[NEST, ITER] = normest (...)"
      ]}
    {:name "normest1"
      :signatures [
        "NEST = normest1 (A)"
        "NEST = normest1 (A, T)"
        "NEST = normest1 (A, T, X0)"
        "NEST = normest1 (AFCN, T, X0, P1, P2, ...)"
        "[NEST, V] = normest1 (A, ...)"
        "[NEST, V, W] = normest1 (A, ...)"
        "[NEST, V, W, ITER] = normest1 (A, ...)"
      ]}
    {:name "null"
      :signatures [
        "Z = null (A)"
        "Z = null (A, TOL)"
      ]}
    {:name "ols"
      :signatures [
        "[BETA, SIGMA, R] = ols (Y, X)"
      ]}
    {:name "ordeig"
      :signatures [
        "LAMBDA = ordeig (A)"
        "LAMBDA = ordeig (A, B)"
      ]}
    {:name "orth"
      :signatures [
        "B = orth (A)"
        "B = orth (A, TOL)"
      ]}
    {:name "planerot"
      :signatures [
        "[G, Y] = planerot (X)"
      ]}
    {:name "qzhess"
      :signatures [
        "[AA, BB, Q, Z] = qzhess (A, B)"
      ]}
    {:name "rank"
      :signatures [
        "K = rank (A)"
        "K = rank (A, TOL)"
      ]}
    {:name "rref"
      :signatures [
        "R = rref (A)"
        "R = rref (A, TOL)"
        "[R, K] = rref (...)"
      ]}
    {:name "subspace"
      :signatures [
        "ANGLE = subspace (A, B)"
      ]}
    {:name "trace"
      :signatures [
        "T = trace (A)"
      ]}
    {:name "vech"
      :signatures [
        "V = vech (X)"
      ]}
    {:name "vecnorm"
      :signatures [
        "N = vecnorm (A)"
        "N = vecnorm (A, P)"
        "N = vecnorm (A, P, DIM)"
      ]}
  ])

(def +miscellaneous+
  "Octave builtins from the `miscellaneous` category, with signatures."
  [
    {:name "bug_report"
      :signatures [
        "bug_report ()"
      ]}
    {:name "bunzip2"
      :signatures [
        "bunzip2 (BZFILE)"
        "bunzip2 (BZFILE, DIR)"
        "FILELIST = bunzip2 (...)"
      ]}
    {:name "cast"
      :signatures [
        "Y = cast (X, \"TYPE\")"
        "Y = cast (X, \"LIKE\", VAR)"
      ]}
    {:name "citation"
      :signatures [
        "citation"
        "citation PACKAGE"
      ]}
    {:name "clearAllMemoizedCaches"
      :signatures [
        "clearAllMemoizedCaches ()"
      ]}
    {:name "clearvars"
      :signatures [
        "clearvars"
        "clearvars PATTERN ..."
        "clearvars -regexp PATTERN ..."
        "clearvars ... -except PATTERN ..."
        "clearvars ... -except -regexp PATTERN ..."
        "clearvars -global ..."
      ]}
    {:name "compare_versions"
      :signatures [
        "TF = compare_versions (V1, V2, OPERATOR)"
      ]}
    {:name "computer"
      :signatures [
        "computer ()"
        "COMP = computer ()"
        "[COMP, MAXSIZE] = computer ()"
        "[COMP, MAXSIZE, ENDIAN] = computer ()"
        "ARCH = computer (\"arch\")"
      ]}
    {:name "copyfile"
      :signatures [
        "copyfile F1 F2"
        "copyfile F1 F2 f"
        "copyfile (F1, F2)"
        "copyfile (F1, F2, 'f')"
        "[STATUS, MSG, MSGID] = copyfile (...)"
      ]}
    {:name "delete"
      :signatures [
        "delete FILE"
        "delete FILE1 FILE2 ..."
        "delete (FILE)"
        "delete (FILE1, FILE2, ...)"
        "delete (HANDLE)"
      ]}
    {:name "dir"
      :signatures [
        "dir"
        "dir DIRECTORY"
        "[LIST] = dir (DIRECTORY)"
      ]}
    {:name "dos"
      :signatures [
        "dos (\"COMMAND\")"
        "STATUS = dos (\"COMMAND\")"
        "[STATUS, TEXT] = dos (\"COMMAND\")"
        "[...] = dos (\"COMMAND\", \"-echo\")"
      ]}
    {:name "edit"
      :signatures [
        "edit NAME"
        "edit FIELD VALUE"
        "VALUE = edit (\"get\", FIELD)"
        "VALUE = edit (\"get\", \"all\")"
      ]}
    {:name "fieldnames"
      :signatures [
        "NAMES = fieldnames (STRUCT)"
        "NAMES = fieldnames (OBJ)"
        "NAMES = fieldnames (JAVAOBJ)"
        "NAMES = fieldnames (\"JAVACLASSNAME\")"
      ]}
    {:name "fileattrib"
      :signatures [
        "fileattrib"
        "fileattrib FILE"
        "fileattrib (FILE)"
        "[STATUS, ATTRIB] = fileattrib (...)"
        "[STATUS, MSG, MSGID] = fileattrib (...)"
      ]}
    {:name "fileparts"
      :signatures [
        "[DIR, NAME, EXT] = fileparts (FILENAME)"
      ]}
    {:name "fullfile"
      :signatures [
        "FILENAME = fullfile (DIR1, DIR2, ..., FILE)"
      ]}
    {:name "getfield"
      :signatures [
        "VAL = getfield (S, FIELD)"
        "VAL = getfield (S, SIDX1, FIELD1, FIDX1, ...)"
      ]}
    {:name "grabcode"
      :signatures [
        "grabcode FILENAME"
        "grabcode URL"
        "CODE_STR = grabcode (...)"
      ]}
    {:name "gunzip"
      :signatures [
        "gunzip (GZFILE)"
        "gunzip (GZFILE, OUTDIR)"
        "FILELIST = gunzip (...)"
      ]}
    {:name "info"
      :signatures [
        "info ()"
      ]}
    {:name "inputParser"
      :signatures [
        "P = inputParser ()"
        "inputParser.Parameters"
        "inputParser.Results"
        "inputParser.Unmatched"
        "inputParser.UsingDefaults"
        "inputParser.CaseSensitive = BOOLEAN"
        "inputParser.FunctionName = NAME"
        "inputParser.KeepUnmatched = BOOLEAN"
        "inputParser.StructExpand = BOOLEAN"
      ]}
    {:name "inputname"
      :signatures [
        "NAMESTR = inputname (N)"
        "NAMESTR = inputname (N, IDS_ONLY)"
      ]}
    {:name "isdeployed"
      :signatures [
        "TF = isdeployed ()"
      ]}
    {:name "isfile"
      :signatures [
        "TF = isfile (F)"
      ]}
    {:name "isfolder"
      :signatures [
        "TF = isfolder (F)"
      ]}
    {:name "ismac"
      :signatures [
        "TF = ismac ()"
      ]}
    {:name "ismethod"
      :signatures [
        "TF = ismethod (OBJ, METHOD)"
        "TF = ismethod (CLASS_NAME, METHOD)"
      ]}
    {:name "ispc"
      :signatures [
        "TF = ispc ()"
      ]}
    {:name "isunix"
      :signatures [
        "TF = isunix ()"
      ]}
    {:name "jupyter_notebook"
      :signatures [
        "NOTEBOOK = jupyter_notebook (NOTEBOOK_FILENAME)"
        "NOTEBOOK = jupyter_notebook (NOTEBOOK_FILENAME, OPTIONS)"
      ]}
    {:name "license"
      :signatures [
        "license"
        "license inuse"
        "license inuse FEATURE"
        "license (\"inuse\")"
        "LICENSE_STRUCT = license (\"inuse\")"
        "LICENSE_STRUCT = license (\"inuse\", FEATURE)"
        "STATUS = license (\"test\", FEATURE)"
        "STATUS = license (\"checkout\", FEATURE)"
        "[STATUS, ERRMSG] = license (\"checkout\", FEATURE)"
      ]}
    {:name "list_primes"
      :signatures [
        "P = list_primes ()"
        "P = list_primes (N)"
      ]}
    {:name "loadobj"
      :signatures [
        "B = loadobj (A)"
      ]}
    {:name "ls"
      :signatures [
        "ls"
        "ls FILENAMES"
        "ls OPTIONS"
        "ls OPTIONS FILENAMES"
        "LIST = ls (...)"
      ]}
    {:name "ls_command"
      :signatures [
        "VAL = ls_command ()"
        "OLD_VAL = ls_command (NEW_VAL)"
      ]}
    {:name "memoize"
      :signatures [
        "MEM_FCN_HANDLE = memoize (FCN_HANDLE)"
      ]}
    {:name "memory"
      :signatures [
        "memory ()"
        "[USERDATA, SYSTEMDATA] = memory ()"
      ]}
    {:name "menu"
      :signatures [
        "CHOICE = menu (TITLE, OPT1, ...)"
        "CHOICE = menu (TITLE, {OPT1, ...})"
      ]}
    {:name "methods"
      :signatures [
        "methods (OBJ)"
        "methods (\"CLASSNAME\")"
        "methods (..., \"-full\")"
        "MTDS = methods (...)"
      ]}
    {:name "mex"
      :signatures [
        "mex [-options] file ..."
        "status = mex (...)"
      ]}
    {:name "mexext"
      :signatures [
        "EXT = mexext ()"
      ]}
    {:name "mkdir"
      :signatures [
        "mkdir DIRNAME"
        "mkdir PARENT DIRNAME"
        "mkdir (DIRNAME)"
        "mkdir (PARENT, DIRNAME)"
        "[STATUS, MSG, MSGID] = mkdir (...)"
      ]}
    {:name "mkoctfile"
      :signatures [
        "mkoctfile [-options] file ..."
        "[OUTPUT, STATUS] = mkoctfile (...)"
      ]}
    {:name "movefile"
      :signatures [
        "movefile F1"
        "movefile F1 F2"
        "movefile F1 F2 f"
        "movefile (F1)"
        "movefile (F1, F2)"
        "movefile (F1, F2, 'f')"
        "[STATUS] = movefile (...)"
        "[STATUS, MSG] = movefile (...)"
        "[STATUS, MSG, MSGID] = movefile (...)"
      ]}
    {:name "mustBeFinite"
      :signatures [
        "mustBeFinite (X)"
      ]}
    {:name "mustBeGreaterThan"
      :signatures [
        "mustBeGreaterThan (X, C)"
      ]}
    {:name "mustBeGreaterThanOrEqual"
      :signatures [
        "mustBeGreaterThanOrEqual (X, C)"
      ]}
    {:name "mustBeInteger"
      :signatures [
        "mustBeInteger (X)"
      ]}
    {:name "mustBeLessThan"
      :signatures [
        "mustBeLessThan (X, C)"
      ]}
    {:name "mustBeLessThanOrEqual"
      :signatures [
        "mustBeLessThanOrEqual (X, C)"
      ]}
    {:name "mustBeMember"
      :signatures [
        "mustBeMember (X, VALID)"
      ]}
    {:name "mustBeNegative"
      :signatures [
        "mustBeNegative (X)"
      ]}
    {:name "mustBeNonNan"
      :signatures [
        "mustBeNonNan (X)"
      ]}
    {:name "mustBeNonempty"
      :signatures [
        "mustBeNonempty (X)"
      ]}
    {:name "mustBeNonnegative"
      :signatures [
        "mustBeNonnegative (X)"
      ]}
    {:name "mustBeNonpositive"
      :signatures [
        "mustBeNonpositive (X)"
      ]}
    {:name "mustBeNonsparse"
      :signatures [
        "mustBeNonsparse (X)"
      ]}
    {:name "mustBeNonzero"
      :signatures [
        "mustBeNonzero (X)"
      ]}
    {:name "mustBeNumeric"
      :signatures [
        "mustBeNumeric (X)"
      ]}
    {:name "mustBeNumericOrLogical"
      :signatures [
        "mustBeNumericOrLogical (X)"
      ]}
    {:name "mustBePositive"
      :signatures [
        "mustBePositive (X)"
      ]}
    {:name "mustBeReal"
      :signatures [
        "mustBeReal (X)"
      ]}
    {:name "namedargs2cell"
      :signatures [
        "C = namedargs2cell (S)"
      ]}
    {:name "namelengthmax"
      :signatures [
        "N = namelengthmax ()"
      ]}
    {:name "nargchk"
      :signatures [
        "MSGSTR = nargchk (MINARGS, MAXARGS, NARGS)"
        "MSGSTR = nargchk (MINARGS, MAXARGS, NARGS, \"string\")"
        "MSGSTRUCT = nargchk (MINARGS, MAXARGS, NARGS, \"struct\")"
      ]}
    {:name "narginchk"
      :signatures [
        "narginchk (MINARGS, MAXARGS)"
      ]}
    {:name "nargoutchk"
      :signatures [
        "nargoutchk (MINARGS, MAXARGS)"
        "MSGSTR = nargoutchk (MINARGS, MAXARGS, NARGS)"
        "MSGSTR = nargoutchk (MINARGS, MAXARGS, NARGS, \"string\")"
        "MSGSTRUCT = nargoutchk (MINARGS, MAXARGS, NARGS, \"struct\")"
      ]}
    {:name "news"
      :signatures [
        "news"
        "news PACKAGE"
      ]}
    {:name "nthargout"
      :signatures [
        "ARG = nthargout (N, FCN, ...)"
        "ARG = nthargout (N, NTOT, FCN, ...)"
      ]}
    {:name "open"
      :signatures [
        "open FILE"
        "OUTPUT = open (FILE)"
      ]}
    {:name "orderfields"
      :signatures [
        "SOUT = orderfields (S1)"
        "SOUT = orderfields (S1, S2)"
        "SOUT = orderfields (S1, {CELLSTR})"
        "SOUT = orderfields (S1, P)"
        "[SOUT, P] = orderfields (...)"
      ]}
    {:name "pack"
      :signatures [
        "pack ()"
      ]}
    {:name "parseparams"
      :signatures [
        "[REG, PROP] = parseparams (PARAMS)"
        "[REG, VAR1, ...] = parseparams (PARAMS, NAME1, DEFAULT1, ...)"
      ]}
    {:name "perl"
      :signatures [
        "OUTPUT = perl (SCRIPTFILE)"
        "OUTPUT = perl (SCRIPTFILE, ARGUMENT1, ARGUMENT2, ...)"
        "[OUTPUT, STATUS] = perl (...)"
      ]}
    {:name "publish"
      :signatures [
        "publish (FILE)"
        "publish (FILE, OUTPUT_FORMAT)"
        "publish (FILE, OPTION1, VALUE1, ...)"
        "publish (FILE, OPTIONS)"
        "OUTPUT_FILE = publish (FILE, ...)"
      ]}
    {:name "python"
      :signatures [
        "OUTPUT = python (SCRIPTFILE)"
        "OUTPUT = python (SCRIPTFILE, ARGUMENT1, ARGUMENT2, ...)"
        "[OUTPUT, STATUS] = python (...)"
      ]}
    {:name "recycle"
      :signatures [
        "VAL = recycle ()"
        "OLD_VAL = recycle (NEW_VAL)"
      ]}
    {:name "run"
      :signatures [
        "run SCRIPT"
        "run (\"SCRIPT\")"
      ]}
    {:name "saveobj"
      :signatures [
        "B = saveobj (A)"
      ]}
    {:name "setfield"
      :signatures [
        "SOUT = setfield (S, FIELD, VAL)"
        "SOUT = setfield (S, SIDX1, FIELD1, FIDX1, SIDX2, FIELD2, FIDX2, ..., VAL)"
      ]}
    {:name "substruct"
      :signatures [
        "S = substruct (TYPE, SUBS, ...)"
      ]}
    {:name "swapbytes"
      :signatures [
        "Y = swapbytes (X)"
      ]}
    {:name "symvar"
      :signatures [
        "VARS = symvar (STR)"
      ]}
    {:name "tar"
      :signatures [
        "FILELIST = tar (TARFILE, FILES)"
        "FILELIST = tar (TARFILE, FILES, ROOTDIR)"
      ]}
    {:name "unix"
      :signatures [
        "unix (\"COMMAND\")"
        "STATUS = unix (\"COMMAND\")"
        "[STATUS, TEXT] = unix (\"COMMAND\")"
        "[...] = unix (\"COMMAND\", \"-echo\")"
      ]}
    {:name "unpack"
      :signatures [
        "FILES = unpack (FILE)"
        "FILES = unpack (FILE, DIR)"
        "FILES = unpack (FILE, DIR, FILETYPE)"
      ]}
    {:name "untar"
      :signatures [
        "untar (TARFILE)"
        "untar (TARFILE, DIR)"
        "FILELIST = untar (...)"
      ]}
    {:name "unzip"
      :signatures [
        "unzip (ZIPFILE)"
        "unzip (ZIPFILE, DIR)"
        "FILELIST = unzip (...)"
      ]}
    {:name "validateattributes"
      :signatures [
        "validateattributes (A, CLASSES, ATTRIBUTES)"
        "validateattributes (A, CLASSES, ATTRIBUTES, ARG_IDX)"
        "validateattributes (A, CLASSES, ATTRIBUTES, FUNC_NAME)"
        "validateattributes (A, CLASSES, ATTRIBUTES, FUNC_NAME, ARG_NAME)"
        "validateattributes (A, CLASSES, ATTRIBUTES, FUNC_NAME, ARG_NAME, ARG_IDX)"
      ]}
    {:name "ver"
      :signatures [
        "ver"
        "ver Octave"
        "ver PACKAGE"
        "v = ver (...)"
      ]}
    {:name "verLessThan"
      :signatures [
        "TF = verLessThan (PACKAGE, VERSION)"
      ]}
    {:name "version"
      :signatures [
        "V = version ()"
        "[V, D] = version ()"
        "V = version (FEATURE)"
      ]}
    {:name "what"
      :signatures [
        "what"
        "what DIR"
        "w = what (DIR)"
      ]}
    {:name "zip"
      :signatures [
        "FILELIST = zip (ZIPFILE, FILES)"
        "FILELIST = zip (ZIPFILE, FILES, ROOTDIR)"
      ]}
  ])

(def +ode+
  "Octave builtins from the `ode` category, with signatures."
  [
    {:name "decic"
      :signatures [
        "[Y0_NEW, YP0_NEW] = decic (FCN, T0, Y0, FIXED_Y0, YP0, FIXED_YP0)"
        "[Y0_NEW, YP0_NEW] = decic (FCN, T0, Y0, FIXED_Y0, YP0, FIXED_YP0, OPTIONS)"
        "[Y0_NEW, YP0_NEW, RESNORM] = decic (...)"
      ]}
    {:name "ode15i"
      :signatures [
        "[T, Y] = ode15i (FCN, TRANGE, Y0, YP0)"
        "[T, Y] = ode15i (FCN, TRANGE, Y0, YP0, ODE_OPT)"
        "[T, Y, TE, YE, IE] = ode15i (...)"
        "SOLUTION = ode15i (...)"
        "ode15i (...)"
      ]}
    {:name "ode15s"
      :signatures [
        "[T, Y] = ode15s (FCN, TRANGE, Y0)"
        "[T, Y] = ode15s (FCN, TRANGE, Y0, ODE_OPT)"
        "[T, Y, TE, YE, IE] = ode15s (...)"
        "SOLUTION = ode15s (...)"
        "ode15s (...)"
      ]}
    {:name "ode23"
      :signatures [
        "[T, Y] = ode23 (FCN, TRANGE, INIT)"
        "[T, Y] = ode23 (FCN, TRANGE, INIT, ODE_OPT)"
        "[T, Y, TE, YE, IE] = ode23 (...)"
        "SOLUTION = ode23 (...)"
        "ode23 (...)"
      ]}
    {:name "ode23s"
      :signatures [
        "[T, Y] = ode23s (FCN, TRANGE, INIT)"
        "[T, Y] = ode23s (FCN, TRANGE, INIT, ODE_OPT)"
        "[T, Y] = ode23s (..., PAR1, PAR2, ...)"
        "[T, Y, TE, YE, IE] = ode23s (...)"
        "SOLUTION = ode23s (...)"
      ]}
    {:name "ode45"
      :signatures [
        "[T, Y] = ode45 (FCN, TRANGE, INIT)"
        "[T, Y] = ode45 (FCN, TRANGE, INIT, ODE_OPT)"
        "[T, Y, TE, YE, IE] = ode45 (...)"
        "SOLUTION = ode45 (...)"
        "ode45 (...)"
      ]}
    {:name "odeget"
      :signatures [
        "VAL = odeget (ODE_OPT, FIELD)"
        "VAL = odeget (ODE_OPT, FIELD, DEFAULT)"
      ]}
    {:name "odeplot"
      :signatures [
        "STOP_SOLVE = odeplot (T, Y, FLAG)"
      ]}
    {:name "odeset"
      :signatures [
        "ODESTRUCT = odeset ()"
        "ODESTRUCT = odeset (\"FIELD1\", VALUE1, \"FIELD2\", VALUE2, ...)"
        "ODESTRUCT = odeset (OLDSTRUCT, \"FIELD1\", VALUE1, \"FIELD2\", VALUE2, ...)"
        "ODESTRUCT = odeset (OLDSTRUCT, NEWSTRUCT)"
        "odeset ()"
      ]}
  ])

(def +optimization+
  "Octave builtins from the `optimization` category, with signatures."
  [
    {:name "fminbnd"
      :signatures [
        "X = fminbnd (FCN, A, B)"
        "X = fminbnd (FCN, A, B, OPTIONS)"
        "[X, FVAL, INFO, OUTPUT] = fminbnd (...)"
      ]}
    {:name "fminsearch"
      :signatures [
        "X = fminsearch (FCN, X0)"
        "X = fminsearch (FCN, X0, OPTIONS)"
        "X = fminsearch (PROBLEM)"
        "[X, FVAL, EXITFLAG, OUTPUT] = fminsearch (...)"
      ]}
    {:name "fminunc"
      :signatures [
        "X = fminunc (FCN, X0)"
        "X = fminunc (FCN, X0, OPTIONS)"
        "[X, FVAL] = fminunc (FCN, ...)"
        "[X, FVAL, INFO] = fminunc (FCN, ...)"
        "[X, FVAL, INFO, OUTPUT] = fminunc (FCN, ...)"
        "[X, FVAL, INFO, OUTPUT, GRAD] = fminunc (FCN, ...)"
        "[X, FVAL, INFO, OUTPUT, GRAD, HESS] = fminunc (FCN, ...)"
      ]}
    {:name "fsolve"
      :signatures [
        "X = fsolve (FCN, X0)"
        "X = fsolve (FCN, X0, OPTIONS)"
        "[X, FVAL] = fsolve (...)"
        "[X, FVAL, INFO] = fsolve (...)"
        "[X, FVAL, INFO, OUTPUT] = fsolve (...)"
        "[X, FVAL, INFO, OUTPUT, FJAC] = fsolve (...)"
      ]}
    {:name "fzero"
      :signatures [
        "X = fzero (FCN, X0)"
        "X = fzero (FCN, X0, OPTIONS)"
        "[X, FVAL] = fzero (...)"
        "[X, FVAL, INFO] = fzero (...)"
        "[X, FVAL, INFO, OUTPUT] = fzero (...)"
      ]}
    {:name "glpk"
      :signatures [
        "[XOPT, FMIN, ERRNUM, EXTRA] = glpk (C, A, B, LB, UB, CTYPE, VARTYPE, SENSE, PARAM)"
      ]}
    {:name "humps"
      :signatures [
        "Y = humps (X)"
        "[X, Y] = humps (X)"
      ]}
    {:name "lsqnonneg"
      :signatures [
        "X = lsqnonneg (C, D)"
        "X = lsqnonneg (C, D, X0)"
        "X = lsqnonneg (C, D, X0, OPTIONS)"
        "[X, RESNORM] = lsqnonneg (...)"
        "[X, RESNORM, RESIDUAL] = lsqnonneg (...)"
        "[X, RESNORM, RESIDUAL, EXITFLAG] = lsqnonneg (...)"
        "[X, RESNORM, RESIDUAL, EXITFLAG, OUTPUT] = lsqnonneg (...)"
        "[X, RESNORM, RESIDUAL, EXITFLAG, OUTPUT, LAMBDA] = lsqnonneg (...)"
      ]}
    {:name "optimget"
      :signatures [
        "OPTVAL = optimget (OPTIONS, OPTNAME)"
        "OPTVAL = optimget (OPTIONS, OPTNAME, DEFAULT)"
      ]}
    {:name "optimset"
      :signatures [
        "optimset ()"
        "OPTIONS = optimset ()"
        "OPTIONS = optimset (PAR, VAL, ...)"
        "OPTIONS = optimset (OLD, PAR, VAL, ...)"
        "OPTIONS = optimset (OLD, NEW)"
      ]}
    {:name "pqpnonneg"
      :signatures [
        "X = pqpnonneg (C, D)"
        "X = pqpnonneg (C, D, X0)"
        "X = pqpnonneg (C, D, X0, OPTIONS)"
        "[X, MINVAL] = pqpnonneg (...)"
        "[X, MINVAL, EXITFLAG] = pqpnonneg (...)"
        "[X, MINVAL, EXITFLAG, OUTPUT] = pqpnonneg (...)"
        "[X, MINVAL, EXITFLAG, OUTPUT, LAMBDA] = pqpnonneg (...)"
      ]}
    {:name "qp"
      :signatures [
        "[X, OBJ, INFO, LAMBDA] = qp (X0, H)"
        "[X, OBJ, INFO, LAMBDA] = qp (X0, H, Q)"
        "[X, OBJ, INFO, LAMBDA] = qp (X0, H, Q, A, B)"
        "[X, OBJ, INFO, LAMBDA] = qp (X0, H, Q, A, B, LB, UB)"
        "[X, OBJ, INFO, LAMBDA] = qp (X0, H, Q, A, B, LB, UB, A_LB, A_IN, A_UB)"
        "[X, OBJ, INFO, LAMBDA] = qp (..., OPTIONS)"
      ]}
    {:name "sqp"
      :signatures [
        "[X, OBJ, INFO, ITER, NF, LAMBDA] = sqp (X0, PHI)"
        "[...] = sqp (X0, PHI, G)"
        "[...] = sqp (X0, PHI, G, H)"
        "[...] = sqp (X0, PHI, G, H, LB, UB)"
        "[...] = sqp (X0, PHI, G, H, LB, UB, MAXITER)"
        "[...] = sqp (X0, PHI, G, H, LB, UB, MAXITER, TOLERANCE)"
      ]}
  ])

(def +path+
  "Octave builtins from the `path` category, with signatures."
  [
    {:name "import"
      :signatures [
        "import PACKAGE.FUNCTION"
        "import PACKAGE.CLASS"
        "import PACKAGE.*"
        "import"
        "LIST = import"
      ]}
    {:name "matlabroot"
      :signatures [
        "DIR = matlabroot ()"
      ]}
    {:name "pathdef"
      :signatures [
        "VAL = pathdef ()"
      ]}
    {:name "savepath"
      :signatures [
        "savepath"
        "savepath FILE"
        "STATUS = savepath (...)"
      ]}
  ])

(def +pkg+
  "Octave builtins from the `pkg` category, with signatures."
  [
    {:name "pkg"
      :signatures [
        "pkg COMMAND PKG_NAME"
        "pkg COMMAND OPTION PKG_NAME"
        "[OUT1, ...] = pkg (COMMAND, ... )"
      ]}
  ])

(def ^:private +plot-1+
  "Chunk 1 of Octave builtins from the `plot` category, with signatures."
  [
    {:name "allchild"
      :signatures [
        "H = allchild (HANDLES)"
      ]}
    {:name "ancestor"
      :signatures [
        "PARENT = ancestor (H, TYPE)"
        "PARENT = ancestor (H, TYPE, \"toplevel\")"
      ]}
    {:name "annotation"
      :signatures [
        "annotation (TYPE)"
        "annotation (\"line\", X, Y)"
        "annotation (\"arrow\", X, Y)"
        "annotation (\"doublearrow\", X, Y)"
        "annotation (\"textarrow\", X, Y)"
        "annotation (\"textbox\", POS)"
        "annotation (\"rectangle\", POS)"
        "annotation (\"ellipse\", POS)"
        "annotation (..., PROP, VAL)"
        "annotation (HF, ...)"
        "H = annotation (...)"
      ]}
    {:name "area"
      :signatures [
        "area (Y)"
        "area (X, Y)"
        "area (..., LVL)"
        "area (..., PROP, VAL, ...)"
        "area (HAX, ...)"
        "H = area (...)"
      ]}
    {:name "axes"
      :signatures [
        "axes ()"
        "axes (PROPERTY, VALUE, ...)"
        "axes (HPAR, PROPERTY, VALUE, ...)"
        "axes (HAX)"
        "H = axes (...)"
      ]}
    {:name "axis"
      :signatures [
        "axis ()"
        "axis ([X_LO X_HI])"
        "axis ([X_LO X_HI Y_LO Y_HI])"
        "axis ([X_LO X_HI Y_LO Y_HI Z_LO Z_HI])"
        "axis ([X_LO X_HI Y_LO Y_HI Z_LO Z_HI C_LO C_HI])"
        "axis (OPTION)"
        "axis (OPTION1, OPTION2, ...)"
        "axis (HAX, ...)"
        "LIMITS = axis ()"
      ]}
    {:name "bar"
      :signatures [
        "bar (Y)"
        "bar (X, Y)"
        "bar (..., W)"
        "bar (..., STYLE)"
        "bar (..., PROP, VAL, ...)"
        "bar (HAX, ...)"
        "H = bar (..., PROP, VAL, ...)"
      ]}
    {:name "barh"
      :signatures [
        "barh (Y)"
        "barh (X, Y)"
        "barh (..., W)"
        "barh (..., STYLE)"
        "barh (..., PROP, VAL, ...)"
        "barh (HAX, ...)"
        "H = barh (..., PROP, VAL, ...)"
      ]}
    {:name "box"
      :signatures [
        "box"
        "box on"
        "box off"
        "box (HAX, ...)"
      ]}
    {:name "camlight"
      :signatures [
        "camlight"
        "camlight right"
        "camlight left"
        "camlight headlight"
        "camlight (AZ, EL)"
        "camlight (..., STYLE)"
        "camlight (HL, ...)"
        "camlight (HAX, ...)"
        "H = camlight (...)"
      ]}
    {:name "camlookat"
      :signatures [
        "camlookat ()"
        "camlookat (H)"
        "camlookat (HANDLE_LIST)"
        "camlookat (HAX)"
      ]}
    {:name "camorbit"
      :signatures [
        "camorbit (THETA, PHI)"
        "camorbit (THETA, PHI, COORSYS)"
        "camorbit (THETA, PHI, COORSYS, DIR)"
        "camorbit (THETA, PHI, \"data\")"
        "camorbit (THETA, PHI, \"data\", \"z\")"
        "camorbit (THETA, PHI, \"data\", \"x\")"
        "camorbit (THETA, PHI, \"data\", \"y\")"
        "camorbit (THETA, PHI, \"data\", [X Y Z])"
        "camorbit (THETA, PHI, \"camera\")"
        "camorbit (HAX, ...)"
      ]}
    {:name "campos"
      :signatures [
        "P = campos ()"
        "campos ([X Y Z])"
        "MODE = campos (\"mode\")"
        "campos (MODE)"
        "campos (HAX, ...)"
      ]}
    {:name "camroll"
      :signatures [
        "camroll (THETA)"
        "camroll (HAX, THETA)"
      ]}
    {:name "camtarget"
      :signatures [
        "T = camtarget ()"
        "camtarget ([X Y Z])"
        "MODE = camtarget (\"mode\")"
        "camtarget (MODE)"
        "camtarget (HAX, ...)"
      ]}
    {:name "camup"
      :signatures [
        "UP = camup ()"
        "camup ([X Y Z])"
        "MODE = camup (\"mode\")"
        "camup (MODE)"
        "camup (HAX, ...)"
      ]}
    {:name "camva"
      :signatures [
        "A = camva ()"
        "camva (A)"
        "MODE = camva (\"mode\")"
        "camva (MODE)"
        "camva (HAX, ...)"
      ]}
    {:name "camzoom"
      :signatures [
        "camzoom (ZF)"
        "camzoom (HAX, ZF)"
      ]}
    {:name "caxis"
      :signatures [
        "caxis ([cmin cmax])"
        "caxis (\"auto\")"
        "caxis (\"manual\")"
        "caxis (HAX, ...)"
        "LIMITS = caxis ()"
      ]}
    {:name "cla"
      :signatures [
        "cla"
        "cla reset"
        "cla (HAX)"
        "cla (HAX, \"reset\")"
      ]}
    {:name "clabel"
      :signatures [
        "clabel (C, H)"
        "clabel (C, H, V)"
        "clabel (C, H, \"manual\")"
        "clabel (C)"
        "clabel (..., PROP, VAL, ...)"
        "HLABELS = clabel (...)"
      ]}
    {:name "clf"
      :signatures [
        "clf"
        "clf reset"
        "clf (HFIG)"
        "clf (HFIG, \"reset\")"
        "H = clf (...)"
      ]}
    {:name "close"
      :signatures [
        "close"
        "close (H)"
        "close FIGNAME"
        "close all"
        "close all hidden"
        "close all force"
        "STATUS = close (...)"
      ]}
    {:name "closereq"
      :signatures [
        "closereq ()"
      ]}
    {:name "colorbar"
      :signatures [
        "colorbar"
        "colorbar (..., LOC)"
        "colorbar (DELETE_OPTION)"
        "colorbar (HCB, ...)"
        "colorbar (HAX, ...)"
        "colorbar (..., \"peer\", HAX, ...)"
        "colorbar (..., \"location\", LOC, ...)"
        "colorbar (..., PROP, VAL, ...)"
        "H = colorbar (...)"
      ]}
    {:name "colstyle"
      :signatures [
        "[STYLE, COLOR, MARKER, MSG] = colstyle (STYLE)"
      ]}
    {:name "comet"
      :signatures [
        "comet (Y)"
        "comet (X, Y)"
        "comet (X, Y, P)"
        "comet (HAX, ...)"
      ]}
    {:name "comet3"
      :signatures [
        "comet3 (Z)"
        "comet3 (X, Y, Z)"
        "comet3 (X, Y, Z, P)"
        "comet3 (HAX, ...)"
      ]}
    {:name "compass"
      :signatures [
        "compass (U, V)"
        "compass (Z)"
        "compass (..., STYLE)"
        "compass (HAX, ...)"
        "H = compass (...)"
      ]}
    {:name "contour"
      :signatures [
        "contour (Z)"
        "contour (Z, VN)"
        "contour (X, Y, Z)"
        "contour (X, Y, Z, VN)"
        "contour (..., STYLE)"
        "contour (HAX, ...)"
        "[C, H] = contour (...)"
      ]}
    {:name "contour3"
      :signatures [
        "contour3 (Z)"
        "contour3 (Z, VN)"
        "contour3 (X, Y, Z)"
        "contour3 (X, Y, Z, VN)"
        "contour3 (..., STYLE)"
        "contour3 (HAX, ...)"
        "[C, H] = contour3 (...)"
      ]}
    {:name "contourc"
      :signatures [
        "C = contourc (Z)"
        "C = contourc (Z, VN)"
        "C = contourc (X, Y, Z)"
        "C = contourc (X, Y, Z, VN)"
        "[C, LEV] = contourc (...)"
      ]}
    {:name "contourf"
      :signatures [
        "contourf (Z)"
        "contourf (Z, VN)"
        "contourf (X, Y, Z)"
        "contourf (X, Y, Z, VN)"
        "contourf (..., STYLE)"
        "contourf (HAX, ...)"
        "[C, H] = contourf (...)"
      ]}
    {:name "copyobj"
      :signatures [
        "HNEW = copyobj (HORIG)"
        "HNEW = copyobj (HORIG, HPARENT)"
      ]}
    {:name "cylinder"
      :signatures [
        "cylinder"
        "cylinder (R)"
        "cylinder (R, N)"
        "cylinder (HAX, ...)"
        "[X, Y, Z] = cylinder (...)"
      ]}
    {:name "daspect"
      :signatures [
        "DATA_ASPECT_RATIO = daspect ()"
        "daspect (DATA_ASPECT_RATIO)"
        "daspect (MODE)"
        "DATA_ASPECT_RATIO_MODE = daspect (\"mode\")"
        "daspect (HAX, ...)"
      ]}
    {:name "datetick"
      :signatures [
        "datetick ()"
        "datetick (AXIS_STR)"
        "datetick (DATE_FORMAT)"
        "datetick (AXIS_STR, DATE_FORMAT)"
        "datetick (..., \"keeplimits\")"
        "datetick (..., \"keepticks\")"
        "datetick (HAX, ...)"
      ]}
    {:name "diffuse"
      :signatures [
        "D = diffuse (SX, SY, SZ, LV)"
      ]}
    {:name "ellipsoid"
      :signatures [
        "ellipsoid (XC, YC, ZC, XR, YR, ZR)"
        "ellipsoid (..., N)"
        "ellipsoid (HAX, ...)"
        "[X, Y, Z] = ellipsoid (...)"
      ]}
    {:name "errorbar"
      :signatures [
        "errorbar (Y, EY)"
        "errorbar (Y, ..., FMT)"
        "errorbar (X, Y, EY)"
        "errorbar (X, Y, ERR, FMT)"
        "errorbar (X, Y, LERR, UERR, FMT)"
        "errorbar (X, Y, EX, EY, FMT)"
        "errorbar (X, Y, LX, UX, LY, UY, FMT)"
        "errorbar (X1, Y1, ..., FMT, XN, YN, ...)"
        "errorbar (HAX, ...)"
        "H = errorbar (...)"
      ]}
    {:name "ezcontour"
      :signatures [
        "ezcontour (F)"
        "ezcontour (..., DOM)"
        "ezcontour (..., N)"
        "ezcontour (HAX, ...)"
        "H = ezcontour (...)"
      ]}
    {:name "ezcontourf"
      :signatures [
        "ezcontourf (F)"
        "ezcontourf (..., DOM)"
        "ezcontourf (..., N)"
        "ezcontourf (HAX, ...)"
        "H = ezcontourf (...)"
      ]}
    {:name "ezmesh"
      :signatures [
        "ezmesh (F)"
        "ezmesh (FX, FY, FZ)"
        "ezmesh (..., DOM)"
        "ezmesh (..., N)"
        "ezmesh (..., \"circ\")"
        "ezmesh (HAX, ...)"
        "H = ezmesh (...)"
      ]}
    {:name "ezmeshc"
      :signatures [
        "ezmeshc (F)"
        "ezmeshc (FX, FY, FZ)"
        "ezmeshc (..., DOM)"
        "ezmeshc (..., N)"
        "ezmeshc (..., \"circ\")"
        "ezmeshc (HAX, ...)"
        "H = ezmeshc (...)"
      ]}
    {:name "ezplot"
      :signatures [
        "ezplot (F)"
        "ezplot (F2V)"
        "ezplot (FX, FY)"
        "ezplot (..., DOM)"
        "ezplot (..., N)"
        "ezplot (HAX, ...)"
        "H = ezplot (...)"
      ]}
    {:name "ezplot3"
      :signatures [
        "ezplot3 (FX, FY, FZ)"
        "ezplot3 (..., DOM)"
        "ezplot3 (..., N)"
        "ezplot3 (..., \"animate\")"
        "ezplot3 (HAX, ...)"
        "H = ezplot3 (...)"
      ]}
    {:name "ezpolar"
      :signatures [
        "ezpolar (F)"
        "ezpolar (..., DOM)"
        "ezpolar (..., N)"
        "ezpolar (HAX, ...)"
        "H = ezpolar (...)"
      ]}
    {:name "ezsurf"
      :signatures [
        "ezsurf (F)"
        "ezsurf (FX, FY, FZ)"
        "ezsurf (..., DOM)"
        "ezsurf (..., N)"
        "ezsurf (..., \"circ\")"
        "ezsurf (HAX, ...)"
        "H = ezsurf (...)"
      ]}
    {:name "ezsurfc"
      :signatures [
        "ezsurfc (F)"
        "ezsurfc (FX, FY, FZ)"
        "ezsurfc (..., DOM)"
        "ezsurfc (..., N)"
        "ezsurfc (..., \"circ\")"
        "ezsurfc (HAX, ...)"
        "H = ezsurfc (...)"
      ]}
    {:name "feather"
      :signatures [
        "feather (U, V)"
        "feather (Z)"
        "feather (..., STYLE)"
        "feather (HAX, ...)"
        "H = feather (...)"
      ]}
    {:name "figure"
      :signatures [
        "figure"
        "figure N"
        "figure (N)"
        "figure (..., \"PROPERTY\", VALUE, ...)"
        "H = figure (...)"
      ]}
    {:name "fill"
      :signatures [
        "fill (X, Y, C)"
        "fill (X1, Y1, C1, X2, Y2, C2)"
        "fill (..., PROP, VAL)"
        "fill (HAX, ...)"
        "H = fill (...)"
      ]}
    {:name "fill3"
      :signatures [
        "fill3 (X, Y, Z, C)"
        "fill3 (X1, Y1, Z1, C1, X2, Y2, Z2, C2)"
        "fill3 (..., PROP, VAL)"
        "fill3 (HAX, ...)"
        "H = fill3 (...)"
      ]}
    {:name "findall"
      :signatures [
        "H = findall ()"
        "H = findall (PROP_NAME, PROP_VALUE, ...)"
        "H = findall (PROP_NAME, PROP_VALUE, \"-LOGICAL_OP\", PROP_NAME, PROP_VALUE)"
        "H = findall (\"-property\", PROP_NAME)"
        "H = findall (\"-regexp\", PROP_NAME, PATTERN)"
        "H = findall (HLIST, ...)"
        "H = findall (HLIST, \"flat\", ...)"
        "H = findall (HLIST, \"-depth\", D, ...)"
      ]}
    {:name "findfigs"
      :signatures [
        "findfigs ()"
      ]}
    {:name "findobj"
      :signatures [
        "H = findobj ()"
        "H = findobj (PROP_NAME, PROP_VALUE, ...)"
        "H = findobj (PROP_NAME, PROP_VALUE, \"-LOGICAL_OP\", PROP_NAME, PROP_VALUE)"
        "H = findobj (\"-property\", PROP_NAME)"
        "H = findobj (\"-regexp\", PROP_NAME, PATTERN)"
        "H = findobj (HLIST, ...)"
        "H = findobj (HLIST, \"flat\", ...)"
        "H = findobj (HLIST, \"-depth\", D, ...)"
      ]}
    {:name "fplot"
      :signatures [
        "fplot (FCN)"
        "fplot (FCN, LIMITS)"
        "fplot (..., TOL)"
        "fplot (..., N)"
        "fplot (..., FMT)"
        "fplot (..., PROPERTY, VALUE, ...)"
        "fplot (HAX, ...)"
        "[X, Y] = fplot (...)"
      ]}
    {:name "gca"
      :signatures [
        "H = gca ()"
      ]}
    {:name "gcbf"
      :signatures [
        "FIG = gcbf ()"
      ]}
    {:name "gcbo"
      :signatures [
        "H = gcbo ()"
        "[H, FIG] = gcbo ()"
      ]}
    {:name "gcf"
      :signatures [
        "H = gcf ()"
      ]}
    {:name "gco"
      :signatures [
        "H = gco ()"
        "H = gco (HFIG)"
      ]}
    {:name "ginput"
      :signatures [
        "[X, Y, BUTTONS] = ginput (N)"
        "[X, Y, BUTTONS] = ginput ()"
      ]}
    {:name "gnuplot_binary"
      :signatures [
        "[PROG, ARGS] = gnuplot_binary ()"
        "[OLD_PROG, OLD_ARGS] = gnuplot_binary (NEW_PROG)"
        "[OLD_PROG, OLD_ARGS] = gnuplot_binary (NEW_PROG, ARG1, ...)"
      ]}
    {:name "graphics_toolkit"
      :signatures [
        "TKIT = graphics_toolkit ()"
        "TKIT = graphics_toolkit (HLIST)"
        "graphics_toolkit (NAME)"
        "graphics_toolkit (HLIST, NAME)"
      ]}
    {:name "grid"
      :signatures [
        "grid"
        "grid on"
        "grid off"
        "grid minor"
        "grid minor on"
        "grid minor off"
        "grid (HAX, ...)"
      ]}
    {:name "groot"
      :signatures [
        "H = groot ()"
      ]}
    {:name "gtext"
      :signatures [
        "gtext (S)"
        "gtext ({S1, S2, ...})"
        "gtext ({S1; S2; ...})"
        "gtext (..., PROP, VAL, ...)"
        "H = gtext (...)"
      ]}
    {:name "gui_mainfcn"
      :signatures [
        "[...] = gui_mainfcn (GUI_STATE, ...)"
      ]}
    {:name "hdl2struct"
      :signatures [
        "S = hdl2struct (H)"
      ]}
    {:name "hggroup"
      :signatures [
        "hggroup ()"
        "hggroup (HAX)"
        "hggroup (..., PROPERTY, VALUE, ...)"
        "H = hggroup (...)"
      ]}
    {:name "hgload"
      :signatures [
        "H = hgload (FILENAME)"
        "[H, OLD_PROP] = hgload (FILENAME, PROP_STRUCT)"
      ]}
    {:name "hgsave"
      :signatures [
        "hgsave (FILENAME)"
        "hgsave (H, FILENAME)"
        "hgsave (H, FILENAME, FMT)"
      ]}
    {:name "hgtransform"
      :signatures [
        "H = hgtransform ()"
        "H = hgtransform (PROPERTY, VALUE, ...)"
        "H = hgtransform (HAX, ...)"
      ]}
    {:name "hidden"
      :signatures [
        "hidden"
        "hidden on"
        "hidden off"
        "MODE = hidden (...)"
      ]}
    {:name "hist"
      :signatures [
        "hist (Y)"
        "hist (Y, NBINS)"
        "hist (Y, X)"
        "hist (Y, X, NORM)"
        "hist (..., PROP, VAL, ...)"
        "hist (HAX, ...)"
        "[NN, XX] = hist (...)"
      ]}
    {:name "hold"
      :signatures [
        "hold"
        "hold on"
        "hold off"
        "hold (HAX, ...)"
      ]}
    {:name "isaxes"
      :signatures [
        "TF = isaxes (H)"
      ]}
    {:name "isfigure"
      :signatures [
        "TF = isfigure (H)"
      ]}
    {:name "isgraphics"
      :signatures [
        "TF = isgraphics (H)"
        "TF = isgraphics (H, TYPE)"
      ]}
    {:name "ishandle"
      :signatures [
        "TF = ishandle (H)"
      ]}
    {:name "ishold"
      :signatures [
        "TF = ishold"
        "TF = ishold (HAX)"
        "TF = ishold (HFIG)"
      ]}
    {:name "isocaps"
      :signatures [
        "FVC = isocaps (V, ISOVAL)"
        "FVC = isocaps (V)"
        "FVC = isocaps (X, Y, Z, V, ISOVAL)"
        "FVC = isocaps (X, Y, Z, V)"
        "FVC = isocaps (..., WHICH_CAPS)"
        "FVC = isocaps (..., WHICH_PLANE)"
        "FVC = isocaps (..., \"verbose\")"
        "[FACES, VERTICES, FVCDATA] = isocaps (...)"
        "isocaps (...)"
      ]}
    {:name "isocolors"
      :signatures [
        "CDAT = isocolors (C, V)"
        "CDAT = isocolors (X, Y, Z, C, V)"
        "CDAT = isocolors (X, Y, Z, R, G, B, V)"
        "CDAT = isocolors (R, G, B, V)"
        "CDAT = isocolors (..., HP)"
        "isocolors (..., HP)"
      ]}
    {:name "isonormals"
      :signatures [
        "VN = isonormals (VAL, VERT)"
        "VN = isonormals (VAL, HP)"
        "VN = isonormals (X, Y, Z, VAL, VERT)"
        "VN = isonormals (X, Y, Z, VAL, HP)"
        "VN = isonormals (..., \"negate\")"
        "isonormals (VAL, HP)"
        "isonormals (X, Y, Z, VAL, HP)"
        "isonormals (..., \"negate\")"
      ]}
    {:name "isosurface"
      :signatures [
        "FV = isosurface (V, ISOVAL)"
        "FV = isosurface (V)"
        "FV = isosurface (X, Y, Z, V, ISOVAL)"
        "FV = isosurface (X, Y, Z, V)"
        "FVC = isosurface (..., COL)"
        "FV = isosurface (..., \"noshare\")"
        "FV = isosurface (..., \"verbose\")"
        "[F, V] = isosurface (...)"
        "[F, V, C] = isosurface (...)"
        "isosurface (...)"
      ]}
    {:name "isprop"
      :signatures [
        "RES = isprop (OBJ, \"PROP\")"
      ]}
    {:name "legend"
      :signatures [
        "legend ()"
        "legend COMMAND"
        "legend (STR1, STR2, ...)"
        "legend (CHARMAT)"
        "legend ({CELLSTR})"
        "legend (..., PROPERTY, VALUE, ...)"
        "legend (HOBJS, ...)"
        "legend (\"COMMAND\")"
        "legend (HAX, ...)"
        "legend (HLEG, ...)"
        "HLEG = legend (...)"
      ]}
    {:name "light"
      :signatures [
        "light ()"
        "light (..., \"PROP\", VAL, ...)"
        "light (HAX, ...)"
        "H = light (...)"
      ]}
    {:name "lightangle"
      :signatures [
        "lightangle (AZ, EL)"
        "lightangle (HAX, AZ, EL)"
        "lightangle (HL, AZ, EL)"
        "HL = lightangle (...)"
        "[AZ, EL] = lightangle (HL)"
      ]}
    {:name "lighting"
      :signatures [
        "lighting (TYPE)"
        "lighting (HAX, TYPE)"
      ]}
    {:name "linkaxes"
      :signatures [
        "linkaxes (HAX)"
        "linkaxes (HAX, OPTSTR)"
      ]}
    {:name "linkprop"
      :signatures [
        "HLINK = linkprop (H, \"PROP\")"
        "HLINK = linkprop (H, {\"PROP1\", \"PROP2\", ...})"
      ]}
    {:name "loglog"
      :signatures [
        "loglog (Y)"
        "loglog (X, Y)"
        "loglog (X, Y, PROP, VALUE, ...)"
        "loglog (X, Y, FMT)"
        "loglog (HAX, ...)"
        "H = loglog (...)"
      ]}
    {:name "loglogerr"
      :signatures [
        "loglogerr (Y, EY)"
        "loglogerr (Y, ..., FMT)"
        "loglogerr (X, Y, EY)"
        "loglogerr (X, Y, ERR, FMT)"
        "loglogerr (X, Y, LERR, UERR, FMT)"
        "loglogerr (X, Y, EX, EY, FMT)"
        "loglogerr (X, Y, LX, UX, LY, UY, FMT)"
        "loglogerr (X1, Y1, ..., FMT, XN, YN, ...)"
        "loglogerr (HAX, ...)"
        "H = loglogerr (...)"
      ]}
    {:name "material"
      :signatures [
        "material shiny"
        "material dull"
        "material metal"
        "material default"
        "material ([AS, DS, SS])"
        "material ([AS, DS, SS, SE])"
        "material ([AS, DS, SS, SE, SCR])"
        "material (HLIST, ...)"
        "MTYPES = material ()"
        "REFL_PROPS = material (MTYPE_STRING)"
      ]}
    {:name "mesh"
      :signatures [
        "mesh (X, Y, Z)"
        "mesh (Z)"
        "mesh (..., C)"
        "mesh (..., PROP, VAL, ...)"
        "mesh (HAX, ...)"
        "H = mesh (...)"
      ]}
    {:name "meshc"
      :signatures [
        "meshc (X, Y, Z)"
        "meshc (Z)"
        "meshc (..., C)"
        "meshc (..., PROP, VAL, ...)"
        "meshc (HAX, ...)"
        "H = meshc (...)"
      ]}
    {:name "meshgrid"
      :signatures [
        "[XX, YY] = meshgrid (X, Y)"
        "[XX, YY, ZZ] = meshgrid (X, Y, Z)"
        "[XX, YY] = meshgrid (X)"
        "[XX, YY, ZZ] = meshgrid (X)"
      ]}
    {:name "meshz"
      :signatures [
        "meshz (X, Y, Z)"
        "meshz (Z)"
        "meshz (..., C)"
        "meshz (..., PROP, VAL, ...)"
        "meshz (HAX, ...)"
        "H = meshz (...)"
      ]}
    {:name "ndgrid"
      :signatures [
        "[Y1, Y2, ..., Yn] = ndgrid (X1, X2, ..., Xn)"
        "[Y1, Y2, ..., Yn] = ndgrid (X)"
      ]}
    {:name "newplot"
      :signatures [
        "newplot ()"
        "newplot (HFIG)"
        "newplot (HAX)"
        "HAX = newplot (...)"
      ]}
    {:name "openfig"
      :signatures [
        "openfig"
        "openfig (FILENAME)"
        "openfig (..., COPIES)"
        "openfig (..., VISIBILITY)"
        "H = openfig (...)"
      ]}
    {:name "orient"
      :signatures [
        "orient (ORIENTATION)"
        "orient (HFIG, ORIENTATION)"
        "ORIENTATION = orient ()"
        "ORIENTATION = orient (HFIG)"
      ]}
    {:name "ostreamtube"
      :signatures [
        "ostreamtube (X, Y, Z, U, V, W, SX, SY, SZ)"
        "ostreamtube (U, V, W, SX, SY, SZ)"
        "ostreamtube (XYZ, X, Y, Z, U, V, W)"
        "ostreamtube (..., OPTIONS)"
        "ostreamtube (HAX, ...)"
        "H = ostreamtube (...)"
      ]}
    {:name "pan"
      :signatures [
        "pan"
        "pan on"
        "pan off"
        "pan xon"
        "pan yon"
        "pan (HFIG, OPTION)"
      ]}
    {:name "pareto"
      :signatures [
        "pareto (Y)"
        "pareto (Y, X)"
        "pareto (HAX, ...)"
        "H = pareto (...)"
      ]}
    {:name "patch"
      :signatures [
        "patch ()"
        "patch (X, Y, C)"
        "patch (X, Y, Z, C)"
        "patch (\"Faces\", FACES, \"Vertices\", VERTS, ...)"
        "patch (..., \"PROP\", VAL, ...)"
        "patch (..., PROPSTRUCT, ...)"
        "patch (HAX, ...)"
        "H = patch (...)"
      ]}
    {:name "pbaspect"
      :signatures [
        "PLOT_BOX_ASPECT_RATIO = pbaspect ( )"
        "pbaspect (PLOT_BOX_ASPECT_RATIO)"
        "pbaspect (MODE)"
        "PLOT_BOX_ASPECT_RATIO_MODE = pbaspect (\"mode\")"
        "pbaspect (HAX, ...)"
      ]}
    {:name "pcolor"
      :signatures [
        "pcolor (X, Y, C)"
        "pcolor (C)"
        "pcolor (HAX, ...)"
        "H = pcolor (...)"
      ]}
    {:name "peaks"
      :signatures [
        "peaks ()"
        "peaks (N)"
        "peaks (X, Y)"
        "Z = peaks (...)"
        "[X, Y, Z] = peaks (...)"
      ]}
    {:name "pie"
      :signatures [
        "pie (X)"
        "pie (..., EXPLODE)"
        "pie (..., LABELS)"
        "pie (HAX, ...)"
        "H = pie (...)"
      ]}
    {:name "pie3"
      :signatures [
        "pie3 (X)"
        "pie3 (..., EXPLODE)"
        "pie3 (..., LABELS)"
        "pie3 (HAX, ...)"
        "H = pie3 (...)"
      ]}
    {:name "plot"
      :signatures [
        "plot (Y)"
        "plot (X, Y)"
        "plot (X, Y, FMT)"
        "plot (..., PROPERTY, VALUE, ...)"
        "plot (X1, Y1, ..., XN, YN)"
        "plot (HAX, ...)"
        "H = plot (...)"
      ]}
    {:name "plot3"
      :signatures [
        "plot3 (X, Y, Z)"
        "plot3 (X, Y, Z, PROP, VALUE, ...)"
        "plot3 (X, Y, Z, FMT)"
        "plot3 (X, CPLX)"
        "plot3 (CPLX)"
        "plot3 (HAX, ...)"
        "H = plot3 (...)"
      ]}
    {:name "plotmatrix"
      :signatures [
        "plotmatrix (X, Y)"
        "plotmatrix (X)"
        "plotmatrix (..., STYLE)"
        "plotmatrix (HAX, ...)"
        "[H, AX, BIGAX, P, PAX] = plotmatrix (...)"
      ]}
    {:name "plotyy"
      :signatures [
        "plotyy (X1, Y1, X2, Y2)"
        "plotyy (..., FCN)"
        "plotyy (..., FUN1, FUN2)"
        "plotyy (HAX, ...)"
        "[AX, H1, H2] = plotyy (...)"
      ]}
    {:name "polar"
      :signatures [
        "polar (THETA, RHO)"
        "polar (THETA, RHO, FMT)"
        "polar (CPLX)"
        "polar (CPLX, FMT)"
        "polar (HAX, ...)"
        "H = polar (...)"
      ]}
    {:name "print"
      :signatures [
        "print ()"
        "print (OPTIONS)"
        "print (FILENAME, OPTIONS)"
        "print (HFIG, ...)"
        "RGB = print (\"-RGBImage\", ...)"
      ]}
    {:name "printd"
      :signatures [
        "printd (OBJ, FILENAME)"
        "OUT_FILE = printd (...)"
      ]}
    {:name "quiver"
      :signatures [
        "quiver (U, V)"
        "quiver (X, Y, U, V)"
        "quiver (..., S)"
        "quiver (..., STYLE)"
        "quiver (..., \"filled\")"
        "quiver (HAX, ...)"
        "H = quiver (...)"
      ]}
    {:name "quiver3"
      :signatures [
        "quiver3 (X, Y, Z, U, V, W)"
        "quiver3 (Z, U, V, W)"
        "quiver3 (..., S)"
        "quiver3 (..., STYLE)"
        "quiver3 (..., \"filled\")"
        "quiver3 (HAX, ...)"
        "H = quiver3 (...)"
      ]}
    {:name "rectangle"
      :signatures [
        "rectangle ()"
        "rectangle (..., \"Position\", POS)"
        "rectangle (..., \"Curvature\", CURV)"
        "rectangle (..., \"EdgeColor\", EC)"
        "rectangle (..., \"FaceColor\", FC)"
        "rectangle (HAX, ...)"
        "H = rectangle (...)"
      ]}
    {:name "reducepatch"
      :signatures [
        "REDUCED_FV = reducepatch (FV)"
        "REDUCED_FV = reducepatch (FACES, VERTICES)"
        "REDUCED_FV = reducepatch (PATCH_HANDLE)"
        "reducepatch (PATCH_HANDLE)"
        "REDUCED_FV = reducepatch (..., REDUCTION_FACTOR)"
        "REDUCED_FV = reducepatch (..., \"fast\")"
        "REDUCED_FV = reducepatch (..., \"verbose\")"
        "[REDUCED_FACES, REDUCES_VERTICES] = reducepatch (...)"
      ]}
    {:name "reducevolume"
      :signatures [
        "[NX, NY, NZ, NV] = reducevolume (V, R)"
        "[NX, NY, NZ, NV] = reducevolume (X, Y, Z, V, R)"
        "NV = reducevolume (...)"
      ]}
    {:name "refresh"
      :signatures [
        "refresh ()"
        "refresh (H)"
      ]}
    {:name "refreshdata"
      :signatures [
        "refreshdata ()"
        "refreshdata (H)"
        "refreshdata (H, WORKSPACE)"
      ]}
    {:name "ribbon"
      :signatures [
        "ribbon (Y)"
        "ribbon (X, Y)"
        "ribbon (X, Y, WIDTH)"
        "ribbon (HAX, ...)"
        "H = ribbon (...)"
      ]}
    {:name "rose"
      :signatures [
        "rose (TH)"
        "rose (TH, NBINS)"
        "rose (TH, BINS)"
        "rose (HAX, ...)"
        "H = rose (...)"
        "[THOUT ROUT] = rose (...)"
      ]}
    {:name "rotate"
      :signatures [
        "rotate (H, DIRECTION, ALPHA)"
        "rotate (..., ORIGIN)"
      ]}
    {:name "rotate3d"
      :signatures [
        "rotate3d"
        "rotate3d on"
        "rotate3d off"
        "rotate3d (HFIG, OPTION)"
      ]}
    {:name "rticks"
      :signatures [
        "TICKVAL = rticks"
        "rticks (TICKVAL)"
        "... = rticks (HAX, ...)"
      ]}
    {:name "saveas"
      :signatures [
        "saveas (H, FILENAME)"
        "saveas (H, FILENAME, FMT)"
      ]}
    {:name "savefig"
      :signatures [
        "savefig ()"
        "savefig (H)"
        "savefig (FILENAME)"
        "savefig (H, FILENAME)"
        "savefig (H, FILENAME, \"compact\")"
      ]}
    {:name "scatter"
      :signatures [
        "scatter (X, Y)"
        "scatter (X, Y, S)"
        "scatter (X, Y, S, C)"
        "scatter (..., STYLE)"
        "scatter (..., \"filled\")"
        "scatter (..., PROP, VAL, ...)"
        "scatter (HAX, ...)"
        "H = scatter (...)"
      ]}
    {:name "scatter3"
      :signatures [
        "scatter3 (X, Y, Z)"
        "scatter3 (X, Y, Z, S)"
        "scatter3 (X, Y, Z, S, C)"
        "scatter3 (..., STYLE)"
        "scatter3 (..., \"filled\")"
        "scatter3 (..., PROP, VAL)"
        "scatter3 (HAX, ...)"
        "H = scatter3 (...)"
      ]}
    {:name "semilogx"
      :signatures [
        "semilogx (Y)"
        "semilogx (X, Y)"
        "semilogx (X, Y, PROPERTY, VALUE, ...)"
        "semilogx (X, Y, FMT)"
        "semilogx (HAX, ...)"
        "H = semilogx (...)"
      ]}
    {:name "semilogxerr"
      :signatures [
        "semilogxerr (Y, EY)"
        "semilogxerr (Y, ..., FMT)"
        "semilogxerr (X, Y, EY)"
        "semilogxerr (X, Y, ERR, FMT)"
        "semilogxerr (X, Y, LERR, UERR, FMT)"
        "semilogxerr (X, Y, EX, EY, FMT)"
        "semilogxerr (X, Y, LX, UX, LY, UY, FMT)"
        "semilogxerr (X1, Y1, ..., FMT, XN, YN, ...)"
        "semilogxerr (HAX, ...)"
        "H = semilogxerr (...)"
      ]}
    {:name "semilogy"
      :signatures [
        "semilogy (Y)"
        "semilogy (X, Y)"
        "semilogy (X, Y, PROPERTY, VALUE, ...)"
        "semilogy (X, Y, FMT)"
        "semilogy (H, ...)"
        "H = semilogy (...)"
      ]}
    {:name "semilogyerr"
      :signatures [
        "semilogyerr (Y, EY)"
        "semilogyerr (Y, ..., FMT)"
        "semilogyerr (X, Y, EY)"
        "semilogyerr (X, Y, ERR, FMT)"
        "semilogyerr (X, Y, LERR, UERR, FMT)"
        "semilogyerr (X, Y, EX, EY, FMT)"
        "semilogyerr (X, Y, LX, UX, LY, UY, FMT)"
        "semilogyerr (X1, Y1, ..., FMT, XN, YN, ...)"
        "semilogyerr (HAX, ...)"
        "H = semilogyerr (...)"
      ]}
    {:name "shading"
      :signatures [
        "shading (TYPE)"
        "shading (HAX, TYPE)"
      ]}
    {:name "shg"
      :signatures [
        "shg"
      ]}
    {:name "shrinkfaces"
      :signatures [
        "shrinkfaces (P, SF)"
        "NFV = shrinkfaces (P, SF)"
        "NFV = shrinkfaces (FV, SF)"
        "NFV = shrinkfaces (F, V, SF)"
        "[NF, NV] = shrinkfaces (...)"
      ]}
    {:name "slice"
      :signatures [
        "slice (X, Y, Z, V, SX, SY, SZ)"
        "slice (X, Y, Z, V, XI, YI, ZI)"
        "slice (V, SX, SY, SZ)"
        "slice (V, XI, YI, ZI)"
        "slice (..., METHOD)"
        "slice (HAX, ...)"
        "H = slice (...)"
      ]}
    {:name "smooth3"
      :signatures [
        "SMOOTHED_DATA = smooth3 (DATA)"
        "SMOOTHED_DATA = smooth3 (DATA, METHOD)"
        "SMOOTHED_DATA = smooth3 (DATA, METHOD, SZ)"
        "SMOOTHED_DATA = smooth3 (DATA, METHOD, SZ, STD_DEV)"
      ]}
    {:name "sombrero"
      :signatures [
        "sombrero ()"
        "sombrero (N)"
        "Z = sombrero (...)"
        "[X, Y, Z] = sombrero (...)"
      ]}
    {:name "specular"
      :signatures [
        "REFL = specular (SX, SY, SZ, LV, VV)"
        "REFL = specular (SX, SY, SZ, LV, VV, SE)"
      ]}
    {:name "sphere"
      :signatures [
        "sphere ()"
        "sphere (N)"
        "sphere (HAX, ...)"
        "[X, Y, Z] = sphere (...)"
      ]}
    {:name "stairs"
      :signatures [
        "stairs (Y)"
        "stairs (X, Y)"
        "stairs (..., STYLE)"
        "stairs (..., PROP, VAL, ...)"
        "stairs (HAX, ...)"
        "H = stairs (...)"
        "[XSTEP, YSTEP] = stairs (...)"
      ]}
    {:name "stem"
      :signatures [
        "stem (Y)"
        "stem (X, Y)"
        "stem (..., LINESPEC)"
        "stem (..., \"filled\")"
        "stem (..., PROP, VAL, ...)"
        "stem (HAX, ...)"
        "H = stem (...)"
      ]}
  ])

(def ^:private +plot-2+
  "Chunk 2 of Octave builtins from the `plot` category, with signatures."
  [
    {:name "stem3"
      :signatures [
        "stem3 (X, Y, Z)"
        "stem3 (..., LINESPEC)"
        "stem3 (..., \"filled\")"
        "stem3 (..., PROP, VAL, ...)"
        "stem3 (HAX, ...)"
        "H = stem3 (...)"
      ]}
    {:name "stemleaf"
      :signatures [
        "stemleaf (X, CAPTION)"
        "stemleaf (X, CAPTION, STEM_SZ)"
        "PLOTSTR = stemleaf (...)"
      ]}
    {:name "stream2"
      :signatures [
        "XY = stream2 (X, Y, U, V, SX, SY)"
        "XY = stream2 (U, V, SX, SY)"
        "XY = stream2 (..., OPTIONS)"
      ]}
    {:name "stream3"
      :signatures [
        "XYZ = stream3 (X, Y, Z, U, V, W, SX, SY, SZ)"
        "XYZ = stream3 (U, V, W, SX, SY, SZ)"
        "XYZ = stream3 (..., OPTIONS)"
      ]}
    {:name "streamline"
      :signatures [
        "streamline (X, Y, Z, U, V, W, SX, SY, SZ)"
        "streamline (U, V, W, SX, SY, SZ)"
        "streamline (..., OPTIONS)"
        "streamline (HAX, ...)"
        "H = streamline (...)"
      ]}
    {:name "streamribbon"
      :signatures [
        "streamribbon (X, Y, Z, U, V, W, SX, SY, SZ)"
        "streamribbon (U, V, W, SX, SY, SZ)"
        "streamribbon (XYZ, X, Y, Z, ANLR_SPD, LIN_SPD)"
        "streamribbon (XYZ, ANLR_SPD, LIN_SPD)"
        "streamribbon (XYZ, ANLR_ROT)"
        "streamribbon (..., WIDTH)"
        "streamribbon (HAX, ...)"
        "H = streamribbon (...)"
      ]}
    {:name "streamtube"
      :signatures [
        "streamtube (X, Y, Z, U, V, W, SX, SY, SZ)"
        "streamtube (U, V, W, SX, SY, SZ)"
        "streamtube (XYZ, X, Y, Z, DIV)"
        "streamtube (XYZ, DIV)"
        "streamtube (XYZ, DIA)"
        "streamtube (..., OPTIONS)"
        "streamtube (HAX, ...)"
        "H = streamtube (...)"
      ]}
    {:name "struct2hdl"
      :signatures [
        "H = struct2hdl (S)"
        "H = struct2hdl (S, P)"
        "H = struct2hdl (S, P, HILEV)"
      ]}
    {:name "subplot"
      :signatures [
        "subplot (ROWS, COLS, INDEX)"
        "subplot (ROWS, COLS, INDEX, HAX)"
        "subplot (RCN)"
        "subplot (HAX)"
        "subplot (..., \"align\")"
        "subplot (..., \"replace\")"
        "subplot (\"position\", POS)"
        "subplot (..., PROP, VAL, ...)"
        "HAX = subplot (...)"
      ]}
    {:name "surf"
      :signatures [
        "surf (X, Y, Z)"
        "surf (Z)"
        "surf (..., C)"
        "surf (..., PROP, VAL, ...)"
        "surf (HAX, ...)"
        "H = surf (...)"
      ]}
    {:name "surface"
      :signatures [
        "surface (X, Y, Z, C)"
        "surface (X, Y, Z)"
        "surface (Z, C)"
        "surface (Z)"
        "surface (..., PROP, VAL, ...)"
        "surface (HAX, ...)"
        "H = surface (...)"
      ]}
    {:name "surfc"
      :signatures [
        "surfc (X, Y, Z)"
        "surfc (Z)"
        "surfc (..., C)"
        "surfc (..., PROP, VAL, ...)"
        "surfc (HAX, ...)"
        "H = surfc (...)"
      ]}
    {:name "surfl"
      :signatures [
        "surfl (Z)"
        "surfl (X, Y, Z)"
        "surfl (..., LSRC)"
        "surfl (X, Y, Z, LSRC, P)"
        "surfl (..., \"cdata\")"
        "surfl (..., \"light\")"
        "surfl (HAX, ...)"
        "H = surfl (...)"
      ]}
    {:name "surfnorm"
      :signatures [
        "surfnorm (X, Y, Z)"
        "surfnorm (Z)"
        "surfnorm (..., PROP, VAL, ...)"
        "surfnorm (HAX, ...)"
        "[NX, NY, NZ] = surfnorm (...)"
      ]}
    {:name "tetramesh"
      :signatures [
        "tetramesh (T, X)"
        "tetramesh (T, X, C)"
        "tetramesh (..., PROPERTY, VAL, ...)"
        "H = tetramesh (...)"
      ]}
    {:name "text"
      :signatures [
        "text (X, Y, STRING)"
        "text (X, Y, Z, STRING)"
        "text (..., PROP, VAL, ...)"
        "text (HAX, ...)"
        "H = text (...)"
      ]}
    {:name "thetaticks"
      :signatures [
        "TICKVAL = thetaticks"
        "thetaticks (TICKVAL)"
        "... = thetaticks (HAX, ...)"
      ]}
    {:name "title"
      :signatures [
        "title (STRING)"
        "title (STRING, PROP, VAL, ...)"
        "title (HAX, ...)"
        "H = title (...)"
      ]}
    {:name "trimesh"
      :signatures [
        "trimesh (TRI, X, Y, Z, C)"
        "trimesh (TRI, X, Y, Z)"
        "trimesh (TRI, X, Y)"
        "trimesh (..., PROP, VAL, ...)"
        "H = trimesh (...)"
      ]}
    {:name "triplot"
      :signatures [
        "triplot (TRI, X, Y)"
        "triplot (TRI, X, Y, LINESPEC)"
        "H = triplot (...)"
      ]}
    {:name "trisurf"
      :signatures [
        "trisurf (TRI, X, Y, Z, C)"
        "trisurf (TRI, X, Y, Z)"
        "trisurf (..., PROP, VAL, ...)"
        "H = trisurf (...)"
      ]}
    {:name "view"
      :signatures [
        "view (AZIMUTH, ELEVATION)"
        "view ([AZIMUTH ELEVATION])"
        "view ([X Y Z])"
        "view (2)"
        "view (3)"
        "view (HAX, ...)"
        "[AZIMUTH, ELEVATION] = view ()"
      ]}
    {:name "waterfall"
      :signatures [
        "waterfall (X, Y, Z)"
        "waterfall (Z)"
        "waterfall (..., C)"
        "waterfall (..., PROP, VAL, ...)"
        "waterfall (HAX, ...)"
        "H = waterfall (...)"
      ]}
    {:name "whitebg"
      :signatures [
        "whitebg ()"
        "whitebg (COLOR)"
        "whitebg (\"none\")"
        "whitebg (HFIG)"
        "whitebg (HFIG, COLOR)"
        "whitebg (HFIG, \"none\")"
      ]}
    {:name "xlabel"
      :signatures [
        "xlabel (STRING)"
        "xlabel (STRING, PROPERTY, VAL, ...)"
        "xlabel (HAX, ...)"
        "H = xlabel (...)"
      ]}
    {:name "xlim"
      :signatures [
        "XLIMITS = xlim ()"
        "XMODE = xlim (\"mode\")"
        "xlim ([X_LO X_HI])"
        "xlim (\"auto\")"
        "xlim (\"manual\")"
        "xlim (HAX, ...)"
      ]}
    {:name "xtickangle"
      :signatures [
        "ANGLE = xtickangle ()"
        "ANGLE = xtickangle (HAX)"
        "xtickangle (ANGLE)"
        "xtickangle (HAX, ANGLE)"
      ]}
    {:name "xticklabels"
      :signatures [
        "LABELS = xticklabels"
        "MODE = xticklabels (\"mode\")"
        "xticklabels (TICKVAL)"
        "xticklabels (\"auto\")"
        "xticklabels (\"manual\")"
        "... = xticklabels (HAX, ...)"
      ]}
    {:name "xticks"
      :signatures [
        "TICKVAL = xticks"
        "MODE = xticks (\"mode\")"
        "xticks (TICKVAL)"
        "xticks (\"auto\")"
        "xticks (\"manual\")"
        "... = xticks (HAX, ...)"
      ]}
    {:name "ylabel"
      :signatures [
        "ylabel (STRING)"
        "ylabel (STRING, PROPERTY, VAL, ...)"
        "ylabel (HAX, ...)"
        "H = ylabel (...)"
      ]}
    {:name "ylim"
      :signatures [
        "YLIMITS = ylim ()"
        "XMODE = ylim (\"mode\")"
        "ylim ([Y_LO Y_HI])"
        "ylim (\"auto\")"
        "ylim (\"manual\")"
        "ylim (HAX, ...)"
      ]}
    {:name "ytickangle"
      :signatures [
        "ANGLE = ytickangle ()"
        "ANGLE = ytickangle (HAX)"
        "ytickangle (ANGLE)"
        "ytickangle (HAX, ANGLE)"
      ]}
    {:name "yticklabels"
      :signatures [
        "LABELS = yticklabels"
        "MODE = yticklabels (\"mode\")"
        "yticklabels (TICKVAL)"
        "yticklabels (\"auto\")"
        "yticklabels (\"manual\")"
        "... = yticklabels (HAX, ...)"
      ]}
    {:name "yticks"
      :signatures [
        "TICKVAL = yticks"
        "MODE = yticks (\"mode\")"
        "yticks (TICKVAL)"
        "yticks (\"auto\")"
        "yticks (\"manual\")"
        "... = yticks (HAX, ...)"
      ]}
    {:name "zlabel"
      :signatures [
        "zlabel (STRING)"
        "zlabel (STRING, PROPERTY, VAL, ...)"
        "zlabel (HAX, ...)"
        "H = zlabel (...)"
      ]}
    {:name "zlim"
      :signatures [
        "ZLIMITS = zlim ()"
        "XMODE = zlim (\"mode\")"
        "zlim ([Z_LO Z_HI])"
        "zlim (\"auto\")"
        "zlim (\"manual\")"
        "zlim (HAX, ...)"
      ]}
    {:name "zoom"
      :signatures [
        "zoom"
        "zoom (FACTOR)"
        "zoom on"
        "zoom off"
        "zoom xon"
        "zoom yon"
        "zoom out"
        "zoom reset"
        "zoom (HFIG, OPTION)"
      ]}
    {:name "ztickangle"
      :signatures [
        "ANGLE = ztickangle ()"
        "ANGLE = ztickangle (HAX)"
        "ztickangle (ANGLE)"
        "ztickangle (HAX, ANGLE)"
      ]}
    {:name "zticklabels"
      :signatures [
        "LABELS = zticklabels"
        "MODE = zticklabels (\"mode\")"
        "zticklabels (TICKVAL)"
        "zticklabels (\"auto\")"
        "zticklabels (\"manual\")"
        "... = zticklabels (HAX, ...)"
      ]}
    {:name "zticks"
      :signatures [
        "TICKVAL = zticks"
        "MODE = zticks (\"mode\")"
        "zticks (TICKVAL)"
        "zticks (\"auto\")"
        "zticks (\"manual\")"
        "... = zticks (HAX, ...)"
      ]}
  ])

(def +plot+
  "Octave builtins from the `plot` category, with signatures."
  (vec (concat +plot-1+ +plot-2+)))

(def +polynomial+
  "Octave builtins from the `polynomial` category, with signatures."
  [
    {:name "compan"
      :signatures [
        "A = compan (C)"
      ]}
    {:name "conv"
      :signatures [
        "Y = conv (A, B)"
        "Y = conv (A, B, SHAPE)"
      ]}
    {:name "deconv"
      :signatures [
        "B = deconv (Y, A)"
        "[B, R] = deconv (Y, A)"
      ]}
    {:name "mkpp"
      :signatures [
        "PP = mkpp (BREAKS, COEFS)"
        "PP = mkpp (BREAKS, COEFS, D)"
      ]}
    {:name "mpoles"
      :signatures [
        "[MULTP, IDXP] = mpoles (P)"
        "[MULTP, IDXP] = mpoles (P, TOL)"
        "[MULTP, IDXP] = mpoles (P, TOL, REORDER)"
      ]}
    {:name "padecoef"
      :signatures [
        "[NUM, DEN] = padecoef (T)"
        "[NUM, DEN] = padecoef (T, N)"
      ]}
    {:name "pchip"
      :signatures [
        "PP = pchip (X, Y)"
        "YI = pchip (X, Y, XI)"
      ]}
    {:name "poly"
      :signatures [
        "Y = poly (A)"
        "Y = poly (X)"
      ]}
    {:name "polyaffine"
      :signatures [
        "G = polyaffine (F, MU)"
      ]}
    {:name "polyder"
      :signatures [
        "K = polyder (P)"
        "K = polyder (A, B)"
        "[Q, D] = polyder (B, A)"
      ]}
    {:name "polyeig"
      :signatures [
        "Z = polyeig (C0, C1, ..., CL)"
        "[V, Z] = polyeig (C0, C1, ..., CL)"
      ]}
    {:name "polyfit"
      :signatures [
        "P = polyfit (X, Y, N)"
        "[P, S] = polyfit (X, Y, N)"
        "[P, S, MU] = polyfit (X, Y, N)"
      ]}
    {:name "polygcd"
      :signatures [
        "Q = polygcd (B, A)"
        "Q = polygcd (B, A, TOL)"
      ]}
    {:name "polyint"
      :signatures [
        "Q = polyint (P)"
        "Q = polyint (P, K)"
      ]}
    {:name "polyout"
      :signatures [
        "polyout (C)"
        "polyout (C, X)"
        "STR = polyout (...)"
      ]}
    {:name "polyreduce"
      :signatures [
        "P = polyreduce (C)"
      ]}
    {:name "polyval"
      :signatures [
        "Y = polyval (P, X)"
        "Y = polyval (P, X, [], MU)"
        "[Y, DY] = polyval (P, X, S)"
        "[Y, DY] = polyval (P, X, S, MU)"
      ]}
    {:name "polyvalm"
      :signatures [
        "Y = polyvalm (C, X)"
      ]}
    {:name "ppder"
      :signatures [
        "ppd = ppder (pp)"
        "ppd = ppder (pp, m)"
      ]}
    {:name "ppint"
      :signatures [
        "PPI = ppint (PP)"
        "PPI = ppint (PP, C)"
      ]}
    {:name "ppjumps"
      :signatures [
        "JUMPS = ppjumps (PP)"
      ]}
    {:name "ppval"
      :signatures [
        "YI = ppval (PP, XI)"
      ]}
    {:name "residue"
      :signatures [
        "[R, P, K, E] = residue (B, A)"
        "[B, A] = residue (R, P, K)"
        "[B, A] = residue (R, P, K, E)"
      ]}
    {:name "roots"
      :signatures [
        "R = roots (C)"
      ]}
    {:name "spline"
      :signatures [
        "PP = spline (X, Y)"
        "YI = spline (X, Y, XI)"
      ]}
    {:name "splinefit"
      :signatures [
        "PP = splinefit (X, Y, BREAKS)"
        "PP = splinefit (X, Y, P)"
        "PP = splinefit (..., \"periodic\", PERIODIC)"
        "PP = splinefit (..., \"robust\", ROBUST)"
        "PP = splinefit (..., \"beta\", BETA)"
        "PP = splinefit (..., \"order\", ORDER)"
        "PP = splinefit (..., \"constraints\", CONSTRAINTS)"
      ]}
    {:name "unmkpp"
      :signatures [
        "[X, P, N, K, D] = unmkpp (PP)"
      ]}
  ])

(def +prefs+
  "Octave builtins from the `prefs` category, with signatures."
  [
    {:name "addpref"
      :signatures [
        "addpref (\"GROUP\", \"PREF\", VAL)"
        "addpref (\"GROUP\", {\"PREF1\", \"PREF2\", ...}, {VAL1, VAL2, ...})"
      ]}
    {:name "getpref"
      :signatures [
        "VAL = getpref (\"GROUP\", \"PREF\")"
        "VAL = getpref (\"GROUP\", \"PREF\", DEFAULT)"
        "{VAL1, VAL2, ...} = getpref (\"GROUP\", {\"PREF1\", \"PREF2\", ...})"
        "PREFSTRUCT = getpref (\"GROUP\")"
        "PREFSTRUCT = getpref ()"
      ]}
    {:name "ispref"
      :signatures [
        "TF = ispref (\"GROUP\", \"PREF\")"
        "TF = ispref (\"GROUP\", {\"PREF1\", \"PREF2\", ...})"
        "TF = ispref (\"GROUP\")"
      ]}
    {:name "prefdir"
      :signatures [
        "DIR = prefdir"
        "DIR = prefdir (1)"
      ]}
    {:name "preferences"
      :signatures [
        "preferences"
      ]}
    {:name "rmpref"
      :signatures [
        "rmpref (\"GROUP\", \"PREF\")"
        "rmpref (\"GROUP\", {\"PREF1\", \"PREF2\", ...})"
        "rmpref (\"GROUP\")"
      ]}
    {:name "setpref"
      :signatures [
        "setpref (\"GROUP\", \"PREF\", VAL)"
        "setpref (\"GROUP\", {\"PREF1\", \"PREF2\", ...}, {VAL1, VAL2, ...})"
      ]}
  ])

(def +profiler+
  "Octave builtins from the `profiler` category, with signatures."
  [
    {:name "profexplore"
      :signatures [
        "profexplore ()"
        "profexplore (DATA)"
      ]}
    {:name "profexport"
      :signatures [
        "profexport (DIR)"
        "profexport (DIR, DATA)"
        "profexport (DIR, NAME)"
        "profexport (DIR, NAME, DATA)"
      ]}
    {:name "profile"
      :signatures [
        "profile on"
        "profile off"
        "profile resume"
        "profile clear"
        "S = profile (\"status\")"
        "T = profile (\"info\")"
      ]}
    {:name "profshow"
      :signatures [
        "profshow (DATA)"
        "profshow (DATA, N)"
        "profshow ()"
        "profshow (N)"
      ]}
  ])

(def +set+
  "Octave builtins from the `set` category, with signatures."
  [
    {:name "intersect"
      :signatures [
        "C = intersect (A, B)"
        "C = intersect (A, B, \"rows\")"
        "C = intersect (..., \"sorted\")"
        "C = intersect (..., \"stable\")"
        "C = intersect (..., \"legacy\")"
        "[C, IA, IB] = intersect (...)"
      ]}
    {:name "ismember"
      :signatures [
        "TF = ismember (A, S)"
        "TF = ismember (A, S, \"rows\")"
        "[TF, S_IDX] = ismember (...)"
      ]}
    {:name "powerset"
      :signatures [
        "P = powerset (A)"
        "P = powerset (A, \"rows\")"
      ]}
    {:name "setdiff"
      :signatures [
        "C = setdiff (A, B)"
        "C = setdiff (A, B, \"rows\")"
        "C = setdiff (..., \"sorted\")"
        "C = setdiff (..., \"stable\")"
        "C = setdiff (..., \"legacy\")"
        "[C, IA] = setdiff (...)"
      ]}
    {:name "setxor"
      :signatures [
        "C = setxor (A, B)"
        "C = setxor (A, B, \"rows\")"
        "C = setxor (..., \"sorted\")"
        "C = setxor (..., \"stable\")"
        "C = setxor (..., \"legacy\")"
        "[C, IA, IB] = setxor (...)"
      ]}
    {:name "union"
      :signatures [
        "C = union (A, B)"
        "C = union (A, B, \"rows\")"
        "C = union (..., \"sorted\")"
        "C = union (..., \"stable\")"
        "C = union (..., \"legacy\")"
        "[C, IA, IB] = union (...)"
      ]}
    {:name "unique"
      :signatures [
        "Y = unique (X)"
        "Y = unique (X, \"rows\")"
        "Y = unique (..., \"sorted\")"
        "Y = unique (..., \"stable\")"
        "[Y, I, J] = unique (...)"
        "[Y, I, J] = unique (..., \"first\")"
        "[Y, I, J] = unique (..., \"last\")"
        "[Y, I, J] = unique (..., \"legacy\")"
      ]}
    {:name "uniquetol"
      :signatures [
        "C = uniquetol (A)"
        "C = uniquetol (A, TOL)"
        "C = uniquetol (..., PROPERTY, VALUE)"
        "[C, IA, IC] = uniquetol (...)"
      ]}
  ])

(def +signal+
  "Octave builtins from the `signal` category, with signatures."
  [
    {:name "arch_fit"
      :signatures [
        "[A, B] = arch_fit (Y, X, P, ITER, GAMMA, A0, B0)"
      ]}
    {:name "arch_rnd"
      :signatures [
        "Y = arch_rnd (A, B, T)"
      ]}
    {:name "arch_test"
      :signatures [
        "[PVAL, LM] = arch_test (Y, X, P)"
      ]}
    {:name "arma_rnd"
      :signatures [
        "X = arma_rnd (A, B, V, T, N)"
      ]}
    {:name "autoreg_matrix"
      :signatures [
        "X = autoreg_matrix (Y, K)"
      ]}
    {:name "bartlett"
      :signatures [
        "C = bartlett (M)"
      ]}
    {:name "blackman"
      :signatures [
        "C = blackman (M)"
        "C = blackman (M, \"periodic\")"
        "C = blackman (M, \"symmetric\")"
      ]}
    {:name "detrend"
      :signatures [
        "Y = detrend (X, P)"
      ]}
    {:name "diffpara"
      :signatures [
        "[D, DD] = diffpara (X, A, B)"
      ]}
    {:name "durbinlevinson"
      :signatures [
        "[NEWPHI, NEWV] = durbinlevinson (C, OLDPHI, OLDV)"
      ]}
    {:name "fftconv"
      :signatures [
        "C = fftconv (X, Y)"
        "C = fftconv (X, Y, N)"
      ]}
    {:name "fftfilt"
      :signatures [
        "Y = fftfilt (B, X)"
        "Y = fftfilt (B, X, N)"
      ]}
    {:name "fftshift"
      :signatures [
        "Y = fftshift (X)"
        "Y = fftshift (X, DIM)"
      ]}
    {:name "filter2"
      :signatures [
        "Y = filter2 (B, X)"
        "Y = filter2 (B, X, SHAPE)"
      ]}
    {:name "fractdiff"
      :signatures [
        "FD = fractdiff (X, D)"
      ]}
    {:name "freqz"
      :signatures [
        "[H, W] = freqz (B, A, N, \"whole\")"
        "[H, W] = freqz (B)"
        "[H, W] = freqz (B, A)"
        "[H, W] = freqz (B, A, N)"
        "H = freqz (B, A, W)"
        "[H, W] = freqz (..., FS)"
        "freqz (...)"
      ]}
    {:name "freqz_plot"
      :signatures [
        "freqz_plot (W, H)"
        "freqz_plot (W, H, FREQ_NORM)"
      ]}
    {:name "hamming"
      :signatures [
        "C = hamming (M)"
        "C = hamming (M, \"periodic\")"
        "C = hamming (M, \"symmetric\")"
      ]}
    {:name "hanning"
      :signatures [
        "C = hanning (M)"
        "C = hanning (M, \"periodic\")"
        "C = hanning (M, \"symmetric\")"
      ]}
    {:name "hurst"
      :signatures [
        "H = hurst (X)"
      ]}
    {:name "ifftshift"
      :signatures [
        "Y = ifftshift (X)"
        "Y = ifftshift (X, DIM)"
      ]}
    {:name "movfun"
      :signatures [
        "Y = movfun (FCN, X, WLEN)"
        "Y = movfun (FCN, X, [NB, NA])"
        "Y = movfun (..., \"PROPERTY\", VALUE)"
      ]}
    {:name "movslice"
      :signatures [
        "SLCIDX = movslice (N, WLEN)"
        "[SLCIDX, C, CPRE, CPOST, WIN] = movslice (...)"
      ]}
    {:name "periodogram"
      :signatures [
        "[PXX, W] = periodogram (X)"
        "[PXX, W] = periodogram (X, WIN)"
        "[PXX, W] = periodogram (X, WIN, NFFT)"
        "[PXX, F] = periodogram (X, WIN, NFFT, FS)"
        "[PXX, F] = periodogram (..., \"RANGE\")"
        "periodogram (...)"
      ]}
    {:name "sinc"
      :signatures [
        "Y = sinc (X)"
      ]}
    {:name "sinetone"
      :signatures [
        "Y = sinetone (FREQ, RATE, SEC, AMPL)"
      ]}
    {:name "sinewave"
      :signatures [
        "Y = sinewave (M, N, D)"
      ]}
    {:name "spectral_adf"
      :signatures [
        "SDE = spectral_adf (C)"
        "SDE = spectral_adf (C, WIN)"
        "SDE = spectral_adf (C, WIN, B)"
      ]}
    {:name "spectral_xdf"
      :signatures [
        "SDE = spectral_xdf (X)"
        "SDE = spectral_xdf (X, WIN)"
        "SDE = spectral_xdf (X, WIN, B)"
      ]}
    {:name "spencer"
      :signatures [
        "SAVG = spencer (X)"
      ]}
    {:name "stft"
      :signatures [
        "Y = stft (X)"
        "Y = stft (X, WIN_SIZE)"
        "Y = stft (X, WIN_SIZE, INC)"
        "Y = stft (X, WIN_SIZE, INC, NUM_COEF)"
        "Y = stft (X, WIN_SIZE, INC, NUM_COEF, WIN_TYPE)"
        "[Y, C] = stft (...)"
      ]}
    {:name "synthesis"
      :signatures [
        "X = synthesis (Y, C)"
      ]}
    {:name "unwrap"
      :signatures [
        "B = unwrap (X)"
        "B = unwrap (X, TOL)"
        "B = unwrap (X, TOL, DIM)"
      ]}
    {:name "yulewalker"
      :signatures [
        "[A, V] = yulewalker (C)"
      ]}
  ])

(def +sparse+
  "Octave builtins from the `sparse` category, with signatures."
  [
    {:name "bicg"
      :signatures [
        "X = bicg (A, B)"
        "X = bicg (A, B, TOL)"
        "X = bicg (A, B, TOL, MAXIT)"
        "X = bicg (A, B, TOL, MAXIT, M)"
        "X = bicg (A, B, TOL, MAXIT, M1, M2)"
        "X = bicg (A, B, TOL, MAXIT, M, [], X0)"
        "X = bicg (A, B, TOL, MAXIT, M1, M2, X0)"
        "X = bicg (A, B, TOL, MAXIT, M, [], X0, ...)"
        "X = bicg (A, B, TOL, MAXIT, M1, M2, X0, ...)"
        "[X, FLAG, RELRES, ITER, RESVEC] = bicg (A, B, ...)"
      ]}
    {:name "bicgstab"
      :signatures [
        "X = bicgstab (A, B, TOL, MAXIT, M1, M2, X0, ...)"
        "X = bicgstab (A, B, TOL, MAXIT, M, [], X0, ...)"
        "[X, FLAG, RELRES, ITER, RESVEC] = bicgstab (A, B, ...)"
      ]}
    {:name "cgs"
      :signatures [
        "X = cgs (A, B, TOL, MAXIT, M1, M2, X0, ...)"
        "X = cgs (A, B, TOL, MAXIT, M, [], X0, ...)"
        "[X, FLAG, RELRES, ITER, RESVEC] = cgs (A, B, ...)"
      ]}
    {:name "colperm"
      :signatures [
        "P = colperm (S)"
      ]}
    {:name "eigs"
      :signatures [
        "D = eigs (A)"
        "D = eigs (A, K)"
        "D = eigs (A, K, SIGMA)"
        "D = eigs (A, K, SIGMA, OPTS)"
        "D = eigs (A, B)"
        "D = eigs (A, B, K)"
        "D = eigs (A, B, K, SIGMA)"
        "D = eigs (A, B, K, SIGMA, OPTS)"
        "D = eigs (AF, N)"
        "D = eigs (AF, N, K)"
        "D = eigs (AF, N, K, SIGMA)"
        "D = eigs (AF, N, K, SIGMA, OPTS)"
        "D = eigs (AF, N, B)"
        "D = eigs (AF, N, B, K)"
        "D = eigs (AF, N, B, K, SIGMA)"
        "D = eigs (AF, N, B, K, SIGMA, OPTS)"
        "[V, D] = eigs (...)"
        "[V, D, FLAG] = eigs (...)"
      ]}
    {:name "etreeplot"
      :signatures [
        "etreeplot (A)"
        "etreeplot (A, NODE_STYLE, EDGE_STYLE)"
      ]}
    {:name "gmres"
      :signatures [
        "X = gmres (A, B, RESTART, TOL, MAXIT, M1, M2, X0, ...)"
        "X = gmres (A, B, RESTART, TOL, MAXIT, M, [], X0, ...)"
        "[X, FLAG, RELRES, ITER, RESVEC] = gmres (A, B, ...)"
      ]}
    {:name "gplot"
      :signatures [
        "gplot (A, XY)"
        "gplot (A, XY, LINE_STYLE)"
        "[X, Y] = gplot (A, XY)"
      ]}
    {:name "ichol"
      :signatures [
        "L = ichol (A)"
        "L = ichol (A, OPTS)"
      ]}
    {:name "ilu"
      :signatures [
        "LUA = ilu (A)"
        "LUA = ilu (A, OPTS)"
        "[L, U] = ilu (...)"
        "[L, U, P] = ilu (...)"
      ]}
    {:name "nonzeros"
      :signatures [
        "V = nonzeros (A)"
      ]}
    {:name "pcg"
      :signatures [
        "X = pcg (A, B, TOL, MAXIT, M1, M2, X0, ...)"
        "X = pcg (A, B, TOL, MAXIT, M, [], X0, ...)"
        "[X, FLAG, RELRES, ITER, RESVEC, EIGEST] = pcg (A, B, ...)"
      ]}
    {:name "pcr"
      :signatures [
        "X = pcr (A, B, TOL, MAXIT, M, X0, ...)"
        "[X, FLAG, RELRES, ITER, RESVEC] = pcr (...)"
      ]}
    {:name "qmr"
      :signatures [
        "X = qmr (A, B, RTOL, MAXIT, M1, M2, X0)"
        "X = qmr (A, B, RTOL, MAXIT, P)"
        "[X, FLAG, RELRES, ITER, RESVEC] = qmr (A, B, ...)"
      ]}
    {:name "spaugment"
      :signatures [
        "S = spaugment (A, C)"
      ]}
    {:name "spconvert"
      :signatures [
        "X = spconvert (M)"
      ]}
    {:name "spdiags"
      :signatures [
        "B = spdiags (A)"
        "[B, D] = spdiags (A)"
        "B = spdiags (A, D)"
        "A = spdiags (V, D, A)"
        "A = spdiags (V, D, M, N)"
      ]}
    {:name "speye"
      :signatures [
        "S = speye (M, N)"
        "S = speye (M)"
        "S = speye (SZ)"
      ]}
    {:name "spfun"
      :signatures [
        "Y = spfun (F, S)"
      ]}
    {:name "spones"
      :signatures [
        "R = spones (S)"
      ]}
    {:name "sprand"
      :signatures [
        "S = sprand (M, N, D)"
        "S = sprand (M, N, D, RC)"
        "S = sprand (S)"
      ]}
    {:name "sprandn"
      :signatures [
        "S = sprandn (M, N, D)"
        "S = sprandn (M, N, D, RC)"
        "S = sprandn (S)"
      ]}
    {:name "sprandsym"
      :signatures [
        "S = sprandsym (N, D)"
        "S = sprandsym (S)"
      ]}
    {:name "spstats"
      :signatures [
        "[COUNT, MEAN, VAR] = spstats (S)"
        "[COUNT, MEAN, VAR] = spstats (S, J)"
      ]}
    {:name "spy"
      :signatures [
        "spy (X)"
        "spy (..., MARKERSIZE)"
        "spy (..., LINE_SPEC)"
      ]}
    {:name "svds"
      :signatures [
        "S = svds (A)"
        "S = svds (A, K)"
        "S = svds (A, K, SIGMA)"
        "S = svds (A, K, SIGMA, OPTS)"
        "[U, S, V] = svds (...)"
        "[U, S, V, FLAG] = svds (...)"
      ]}
    {:name "tfqmr"
      :signatures [
        "X = tfqmr (A, B, TOL, MAXIT, M1, M2, X0, ...)"
        "X = tfqmr (A, B, TOL, MAXIT, M, [], X0, ...)"
        "[X, FLAG, RELRES, ITER, RESVEC] = tfqmr (A, B, ...)"
      ]}
    {:name "treelayout"
      :signatures [
        "[X, Y] = treelayout (TREE)"
        "[X, Y] = treelayout (TREE, PERMUTATION)"
        "[X, Y, H, S] = treelayout (...)"
      ]}
    {:name "treeplot"
      :signatures [
        "treeplot (TREE)"
        "treeplot (TREE, NODE_STYLE, EDGE_STYLE)"
      ]}
  ])

(def +specfun+
  "Octave builtins from the `specfun` category, with signatures."
  [
    {:name "beta"
      :signatures [
        "Y = beta (A, B)"
      ]}
    {:name "betainc"
      :signatures [
        "I = betainc (X, A, B)"
        "I = betainc (X, A, B, TAIL)"
      ]}
    {:name "betaincinv"
      :signatures [
        "X = betaincinv (Y, A, B)"
        "X = betaincinv (Y, A, B, \"lower\")"
        "X = betaincinv (Y, A, B, \"upper\")"
      ]}
    {:name "betaln"
      :signatures [
        "LNB = betaln (A, B)"
      ]}
    {:name "cosint"
      :signatures [
        "Y = cosint (X)"
      ]}
    {:name "ellipke"
      :signatures [
        "K = ellipke (M)"
        "K = ellipke (M, TOL)"
        "[K, E] = ellipke (...)"
      ]}
    {:name "expint"
      :signatures [
        "Y = expint (X)"
      ]}
    {:name "factor"
      :signatures [
        "PF = factor (Q)"
        "[PF, N] = factor (Q)"
      ]}
    {:name "factorial"
      :signatures [
        "F = factorial (N)"
      ]}
    {:name "gammainc"
      :signatures [
        "Y = gammainc (X, A)"
        "Y = gammainc (X, A, TAIL)"
      ]}
    {:name "gammaincinv"
      :signatures [
        "X = gammaincinv (Y, A)"
        "X = gammaincinv (Y, A, TAIL)"
      ]}
    {:name "isprime"
      :signatures [
        "TF = isprime (X)"
      ]}
    {:name "lcm"
      :signatures [
        "L = lcm (X, Y)"
        "L = lcm (X, Y, ...)"
      ]}
    {:name "legendre"
      :signatures [
        "L = legendre (N, X)"
        "L = legendre (N, X, NORMALIZATION)"
      ]}
    {:name "nchoosek"
      :signatures [
        "C = nchoosek (N, K)"
        "C = nchoosek (SET, K)"
      ]}
    {:name "nthroot"
      :signatures [
        "Y = nthroot (X, N)"
      ]}
    {:name "perms"
      :signatures [
        "P = perms (V)"
        "P = perms (V, \"unique\")"
      ]}
    {:name "primes"
      :signatures [
        "P = primes (N)"
      ]}
    {:name "reallog"
      :signatures [
        "Y = reallog (X)"
      ]}
    {:name "realpow"
      :signatures [
        "Z = realpow (X, Y)"
      ]}
    {:name "realsqrt"
      :signatures [
        "Y = realsqrt (X)"
      ]}
    {:name "sinint"
      :signatures [
        "Y = sinint (X)"
      ]}
  ])

(def +special-matrix+
  "Octave builtins from the `special-matrix` category, with signatures."
  [
    {:name "gallery"
      :signatures [
        "gallery (NAME)"
        "gallery (NAME, ARGS)"
        "C = gallery (\"cauchy\", X)"
        "C = gallery (\"cauchy\", X, Y)"
        "C = gallery (\"chebspec\", N)"
        "C = gallery (\"chebspec\", N, K)"
        "C = gallery (\"chebvand\", P)"
        "C = gallery (\"chebvand\", M, P)"
        "A = gallery (\"chow\", N)"
        "A = gallery (\"chow\", N, ALPHA)"
        "A = gallery (\"chow\", N, ALPHA, DELTA)"
        "C = gallery (\"circul\", V)"
        "A = gallery (\"clement\", N)"
        "A = gallery (\"clement\", N, K)"
        "C = gallery (\"compar\", A)"
        "C = gallery (\"compar\", A, K)"
        "A = gallery (\"condex\", N)"
        "A = gallery (\"condex\", N, K)"
        "A = gallery (\"condex\", N, K, THETA)"
        "A = gallery (\"cycol\", [M N])"
        "A = gallery (\"cycol\", N)"
        "A = gallery (..., K)"
        "[C, D, E] = gallery (\"dorr\", N)"
        "[C, D, E] = gallery (\"dorr\", N, THETA)"
        "A = gallery (\"dorr\", ...)"
        "A = gallery (\"dramadah\", N)"
        "A = gallery (\"dramadah\", N, K)"
        "A = gallery (\"fiedler\", C)"
        "A = gallery (\"forsythe\", N)"
        "A = gallery (\"forsythe\", N, ALPHA)"
        "A = gallery (\"forsythe\", N, ALPHA, LAMBDA)"
        "F = gallery (\"frank\", N)"
        "F = gallery (\"frank\", N, K)"
        "C = gallery (\"gcdmat\", N)"
        "A = gallery (\"gearmat\", N)"
        "A = gallery (\"gearmat\", N, I)"
        "A = gallery (\"gearmat\", N, I, J)"
        "G = gallery (\"grcar\", N)"
        "G = gallery (\"grcar\", N, K)"
        "A = gallery (\"hanowa\", N)"
        "A = gallery (\"hanowa\", N, D)"
        "V = gallery (\"house\", X)"
        "[V, BETA] = gallery (\"house\", X)"
        "A = gallery (\"integerdata\", IMAX, [M N ...], J)"
        "A = gallery (\"integerdata\", IMAX, M, N, ..., J)"
        "A = gallery (\"integerdata\", [IMIN, IMAX], [M N ...], J)"
        "A = gallery (\"integerdata\", [IMIN, IMAX], M, N, ..., J)"
        "A = gallery (\"integerdata\", ..., \"CLASS\")"
        "A = gallery (\"invhess\", X)"
        "A = gallery (\"invhess\", X, Y)"
        "A = gallery (\"invol\", N)"
        "A = gallery (\"ipjfact\", N)"
        "A = gallery (\"ipjfact\", N, K)"
        "A = gallery (\"jordbloc\", N)"
        "A = gallery (\"jordbloc\", N, LAMBDA)"
        "U = gallery (\"kahan\", N)"
        "U = gallery (\"kahan\", N, THETA)"
        "U = gallery (\"kahan\", N, THETA, PERT)"
        "A = gallery (\"kms\", N)"
        "A = gallery (\"kms\", N, RHO)"
        "B = gallery (\"krylov\", A)"
        "B = gallery (\"krylov\", A, X)"
        "B = gallery (\"krylov\", A, X, J)"
        "A = gallery (\"lauchli\", N)"
        "A = gallery (\"lauchli\", N, MU)"
        "A = gallery (\"lehmer\", N)"
        "T = gallery (\"lesp\", N)"
        "A = gallery (\"lotkin\", N)"
        "A = gallery (\"minij\", N)"
        "A = gallery (\"moler\", N)"
        "A = gallery (\"moler\", N, ALPHA)"
        "[A, T] = gallery (\"neumann\", N)"
        "A = gallery (\"normaldata\", [M N ...], J)"
        "A = gallery (\"normaldata\", M, N, ..., J)"
        "A = gallery (\"normaldata\", ..., \"CLASS\")"
        "Q = gallery (\"orthog\", N)"
        "Q = gallery (\"orthog\", N, K)"
        "A = gallery (\"parter\", N)"
        "P = gallery (\"pei\", N)"
        "P = gallery (\"pei\", N, ALPHA)"
        "A = gallery (\"poisson\", N)"
        "A = gallery (\"prolate\", N)"
        "A = gallery (\"prolate\", N, W)"
        "H = gallery (\"randhess\", X)"
        "A = gallery (\"rando\", N)"
        "A = gallery (\"rando\", N, K)"
        "A = gallery (\"randsvd\", N)"
        "A = gallery (\"randsvd\", N, KAPPA)"
        "A = gallery (\"randsvd\", N, KAPPA, MODE)"
        "A = gallery (\"randsvd\", N, KAPPA, MODE, KL)"
        "A = gallery (\"randsvd\", N, KAPPA, MODE, KL, KU)"
        "A = gallery (\"redheff\", N)"
        "A = gallery (\"riemann\", N)"
        "A = gallery (\"ris\", N)"
        "A = gallery (\"smoke\", N)"
        "A = gallery (\"smoke\", N, K)"
        "T = gallery (\"toeppd\", N)"
        "T = gallery (\"toeppd\", N, M)"
        "T = gallery (\"toeppd\", N, M, W)"
        "T = gallery (\"toeppd\", N, M, W, THETA)"
        "P = gallery (\"toeppen\", N)"
        "P = gallery (\"toeppen\", N, A)"
        "P = gallery (\"toeppen\", N, A, B)"
        "P = gallery (\"toeppen\", N, A, B, C)"
        "P = gallery (\"toeppen\", N, A, B, C, D)"
        "P = gallery (\"toeppen\", N, A, B, C, D, E)"
        "A = gallery (\"tridiag\", X, Y, Z)"
        "A = gallery (\"tridiag\", N)"
        "A = gallery (\"tridiag\", N, C, D, E)"
        "T = gallery (\"triw\", N)"
        "T = gallery (\"triw\", N, ALPHA)"
        "T = gallery (\"triw\", N, ALPHA, K)"
        "A = gallery (\"uniformdata\", [M N ...], J)"
        "A = gallery (\"uniformdata\", M, N, ..., J)"
        "A = gallery (\"uniformdata\", ..., \"CLASS\")"
        "A = gallery (\"wathen\", NX, NY)"
        "A = gallery (\"wathen\", NX, NY, K)"
        "[A, B] = gallery (\"wilk\", N)"
      ]}
    {:name "hadamard"
      :signatures [
        "H = hadamard (N)"
      ]}
    {:name "hankel"
      :signatures [
        "H = hankel (C)"
        "H = hankel (C, R)"
      ]}
    {:name "hilb"
      :signatures [
        "H = hilb (N)"
      ]}
    {:name "invhilb"
      :signatures [
        "HINV = invhilb (N)"
      ]}
    {:name "magic"
      :signatures [
        "M = magic (N)"
      ]}
    {:name "pascal"
      :signatures [
        "P = pascal (N)"
        "P = pascal (N, T)"
      ]}
    {:name "rosser"
      :signatures [
        "R = rosser ()"
      ]}
    {:name "toeplitz"
      :signatures [
        "T = toeplitz (C)"
        "T = toeplitz (C, R)"
      ]}
    {:name "vander"
      :signatures [
        "V = vander (C)"
        "V = vander (C, N)"
      ]}
    {:name "wilkinson"
      :signatures [
        "W = wilkinson (N)"
      ]}
  ])

(def +statistics+
  "Octave builtins from the `statistics` category, with signatures."
  [
    {:name "bounds"
      :signatures [
        "[S, L] = bounds (X)"
        "[S, L] = bounds (X, DIM)"
        "[S, L] = bounds (..., \"nanflag\")"
      ]}
    {:name "center"
      :signatures [
        "Y = center (X)"
        "Y = center (X, DIM)"
      ]}
    {:name "corr"
      :signatures [
        "R = corr (X)"
        "R = corr (X, Y)"
      ]}
    {:name "corrcoef"
      :signatures [
        "R = corrcoef (X)"
        "R = corrcoef (X, Y)"
        "R = corrcoef (..., PARAM, VALUE, ...)"
        "[R, P] = corrcoef (...)"
        "[R, P, LCI, HCI] = corrcoef (...)"
      ]}
    {:name "cov"
      :signatures [
        "C = cov (X)"
        "C = cov (X, OPT)"
        "C = cov (X, Y)"
        "C = cov (X, Y, OPT)"
      ]}
    {:name "discrete_cdf"
      :signatures [
        "CDF = discrete_cdf (X, V, P)"
      ]}
    {:name "discrete_inv"
      :signatures [
        "Q = discrete_inv (X, V, P)"
      ]}
    {:name "discrete_pdf"
      :signatures [
        "PDF = discrete_pdf (X, V, P)"
      ]}
    {:name "discrete_rnd"
      :signatures [
        "RND = discrete_rnd (V, P)"
        "RND = discrete_rnd (V, P, R)"
        "RND = discrete_rnd (V, P, R, C, ...)"
        "RND = discrete_rnd (V, P, [SZ])"
      ]}
    {:name "empirical_cdf"
      :signatures [
        "CDF = empirical_cdf (X, DATA)"
      ]}
    {:name "empirical_inv"
      :signatures [
        "Q = empirical_inv (X, DATA)"
      ]}
    {:name "empirical_pdf"
      :signatures [
        "PDF = empirical_pdf (X, DATA)"
      ]}
    {:name "empirical_rnd"
      :signatures [
        "RND = empirical_rnd (DATA)"
        "RND = empirical_rnd (DATA, R)"
        "RND = empirical_rnd (DATA, R, C, ...)"
        "RND = empirical_rnd (DATA, [SZ])"
      ]}
    {:name "histc"
      :signatures [
        "N = histc (X, EDGES)"
        "N = histc (X, EDGES, DIM)"
        "[N, IDX] = histc (...)"
      ]}
    {:name "iqr"
      :signatures [
        "Z = iqr (X)"
        "Z = iqr (X, DIM)"
        "Z = iqr (X, \"ALL\")"
      ]}
    {:name "kendall"
      :signatures [
        "TAU = kendall (X)"
        "TAU = kendall (X, Y)"
      ]}
    {:name "kurtosis"
      :signatures [
        "Y = kurtosis (X)"
        "Y = kurtosis (X, FLAG)"
        "Y = kurtosis (X, FLAG, DIM)"
      ]}
    {:name "mad"
      :signatures [
        "Y = mad (X)"
        "Y = mad (X, OPT)"
        "Y = mad (X, OPT, DIM)"
      ]}
    {:name "mean"
      :signatures [
        "Y = mean (X)"
        "Y = mean (X, 'all')"
        "Y = mean (X, DIM)"
        "Y = mean (..., 'OUTTYPE')"
        "Y = mean (..., 'NANFLAG')"
      ]}
    {:name "meansq"
      :signatures [
        "Y = meansq (X)"
        "Y = meansq (X, DIM)"
      ]}
    {:name "median"
      :signatures [
        "Y = median (X)"
        "Y = median (X, DIM)"
      ]}
    {:name "mode"
      :signatures [
        "M = mode (X)"
        "M = mode (X, DIM)"
        "[M, F, C] = mode (...)"
      ]}
    {:name "moment"
      :signatures [
        "M = moment (X, P)"
        "M = moment (X, P, TYPE)"
        "M = moment (X, P, DIM)"
        "M = moment (X, P, TYPE, DIM)"
        "M = moment (X, P, DIM, TYPE)"
      ]}
    {:name "movmad"
      :signatures [
        "Y = movmad (X, WLEN)"
        "Y = movmad (X, [NB, NA])"
        "Y = movmad (..., DIM)"
        "Y = movmad (..., \"NANCOND\")"
        "Y = movmad (..., PROPERTY, VALUE)"
      ]}
    {:name "movmax"
      :signatures [
        "Y = movmax (X, WLEN)"
        "Y = movmax (X, [NB, NA])"
        "Y = movmax (..., DIM)"
        "Y = movmax (..., \"NANCOND\")"
        "Y = movmax (..., PROPERTY, VALUE)"
      ]}
    {:name "movmean"
      :signatures [
        "Y = movmean (X, WLEN)"
        "Y = movmean (X, [NB, NA])"
        "Y = movmean (..., DIM)"
        "Y = movmean (..., \"NANCOND\")"
        "Y = movmean (..., PROPERTY, VALUE)"
      ]}
    {:name "movmedian"
      :signatures [
        "Y = movmedian (X, WLEN)"
        "Y = movmedian (X, [NB, NA])"
        "Y = movmedian (..., DIM)"
        "Y = movmedian (..., \"NANCOND\")"
        "Y = movmedian (..., PROPERTY, VALUE)"
      ]}
    {:name "movmin"
      :signatures [
        "Y = movmin (X, WLEN)"
        "Y = movmin (X, [NB, NA])"
        "Y = movmin (..., DIM)"
        "Y = movmin (..., \"NANCOND\")"
        "Y = movmin (..., PROPERTY, VALUE)"
      ]}
    {:name "movprod"
      :signatures [
        "Y = movprod (X, WLEN)"
        "Y = movprod (X, [NB, NA])"
        "Y = movprod (..., DIM)"
        "Y = movprod (..., \"NANCOND\")"
        "Y = movprod (..., PROPERTY, VALUE)"
      ]}
    {:name "movstd"
      :signatures [
        "Y = movstd (X, WLEN)"
        "Y = movstd (X, [NB, NA])"
        "Y = movstd (..., OPT)"
        "Y = movstd (..., OPT, DIM)"
        "Y = movstd (..., \"NANCOND\")"
        "Y = movstd (..., PROPERTY, VALUE)"
      ]}
    {:name "movsum"
      :signatures [
        "Y = movsum (X, WLEN)"
        "Y = movsum (X, [NB, NA])"
        "Y = movsum (..., DIM)"
        "Y = movsum (..., \"NANCOND\")"
        "Y = movsum (..., PROPERTY, VALUE)"
      ]}
    {:name "movvar"
      :signatures [
        "Y = movvar (X, WLEN)"
        "Y = movvar (X, [NB, NA])"
        "Y = movvar (..., OPT)"
        "Y = movvar (..., OPT, DIM)"
        "Y = movvar (..., \"NANCOND\")"
        "Y = movvar (..., PROPERTY, VALUE)"
      ]}
    {:name "normalize"
      :signatures [
        "Z = normalize (X)"
        "Z = normalize (X, DIM)"
        "Z = normalize (..., METHOD)"
        "Z = normalize (..., METHOD, OPTION)"
        "Z = normalize (..., SCALE, SCALEOPTION, CENTER, CENTEROPTION)"
        "[Z, C, S] = normalize (...)"
      ]}
    {:name "prctile"
      :signatures [
        "Q = prctile (X)"
        "Q = prctile (X, P)"
        "Q = prctile (X, P, DIM)"
      ]}
    {:name "quantile"
      :signatures [
        "Q = quantile (X)"
        "Q = quantile (X, P)"
        "Q = quantile (X, P, DIM)"
        "Q = quantile (X, P, DIM, METHOD)"
      ]}
    {:name "range"
      :signatures [
        "Y = range (X)"
        "Y = range (X, DIM)"
      ]}
    {:name "ranks"
      :signatures [
        "Y = ranks (X)"
        "Y = ranks (X, DIM)"
        "Y = ranks (X, DIM, RTYPE)"
      ]}
    {:name "run_count"
      :signatures [
        "CNT = run_count (X, N)"
        "CNT = run_count (X, N, DIM)"
      ]}
    {:name "runlength"
      :signatures [
        "count = runlength (X)"
        "[count, value] = runlength (X)"
      ]}
    {:name "skewness"
      :signatures [
        "Y = skewness (X)"
        "Y = skewness (X, FLAG)"
        "Y = skewness (X, FLAG, DIM)"
      ]}
    {:name "spearman"
      :signatures [
        "RHO = spearman (X)"
        "RHO = spearman (X, Y)"
      ]}
    {:name "statistics"
      :signatures [
        "STATS = statistics (X)"
        "STATS = statistics (X, DIM)"
      ]}
    {:name "std"
      :signatures [
        "Y = std (X)"
        "Y = std (X, W)"
        "Y = std (X, W, DIM)"
        "Y = std (X, W, \"ALL\")"
        "[Y, MU] = std (...)"
      ]}
    {:name "var"
      :signatures [
        "V = var (X)"
        "V = var (X, W)"
        "V = var (X, W, DIM)"
        "V = var (X, W, \"ALL\")"
        "[V, M] = var (...)"
      ]}
    {:name "zscore"
      :signatures [
        "Z = zscore (X)"
        "Z = zscore (X, OPT)"
        "Z = zscore (X, OPT, DIM)"
        "[Z, MU, SIGMA] = zscore (...)"
      ]}
  ])

(def +strings+
  "Octave builtins from the `strings` category, with signatures."
  [
    {:name "base2dec"
      :signatures [
        "D = base2dec (STR, BASE)"
      ]}
    {:name "bin2dec"
      :signatures [
        "D = bin2dec (STR)"
      ]}
    {:name "blanks"
      :signatures [
        "STR = blanks (N)"
      ]}
    {:name "cstrcat"
      :signatures [
        "STR = cstrcat (S1, S2, ...)"
      ]}
    {:name "deblank"
      :signatures [
        "S = deblank (S)"
      ]}
    {:name "dec2base"
      :signatures [
        "STR = dec2base (D, BASE)"
        "STR = dec2base (D, BASE, LEN)"
      ]}
    {:name "dec2bin"
      :signatures [
        "BSTR = dec2bin (D)"
        "BSTR = dec2bin (D, LEN)"
      ]}
    {:name "dec2hex"
      :signatures [
        "HSTR = dec2hex (D)"
        "HSTR = dec2hex (D, LEN)"
      ]}
    {:name "endsWith"
      :signatures [
        "RETVAL = endsWith (STR, PATTERN)"
        "RETVAL = endsWith (STR, PATTERN, \"IgnoreCase\", IGNORE_CASE)"
      ]}
    {:name "erase"
      :signatures [
        "NEWSTR = erase (STR, PTN)"
      ]}
    {:name "hex2dec"
      :signatures [
        "D = hex2dec (STR)"
      ]}
    {:name "index"
      :signatures [
        "N = index (S, T)"
        "N = index (S, T, DIRECTION)"
      ]}
    {:name "isletter"
      :signatures [
        "TF = isletter (S)"
      ]}
    {:name "isstring"
      :signatures [
        "TF = isstring (S)"
      ]}
    {:name "isstrprop"
      :signatures [
        "TF = isstrprop (STR, PROP)"
      ]}
    {:name "mat2str"
      :signatures [
        "S = mat2str (X, N)"
        "S = mat2str (X, N, \"class\")"
      ]}
    {:name "native2unicode"
      :signatures [
        "UTF8_STR = native2unicode (NATIVE_BYTES, CODEPAGE)"
        "UTF8_STR = native2unicode (NATIVE_BYTES)"
      ]}
    {:name "ostrsplit"
      :signatures [
        "[CSTR] = ostrsplit (S, SEP)"
        "[CSTR] = ostrsplit (S, SEP, STRIP_EMPTY)"
      ]}
    {:name "regexptranslate"
      :signatures [
        "STR = regexptranslate (OP, S)"
      ]}
    {:name "rindex"
      :signatures [
        "N = rindex (S, T)"
      ]}
    {:name "startsWith"
      :signatures [
        "RETVAL = startsWith (STR, PATTERN)"
        "RETVAL = startsWith (STR, PATTERN, \"IgnoreCase\", IGNORE_CASE)"
      ]}
    {:name "str2num"
      :signatures [
        "X = str2num (S)"
        "[X, STATE] = str2num (S)"
      ]}
    {:name "strcat"
      :signatures [
        "STR = strcat (S1, S2, ...)"
      ]}
    {:name "strchr"
      :signatures [
        "IDX = strchr (STR, CHARS)"
        "IDX = strchr (STR, CHARS, N)"
        "IDX = strchr (STR, CHARS, N, DIRECTION)"
        "[I, J] = strchr (...)"
      ]}
    {:name "strjoin"
      :signatures [
        "STR = strjoin (CSTR)"
        "STR = strjoin (CSTR, DELIMITER)"
      ]}
    {:name "strjust"
      :signatures [
        "STR = strjust (S)"
        "STR = strjust (S, POS)"
      ]}
    {:name "strsplit"
      :signatures [
        "[CSTR] = strsplit (STR)"
        "[CSTR] = strsplit (STR, DEL)"
        "[CSTR] = strsplit (..., NAME, VALUE)"
        "[CSTR, MATCHES] = strsplit (...)"
      ]}
    {:name "strtok"
      :signatures [
        "[TOK, REM] = strtok (STR)"
        "[TOK, REM] = strtok (STR, DELIM)"
      ]}
    {:name "strtrim"
      :signatures [
        "S = strtrim (S)"
      ]}
    {:name "strtrunc"
      :signatures [
        "S = strtrunc (S, N)"
      ]}
    {:name "substr"
      :signatures [
        "STR = substr (S, OFFSET)"
        "substr (S, OFFSET, LEN)"
      ]}
    {:name "unicode2native"
      :signatures [
        "NATIVE_BYTES = unicode2native (UTF8_STR, CODEPAGE)"
        "NATIVE_BYTES = unicode2native (UTF8_STR)"
      ]}
    {:name "untabify"
      :signatures [
        "STR = untabify (T)"
        "STR = untabify (T, TW)"
        "STR = untabify (T, TW, DEBLANK)"
      ]}
    {:name "validatestring"
      :signatures [
        "VALIDSTR = validatestring (STR, STRARRAY)"
        "VALIDSTR = validatestring (STR, STRARRAY, FUNCNAME)"
        "VALIDSTR = validatestring (STR, STRARRAY, FUNCNAME, VARNAME)"
        "VALIDSTR = validatestring (..., POSITION)"
      ]}
  ])

(def +testfun+
  "Octave builtins from the `testfun` category, with signatures."
  [
    {:name "assert"
      :signatures [
        "assert (COND)"
        "assert (COND, ERRMSG)"
        "assert (COND, ERRMSG, ...)"
        "assert (COND, MSG_ID, ERRMSG, ...)"
        "assert (OBSERVED, EXPECTED)"
        "assert (OBSERVED, EXPECTED, TOL)"
      ]}
    {:name "demo"
      :signatures [
        "demo NAME"
        "demo NAME N"
        "demo (\"NAME\")"
        "demo (\"NAME\", N)"
      ]}
    {:name "example"
      :signatures [
        "example NAME"
        "example NAME N"
        "example (\"NAME\")"
        "example (\"NAME\", N)"
        "[CODESTR, CODEIDX] = example (...)"
      ]}
    {:name "fail"
      :signatures [
        "STATUS = fail (CODE)"
        "STATUS = fail (CODE, PATTERN)"
        "STATUS = fail (CODE, \"warning\")"
        "STATUS = fail (CODE, \"warning\", PATTERN)"
      ]}
    {:name "oruntests"
      :signatures [
        "oruntests ()"
        "oruntests (DIRECTORY)"
      ]}
    {:name "rundemos"
      :signatures [
        "rundemos ()"
        "rundemos (DIRECTORY)"
      ]}
    {:name "speed"
      :signatures [
        "speed (F, INIT, MAX_N, F2, TOL)"
        "[ORDER, N, T_F, T_F2] = speed (...)"
      ]}
    {:name "test"
      :signatures [
        "test NAME"
        "test NAME quiet|normal|verbose"
        "test (\"NAME\", \"quiet|normal|verbose\", FID)"
        "test (\"NAME\", \"quiet|normal|verbose\", FNAME)"
        "SUCCESS = test (...)"
        "[N, NMAX, NXFAIL, NBUG, NSKIP, NRTSKIP, NREGRESSION] = test (...)"
        "[CODE, IDX] = test (\"NAME\", \"grabdemo\")"
        "test ([], \"explain\", FID)"
        "test ([], \"explain\", FNAME)"
      ]}
  ])

(def +time+
  "Octave builtins from the `time` category, with signatures."
  [
    {:name "addtodate"
      :signatures [
        "D = addtodate (D, Q, F)"
      ]}
    {:name "asctime"
      :signatures [
        "STR = asctime (TM_STRUCT)"
      ]}
    {:name "calendar"
      :signatures [
        "C = calendar ()"
        "C = calendar (D)"
        "C = calendar (Y, M)"
        "calendar (...)"
      ]}
    {:name "clock"
      :signatures [
        "DATEVEC = clock ()"
        "[DATEVEC, ISDST] = clock ()"
      ]}
    {:name "ctime"
      :signatures [
        "STR = ctime (T)"
      ]}
    {:name "date"
      :signatures [
        "STR = date ()"
      ]}
    {:name "datenum"
      :signatures [
        "DAYS = datenum (DATEVEC)"
        "DAYS = datenum (YEAR, MONTH, DAY)"
        "DAYS = datenum (YEAR, MONTH, DAY, HOUR)"
        "DAYS = datenum (YEAR, MONTH, DAY, HOUR, MINUTE)"
        "DAYS = datenum (YEAR, MONTH, DAY, HOUR, MINUTE, SECOND)"
        "DAYS = datenum (\"datestr\")"
        "DAYS = datenum (\"datestr\", F)"
        "DAYS = datenum (\"datestr\", P)"
        "[DAYS, SECS] = datenum (...)"
      ]}
    {:name "datestr"
      :signatures [
        "STR = datestr (DATE)"
        "STR = datestr (DATE, F)"
        "STR = datestr (DATE, F, P)"
      ]}
    {:name "datevec"
      :signatures [
        "V = datevec (DATE)"
        "V = datevec (DATE, F)"
        "V = datevec (DATE, P)"
        "V = datevec (DATE, F, P)"
        "[Y, M, D, H, MI, S] = datevec (...)"
      ]}
    {:name "eomday"
      :signatures [
        "E = eomday (Y, M)"
      ]}
    {:name "etime"
      :signatures [
        "SECS = etime (T2, T1)"
      ]}
    {:name "is_leap_year"
      :signatures [
        "TF = is_leap_year ()"
        "TF = is_leap_year (YEAR)"
      ]}
    {:name "now"
      :signatures [
        "t = now ()"
      ]}
    {:name "weekday"
      :signatures [
        "[N, S] = weekday (D)"
        "[N, S] = weekday (D, FORMAT)"
      ]}
  ])

(def +web+
  "Octave builtins from the `web` category, with signatures."
  [
    {:name "web"
      :signatures [
        "STATUS = web ()"
        "STATUS = web (URL)"
        "STATUS = web (URL, OPTION)"
        "STATUS = web (URL, OPTION_1, ..., OPTION_N)"
        "[STATUS, H, URL] = web (...)"
      ]}
    {:name "weboptions"
      :signatures [
        "OUTPUT = weboptions ()"
        "OUTPUT = weboptions (NAME1, VALUE1, ...)"
      ]}
    {:name "webread"
      :signatures [
        "RESPONSE = webread (URL)"
        "RESPONSE = webread (URL, NAME1, VALUE1, ...)"
        "RESPONSE = webread (..., OPTIONS)"
      ]}
    {:name "webwrite"
      :signatures [
        "RESPONSE = webwrite (URL, NAME1, VALUE1, ...)"
        "RESPONSE = webwrite (URL, DATA)"
        "RESPONSE = webwrite (..., OPTIONS)"
      ]}
  ])

(def +all+
  "All documented Octave builtins with signatures (1654 total)."
  (vec (concat +containers+ +matlab+ +ftp+ +audio+ +core+ +deprecated+ +elfun+ +external+ +general+ +geometry+ +gui+ +help+ +image+ +io+ +java+ +legacy+ +linear-algebra+ +miscellaneous+ +ode+ +optimization+ +path+ +pkg+ +plot+ +polynomial+ +prefs+ +profiler+ +set+ +signal+ +sparse+ +specfun+ +special-matrix+ +statistics+ +strings+ +testfun+ +time+ +web+)))

(def +count+
  "Total number of documented Octave builtins."
  1654)
