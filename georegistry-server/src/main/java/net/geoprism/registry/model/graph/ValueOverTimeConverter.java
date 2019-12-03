package net.geoprism.registry.model.graph;

import org.commongeoregistry.adapter.dataaccess.ValueOverTimeCollectionDTO;
import org.commongeoregistry.adapter.dataaccess.ValueOverTimeDTO;
import org.commongeoregistry.adapter.metadata.AttributeType;

import com.runwaysdk.dataaccess.graph.attributes.ValueOverTime;
import com.runwaysdk.dataaccess.graph.attributes.ValueOverTimeCollection;

public class ValueOverTimeConverter
{
  public static ValueOverTimeDTO votToDTO(ValueOverTime vot, AttributeType type)
  {
    return new ValueOverTimeDTO(vot.getStartDate(), vot.getEndDate(), vot.getValue(), type);
  }
  
  public static ValueOverTime dtoToVot(ValueOverTimeDTO dto)
  {
    return new ValueOverTime(dto.getStartDate(), dto.getEndDate(), dto.getValue());
  }
  
  public static ValueOverTimeCollectionDTO colToDTO(ValueOverTimeCollection col, AttributeType type)
  {
    ValueOverTimeCollectionDTO dto = new ValueOverTimeCollectionDTO();
    
    for (ValueOverTime vot : col)
    {
      dto.add(votToDTO(vot, type));
    }
    
    return dto;
  }
  
  public static ValueOverTimeCollection dtoToCol(ValueOverTimeCollectionDTO dto)
  {
    ValueOverTimeCollection col = new ValueOverTimeCollection();
    
    for (int i = 0; i < dto.size(); ++i)
    {
      col.add(dtoToVot(dto.get(i)));
    }
    
    return col;
  }
}
