# react-native-local-only-hotspot
This is a react native module to start a local-only hotspot on android devices running android 8 and above

## Getting started

`$ npm install react-native-local-only-hotspot --save`

```
// Following permissions should be added in AndroidManifest.xml
    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE"/>
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>

// Location permission should also be taken from user at runtime
// make sure to check for user's Android API,
// and not starting hotspot again when hotspot is already started.
```

## Usage
```javascript
import LocalOnlyHotspot from 'react-native-local-only-hotspot';

// To start the local-only-hotspot
LocalOnlyHotspot.start()
LocalOnlyHotspot.stop()
LocalOnlyHotspot.getConfig(({ssid, secret}) => console.log('ssid:' + ssid + ' secret:' + secret))
//onData we get a jsonobject with ssid and secret


const eventEmitter = new NativeEventEmitter(NativeModules.LocalOnlyHotspot);

const eventListener = eventEmitter.addListener('LocalOnlyHotspotStarted', 
    ({ssid, secret}) => console.log('ssid:' + ssid + ' secret:' + secret));

const eventListener = eventEmitter.addListener('LocalOnlyHotspotFailed', 
    ({reason}) => console.log('reason:' + reason));

const eventListener = eventEmitter.addListener('LocalOnlyHotspotStopped', 
    ({unexpected}) => console.log('stopped by:' + (unexpected?'unexpected reason':'stop()')));
```



## Refer to example.js for sample code
