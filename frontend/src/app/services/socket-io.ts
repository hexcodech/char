import { Injectable } from '@angular/core';
import 'rxjs/add/operator/map';
import * as io from 'socket.io-client';

@Injectable()
export class SocketIO {
  private url = '//localhost:6969';
  private socket;

  constructor() {
    if (!this.socket) {
      this.initializeSocket();
    }
  }
  private initializeSocket() {
    this.socket = io(this.url, {
      secure: true
    });
  }
  public getSocket()  {
    return this.socket;
  }
  public sendMessage(channel, message) {
    this.socket.emit(channel, message);
  }
}
