import {AfterViewInit, Component, ElementRef, Input, ViewChild} from "@angular/core";
@Component({
  selector: 'app-canvas-camera',
  template:   '<video #cameraVideo autoplay></video>' + '<canvas #cameraCanvas id="cameraCanvas" style="margin-bottom: 50px;"></canvas>',
  styleUrls: ['app.canvas-camera.scss']
})
export class CanvasCameraComponent implements AfterViewInit {
  @ViewChild('cameraCanvas') public cameraCanvas: ElementRef;
  @ViewChild('cameraVideo') public cameraVideo: ElementRef;

  //Height & Width for the canvas
  @Input() public width = 800;
  @Input() public height = 600;
  @Input() public strokeWidth = 10;

  private cx: CanvasRenderingContext2D;
  private canvasEl: HTMLCanvasElement;
  private videoEl: HTMLVideoElement;

  ngAfterViewInit() {
    this.canvasEl = this.cameraCanvas.nativeElement;
    this.cx = this.canvasEl.getContext('2d');
    this.videoEl = this.cameraVideo.nativeElement;

    //set width & height
    this.canvasEl.width = this.width;
    this.canvasEl.height = this.height;
    this.videoEl.width = this.width;
    this.videoEl.height = this.height;
    this.videoEl.style.display = 'none';

    navigator.getUserMedia({video: true}, stream => {this.videoEl.src = URL.createObjectURL(stream);}, function () {});

    setInterval(() => {
      this.cx.drawImage(this.videoEl, 0, 0, this.width, this.height);
    }, 50);
  }
}
