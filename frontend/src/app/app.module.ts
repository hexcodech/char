import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { AppComponent } from './app.component';
import { CanvasComponent } from './app.canvas';

import { SocketIO } from './services/socket-io';
import {CanvasCameraComponent} from "./app.canvas-camera";

@NgModule({
  declarations: [
    AppComponent,
    CanvasComponent,
    CanvasCameraComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule
  ],
  providers: [
    SocketIO
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
