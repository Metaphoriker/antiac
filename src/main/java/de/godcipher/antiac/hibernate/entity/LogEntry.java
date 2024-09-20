package de.godcipher.antiac.hibernate.entity;

import de.godcipher.antiac.click.ClickType;
import java.util.UUID;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Entity
@EqualsAndHashCode
@ToString
public class LogEntry {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(nullable = false)
  private final UUID uuid;

  @Basic
  @Column(nullable = false)
  private final String checkName;

  @Basic
  @Column(nullable = false)
  private final int lastCPS;

  @Basic
  @Column(nullable = false)
  private final ClickType averageClickType;

  public LogEntry(UUID uuid, String checkName, int lastCPS, ClickType averageClickType) {
    this.uuid = uuid;
    this.checkName = checkName;
    this.lastCPS = lastCPS;
    this.averageClickType = averageClickType;
  }
}
