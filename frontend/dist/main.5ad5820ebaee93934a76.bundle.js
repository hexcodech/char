webpackJsonp([1,5],{"/fcW":function(t,n){function e(t){throw new Error("Cannot find module '"+t+"'.")}e.keys=function(){return[]},e.resolve=e,t.exports=e,e.id="/fcW"},0:function(t,n){},1:function(t,n,e){t.exports=e("x35b")},"1A80":function(t,n,e){"use strict";function l(t){return u._15(0,[(t()(),u._16(0,null,null,8,"tr",[],null,null,null,null,null)),(t()(),u._17(null,["\n            "])),(t()(),u._16(0,null,null,1,"td",[],null,null,null,null,null)),(t()(),u._17(null,["",""])),(t()(),u._17(null,["\n            "])),(t()(),u._16(0,null,null,2,"td",[],null,null,null,null,null)),(t()(),u._17(null,["","%"])),u._18(2),(t()(),u._17(null,["\n          "]))],null,function(t,n){t(n,3,0,n.context.$implicit[0]),t(n,6,0,u._19(n,6,0,t(n,7,0,u._20(n.parent,0),100*n.context.$implicit[1],"1.1-1")))})}function i(t){return u._15(0,[u._21(0,o.f,[u.c]),u._22(201326592,1,{canvas:0}),(t()(),u._16(0,null,null,48,"div",[["class","container"]],null,null,null,null,null)),(t()(),u._17(null,["\n  "])),(t()(),u._16(0,null,null,1,"h1",[],null,null,null,null,null)),(t()(),u._17(null,["Char"])),(t()(),u._17(null,["\n  "])),(t()(),u._16(0,null,null,32,"div",[["class","col-md-4"]],null,null,null,null,null)),(t()(),u._17(null,["\n      "])),(t()(),u._16(0,null,null,29,"div",[["class","center"]],null,null,null,null,null)),(t()(),u._17(null,["\n        "])),(t()(),u._16(0,null,null,1,"h1",[],null,null,null,null,null)),(t()(),u._17(null,["Our guess:"])),(t()(),u._17(null,["\n        "])),(t()(),u._16(0,null,null,2,"span",[["class","result"]],null,null,null,null,null)),(t()(),u._16(0,null,null,1,"b",[],null,null,null,null,null)),(t()(),u._17(null,["",""])),(t()(),u._17(null,["\n        "])),(t()(),u._16(0,null,null,0,"hr",[],null,null,null,null,null)),(t()(),u._17(null,["\n        "])),(t()(),u._16(0,null,null,1,"h3",[],null,null,null,null,null)),(t()(),u._17(null,["Other guesses:"])),(t()(),u._17(null,["\n        "])),(t()(),u._16(0,null,null,14,"table",[["class","table"]],null,null,null,null,null)),(t()(),u._17(null,["\n          "])),(t()(),u._16(0,null,null,12,"tbody",[],null,null,null,null,null)),(t()(),u._16(0,null,null,7,"tr",[],null,null,null,null,null)),(t()(),u._17(null,["\n            "])),(t()(),u._16(0,null,null,1,"td",[],null,null,null,null,null)),(t()(),u._17(null,["#"])),(t()(),u._17(null,["\n            "])),(t()(),u._16(0,null,null,1,"td",[],null,null,null,null,null)),(t()(),u._17(null,["Activation"])),(t()(),u._17(null,["\n          "])),(t()(),u._17(null,["\n          "])),(t()(),u._23(8388608,null,null,1,null,l)),u._24(401408,null,0,o.g,[u._0,u._1,u.t],{ngForOf:[0,"ngForOf"]},null),(t()(),u._17(null,["\n        "])),(t()(),u._17(null,["\n      "])),(t()(),u._17(null,["\n    "])),(t()(),u._17(null,["\n  "])),(t()(),u._17(null,["\n    "])),(t()(),u._16(0,null,null,7,"div",[["class","col-md-8"]],null,null,null,null,null)),(t()(),u._17(null,["\n      "])),(t()(),u._16(0,null,null,4,"div",[["class","center"]],null,null,null,null,null)),(t()(),u._17(null,["\n        "])),(t()(),u._16(0,null,null,1,"app-canvas-camera",[],null,null,null,a.a,a.b)),u._24(2154496,null,0,s.a,[c.a,u.h],null,null),(t()(),u._17(null,["\n      "])),(t()(),u._17(null,["\n    "])),(t()(),u._17(null,["\n"])),(t()(),u._17(null,["\n"]))],function(t,n){t(n,36,0,n.component.lastData),t(n,47,0)},function(t,n){t(n,16,0,n.component.maxData)})}function _(t){return u._15(0,[(t()(),u._16(0,null,null,1,"app-root",[],null,null,null,i,f)),u._24(57344,null,0,h.a,[c.a],null,null)],function(t,n){t(n,1,0)},null)}var r=e("l0Vc"),u=e("3j3K"),o=e("2Je8"),a=e("XUgf"),s=e("E7n8"),c=e("HjlW"),h=e("YWx4");e.d(n,"a",function(){return d});var p=[r.a],f=u._14({encapsulation:0,styles:p,data:{}}),d=u._25("app-root",h.a,_,{},{},[])},E7n8:function(t,n,e){"use strict";var l=e("3j3K"),i=e("M4fF"),_=(e.n(i),e("HjlW"));e.d(n,"a",function(){return r});var r=function(){function t(t,n){var e=this;this.socketIO=t,this.ngZone=n,this.width=320,this.height=240,this.rectData={x:null,y:null,height:null,width:null},this.trackFace=function(t){t.data.length>0&&(e.cx.drawImage(e.videoEl,0,0,e.width,e.height),e.image=e.canvasEl.toDataURL()),e.cx.clearRect(0,0,e.width,e.height),t.data.forEach(function(t){e.cx.strokeStyle="#a64ceb",e.cx.strokeRect(t.x,t.y,t.width,t.height),e.cx.font="11px Helvetica",e.cx.fillStyle="#fff",e.cx.fillText("x: "+t.x+"px",t.x+t.width+5,t.y+11),e.cx.fillText("y: "+t.y+"px",t.x+t.width+5,t.y+22),e.rectData.x=t.x,e.rectData.y=t.y,e.rectData.width=t.width,e.rectData.height=t.height}),t.data.length>0&&e.sendBase64PNGAndDimensions()},this.sendBase64PNGAndDimensions=i.throttle(function(){e.socketIO.sendMessage("read",{image:e.image,x:e.rectData.x,y:e.rectData.y,width:e.rectData.width,height:e.rectData.height})},250)}return t.prototype.ngOnInit=function(){this.tracker=new tracking.ObjectTracker("face"),this.tracker.setInitialScale(4),this.tracker.setStepSize(2),this.tracker.setEdgesDensity(.1),tracking.track("#cameraVideo",this.tracker,{camera:!0}),this.tracker.on("track",this.trackFace)},t.prototype.ngAfterViewInit=function(){this.canvasEl=this.cameraCanvas.nativeElement,this.cx=this.canvasEl.getContext("2d"),this.cx.beginPath(),this.videoEl=this.cameraVideo.nativeElement,this.canvasEl.width=this.width,this.canvasEl.height=this.height,this.videoEl.width=this.width,this.videoEl.height=this.height},t.ctorParameters=function(){return[{type:_.a},{type:l.h}]},t}()},HjlW:function(t,n,e){"use strict";var l=e("+pb+"),i=(e.n(l),e("DmT9"));e.n(i);e.d(n,"a",function(){return _});var _=function(){function t(){this.url="https://46.105.255.24:9316",this.socket||this.initializeSocket()}return t.prototype.initializeSocket=function(){this.socket=i(this.url,{secure:!0})},t.prototype.getSocket=function(){return this.socket},t.prototype.sendMessage=function(t,n){this.socket.emit(t,n)},t.ctorParameters=function(){return[]},t}()},Iksp:function(t,n,e){"use strict";e.d(n,"a",function(){return l});var l=function(){function t(){}return t}()},XUgf:function(t,n,e){"use strict";function l(t){return r._15(0,[r._22(201326592,1,{cameraCanvas:0}),r._22(201326592,2,{cameraVideo:0}),(t()(),r._16(0,[[2,0],["cameraVideo",1]],null,0,"video",[["autoplay",""],["id","cameraVideo"],["loop",""],["muted",""],["preload",""]],null,null,null,null,null)),(t()(),r._16(0,[[1,0],["cameraCanvas",1]],null,0,"canvas",[["id","cameraCanvas"],["style","margin-bottom: 50px;"]],null,null,null,null,null))],null,null)}function i(t){return r._15(0,[(t()(),r._16(0,null,null,1,"app-canvas-camera",[],null,null,null,l,s)),r._24(2154496,null,0,u.a,[o.a,r.h],null,null)],function(t,n){t(n,1,0)},null)}var _=e("gfxF"),r=e("3j3K"),u=e("E7n8"),o=e("HjlW");e.d(n,"b",function(){return s}),n.a=l;var a=[_.a],s=r._14({encapsulation:0,styles:a,data:{}});r._25("app-canvas-camera",u.a,i,{width:"width",height:"height"},{},[])},YWx4:function(t,n,e){"use strict";var l=e("HjlW");e.d(n,"a",function(){return i});var i=function(){function t(t){this.socketIO=t}return t.prototype.ngOnInit=function(){var t=this;this.socketIO.getSocket().on("read",function(n){n=JSON.parse(n),t.lastData=[];for(var e in n)t.lastData.push([parseInt(e),n[e]]);t.lastData.sort(function(t,n){return n[1]-t[1]}),t.maxData=n.indexOf(Math.max.apply(Math,n))})},t.ctorParameters=function(){return[{type:l.a}]},t}()},gfxF:function(t,n,e){"use strict";e.d(n,"a",function(){return l});var l=["#cameraCanvas[_ngcontent-%COMP%], #cameraVideo[_ngcontent-%COMP%]{position:absolute}#cameraCanvas[_ngcontent-%COMP%]{border:1px solid #000}"]},kZql:function(t,n,e){"use strict";e.d(n,"a",function(){return l});var l={production:!0}},kke6:function(t,n,e){"use strict";var l=e("3j3K"),i=e("Iksp"),_=e("2Je8"),r=e("Qbdm"),u=e("NVOs"),o=e("Fzro"),a=e("HjlW"),s=e("1A80");e.d(n,"a",function(){return p});var c=this&&this.__extends||function(){var t=Object.setPrototypeOf||{__proto__:[]}instanceof Array&&function(t,n){t.__proto__=n}||function(t,n){for(var e in n)n.hasOwnProperty(e)&&(t[e]=n[e])};return function(n,e){function l(){this.constructor=n}t(n,e),n.prototype=null===e?Object.create(e):(l.prototype=e.prototype,new l)}}(),h=function(t){function n(n){return t.call(this,n,[s.a],[s.a])||this}return c(n,t),Object.defineProperty(n.prototype,"_LOCALE_ID_12",{get:function(){return null==this.__LOCALE_ID_12&&(this.__LOCALE_ID_12=l.b(this.parent.get(l.c,null))),this.__LOCALE_ID_12},enumerable:!0,configurable:!0}),Object.defineProperty(n.prototype,"_NgLocalization_13",{get:function(){return null==this.__NgLocalization_13&&(this.__NgLocalization_13=new _.a(this._LOCALE_ID_12)),this.__NgLocalization_13},enumerable:!0,configurable:!0}),Object.defineProperty(n.prototype,"_Compiler_14",{get:function(){return null==this.__Compiler_14&&(this.__Compiler_14=new l.d),this.__Compiler_14},enumerable:!0,configurable:!0}),Object.defineProperty(n.prototype,"_APP_ID_15",{get:function(){return null==this.__APP_ID_15&&(this.__APP_ID_15=l.e()),this.__APP_ID_15},enumerable:!0,configurable:!0}),Object.defineProperty(n.prototype,"_IterableDiffers_16",{get:function(){return null==this.__IterableDiffers_16&&(this.__IterableDiffers_16=l.f()),this.__IterableDiffers_16},enumerable:!0,configurable:!0}),Object.defineProperty(n.prototype,"_KeyValueDiffers_17",{get:function(){return null==this.__KeyValueDiffers_17&&(this.__KeyValueDiffers_17=l.g()),this.__KeyValueDiffers_17},enumerable:!0,configurable:!0}),Object.defineProperty(n.prototype,"_DomSanitizer_18",{get:function(){return null==this.__DomSanitizer_18&&(this.__DomSanitizer_18=new r.b(this.parent.get(r.c))),this.__DomSanitizer_18},enumerable:!0,configurable:!0}),Object.defineProperty(n.prototype,"_Sanitizer_19",{get:function(){return null==this.__Sanitizer_19&&(this.__Sanitizer_19=this._DomSanitizer_18),this.__Sanitizer_19},enumerable:!0,configurable:!0}),Object.defineProperty(n.prototype,"_HAMMER_GESTURE_CONFIG_20",{get:function(){return null==this.__HAMMER_GESTURE_CONFIG_20&&(this.__HAMMER_GESTURE_CONFIG_20=new r.d),this.__HAMMER_GESTURE_CONFIG_20},enumerable:!0,configurable:!0}),Object.defineProperty(n.prototype,"_EVENT_MANAGER_PLUGINS_21",{get:function(){return null==this.__EVENT_MANAGER_PLUGINS_21&&(this.__EVENT_MANAGER_PLUGINS_21=[new r.e(this.parent.get(r.c)),new r.f(this.parent.get(r.c)),new r.g(this.parent.get(r.c),this._HAMMER_GESTURE_CONFIG_20)]),this.__EVENT_MANAGER_PLUGINS_21},enumerable:!0,configurable:!0}),Object.defineProperty(n.prototype,"_EventManager_22",{get:function(){return null==this.__EventManager_22&&(this.__EventManager_22=new r.h(this._EVENT_MANAGER_PLUGINS_21,this.parent.get(l.h))),this.__EventManager_22},enumerable:!0,configurable:!0}),Object.defineProperty(n.prototype,"_ɵDomSharedStylesHost_23",{get:function(){return null==this.__ɵDomSharedStylesHost_23&&(this.__ɵDomSharedStylesHost_23=new r.i(this.parent.get(r.c))),this.__ɵDomSharedStylesHost_23},enumerable:!0,configurable:!0}),Object.defineProperty(n.prototype,"_ɵDomRendererFactory2_24",{get:function(){return null==this.__ɵDomRendererFactory2_24&&(this.__ɵDomRendererFactory2_24=new r.j(this._EventManager_22,this._ɵDomSharedStylesHost_23)),this.__ɵDomRendererFactory2_24},enumerable:!0,configurable:!0}),Object.defineProperty(n.prototype,"_RendererFactory2_25",{get:function(){return null==this.__RendererFactory2_25&&(this.__RendererFactory2_25=this._ɵDomRendererFactory2_24),this.__RendererFactory2_25},enumerable:!0,configurable:!0}),Object.defineProperty(n.prototype,"_ɵSharedStylesHost_26",{get:function(){return null==this.__ɵSharedStylesHost_26&&(this.__ɵSharedStylesHost_26=this._ɵDomSharedStylesHost_23),this.__ɵSharedStylesHost_26},enumerable:!0,configurable:!0}),Object.defineProperty(n.prototype,"_Testability_27",{get:function(){return null==this.__Testability_27&&(this.__Testability_27=new l.i(this.parent.get(l.h))),this.__Testability_27},enumerable:!0,configurable:!0}),Object.defineProperty(n.prototype,"_Meta_28",{get:function(){return null==this.__Meta_28&&(this.__Meta_28=new r.k(this.parent.get(r.c))),this.__Meta_28},enumerable:!0,configurable:!0}),Object.defineProperty(n.prototype,"_Title_29",{get:function(){return null==this.__Title_29&&(this.__Title_29=new r.l(this.parent.get(r.c))),this.__Title_29},enumerable:!0,configurable:!0}),Object.defineProperty(n.prototype,"_ɵi_30",{get:function(){return null==this.__ɵi_30&&(this.__ɵi_30=new u.a),this.__ɵi_30},enumerable:!0,configurable:!0}),Object.defineProperty(n.prototype,"_BrowserXhr_31",{get:function(){return null==this.__BrowserXhr_31&&(this.__BrowserXhr_31=new o.a),this.__BrowserXhr_31},enumerable:!0,configurable:!0}),Object.defineProperty(n.prototype,"_ResponseOptions_32",{get:function(){return null==this.__ResponseOptions_32&&(this.__ResponseOptions_32=new o.b),this.__ResponseOptions_32},enumerable:!0,configurable:!0}),Object.defineProperty(n.prototype,"_XSRFStrategy_33",{get:function(){return null==this.__XSRFStrategy_33&&(this.__XSRFStrategy_33=o.c()),this.__XSRFStrategy_33},enumerable:!0,configurable:!0}),Object.defineProperty(n.prototype,"_XHRBackend_34",{get:function(){return null==this.__XHRBackend_34&&(this.__XHRBackend_34=new o.d(this._BrowserXhr_31,this._ResponseOptions_32,this._XSRFStrategy_33)),this.__XHRBackend_34},enumerable:!0,configurable:!0}),Object.defineProperty(n.prototype,"_RequestOptions_35",{get:function(){return null==this.__RequestOptions_35&&(this.__RequestOptions_35=new o.e),this.__RequestOptions_35},enumerable:!0,configurable:!0}),Object.defineProperty(n.prototype,"_Http_36",{get:function(){return null==this.__Http_36&&(this.__Http_36=o.f(this._XHRBackend_34,this._RequestOptions_35)),this.__Http_36},enumerable:!0,configurable:!0}),Object.defineProperty(n.prototype,"_SocketIO_37",{get:function(){return null==this.__SocketIO_37&&(this.__SocketIO_37=new a.a),this.__SocketIO_37},enumerable:!0,configurable:!0}),n.prototype.createInternal=function(){return this._CommonModule_0=new _.b,this._ErrorHandler_1=r.m(),this._APP_INITIALIZER_2=[l.j,r.n(this.parent.get(r.o,null),this.parent.get(l.k,null))],this._ApplicationInitStatus_3=new l.l(this._APP_INITIALIZER_2),this._ɵf_4=new l.m(this.parent.get(l.h),this.parent.get(l.n),this,this._ErrorHandler_1,this.componentFactoryResolver,this._ApplicationInitStatus_3),this._ApplicationRef_5=this._ɵf_4,this._ApplicationModule_6=new l.o(this._ApplicationRef_5),this._BrowserModule_7=new r.p(this.parent.get(r.p,null)),this._ɵba_8=new u.b,this._FormsModule_9=new u.c,this._HttpModule_10=new o.g,this._AppModule_11=new i.a,this._AppModule_11},n.prototype.getInternal=function(t,n){return t===_.b?this._CommonModule_0:t===l.p?this._ErrorHandler_1:t===l.q?this._APP_INITIALIZER_2:t===l.l?this._ApplicationInitStatus_3:t===l.m?this._ɵf_4:t===l.r?this._ApplicationRef_5:t===l.o?this._ApplicationModule_6:t===r.p?this._BrowserModule_7:t===u.b?this._ɵba_8:t===u.c?this._FormsModule_9:t===o.g?this._HttpModule_10:t===i.a?this._AppModule_11:t===l.c?this._LOCALE_ID_12:t===_.c?this._NgLocalization_13:t===l.d?this._Compiler_14:t===l.s?this._APP_ID_15:t===l.t?this._IterableDiffers_16:t===l.u?this._KeyValueDiffers_17:t===r.q?this._DomSanitizer_18:t===l.v?this._Sanitizer_19:t===r.r?this._HAMMER_GESTURE_CONFIG_20:t===r.s?this._EVENT_MANAGER_PLUGINS_21:t===r.h?this._EventManager_22:t===r.i?this._ɵDomSharedStylesHost_23:t===r.j?this._ɵDomRendererFactory2_24:t===l.w?this._RendererFactory2_25:t===r.t?this._ɵSharedStylesHost_26:t===l.i?this._Testability_27:t===r.k?this._Meta_28:t===r.l?this._Title_29:t===u.a?this._ɵi_30:t===o.a?this._BrowserXhr_31:t===o.h?this._ResponseOptions_32:t===o.i?this._XSRFStrategy_33:t===o.d?this._XHRBackend_34:t===o.j?this._RequestOptions_35:t===o.k?this._Http_36:t===a.a?this._SocketIO_37:n},n.prototype.destroyInternal=function(){this._ɵf_4.ngOnDestroy(),this.__ɵDomSharedStylesHost_23&&this._ɵDomSharedStylesHost_23.ngOnDestroy()},n}(l.x),p=new l.y(h,i.a)},l0Vc:function(t,n,e){"use strict";e.d(n,"a",function(){return l});var l=[".result[_ngcontent-%COMP%]{font-size:3em}"]},x35b:function(t,n,e){"use strict";Object.defineProperty(n,"__esModule",{value:!0});var l=e("3j3K"),i=e("kZql"),_=e("Qbdm"),r=e("kke6");i.a.production&&e.i(l.a)(),e.i(_.a)().bootstrapModuleFactory(r.a)}},[1]);