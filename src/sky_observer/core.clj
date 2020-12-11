(ns sky-observer.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [org.httpkit.server :refer [run-server]]
            [ring.middleware.json :refer [wrap-json-body]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [cheshire.core :refer [generate-string]]
            [sky-observer.logic :as logic]))

(defroutes app-routes
           (GET "/" [] "Home")
           (POST "/locations" {body :body} (logic/locations body))
           (GET "/search" [location date time lat lon]
             (logic/search location (Double/parseDouble (str lat)) (Double/parseDouble (str lon)) date time))

           (route/not-found "Not Found"))
(def app
  (wrap-json-body app-routes {:keywords? true :bigdecimals? false}))

(defn -main
  []
  (run-server (wrap-defaults app api-defaults) {:port 882}))