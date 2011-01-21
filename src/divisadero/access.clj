(ns divisadero.access
  (:require [clojure.contrib.logging :as log]
            [clojure.set :as set]))

(def sample-access-map
  [#"/admin/login.*" #{:any}
   #"/admin/.*" #{:admin}
   #".*" #{:any}])

(defn required-roles [access-map url]
  (let [access-pairs (partition 2 access-map)]
    (some
     #(when (re-matches (first %) url)
        (second %))
     access-pairs)))

(defn wrap-access-control [app access-map auth-fail-redirect]
  (let [roles-for-url (partial required-roles access-map)]
    (fn [req]
      (log/info (format "auth: user req %s" req))
      (let [user-roles (set/union (set (-> req :session :roles))
                                  #{:any}) ;; all users have the :any role
            uri (:uri req)
            authorized-roles (roles-for-url uri)
            authorized? (some authorized-roles user-roles)]
        (log/info (format "%s requires %s, user has %s -> %s"
                           uri authorized-roles user-roles
                           (if authorized? "AUTHORIZED" "DENIED")))
        (if authorized?
          ;; role match -> continue
          (app req)
          ;; no role match for this url -> redirect
          auth-fail-redirect)))))

(comment
  (def my-wrap (wrap-access-control println sample-access-map {:redirect}))
  (my-wrap {:uri "/admin/hello" :session {}})
  ;; -> nil
  (my-wrap {:uri "/admin/hello" :session {:auth {:roles [:admin]}}})
  ;;-> {:uri /admin/hello, :session {:auth {:roles [:admin]}}}
  ;;-> nil
  )