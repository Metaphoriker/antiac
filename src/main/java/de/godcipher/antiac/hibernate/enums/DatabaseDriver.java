package de.godcipher.antiac.hibernate.enums;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public enum DatabaseDriver {
  MYSQL("com.mysql.cj.jdbc.Driver"),
  POSTGRESQL("org.postgresql.Driver"),
  H2("org.h2.Driver"),
  ORACLE("oracle.jdbc.OracleDriver"),
  SQLSERVER("com.microsoft.sqlserver.jdbc.SQLServerDriver"),
  SQLITE("org.sqlite.JDBC");

  private final String driverClass;

  DatabaseDriver(String driverClass) {
    this.driverClass = driverClass;
  }
}
