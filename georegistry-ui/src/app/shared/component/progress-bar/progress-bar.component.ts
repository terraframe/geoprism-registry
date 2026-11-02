///
/// Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
///
/// This file is part of Geoprism Registry(tm).
///
/// Geoprism Registry(tm) is free software: you can redistribute it and/or modify
/// it under the terms of the GNU Lesser General Public License as
/// published by the Free Software Foundation, either version 3 of the
/// License, or (at your option) any later version.
///
/// Geoprism Registry(tm) is distributed in the hope that it will be useful, but
/// WITHOUT ANY WARRANTY; without even the implied warranty of
/// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/// GNU Lesser General Public License for more details.
///
/// You should have received a copy of the GNU Lesser General Public
/// License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
///

import { afterNextRender, Component, ElementRef, OnDestroy, OnInit, signal } from '@angular/core';

import { Progress } from '@shared/model/progress';
import { ProgressService, IProgressListener } from '@shared/service';
import { ProgressbarModule } from 'ngx-bootstrap/progressbar';

interface OverlayArea {
  top: number;
  left: number;
  width: number;
  height: number;
}

@Component({
  selector: 'progress-bar',
  templateUrl: './progress-bar.component.html',
  styles: [
    /*
     * The overlay is fixed to the viewport so it stays put no matter how far
     * the page (#app-container) is scrolled, and it takes up no space in the
     * layout. Its position and size are matched to #app-container so the
     * sidebar stays uncovered and usable for navigation. Without an
     * #app-container ancestor it falls back to covering the whole viewport.
     */
    '.progress-overlay { background-color: rgba(204, 204, 204, 0.8); position: fixed; inset: 0; display: flex; align-items: center; justify-content: center; z-index: 99999 !important; }',
    '.progress-div { width: 50%; }',
  ],
  standalone: true,
  imports: [ProgressbarModule],
})
export class ProgressBarComponent implements OnInit, OnDestroy, IProgressListener {
  public showIndicator: boolean = true;

  public prog: Progress = {
    current: 0,
    total: 1,
    description: 'Initializing',
  };

  // A signal so updates from the ResizeObserver/resize callbacks are always rendered
  public area = signal<OverlayArea | null>(null);

  private container: HTMLElement | null = null;

  private resizeObserver: ResizeObserver | null = null;

  private onWindowResize = (): void => this.updateArea();

  constructor(
    private service: ProgressService,
    private el: ElementRef<HTMLElement>
  ) {
    // Wait until the host element is in the DOM so it can find its #app-container ancestor
    afterNextRender(() => this.observeContainer());
  }

  ngOnInit(): void {
    this.service.registerListener(this);
  }

  observeContainer(): void {
    this.container = this.el.nativeElement.closest<HTMLElement>('#app-container');

    if (this.container != null) {
      this.updateArea();

      // Fires when the sidebar is expanded/collapsed, since that resizes the content column
      this.resizeObserver = new ResizeObserver(() => this.updateArea());
      this.resizeObserver.observe(this.container);

      window.addEventListener('resize', this.onWindowResize);
    }
  }

  ngOnDestroy(): void {
    this.service.deregisterListener(this);

    if (this.resizeObserver != null) {
      this.resizeObserver.disconnect();
    }

    window.removeEventListener('resize', this.onWindowResize);
  }

  updateArea(): void {
    if (this.container != null) {
      const rect = this.container.getBoundingClientRect();

      this.area.set({ top: rect.top, left: rect.left, width: rect.width, height: rect.height });
    }
  }

  start(): void {
    this.prog = {
      current: 0,
      total: 1,
      description: 'Initializing',
    };

    this.showIndicator = true;
  }

  progress(progress: Progress): void {
    this.prog = progress;
  }

  complete(): void {
    this.showIndicator = false;
  }
}
