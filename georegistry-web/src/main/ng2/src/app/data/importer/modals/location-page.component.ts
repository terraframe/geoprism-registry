import { Component, OnInit, Input, Output, EventEmitter, Directive } from '@angular/core';

import { ShapefileConfiguration } from '../shapefile';
import { GeoObjectType } from '../../hierarchy/hierarchy';

import { ShapefileService } from '../../../service/shapefile.service';

@Component( {

    selector: 'location-page',
    templateUrl: './location-page.component.html',
    styleUrls: []
} )
export class LocationPageComponent implements OnInit {

    @Input() configuration: ShapefileConfiguration;
    @Output() configurationChange = new EventEmitter<ShapefileConfiguration>();
    @Output() stateChange = new EventEmitter<string>();

    constructor( private service: ShapefileService ) { }

    ngOnInit(): void {
        this.service.getTypeAncestors( this.configuration.type.code, this.configuration.hierarchy ).then( locations => {
            this.configuration.locations = locations;
        } );
    }

    onNext(): void {
        // Map the universals
        this.configurationChange.emit( this.configuration );
        this.stateChange.emit( 'NEXT' );
    }

    onBack(): void {
        this.stateChange.emit( 'BACK' );
    }

    onCancel(): void {
        this.stateChange.emit( 'CANCEL' );
    }


