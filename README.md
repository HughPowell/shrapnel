# Shrapnel

> ... Loose change of little value.
> 
> \- Urban dictionary

Shrapnel will, hopefully, be a loose, slightly opinionated wrapper, around the
[Java Money libraries](https://javamoney.github.io/). With some quality of life
additions. Inspired, at least in part, by
[java-time](https://github.com/dm3/clojure.java-time).

Currently, it's tagged data literals for the MonetaryAmount implementations.

## Other options

Here's a few other options for dealing with money.

### Doubles
If you're only recording prices or doing a small amount of maths (for some definition of small) then using a double is
probably fine. You'll likely have to do some work to display the minor unit (cents, pence, etc.).

### Ints as the minor unit (e.g. cents, pence, etc.) value
This will work for most arithmetic calculations and predicates. It becomes complex once you have to do division,
including calculating the value in another currency using an exchange rate. Experience tells me you'll also end up with
your monetary values beings suffixed with "-as-cents" (or your local currency equivalent) and then one or two that don't
and wondering if that matters or not.

### [clojureworkz/money](https://github.com/clojurewerkz/money)
Battletested library powered by [joda-money](https://www.joda.org/joda-money/). An excellent choice.

## Quick start
[![Clojars Project](https://img.shields.io/clojars/v/net.hughpowell/shrapnel.svg)](https://clojars.org/net.hughpowell/shrapnel)

### deps.edn
If you're using deps.edn add the following to your deps file

`{:deps {net.hughpowell/shrapnel {:mvn/version "0.1.9-SNAPSHOT"}}}`

### Leiningen/Boot

If your using Leiningen or Boot add the following to your porject

`[net.hughpowell/shrapnel "0.1.9-SNAPSHOT"]`

### Using shrapnel

```clojure
(require '[net.hughpowell.shrapnel.money :as money])

;; Serialise MonetaryAmount implementations as
;; #money/money "<currency code> <amount>"
(money/print-money-literals!)
```

## Representation

Monetary amounts are represented like so `#money/money "<currency code> <amount>"`
e.g. 100 Australian dollars is `#money/money "AUD 100"`.

### Note
You might expect 100 Australian dollars to be serialised as
`#money/money "AUD 100.00"`, since there are 100 cents in a dollar. The possibly
more interesting case is `#money/money "AUD 1.6666666667"`. We probably don't
want to persist that because it requires assumptions to understand what it means
and assumptions are particularly bad when it comes to money. We do, however,
want to keep the full fidelity while doing calculations. I'll come back to this
later, but for the moment just expect all trailing zeros after the decimal point
to be dropped.

## (De)serialisation

All public functions are in the `net.hughpowell.shrapnel.money` namespace.
To auto-magically serialise monetary amounts call `(print-money-literals!)`.

```clojure
(require '[net.hughpowell.shrapnel.money :as money])
(money/print-money-literals!)
=> nil
```

To deserialise a monetary amount tagged literal pass the `tags` var to your
reader function.

```clojure
(require '[net.hughpowell.shrapnel.money :as money])
(edn/read-string {:readers money/tags} "#money/money \"AUD 100\"")
=> #money/money "AUD 100"
```

By default, that creates a MonetaryAmount of Money. If you want to deserialise
into a different MonetaryAmount then you'll need to rebind the
`money/*monetary-amount-implementation*` var.

```clojure
(require '[net.hughpowell.shrapnel.money :as money])

(-> (edn/read-string {:readers tags} "#money/money \"AUD 100\"")
    (type))
=> org.javamoney.moneta.Money

(binding [*monetary-amount-implementation* :fast-money]
  (-> (edn/read-string {:readers tags} "#money/money \"AUD 100\"")
      (type)))
=> org.javamoney.moneta.FastMoney
```

## Brining your own money

If you have your own implementation of the
[MonetaryAmount](https://javamoney.github.io/apidocs/java.money/javax/money/MonetaryAmount.html)
interface that can be parsed from a string then you can add a `defmethod` to the
`parse-monetary-amount` `defmulti`. You'll then need to bind
`*monetary-amount-implementation*` to the dispatch value you used for your
`defmethod` when reading in monetary amounts.

```clojure
(require '[net.hughpowell.shrapnel.money :as money]
          [my.namespace :as my])

(defmethod money/parse-monetary-amount ::my/money [s _]
  ;; Assumes you've implemented the prase-my-money function
  (my/prase-my-money s))

(binding [money/*monetary-amount-implementation* ::my/money]
  (-> (edn/read-string {:readers tags} "#money/money \"AUD 100\"")
      (type)))
=> my.namespace.MyMoney
```

If you're going to use your implementation exclusively then you could set
`*monetary-amount-implementation*` permanently.

```clojure
(require '[net.hughpowell.shrapnel.money :as money])

(alter-var-root money/*monetary-amount-implementation* (constantly true))
```

## API docs

Can be found at [docs/api.md](docs/api.md).

## License

Distributed under the [MPLv2.0 License](/LICENSE)
