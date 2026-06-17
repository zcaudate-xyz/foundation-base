(ns haskell.core.builtin
  "Generated outline of Haskell Prelude builtins from `ghci :browse Prelude`."
  (:require [hara.lang :as l]
            [std.lib.foundation :as f]))

(l/script :haskell
  haskell.core
  {:macro-only true})

(def +prelude+
  "Haskell Prelude functions and operators with type signatures."
  [
    {:name "!!"
     :signature "(!!) :: GHC.Stack.Types.HasCallStack => [a] -> Int -> a"}
    {:name "$"
     :signature "($) :: (a -> b) -> a -> b"}
    {:name "$!"
     :signature "($!) :: (a -> b) -> a -> b"}
    {:name "&&"
     :signature "(&&) :: Bool -> Bool -> Bool"}
    {:name "++"
     :signature "(++) :: [a] -> [a] -> [a]"}
    {:name "."
     :signature "(.) :: (b -> c) -> (a -> b) -> a -> c"}
    {:name "<$>"
     :signature "(<$>) :: Functor f => (a -> b) -> f a -> f b"}
    {:name "=<<"
     :signature "(=<<) :: Monad m => (a -> m b) -> m a -> m b"}
    {:name "pure"
     :signature "pure :: a -> f a"}
    {:name "<*>"
     :signature "(<*>) :: f (a -> b) -> f a -> f b"}
    {:name "liftA2"
     :signature "liftA2 :: (a -> b -> c) -> f a -> f b -> f c"}
    {:name "*>"
     :signature "(*>) :: f a -> f b -> f b"}
    {:name "<*"
     :signature "(<*) :: f a -> f b -> f a"}
    {:name "minBound"
     :signature "minBound :: a"}
    {:name "maxBound"
     :signature "maxBound :: a"}
    {:name "succ"
     :signature "succ :: a -> a"}
    {:name "pred"
     :signature "pred :: a -> a"}
    {:name "toEnum"
     :signature "toEnum :: Int -> a"}
    {:name "fromEnum"
     :signature "fromEnum :: a -> Int"}
    {:name "enumFrom"
     :signature "enumFrom :: a -> [a]"}
    {:name "enumFromThen"
     :signature "enumFromThen :: a -> a -> [a]"}
    {:name "enumFromTo"
     :signature "enumFromTo :: a -> a -> [a]"}
    {:name "enumFromThenTo"
     :signature "enumFromThenTo :: a -> a -> a -> [a]"}
    {:name "=="
     :signature "(==) :: a -> a -> Bool"}
    {:name "/="
     :signature "(/=) :: a -> a -> Bool"}
    {:name "pi"
     :signature "pi :: a"}
    {:name "exp"
     :signature "exp :: a -> a"}
    {:name "log"
     :signature "log :: a -> a"}
    {:name "sqrt"
     :signature "sqrt :: a -> a"}
    {:name "**"
     :signature "(**) :: a -> a -> a"}
    {:name "logBase"
     :signature "logBase :: a -> a -> a"}
    {:name "sin"
     :signature "sin :: a -> a"}
    {:name "cos"
     :signature "cos :: a -> a"}
    {:name "tan"
     :signature "tan :: a -> a"}
    {:name "asin"
     :signature "asin :: a -> a"}
    {:name "acos"
     :signature "acos :: a -> a"}
    {:name "atan"
     :signature "atan :: a -> a"}
    {:name "sinh"
     :signature "sinh :: a -> a"}
    {:name "cosh"
     :signature "cosh :: a -> a"}
    {:name "tanh"
     :signature "tanh :: a -> a"}
    {:name "asinh"
     :signature "asinh :: a -> a"}
    {:name "acosh"
     :signature "acosh :: a -> a"}
    {:name "atanh"
     :signature "atanh :: a -> a"}
    {:name "log1p"
     :signature "log1p :: a -> a"}
    {:name "expm1"
     :signature "expm1 :: a -> a"}
    {:name "log1pexp"
     :signature "log1pexp :: a -> a"}
    {:name "log1mexp"
     :signature "log1mexp :: a -> a"}
    {:name "fold"
     :signature "fold :: Monoid m => t m -> m"}
    {:name "foldMap"
     :signature "foldMap :: Monoid m => (a -> m) -> t a -> m"}
    {:name "foldMap'"
     :signature "foldMap' :: Monoid m => (a -> m) -> t a -> m"}
    {:name "foldr"
     :signature "foldr :: (a -> b -> b) -> b -> t a -> b"}
    {:name "foldr'"
     :signature "foldr' :: (a -> b -> b) -> b -> t a -> b"}
    {:name "foldl"
     :signature "foldl :: (b -> a -> b) -> b -> t a -> b"}
    {:name "foldl'"
     :signature "foldl' :: (b -> a -> b) -> b -> t a -> b"}
    {:name "foldr1"
     :signature "foldr1 :: (a -> a -> a) -> t a -> a"}
    {:name "foldl1"
     :signature "foldl1 :: (a -> a -> a) -> t a -> a"}
    {:name "toList"
     :signature "toList :: t a -> [a]"}
    {:name "null"
     :signature "null :: t a -> Bool"}
    {:name "length"
     :signature "length :: t a -> Int"}
    {:name "elem"
     :signature "elem :: Eq a => a -> t a -> Bool"}
    {:name "maximum"
     :signature "maximum :: Ord a => t a -> a"}
    {:name "minimum"
     :signature "minimum :: Ord a => t a -> a"}
    {:name "sum"
     :signature "sum :: Num a => t a -> a"}
    {:name "product"
     :signature "product :: Num a => t a -> a"}
    {:name "/"
     :signature "(/) :: a -> a -> a"}
    {:name "recip"
     :signature "recip :: a -> a"}
    {:name "fromRational"
     :signature "fromRational :: Rational -> a"}
    {:name "fmap"
     :signature "fmap :: (a -> b) -> f a -> f b"}
    {:name "<$"
     :signature "(<$) :: a -> f b -> f a"}
    {:name "quot"
     :signature "quot :: a -> a -> a"}
    {:name "rem"
     :signature "rem :: a -> a -> a"}
    {:name "div"
     :signature "div :: a -> a -> a"}
    {:name "mod"
     :signature "mod :: a -> a -> a"}
    {:name "quotRem"
     :signature "quotRem :: a -> a -> (a, a)"}
    {:name "divMod"
     :signature "divMod :: a -> a -> (a, a)"}
    {:name "toInteger"
     :signature "toInteger :: a -> Integer"}
    {:name ">>="
     :signature "(>>=) :: m a -> (a -> m b) -> m b"}
    {:name ">>"
     :signature "(>>) :: m a -> m b -> m b"}
    {:name "return"
     :signature "return :: a -> m a"}
    {:name "fail"
     :signature "fail :: String -> m a"}
    {:name "mempty"
     :signature "mempty :: a"}
    {:name "mappend"
     :signature "mappend :: a -> a -> a"}
    {:name "mconcat"
     :signature "mconcat :: [a] -> a"}
    {:name "+"
     :signature "(+) :: a -> a -> a"}
    {:name "-"
     :signature "(-) :: a -> a -> a"}
    {:name "*"
     :signature "(*) :: a -> a -> a"}
    {:name "negate"
     :signature "negate :: a -> a"}
    {:name "abs"
     :signature "abs :: a -> a"}
    {:name "signum"
     :signature "signum :: a -> a"}
    {:name "fromInteger"
     :signature "fromInteger :: Integer -> a"}
    {:name "compare"
     :signature "compare :: a -> a -> Ordering"}
    {:name "<"
     :signature "(<) :: a -> a -> Bool"}
    {:name "<="
     :signature "(<=) :: a -> a -> Bool"}
    {:name ">"
     :signature "(>) :: a -> a -> Bool"}
    {:name ">="
     :signature "(>=) :: a -> a -> Bool"}
    {:name "max"
     :signature "max :: a -> a -> a"}
    {:name "min"
     :signature "min :: a -> a -> a"}
    {:name "readsPrec"
     :signature "readsPrec :: Int -> ReadS a"}
    {:name "readList"
     :signature "readList :: ReadS [a]"}
    {:name "readPrec"
     :signature "readPrec :: Text.ParserCombinators.ReadPrec.ReadPrec a"}
    {:name "readListPrec"
     :signature "readListPrec :: Text.ParserCombinators.ReadPrec.ReadPrec"}
    {:name "toRational"
     :signature "toRational :: a -> Rational"}
    {:name "floatRadix"
     :signature "floatRadix :: a -> Integer"}
    {:name "floatDigits"
     :signature "floatDigits :: a -> Int"}
    {:name "floatRange"
     :signature "floatRange :: a -> (Int, Int)"}
    {:name "decodeFloat"
     :signature "decodeFloat :: a -> (Integer, Int)"}
    {:name "encodeFloat"
     :signature "encodeFloat :: Integer -> Int -> a"}
    {:name "exponent"
     :signature "exponent :: a -> Int"}
    {:name "significand"
     :signature "significand :: a -> a"}
    {:name "scaleFloat"
     :signature "scaleFloat :: Int -> a -> a"}
    {:name "isNaN"
     :signature "isNaN :: a -> Bool"}
    {:name "isInfinite"
     :signature "isInfinite :: a -> Bool"}
    {:name "isDenormalized"
     :signature "isDenormalized :: a -> Bool"}
    {:name "isNegativeZero"
     :signature "isNegativeZero :: a -> Bool"}
    {:name "isIEEE"
     :signature "isIEEE :: a -> Bool"}
    {:name "atan2"
     :signature "atan2 :: a -> a -> a"}
    {:name "properFraction"
     :signature "properFraction :: Integral b => a -> (b, a)"}
    {:name "truncate"
     :signature "truncate :: Integral b => a -> b"}
    {:name "round"
     :signature "round :: Integral b => a -> b"}
    {:name "ceiling"
     :signature "ceiling :: Integral b => a -> b"}
    {:name "floor"
     :signature "floor :: Integral b => a -> b"}
    {:name "<>"
     :signature "(<>) :: a -> a -> a"}
    {:name "sconcat"
     :signature "sconcat :: GHC.Base.NonEmpty a -> a"}
    {:name "stimes"
     :signature "stimes :: Integral b => b -> a -> a"}
    {:name "showsPrec"
     :signature "showsPrec :: Int -> a -> ShowS"}
    {:name "show"
     :signature "show :: a -> String"}
    {:name "showList"
     :signature "showList :: [a] -> ShowS"}
    {:name "traverse"
     :signature "traverse :: Applicative f => (a -> f b) -> t a -> f (t b)"}
    {:name "sequenceA"
     :signature "sequenceA :: Applicative f => t (f a) -> f (t a)"}
    {:name "mapM"
     :signature "mapM :: Monad m => (a -> m b) -> t a -> m (t b)"}
    {:name "sequence"
     :signature "sequence :: Monad m => t (m a) -> m (t a)"}
    {:name "^"
     :signature "(^) :: (Num a, Integral b) => a -> b -> a"}
    {:name "^^"
     :signature "(^^) :: (Fractional a, Integral b) => a -> b -> a"}
    {:name "all"
     :signature "all :: Foldable t => (a -> Bool) -> t a -> Bool"}
    {:name "and"
     :signature "and :: Foldable t => t Bool -> Bool"}
    {:name "any"
     :signature "any :: Foldable t => (a -> Bool) -> t a -> Bool"}
    {:name "appendFile"
     :signature "appendFile :: FilePath -> String -> IO ()"}
    {:name "asTypeOf"
     :signature "asTypeOf :: a -> a -> a"}
    {:name "break"
     :signature "break :: (a -> Bool) -> [a] -> ([a], [a])"}
    {:name "concat"
     :signature "concat :: Foldable t => t [a] -> [a]"}
    {:name "concatMap"
     :signature "concatMap :: Foldable t => (a -> [b]) -> t a -> [b]"}
    {:name "const"
     :signature "const :: a -> b -> a"}
    {:name "curry"
     :signature "curry :: ((a, b) -> c) -> a -> b -> c"}
    {:name "cycle"
     :signature "cycle :: GHC.Stack.Types.HasCallStack => [a] -> [a]"}
    {:name "drop"
     :signature "drop :: Int -> [a] -> [a]"}
    {:name "dropWhile"
     :signature "dropWhile :: (a -> Bool) -> [a] -> [a]"}
    {:name "either"
     :signature "either :: (a -> c) -> (b -> c) -> Either a b -> c"}
    {:name "error"
     :signature "error :: GHC.Stack.Types.HasCallStack => [Char] -> a"}
    {:name "errorWithoutStackTrace"
     :signature "errorWithoutStackTrace :: [Char] -> a"}
    {:name "even"
     :signature "even :: Integral a => a -> Bool"}
    {:name "filter"
     :signature "filter :: (a -> Bool) -> [a] -> [a]"}
    {:name "flip"
     :signature "flip :: (a -> b -> c) -> b -> a -> c"}
    {:name "fromIntegral"
     :signature "fromIntegral :: (Integral a, Num b) => a -> b"}
    {:name "fst"
     :signature "fst :: (a, b) -> a"}
    {:name "gcd"
     :signature "gcd :: Integral a => a -> a -> a"}
    {:name "getChar"
     :signature "getChar :: IO Char"}
    {:name "getContents"
     :signature "getContents :: IO String"}
    {:name "getLine"
     :signature "getLine :: IO String"}
    {:name "head"
     :signature "head :: GHC.Stack.Types.HasCallStack => [a] -> a"}
    {:name "id"
     :signature "id :: a -> a"}
    {:name "init"
     :signature "init :: GHC.Stack.Types.HasCallStack => [a] -> [a]"}
    {:name "interact"
     :signature "interact :: (String -> String) -> IO ()"}
    {:name "ioError"
     :signature "ioError :: IOError -> IO a"}
    {:name "iterate"
     :signature "iterate :: (a -> a) -> a -> [a]"}
    {:name "last"
     :signature "last :: GHC.Stack.Types.HasCallStack => [a] -> a"}
    {:name "lcm"
     :signature "lcm :: Integral a => a -> a -> a"}
    {:name "lex"
     :signature "lex :: ReadS String"}
    {:name "lines"
     :signature "lines :: String -> [String]"}
    {:name "lookup"
     :signature "lookup :: Eq a => a -> [(a, b)] -> Maybe b"}
    {:name "map"
     :signature "map :: (a -> b) -> [a] -> [b]"}
    {:name "mapM_"
     :signature "mapM_ :: (Foldable t, Monad m) => (a -> m b) -> t a -> m ()"}
    {:name "maybe"
     :signature "maybe :: b -> (a -> b) -> Maybe a -> b"}
    {:name "not"
     :signature "not :: Bool -> Bool"}
    {:name "notElem"
     :signature "notElem :: (Foldable t, Eq a) => a -> t a -> Bool"}
    {:name "odd"
     :signature "odd :: Integral a => a -> Bool"}
    {:name "or"
     :signature "or :: Foldable t => t Bool -> Bool"}
    {:name "otherwise"
     :signature "otherwise :: Bool"}
    {:name "print"
     :signature "print :: Show a => a -> IO ()"}
    {:name "putChar"
     :signature "putChar :: Char -> IO ()"}
    {:name "putStr"
     :signature "putStr :: String -> IO ()"}
    {:name "putStrLn"
     :signature "putStrLn :: String -> IO ()"}
    {:name "read"
     :signature "read :: Read a => String -> a"}
    {:name "readFile"
     :signature "readFile :: FilePath -> IO String"}
    {:name "readIO"
     :signature "readIO :: Read a => String -> IO a"}
    {:name "readLn"
     :signature "readLn :: Read a => IO a"}
    {:name "readParen"
     :signature "readParen :: Bool -> ReadS a -> ReadS a"}
    {:name "reads"
     :signature "reads :: Read a => ReadS a"}
    {:name "realToFrac"
     :signature "realToFrac :: (Real a, Fractional b) => a -> b"}
    {:name "repeat"
     :signature "repeat :: a -> [a]"}
    {:name "replicate"
     :signature "replicate :: Int -> a -> [a]"}
    {:name "reverse"
     :signature "reverse :: [a] -> [a]"}
    {:name "scanl"
     :signature "scanl :: (b -> a -> b) -> b -> [a] -> [b]"}
    {:name "scanl1"
     :signature "scanl1 :: (a -> a -> a) -> [a] -> [a]"}
    {:name "scanr"
     :signature "scanr :: (a -> b -> b) -> b -> [a] -> [b]"}
    {:name "scanr1"
     :signature "scanr1 :: (a -> a -> a) -> [a] -> [a]"}
    {:name "seq"
     :signature "seq :: a -> b -> b"}
    {:name "sequence_"
     :signature "sequence_ :: (Foldable t, Monad m) => t (m a) -> m ()"}
    {:name "showChar"
     :signature "showChar :: Char -> ShowS"}
    {:name "showParen"
     :signature "showParen :: Bool -> ShowS -> ShowS"}
    {:name "showString"
     :signature "showString :: String -> ShowS"}
    {:name "shows"
     :signature "shows :: Show a => a -> ShowS"}
    {:name "snd"
     :signature "snd :: (a, b) -> b"}
    {:name "span"
     :signature "span :: (a -> Bool) -> [a] -> ([a], [a])"}
    {:name "splitAt"
     :signature "splitAt :: Int -> [a] -> ([a], [a])"}
    {:name "subtract"
     :signature "subtract :: Num a => a -> a -> a"}
    {:name "tail"
     :signature "tail :: GHC.Stack.Types.HasCallStack => [a] -> [a]"}
    {:name "take"
     :signature "take :: Int -> [a] -> [a]"}
    {:name "takeWhile"
     :signature "takeWhile :: (a -> Bool) -> [a] -> [a]"}
    {:name "uncurry"
     :signature "uncurry :: (a -> b -> c) -> (a, b) -> c"}
    {:name "undefined"
     :signature "undefined :: GHC.Stack.Types.HasCallStack => a"}
    {:name "unlines"
     :signature "unlines :: [String] -> String"}
    {:name "until"
     :signature "until :: (a -> Bool) -> (a -> a) -> a -> a"}
    {:name "unwords"
     :signature "unwords :: [String] -> String"}
    {:name "unzip"
     :signature "unzip :: [(a, b)] -> ([a], [b])"}
    {:name "unzip3"
     :signature "unzip3 :: [(a, b, c)] -> ([a], [b], [c])"}
    {:name "userError"
     :signature "userError :: String -> IOError"}
    {:name "words"
     :signature "words :: String -> [String]"}
    {:name "writeFile"
     :signature "writeFile :: FilePath -> String -> IO ()"}
    {:name "zip"
     :signature "zip :: [a] -> [b] -> [(a, b)]"}
    {:name "zip3"
     :signature "zip3 :: [a] -> [b] -> [c] -> [(a, b, c)]"}
    {:name "zipWith"
     :signature "zipWith :: (a -> b -> c) -> [a] -> [b] -> [c]"}
    {:name "zipWith3"
     :signature "zipWith3 :: (a -> b -> c -> d) -> [a] -> [b] -> [c] -> [d]"}
    {:name "||"
     :signature "(||) :: Bool -> Bool -> Bool"}
  ])

(def +count+
  "Number of Prelude builtins catalogued."
  223)
