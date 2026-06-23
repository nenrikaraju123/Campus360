package com.campus360.importer.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "import_job_rows")
public class ImportJobRow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private ImportJob job;

    @Column(nullable = false)
    private Integer rowIndex;

    @Column(columnDefinition = "JSONB", nullable = false)
    private String dataJson;

    @Column(nullable = false, length = 50)
    private String status; // PENDING, VALID, INVALID, COMMITTED, FAILED

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ImportJob getJob() { return job; }
    public void setJob(ImportJob job) { this.job = job; }

    public Integer getRowIndex() { return rowIndex; }
    public void setRowIndex(Integer rowIndex) { this.rowIndex = rowIndex; }

    public String getDataJson() { return dataJson; }
    public void setDataJson(String dataJson) { this.dataJson = dataJson; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
