///
/// Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Runway SDK(tm).
///
/// Runway SDK(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Runway SDK(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
///

import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { Router } from '@angular/router';

import { TreeNode, TreeComponent } from 'angular-tree-component';


//import { Category, BasicCategory } from '../model/category';

import { HierarchyService } from '../../service/hierarchy.service';

class Instance {
  active: boolean;
  label: string;   
}

@Component({
  
  selector: 'hierarchies',
  templateUrl: './hierarchy.component.html',
  styleUrls: []
})
export class HierarchyComponent implements OnInit {
  instance : Instance = new Instance();  

  constructor(
    private router: Router,
    private hierarchyService: HierarchyService) { }

  ngOnInit(): void {
  }
  
  
  /*
   * Tree component
   */
  @ViewChild( TreeComponent )
  private tree: TreeComponent;
  
  nodes = [
           {
             id: 1,
             name: 'root1',
             children: [
               { id: 2, name: 'child1' },
               { id: 3, name: 'child2' }
             ]
           },
           {
             id: 4,
             name: 'root2',
             children: [
               { id: 5, name: 'child2.1' },
               {
                 id: 6,
                 name: 'child2.2',
                 children: [
                   { id: 7, name: 'subsub' }
                 ]
               }
             ]
           }
         ];
  options = {};
   
}
