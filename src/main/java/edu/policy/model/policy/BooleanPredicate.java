package edu.policy.model.policy;

import edu.policy.common.PCDCException;
import edu.policy.model.AttributeType;
import edu.policy.model.OperationType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.stream.Stream;

public class BooleanPredicate {

    private String value;

    private OperationType operator;

    public BooleanPredicate(String value, OperationType operator) {
        this.value = value;
        this.operator = operator;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public OperationType getOperator() {
        return operator;
    }

    public void setOperator(OperationType operator) {
        this.operator = operator;
    }

    public int compareOnType(BooleanPredicate o, String attribute) {
        if(Stream.of(AttributeType.STRING.toString()).anyMatch(attribute::equalsIgnoreCase)
                && o.getValue().matches("-?\\d+")) {
            int o1 = Integer.parseInt(this.getValue());
            int o2 = Integer.parseInt(o.getValue());
            return o1-o2;
        }
        else if(attribute.equalsIgnoreCase(AttributeType.TIME.toString())) {
            LocalTime o1 = LocalTime.parse(this.getValue());
            LocalTime o2 = LocalTime.parse(o.getValue());
            return o1.compareTo(o2);
        }
        else if (attribute.equalsIgnoreCase(AttributeType.DATE.toString())) {
            LocalDate o1 = LocalDate.parse(this.getValue());
            LocalDate o2 = LocalDate.parse(o.getValue());
            return o1.compareTo(o2);
        }
        else if (Stream.of(AttributeType.STRING.toString()).anyMatch(attribute::equalsIgnoreCase))  {
            String o1 = this.getValue();
            String o2 = o.getValue();
            return o1.compareTo(o2);
        }
        else{
            throw new PCDCException("Incompatible Attribute Type");
        }
    }
}
