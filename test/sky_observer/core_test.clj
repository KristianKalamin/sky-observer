(ns sky-observer.core-test
  (:use midje.sweet)
  (:require [sky-observer.core :refer :all]
            [ring.mock.request :as mock]
            [cheshire.core :refer [parse-string]]))

(fact "locations endpoint test"
      (app (-> (mock/request :post "/locations")
               (mock/json-body {:location "Beograd"})))
      => (fn [res] (:body res))
      => (fn [body] (> (count body)) 0))

(fact "locations endpoint test-2"
      (app (-> (mock/request :post "/locations")
               (mock/json-body {:location ""})))
      => empty)

(fact "location endpoint test"
      (app (-> (mock/request :post "/location")
               (mock/json-body {:lat "44.50155"
                                :lon "11.33989"})))
      => (fn [res] (:body res))
      => (contains "Emilia-Romagna, 40122, Italia"))

(fact "location endpoint test-2"
      (app (-> (mock/request :post "/location")
               (mock/json-body {:lat "-1"                   ;invalid lat and lon
                                :lon "-1"})))
      => empty)

(fact "historic searches endpoint test"
      (app (-> (mock/request :post "/historic-searches")
               (mock/json-body {:lat "44.7855611"
                                :lon "20.4732303"})))
      => (fn [res] (:body res))
      => (fn [body] (> (count body)) -1))

(fact "historic searches endpoint test-2"
      (app (-> (mock/request :post "/historic-searches")
               (mock/json-body {:lat "-1"                   ;invalid lat and lon
                                :lon "-1"})))
      => empty)

(fact "search endpoint test"
      (app (mock/request "get" "http://localhost:882/search?" {"location" "MSAOIF"
                                         "date" "2020-10-10"
                                         "time" "12:10"
                                         "lat" "40.5555"
                                         "lon" "87.255"}))
      => (fn [res] (:body res))
      => (fn [body] (= (count body) 24)))