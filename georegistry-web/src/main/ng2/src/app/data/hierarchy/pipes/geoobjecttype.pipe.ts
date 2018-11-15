import { Pipe, PipeTransform } from '@angular/core';
import { GeoObjectType, HierarchyNode } from '../hierarchy';

@Pipe({
  name: 'geoobjecttype'
})
export class GeoObjectTypePipe implements PipeTransform {
  transform(items: GeoObjectType[], filter: HierarchyNode[]): any {
	  if (!items || !filter) {
          return items;
      }
	  
	  let unassignedGeoObjTypes:string[] = [];
      this.buildUnassignedGeoObjTypes(filter, unassignedGeoObjTypes)
      
      // filter items array, items which match and return true will be
      // kept, false will be filtered out
      return items.filter(item => unassignedGeoObjTypes.indexOf(item.code) === -1);
  }
  
  buildUnassignedGeoObjTypes(filter: HierarchyNode[], unassignedGeoObjTypes: string[]): void{
	  filter.forEach(f => {
		  this.processHierarchyNodes(f, unassignedGeoObjTypes);
	  })
  }
  
  processHierarchyNodes(node: HierarchyNode, unassignedGeoObjTypes: string[]){
	  unassignedGeoObjTypes.push(node.geoObjectType)
	  
	  node.children.forEach(child => {
		  this.processHierarchyNodes(child, unassignedGeoObjTypes);
	  })
  }
}