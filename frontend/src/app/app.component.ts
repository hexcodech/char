import {Component, OnInit, ViewChild} from '@angular/core';
import { SocketIO } from './services/socket-io';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  @ViewChild('canvas') canvas;

  private socketIO;
  private lastShutdownClick;
  private lastData;
  private maxData;

  constructor(socketIO: SocketIO) {
    this.socketIO = socketIO;
  }

  ngOnInit()  {
    this.socketIO.getSocket().on('read', (data) =>  {
      console.log(data);
      this.lastData = data;
      this.maxData = data.indexOf(Math.max(...data));
    });
  }

  private shutdownServer()  {
    if(!this.lastShutdownClick)
      this.lastShutdownClick = Date.now();

    if((Date.now() - this.lastShutdownClick)<=500)
      this.socketIO.sendMessage('stop hammertime', '');
  }
}
