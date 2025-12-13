package ru.nsu.spendsphere.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/** Сущность для хранения отдельного совета. */
@Entity
@Table(name = "advice_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AdviceItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "advice_id", nullable = false)
  @ToString.Exclude
  private Advice advice;

  @Column(name = "item_order", nullable = false)
  private Integer itemOrder;

  @Column(name = "title", nullable = false, length = 200)
  private String title;

  @Column(name = "priority", nullable = false, length = 20)
  private String priority;

  @Column(name = "description", nullable = false, length = 2000)
  private String description;
}
