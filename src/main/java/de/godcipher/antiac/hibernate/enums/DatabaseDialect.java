package de.godcipher.antiac.hibernate.enums;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public enum DatabaseDialect {
  MYSQL("org.hibernate.dialect.MySQLDialect"),
  POSTGRESQL("org.hibernate.dialect.PostgreSQLDialect"),
  H2("org.hibernate.dialect.H2Dialect"),
  ORACLE("org.hibernate.dialect.OracleDialect"),
  SQLSERVER("org.hibernate.dialect.SQLServerDialect"),
  SQLITE("org.hibernate.dialect.SQLiteDialect");

  private final String dialectClass;

  DatabaseDialect(String dialectClass) {
    this.dialectClass = dialectClass;
  }
}
