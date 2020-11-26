(ns sky-observer.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
    ; [ring.adapter.jetty :refer [run-jetty]]
            [org.httpkit.server :refer [run-server]]
            [sky-observer.sightings :as sightings]
            [ring.middleware.json :refer [wrap-json-body]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [cheshire.core :refer [generate-string]]
            [sky-observer.logic :as logic]))

(defroutes app-routes
           (GET "/" [] (generate-string (sightings/select-all)))
           (GET "/search" [date time lat lon] (generate-string (logic/search (Double/parseDouble (str lat)) (Double/parseDouble (str lon)) date time)))
           (POST "/insert" {body :body}
             (sightings/insert body))
           (PUT "/update/:id" {id   :params
                               body :body}
             (sightings/update (get id :id) body))
           (DELETE "/remove/:id" [id] (sightings/delete id))
           (route/not-found "Not Found"))
(def app
  (wrap-json-body app-routes {:keywords? true :bigdecimals? false}))

(defn -main
  []
  (run-server (wrap-defaults app api-defaults) {:port 882}))