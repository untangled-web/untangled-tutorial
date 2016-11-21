(ns untangled-devguide.L-Internationalization
  (:require-macros [cljs.test :refer [is]]
                   [untangled-devguide.tutmacros :refer [untangled-app]])
  (:require [devcards.core :as dc :include-macros true :refer-macros [defcard defcard-doc dom-node]]
            [untangled.i18n :refer [tr trc trf]]
            yahoo.intl-messageformat-with-locales
            [untangled.client.cards :refer [untangled-app]]
            [untangled.client.core :as uc]
            [cljs.reader :as r]
            [om.next.impl.parser :as p]
            [om.dom :as dom]
            [untangled.i18n.core :as ic]
            app.i18n.de
            app.i18n.es
            [om.next :as om :refer [defui]]
            [untangled.client.mutations :as m]))

;; FIXME: the i18n stuff needs work:
;; 1. The module loading should be something that can be triggered automatically from changing locales
;; 2. The lein plugin is all confused: asks you to put the module configs in the wrong place, doesn't give good error
;;    messages or help, dies on NPE if configs are wrong, etc.

(reset! ic/*loaded-translations* {"es" {"|This is a test" "Spanish for 'this is a test'"
                                        "|Hi, {name}"     "Ola, {name}"}})

(defn locale-switcher [comp]
  (dom/div nil
    (dom/button #js {:onClick #(om/transact! comp '[(ui/change-locale {:lang "en"}) :ui/locale])} "en")
    (dom/button #js {:onClick #(om/transact! comp '[(ui/change-locale {:lang "es"}) :ui/locale])} "es")))

(defui Test
  static om/IQuery
  (query [this] [:ui/react-key :ui/locale])
  Object
  (render [this]
    (let [{:keys [ui/react-key ui/locale]} (om/props this)]
      (dom/div #js {:key react-key}
        (locale-switcher this)
        (dom/span nil "Locale: " locale)
        (dom/br nil)
        (tr "This is a test")))))

(defcard-doc
  "# Internationalization

  Untangled combines together a few tools and libraries to give a consistent and easy-to-use method of internationalizing
  you application. The approach includes the following features:

  - A global setting for the *current* application locale.
  - The ability to write UI strings in a default language/locale in the code. These are the defaults that are shown on
  the UI if no translation is available.
  - The ability to extract these UI strings for translation in POT files (GNU gettext-style)
      - The translator can use tools like POEdit to generate translation files
  - The ability to convert the translation files into Clojurescript as modules that can be dynamically loaded when
    the locale is changed.
  - The ability to format messages (using Yahoo's formatJS library), including very configurable plural support

  Untangled leverages the GNU `xgettext` tool, and Yahoo's formatJS library (which in turn leverages browser support
  for locales) to do most of the heavy lifting.

  ## Annotating Strings

  The simplest thing to do is marking the UI strings that need translation. This is done with the `tr` macro:

  ```
  (namespace boo
    (:require [untangled.i18n :refer [tr]]))

  ...
  (tr \"This is a test\")
  ...
  ```

  By default (you have not created any translations) a call to `tr` will simply return the parameter unaltered.
  ")

(defcard sample-translation
  "This card shows the output of a call to `tr`. Note that this page has translations for the string, so if you
  change the locale this string will change."
  (untangled-app Test))

(defcard-doc "
  ## Changing the Locale

  The locale of the current browser tab can be changed through the built-in mutation `ui/change-locale` with a `:lang`
  parameter (which can use the ISO standard two-letter language with an optional country code):

  ```
  (om/transact! reconciler '[(ui/change-locale {:lang \"en-US\"})])
  ```

  The rendering functions will search for a translation in that language and country, fall back to the language if
  no country-specific entries exist, and will fall back to the default language (the one in the UI code) if no
  translation is found.

  ## Resolving Ambiguity

  It is quite common for a string to be ambiguous to the developer or translator. For example the abbreviation for 'male'
  might be 'M' in English, but the letter itself doesn't provide enough information for a translator to know what they
  are doing. A call to `trc` (translate with context) resolves this by including a context string as part of the
  internal lookup key and as a comment to the translator:

  ```
  (trc \"Abbreviation for male\" \"M\")
  ```

  ## Formatting

  `trf` (translate with format) accepts a format string (see the Yahoo FormatJS library) and any additional arguments
  that should be placed in the string. This function handles formatting of numbers, percentages, plurals, etc.

  Any named parameter that you use in the format string must have a corresponding named parameter in the call:

  ```
  (trf \"{a}, {b}, and {c}\" :a 1 :b 2 :c 3) ; => 1, 2, and 3
  ```

  If the input parameter is needs to be further localized you may include a variety of formatting types (which
  are extensible):

  ```
  (trf \"N: {n, number} ({m, date, long})\" :n 10229 :m (new js/Date))
  ```

  See the formatJS documentation for further details.
")

(defui Format
  static uc/InitialAppState
  (initial-state [clz p] {:ui/label "Your Name"})
  static om/Ident
  (ident [this props] [:components :ui])
  static om/IQuery
  (query [this] [:ui/label])
  Object
  (render [this]
    (let [{:keys [ui/label]} (om/props this)]
      (dom/div nil
        (locale-switcher this)
        (js/console.log :l label)
        (dom/input #js {:value label :onChange #(m/set-string! this :ui/label :event %)})
        (trf "Hi, {name}" :name label)
        (dom/br nil)
        (trf "N: {n, number} ({m, date, long})" :n 10229 :m (new js/Date))
        (dom/br nil)))))

(def ui-format (om/factory Format))

(defui Root2
  static uc/InitialAppState
  (initial-state [clz p] {:format (uc/initial-state Format {})})
  static om/IQuery
  (query [this] [:ui/react-key :ui/locale {:format (om/get-query Format)}])
  Object
  (render [this]
    (let [{:keys [ui/react-key ui/locale format]} (om/props this)]
      (dom/div #js {:key react-key}
        (dom/span nil "Locale: " locale)
        (dom/br nil)
        (ui-format format)))))


(defcard formatted-examples
  "This card shows the results of some formatted and translated strings"
  (untangled-app Root2))

(defcard-doc
  "
  ## Extracting Strings for Translation

  String extraction is done via the following process:

  1. The application is compiled using `:whitespace` optimization. This provides a single Javascript file. The GNU
  utility xgettext can then be used to extract the strings. You can use something like Home Brew to install this
  utility.

  ```
  xgettext --from-code=UTF-8 --debug -k -ktr:1 -ktrc:1c,2 -ktrf:1 -o messages.pot compiled.js
  ```

  A leiningen plugin exists for doing this step for you:

  ```
  (defproject boo \"0.1.0-SNAPSHOT\"
    ...
    :plugins [navis/untangled-lein-i18n \"0.1.2\"]

    :untangled-i18n {:default-locale        \"en\" ;; the default locale of your app
                     :translation-namespace \"app.i18n\" ;; the namespace for generating cljs translations
                     :source-folder         \"src\" ;; the target source folder for generated code
                     :target-build          \"i18n\" ;; the build that will create i18n/out/compiled.js
                     }

    :cljsbuild {:builds [{:id           \"i18n\"
                          :source-paths [\"src\"]
                          :compiler     {:output-to     \"i18n/out/compiled.js\"
                                         ; BUG: At the moment you must put modules here, even though they are not used in this step
                                         :modules       {}
                                         :main          entry-point
                                         :optimizations :whitespace}}]})
  ```

  ```
  lein i18n extract-strings
  ```

  should generate the file `i18n/msgs/messages.pot`.

  ## Translating Strings

  Once you've extracted the strings to a POT file you may translate them as you would any other gettext app. We
  recommend the GUI program POEdit. You should end up with a number of translations in files that you place
  in files like `i18n/msgs/es.po`, where `es` is the locale of the translation.

  ## Generating Clojurescript Versions of the Translations

  There are three ways of generating the cljs code for translations: manual, as code included in your main application,
  and as Closure modules. The lein i18n plugin will generate modules that are compatible with dynamic loading in the
  Google Closure environment (which requires a bit of extra manual work).

  The generation of the `cljs` files uses the `:source-folder` and `:translation-namespace` settings in the
  `:untangled-i18n` section of your project to determine where to put the results:

  ```
  lein i18n deploy-translations
  ```

  ### Including the translations into your final application

  This is the simplest approach. You need only `require` the necessary translations from your main entry point file to
  ensure that they are included.

  ### Translations During Development

  You should specifically require all translation namespaces in a `user` namespace (which is only compiled in dev mode)
  in order to make the translations available during development. The module loading does not work in dev mode. You
  can avoid the js console error messages (which are harmless) by setting `js/i18nDevMode` to `true`.

  ### Compiling the Translations into Modules (ALPHA QUALITY SUPPORT)

  In order for this to work you should do the following things:

  1. Read and understand the module loading documentation of Closure
  2. Configure the following modules for your application (with optimizations): One for each locale, and one module for
  your main. Your main should require the generated `default-locale` namespace (but not any of the others).
  3. Use at least whitespace optimizations.
  4. When you change locales, you need to trigger the module load. This looks something like this:

  ```
  (ns app.i18n.locales
    (:require
      goog.module
      goog.module.ModuleLoader
      [goog.module.ModuleManager :as module-manager]
      [untangled.i18n.core :as i18n]
      untangled-todomvc.i18n.en-US
      untangled-todomvc.i18n.es-MX)
    (:import goog.module.ModuleManager))

  (defonce manager (module-manager/getInstance))

  (defonce modules #js {\"es\" \"/es.js\", \"de\" \"/de.js\"})

  (defonce module-info #js {\"es\" [], \"de\" []})

  (defonce ^:export loader (let [loader (goog.module.ModuleLoader.)]
                             (.setLoader manager loader)
                             (.setAllModuleInfo manager module-info)
                             (.setModuleUris manager modules) loader))

  (defn ^:export set-locale [l]
    (.execOnLoad manager l #(reset! i18n/*current-locale* l)))
  ```

  and have your UI code call the `set-locale` function defined above to both trigger the module load and change the UI
  locale.

  Your module config in your production build should look something like this (untested):

  ```
    :cljsbuild {:builds [{:id           \"production\"
                          :source-paths [\"src\"]
                          :compiler     {:output-to     \"resources/public/main.js\"
                                         :modules       {:de   {:output-to \"resource/public/de.js\", :entries #{\"app.i18n.de\"}},
                                                         :es   {:output-to \"resource/public/es.js\", :entries #{\"app.i18n.es\"}},
                                                         :main {:output-to \"resource/public/main.js\",
                                                                :entries   #{\"app.core\"}}}
                                         :optimizations :simple}}]})
  ```
  ")


