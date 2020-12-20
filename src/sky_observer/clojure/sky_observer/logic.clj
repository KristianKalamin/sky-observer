(ns sky-observer.logic
  (:require [sky-observer.api-call :refer [weather-condition
                                           find-location
                                           find-location-with-coordinates]]
            [sky-observer.file-worker :as file-worker]
            [clojure.string :as str]
            [cheshire.core :refer [generate-string]]
            [clojure.set :refer [union]]
            [clojure.core.async :refer [thread <!!]]
            [sky-observer.db :refer [save-search get-within-radius]])
  (:import (sky_observer.space VisibilityCheck)
           (java.time LocalDateTime)))

(def ^:private visibility-check (VisibilityCheck. (file-worker/get-orekit-data)))
(def ^:private satellite-list (doall (file-worker/load-satellites)))

(defn ^:private propagate [date time lat lon satellites sky-visibility]
  (thread
    (map (fn [obj]
           (dissoc obj :class))
         (filter (fn [x]
                   (some? (:startFlybyTime x)))             ; not null
                 (map (fn [satellite]
                        (bean (.propagate visibility-check
                                          (str date "T" time) ;"2020-11-20T23:00:00"
                                          sky-visibility
                                          lat
                                          lon
                                          (get satellite :satellite-name)
                                          (get satellite :line1)
                                          (get satellite :line2))))
                      satellites))
         )))

(defn ^:private get-coco [lat lon date time]
  (:coco (first (filter (fn [hour-weather]
                          (= (subs (get (str/split (get hour-weather :time) #" ") 1) 0 2) (subs time 0 2)))
                        (weather-condition lat lon date)))))

(defn ^:private get-visible-flyby [date time lat lon sky-visibility]
  (generate-string (distinct (reduce concat (map (fn [thread-result]
                                                   (<!! thread-result))
                                                 (map (fn [s]
                                                        (propagate date time lat lon s sky-visibility))
                                                      (partition-all (int (/ (count satellite-list) 4)) satellite-list)))))))

(defn ^:private check-date-time [date time]
  (try
    (LocalDateTime/parse (str date "T" time))
    :true
    (catch Exception e
      (prn e)
      :false)))

(defn search [location lat lon date time]
  (cond (= lat "") :false
        (= lon "") :false
        (= (check-date-time date time) :false) :false
        :default

        (let [num-lat (Double/parseDouble (str lat))
              num-lon (Double/parseDouble (str lon))]

          (let [weather-condition-code (get-coco num-lat num-lon date time)]
            (save-search location num-lat num-lon date time)
            (cond
              (< weather-condition-code 3) (get-visible-flyby date time num-lat num-lon 0)
              (= weather-condition-code 3) (get-visible-flyby date time num-lat num-lon 60)
              :default (get-visible-flyby date time num-lat num-lon 100))
            ))))

(defn locations [params]
  (generate-string (find-location params)))

(defn coordinate-location [lat lon]
  (generate-string (find-location-with-coordinates lat lon)))

(defn ^:private get-lat-lon [data]
  (hash-map
    :lon ((get (get data :loc) :coordinates) 0)
    :lat ((get (get data :loc) :coordinates) 1)))

(defn find-historic-searches [lat lon]
  (generate-string (map get-lat-lon
                        (get-within-radius (Double/parseDouble lat)
                                           (Double/parseDouble lon)))))