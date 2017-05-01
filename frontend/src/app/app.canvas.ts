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
  private canvasEl: HTMLCanvasElement;
  private socketIO;

  constructor(socketIO: SocketIO) {
    this.socketIO = socketIO;
  }

  ngAfterViewInit() {
    //get context
    this.canvasEl = this.canvas.nativeElement;
    this.cx = this.canvasEl.getContext('2d');

    //set width & height
    this.canvasEl.width = this.width;
    this.canvasEl.height = this.height;

    //set draw settings
    this.cx.lineWidth = 10;
    this.cx.lineCap = 'round';
    this.cx.strokeStyle = '#000000';

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

    if(prevPos) {
      this.cx.moveTo(prevPos.x, prevPos.y); //from
      this.cx.lineTo(currentPos.x, currentPos.y); //to
      this.cx.stroke(); //apply the stroke settings set in ngAfterViewInit
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
    this.socketIO.sendMessage('read', this.canvas.nativeElement.toDataURL());
  }, 250);

  public clearRect() {
    if(!this.cx)
      return;

    this.cx.fillStyle = "#ffffff";
    this.cx.fillRect(0,0,this.width, this.height);
  }
}
