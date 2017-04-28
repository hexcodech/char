import { Component, Input, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import { SocketIO } from './services/socket-io';
import * as _ from "lodash";

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
  private socketIO;

  constructor(socketIO: SocketIO) {
    this.socketIO = socketIO;
  }

  ngAfterViewInit() {
    //get context
    const canvasEl: HTMLCanvasElement = this.canvas.nativeElement;
    this.cx = canvasEl.getContext('2d');

    //set width & height
    canvasEl.width = this.width;
    canvasEl.height = this.height;

    //set draw settings
    this.cx.lineWidth = 10;
    this.cx.lineCap = 'round';
    this.cx.strokeStyle = '#000000';

    this.cx.fillStyle = "#ffffff";
    this.cx.fillRect(0,0,this.width, this.height);

    //capture mouse events
    this.captureEvents(canvasEl);
  }

  private captureEvents(canvasEl: HTMLCanvasElement)  {
    //Turn js event to an observable
    Observable
      .fromEvent(canvasEl, 'mousedown')
      .switchMap((event) => {
        //We got a click now record all mouse moves till the click stops (or off canvas) and get pairs of values to generate a line
        return Observable
          .fromEvent(canvasEl, 'mousemove')
          .takeUntil(Observable.fromEvent(canvasEl, 'mouseup'))
          .takeUntil(Observable.fromEvent(canvasEl, 'mouseout'))
          .pairwise()
      })
      .subscribe((res: [MouseEvent, MouseEvent]) => {
        const rect = canvasEl.getBoundingClientRect();

        //current & previous pos - offset
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
        //and then send...
        this.sendBase64PNG();
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

  private sendBase64PNG = _.throttle(() => {
    this.socketIO.sendMessage('read', this.canvas.nativeElement.toDataURL());
    console.log(this.canvas.nativeElement.toDataURL());
  }, 250);

  public clearRect() {
    if(!this.cx)
      return;

    this.cx.fillStyle = "#ffffff";
    this.cx.fillRect(0,0,this.width, this.height);
  }
}
