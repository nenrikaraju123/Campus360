package com.campus360.platform.numbering;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "number_sequences", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tenant_id", "sequence_key", "financial_year"})
})
public class NumberSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 50)
    private String sequenceKey;

    @Column(length = 20)
    private String prefix;

    @Column(nullable = false)
    private Long nextValue = 1L;

    @Column(nullable = false)
    private Integer padding = 0;

    @Column(length = 20)
    private String financialYear;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public String getSequenceKey() { return sequenceKey; }
    public void setSequenceKey(String sequenceKey) { this.sequenceKey = sequenceKey; }

    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }

    public Long getNextValue() { return nextValue; }
    public void setNextValue(Long nextValue) { this.nextValue = nextValue; }

    public Integer getPadding() { return padding; }
    public void setPadding(Integer padding) { this.padding = padding; }

    public String getFinancialYear() { return financialYear; }
    public void setFinancialYear(String financialYear) { this.financialYear = financialYear; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
