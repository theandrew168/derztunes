(ns build
  (:require [clojure.tools.build.api :as b]))

(def lib 'theandrew168/derztunes)
(def class-dir "target/classes")
(def basis (delay (b/create-basis {:project "deps.edn"})))
(def jar-file (format "target/%s.jar" (name lib)))

(defn clean [_]
  (b/delete {:path "target"}))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn jar [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/compile-clj {:basis @basis
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file jar-file
           :basis @basis
           :main 'derztunes.core}))
