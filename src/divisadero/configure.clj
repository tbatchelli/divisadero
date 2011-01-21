(ns divisadero.configure
  "Functions that configure the application"
  (:use [clojure.pprint :only (pprint)])
  (:require [divisadero.context :as context]
            [com.ashafa.clutch :as clutch]
            [clojure.contrib.logging :as log]
            [divisadero.couchdb :as couchdb]))


(def configuration (atom {:reconfigure true}))

(defn reset-configuration
  "A holder of the application configuration at runtime.
be reconfigured with the current divisadero.context/*context* values "
  []
  (swap! configuration (fn [conf] (assoc conf :reconfigure true))))

(defn- pprint-str [o]
  (with-out-str
    (pprint o)))

(defn wrap-config
  "A RING wrapper that will configure the system based on the values in
divisadero.context/*context*"
  [app]
  (fn [req]
    (when (:reconfigure @configuration)
      (swap! configuration
             (fn [conf]
               (when (:reconfigure conf)
                 (log/info (format
                            "Reconfiguring system with:\n%s"
                            (pprint-str context/*context*))))
               ;; build the new configuration
               (-> conf
                   (couchdb/configure-couchdb)
                   (assoc :reconfigure false))))
      (log/info (format "Current config \n%s"
                        (pprint-str @configuration))))
    (app req)))
