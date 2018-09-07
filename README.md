
# react-native-dfp-ana

## Getting started

`$ npm install react-native-dfp-ana --save`

### Mostly automatic installation

`$ react-native link react-native-dfp-ana`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-dfp-ana` and add `RNDfpAna.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNDfpAna.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNDfpAnaPackage;` to the imports at the top of the file
  - Add `new RNDfpAnaPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-dfp-ana'
  	project(':react-native-dfp-ana').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-dfp-ana/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-dfp-ana')
  	```

#### Windows
[Read it! :D](https://github.com/ReactWindows/react-native)

1. In Visual Studio add the `RNDfpAna.sln` in `node_modules/react-native-dfp-ana/windows/RNDfpAna.sln` folder to their solution, reference from their app.
2. Open up your `MainPage.cs` app
  - Add `using Dfp.Ana.RNDfpAna;` to the usings at the top of the file
  - Add `new RNDfpAnaPackage()` to the `List<IReactPackage>` returned by the `Packages` method


## Usage
```javascript
import RNDfpAna from 'react-native-dfp-ana';

// TODO: What to do with the module?
RNDfpAna;
```
  