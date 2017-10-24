import { AfterViewInit, Component, ElementRef, ViewChild } from '@angular/core';
import olFeature from 'ol/feature';
import olLayer from 'ol/layer/layer';
import { OlMapObject } from '../portal-core-ui/service/openlayermap/ol-map-object';


@Component({
    selector: 'app-ol-map',
    template: `
    <div #mapElement id="map" class="height-full width-full"> </div>
    `
    // The "#" (template reference variable) matters to access the map element with the ViewChild decorator!
})

export class OlMapComponent implements AfterViewInit {
    // This is necessary to access the html element to set the map target (after view init)!
    @ViewChild('mapElement') mapElement: ElementRef;



    constructor(public olMapObject: OlMapObject) {
    }


    // After view init the map target can be set!
    ngAfterViewInit() {
        this.olMapObject.getMap().setTarget(this.mapElement.nativeElement.id);
    }
}
