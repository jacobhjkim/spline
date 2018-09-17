/*
 * Copyright 2017 Barclays Africa Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {Component, EventEmitter, Input, OnInit, Output} from "@angular/core";
import {IPersistedDatasetDescriptor} from '../../../generated-ts/lineage-model';
import {ScrollEvent} from 'ngx-scroll-event';

@Component({
    selector: "dataset-results",
    template: `
        <ul class="nav nav-pills nav-stacked"
            detect-scroll
            [bottomOffset]="300"
            (onScroll)="scrollEvent.emit($event)">
            <li role="presentation"
                *ngFor="let d of descriptors"
                (click)="selectLineage.emit(d.datasetId)">
                <a href="javascript:void(0)">
                    <b>{{d.path}}</b>
                    <br/>
                    <span class="small" title="Spark application name">{{d.appName}}</span>
                    <br>
                    <!-- FIXME ensure UTC -->
                    <!-- FIXME acquire more accurate write timestamp -->
                    <span class="small" title="Approximate write timestamp">{{ toDateString(d.timestamp) }}</span>
                </a>
            </li>
        </ul>
    `,
    // styleUrls: ["dataset-browser.component.less"]
})
export class DatasetResultsComponent {

    @Input()
    descriptors: Array<IPersistedDatasetDescriptor>

    @Output()
    scrollEvent = new EventEmitter<ScrollEvent>()

    @Output()
    selectLineage = new EventEmitter<string>()

    toDateString(timestamp: number): string {
        return new Date(timestamp).toUTCString()
    }

}