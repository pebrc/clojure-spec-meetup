(ns clj-spec-demo.core
  (:require [clojure.spec.alpha :as s]))

(s/conform  (s/coll-of number?) [1 2 'a])

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


