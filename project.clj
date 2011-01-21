(defproject divisadero "0.0.1-SNAPSHOT"
  :description "A Web library for Clojure/Ring/Compojure/etc..."
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [compojure "0.6.0-RC1"]
                 [ring/ring-jetty-adapter "0.3.5"]
                 ;; json for in :params of request
                 [ring-json-params "0.1.3"]
                 [enlive "1.0.0-SNAPSHOT"]
                 [cljs "1.0.0-SNAPSHOT"]
                 [com.ashafa/clutch "0.2.3-SNAPSHOT"]]
  :dev-dependencies [[swank-clojure/swank-clojure "1.2.1"]
                     [marginalia "0.3.2"]
                     [lein-ring "0.2.4"]
                     [ring-serve "0.1.0"]
                     [faker "0.2.1"]])
