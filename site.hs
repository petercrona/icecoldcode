{-# LANGUAGE OverloadedStrings #-}

import Control.Monad (forM_)
import qualified Data.ByteString.Char8 as BS
import Data.Digest.Pure.MD5
  ( MD5Digest,
    hash',
  )
import Data.List (isSuffixOf)
import Hakyll
import System.FilePath
import Text.Pandoc
import Text.Pandoc.Highlighting

----------
-- Global vars

mySiteName :: String
mySiteName = "IceColdCode"

mySiteRoot :: String
mySiteRoot = "https://www.icecoldcode.com"

mainCss :: FilePath
mainCss = "css/default.css"

config :: Configuration
config =
  defaultConfiguration {providerDirectory = "src"}

codeHighlightStyle :: Style
codeHighlightStyle = tango

----------
-- Hakyll config

main :: IO ()
main = hakyllWith config $ do
  compiledStylesheetPath <- preprocess $ do
    css <- BS.readFile ("src/" <> mainCss)
    let h = hash' css :: MD5Digest
    pure $ dropExtension mainCss <> "_" <> show h <> ".css"

  let defaultPageCtx =
        constField "root" mySiteRoot
          <> constField "siteName" mySiteName
          <> constField "websiteUrlAsName" (mySiteName <> ".com")
          <> constField "cssPath" compiledStylesheetPath
          <> defaultContext

  forM_
    [ "robots.txt",
      "images/*",
      "js/*"
    ]
    $ \f -> match f $ do
      route idRoute
      compile copyFileCompiler

  create [fromFilePath compiledStylesheetPath] $ do
    route idRoute
    compile $ do
      css <- (fmap itemBody . load . fromFilePath $ mainCss)
      makeItem (css :: String)

  match "css/*" $ do
    route idRoute
    compile compressCssCompiler

  match "posts/*" $ do
    route $ cleanRoute
    compile $ do
      pandocCompilerCustom
        >>= saveSnapshot "content"
        >>= loadAndApplyTemplate "templates/post.html" defaultPageCtx
        >>= loadAndApplyTemplate "templates/default.html" defaultPageCtx
        >>= cleanIndexUrls

  match "index.html" $ do
    route idRoute
    compile $ do
      posts <- recentFirst =<< loadAllSnapshots "posts/*" "content"

      let postsCtx = listField "posts" defaultPageCtx (return posts)

      getResourceBody
        >>= applyAsTemplate (defaultPageCtx <> postsCtx)
        >>= loadAndApplyTemplate "templates/default.html" (defaultPageCtx)
        >>= cleanIndexUrls

  match "about.html" $ do
    route cleanRoute
    compile $ do
      getResourceBody
        >>= applyAsTemplate (defaultPageCtx)
        >>= loadAndApplyTemplate "templates/default.html" (defaultPageCtx)
        >>= cleanIndexUrls

  match "impressum.md" $ do
    route cleanRoute
    compile $ do
      getResourceBody
        >>= applyAsTemplate (defaultPageCtx)
        >>= renderPandoc
        >>= applyAsTemplate (defaultPageCtx)
        >>= loadAndApplyTemplate "templates/default.html" (defaultPageCtx)
        >>= cleanIndexUrls

  makePatternDependency "css/*" >>= \dependency ->
    rulesExtraDependencies [dependency] $ do
      match "templates/*" $
        compile templateBodyCompiler

  create ["css/code.css"] $ do
    route idRoute
    compile
      ( makeItem
          . compressCss
          . styleToCss
          $ codeHighlightStyle
      )

----------
-- Pandoc config

pandocCompilerCustom :: Compiler (Item String)
pandocCompilerCustom =
  pandocCompilerWith pandocReaderOpts pandocWriterOpts

pandocReaderOpts :: ReaderOptions
pandocReaderOpts =
  defaultHakyllReaderOptions
    { readerExtensions = pandocExtensionsCustom
    }

pandocWriterOpts :: WriterOptions
pandocWriterOpts =
  defaultHakyllWriterOptions
    { writerExtensions = pandocExtensionsCustom,
      writerHighlightStyle = Just codeHighlightStyle,
      writerHTMLMathMethod = MathJax ""
    }

pandocExtensionsCustom :: Extensions
pandocExtensionsCustom =
  githubMarkdownExtensions
    <> extensionsFromList
      [ Ext_fenced_code_attributes,
        Ext_gfm_auto_identifiers,
        Ext_implicit_header_references,
        Ext_smart,
        Ext_footnotes,
        Ext_tex_math_dollars,
        Ext_tex_math_double_backslash,
        Ext_latex_macros,
        Ext_raw_tex
      ]

----------
-- Routing utilities

cleanRoute :: Routes
cleanRoute = customRoute createIndexRoute
  where
    createIndexRoute ident =
      takeDirectory p
        </> takeBaseName p
        </> "index.html"
      where
        p = toFilePath ident

cleanIndexUrls :: Item String -> Compiler (Item String)
cleanIndexUrls = return . fmap (withUrls cleanIndex)

cleanIndex :: String -> String
cleanIndex url
  | idx `isSuffixOf` url = take (length url - length idx) url
  | otherwise = url
  where
    idx = "index.html"
