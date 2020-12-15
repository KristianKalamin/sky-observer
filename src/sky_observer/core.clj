(ns sky-observer.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [org.httpkit.server :refer [run-server]]
            [ring.middleware.json :refer [wrap-json-body]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [cheshire.core :refer [generate-string]]
            [sky-observer.logic :as logic]))

(defroutes app-routes
           (POST "/locations" {body :body} (logic/locations (:location body)))
           (POST "/location" {body :body} (logic/coordinate-location (:lat body) (:lon body)))
           (POST "/historic-searches" {body :body} (logic/find-historic-searches (:lat body) (:lon body)))
           (GET "/search" [location date time lat lon]
             (logic/search location (Double/parseDouble (str lat)) (Double/parseDouble (str lon)) date time))

           (route/not-found "Not Found"))
(def app
  (-> (wrap-json-body app-routes {:keywords? true :bigdecimals? false})
      (wrap-cors
        :access-control-allow-origin [#".*"]
        :access-control-allow-credentials true
        :access-control-allow-headers ["Content-Type"]
        :access-control-allow-methods [:get :put :post :delete])))

(defn -main
  []
  (run-server (wrap-defaults app api-defaults) {:port 882})
  (println "Server is running"))