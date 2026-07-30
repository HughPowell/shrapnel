(ns documentation
  (:require [clojure.string :as string]
            [clojure.tools.build.api :as build]
            [net.hughpowell.shrapnel.money]))

(defn ->markdown [docs]
  (map
    (fn [{:keys [name file line arglists doc]}]
      (string/join
        \newline
        [(str "## " (string/replace name "*" "\\*"))
         (format "\\[[source](%s)\\]\n"
                 (format "%s#L%d"
                         (string/replace file #".*/src" "../src")
                         line))
         (when arglists (str arglists \newline))
         doc]))
    docs))

(defn generate-docs [ns-symbol]
  (->> (ns-publics ns-symbol)
       (sort-by (comp str second))
       (keep
         (fn [[_ var-ref]]
           (let [{n     :ns
                  nm    :name
                  :keys [doc arglists line file]} (meta var-ref)]
             (when doc
               {:name     nm
                :ns       n
                :line     line
                :file     file
                :arglists arglists
                :doc      doc}))))))

(defn ns-doc [ns-symbol]
  (string/join
    \newline
    [(str "# " (name (ns-name ns-symbol)))
     (:doc (meta ns-symbol))]))

(defn commit-changes! [file]
  (build/git-process {:git-args ["add" "-f" file]})
  (when (build/git-process {:git-args "diff" "--cached" "--numstat"})
    (build/git-process {:git-args ["commit" "-m" "Update API docs"]})))

(defn generate-api-docs [ns-symbol file]
  (->> (generate-docs ns-symbol)
       (->markdown)
       (cons (ns-doc ns-symbol))
       (string/join \newline)
       (spit file))
  (commit-changes! file))

(defn update-co-ordinates [file]
  (let [version (build/git-count-revs nil)]
    (as-> file $
      (slurp $)
      (string/replace $
                      #"\{\:mvn/version \"[^\"]*\"\}"
                      (format "{:mvn/version \"0.1.%s}\"" version))
      (string/replace $
                      #"\[net.hughpowell.shrapnel \"[^\"]*\"\]"
                      (format "[net.hughpowell.shrapnel \"0.1.%s\"]" version))
      (spit file $))))

(defn generate [_]
  (generate-api-docs 'net.hughpowell.shrapnel.money "docs/api.md")
  (update-co-ordinates "README.md"))
