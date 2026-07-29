(ns build
  (:require [clojure.tools.build.api :as build]))

(def lib 'net.hughpowell/shrapnel)
(def version (format "0.1.%s" (build/git-count-revs nil)))
(def class-dir "target/classes")
(def jar-file (format "target/%s.jar" (name lib)))

(def basis (delay (build/create-basis {:project "deps.edn"})))

(defn clean [_]
  (build/delete {:path "pom.xml"})
  (build/delete {:path "pom.properties"})
  (build/delete {:path "target"}))

(defn jar [_]
  (let [pom {:lib      lib
             :version  version
             :basis    @basis
             :src-dirs ["src"]
             :pom-data [[:licenses
                         [:license
                          [:name "Mozilla Public License Version 2.0"]
                          [:url "https://www.mozilla.org/en-GB/MPL/2.0/"]
                          [:distribution "repo"]]]]}]
    (build/write-pom (assoc pom :class-dir class-dir))
    (build/write-pom (assoc pom :target "")))
  (build/copy-dir {:src-dirs   ["src"]
                   :target-dir class-dir})
  (build/jar {:class-dir class-dir
              :jar-file  jar-file}))
