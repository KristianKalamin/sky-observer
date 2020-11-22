(ns sky-observer.logic
  (:require [sky-observer.weather :refer [weather-condition]]
            [sky-observer.file-worker :as file-worker])
  (:import (sky_observer.space VisibilityCheck)))

(def visibility-check (VisibilityCheck.))

(defn propagate [date time lat lon line1 line2 satellite-name]

  (let [space-object (.propagate visibility-check
                                 (file-worker/get-orekit-data)
                                 (str date "T" time)        ;"2020-11-20T23:00:00"
                                 lat
                                 lon
                                 satellite-name
                                 line1
                                 line2)]

    ;  (println (.getSatelliteName space-object))
    ; (filter #(= (:time %) (str date " " time)) (weather-condition lat lon date))

    ))


(defn search [lat lon date time]
  (map (fn [satellite]
         (propagate date time lat lon (get satellite :line1) (get satellite :line2) (get satellite :satellite-name)))
       (file-worker/load-satellites)))


