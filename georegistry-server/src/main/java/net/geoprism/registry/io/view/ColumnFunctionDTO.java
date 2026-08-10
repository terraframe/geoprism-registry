package net.geoprism.registry.io.view;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, // use logical type name
    include = JsonTypeInfo.As.PROPERTY, property = "type")

@JsonSubTypes({ //
    @JsonSubTypes.Type(value = BasicColumnFunctionDTO.class, name = BasicColumnFunctionDTO.TYPE), //
    @JsonSubTypes.Type(value = ConstantFunctionDTO.class, name = ConstantFunctionDTO.TYPE), //
    @JsonSubTypes.Type(value = LocalizedValueFunctionDTO.class, name = LocalizedValueFunctionDTO.TYPE), //
})
@JsonIgnoreProperties({ "type" })
public abstract class ColumnFunctionDTO
{
  public abstract String getType();
}
