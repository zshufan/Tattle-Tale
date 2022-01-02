package edu.policy.model.policy;

import edu.policy.model.PurposeType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Policy {

    private UUID id;

    private String tableName;

    private List<SelectionCondition> selectionConditions;

    private List<ProjectionAttribute> projectionAttributes;

    private PurposeType purpose;

    private String action = "deny";

    private LocalDateTime insertedAt;

    public Policy(UUID id, String tableName, List<SelectionCondition> selectionConditions, List<ProjectionAttribute> projectionAttributes, PurposeType purpose, String action, LocalDateTime insertedAt) {
        this.id = id;
        this.tableName = tableName;
        this.selectionConditions = selectionConditions;
        this.projectionAttributes = projectionAttributes;
        this.purpose = purpose;
        this.action = action;
        this.insertedAt = insertedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Policy policy = (Policy) o;
        return id.equals(policy.id) && tableName.equals(policy.tableName) && Objects.equals(selectionConditions, policy.selectionConditions) && Objects.equals(projectionAttributes, policy.projectionAttributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tableName, selectionConditions, projectionAttributes);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<SelectionCondition> getSelectionConditions() {
        return selectionConditions;
    }

    public void setSelectionConditions(List<SelectionCondition> selectionConditions) {
        this.selectionConditions = selectionConditions;
    }

    public List<ProjectionAttribute> getProjectionAttributes() {
        return projectionAttributes;
    }

    public void setProjectionAttributes(List<ProjectionAttribute> projectionAttributes) {
        this.projectionAttributes = projectionAttributes;
    }

    public PurposeType getPurpose() {
        return purpose;
    }

    public void setPurpose(PurposeType purpose) {
        this.purpose = purpose;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LocalDateTime getInsertedAt() {
        return insertedAt;
    }

    public void setInsertedAt(LocalDateTime insertedAt) {
        this.insertedAt = insertedAt;
    }

    @Override
    public String toString() {
        return "Policy{" +
                "id=" + id +
                ", tableName='" + tableName + '\'' +
                ", selectionConditions=" + selectionConditions +
                ", projectionConditions=" + projectionAttributes +
                ", purpose=" + purpose +
                ", action='" + action + '\'' +
                ", insertedAt=" + insertedAt +
                '}';
    }
}
