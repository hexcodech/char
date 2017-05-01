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
  public lastData;
  public maxData;

  constructor(socketIO: SocketIO) {
    this.socketIO = socketIO;
  }

  ngOnInit()  {
    this.socketIO.getSocket().on('read', (data) =>  {
      data = JSON.parse(data);
      console.log(data);
      this.lastData = data;
      this.maxData = data.indexOf(Math.max(...data));
    });
  }
}
