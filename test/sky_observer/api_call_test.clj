(ns sky-observer.api-call-test
  (:use midje.sweet)
  (:require [sky-observer.api-call :refer :all]))

(fact "get historic weather too far into future"
      (weather-condition 44.7855611 20.4732303 "2022-01-01")
      => (fn [response] (nil? response)))

(fact "get historic weather on day of test"
      (weather-condition 44.7855611 20.4732303 "2021-01-12")
      => (fn [response] (= (count response) 24)))

(fact "find location"
      (find-location "Beograd Srbija")
      => (list {:lat   "44.8178131"
                :lon   "20.4568974"
                :place "Београд, Град Београд, Централна Србија, 11000, Србија"}
               {:lat   "44.6802084"
                :lon   "20.38179182642312"
                :place "Град Београд, Централна Србија, Србија"}
               {:lat   "44.8183949"
                :lon   "20.4616706"
                :place "Beograd, 27, Симина, Дорћол, Београд (Стари град), Београд, Град Београд, Централна Србија, 11108, Србија"}
               {:lat   "44.8006726"
                :lon   "20.4704368"
                :place "Београд, 47, Макензијева, Чубура, Београд (Врачар), Градска општина Врачар, Београд, Град Београд, Централна Србија, 11000, Србија"}
               {:lat   "44.8106196"
                :lon   "20.4764939"
                :place "Београд, 16, Кнеза Данила, МЗ Стара Палилула, Београд (Палилула), Градска општина Палилула, Београд, Град Београд, Централна Србија, 11060, Србија"}))

(fact "find not existing location"
      (find-location "xzy")
      => (list))

(fact "find location with valid coordinates"
      (find-location-with-coordinates "44.7727174" "20.4747931")
      => {:lat   "44.7727708"
          :lon   "20.475007857226572"
          :place "Факултет организационих наука, 154, Јове Илића, МЗ Чиновничка колонија, Београд (Вождовац), Градска општина Вождовац, Београд, Град Београд, Централна Србија, 11000, Србија"})

(fact "find location with invalid coordinates"
      (find-location-with-coordinates "100" "190")
      => {:lat   nil
          :lon   nil
          :place nil})

