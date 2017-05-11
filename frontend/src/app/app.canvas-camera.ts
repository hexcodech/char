import {AfterViewInit, Component, ElementRef, Input, NgZone, OnInit, ViewChild} from "@angular/core";
import * as _ from "lodash";
import {SocketIO} from "./services/socket-io";
declare const tracking: any;

@Component({
  selector: 'app-canvas-camera',
  template:   '<video #cameraVideo id="cameraVideo" preload autoplay loop muted></video>' + '<canvas #cameraCanvas id="cameraCanvas" style="margin-bottom: 50px;"></canvas>',
  styleUrls: ['app.canvas-camera.scss']
})
export class CanvasCameraComponent implements AfterViewInit, OnInit {
  @ViewChild('cameraCanvas') public cameraCanvas: ElementRef;
  @ViewChild('cameraVideo') public cameraVideo: ElementRef;

  //Height & Width for the canvas
  @Input() public width = 320;
  @Input() public height = 240;

  private cx: CanvasRenderingContext2D;
  private canvasEl: HTMLCanvasElement;
  private videoEl: HTMLVideoElement;
  private tracker;

  private image;
  private rectData = {
    x: null,
    y: null,
    height: null,
    width: null,
  };

  constructor(private socketIO: SocketIO, private ngZone: NgZone) {
  }

  ngOnInit()  {
    this.tracker = new tracking.ObjectTracker('face');
    this.tracker.setInitialScale(4);
    this.tracker.setStepSize(2);
    this.tracker.setEdgesDensity(0.1);

    tracking.track('#cameraVideo', this.tracker, { camera: true });
    this.tracker.on('track', this.trackFace);
  }

  ngAfterViewInit() {
    this.canvasEl = this.cameraCanvas.nativeElement;
    this.cx = this.canvasEl.getContext('2d');
    this.cx.beginPath();
    this.videoEl = this.cameraVideo.nativeElement;


    //set width & height
    this.canvasEl.width = this.width;
    this.canvasEl.height = this.height;
    this.videoEl.width = this.width;
    this.videoEl.height = this.height;
    //navigator.getUserMedia({video: true}, stream => {this.videoEl.src = URL.createObjectURL(stream);}, function () {});
    /*setInterval(() => {
      this.cx.drawImage(this.videoEl, 0, 0, this.width, this.height);
    }, 50);*/
  }

  private trackFace = (event) => {
    if(event.data.length > 0) {
      this.cx.drawImage(this.videoEl, 0, 0, this.width, this.height);
      this.image = this.canvasEl.toDataURL();
    }

    this.cx.clearRect(0, 0, this.width, this.height);
    event.data.forEach((rect) => {
      this.cx.strokeStyle = '#a64ceb';
      this.cx.strokeRect(rect.x, rect.y, rect.width, rect.height);
      this.cx.font = '11px Helvetica';
      this.cx.fillStyle = "#fff";
      this.cx.fillText('x: ' + rect.x + 'px', rect.x + rect.width + 5, rect.y + 11);
      this.cx.fillText('y: ' + rect.y + 'px', rect.x + rect.width + 5, rect.y + 22);

      this.rectData.x = rect.x; this.rectData.y = rect.y;
      this.rectData.width = rect.width; this.rectData.height = rect.height;
    });

    if(event.data.length > 0)
      this.sendBase64PNGAndDimensions();
  }

  private sendBase64PNGAndDimensions = _.throttle(() => {
    /*this.socketIO.sendMessage('read', {
     image: this.canvas.nativeElement.toDataURL(),
     x: ((this.borderPos.maxX - this.borderPos.minX) / 2) + this.borderPos.minX,
     y: ((this.borderPos.maxY - this.borderPos.minY) / 2) + this.borderPos.minY,
     d: Math.max(this.borderPos.maxX - this.borderPos.minX, this.borderPos.maxY - this.borderPos.minY)
     });*/
    this.socketIO.sendMessage('read', {
      image: this.image,
      x: this.rectData.x,
      y: this.rectData.y,
      width: this.rectData.width,
      height: this.rectData.height,
    });
  }, 250);
}
