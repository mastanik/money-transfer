# Simple Money Transfer Project

The application exposes 3 REST endpoints in order to create Customer, Account and make an operation. It uses in-memory H2 database for persistence.

##Building the app

To generate sources and execute tests run the following command:
```$java
mvn clean package
```

By default the following database properties are used by Flyway migration plugin and jooq generation plugin:
```bash
db.url: jdbc:h2:~/revolut-db
db.username: revolut-user
db.password: revolut-password
```

You can override it by providing the properties via command line, like this:

```bash
mvn clean package -Ddb.url=$URL -Ddb.username=$USERNAME -Ddb.password=$PASSWORD
```

##Rest endpoints

