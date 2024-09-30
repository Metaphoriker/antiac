package de.godcipher.antiac.hibernate.entity;

import de.godcipher.antiac.click.ClickType;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(of = {"id"})
@ToString
@Entity
@Table(name = "log_entry")
public class LogEntry {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(nullable = false)
  private UUID uuid;

  @Column(nullable = false)
  private String checkName;

  @Column(nullable = false)
  private int lastCPS;

  @Column(nullable = false)
  private ClickType averageClickType;

  protected LogEntry() {}

  public LogEntry(UUID uuid, String checkName, int lastCPS, ClickType averageClickType) {
    this.uuid = uuid;
    this.checkName = checkName;
    this.lastCPS = lastCPS;
    this.averageClickType = averageClickType;
  }
}
