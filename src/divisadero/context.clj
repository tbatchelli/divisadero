(ns divisadero.context
  "Handles the operations on the application context")

(def *context*
  {:user-db {:url "http://127.0.0.1:5984/om-user"}
   :environment :dev})

(defn get [key-seq]
  (get-in *context* key-seq))

(defn set! [key-seq value]
  (alter-var-root #'*context* assoc-in key-seq value))
