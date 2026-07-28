(ns build
  (:require [clojure.tools.build.api :as build]))

(def lib 'net.hughpowell/shrapnel)
(def version (format "0.1.%s" (build/git-count-revs nil)))
(def class-dir "target/classes")
(def jar-file (format "target/%s-%s.jar" (name lib) version))

(def basis (delay (build/create-basis {:project "deps.edn"})))

(defn clean [_] (build/delete {:path "target"}))

(defn jar [_]
  (build/write-pom {:class-dir class-dir
                    :lib       lib
                    :version   version
                    :basis     @basis
                    :src-dirs  ["src"]})
  (build/copy-dir {:src-dirs   ["src"]
                   :target-dir class-dir})
  (build/jar {:class-dir class-dir
              :jar-file  jar-file}))
