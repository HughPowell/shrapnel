# net.hughpowell.shrapnel.money

## \*monetary-amount-implementation\*
\[[source](net/hughpowell/shrapnel/money.clj#L35)\]


Rebind this var to change the implementation of the monetary amount used.

  Included options are: `:money` (default), `:fast-money`, `rounded-money`.

  If you have implemented a new method to parse-monetary-amount then dispatch
  value can be set here.
## parse-monetary-amount
\[[source](net/hughpowell/shrapnel/money.clj#L45)\]


Creates a [MonetaryAmount](https://javamoney.github.io/apidocs/java.money/javax/money/MonetaryAmount.html)
          from a string.

          Included implementations for `:money`, `fast-money` and `rounded-money`
          that produce their associated monetary amounts.
## print-money-literals!
\[[source](net/hughpowell/shrapnel/money.clj#L17)\]

([])

Add defmethods to support serialising monetary amounts.

  Call this as part of your program's initialisation or dev set up.
## tags
\[[source](net/hughpowell/shrapnel/money.clj#L68)\]


Convenience for passing to data reader functions, e.g.
  `(clojure.edn/read-string {:readers tags} "#money/money \"AUD 100\"")`