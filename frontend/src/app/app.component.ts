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
  public lastData;
  public maxData;

  constructor(socketIO: SocketIO) {
    this.socketIO = socketIO;
  }

  ngOnInit()  {
    this.socketIO.getSocket().on('read', (data) =>  {
      data = JSON.parse(data);
      this.lastData = [];
      for(let key in data)  {
        this.lastData.push([parseInt(key), data[key]])
      }
      this.lastData.sort(function(a,b) {
          return b[1]-a[1];
      });
      this.maxData = data.indexOf(Math.max(...data));
    });
  }
}
