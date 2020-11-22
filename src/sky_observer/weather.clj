(ns sky-observer.weather
  (:require [cheshire.core :refer [parse-string generate-string]]
            [org.httpkit.client :as client]
            [org.httpkit.sni-client :as sni-client]
            [sky-observer.file-worker :as file-worker]))


(alter-var-root #'org.httpkit.client/*default-client* (fn [_] sni-client/default-client))

(defn get-historic-weather [lat, lon, start-date, end-date]
  (let [{url     :hourly-weather-url
         api-key :key
         method  :method} (file-worker/get-endpoint :weather)]

    (:body @(client/request {
                             :url          url
                             :method       (keyword method)
                             :headers      {
                                            "x-api-key" api-key
                                            }
                             :query-params {
                                            "lat"   lat
                                            "lon"   lon
                                            "start" start-date
                                            "end"   end-date
                                            }
                             }))))

(defn weather-condition [lat, lon, start-date]
  ;(str(:coco (get (:data (parse-string
  ;              (get-historic-weather lat, lon, start-date, start-date) true)
  ;       ) 3)))

  (:data (parse-string
           (get-historic-weather lat, lon, start-date, start-date) true))

  )
