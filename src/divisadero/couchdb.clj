(ns divisadero.couchdb
  (:use com.ashafa.clutch)
  (:require [divisadero.context :as context]
            [divisadero.configure :as configure]
            [clojure.contrib.logging :as log]))

(defn configure-couchdb
  "Creates the various needed couchdb databases and puts them into the context"
  [conf]
  (let [db-info (context/get [:user-db :url])
        user-db (get-database db-info)]
    (assoc-in conf [:user-db :db] user-db)))
