import { Component, Input, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { Observable } from 'rxjs/Rx';

@Component({
  selector: 'app-canvas',
  template: '<canvas #canvas></canvas>',
  styleUrls: ['app.canvas.scss']
})
export class CanvasComponent implements AfterViewInit {
  //Reference to the canvas from the template
  @ViewChild('canvas') public canvas: ElementRef;

  //Height & Width for the canvas
  @Input() public width = 400;
  @Input() public height = 400;

  private cx: CanvasRenderingContext2D;

  ngAfterViewInit() {
    //get context
    const canvasEl: HTMLCanvasElement = this.canvas.nativeElement;
    this.cx = canvasEl.getContext('2d');

    //set width & height
    canvasEl.width = this.width;
    canvasEl.height = this.height;

    //set draw settings
    this.cx.lineWidth = 3;
    this.cx.lineCap = 'round';
    this.cx.strokeStyle = '#000000';

    //capture mouse events
    this.captureEvents(canvasEl);
  }

  private captureEvents(canvasEl: HTMLCanvasElement)  {
    //Turn js event to an observable
    Observable
      .fromEvent(canvasEl, 'mousedown')
      .switchMap((event) => {
        //We got a click now record all mouse moves till the click stops and get pairs of values to generate a line
        return Observable
          .fromEvent(canvasEl, 'mousemove')
          .takeUntil(Observable.fromEvent(canvasEl, 'mouseup'))
          .pairwise()
      })
      .subscribe((res: [MouseEvent, MouseEvent]) => {
        const rect = canvasEl.getBoundingClientRect();

        //current & previous pos + offset
        const prevPos = {
          x: res[0].clientX - rect.left,
          y: res[0].clientY - rect.top
        };
        const currentPos = {
          x: res[1].clientX - rect.left,
          y: res[1].clientY - rect.top
        };

        //let's draw
        this.drawOnCanvas(prevPos, currentPos);
      })
  }

  private drawOnCanvas(
    prevPos: {
      x: number,
      y: number
    },
    currentPos: {
      x: number,
      y: number
    }
  ) {
    //just to be sure...
    if(!this.cx)
      return;

    this.cx.beginPath();

    if(prevPos) {
      this.cx.moveTo(prevPos.x, prevPos.y); //from
      this.cx.lineTo(currentPos.x, currentPos.y); //to
      this.cx.stroke(); //apply the stroke settings set in ngAfterViewInit
    }
  }
}
