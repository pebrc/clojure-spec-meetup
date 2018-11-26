(ns clj-spec-demo.core
  (:require [clojure.spec.alpha :as s]))

(s/conform  (s/coll-of number?) [1 2 'a])

;; Validation


;; Parsing


(s/def ::day
  (s/&
   (s/+ (set "0123456789"))
   (s/or
    :single (s/cat :first #{\0} :second (s/+ (set "123456789")))
    :tens (s/cat :first #{\1}  :second  (s/+ (set "0123456789")))
    :twenties (s/cat :first #{\2} :second (s/+ (set "0123456789")))
    :thirties (s/cat :first #{\3} :second (s/+ (set "01"))))))

(s/def ::month
  (s/&
   (s/+ (set "0123456789"))
   (s/or
    :single (s/cat :first #{\0} :second (s/+ (set "123456789")))
    :double (s/cat :first #{\1}  :second  (s/+ (set "012"))))))

(s/def ::year
  (s/+ (set "0123456789")))

(s/def ::yyyy-MM-dd
  (s/cat
   :year ::year
   :h1 #{\-}
   :month ::month
   :h2 #{\-}
   :day ::day
   ))

(s/conform ::yyyy-MM-dd (seq "2001-11-01"))

(s/conform ::day (seq "01"))
(s/conform ::month (seq "11"))
(s/conform ::year (seq "2001"))





;; Transformation

(s/def ::fizz (s/and pos-int? #(zero? (mod % 3))))
(s/def ::buzz (s/and pos-int? #(zero? (mod % 5))))
(s/def ::fizzbuzz (s/and pos-int? #(zero? (mod % 15))))


(s/def ::fizzbuzznum
  (s/and (s/or :name (s/and (s/or :FizzBuzz ::fizzbuzz :Buzz ::buzz :Fizz ::fizz)
                            (s/conformer first))
               :num pos-int?)
         (s/conformer second)))

(s/exercise ::fizzbuzznum 10)

(def fizzbuzz-xform (map (partial s/conform ::fizzbuzznum)))

(eduction fizzbuzz-xform (range 1 20))

(s/exercise ::fizz)
(s/conform ::fizz 8)


