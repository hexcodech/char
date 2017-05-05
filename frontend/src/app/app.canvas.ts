import { Component, Input, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import { SocketIO } from './services/socket-io';
import * as _ from "lodash";

@Component({
  selector: 'app-canvas',
  template: '<canvas #canvas id="canvas"></canvas><canvas #canvasOverlay id="canvasOverlay"></canvas>',
  styleUrls: ['app.canvas.scss']
})
export class CanvasComponent implements AfterViewInit {
  //Reference to the canvas from the template
  @ViewChild('canvas') public canvas: ElementRef;
  @ViewChild('canvasOverlay') public canvasOverlay: ElementRef;

  //Height & Width for the canvas
  @Input() public width = 400;
  @Input() public height = 400;
  @Input() public strokeWidth = 10;

  private cx: CanvasRenderingContext2D;
  private cxOverlay: CanvasRenderingContext2D;
  private canvasEl: HTMLCanvasElement;
  private canvasOverlayEl: HTMLCanvasElement;
  private socketIO;
  private borderPos = {
    minX: -1,
    minY: -1,
    maxX: -1,
    maxY: -1,
  };

  constructor(socketIO: SocketIO) {
    this.socketIO = socketIO;
  }

  ngAfterViewInit() {
    //get context
    this.canvasEl = this.canvas.nativeElement;
    this.canvasOverlayEl = this.canvasOverlay.nativeElement;
    this.cx = this.canvasEl.getContext('2d');
    this.cxOverlay = this.canvasOverlayEl.getContext('2d');

    //set width & height
    this.canvasEl.width = this.width;
    this.canvasEl.height = this.height;
    this.canvasOverlayEl.width = this.width;
    this.canvasOverlayEl.height = this.height;

    //set draw settings
    this.cx.lineWidth = this.strokeWidth;
    this.cx.lineCap = 'round';
    this.cx.strokeStyle = '#000000';
    this.cxOverlay.strokeStyle = '#000000';

    this.cx.fillStyle = "#ffffff";
    this.cx.fillRect(0,0,this.width, this.height);

    //capture mouse events
    this.captureEvents(this.canvasEl);
  }

  private captureEvents(canvasEl: HTMLCanvasElement)  {
    canvasEl.addEventListener('touchstart', this.touchHandler, true);
    canvasEl.addEventListener('touchmove', this.touchHandler, true);
    canvasEl.addEventListener('touchend', this.touchHandler, true);
    canvasEl.addEventListener('touchcancel', this.touchHandler, true);
    canvasEl.addEventListener('touchleave', this.touchHandler, true);

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
      .subscribe(this.drawSubscriber);

  }

  private drawSubscriber = (res: [MouseEvent, MouseEvent]) => {
    const rect = this.canvasEl.getBoundingClientRect();

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
  };

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
    this.cxOverlay.beginPath();

    if(prevPos) {
      this.cx.moveTo(prevPos.x, prevPos.y); //from
      this.cx.lineTo(currentPos.x, currentPos.y); //to
      this.cx.stroke(); //apply the stroke settings set in ngAfterViewInit

      if(currentPos.x < this.borderPos.minX + this.strokeWidth || this.borderPos.minX == -1) {
        this.borderPos.minX = currentPos.x - this.strokeWidth;
        this.borderPos.minX = this.borderPos.minX < 0 ? 0 : this.borderPos.minX;
      }
      if(currentPos.y < this.borderPos.minY + this.strokeWidth || this.borderPos.minY == -1) {
        this.borderPos.minY = currentPos.y - this.strokeWidth;
        this.borderPos.minY = this.borderPos.minY < 0 ? 0 : this.borderPos.minY;
      }
      if(currentPos.x > this.borderPos.maxX - this.strokeWidth || this.borderPos.maxX == -1) {
        this.borderPos.maxX = currentPos.x + this.strokeWidth;
        this.borderPos.maxX = this.borderPos.maxX > this.width ? this.width : this.borderPos.maxX;
      }
      if(currentPos.y > this.borderPos.maxY - this.strokeWidth || this.borderPos.maxY == -1) {
        this.borderPos.maxY = currentPos.y + this.strokeWidth;
        this.borderPos.maxY = this.borderPos.maxY > this.height ? this.height : this.borderPos.maxY;
      }

      this.cxOverlay.clearRect(0,0,this.width, this.height);
      this.cxOverlay.rect(this.borderPos.minX, this.borderPos.minY, this.borderPos.maxX-this.borderPos.minX, this.borderPos.maxY-this.borderPos.minY);
      this.cxOverlay.stroke();
    }
  }

  private touchHandler(event){
    let touches = event.changedTouches,
      first = touches[0],
      type = "";
    switch(event.type)
    {
      case "touchstart": type = "mousedown"; break;
      case "touchmove":  type = "mousemove"; event.preventDefault(); break;
      case "touchend":   type = "mouseup";   break;
      case "touchleave": type = "mouseout";  break;
      default:           return;
    }

    // initMouseEvent(type, canBubble, cancelable, view, clickCount,
    //                screenX, screenY, clientX, clientY, ctrlKey,
    //                altKey, shiftKey, metaKey, button, relatedTarget);

    let simulatedEvent = document.createEvent("MouseEvent");
    simulatedEvent.initMouseEvent(type, true, true, window, 1,
      first.screenX, first.screenY,
      first.clientX, first.clientY, false,
      false, false, false, 0/*left*/, null);

    first.target.dispatchEvent(simulatedEvent);
  }

  private sendBase64PNG = _.throttle(() => {
    this.socketIO.sendMessage('read', {
      image: this.canvas.nativeElement.toDataURL(),
      x: ((this.borderPos.maxX - this.borderPos.minX) / 2) + this.borderPos.minX,
      y: ((this.borderPos.maxY - this.borderPos.minY) / 2) + this.borderPos.minY,
      d: Math.max(this.borderPos.maxX - this.borderPos.minX, this.borderPos.maxY - this.borderPos.minY)
    });
  }, 250);

  public clearRect() {
    if(!this.cx)
      return;

    this.cx.fillStyle = "#ffffff";
    this.cx.fillRect(0,0,this.width, this.height);
    this.cxOverlay.clearRect(0,0,this.width, this.height);
    this.borderPos.minX = -1;
    this.borderPos.minY = -1;
    this.borderPos.maxX = -1;
    this.borderPos.maxY = -1;
  }
}
