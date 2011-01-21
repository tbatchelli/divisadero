(ns divisadero.view-resolver
  (:require [clojure.contrib.logging :as log]))

(defmulti render-view :type)

(defmethod render-view :fn [view resp]
  (log/debug (format "rendering function for %s" view))
  (let [template (:template view)]
    (assoc resp :body (apply str (template resp)))))

(defmethod render-view :static [view resp]
  (log/debug (format "rendering static page for %s" view))
  (let [url (:url view)]
    (assoc resp :body url)))


(defn wrap-view-resolver [app views]
  (fn [req]
    (log/info "entered view resolver")
    (let [resp (app req)]
      (log/info
       (format "rest of the stack executed. Checking for a view in " resp))
      (if-let [view (:view resp)]
        (do
          (log/info (format "View found %s" view))
          (if (keyword? view)
            (do (log/info (format "selecting keyword rendering for %s" view))
                (render-view (view views) resp))
            (do (log/info (format "selecting map-based rendering for %s" view))
                (render-view view resp))))
        ;; no view defined. do nothing.
        resp))))
