package com.runwaysdk.build.domain;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.runwaysdk.business.graph.EdgeObject;
import com.runwaysdk.business.graph.GraphQuery;
import com.runwaysdk.dataaccess.graph.GraphDBService;
import com.runwaysdk.dataaccess.transaction.Transaction;
import com.runwaysdk.session.Request;

import net.geoprism.registry.model.graph.ExternalId;

public class ExternalIdIntegrityPatch
{
  private Logger logger = LoggerFactory.getLogger(ExternalIdIntegrityPatch.class);
  
  public static void main(String[] args)
  {
    new ExternalIdIntegrityPatch().doItInReq();
  }
  
//  select from external_id
//  let $tmp = ( select from external_id where id = $parent.current.id and out.oid = $parent.current.out.oid and in.@class = $parent.current.in.@class )
//  where $tmp.SIZE() > 1
//  limit 100;
  
  @Request
  private void doItInReq()
  {
    deleteDuplicates();
    updateKey();
  }
  
  @Transaction
  private void deleteDuplicates()
  {
    logger.info("Querying for duplicate external ids. This will take upwards of 6 minutes.");
    
    String sql = "select from external_id\n"
        + "let $tmp = ( select from external_id where id = $parent.current.id and out.oid = $parent.current.out.oid and in.@class = $parent.current.in.@class )\n"
        + "where $tmp.SIZE() > 1;";
    
    GraphQuery<EdgeObject> query = new GraphQuery<EdgeObject>(sql);
    
    for (EdgeObject edge : query.getResults())
    {
      logger.error("Deleting corrupt edge " + edge.getOid() + " which referenced external id " + String.valueOf((Object) edge.getObjectValue("id")) + " and external system " + edge.getParent().getOid() + " and GOT " + edge.getChild().getType());
      edge.delete();
    }
  }
  
  @Transaction
  private void updateKey()
  {
    logger.info("Calculating keys.");
    
    String sql = "update external_id set key = id + '" + ExternalId.KEY_SEPARATOR + "' + out.oid + '" + ExternalId.KEY_SEPARATOR + "' + in.@class";
    
    GraphDBService service = GraphDBService.getInstance();
    
    service.command(service.getGraphDBRequest(), sql, new HashMap<String,Object>());
  }
}
