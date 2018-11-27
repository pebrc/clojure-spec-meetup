(ns clj-spec-demo.core
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]))


;;; Why do we need / what is clojure.spec?

;; no typesystem (Clojure has none and spec is not one)
;; generic types maps/vectors/lists are pervasive

;; how to specify and communicate contracts/interfaces 
;; how to generate test data
;; how to specify contracts of functions


;;; How

;; in statically typed language we might: 

;; > Person(name: String, email: Email )

;; but: conflates attributes, values and entity
;; you need a ton of entities

;; > RegisteredPerson(name: String, email: Email, accountId: Int)
;; > etc. pp. 

;; typically compile time only


;;; RDF

"https://en.wikipedia.org/wiki/Resource_Description_Framework"

;; Making statements about resources/entities in the form subject-predicate-object
;; every attribute exists independently of the entity
;; designed to be cross-schema cross-database

;; pseudo-RDF:
;; :me :name "peter"
;; :me :email "p.brc@blah.com"
;; :me :isa :person


;; Domain modelling

(s/def ::name string?)

(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")

(s/def ::email (s/and string? #(re-matches email-regex %)))


(s/def ::person (s/keys :req [::name ::email]
                        :opt [::phone]))

(s/conform ::person {::name "peter" ::email "p.brc@blah.com"})


;; Validation

(s/conform ::email "p")
(s/valid? ::email "a@b.com")



;; Parsing

"Can we parse the date part of ISO 8601 with clojure.spec?"

;;; some helpers
(defn int-from-chars [cs]
  (Integer. (apply str cs)))

(defn int-from-labeled-nested-chars [cs]
  (Integer. (apply str (flatten (vals (second cs))))))


;;; parsing a day with leading zeros

;; http://matt.might.net/articles/implementation-of-regular-expression-matching-in-scheme-with-derivatives/
;; http://www.ccs.neu.edu/home/turon/re-deriv.pdf
;; http://matt.might.net/papers/might2011derivatives.pdf

(s/def ::day
  (s/&
   (s/+ (set "0123456789"))
   #(= 2 (count %))
   (s/or
    :single (s/cat :first #{\0} :second (s/+ (set "123456789")))
    :tens (s/cat :first #{\1}  :second  (s/+ (set "0123456789")))
    :twenties (s/cat :first #{\2} :second (s/+ (set "0123456789")))
    :thirties (s/cat :first #{\3} :second (s/+ (set "01"))))
   (s/conformer int-from-labeled-nested-chars)
   ))


(s/conform ::day (seq "01"))


;;; parsing a month 
(s/def ::month
  (s/&
   (s/+ (set "0123456789"))
   #(= 2 (count %))
   (s/or
    :single (s/cat :first #{\0} :second (s/+ (set "123456789")))
    :double (s/cat :first #{\1}  :second  (s/+ (set "012"))))   
   (s/conformer int-from-labeled-nested-chars)))

(s/conform ::month (seq "11"))


;;; parsing a four digit year
(s/def ::year
  (s/&
   (s/+ (set "0123456789"))
   #(= 4 (count %))
   (s/conformer int-from-chars)))

(s/conform ::year (seq "2001"))


;;; parsing the date part of ISO 8601
(s/def ::yyyy-MM-dd
  (s/and (s/cat
          :year ::year
          :h1 #{\-}
          :month ::month
          :h2 #{\-}
          :day ::day
          )
         (s/conformer #(select-keys % [:year :month :day]))))


(s/conform ::yyyy-MM-dd (seq "2001-11-01"))

;; date parsing is hard
(s/conform ::yyyy-MM-dd (seq "2018-02-31"))

;; some constraints are checked
(s/conform ::yyyy-MM-dd (seq "2018-31-02"))


;; generating data is difficult for such a refined spec
(s/exercise ::day 1)

(defn char-numeric []
  (gen/such-that #(= 2 (count %))
                 (gen/not-empty
                  (gen/vector
                   (gen/fmap char
                             (gen/choose 48 57))
                   2))))

(gen/sample (char-numeric) 10)

(s/exercise ::day 10 {::day char-numeric})

;; Transformation

;; from https://gist.github.com/stuarthalloway/01a2b7233b1285a8b43dfc206ba0036e

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



