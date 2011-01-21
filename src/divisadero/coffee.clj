(ns divisadero.coffee
  "A ring wrapper for coffeescript files to be used during development. It
will identify when a .js file is requested that has a coffeescript counterpart,
and if the coffeescript version is newer than the javascript version, it will
compile the coffeescript one.

Usage (wrap-coffee app uri-path coffee-sources-path js-destination-path)

e.g: (wrap-coffee app \"/js/\" \"src/coffee/\" \"resources/public/js/\")
will intercept all urls of the type \"http://host:port/js/my-script.js, look
for \"<project>/src/coffee/my-script.coffee\", and if it is newer, compile it
and place the complied js file into \"<project>/resources/public/js/\".

It works with subdirectories.

This wrapper will forward the request to the next middleware.

To make this wrapper work correctly, it is assumed that the directory where
the compiled files will be placed is correctly mapped to the path where the
client was looking for the compiled file
(e.g: /js/ -> <project>/resources/public/js/)"
  (:require [cljs.compiler :as compiler]
            [cljs.rhino :as rhino]
            [clojure.contrib.logging :as log]
            [clojure.java.io :as io]
            [clout.core :as clout]))

(defn build-script-matcher
  "Builds a matcher that will get the relative path name of a .js file
  identified by a uri that starts with the path specified by 'path'"
  [^String path]
  (clout/route-compile (str path "*.js")))

(defn get-script-path
  "Gets the path to the .js file in the uri of the request, if there
  is one. To be used with a matcher built by 'build-script-matcher'"
  [matcher request]
  (get (clout/route-matches matcher request)
       "*"))

(defonce compiler (compiler/build-compiler))

(defn make-dir-for-file
  "Creates the missing directory path to a file.

e.g. (make-dir-for-file
          (clojure.java.io/file
                 '/tmp/a/b/c/d/e/file.txt')

will create the all the directories in the path '/tmp/a/b/c/d/e'."
  [^java.io.File f]
  (.mkdirs (io/file (.getParent f))))

(defn compile-coffee-file [coffee-file js-file]
  (let [coffee-code (slurp coffee-file)
        js-code (compiler/compile-string compiler coffee-code)]
    (make-dir-for-file js-file) ;; make sure it can be saved
    (spit js-file js-code)))

(defn wrap-coffee [app uri-path coffee-sources-path js-destination-path]
  ;; todo: check that the destination and origin directories exist
  (let [matcher (build-script-matcher uri-path)]
    (fn [request]
      (when-let [script-path (get-script-path matcher request)]
        (let [coffee-file (io/file
                           coffee-sources-path
                           (str script-path ".coffee"))
              js-file (io/file
                       js-destination-path
                       (str script-path ".js"))]
          (when (and (.exists coffee-file)
                     (or (not (.exists js-file))
                         (> (.lastModified coffee-file)
                            (.lastModified js-file))))
            (log/info (format "Compiling file %s to %s"
                              (.getAbsolutePath coffee-file)
                              (.getAbsolutePath js-file)))
            (compile-coffee-file coffee-file js-file))))
      (app request))))
