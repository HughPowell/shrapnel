(ns net.hughpowell.shrapnel.money
  "Support (de)serialisation of monetary amounts, using the #money/money tag
  literal.

  Monetary amounts are serialised to #money/money \"<currency code> <amount>\",
  e.g. `#money/money \"100 AUD\"`."
  (:refer-clojure :exclude [print])
  (:require [clojure.edn :as edn])
  (:import (java.io Writer)
           (javax.money MonetaryAmount)
           (org.javamoney.moneta.format
             ToStringMonetaryAmountFormat
             ToStringMonetaryAmountFormat$ToStringMonetaryAmountFormatStyle)))

(defn print [m] (format "#money/money \"%s\"" m))

(defn print-money-literals!
  "Add defmethods to support serialising monetary amounts.

  Call this as part of your program's initialisation or dev set up."
  []
  (defmethod print-method MonetaryAmount [c ^Writer w]
    (.write w ^String (print c)))
  (defmethod print-dup MonetaryAmount [c ^Writer w]
    (.write w ^String (print c))))

(def ^{:dynamic true
       :doc
       "Rebind this var to change the implementation of the monetary amount used.

  Included options are: `:money` (default), `:fast-money`, `rounded-money`.

  If you have implemented a new method to parse-monetary-amount then dispatch
  value can be set here."}
  *monetary-amount-implementation* :money)

(defmulti parse-monetary-amount
  "Creates a [MonetaryAmount](https://javamoney.github.io/apidocs/java.money/javax/money/MonetaryAmount.html)
   from a string of the form \"<currency code> <amount>\".

   Included implementations for `:money`, `fast-money` and `rounded-money`
   that produce their associated monetary amounts."
  (fn [_ type] type))
(defmethod parse-monetary-amount :money [s _]
  (-> ToStringMonetaryAmountFormat$ToStringMonetaryAmountFormatStyle/MONEY
      (ToStringMonetaryAmountFormat/of)
      (.parse s)))
(defmethod parse-monetary-amount :fast-money [s _]
  (-> ToStringMonetaryAmountFormat$ToStringMonetaryAmountFormatStyle/FAST_MONEY
      (ToStringMonetaryAmountFormat/of)
      (.parse s)))
(defmethod parse-monetary-amount :rounded-money [s _]
  (-> ToStringMonetaryAmountFormat$ToStringMonetaryAmountFormatStyle/ROUNDED_MONEY
      (ToStringMonetaryAmountFormat/of)
      (.parse s)))

(defn parse [s]
  (parse-monetary-amount s *monetary-amount-implementation*))

(def tags
  "Convenience for passing to data reader functions, e.g.
  `(clojure.edn/read-string {:readers tags} \"#money/money \\\"AUD 100\\\"\")`"
  {'money/money parse})

^:rct/test
(comment
  (import '(org.javamoney.moneta Money))
  (print-money-literals!)
  (Money/of 100 "AUD")
  ;=> #money/money "AUD 100"

  (require '[clojure.edn :as edn])
  (edn/read-string {:readers tags} "#money/money \"AUD 100\"")
  ;=> #money/money "AUD 100"

  ;; Round trip
  (let [m (Money/of 100 "AUD")]
    (->> (with-out-str (clojure.core/print m))
         (edn/read-string {:readers tags})
         (= m)))
  ;=> true

  ;; Round trip FastMoney
  (import '(org.javamoney.moneta FastMoney))
  (let [m (FastMoney/of 100 "AUD")]
    (binding [*monetary-amount-implementation* :fast-money]
      (->> (with-out-str (clojure.core/print m))
           (edn/read-string {:readers tags})
           (= m))))
  ;=> true
  )