    //    locationFields: { [key: string]: Field[]; } = {};
    //    universals: Universal[];
    //
    //    universalOptions: Universal[];
    //    attribute: LocationAttribute;
    //    unassignedFields: Field[];
    //
    //    constructor( private idService: IdService ) {
    //        this.unassignedFields = [];
    //        this.attribute = this.createNewAttribute();
    //    }
    //
    //    ngOnInit(): void {
    //
    //        // Initialize the universal options
    //        let countries = this.info.options.countries;
    //
    //        for ( let i = 0; i < countries.length; i++ ) {
    //            let country = countries[i];
    //
    //            if ( country.value == this.sheet.country ) {
    //                this.universals = country.options;
    //            }
    //        }
    //
    //        // Create a map of possible location fields
    //        for ( let j = 0; j < this.sheet.fields.length; j++ ) {
    //            let field = this.sheet.fields[j];
    //
    //            if ( field.type == 'LOCATION' ) {
    //                if ( this.locationFields[field.universal] == null ) {
    //                    this.locationFields[field.universal] = [];
    //                }
    //
    //                this.locationFields[field.universal].push( field );
    //            }
    //        }
    //
    //        this.setLocationFieldAutoAssignment();
    //    }
    //
    //    getCoordinateFieldsFromSource(): Field[] {
    //        let coordinateFields: any[] = [];
    //        let latFields: Field[] = [];
    //        let longFields: Field[] = [];
    //        if ( this.sheet ) {
    //            for ( let i = 0; i < this.sheet.fields.length; i++ ) {
    //                let field = this.sheet.fields[i];
    //
    //                if ( field.type == 'LATITUDE' ) {
    //                    latFields.push( field );
    //                }
    //                else if ( field.type == 'LONGITUDE' ) {
    //                    longFields.push( field );
    //                }
    //            }
    //
    //            if ( latFields.length > 0 && longFields.length > 0 ) {
    //                coordinateFields.push( latFields );
    //                coordinateFields.push( longFields );
    //            }
    //        }
    //
    //        return coordinateFields;
    //    }
    //
    //    setDefaults(): void {
    //        if ( this.attribute &&
    //            ( !this.attribute.latForLocationAssignment || this.attribute.latForLocationAssignment.length < 1 ) &&
    //            ( !this.attribute.longForLocationAssignment || this.attribute.longForLocationAssignment.length < 1 ) ) {
    //
    //            let latFields: Field[] = [];
    //            let longFields: Field[] = [];
    //            if ( this.sheet ) {
    //                for ( let i = 0; i < this.sheet.fields.length; i++ ) {
    //                    let field = this.sheet.fields[i];
    //
    //                    if ( field.type == 'LATITUDE' ) {
    //                        latFields.push( field );
    //                    }
    //                    else if ( field.type == 'LONGITUDE' ) {
    //                        longFields.push( field );
    //                    }
    //                }
    //            }
    //
    //            this.attribute.latForLocationAssignment = latFields[0].label;
    //            this.attribute.longForLocationAssignment = longFields[0].label;
    //        }
    //    }
    //
    //
    //    /**
    //     * Gets the lowest unassigned location fields in across the entire universal hierarchy.
    //     */
    //    getLowestUnassignedLocationFields(): LocationField[] {
    //        let unassignedLowestFields = new Array<LocationField>();
    //
    //        for ( let i = ( this.universals.length - 1 ); i >= 0; i-- ) {
    //            let universal = this.universals[i];
    //
    //            if ( this.locationFields[universal.value] != null ) {
    //                let fields = this.locationFields[universal.value];
    //
    //                for ( let j = 0; j < fields.length; j++ ) {
    //                    let field = fields[j];
    //
    //                    if ( !field.assigned ) {
    //                        unassignedLowestFields.push( new LocationField( field, universal ) );
    //                    }
    //                }
    //
    //                if ( unassignedLowestFields.length > 0 ) {
    //                    return unassignedLowestFields;
    //                }
    //            }
    //        }
    //
    //        return unassignedLowestFields;
    //    }
    //
    //
    //    /**
    //     * Gets all unassigned location fields at a given universal level
    //     * 
    //     * @universalId - The universal level at which to search for unassigned fields
    //     */
    //    getUnassignedLocationFields( universalId: string ): Field[] {
    //        if ( this.locationFields.hasOwnProperty( universalId ) ) {
    //            let fields = this.locationFields[universalId];
    //            let unassignedFields: any = [];
    //
    //            for ( let j = 0; j < fields.length; j++ ) {
    //                let field = fields[j];
    //
    //                if ( !field.assigned ) {
    //                    unassignedFields.push( field );
    //                }
    //            }
    //
    //            return unassignedFields;
    //        }
    //
    //        return [];
    //    }
    //
    //    /** 
    //     * Gets the next location moving from low to high in the universal hierarchy. 
    //     * Valid returns include another field at the same universal level or a higher universal level.
    //     * 
    //     */
    //    getNextLocationField(): LocationField {
    //        for ( let i = ( this.universals.length - 1 ); i >= 0; i-- ) {
    //            let universal = this.universals[i];
    //
    //            if ( this.locationFields[universal.value] != null ) {
    //                let fields = this.locationFields[universal.value];
    //
    //                for ( let j = 0; j < fields.length; j++ ) {
    //                    let field = fields[j];
    //
    //                    if ( !field.assigned ) {
    //                        return new LocationField( field, universal );
    //                    }
    //                }
    //            }
    //        }
    //
    //        return null;
    //    }
    //
    //
    //    edit( attribute: LocationAttribute ): void {
    //        // This should only be hit if trying to edit when an existing edit session is in place. 
    //        if ( this.attribute && this.sheet.attributes.values[this.attribute.oid] ) {
    //            if ( this.sheet.attributes.values[this.attribute.oid].editing ) {
    //                this.sheet.attributes.values[this.attribute.oid].editing = false;
    //            }
    //
    //            // all fields that are in the current attribute (ui widget) should be set back to assigned
    //            // which creates a save type behavior for the current state of the attribute.
    //            this.setFieldAssigned();
    //        }
    //
    //        if ( this.attribute == null ) {
    //            this.attribute = this.createNewAttribute();
    //        }
    //
    //        Object.assign( this.attribute, attribute );
    //
    //        this.sheet.attributes.values[this.attribute.oid].editing = true;
    //
    //        this.unassignLocationFields();
    //
    //        let locFieldsSelectedButNotYetAssigned = this.getLocationFieldsSelectedInWidget();
    //        this.refreshUnassignedFields( locFieldsSelectedButNotYetAssigned );
    //
    //        let fieldLabel = this.attribute.fields[attribute.universal];
    //        let field = this.getField( fieldLabel );
    //
    //        // EXCLUDEed fields should be skipped
    //        if ( field ) {
    //            this.setUniversalOptions( field );
    //        }
    //    }
    //
    //
    //    /**
    //     * Sets all this.locationFields to false if the location field is part of this.attribute.fields.
    //     */
    //    unassignLocationFields(): void {
    //        for ( let key in this.attribute.fields ) {
    //            if ( this.attribute.fields.hasOwnProperty( key ) ) {
    //                let attributeFieldLabel = this.attribute.fields[key];
    //                let locFields = this.locationFields[key];
    //
    //                for ( let i = 0; i < locFields.length; i++ ) {
    //                    let locField = locFields[i];
    //
    //                    if ( locField.label === attributeFieldLabel ) {
    //                        locField.assigned = false;
    //                    }
    //                }
    //            }
    //        }
    //    }
    //
    //
    //    getLocationFieldsSelectedInWidget(): string[] {
    //        let unassignedFields: any = [];
    //
    //        for ( let key in this.attribute.fields ) {
    //            if ( this.attribute.fields.hasOwnProperty( key ) ) {
    //                let attributeFieldLabel = this.attribute.fields[key];
    //                let locFields = this.locationFields[key];
    //
    //                for ( let i = 0; i < locFields.length; i++ ) {
    //                    let locField = locFields[i];
    //
    //                    // making sure to only add fields that are part of this.attribute
    //                    if ( locField.label === attributeFieldLabel && !locField.assigned ) {
    //                        unassignedFields.push( locField.label );
    //                    }
    //                }
    //            }
    //        }
    //
    //        return unassignedFields;
    //    }
    //
    //
    //    /**
    //     * Gets a field from sheet.fields by label
    //     * 
    //     * @label = label of a field 
    //     */
    //    getField( label: string ): Field {
    //        for ( let j = 0; j < this.sheet.fields.length; j++ ) {
    //            let field = this.sheet.fields[j];
    //
    //            if ( field.label === label ) {
    //                return field;
    //            }
    //        }
    //
    //        return null;
    //    }
    //
    //    /**
    //     * Populate the unassigned field array from sheet.fields
    //     * 
    //     * @selectedFields - fields that are to be excluded from the unassignedFields array. Typically this is because they are set in the 
    //     *            location field attribute by the user in the UI and that location card has not yet been set. Keeping these fields out
    //     *            of the unassignedFields array ensures the fields aren't displayed as unassigned in the UI.
    //     */
    //    refreshUnassignedFields( selectedFields: string[] ): void {
    //        this.unassignedFields = [];
    //
    //        if ( this.attribute != null ) {
    //            for ( let i = 0; i < this.sheet.fields.length; i++ ) {
    //                let field = this.sheet.fields[i];
    //
    //                if ( field.type == 'LOCATION' && !field.assigned && field.name != this.attribute.label && selectedFields.indexOf( field.name ) === -1 ) {
    //                    this.unassignedFields.push( field );
    //                }
    //            }
    //        }
    //    }
    //
    //    /**
    //     * Remove attribute from the sheet 
    //     * 
    //     * @attribute - attribute to remove
    //     */
    //    remove( attribute: LocationAttribute ): void {
    //        if ( this.sheet.attributes.values[attribute.oid] ) {
    //
    //            delete this.sheet.attributes.values[attribute.oid];
    //            this.sheet.attributes.ids.splice( this.sheet.attributes.ids.indexOf( attribute.oid ), 1 );
    //
    //            this.setFieldAssigned();
    //
    //            if ( !this.attribute ) {
    //                this.newAttribute();
    //            }
    //            else {
    //                this.refreshUnassignedFields( [] );
    //            }
    //        }
    //    }
    //
    //    createNewAttribute(): LocationAttribute {
    //        let attribute = new LocationAttribute();
    //        attribute.label = '';
    //        attribute.name = '';
    //        attribute.universal = '';
    //        attribute.oid = '';
    //        attribute.fields = {};
    //        attribute.useCoordinatesForLocationAssignment = false;
    //        attribute.coordinatesForLocationAssignmentOptions = this.getCoordinateFieldsFromSource();
    //        attribute.latForLocationAssignment = null;
    //        attribute.longForLocationAssignment = null;
    //
    //        return attribute;
    //    }
    //
    //    /**
    //     * Create a new attribute from a location field in the source data
    //     * 
    //     * @attribute - location field that is being constructed. 
    //     *         It corresponds to a configurable location card on the UI where the user can set the location.
    //     */
    //    newAttribute(): void {
    //
    //        if ( this.attribute ) {
    //            if ( this.attribute.oid === '' ) {
    //                this.attribute.oid = this.idService.generateId();
    //
    //                this.sheet.attributes.ids.push( this.attribute.oid );
    //                this.sheet.attributes.values[this.attribute.oid] = new LocationAttribute();
    //            }
    //
    //            let attributeInSheet = this.sheet.attributes.values[this.attribute.oid];
    //            attributeInSheet.editing = false;
    //
    //            // copy the properties from the attribute (cofigurable widget in the ui) to sheet.attributes
    //            // this.attribute is coppied to --> attributeInSheet
    //            Object.assign( attributeInSheet, this.attribute );
    //
    //            // Set the assigned prop in sheet.fields if an attribute stored in sheet.attributes.values
    //            // also references the field.  This means that a saved attribute has claimed that field.
    //            this.setFieldAssigned();
    //        }
    //
    //        let nextLocation = this.getNextLocationField();
    //
    //        // This typically passes when a user manually sets an attribute and 
    //        // there are more location fields yet to be set
    //        if ( nextLocation ) {
    //            let field = nextLocation.field;
    //            let universal = nextLocation.universal;
    //
    //            this.attribute = this.createNewAttribute();
    //            this.attribute.label = field.label;
    //            this.attribute.universal = universal.value;
    //
    //            this.addField( field );
    //            this.setUniversalOptions( field );
    //            this.refreshUnassignedFields( [] );
    //        }
    //        else {
    //            this.refreshUnassignedFields( [] );
    //            this.attribute = null;
    //        }
    //    }
    //
    //    /**
    //     * Try to auto build the location field if there is only one field option per universal
    //     * 
    //     * Rules:
    //     *  1. Attempt to auto-assign context fields if there is ONLY one lowest level universal
    //     *  2. Auto-assign a single context field only if there is a single option
    //     *  
    //     */
    //    setLocationFieldAutoAssignment(): void {
    //
    //        let lowestLevelUnassignedLocationFields = this.getLowestUnassignedLocationFields();
    //        let lowestLevelUnassignedUniversal: any = null;
    //
    //        if ( lowestLevelUnassignedLocationFields.length > 0 ) {
    //            lowestLevelUnassignedUniversal = lowestLevelUnassignedLocationFields[0].universal.value;
    //        }
    //
    //        if ( lowestLevelUnassignedLocationFields.length === 1 ) {
    //
    //            for ( let j = 0; j < lowestLevelUnassignedLocationFields.length; j++ ) {
    //                let field = lowestLevelUnassignedLocationFields[j].field;
    //
    //                // construct the initial model for a location field
    //                this.attribute = this.createNewAttribute();
    //                this.attribute.label = field.label;
    //                this.attribute.name = field.name;
    //                this.attribute.universal = field.universal;
    //
    //                // add the targetLocationField.field (remember, it's from the source data) 
    //                // to the new location field (i.e. attribute)
    //                this.addField( field );
    //
    //                // sets all valid universal options (excluding the current universal) for this location field
    //                this.setUniversalOptions( field );
    //
    //                // There is only one or no universal options (i.e. context locations) so just set the field
    //                // to save a click for the user
    //                if ( this.universalOptions.length > 0 ) {
    //                    // Attempting auto-assignment of context fields
    //                    this.constructContextFieldsForAttribute( field );
    //                }
    //            }
    //        }
    //        // There are more than one lowest level un-assigned fields so lets not assume we can guess context fields.
    //        else if ( lowestLevelUnassignedLocationFields.length > 1 ) {
    //            for ( let j = 0; j < lowestLevelUnassignedLocationFields.length; j++ ) {
    //                let field = lowestLevelUnassignedLocationFields[j].field;
    //
    //                // construct the initial model for a location field
    //                this.attribute = this.createNewAttribute();
    //                this.attribute.label = field.label;
    //                this.attribute.name = field.name;
    //                this.attribute.universal = field.universal;
    //
    //                // add the targetLocationField.field (remember, it's from the source data) 
    //                // to the new location field (i.e. attribute)
    //                this.addField( field );
    //
    //                // sets all valid universal options (excluding the current universal) for this location field
    //                this.setUniversalOptions( field );
    //
    //                this.refreshUnassignedFields( [field.name] );
    //
    //                break;
    //            }
    //        }
    //        else if ( lowestLevelUnassignedLocationFields.length == 0 ) {
    //            this.attribute = null;
    //        }
    //    }
    //
    //
    //    /**
    //     * Construct all possible context fields for a given target field.
    //     * NOTE: there is an assumption that this will only be called for a lowest level univeral field
    //     * 
    //     * @field - the target field to which context fields would be built from. This is typically a field
    //     *       in the lowest level universal of a sheet.
    //     */
    //    constructContextFieldsForAttribute( field: Field ) {
    //        let unassignedLocationFieldsForTargetFieldUniversal = this.getUnassignedLocationFields( field.universal );
    //
    //        if ( this.universalOptions.length > 0 && unassignedLocationFieldsForTargetFieldUniversal.length === 1 ) {
    //            let fieldsSetToUniversalOptions = new Array<string>();
    //
    //            for ( let i = this.universalOptions.length; i--; ) {
    //                let universalOption = this.universalOptions[i];
    //                let unassignedLocationFieldsForThisUniversal = this.getUnassignedLocationFields( universalOption.value );
    //
    //                // Set the field ONLY if there is a single option per universal
    //                if ( unassignedLocationFieldsForThisUniversal.length === 1 ) {
    //                    this.addField( unassignedLocationFieldsForThisUniversal[0] );
    //
    //                    fieldsSetToUniversalOptions.push( unassignedLocationFieldsForThisUniversal[0].name );
    //                }
    //            }
    //
    //            this.refreshUnassignedFields( fieldsSetToUniversalOptions );
    //        }
    //    }
    //
    //    addField( field: Field ): void {
    //        this.attribute.fields[field.universal] = field.label;
    //    }
    //
    //    /**
    //     * Sets the valid universal options for a given field.
    //     * 
    //     * Valid options are:
    //     *   1.  universals that are assigned by the user in the source data 
    //     *   2.  universals with fields that are not yet assigned
    //     */
    //    setUniversalOptions( field: Field ): void {
    //        this.universalOptions = [];
    //        let valid = true;
    //
    //        for ( let i = 0; i < this.universals.length; i++ ) {
    //            let universal = this.universals[i];
    //            let unassignedFieldsForThisUniversal = this.getUnassignedLocationFields( universal.value );
    //
    //            if ( universal.value == field.universal ) {
    //                valid = false;
    //            }
    //            else if ( valid && unassignedFieldsForThisUniversal.length > 0 ) {
    //                this.universalOptions.push( universal );
    //            }
    //        }
    //    }
    //
    //
    //    /**
    //     * Sets all sheet.fields in a sheet to isAssigned = true if the field is assigned to a sheet attribute
    //     * and is a location field.
    //     * 
    //     */
    //    setFieldAssigned(): void {
    //        for ( let i = 0; i < this.sheet.fields.length; i++ ) {
    //            let field = this.sheet.fields[i]
    //
    //            if ( field.type == 'LOCATION' ) {
    //                field.assigned = this.isAssigned( field );
    //            }
    //            else {
    //                field.assigned = false;
    //            }
    //        }
    //    }
    //
    //
    //    /**
    //     * Check if a field is assigned to a sheet attribute 
    //     * 
    //     * @field - the field to check
    //     * 
    //     * isAssigned = true if the sheet attribute fields has a field with a corresponding field label
    //     */
    //    isAssigned( field: Field ): boolean {
    //        for ( let i = 0; i < this.sheet.attributes.ids.length; i++ ) {
    //            let oid = this.sheet.attributes.ids[i];
    //            let attribute = this.sheet.attributes.values[oid];
    //
    //            for ( let key in attribute.fields ) {
    //                if ( attribute.fields.hasOwnProperty( key ) ) {
    //                    if ( attribute.fields[key] == field.label ) {
    //                        return true;
    //                    }
    //                }
    //            }
    //        }
    //
    //        return false;
    //    }
    //
    //    change( selectedFields: { [key: string]: string } ): void {
    //        let selectedFieldsArr: any = [];
    //
    //        for ( let key in selectedFields ) {
    //            if ( selectedFields.hasOwnProperty( key ) && selectedFields[key] !== "EXCLUDE" ) {
    //                selectedFieldsArr.push( selectedFields[key] );
    //            }
    //        }
    //
    //        this.refreshUnassignedFields( selectedFieldsArr );
    //    }
    //
    //
    //    localValidate( value: string, config: string ): { [key: string]: any } {
    //        if ( config === 'label' ) {
    //            return this.validateLabel( value );
    //        }
    //
    //        return null;
    //    }
    //
    //
    //    validateLabel( label: string ): { [key: string]: any } {
    //        if ( this.sheet != null ) {
    //            let count = 0;
    //
    //            for ( let i = 0; i < this.sheet.fields.length; i++ ) {
    //                let field = this.sheet.fields[i];
    //
    //                if ( field.type != 'LOCATION' && field.label == label ) {
    //                    count++;
    //                }
    //            }
    //
    //            for ( let i = 0; i < this.sheet.attributes.ids.length; i++ ) {
    //                let oid = this.sheet.attributes.ids[i];
    //                let attribute = this.sheet.attributes.values[oid];
    //
    //                if ( attribute.label === label && !attribute.editing ) {
    //                    count++;
    //                }
    //                if ( this.attribute && this.attribute.label === attribute.label && !attribute.editing ) {
    //                    count++;
    //                }
    //            }
    //
    //            if ( count > 0 ) {
    //                return { unique: false };
    //            }
    //        }
    //
    //        return null;
    //    }
}
