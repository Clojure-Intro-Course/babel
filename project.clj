(defproject babel-middleware "0.2.0-alpha"
  :description "A proof of concept library to rewrite error messages."
  :url "https://github.com/Clojure-Intro-Course/babel"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.11.1"] ;[org.clojure/clojure "1.10.1"]
                 ;[org.clojure/spec-alpha2 "0.2.177-SNAPSHOT"]
                 [org.clojure/spec.alpha "0.2.176"]
                 [nrepl "0.6.0"]
                 [expectations "2.2.0-rc3"]
                 [com.rpl/specter "1.1.3"]
                 [hiccup "1.0.5"]]
  :plugins [[lein-expectations "0.0.8"]]
  ;:jvm-opts ["--report" "none"] ; temporarily setting it here, will need to be set in the client project
  :repl-options {;; :nrepl-middleware
                 ;; [babel.middleware/interceptor]
                 :port 7888
                 :report "none"
                 ;; This is a repl hook, not an nrepl hook (and that's what we want):
                 :caught babel.middleware/babel-errors}
   :injections [(require 'corefns.corefns)]
   :main babel.middleware
   :aot [babel.middleware])
