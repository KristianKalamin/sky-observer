(ns sky-observer.api-call
  (:require [cheshire.core :refer [parse-string]]
            [org.httpkit.client :as client]
            [org.httpkit.sni-client :as sni-client]
            [sky-observer.file-worker :as file-worker]))


(alter-var-root #'org.httpkit.client/*default-client* (fn [_] sni-client/default-client))

(defn ^:private get-historic-weather [lat, lon, start-date, end-date]
  (let [{url     :hourly-weather-url
         api-key :key
         method  :method} (file-worker/get-endpoint :weather)]

    (:body @(client/request {:url          url
                             :method       (keyword method)
                             :headers      {"x-api-key" api-key}
                             :query-params {"lat"   lat
                                            "lon"   lon
                                            "start" start-date
                                            "end"   end-date}}))))

(defn weather-condition [lat, lon, start-date]
  (:data (parse-string
           (get-historic-weather lat, lon, start-date, start-date) true)))

(defn ^:private get-location [request-params]
  (parse-string (:body @(client/request request-params)) true))

(defn find-location [location]
  (let [{url    :search-url
         method :method} (file-worker/get-endpoint :map-search)]

    (map (fn [loc] {:lat   (get loc :lat)
                    :lon   (get loc :lon)
                    :place (get loc :display_name)})
         (get-location {:url          (str url (clojure.string/replace location #" " "%20"))
                        :method       (keyword method)
                        :query-params {"format" "json"}}))))

(defn find-location-with-coordinates [lat-param lon-param]
  (let [{url    :search-url
         method :method} (file-worker/get-endpoint :map-search-coordinates)]
    (let [{lat          :lat
           lon          :lon
           display-name :display_name}
          (get-location {:url          url
                         :method       (keyword method)
                         :query-params {"format" "jsonv2"
                                        "lat"    lat-param
                                        "lon"    lon-param}})]
      {:lat   lat
       :lon   lon
       :place display-name})))
