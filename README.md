# clj-restaurants

This application is a demonstration of Clojure for the greatest Julien
Plumez's restaurants application.
Basically, it's a trivial business application to demonstrate how
the Clojure ecosystem could solve the exact same problems with
simple building blocks.

From a Java perspective, here is what we use:
| Building block        | Java    | Clojure    | Code |
|-----------------------|:--------|:--------   |:-----|
| Build tools           | Maven   | [Leiningen](https://leiningen.org/) | [project.clj](https://github.com/arnaudgeiser/clj-restaurants/blob/master/project.clj)
| Dependency Injection  | Spring  | [component](https://github.com/stuartsierra/component)  | [system.clj](https://github.com/arnaudgeiser/clj-restaurants/blob/master/src/clj_restaurants/system.clj)|
| Database libraries    | JPA     | [JDBC](https://github.com/seancorfield/next-jdbc), [SEQL](https://github.com/exoscale/seql) | [db.clj](https://github.com/arnaudgeiser/clj-restaurants/blob/master/src/clj_restaurants/db.clj)
| Transaction           | Spring  | [JDBC](https://cljdoc.org/d/com.github.seancorfield/next.jdbc/1.1.643/doc/getting-started/transactions) | [service.clj](https://github.com/arnaudgeiser/clj-restaurants/blob/master/src/clj_restaurants/service.clj)
| Database migrations   | Flyway  | [Ragtime](https://github.com/weavejester/ragtime) | [migrations.clj](https://github.com/arnaudgeiser/clj-restaurants/blob/master/src/clj_restaurants/migrations.clj)
| Connection pools      | HikariCP | HikariCP | [datasource.clj](https://github.com/arnaudgeiser/clj-restaurants/blob/master/src/clj_restaurants/datasource.clj)
| Configuration files   | YAML     | EDN | [config.edn](https://github.com/arnaudgeiser/clj-restaurants/blob/master/resrouces/config.edn)

## Usage

Install Leningen : https://leiningen.org/

```shell
lein run
```
