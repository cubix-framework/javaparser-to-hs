{-# LANGUAGE StandaloneDeriving #-}

import Control.Monad ( liftM )

import Language.Java.Syntax
import Language.Java.Pretty
import Language.Java.Lexer

import System.Environment
import System.Exit
import System.IO
import System.IO.Temp
import System.Process

deriving instance Read CompilationUnit
deriving instance Read PackageDecl
deriving instance Read ImportDecl
deriving instance Read TypeDecl
deriving instance Read ClassDecl
deriving instance Read ClassBody
deriving instance Read EnumBody
deriving instance Read EnumConstant
deriving instance Read InterfaceDecl
deriving instance Read InterfaceBody
deriving instance Read Decl
deriving instance Read MemberDecl
deriving instance Read VarDecl
deriving instance Read VarDeclId
deriving instance Read VarInit
deriving instance Read FormalParam
deriving instance Read MethodBody
deriving instance Read ConstructorBody
deriving instance Read ExplConstrInv
deriving instance Read Modifier
deriving instance Read Annotation
deriving instance Read ElementValue
deriving instance Read Block
deriving instance Read BlockStmt
deriving instance Read Stmt
deriving instance Read Catch
deriving instance Read SwitchBlock
deriving instance Read SwitchLabel
deriving instance Read ForInit
deriving instance Read Exp
deriving instance Read Literal
deriving instance Read Op
deriving instance Read AssignOp
deriving instance Read Lhs
deriving instance Read ArrayIndex
deriving instance Read FieldAccess
deriving instance Read MethodInvocation
deriving instance Read ArrayInit
deriving instance Read Type
deriving instance Read RefType
deriving instance Read ClassType
deriving instance Read TypeArgument
deriving instance Read WildcardBound
deriving instance Read PrimType
deriving instance Read TypeParam
deriving instance Read Ident
deriving instance Read Name



parse :: FilePath -> IO (Either String CompilationUnit)
parse path = withSystemTempFile "parse" $ \tmp h -> do
                         hClose h
                         exitCode <- system $ "java -jar javaparser-to-hs.jar " ++ (show path) ++ " " ++ (show tmp)
                         case exitCode of
                           ExitFailure _ -> return $ Left "parse failed"
                           ExitSuccess   -> liftM (Right . read) $ readFile tmp

{-
Parsing can rearrange modifiers, pretty-printing can insert parens, annotations are dropped. What do we do? Remove them!
-}
lexicallyEqual :: [Token] -> [Token] -> Bool
lexicallyEqual xs ys = let xs' = [x | x <- removeAnno xs, not (x `elem` unstableTokens)]
                           ys' = [y | y <- removeAnno ys, not (y `elem` unstableTokens)] in
                       xs' == ys'
  where
    removeAnno ts = rAnn False 0 ts

    rAnn True 0 ts                = rAnn False 0 ts
    rAnn True n (CloseParen : ts) = rAnn True (n-1) ts
    rAnn True n (OpenParen : ts)  = rAnn True (n+1) ts
    rAnn True n (t:ts)            = rAnn True n ts

    rAnn False n (Op_AtSign : OpenParen : ts) = rAnn True 1 ts
    rAnn False n (Op_AtSign : t : ts)         = rAnn False n ts
    rAnn False n (t : ts)                     = t : rAnn False n ts
    rAnn False 0 []                           = []

    unstableTokens = [ OpenParen
                      , CloseParen
                      , KW_Public
                      , KW_Private
                      , KW_Protected
                      , KW_Abstract
                      , KW_Final
                      , KW_Static
                      , KW_Strictfp
                      , KW_Transient
                      , KW_Volatile
                      , KW_Native
                      , KW_Synchronized
                      ]


unL :: L a -> a
unL (L _ x) = x

main :: IO ()
main = do fil <- liftM head $ getArgs
          origStream <- liftM (map unL.lexer) $ readFile fil
          res <- parse fil
          case res of
               Left _     -> exitFailure
               Right tree -> let reread = map unL $ lexer $ show $ pretty tree in
                             if lexicallyEqual reread origStream then
                               return ()
                             else
                               do putStrLn "Different!"
                                  exitFailure